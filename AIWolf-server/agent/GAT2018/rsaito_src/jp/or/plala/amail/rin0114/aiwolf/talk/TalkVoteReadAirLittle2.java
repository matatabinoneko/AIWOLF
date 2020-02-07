package jp.or.plala.amail.rin0114.aiwolf.talk;

import java.util.List;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.model.VoteModel.VoteStatus;
import jp.or.plala.amail.rin0114.aiwolf.util.HashCounter;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;
import static jp.or.plala.amail.rin0114.aiwolf.util.Utils.*;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

public class TalkVoteReadAirLittle2 extends TFAFTalkTactic {

    static int possibleMaximumWolf(int aliveNum) {
        return Math.min(3,
            aliveNum % 2 == 0
                ? aliveNum / 2 - 1
                : aliveNum / 2
        );
    }

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent currentVote = model.currentVoteTarget;
        VoteStatus vmodel = model.voteModel.currentVote();
        HashCounter<GameAgent> counts = vmodel.getVoteCount();
        counts.countMinus(currentVote);//自分の票を抜く

        counts.sort(false);
        int maxVote = counts.getCount(counts.getKeyAt(1));

        log("counts", counts);
        log("who-vote-who", vmodel.whoVoteWhoMap);
        log("maxVote", maxVote);

        int maxWolf = possibleMaximumWolf(game.getAliveSize());

        List<GameAgent> candidates = game.getAliveOthers();
        if (!candidates.isEmpty()) {
            double[] evilScore = model.getEvilScore();
            Utils.sortByScore(candidates, evilScore, false);
            log("candidates", candidates);
            //es最大～狼最大数番目までの対象が最大得票ないしは1票差の場合そいつ。
            GameAgent newTarget = null;
            for (int i = 0; i < maxWolf; i++) {
                GameAgent candidate = candidates.get(i);
                int vote = counts.getCount(candidate);
                if (maxVote - vote <= 1) {
                    newTarget = candidate;
                    break;
                }
            }
            //そうで無い場合、諸条件は無視してes最大値のやつ
            if (newTarget == null) {
                newTarget = candidates.get(0);
            }

            if (currentVote != newTarget) {
                model.currentVoteTarget = newTarget;
                return new VoteContentBuilder(newTarget.agent);
            }
        }
        return null;
    }

}
