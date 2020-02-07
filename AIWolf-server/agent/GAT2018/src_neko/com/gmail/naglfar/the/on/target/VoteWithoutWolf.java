package com.gmail.naglfar.the.on.target;

import java.util.List;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.model.TFAFGameModel;
import com.gmail.naglfar.the.on.util.Utils;

public class VoteWithoutWolf extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameAgent> list = game.getAliveOthers();
        Utils.sortByScore(list, model.getEvilScore(), false);
        GameAgent wolfCand = list.get(list.size() - 1);
        return wolfCand;
    }

}
