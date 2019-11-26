package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;
import com.gmail.romanesco2090.util.Utils;

/**
 * 最も狼らしいAgentに投票宣言する
 *
 */
public class TalkVoteWolfbySeer5ver extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent target = null;
        if (game.getDay() == 1) {
            if (divineTarget != null && result == Species.WEREWOLF) {
                target = divineTarget;
                return new VoteContentBuilder(target.agent);
            } else {
                // 最も狼らしいAgentに投票宣言
                List<GameAgent> list = game.getAliveOthers();
                Utils.sortByScore(list, model.getEvilScore(), false);
                GameAgent wolfCand = list.get(0);
                model.currentVoteTarget = wolfCand;
                return new VoteContentBuilder(wolfCand.agent);
            }
        }
        if (game.getDay() == 2) {
            if (divineTarget != null && result == Species.WEREWOLF) {
                target = divineTarget;
            } else {
                List<GameAgent> others = game.getAliveOthers();
                if (divineTarget != null) {
                    others.remove(divineTarget);
                }
                target = others.get(0);
            }
            return new VoteContentBuilder(target.agent);
        }
        return null;
    }

    private GameAgent divineTarget;
    private Species result;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DIVINE) {
            divineTarget = e.target;
            result = e.species;
        }
    }
}
