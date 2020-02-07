package jp.or.plala.amail.rin0114.aiwolf.role;

import jp.or.plala.amail.rin0114.aiwolf.framework.AbstractRole;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameModel;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;

public abstract class TFAFBaseRole extends AbstractRole {

    public TFAFBaseRole(Game game) {
        super(game);
    }

    @Override
    protected GameModel createModel(Game game) {
        return new TFAFGameModel(game);
    }

}
