package tkg.aiwolf.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import tkg.aiwolf.model.TFAFGameModel;

/**
 * 生存者が3人になったら狂人CO
 */
public class TalkCO5PosLast3PP extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getAlives().size() == 3 && game.getSelf().coRole == null) {
            return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED);
        }
        return null;
    }

}
