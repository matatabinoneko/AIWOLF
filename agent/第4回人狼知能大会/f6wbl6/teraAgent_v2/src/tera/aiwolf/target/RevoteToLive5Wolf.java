package tera.aiwolf.target;

import static tera.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import tera.aiwolf.framework.EventType;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameEvent;
import tera.aiwolf.framework.GameTalk;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.HashCounter;

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
