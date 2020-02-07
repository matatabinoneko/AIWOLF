package com.gmail.naglfar.the.on.talk;

import org.aiwolf.client.lib.ContentBuilder;

import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameModel;
import com.gmail.naglfar.the.on.framework.TalkTactic;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public abstract class TFAFTalkTactic extends TalkTactic {

    @Override
    public ContentBuilder talk(int turn, int skip, int utter, GameModel model, Game game) {
        return talkImpl(turn, skip, utter, (TFAFGameModel) model, game);
    }

    public abstract ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game);

}
