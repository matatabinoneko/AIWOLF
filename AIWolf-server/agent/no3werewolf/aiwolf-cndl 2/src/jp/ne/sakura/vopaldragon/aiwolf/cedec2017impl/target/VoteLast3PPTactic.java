package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.model.TFAFGameModel;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;

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
