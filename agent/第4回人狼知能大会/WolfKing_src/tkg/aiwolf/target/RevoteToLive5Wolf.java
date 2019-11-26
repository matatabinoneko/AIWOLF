package tkg.aiwolf.target;

import static jp.ne.sakura.vopaldragon.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import jp.ne.sakura.vopaldragon.aiwolf.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.util.HashCounter;
import tkg.aiwolf.model.TFAFGameModel;

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
