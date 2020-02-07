package com.gmail.shoot7arrow25.player;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/** ��l�G�[�W�F���g�N���X */
public class NeoElizaBodyguard extends NeoElizaVillager {
    private static final int ENABLE_GJ_CO_DAY = 3;
    private static final int ENABLE_SUSPECTED_CO_DAY = 2;

    private Agent guardedAgent;
    private boolean isCameout;
    private boolean goodJob;
    private boolean goodJobExists;
    private boolean reportDone;
    private boolean estimateDone;
    private List<Agent> fakeBodyguards = new ArrayList<>();

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        super.initialize(gameInfo, gameSetting);

        guardedAgent = null;
        isCameout = false;
    }

    public void dayStart() {
        super.dayStart();
        goodJob = guardedAgent != null && currentGameInfo.getLastDeadAgentList().isEmpty();
        goodJobExists = goodJobExists || goodJob;
        reportDone = false;
        estimateDone = false;
    }

    protected void chooseVoteCandidate() {
        List<Agent> candidates = new ArrayList<>();
        // �U��l
        fakeBodyguards.clear();
        for(Agent a: aliveOthers) {
            if(comingoutMap.get(a) == Role.BODYGUARD) {
                fakeBodyguards.add(a);
            }
        }
        // ���[:�@�ߋ���GJ�Ȃ��ŋU��l or GJ���肩������CO�ςŋU��l
        // GJ����Ŏ�����CO���ĂȂ��̂ɋU��l�ɓ��[�͉����܂��
        // ���l�͋U�肢CO�ɏo�Ă��ċU��lCO�͐l�T�̉\�����������ߓ��[&�l�T���Ƌ^��
        if(!goodJobExists || isCameout) {
            for (Agent a: fakeBodyguards) {
                if (isAlive(a)) {
                    candidates.add(a);
                    if(!estimateDone) {
                        estimateWolf(a);
                    }
                }
            }
            voteCandidate = randomSelect(candidates);
        }else {
            super.chooseVoteCandidate();
        }
    }

    // ��q��w��
    public Agent guard() {
        Agent guardCandidate;
        // �O���q�Ώێ҂������Ă���A����q
        if (guardedAgent != null
                && isAlive(guardedAgent)
                && currentGameInfo.getLastDeadAgentList().isEmpty()) {
            guardCandidate = guardedAgent;
        } else {
            List<Agent> candidates = new ArrayList<>();
            // �肢CO���l�T���łȂ��B�������肢CO���ߋ��ɎE����Ă����珜�O�i���[���[���͌�q���Ȃ� or �UCO��q�h�~)
            if(!deadAgentExists(Role.SEER)) {
                for (Agent agent : aliveOthers) {
                    if (comingoutMap.get(agent) == Role.SEER && !werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // ��}�t�i�肢���l�j
            if (candidates.isEmpty() && !deadAgentExists(Role.MEDIUM)) {
                for (Agent agent : aliveOthers) {
                    if (comingoutMap.get(agent) == Role.MEDIUM && !werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // ����ł�������Ȃ���Ύ����Ɛl�T���ȊO�����q
            if (candidates.isEmpty()) {
                for (Agent agent : aliveOthers) {
                    if (!werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // ����ł����Ȃ���Ύ����ȊO�����q
            if (candidates.isEmpty()) {
                candidates.addAll(aliveOthers);
            }
            // ��q��₩�烉���_���Ɍ�q
            guardCandidate = randomSelect(candidates);
        }
        guardedAgent = guardCandidate;
        return guardCandidate;
    }

    public String talk() {
        if(shouldComeout()) {
            talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.BODYGUARD)));
            isCameout = true;
        }
        // ��q���
        if(guardedAgent != null && isCameout && !reportDone) {
            talkQueue.offer(new Content(new GuardedAgentContentBuilder(guardedAgent)));
            reportDone = true;
        }
        return super.talk();
    }

    private void estimateWolf(Agent agent) {
        talkQueue.offer(new Content(new EstimateContentBuilder(agent, Role.WEREWOLF)));
        estimateDone = true;
    }


    private boolean shouldComeout() {
        // (GJ���ߋ��ɂ���ŋU��l����)��N���ڈȍ~�Ȃ�CO
        // 2���ڈȍ~�Ŏ����������������肢�t����Ȃ�CO
        return !isCameout && (
                (goodJobExists && !fakeBodyguards.isEmpty() && currentGameInfo.getDay() >= ENABLE_GJ_CO_DAY)
                || (currentGameInfo.getDay() >= ENABLE_SUSPECTED_CO_DAY && !werewolves.isEmpty()));
    }

    private boolean deadAgentExists(Role role) {
        boolean deadAgentExists = false;
        for (Agent agent: currentGameInfo.getAgentList()) {
            if (comingoutMap.get(agent) == role && !isAlive(agent)) {
                deadAgentExists = true;
                break;
            }
        }
        return deadAgentExists;

    }

}
