package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.util.Utils;

public class TalkDivineWhiteRandom extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        if (!me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED).collect(Collectors.toList()).isEmpty()) {
            return null;
        }
        Set<GameAgent> my_divine = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED).map(x -> x.getTarget()).collect(Collectors.toSet());
        Agent ret = Utils.getRandom(game.getAliveOthers().stream().filter(x -> !my_divine.contains(x)).collect(Collectors.toList())).agent;
        return new DivinedResultContentBuilder(ret, Species.HUMAN);
    }

}
