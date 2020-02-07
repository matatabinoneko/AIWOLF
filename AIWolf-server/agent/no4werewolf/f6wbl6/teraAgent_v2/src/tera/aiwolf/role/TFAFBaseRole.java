package tera.aiwolf.role;

import tera.aiwolf.framework.AbstractRole;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameModel;
import tera.aiwolf.model.TFAFGameModel;

public abstract class TFAFBaseRole extends AbstractRole {

    public TFAFBaseRole(Game game) {
        super(game);
    }

    @Override
    protected GameModel createModel(Game game) {
        return new TFAFGameModel(game);
    }

}
