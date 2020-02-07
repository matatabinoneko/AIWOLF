package com.gmail.shoot7arrow25.player;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/** ëlƒG[ƒWƒFƒ“ƒgƒNƒ‰ƒX */
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
        // ‹Uël
        fakeBodyguards.clear();
        for(Agent a: aliveOthers) {
            if(comingoutMap.get(a) == Role.BODYGUARD) {
                fakeBodyguards.add(a);
            }
        }
        // “Š•[:@‰ß‹‚ÉGJ‚È‚µ‚Å‹Uël or GJ‚ ‚è‚©‚Â©•ª‚ªCOÏ‚Å‹Uël
        // GJ‚ ‚è‚Å©•ª‚ªCO‚µ‚Ä‚È‚¢‚Ì‚É‹Uël‚É“Š•[‚Í‰ö‚µ‚Ü‚ê‚é
        // ‹¶l‚Í‹Uè‚¢CO‚Éo‚Ä‚¢‚Ä‹UëlCO‚Íl˜T‚Ì‰Â”\«‚ª‚‚¢‚½‚ß“Š•[&l˜T‚¾‚Æ‹^‚¤
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

    // Œì‰qæw’è
    public Agent guard() {
        Agent guardCandidate;
        // ‘O‰ñŒì‰q‘ÎÛÒ‚ª¶‚«‚Ä‚½‚ç˜A‘±Œì‰q
        if (guardedAgent != null
                && isAlive(guardedAgent)
                && currentGameInfo.getLastDeadAgentList().isEmpty()) {
            guardCandidate = guardedAgent;
        } else {
            List<Agent> candidates = new ArrayList<>();
            // è‚¢CO‚©‚Âl˜TŒó•â‚Å‚È‚¢B‚½‚¾‚µè‚¢CO‚ª‰ß‹‚ÉE‚³‚ê‚Ä‚¢‚½‚çœŠOiƒ[ƒ‰[‚ÍŒì‰q‚µ‚È‚¢ or ‹UCOŒì‰q–h~)
            if(!deadAgentExists(Role.SEER)) {
                for (Agent agent : aliveOthers) {
                    if (comingoutMap.get(agent) == Role.SEER && !werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // —ì”}tiè‚¢“¯—lj
            if (candidates.isEmpty() && !deadAgentExists(Role.MEDIUM)) {
                for (Agent agent : aliveOthers) {
                    if (comingoutMap.get(agent) == Role.MEDIUM && !werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // ‚»‚ê‚Å‚àŒ©‚Â‚©‚ç‚È‚¯‚ê‚Î©•ª‚Æl˜TŒó•âˆÈŠO‚©‚çŒì‰q
            if (candidates.isEmpty()) {
                for (Agent agent : aliveOthers) {
                    if (!werewolves.contains(agent)) {
                        candidates.add(agent);
                    }
                }
            }
            // ‚»‚ê‚Å‚à‚¢‚È‚¯‚ê‚Î©•ªˆÈŠO‚©‚çŒì‰q
            if (candidates.isEmpty()) {
                candidates.addAll(aliveOthers);
            }
            // Œì‰qŒó•â‚©‚çƒ‰ƒ“ƒ_ƒ€‚ÉŒì‰q
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
        // Œì‰qæ•ñ
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
        // (GJ‚ª‰ß‹‚É‚ ‚è‚Å‹Uël‘¶İ)‚ÅN“ú–ÚˆÈ~‚È‚çCO
        // 2“ú–ÚˆÈ~‚Å©•ª‚ğ•‚¾‚µ‚µ‚½è‚¢t‚¢‚é‚È‚çCO
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
