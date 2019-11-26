package com.gmail.romanesco2090.cedec2018impl.target;

import java.util.List;
import java.util.Set;

import com.gmail.romanesco2090.cedec2018impl.metagame.TFAFMetagameModel;
import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.util.Utils;

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
