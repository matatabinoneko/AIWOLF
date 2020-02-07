package com.gmail.shoot7arrow25.player;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.*;

public class NeoElizaMedium extends NeoElizaVillager {
    /** ��\CO���������Ǘ�����t���O */
    boolean isCameout;
    /** ��������Ă��Ȃ���\���ʂ����Ă����҂��s�� */
    Deque<Judge> identQueue = new LinkedList<>();
    /** ��\���ʂ��i�[�����}�b�v */
    Map<Agent, Species> myIdentMap = new HashMap<>();

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        super.initialize(gameInfo, gameSetting);
        isCameout = false;
        identQueue.clear();
        myIdentMap.clear();
    }

    public void dayStart() {
        super.dayStart();
        // ��}���ʂ�҂��s��ɓ����
        Judge ident = currentGameInfo.getMediumResult();
        if (ident != null) {
            identQueue.offer(ident);
            myIdentMap.put(ident.getTarget(), ident.getResult());
        }
    }

    protected void chooseVoteCandidate() {
        /** �����Ă��Ď������_�������Ă��邱�Ƃ𔭌����Ă���G�[�W�F���g�����郊�X�g */
        List<Agent> nonVillagerSide = new ArrayList<>();
        // ��}�t���J�~���O�A�E�g���Ă��鑼�̃G�[�W�F���g�͐l�T���
        for (Agent agent : aliveOthers) {
            if (comingoutMap.get(agent) == Role.MEDIUM) {
                nonVillagerSide.add(agent);
        	}
        }
        // ������P�����ꂽ�G�[�W�F���g��l�T�Ɣ���C���邢�͎����ƈقȂ锻��̐肢�t�͐l�T���
        // TODO: ���񂾃G�[�W�F���g��l�T�Ɣ��肵���肢�t���U�Ƃ��ē��[����̂�Medium�ł͂Ȃ�Villager�Ɏ�����������悢�̂ł͂Ȃ���
        for (Judge j : divinationList) {
            Agent agent = j.getAgent();
            Agent target = j.getTarget();
            if (j.getResult() == Species.WEREWOLF && (target == me || isKilled(target)) || (myIdentMap.containsKey(target) && j.getResult() != myIdentMap.get(target))) {
                if (isAlive(agent) && !werewolves.contains(agent)) {
                    nonVillagerSide.add(agent);
                }
            }
        }
        // �������_�U�̂��Ƃ������Ă���G�[�W�F���g��ނ�A���Ȃ���ΐ����҂̒����烉���_���Œނ�
        if (!nonVillagerSide.isEmpty()) {
            voteCandidate = randomSelect(nonVillagerSide);
        } else {
            voteCandidate = randomSelect(aliveOthers);
        }
    }

    // TODO: �����ƃ��C�����Ȃ���Ȃ��肢�t�������Ƃ����̐l��l�T�����l���A�Ǝ咣����
    // TODO: �����̐肢���ʂ��� -> ��\CO���Ȃ��������������m��Ȃ�
    public String talk() {
        // ���̊m����CO���� 1���ڂ̐肢���ʂ����Ȃ��������m�����グ��
        double border = 0.7;
        double willCo = Math.random(); // Math.random()��1��ڂɓǂ񂾂Ƃ���seed�����̌�g����
        if (!isCameout) {
            // �΍R������ or ��\���ʂ��� or �T�C�R����U����臒l���傫�� �Ȃ��CO����
            if ((isCo(Role.MEDIUM)) || !isCameout && myIdentMap.values().contains(Species.WEREWOLF) || willCo > border) {
                talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.MEDIUM)));
                isCameout = true;
            }
        } else {
            while (!identQueue.isEmpty()) {
                Judge ident = identQueue.poll();
                talkQueue.offer(new Content(new IdentContentBuilder(ident.getTarget(), ident.getResult())));
            }
        }
        return super.talk();
    }
}
