package jp.or.plala.amail.rin0114.aiwolf.target;

import java.util.List;
import java.util.Set;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.metagame.TFAFMetagameModel;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;

public class DivineByAI extends TFAFTargetTactic {

    Set<GameAgent> divined;

    public DivineByAI(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        if (game.getDay() == 0) {
            List<GameAgent> agents = game.getAliveOthers();
            //最も勝率の高いAgentから一人を抽出
            double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
            GameAgent agent = Utils.getRandom(Utils.getHighestScores(agents, (ag -> winCount[ag.getIndex()])));
            divined.add(agent);
            return agent;
        }
        return null;
    }

}
