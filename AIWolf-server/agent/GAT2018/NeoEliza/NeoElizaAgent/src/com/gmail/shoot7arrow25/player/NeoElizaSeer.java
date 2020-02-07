package com.gmail.shoot7arrow25.player;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.*;

/** 占い師役エージェントクラス */
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
        // 生存人狼がいれば当然投票
        List<Agent> aliveWolves = new ArrayList<>();
        for (Agent a : blackList) {
            if (isAlive(a)) {
                aliveWolves.add(a);
            }
        }
        // 既定の投票先が生存人狼でない場合投票先を変える
        if (!aliveWolves.isEmpty()) {
            if (!aliveWolves.contains(voteCandidate)) {
                voteCandidate = randomSelect(aliveWolves);
                if (canTalk) {
                    talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new VoteContentBuilder(voteCandidate)))));
                }
            }
            return;
        }
        // 確定人狼がいない場合は推測する
        werewolves.clear();
        // 偽占い師
        for (Agent a : aliveOthers) {
            if (comingoutMap.get(a) == Role.SEER) {
                werewolves.add(a);
            }
        }
        // 偽霊媒師
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
            // 人狼候補なのに人間⇒裏切り者
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
                // 以前の投票先から変わる場合，新たに推測発言をする
                if (canTalk) {
                    talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
                }
            }
        }
        // 人狼候補がいない場合は狂人候補からランダム
        else if (!possessedList.isEmpty()) {
            if (!possessedList.contains(voteCandidate)) {
                voteCandidate = randomSelect(possessedList);
                // 以前の投票先から変わる場合，新たに推測発言をする
                if (canTalk) {
                    talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.POSSESSED)));
                }
            }
        }
        // 狂人候補がいない場合はグレイからランダム
        else {
            if (!grayList.isEmpty()) {
                if (!grayList.contains(voteCandidate)) {
                    voteCandidate = randomSelect(grayList);
                }
            }
            // グレイもいない場合ランダム
            else {
                if (!aliveOthers.contains(voteCandidate)) {
                    voteCandidate = randomSelect(aliveOthers);
                }
            }
        }
    }

    public String talk() {
        // ゲーム開始後すぐカミングアウト
        if (!isCameout) {
            talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
            isCameout = true;
        }
        // これまでの占い結果をすべて公開
        while (!divinationQueue.isEmpty()) {
            Judge ident = divinationQueue.poll();
            talkQueue.offer(new Content(new DivinedResultContentBuilder(ident.getTarget(), ident.getResult())));
        }
        return super.talk();
    }

    public Agent divine() {
        // 人狼候補がいればそれらからランダムに占う
        if (!semiWolves.isEmpty()) {
            return randomSelect(semiWolves);
        }
        // 人狼候補がいない場合，まだ占っていない生存者からランダムに占う
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
