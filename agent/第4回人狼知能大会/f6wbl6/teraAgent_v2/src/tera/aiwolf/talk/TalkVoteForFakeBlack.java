package tera.aiwolf.talk;

import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;

/**
 *
 * 自分が「Divined WEREWOLF」だと主張した相手からランダムに一人 vote 宣言対象とする 3か目以降は無効
 */
public class TalkVoteForFakeBlack extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        Set<GameAgent> targets = me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED && x.getResult() == Species.WEREWOLF)
            .map(x -> x.getTarget()).filter(x -> x.isAlive).collect(Collectors.toSet());
        Agent tar = null;
        for (GameAgent gameAgent : targets) {
            tar = gameAgent.agent;
            break;
        }
        if (tar != null) {
            return new VoteContentBuilder(tar);
        } else {
            return null;
        }
    }
}
