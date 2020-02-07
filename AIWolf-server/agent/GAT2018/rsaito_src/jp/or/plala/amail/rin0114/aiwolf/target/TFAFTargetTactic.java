package jp.or.plala.amail.rin0114.aiwolf.target;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameModel;
import jp.or.plala.amail.rin0114.aiwolf.framework.TargetTactic;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;

public abstract class TFAFTargetTactic extends TargetTactic {

    @Override
    public GameAgent target(GameModel model, Game game) {
        return targetImpl((TFAFGameModel) model, game);
    }

    public abstract GameAgent targetImpl(TFAFGameModel model, Game game);

}
