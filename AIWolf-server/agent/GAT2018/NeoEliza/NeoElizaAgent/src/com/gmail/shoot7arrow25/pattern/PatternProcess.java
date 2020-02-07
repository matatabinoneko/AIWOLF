package com.gmail.shoot7arrow25.pattern;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.max;

public class PatternProcess {

    public HashMap<Integer, Role> getMostProbablePattern(HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        Double mostProbableScore = max(patternScoreList.values());
        HashMap<Integer, Role> mostProbablePattern = new HashMap<>();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            // スコアが最大のパターンが複数ある場合リストにそのうち1つだけ返す
            if (entry.getValue().equals(mostProbableScore)) mostProbablePattern = entry.getKey();
        }
        return mostProbablePattern;
    }

    // ありうる組み合わせのリストの中から特定の組み合わせを削除
    public HashMap<HashMap<Integer, Role>, Double> removePatternFromPossiblePatterns(HashMap<Integer, Role> pattern, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        if (patternScoreList.keySet().contains(pattern)) patternScoreList.remove(pattern);
        return patternScoreList;
    }

    // 襲撃されたエージェントが狼になっている組み合わせを削除
    public HashMap<HashMap<Integer, Role>, Double> removePatternWithAttackedWerewolf(List<Agent> killedAgents, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        List<HashMap<Integer, Role>> patternToDelete = new ArrayList<>();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            for (Agent attackedAg: killedAgents) {
                Integer attackedIdx = attackedAg.getAgentIdx();
                if (pattern.containsKey(attackedIdx)) {
                  if (pattern.get(attackedIdx) == Role.WEREWOLF) {
                      patternToDelete.add(pattern);
                  }
                }
            }
        }
        for (HashMap<Integer, Role> pattern : patternToDelete) {
            patternScoreList = removePatternFromPossiblePatterns(pattern, patternScoreList);
        }
        return patternScoreList;
    }

    // 村人以外の役職をCOしたエージェントは村人ではないと考えられるためそのようなエージェントが村人となる組み合わせを削除
    public HashMap<HashMap<Integer, Role>, Double> removePatternWithCoVillager(Map<Agent, Role> comingoutMap, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        List<HashMap<Integer, Role>> patternToDelete = new ArrayList<>();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            for (HashMap.Entry<Agent, Role> co : comingoutMap.entrySet()) {
                // 村人以外の役職COをした場合 そのエージェントが村人としているパターンを消去する 村騙りがないと仮定
                if (co.getValue() != Role.VILLAGER) {
                    Integer agentWithCoIdx = co.getKey().getAgentIdx();
                    if (pattern.get(agentWithCoIdx) == Role.VILLAGER) {
                        patternToDelete.add(pattern);
                    }
                }
            }
        }
        for (HashMap<Integer, Role> pattern : patternToDelete) {
            patternScoreList = removePatternFromPossiblePatterns(pattern, patternScoreList);
        }
        return patternScoreList;
    }

    // COのなかったエージェントが占いである組み合わせを削除 (1日目終了時以降にこのメソッドを呼ぶ)
    public HashMap<HashMap<Integer, Role>, Double> removePatternWithoutCoSeer(Map<Agent, Role> comingoutMap, Integer numAgents, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        List<HashMap<Integer, Role>> patternToDelete = new ArrayList<>();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            List<Integer> candidateSeerWithCo = new ArrayList<>();
            for (HashMap.Entry<Agent, Role> co : comingoutMap.entrySet()) {
                if (co.getValue() == Role.SEER) {
                    candidateSeerWithCo.add(co.getKey().getAgentIdx());
                }
            }
            List<Integer> agentIdxNoSeerCo = new ArrayList<>();
            // まずエージェント全員のidxを入れ, その後占いCOしているエージェントのidxは除く
            for (int i = 1; i <= numAgents; i++) {
                agentIdxNoSeerCo.add(i);
            }
            for (Integer candidateSeer: candidateSeerWithCo) {
                agentIdxNoSeerCo.remove(candidateSeer);
            }
            for (Integer idxNoSeerCo: agentIdxNoSeerCo) {
                if (pattern.containsKey(idxNoSeerCo)) {
                    if (pattern.get(idxNoSeerCo) == Role.SEER) {
                        patternToDelete.add(pattern);
                    }
                }
            }
        }
        for (HashMap<Integer, Role> pattern : patternToDelete) {
            patternScoreList = removePatternFromPossiblePatterns(pattern, patternScoreList);
        }
        return patternScoreList;
    }

    // 各indexを持つAgentがSeerである確率のMapを返す
    public Map<Integer, Double> calculateSeerScore(HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        Double totalScore = 0d;
        Map<Integer, Double> agentIdxScore = new HashMap();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            Integer agentIdx = -1; // ありえない値で初期化
            for (HashMap.Entry<Integer, Role> idRole : pattern.entrySet()) {
                if (idRole.getValue() == Role.SEER) {
                    agentIdx = idRole.getKey();
                }
            }
            Double score = entry.getValue();
            if (agentIdxScore.containsKey(agentIdx)) {
                score += agentIdxScore.get(agentIdx); // これまでの累積
            }
            agentIdxScore.put(agentIdx, score);
            totalScore += score;
        }
        // scoreをnormalizeする
        for (HashMap.Entry<Integer, Double> entry : agentIdxScore.entrySet()) {
            Integer agentIdx = entry.getKey();
            Double score = entry.getValue();
            agentIdxScore.put(agentIdx, score/totalScore);
        }
        return agentIdxScore;
    }
}
