package jp.or.plala.amail.rin0114.aiwolf.target;

import java.util.List;
import java.util.Set;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;

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
