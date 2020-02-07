package tera.aiwolf.talk;

import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;

/**
 *
 * 別の占い師から白を出されたものに黒判定
 *
 */
public class TalkDivineBlackForWhite extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        if (!me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED).collect(Collectors.toList()).isEmpty()) {
            return null;
        }
        /* 自分以外の占い師が白出ししている者共で生きてる者 */
        Set<GameAgent> whites = game.getAllTalks().filter(x -> x.getTalker() != me && x.getTopic() == Topic.DIVINED && x.getResult() == Species.HUMAN)
            .map(x -> x.getTarget()).filter(x -> x.isAlive).collect(Collectors.toSet());
        /* 自分がすでに判定した者共 */
        Set<GameAgent> my_divined = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED).map(x -> x.getTarget()).filter(x -> x.isAlive).collect(Collectors.toSet());
        whites.removeAll(my_divined);
        Agent tar = null;
        for (GameAgent gameAgent : whites) {
            tar = gameAgent.agent;
            break;
        }
        if (tar != null) {
            return new DivinedResultContentBuilder(tar, Species.WEREWOLF);
        }
        return null;
    }
}
