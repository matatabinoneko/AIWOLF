package tera.aiwolf.target;

import static tera.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.model.VoteModel.VoteStatus;
import tera.aiwolf.util.HashCounter;
import tera.aiwolf.util.Utils;

public class VoteAsAnnouncedToLive extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        GameAgent currentVote = model.currentVoteTarget;
        model.currentVoteTarget = null;

        //自分が狙われているかチェック
        VoteStatus vmodel = model.voteModel.currentVote();
        HashCounter<GameAgent> counts = vmodel.getVoteCount();
        counts.countMinus(currentVote);//自分の票を抜く
        counts.sort(false);
        int maxVote = counts.getCount(counts.getKeyAt(1));
        if (counts.getCount(game.getSelf()) == maxVote) {
            //自分の命が危険な場合、他に吊られる可能性がある人で最もEvilScoreが高い人に投票
            List<GameAgent> candidates = counts.getKeyList().stream().filter(ga -> !ga.isSelf && counts.getCount(ga) >= maxVote - 1).collect(Collectors.toList());
            if (!candidates.isEmpty()) {
                double[] evilScore = model.getEvilScore();
                Utils.sortByScore(candidates, evilScore, false);
                Utils.sortByScore(candidates, model.voteModel.voteScore(), false);
                log("candidates", candidates);
                currentVote = candidates.get(0);
            }
        }

        return currentVote;
    }
}
