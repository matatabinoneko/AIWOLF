package com.gmail.romanesco2090.cedec2018impl.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;

/**
 * 生存者が3人になったら狂人CO
 */
public class TalkCO5PosLast3PP extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getAlives().size() == 3 && game.getSelf().coRole == null) {
            return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED);
        }
        return null;
    }

}
