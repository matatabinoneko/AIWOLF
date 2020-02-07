package com.gmail.romanesco2090.cedec2018impl.talk;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;

/**
 * 占った相手が黒と発言した場合に、currentVoteTargetに値が入るので、それを使って投票宣言も行う
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
