package com.gmail.shoot7arrow25.player;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.*;

/** ���l���G�[�W�F���g�N���X */
public class NeoElizaVillager extends NeoElizaBasePlayer {
    protected void chooseVoteCandidate() {
        werewolves.clear();
        for (Judge j : divinationList) {
            // �������邢�͎E���ꂽ�G�[�W�F���g��l�T�Ɣ��肵�Ă��āC�������Ă��鎩�̐肢�t�𓊕[����Ƃ���
            if (j.getResult() == Species.WEREWOLF && (j.getTarget() == me || isKilled(j.getTarget()))) {
                Agent candidate = j.getAgent();
                if (isAlive(candidate) && !werewolves.contains(candidate)) {
                    werewolves.add(candidate);
                }
            }
        }
        // ��₪���Ȃ��ꍇ�̓����_��
        if (werewolves.isEmpty()) {
            if (!aliveOthers.contains(voteCandidate)) {
                voteCandidate = randomSelect(aliveOthers);
            }
        } else {
            if (!werewolves.contains(voteCandidate)) {
                voteCandidate = randomSelect(werewolves);
                // �ȑO�̓��[�悩��ς��ꍇ�C�V���ɐ��������Ɛ肢�v��������
                if (canTalk) {
                    talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
                    talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
                }
            }
        }
    }

    public String whisper() {
        throw new UnsupportedOperationException();
    }

    public Agent attack() {
        throw new UnsupportedOperationException();
    }

    public Agent divine() {
        throw new UnsupportedOperationException();
    }

    public Agent guard() {
        throw new UnsupportedOperationException();
    }
}
