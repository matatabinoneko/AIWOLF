package com.gmail.romanesco2090.cedec2018impl.target;

import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;

public class VoteWByPo5ver extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        if (game.getDay() == 1) {
            GameAgent me = game.getSelf();
            Set<GameAgent> targets = me.talkList.stream()
                .filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED
                && x.getResult() == Species.WEREWOLF)
                .map(x -> x.getTarget()).filter(x -> x.isAlive).collect(Collectors.toSet());
            GameAgent tar = null;
            for (GameAgent gameAgent : targets) {
                tar = gameAgent;
                break;
            }
            if (tar != null) {
                return tar;
            } else {
                return null;
            }
        }
        return null;
    }

}
