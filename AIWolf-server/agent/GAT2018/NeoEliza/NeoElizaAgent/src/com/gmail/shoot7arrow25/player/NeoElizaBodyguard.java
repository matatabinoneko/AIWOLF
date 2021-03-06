package com.gmail.shoot7arrow25.player;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/** 狩人エージェントクラス */
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
        // 偽狩人
        fakeBodyguards.clear();
        for(Agent a: aliveOthers) {
            if(comingoutMap.get(a) == Role.BODYGUARD) {
                fakeBodyguards.add(a);
            }
        }
        // 投票:　過去にGJなしで偽狩人 or GJありかつ自分がCO済で偽狩人
        // GJありで自分がCOしてないのに偽狩人に投票は怪しまれる
        // 狂人は偽占いCOに出ていて偽狩人COは人狼の可能性が高いため投票&人狼だと疑う
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

    // 護衛先指定
    public Agent guard() {
        Agent guardCandidate;
        // 前回護衛対象者が生きてたら連続護衛
        if (guardedAgent != null
                && isAlive(guardedAgent)
                && currentGameInfo.getLastDeadAgentList().isEmpty()) {
            guardCandidate = guardedAgent;
        } else {
            List<Agent> candidates = new ArrayList<>();
            // 占いCOかつ人狼候補でない。ただし占いCOが過去に殺されていたら除外（ローラー時は護衛しない or 偽CO護衛防止)
            if(!deadAgentExists(Role.SEER)) {
                for (Agent agent : aliveOthers) {
                    if (comingoutMap.get(agent) == Role.SEER && !werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // 霊媒師（占い同様）
            if (candidates.isEmpty() && !deadAgentExists(Role.MEDIUM)) {
                for (Agent agent : aliveOthers) {
                    if (comingoutMap.get(agent) == Role.MEDIUM && !werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // それでも見つからなければ自分と人狼候補以外から護衛
            if (candidates.isEmpty()) {
                for (Agent agent : aliveOthers) {
                    if (!werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // それでもいなければ自分以外から護衛
            if (candidates.isEmpty()) {
                candidates.addAll(aliveOthers);
            }
            // 護衛候補からランダムに護衛
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
        // 護衛先報告
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
        // (GJが過去にありで偽狩人存在)でN日目以降ならCO
        // 2日目以降で自分を黒だしした占い師いるならCO
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
