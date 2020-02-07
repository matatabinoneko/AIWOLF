package jp.or.plala.amail.rin0114.aiwolf.talk;

import java.util.List;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.metagame.TFAFMetagameModel;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

/**
 * 初日の最初に、最も勝率の高いAgentに対して投票宣言を行う（同数の場合にはランダム）
 */
public class TalkVoteByAI extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getDay() == 1 && turn == 0) {
            List<GameAgent> agents = game.getAliveOthers();
            //最も勝率の高いAgentから一人を抽出
            double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
            GameAgent agent = Utils.getRandom(Utils.getHighestScores(agents, (ag -> winCount[ag.getIndex()])));
            model.currentVoteTarget = agent;
            return new VoteContentBuilder(agent.agent);
        }
        return null;
    }

}
