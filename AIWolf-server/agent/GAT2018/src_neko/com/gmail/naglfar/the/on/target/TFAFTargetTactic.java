package com.gmail.naglfar.the.on.target;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.framework.GameModel;
import com.gmail.naglfar.the.on.framework.TargetTactic;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public abstract class TFAFTargetTactic extends TargetTactic {

    @Override
    public GameAgent target(GameModel model, Game game) {
        return targetImpl((TFAFGameModel) model, game);
    }

    public abstract GameAgent targetImpl(TFAFGameModel model, Game game);

}
