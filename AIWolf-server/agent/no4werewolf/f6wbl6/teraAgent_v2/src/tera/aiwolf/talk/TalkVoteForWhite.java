package tera.aiwolf.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;

public class TalkVoteForWhite extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        /* 自分の今日の投票宣言先のリスト */
        List<GameAgent> myVoteTargets = me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE).map(x -> x.getTarget()).collect(Collectors.toList());
        GameAgent tar = null;
        if (!myVoteTargets.isEmpty()) {
            tar = myVoteTargets.get(myVoteTargets.size() - 1);
        }

        List<GameAgent> targets = game.getAliveOthers();
        if (tar != null && targets.contains(tar)) {
            return new SkipContentBuilder();
        } else {
            /* evelscoreの小さい生き物に投票 */
            tar = Collections.min(targets, Comparator.comparing(x -> model.getEvilScore()[x.getIndex()]));
        }
        if (tar != null) {
            return new VoteContentBuilder(tar.agent);
        }
        return null;
    }

}
