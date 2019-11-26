package com.gmail.romanesco2090.cedec2018impl.target;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameModel;
import com.gmail.romanesco2090.framework.TargetTactic;

public abstract class TFAFTargetTactic extends TargetTactic {

    @Override
    public GameAgent target(GameModel model, Game game) {
        return targetImpl((TFAFGameModel) model, game);
    }

    public abstract GameAgent targetImpl(TFAFGameModel model, Game game);

}
