package tera.aiwolf.target;

import java.util.List;

import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.EventType;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameEvent;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.Utils;

public class VoteWolfbySeer extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        if (divineTarget != null && result == Species.WEREWOLF) {
            return divineTarget;
        } else {
            // 白を除く、最も狼らしいAgentに投票宣言
            List<GameAgent> list = game.getAliveOthers();
            if (divineTarget != null) {
                list.remove(divineTarget);
            }
            Utils.sortByScore(list, model.getEvilScore(), false);
            GameAgent wolfCand = list.get(0);
            return wolfCand;
        }
    }

    private GameAgent divineTarget;
    private Species result;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DIVINE) {
            divineTarget = e.target;
            result = e.species;
        }
    }
}
