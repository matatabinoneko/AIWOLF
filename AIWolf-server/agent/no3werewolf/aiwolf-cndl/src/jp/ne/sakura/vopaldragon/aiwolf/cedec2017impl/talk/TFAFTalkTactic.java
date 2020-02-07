package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.model.TFAFGameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.TalkTactic;
import org.aiwolf.client.lib.ContentBuilder;

public abstract class TFAFTalkTactic extends TalkTactic {

    @Override
    public ContentBuilder talk(int turn, int skip, int utter, GameModel model, Game game) {
        return talkImpl(turn, skip, utter, (TFAFGameModel) model, game);
    }

    public abstract ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game);

}
