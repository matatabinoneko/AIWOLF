package com.gmail.romanesco2090.cedec2018impl.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameEvent;

/**
 * 占い結果または霊能結果が黒だった時に霊能CO
 */
public class TalkCOFoundBlack extends TFAFTalkTactic {

	private Role role;
    private Species result;

    public TalkCOFoundBlack(Role role) {
        this.role = role;
    }

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (!game.getSelf().hasCO() && result == Species.WEREWOLF) {
            return new ComingoutContentBuilder(game.getSelf().agent, role);
        }
        return null;
    }



    @Override
    public void handleEvent(Game g, GameEvent e) {
    	if (e.type == EventType.DIVINE) {
            this.result = e.species;
        } else if (e.type == EventType.MEDIUM) {
            this.result = e.species;
        }

    }

}
