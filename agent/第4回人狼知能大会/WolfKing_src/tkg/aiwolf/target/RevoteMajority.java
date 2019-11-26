package tkg.aiwolf.target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jp.ne.sakura.vopaldragon.aiwolf.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.util.HashCounter;
import tkg.aiwolf.model.TFAFGameModel;
import tkg.aiwolf.model.VoteModel.VoteStatus;
import tkg.aiwolf.model.VoteModel.VoteStatus.VoteGroup;

import static jp.ne.sakura.vopaldragon.aiwolf.util.Utils.log;

public class RevoteMajority extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        //前回の投票から投票状態を作る
        GameEvent lastVotes = game.getLastEventOf(EventType.VOTE);
        VoteStatus vmodel = new VoteStatus();
        lastVotes.votes.forEach(e -> vmodel.set(e.initiator, e.target));

        //
        HashCounter<GameAgent> counts = vmodel.getVoteCount();
        counts.removeCount(game.getSelf());//自分への票は気にしない
        double[] evilScore = model.getEvilScore();
        counts.sort(false);
        log("revote-count", counts);
        List<GameAgent> keys = counts.getKeyList();

        List<VoteGroup> vg = new ArrayList<>();
        int topVoteCount = counts.getCount(counts.getKeyList().get(0));
        for (int i = 0; i < keys.size(); i++) {
            GameAgent target = keys.get(i);
            int count = counts.getCount(target);
            //最大票-1までを考慮する
            if (count >= topVoteCount - 1) {
                vg.add(new VoteGroup(target, evilScore[target.getIndex()]));
            }
        }
        vg.sort(Comparator.naturalOrder());
        log("revote-order", vg);
        return vg.get(0).target;
    }

}
