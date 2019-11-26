package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.romanesco2090.cedec2018impl.metagame.TFAFMetagameModel;
import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.util.Utils;

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
