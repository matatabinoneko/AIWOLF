package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.util.Utils;

/**
 * 最も狼らしいAgentに投票する
 *
 */
public class TalkVoteWolf extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (model.currentVoteTarget == null) {
            //最も狼らしいAgentに投票宣言
            List<GameAgent> list = game.getAliveOthers();
            Utils.sortByScore(list, model.getEvilScore(), false);
            GameAgent wolfCand = list.get(0);
            model.currentVoteTarget = wolfCand;
            return new VoteContentBuilder(wolfCand.agent);
        }
        return null;
    }
}
