package tera.aiwolf.target;

import java.util.List;

import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;

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
