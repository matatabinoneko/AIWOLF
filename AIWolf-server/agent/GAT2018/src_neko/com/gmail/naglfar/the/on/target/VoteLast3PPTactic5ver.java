package com.gmail.naglfar.the.on.target;

import java.util.List;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

import com.gmail.naglfar.the.on.framework.EventType;
import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.framework.GameEvent;
import com.gmail.naglfar.the.on.framework.GameTalk;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public class VoteLast3PPTactic5ver extends TFAFTargetTactic {

    private GameAgent wolf = null;
    private GameAgent villager = null;

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {
        List<GameAgent> others = game.getAliveOthers();
        int cnt = 0;
        GameAgent estimatewolf = null;
        GameAgent estimatevillager = null;
        List<GameEvent> talks = game.getEventAtDay(EventType.TALK, game.getDay());
        for (GameEvent evt : talks) {
            for (GameTalk talk : evt.talks) {
                if (talk.getTopic() == Topic.DIVINED && !talk.getTalker().isSelf) {
                    cnt++;
                    if (talk.getResult() == Species.WEREWOLF) {
                        estimatewolf = talk.getTarget();
                    } else {
                        estimatevillager = talk.getTarget();
                    }
                }
            }
        }
        if (cnt == 1) {
            if (estimatewolf != null) {
                wolf = estimatewolf;
            }
            if (estimatevillager != null) {
                villager = estimatevillager;
            }
        }
        cnt = 0;
        if (game.getAlives().size() == 3) {
            others.remove(wolf);
            if (others.size() == 1) {
                //唯一の対抗占いの黒判定は真狼とみて投票先から除外
                return others.get(0);
            }
            if (others.contains(villager)) {
                //対抗占いによる確定白が生き残っている場合は、そちらに投票
                return villager;
            }
        }
        return null;
    }

}
