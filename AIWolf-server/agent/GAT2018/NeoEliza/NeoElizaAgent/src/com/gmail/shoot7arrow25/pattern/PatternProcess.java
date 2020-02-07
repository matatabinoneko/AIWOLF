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
            // �X�R�A���ő�̃p�^�[������������ꍇ���X�g�ɂ��̂���1�����Ԃ�
            if (entry.getValue().equals(mostProbableScore)) mostProbablePattern = entry.getKey();
        }
        return mostProbablePattern;
    }

    // ���肤��g�ݍ��킹�̃��X�g�̒��������̑g�ݍ��킹���폜
    public HashMap<HashMap<Integer, Role>, Double> removePatternFromPossiblePatterns(HashMap<Integer, Role> pattern, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        if (patternScoreList.keySet().contains(pattern)) patternScoreList.remove(pattern);
        return patternScoreList;
    }

    // �P�����ꂽ�G�[�W�F���g���T�ɂȂ��Ă���g�ݍ��킹���폜
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

    // ���l�ȊO�̖�E��CO�����G�[�W�F���g�͑��l�ł͂Ȃ��ƍl�����邽�߂��̂悤�ȃG�[�W�F���g�����l�ƂȂ�g�ݍ��킹���폜
    public HashMap<HashMap<Integer, Role>, Double> removePatternWithCoVillager(Map<Agent, Role> comingoutMap, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        List<HashMap<Integer, Role>> patternToDelete = new ArrayList<>();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            for (HashMap.Entry<Agent, Role> co : comingoutMap.entrySet()) {
                // ���l�ȊO�̖�ECO�������ꍇ ���̃G�[�W�F���g�����l�Ƃ��Ă���p�^�[������������ ���x�肪�Ȃ��Ɖ���
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

    // CO�̂Ȃ������G�[�W�F���g���肢�ł���g�ݍ��킹���폜 (1���ڏI�����ȍ~�ɂ��̃��\�b�h���Ă�)
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
            // �܂��G�[�W�F���g�S����idx�����, ���̌�肢CO���Ă���G�[�W�F���g��idx�͏���
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

    // �eindex������Agent��Seer�ł���m����Map��Ԃ�
    public Map<Integer, Double> calculateSeerScore(HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        Double totalScore = 0d;
        Map<Integer, Double> agentIdxScore = new HashMap();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            Integer agentIdx = -1; // ���肦�Ȃ��l�ŏ�����
            for (HashMap.Entry<Integer, Role> idRole : pattern.entrySet()) {
                if (idRole.getValue() == Role.SEER) {
                    agentIdx = idRole.getKey();
                }
            }
            Double score = entry.getValue();
            if (agentIdxScore.containsKey(agentIdx)) {
                score += agentIdxScore.get(agentIdx); // ����܂ł̗ݐ�
            }
            agentIdxScore.put(agentIdx, score);
            totalScore += score;
        }
        // score��normalize����
        for (HashMap.Entry<Integer, Double> entry : agentIdxScore.entrySet()) {
            Integer agentIdx = entry.getKey();
            Double score = entry.getValue();
            agentIdxScore.put(agentIdx, score/totalScore);
        }
        return agentIdxScore;
    }
}
