package tkg.aiwolf.role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.AbstractRole;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameModel;
import tkg.aiwolf.model.TFAFGameModel;

public abstract class TFAFBaseRole extends AbstractRole {

    public TFAFBaseRole(Game game) {
        super(game);
    }

    @Override
    protected GameModel createModel(Game game) {
        return new TFAFGameModel(game);
    }

}
