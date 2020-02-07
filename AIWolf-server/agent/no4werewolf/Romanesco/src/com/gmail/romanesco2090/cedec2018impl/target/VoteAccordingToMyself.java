package com.gmail.romanesco2090.cedec2018impl.target;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;

/**
 *
 * 自分が Vote すると言った相手に Vote
 *
 */
public class VoteAccordingToMyself extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        /* 自分が投票すると宣言した先を取得 */
        List<GameAgent> vote_targets = me.talkList.stream().filter(x -> x.getTopic() == Topic.VOTE)
            .map(x -> x.getTarget()).collect(Collectors.toList());
        Collections.reverse(vote_targets); // リストを逆順に

        GameAgent target = null;
        for (GameAgent agent : vote_targets) {
            if (agent != me && agent.isAlive) {
                target = agent;
                break;
            }
        }
        if (target != null) {
            return target;
        }
        return null;
    }
}
