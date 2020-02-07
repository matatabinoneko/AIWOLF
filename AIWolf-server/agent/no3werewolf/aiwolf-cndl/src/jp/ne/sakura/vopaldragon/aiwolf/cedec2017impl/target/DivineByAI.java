package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target;

import java.util.List;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.metagame.TFAFMetagameModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.model.TFAFGameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;

public class DivineByAI extends TFAFTargetTactic {

    Set<GameAgent> divined;

    public DivineByAI(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        if (game.getDay() == 0) {
            List<GameAgent> agents = game.getAliveOthers();
            //最も勝率の高いAgentから一人を抽出
            double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
            GameAgent agent = Utils.getRandom(Utils.getHighestScores(agents, (ag -> winCount[ag.getIndex()])));
            divined.add(agent);
            return agent;
        }
        return null;
    }

}
