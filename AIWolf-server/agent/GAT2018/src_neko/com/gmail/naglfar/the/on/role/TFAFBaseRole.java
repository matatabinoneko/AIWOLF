package com.gmail.naglfar.the.on.role;

import com.gmail.naglfar.the.on.framework.AbstractRole;
import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameModel;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public abstract class TFAFBaseRole extends AbstractRole {

    public TFAFBaseRole(Game game) {
        super(game);
    }

    @Override
    protected GameModel createModel(Game game) {
        return new TFAFGameModel(game);
    }

}
