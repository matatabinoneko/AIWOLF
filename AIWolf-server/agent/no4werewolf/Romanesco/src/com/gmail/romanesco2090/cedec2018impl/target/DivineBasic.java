package com.gmail.romanesco2090.cedec2018impl.target;

import java.util.List;
import java.util.Set;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.util.Utils;

/**
 * 占いの基本戦術。Evilスコアが高い順に占う
 */
public class DivineBasic extends TFAFTargetTactic {

    private Set<GameAgent> divined;

    public DivineBasic(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameAgent> alives = game.getAliveOthers();
        alives.removeAll(divined);
        if (alives.isEmpty()) return null;
        double[] evilScore = model.getEvilScore();
        Utils.sortByScore(alives, evilScore, false);
        GameAgent target = alives.get(0);
        divined.add(target);
        return target;
    }

}
