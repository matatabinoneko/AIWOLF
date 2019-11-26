package com.gmail.romanesco2090.cedec2018impl.target;

import static com.gmail.romanesco2090.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;
import com.gmail.romanesco2090.framework.GameTalk;
import com.gmail.romanesco2090.util.HashCounter;

public class RevoteToLive5Wolf extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        GameEvent lastVotes = game.getLastEventOf(EventType.VOTE);
        HashCounter<GameAgent> voteCount = new HashCounter<>();
        lastVotes.votes.stream().filter(gv -> !gv.initiator.isSelf).forEach(e -> voteCount.countPlus(e.target));
        voteCount.removeCount(game.getSelf());//自分への票は気にしない
        voteCount.sort(false);
        log("revote-count", voteCount);

        if (voteCount.getKeyList().isEmpty()) {
            List<GameTalk> myVotes = game.getSelf().talkList.stream().filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.VOTE).collect(Collectors.toList());
            if (!myVotes.isEmpty()) {
                return myVotes.get(myVotes.size() - 1).getTarget();
            }
        }
        return voteCount.getKeyAt(1);
    }

}
