package com.gmail.shoot7arrow25.player;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.*;

/** �肢�t���G�[�W�F���g�N���X */
public class NeoElizaSeer extends NeoElizaVillager {

    boolean isCameout;
    Deque<Judge> divinationQueue = new LinkedList<>();
    Map<Agent, Species> myDivinationMap = new HashMap<>();
    List<Agent> whiteList = new ArrayList<>();
    List<Agent> blackList = new ArrayList<>();
    List<Agent> grayList;
    List<Agent> semiWolves = new ArrayList<>();
    List<Agent> possessedList = new ArrayList<>();

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        super.initialize(gameInfo, gameSetting);
        isCameout = false;
        divinationQueue.clear();
        myDivinationMap.clear();
        whiteList.clear();
        blackList.clear();
        grayList = new ArrayList<>();
        semiWolves.clear();
        possessedList.clear();
    }

    public void dayStart() {
        super.dayStart();
        Judge divination = currentGameInfo.getDivineResult();
        if (divination != null) {
            divinationQueue.offer(divination);
            grayList.remove(divination.getTarget());
            if (divination.getResult() == Species.HUMAN) {
                whiteList.add(divination.getTarget());
            } else {
                blackList.add(divination.getTarget());
            }
            myDivinationMap.put(divination.getTarget(), divination.getResult());
        }
    }

    protected void chooseVoteCandidate() {
        // �����l�T������Γ��R���[
        List<Agent> aliveWolves = new ArrayList<>();
        for (Agent a : blackList) {
            if (isAlive(a)) {
                aliveWolves.add(a);
            }
        }
        // ����̓��[�悪�����l�T�łȂ��ꍇ���[���ς���
        if (!aliveWolves.isEmpty()) {
            if (!aliveWolves.contains(voteCandidate)) {
                voteCandidate = randomSelect(aliveWolves);
                if (canTalk) {
                    talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new VoteContentBuilder(voteCandidate)))));
                }
            }
            return;
        }
        // �m��l�T�����Ȃ��ꍇ�͐�������
        werewolves.clear();
        // �U�肢�t
        for (Agent a : aliveOthers) {
            if (comingoutMap.get(a) == Role.SEER) {
                werewolves.add(a);
            }
        }
        // �U��}�t
        for (Judge j : identList) {
            Agent agent = j.getAgent();
            if ((myDivinationMap.containsKey(j.getTarget()) && j.getResult() != myDivinationMap.get(j.getTarget()))) {
                if (isAlive(agent) && !werewolves.contains(agent)) {
                    werewolves.add(agent);
                }
            }
        }
        possessedList.clear();
        semiWolves.clear();
        for (Agent a : werewolves) {
            // �l�T���Ȃ̂ɐl�ԁ˗��؂��
            if (whiteList.contains(a)) {
                if (!possessedList.contains(a)) {
                    talkQueue.offer(new Content(new EstimateContentBuilder(a, Role.POSSESSED)));
                    possessedList.add(a);
                }
            } else {
                semiWolves.add(a);
            }
        }
        if (!semiWolves.isEmpty()) {
            if (!semiWolves.contains(voteCandidate)) {
                voteCandidate = randomSelect(semiWolves);
                // �ȑO�̓��[�悩��ς��ꍇ�C�V���ɐ�������������
                if (canTalk) {
                    talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
                }
            }
        }
        // �l�T��₪���Ȃ��ꍇ�͋��l��₩�烉���_��
        else if (!possessedList.isEmpty()) {
            if (!possessedList.contains(voteCandidate)) {
                voteCandidate = randomSelect(possessedList);
                // �ȑO�̓��[�悩��ς��ꍇ�C�V���ɐ�������������
                if (canTalk) {
                    talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.POSSESSED)));
                }
            }
        }
        // ���l��₪���Ȃ��ꍇ�̓O���C���烉���_��
        else {
            if (!grayList.isEmpty()) {
                if (!grayList.contains(voteCandidate)) {
                    voteCandidate = randomSelect(grayList);
                }
            }
            // �O���C�����Ȃ��ꍇ�����_��
            else {
                if (!aliveOthers.contains(voteCandidate)) {
                    voteCandidate = randomSelect(aliveOthers);
                }
            }
        }
    }

    public String talk() {
        // �Q�[���J�n�シ���J�~���O�A�E�g
        if (!isCameout) {
            talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
            isCameout = true;
        }
        // ����܂ł̐肢���ʂ����ׂČ��J
        while (!divinationQueue.isEmpty()) {
            Judge ident = divinationQueue.poll();
            talkQueue.offer(new Content(new DivinedResultContentBuilder(ident.getTarget(), ident.getResult())));
        }
        return super.talk();
    }

    public Agent divine() {
        // �l�T��₪����΂���炩�烉���_���ɐ肤
        if (!semiWolves.isEmpty()) {
            return randomSelect(semiWolves);
        }
        // �l�T��₪���Ȃ��ꍇ�C�܂�����Ă��Ȃ������҂��烉���_���ɐ肤
        List<Agent> candidates = new ArrayList<>();
        for (Agent a : aliveOthers) {
            if (!myDivinationMap.containsKey(a)) {
                candidates.add(a);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return randomSelect(candidates);
    }
}
