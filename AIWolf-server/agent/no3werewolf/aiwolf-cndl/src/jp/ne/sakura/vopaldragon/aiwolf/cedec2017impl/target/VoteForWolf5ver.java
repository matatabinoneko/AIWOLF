package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target;

import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.model.TFAFGameModel;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;

public class VoteForWolf5ver extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameAgent> list = game.getAliveOthers();
        Utils.sortByScore(list, model.getEvilScore(), false);
        GameAgent wolfCand = list.get(0);
        return wolfCand;
    }

}
