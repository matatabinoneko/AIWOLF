package tera.aiwolf.target;

import java.util.List;
import java.util.Set;

import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.Utils;

/**
 * 占いの基本戦術。Evilスコアが高い順に占うが、SEERCOしている人は飛ばす。DivineWithEvilScoreと併用して、SEER-COのやつは吊り、ステルス人狼を探す
 */
public class DivineBasicAvoidSeer extends TFAFTargetTactic {

    private Set<GameAgent> divined;

    public DivineBasicAvoidSeer(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameAgent> alives = game.getAliveOthers();
        alives.removeAll(divined);
        if (alives.isEmpty()) return null;
        double[] evilScore = model.getEvilScore();
        Utils.sortByScore(alives, evilScore, false);

        long seerCount = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.SEER).count();
        long medCount = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.MEDIUM).count();
        for (GameAgent target : alives) {
            //SEERのCOしているやつは吊ればいいので、占わない
            if (seerCount <= 4 && target.coRole == Role.SEER) continue;
            //MEDIUM一人の場合はだいたい本物なので、占わない
            if (medCount == 1 && target.coRole == Role.MEDIUM) continue;
            divined.add(target);
            return target;
        }
        return null;
    }

}
