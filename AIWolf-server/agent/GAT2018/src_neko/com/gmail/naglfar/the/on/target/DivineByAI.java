package com.gmail.naglfar.the.on.target;

import java.util.List;
import java.util.Set;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.metagame.TFAFMetagameModel;
import com.gmail.naglfar.the.on.model.TFAFGameModel;
import com.gmail.naglfar.the.on.util.Utils;

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
