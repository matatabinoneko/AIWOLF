package tkg.aiwolf.target;

import java.util.List;

import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;
import tkg.aiwolf.model.TFAFGameModel;

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
