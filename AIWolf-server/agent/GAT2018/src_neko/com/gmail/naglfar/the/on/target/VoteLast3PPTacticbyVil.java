package com.gmail.naglfar.the.on.target;

import java.util.List;

import org.aiwolf.common.data.Role;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public class VoteLast3PPTacticbyVil extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        //裏切者ではない者を返す
        List<GameAgent> others = game.getAliveOthers();
        for (GameAgent gp : game.getAlives()) {
            if (gp.coRole == Role.POSSESSED) {
                others.remove(gp);
            }
        }

        if (others.size() == 1) {
            GameAgent agent = others.get(0);
            return agent;
        }
        return null;
    }

}
