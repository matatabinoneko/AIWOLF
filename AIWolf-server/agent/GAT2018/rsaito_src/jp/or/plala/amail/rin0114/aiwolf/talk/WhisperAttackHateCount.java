package jp.or.plala.amail.rin0114.aiwolf.talk;

import java.util.HashMap;
import java.util.Map;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;

public class WhisperAttackHateCount extends TFAFTalkTactic {

    Map<GameAgent, Integer> voteFrom = new HashMap<>();
    int day = 0;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        /* 死者は死んだ */
        game.getAgents().stream().filter(x -> !x.isAlive).forEach(x -> voteFrom.remove(x));
        /* 前日の自分たちへの投票 */
        if (day < game.getDay()) {
            game.getLastEventOf(EventType.VOTE).votes.stream().filter(v -> v.target.role == Role.WEREWOLF && v.initiator.role != Role.WEREWOLF).forEach(v -> {
                if (voteFrom.containsKey(v.initiator)) {
                    voteFrom.put(v.initiator, voteFrom.get(v.initiator) + 1);
                } else {
                    voteFrom.put(v.initiator, 1);
                }
            });
            day = game.getDay();
        }
        if (game.getDay() < 2) {
            return null; // 初日は差がつかない
        }
        /* ヘイトの高いエージェントを探す */
        GameAgent tar = null;
        for (GameAgent agent : voteFrom.keySet()) {
            if ((game.getDay() > 4) || agent.coRole == null) {
                /* 4か目まではCO勢は保護 */
                if (tar == null || voteFrom.get(tar) < voteFrom.get(agent)) {
                    tar = agent;
                }
            }
        }
        if (tar != null) {
            return new AttackContentBuilder(tar.agent);
        }
        return null;
    }

}
