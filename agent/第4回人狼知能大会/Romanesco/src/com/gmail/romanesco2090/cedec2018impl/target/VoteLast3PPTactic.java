package com.gmail.romanesco2090.cedec2018impl.target;

import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;

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
