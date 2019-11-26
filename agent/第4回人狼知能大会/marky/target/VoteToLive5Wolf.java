package tkg.aiwolf.target;

import static jp.ne.sakura.vopaldragon.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.util.HashCounter;
import tkg.aiwolf.model.TFAFGameModel;
import tkg.aiwolf.model.VoteModel.VoteStatus;

public class VoteToLive5Wolf extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {

    	//もし自分以外に黒出しされてるやつがいたらそいつに投票
        List<GameTalk> divine = game.getAllTalks().filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.DIVINED).collect(Collectors.toList());
        for (GameTalk t : divine) {
            if (!t.getTarget().isSelf && t.getResult() == Species.WEREWOLF) {
            	return t.getTarget();
            }
        }

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
