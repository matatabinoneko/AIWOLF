package jp.or.plala.amail.rin0114.aiwolf.target;

import static jp.or.plala.amail.rin0114.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameTalk;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.model.VoteModel.VoteStatus;
import jp.or.plala.amail.rin0114.aiwolf.util.HashCounter;

public class VoteToLive5Wolf extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameTalk> myVotes = game.getSelf().talkList.stream().filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.VOTE).collect(Collectors.toList());

        VoteStatus vmodel = model.voteModel.currentVote();
        HashCounter<GameAgent> counts = vmodel.getVoteCount();

        GameAgent target = null;
        if (!myVotes.isEmpty()) {
            target = myVotes.get(myVotes.size() - 1).getTarget();
            counts.countMinus(target);//自分の票を抜く
        }

        counts.sort(false);
        int maxVote = counts.getCount(counts.getKeyAt(1));

        log("counts", counts);
        log("who-vote-who", vmodel.whoVoteWhoMap);
        log("maxVote", maxVote);

        //宣言した対象が票を稼いでいるならそいつに投票
        if (counts.getCount(target) >= maxVote - 1) {
            return target;
        }
        //そうでないなら自分以外で得票数が最も高いエージェントに投票
        for (GameAgent ag : counts.getKeyList()) {
            log(ag, counts.getCount(ag), maxVote);
            if (!ag.isSelf) return ag;
        }
        return null;
    }

}
