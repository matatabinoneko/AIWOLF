package com.gmail.naglfar.the.on.target;

import static com.gmail.naglfar.the.on.util.Utils.log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gmail.naglfar.the.on.framework.EventType;
import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.framework.GameEvent;
import com.gmail.naglfar.the.on.model.TFAFGameModel;
import com.gmail.naglfar.the.on.model.VoteModel.VoteStatus;
import com.gmail.naglfar.the.on.model.VoteModel.VoteStatus.VoteGroup;
import com.gmail.naglfar.the.on.util.HashCounter;

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
