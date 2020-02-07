package jp.or.plala.amail.rin0114.aiwolf.metagame;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.MetagameEventListener;

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
