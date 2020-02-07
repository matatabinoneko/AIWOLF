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
    /** 霊能COしたかを管理するフラグ */
    boolean isCameout;
    /** 発言されていない霊能結果を入れておく待ち行列 */
    Deque<Judge> identQueue = new LinkedList<>();
    /** 霊能結果を格納したマップ */
    Map<Agent, Species> myIdentMap = new HashMap<>();

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        super.initialize(gameInfo, gameSetting);
        isCameout = false;
        identQueue.clear();
        myIdentMap.clear();
    }

    public void dayStart() {
        super.dayStart();
        // 霊媒結果を待ち行列に入れる
        Judge ident = currentGameInfo.getMediumResult();
        if (ident != null) {
            identQueue.offer(ident);
            myIdentMap.put(ident.getTarget(), ident.getResult());
        }
    }

    protected void chooseVoteCandidate() {
        /** 生きていて自分視点矛盾していることを発言しているエージェントを入れるリスト */
        List<Agent> nonVillagerSide = new ArrayList<>();
        // 霊媒師をカミングアウトしている他のエージェントは人狼候補
        for (Agent agent : aliveOthers) {
            if (comingoutMap.get(agent) == Role.MEDIUM) {
                nonVillagerSide.add(agent);
        	}
        }
        // 自分や襲撃されたエージェントを人狼と判定，あるいは自分と異なる判定の占い師は人狼候補
        // TODO: 死んだエージェントを人狼と判定した占い師を偽として投票するのはMediumではなくVillagerに実装する方がよいのではないか
        for (Judge j : divinationList) {
            Agent agent = j.getAgent();
            Agent target = j.getTarget();
            if (j.getResult() == Species.WEREWOLF && (target == me || isKilled(target)) || (myIdentMap.containsKey(target) && j.getResult() != myIdentMap.get(target))) {
                if (isAlive(agent) && !werewolves.contains(agent)) {
                    nonVillagerSide.add(agent);
                }
            }
        }
        // 自分視点偽のことを言っているエージェントを釣る、いなければ生存者の中からランダムで釣る
        if (!nonVillagerSide.isEmpty()) {
            voteCandidate = randomSelect(nonVillagerSide);
        } else {
            voteCandidate = randomSelect(aliveOthers);
        }
    }

    // TODO: 自分とラインがつながらない占い師がいたときその人を人狼か狂人だ、と主張する
    // TODO: 初日の占い結果が● -> 霊能COしない方がいいかも知れない
    public String talk() {
        // 一定の確率でCOする 1日目の占い結果が●なら潜伏する確率を上げる
        double border = 0.7;
        double willCo = Math.random(); // Math.random()は1回目に読んだときのseedがその後使われる
        if (!isCameout) {
            // 対抗がいる or 霊能結果が● or サイコロを振って閾値より大きい ならばCOする
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
