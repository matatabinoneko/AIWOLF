package tera.aiwolf.model;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameModel;
import tera.aiwolf.metagame.TFAFMetagameModel;
import tera.aiwolf.util.VectorMath;

/**
 * cedec2017用のゲームモデル
 */
public class TFAFGameModel extends GameModel {

    //各種モデル
    public AgentReliabilityModel agentReliabilityModel;
    public VoteModel voteModel;
    public BelieveSeerModel divinerModel;

    //各種状態
    public GameAgent currentVoteTarget;

    public TFAFGameModel(Game game) {
        super(game);
        agentReliabilityModel = new AgentReliabilityModel(game);
        voteModel = new VoteModel(game);
        divinerModel = new BelieveSeerModel(game, agentReliabilityModel);
    }

    private boolean once3seer = false;
    private boolean once2medium = false;

    /**
     * 狼らしさを推定するスコア。信頼性を加味した占い・霊媒の情報と役割確率を足し合わせ、いくつかの救済条項を追加。
     */
    public double[] getEvilScore() {

        double[] divinerScore = divinerModel.getScore();

        double[] tfScore = getRoleProbability(Role.WEREWOLF);

        double[] integratedScore = VectorMath.addAll(divinerScore, tfScore);

        if (game.getDay() <= 4) {
            //占いCOが二人までなら、4日目まで占い師は見逃す
            if (game.getSelf().role != Role.SEER && !once3seer) {
                List<GameAgent> seers = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.SEER).collect(Collectors.toList());
                if (seers.size() <= 2) {
                    for (GameAgent ag : seers) {
                        integratedScore[ag.getIndex()] = 0;
                    }
                } else {
                    once3seer = true;
                }
            }

            //霊媒COが一人までなら、4日目までは霊媒は見逃す
            if (game.getSelf().role != Role.MEDIUM && !once2medium) {
                List<GameAgent> mediums = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.MEDIUM).collect(Collectors.toList());
                if (mediums.size() <= 1) {
                    for (GameAgent ag : mediums) {
                        integratedScore[ag.getIndex()] = 0;
                    }
                } else {
                    once2medium = true;
                }
            }
        }

        //信頼性が一定値以上のAgentはEvilではない()
        double[] relScore = agentReliabilityModel.getReliability();
        for (GameAgent ag : game.getAliveOthers()) {
            if (relScore[ag.getIndex()] >= 1.3 && ag.coRole == Role.SEER) {
                integratedScore[ag.getIndex()] = 0;
            }
        }

        //自分が狼で無い場合には、スコアを強制的に0に
        if (game.getSelf().role != Role.WEREWOLF) {
            integratedScore[game.getSelf().getIndex()] = 0;
        }
        return VectorMath.normalize(integratedScore);
    }

    /**
     * 信頼性と役割確率を使って、占い師らしさを求める
     */
    public double[] getSeerScore() {
        double[] tfScore = getRoleProbability(Role.SEER);
        double[] relScore = agentReliabilityModel.getScore();
        double[] score = VectorMath.addAll(tfScore, relScore);
        VectorMath.normalize(score);
        return score;
    }

    private TFAFMetagameModel getMetaModel() {
        return game.getMeta();
    }

    /**
     * TFとAFを使って特定の役割の確率っぽいスコアを求める。
     */
    public double[] getRoleProbability(Role role) {
        double[] tfScore = new double[game.getVillageSize()];
        double[] afScore = new double[game.getVillageSize()];
        game.getAgentStream().filter(ag -> !ag.isSelf).forEach(ag -> afScore[ag.getIndex()] = getMetaModel().actFrequencyModel.getRoleProbability(ag, role));
        //if(game.getVillageSize()==5){
        //game.getAgentStream().filter(ag -> !ag.isSelf).forEach(ag -> tfScore[ag.getIndex()] = getMetaModel().talkFrequencyModel.getRoleProbability(ag, role));
        //}else{
            game.getAgentStream().filter(ag -> !ag.isSelf).forEach(ag -> tfScore[ag.getIndex()] = getMetaModel().talkFrequencyModel2gram.getRoleProbability(ag, role));
        //}
        double[] score = VectorMath.addAll(VectorMath.normalize(tfScore), VectorMath.normalize(afScore));
        return VectorMath.normalize(score);
    }
}
