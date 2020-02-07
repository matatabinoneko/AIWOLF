package tera.aiwolf.target;

import java.util.List;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.Utils;

public class VoteForWolf5ver extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {

        List<GameAgent> list = game.getAliveOthers();
        Utils.sortByScore(list, model.getEvilScore(), false); // 計算されたScoreを降順に並び替える
        GameAgent wolfCand = list.get(0); // 配列の要素の最初のものを取り出す
        return wolfCand;
    }

}
