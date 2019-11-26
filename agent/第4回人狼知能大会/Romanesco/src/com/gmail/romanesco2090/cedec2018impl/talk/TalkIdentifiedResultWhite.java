package com.gmail.romanesco2090.cedec2018impl.talk;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;

/**
 * ニセ霊媒結果黒をお伝えする発言
 */
public class TalkIdentifiedResultWhite extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getSelf().hasCO() && target != null) {
            IdentContentBuilder icb = new IdentContentBuilder(target.agent, Species.HUMAN);
            target = null;
            return icb;
        }
        return null;
    }

    private GameAgent target;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.EXECUTE) {
            this.target = e.target;
        }
    }

}
