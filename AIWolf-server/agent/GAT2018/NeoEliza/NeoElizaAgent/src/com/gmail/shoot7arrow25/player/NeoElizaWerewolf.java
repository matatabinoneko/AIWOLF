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
    /** �K��l�T�� */
    int numWolves;
    /** �������Ă���l�T�� */
    int numAliveWolves;
    /** �l�T���X�g */
    List<Agent> werewolves;
    /** �l��(��l�T)���X�g */
    List<Agent> humans;
    /** ���؂�Ҍ�⃊�X�g */
    List<Agent> possessedList = new ArrayList<>();
    /** �p���[�v���C(PP)���\���ǂ����̃t���O(���[�̉ߔ���)
     *�@���̃t���O�̗L���𐶂������A���S���Y���͖�����
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
        // �����Ă���l�T�͐l�T�v���C���[�Ɛ����v���C���[�̐ϏW��
        tmp.retainAll(aliveAgents);
        numAliveWolves = tmp.size();
        if (numAliveWolves + possessedList.size() > aliveAgents.size() / 2) isPP = true;
    }

    public void update(GameInfo gameInfo) {
        super.update(gameInfo);
        // �肢/��}���ʂ��R�̏ꍇ�C���؂�Ҍ��
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
     * 1st�}�V���}���Ƃ��ċ��l�ȊO�ɓ��[����
     */
    protected void chooseVoteCandidate() {
        // TODO: �p���[�v���C�������ɂ͕[�����킹��(talk, update�̎���?)
        List<Agent> candidates = new ArrayList<>();
        for (Agent h : humans) {
            if (!possessedList.contains(h)) {
                candidates.add(h);
            }
        }
        if (!candidates.isEmpty()) {
            // �\�Ȃ狶�l�ȊO���P��
            voteCandidate = randomSelect(candidates);
        } else {
            // ���l�ȊO�����Ȃ��̂ł���΋��l���P��
            voteCandidate = randomSelect(possessedList);
        }
    }

    /**
     * 1st�}�V���}���Ƃ��ċ��l�ȊO���P������
     */
    protected void chooseAttackVoteCandidate() {
        List<Agent> candidates = new ArrayList<>();
        for (Agent h : humans) {
            if (!possessedList.contains(h)) {
                candidates.add(h);
            }
        }
        if (!candidates.isEmpty()) {
            // �\�Ȃ狶�l�ȊO���P��
            attackVoteCandidate = randomSelect(candidates);
        } else {
            // ���l�ȊO�����Ȃ��̂ł���΋��l���P��
            attackVoteCandidate = randomSelect(possessedList);
        }
    }
}
