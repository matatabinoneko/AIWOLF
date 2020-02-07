package jp.or.plala.amail.rin0114.aiwolf.target;

import static jp.or.plala.amail.rin0114.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameEvent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameTalk;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.HashCounter;

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
