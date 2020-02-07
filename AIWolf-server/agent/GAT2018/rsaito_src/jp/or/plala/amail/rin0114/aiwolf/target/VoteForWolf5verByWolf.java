package jp.or.plala.amail.rin0114.aiwolf.target;

import java.util.List;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;

public class VoteForWolf5verByWolf extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        if (game.getDay() == 1) {
            List<GameAgent> list = game.getAliveOthers();
            Utils.sortByScore(list, model.getEvilScore(), false);
            GameAgent wolfCand = list.get(0);
            return wolfCand;
        }
        return null;
    }

}
