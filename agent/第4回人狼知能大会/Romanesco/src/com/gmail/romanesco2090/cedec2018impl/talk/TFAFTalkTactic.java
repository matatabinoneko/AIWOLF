package com.gmail.romanesco2090.cedec2018impl.talk;

import org.aiwolf.client.lib.ContentBuilder;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameModel;
import com.gmail.romanesco2090.framework.TalkTactic;

public abstract class TFAFTalkTactic extends TalkTactic {

    @Override
    public ContentBuilder talk(int turn, int skip, int utter, GameModel model, Game game) {
        return talkImpl(turn, skip, utter, (TFAFGameModel) model, game);
    }

    public abstract ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game);

}
