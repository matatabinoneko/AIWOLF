package com.gmail.naglfar.the.on.target;

import org.aiwolf.common.data.Role;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public class VoteLast3PPTactic extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        if (game.getAlives().size() == 3) {
            for (GameAgent gp : game.getAlives()) {
                if (!gp.isSelf && gp.coRole != Role.WEREWOLF) {
                    return gp;
                }
            }
        }
        return null;
    }

}
