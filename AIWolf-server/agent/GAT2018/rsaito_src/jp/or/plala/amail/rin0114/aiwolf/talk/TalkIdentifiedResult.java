package jp.or.plala.amail.rin0114.aiwolf.talk;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameEvent;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.common.data.Species;

/**
 * 霊媒結果をお伝えする発言
 */
public class TalkIdentifiedResult extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getSelf().hasCO() && target != null) {
            IdentContentBuilder icb = new IdentContentBuilder(target.agent, result);
            target = null;
            result = null;
            return icb;
        }
        return null;
    }

    private GameAgent target;
    private Species result;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.MEDIUM) {
            this.target = e.target;
            this.result = e.species;
        }

    }

}
