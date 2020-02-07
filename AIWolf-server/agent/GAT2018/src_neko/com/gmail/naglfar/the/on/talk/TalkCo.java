package com.gmail.naglfar.the.on.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

/**
 * とりあえずCOする
 */
public class TalkCo extends TFAFTalkTactic {

    private Role role;

    public TalkCo(Role role) {
        this.role = role;
    }

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (!game.getSelf().hasCO()) {
            return new ComingoutContentBuilder(game.getSelf().agent, role);
        }
        return null;
    }

}
