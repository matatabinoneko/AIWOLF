package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.model.TFAFGameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.AbstractRole;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameModel;

public abstract class TFAFBaseRole extends AbstractRole {

    public TFAFBaseRole(Game game) {
        super(game);
    }

    @Override
    protected GameModel createModel(Game game) {
        return new TFAFGameModel(game);
    }

}
