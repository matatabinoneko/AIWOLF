package tkg.aiwolf.target;

import java.util.List;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;
import static jp.ne.sakura.vopaldragon.aiwolf.util.Utils.log;
import jp.ne.sakura.vopaldragon.aiwolf.util.VectorMath;
import tkg.aiwolf.model.TFAFGameModel;

import org.aiwolf.common.data.Role;

public class GuardBasic extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        ListMap<Role, GameAgent> coMap = new ListMap<>();
        game.getAgentStream().filter(ag -> ag.isAlive && ag.hasCO()).forEach(ag -> coMap.add(ag.coRole, ag));
        log("co-map", coMap);
        int seerNum = coMap.getList(Role.SEER).size();
        int medNum = coMap.getList(Role.MEDIUM).size();
        double[] reliablityScore = model.getSeerScore();
        double[] evilScore = model.getEvilScore();
        GameAgent guardTarget = null;
        if (seerNum >= 1) {
            List<GameAgent> list = coMap.getList(Role.SEER);
            Utils.sortByScore(list, reliablityScore, false);
            log("seer score", list);
            guardTarget = list.get(0);
        } else if (seerNum == 0 && medNum >= 1) {
            List<GameAgent> list = coMap.getList(Role.MEDIUM);
            Utils.sortByScore(list, reliablityScore, false);
            log("med score", list);
            guardTarget = list.get(0);
        }

        //4日目以降で、evilScoreが平均値以上の場合、やっぱり守らない
        if (guardTarget != null && game.getDay() >= 4 && evilScore[guardTarget.getIndex()] > VectorMath.ave(evilScore)) {
            log("stop gurading evil", guardTarget);
            guardTarget = null;
        }

        //守る相手が決まっていない場合、evilScoreの低い人を守る
        if (guardTarget == null) {
            List<GameAgent> aliveOthers = game.getAliveOthers();
            Utils.sortByScore(aliveOthers, evilScore, true);
            log("all score", aliveOthers);
            guardTarget = aliveOthers.get(0);
        }

        return guardTarget;
    }

}
