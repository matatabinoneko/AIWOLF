package tera.aiwolf.talk;

import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameModel;
import tera.aiwolf.framework.TalkTactic;
import tera.aiwolf.model.TFAFGameModel;

public abstract class TFAFTalkTactic extends TalkTactic {
	int numFirstCO = 0; // 初日COエージェント数
	List<GameAgent> firstDayCO = null;

    @Override
    public ContentBuilder talk(int turn, int skip, int utter, GameModel model, Game game) {
        return talkImpl(turn, skip, utter, (TFAFGameModel) model, game);
    }

    public abstract ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game);

}
