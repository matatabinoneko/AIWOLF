package jp.or.plala.amail.rin0114.aiwolf.talk;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameModel;
import jp.or.plala.amail.rin0114.aiwolf.framework.TalkTactic;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;

import org.aiwolf.client.lib.ContentBuilder;

public abstract class TFAFTalkTactic extends TalkTactic {

    @Override
    public ContentBuilder talk(int turn, int skip, int utter, GameModel model, Game game) {
        return talkImpl(turn, skip, utter, (TFAFGameModel) model, game);
    }

    public abstract ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game);

}
