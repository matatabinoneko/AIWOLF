package com.gmail.naglfar.the.on.talk;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

/**
 * 占った相手が黒の場合に、currentVoteTargetに値が入るので、それを使って投票宣言も行う
 */
public class TalkVoteDivined extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (model.currentVoteTarget != null) {
            return new VoteContentBuilder(model.currentVoteTarget.agent);
        }
        return null;
    }

}
