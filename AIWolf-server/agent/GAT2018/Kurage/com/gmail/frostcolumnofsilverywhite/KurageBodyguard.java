package com.gmail.frostcolumnofsilverywhite;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.*;
import org.aiwolf.common.net.*;

/**
 * 狩人役エージェントクラス
 */
public class KurageBodyguard extends KurageVillager {
    /** 護衛したエージェント */
    Agent guardedAgent;

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        super.initialize(gameInfo, gameSetting);
        guardedAgent = null;
    }
    
    public Agent guard() {
        Agent guardCandidate = null;
        if (guardedAgent != null && isAlive(guardedAgent) && currentGameInfo.getLastDeadAgentList().isEmpty()) {
            guardCandidate = guardedAgent;
        }
        // 新しい護衛先の選定
        else {
            // 占い師をカミングアウトしていて、かつ人狼候補になっていないエージェントを探す
            List<Agent> candidates = new ArrayList<>();
            for (Agent agent : aliveOthers) {
                if (database.getRole(agent) == Role.SEER && !werewolves.contains(agent)) {
                    candidates.add(agent);
                }
            }
            // 見つからなければ霊媒師をカミングアウトしていて、かつ人狼候補になっていないエージェントを探す
            if (candidates.isEmpty()) {
                for (Agent agent : aliveOthers) {
                    if (!werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // それでも見つからなければ、自分と人狼候補以外から護衛
            if (candidates.isEmpty()) {
                for (Agent agent : aliveOthers) {
                    if (!werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // それでもいなければ、自分以外から護衛
            if (candidates.isEmpty()) {
                candidates.addAll(aliveOthers);
            }
            // 護衛候補からランダムに護衛
            guardCandidate = randomSelect(candidates);
        }
        guardedAgent = guardCandidate;
        return guardCandidate;
    }

}
