package tera.aiwolf.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;

/**
 *
 * Vote 及び Request(Vote) のターゲットとしてより多くの票が入っているエージェントを Vote対象にする。 すでに自分が3つ黒出しをしているなら、その3人から選ぶ。
 */
public class TalkVoteMajority extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();

        /* 自分のこれまでの投票宣言先のリスト */
        List<GameAgent> myVoteTargets = me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE).map(x -> x.getTarget()).collect(Collectors.toList());
        GameAgent myLastVote = null;
        if (!myVoteTargets.isEmpty()) {
            myLastVote = myVoteTargets.get(myVoteTargets.size() - 1);
        }

        /* エージェント別投票宣言数のカウント */
        Map<GameAgent, Integer> votes = new HashMap<>();
        Set<GameAgent> myDivine = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED && x.getResult() == Species.WEREWOLF).map(x -> x.getTarget()).collect(Collectors.toSet());
        if (myDivine.size() >= 3) {
            myDivine.removeIf(x -> !x.isAlive);
            for (GameAgent ga : myDivine) {
                votes.put(ga, 0);
            }
        } else {
            for (GameAgent ga : game.getAliveOthers()) {
                votes.put(ga, 0);
            }
        }
        game.getAllTalks().filter(x -> x.getDay() == game.getDay()).filter(x -> x.getTopic() == Topic.VOTE) // REQUEST に関する処理は未実装
            .map(x -> x.getTarget()).forEach(x -> {
            if (votes.containsKey(x)) {
                votes.put(x, votes.get(x) + 1);
            }
        });

        /* 最大被投票者 */
        GameAgent tar = Collections.max(votes.keySet(), Comparator.comparing(x -> votes.get(x)));
        if (tar != null && tar != myLastVote) {
            return new VoteContentBuilder(tar.agent);
        } else {
            return new SkipContentBuilder();
        }
    }

}
