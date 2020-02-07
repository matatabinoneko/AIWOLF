package com.gmail.naglfar.the.on.metagame;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.framework.MetagameEventListener;

/**
 * 各Agentの勝利回数を数えるだけのモデル
 */
public class WinCountModel implements MetagameEventListener {

    private double[] winCount;

    public double[] getWinCount() {
        if (winCount == null) return new double[vilSize];
        return winCount;
    }

    private int vilSize = 0;

    @Override
    public void startGame(Game g) {
        vilSize = g.getVillageSize();
    }

    @Override
    public void endGame(Game g) {
        if (winCount == null) {
            winCount = new double[vilSize];
        }
        for (GameAgent agent : g.getAgents()) {
            if (agent.role.getTeam() == g.getWonTeam()) winCount[agent.getIndex()]++;
        }
    }

}
