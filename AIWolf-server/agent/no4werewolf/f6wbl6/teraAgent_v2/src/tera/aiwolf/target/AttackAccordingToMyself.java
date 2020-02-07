package tera.aiwolf.target;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.EventType;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;

/**
 *
 * 自分が Vote すると言った相手に Vote
 *
 */
public class AttackAccordingToMyself extends TFAFTargetTactic {

    Map<GameAgent, Integer> voteFrom = new HashMap<>();

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        /* 自分が襲撃すると宣言した先を取得 */
        List<GameAgent> attack_targets = me.whisperList.stream().filter(x -> x.getDay() == game.getDay()
            && x.getTopic() == Topic.ATTACK).map(x -> x.getTarget()).filter(x -> x.isAlive)
            .collect(Collectors.toList());
        Collections.reverse(attack_targets); // リストを逆順に

        /* 死者は死んだ */
        game.getAgents().stream().filter(x -> !x.isAlive).forEach(x -> voteFrom.remove(x));
        /* 前日の自分たちへの投票 */
        game.getLastEventOf(EventType.VOTE).votes.stream().filter(v -> v.target.role == Role.WEREWOLF && v.initiator.role != Role.WEREWOLF).forEach(v -> {
            if (voteFrom.containsKey(v.initiator)) {
                voteFrom.put(v.initiator, voteFrom.get(v.initiator) + 1);
            } else {
                voteFrom.put(v.initiator, 1);
            }
        });

        GameAgent target = null;
        for (GameAgent agent : attack_targets) {
            if (agent.role != Role.WEREWOLF) {
                target = agent;
                break;
            }
        }
        if (target != null) {
            return target;
        } else {
            /* ヘイトの高いエージェントを探す */
            for (GameAgent agent : voteFrom.keySet()) {
                if (target == null || voteFrom.get(target) < voteFrom.get(agent)) {
                    target = agent;
                }
            }
            return target;
        }
    }

}
