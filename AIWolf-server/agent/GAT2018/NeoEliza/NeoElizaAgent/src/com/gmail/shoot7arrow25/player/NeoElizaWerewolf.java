package com.gmail.shoot7arrow25.player;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.ArrayList;
import java.util.List;

public class NeoElizaWerewolf extends NeoElizaBasePlayer {
    /** 規定人狼数 */
    int numWolves;
    /** 生存している人狼数 */
    int numAliveWolves;
    /** 人狼リスト */
    List<Agent> werewolves;
    /** 人間(非人狼)リスト */
    List<Agent> humans;
    /** 裏切り者候補リスト */
    List<Agent> possessedList = new ArrayList<>();
    /** パワープレイ(PP)が可能かどうかのフラグ(投票の過半数)
     *　このフラグの有無を生かしたアルゴリズムは未実装
     */
    boolean isPP;

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        super.initialize(gameInfo, gameSetting);
        numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);
        numAliveWolves = numWolves;
        werewolves = new ArrayList<>(gameInfo.getRoleMap().keySet());
        humans = new ArrayList<>();
        for (Agent a : aliveOthers) {
            if (!werewolves.contains(a)) {
                humans.add(a);
            }
        }
        isPP = false;
        possessedList.clear();
    }

    public void dayStart() {
        super.dayStart();
        List<Agent> werewolves = new ArrayList<>(currentGameInfo.getRoleMap().keySet());
        List<Agent> aliveAgents = currentGameInfo.getAliveAgentList();
        List<Agent> tmp = werewolves;
        // 生きている人狼は人狼プレイヤーと生存プレイヤーの積集合
        tmp.retainAll(aliveAgents);
        numAliveWolves = tmp.size();
        if (numAliveWolves + possessedList.size() > aliveAgents.size() / 2) isPP = true;
    }

    public void update(GameInfo gameInfo) {
        super.update(gameInfo);
        // 占い/霊媒結果が嘘の場合，裏切り者候補
        for (Judge j : divinationList) {
            Agent agent = j.getAgent();
            if (!werewolves.contains(agent) && ((humans.contains(j.getTarget()) && j.getResult() == Species.WEREWOLF) || (werewolves.contains(j.getTarget()) && j.getResult() == Species.HUMAN))) {
                if (!possessedList.contains(agent)) {
                    possessedList.add(agent);
                    whisperQueue.offer(new Content(new EstimateContentBuilder(agent, Role.POSSESSED)));
                }
            }
        }
        for (Judge j : identList) {
            Agent agent = j.getAgent();
            if (!werewolves.contains(agent) && ((humans.contains(j.getTarget()) && j.getResult() == Species.WEREWOLF) || (werewolves.contains(j.getTarget()) && j.getResult() == Species.HUMAN))) {
                if (!possessedList.contains(agent)) {
                    possessedList.add(agent);
                    whisperQueue.offer(new Content(new EstimateContentBuilder(agent, Role.POSSESSED)));
                }
            }
        }
    }

    /**
     * 1stマシュマロとして狂人以外に投票する
     */
    protected void chooseVoteCandidate() {
        // TODO: パワープレイ発生時には票を合わせる(talk, updateの実装?)
        List<Agent> candidates = new ArrayList<>();
        for (Agent h : humans) {
            if (!possessedList.contains(h)) {
                candidates.add(h);
            }
        }
        if (!candidates.isEmpty()) {
            // 可能なら狂人以外を襲う
            voteCandidate = randomSelect(candidates);
        } else {
            // 狂人以外がいないのであれば狂人を襲う
            voteCandidate = randomSelect(possessedList);
        }
    }

    /**
     * 1stマシュマロとして狂人以外を襲撃する
     */
    protected void chooseAttackVoteCandidate() {
        List<Agent> candidates = new ArrayList<>();
        for (Agent h : humans) {
            if (!possessedList.contains(h)) {
                candidates.add(h);
            }
        }
        if (!candidates.isEmpty()) {
            // 可能なら狂人以外を襲う
            attackVoteCandidate = randomSelect(candidates);
        } else {
            // 狂人以外がいないのであれば狂人を襲う
            attackVoteCandidate = randomSelect(possessedList);
        }
    }
}
