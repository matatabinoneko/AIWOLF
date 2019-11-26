package tkg.aiwolf.target;

import java.util.List;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;
import tkg.aiwolf.model.TFAFGameModel;

public class VoteWithoutWolf extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameAgent> list = game.getAliveOthers();
        Utils.sortByScore(list, model.getEvilScore(), false);
        GameAgent wolfCand = list.get(list.size() - 1);
        return wolfCand;
    }

}
