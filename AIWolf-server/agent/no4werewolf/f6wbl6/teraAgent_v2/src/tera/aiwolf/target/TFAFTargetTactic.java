package tera.aiwolf.target;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameModel;
import tera.aiwolf.framework.TargetTactic;
import tera.aiwolf.model.TFAFGameModel;

public abstract class TFAFTargetTactic extends TargetTactic {

    @Override
    public GameAgent target(GameModel model, Game game) {
        return targetImpl((TFAFGameModel) model, game);
    }

    public abstract GameAgent targetImpl(TFAFGameModel model, Game game);

}
