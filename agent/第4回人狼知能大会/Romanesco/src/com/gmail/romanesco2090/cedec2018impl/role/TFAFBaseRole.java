package com.gmail.romanesco2090.cedec2018impl.role;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.AbstractRole;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameModel;

public abstract class TFAFBaseRole extends AbstractRole {

    public TFAFBaseRole(Game game) {
        super(game);
    }

    @Override
    protected GameModel createModel(Game game) {
        return new TFAFGameModel(game);
    }

}
