package com.gmail.romanesco2090.cedec2018impl.target;

import static com.gmail.romanesco2090.util.Utils.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.cedec2018impl.model.VoteModel.VoteStatus;
import com.gmail.romanesco2090.cedec2018impl.model.VoteModel.VoteStatus.VoteGroup;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;
import com.gmail.romanesco2090.util.HashCounter;

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
