package tera.aiwolf.target;

import java.util.List;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.EventType;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameEvent;
import tera.aiwolf.framework.GameTalk;
import tera.aiwolf.model.TFAFGameModel;

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
