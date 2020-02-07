package jp.or.plala.amail.rin0114.aiwolf.target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameEvent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.model.VoteModel.VoteStatus;
import jp.or.plala.amail.rin0114.aiwolf.model.VoteModel.VoteStatus.VoteGroup;
import jp.or.plala.amail.rin0114.aiwolf.util.HashCounter;
import static jp.or.plala.amail.rin0114.aiwolf.util.Utils.*;

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
