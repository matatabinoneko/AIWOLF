package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.model.TFAFGameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.TargetTactic;

public abstract class TFAFTargetTactic extends TargetTactic {

    @Override
    public GameAgent target(GameModel model, Game game) {
        return targetImpl((TFAFGameModel) model, game);
    }

    public abstract GameAgent targetImpl(TFAFGameModel model, Game game);

}
