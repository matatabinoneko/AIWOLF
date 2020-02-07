package jp.or.plala.amail.rin0114.aiwolf.talk;

import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameEvent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;

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
