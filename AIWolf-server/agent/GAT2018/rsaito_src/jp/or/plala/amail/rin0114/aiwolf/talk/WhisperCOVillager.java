package jp.or.plala.amail.rin0114.aiwolf.talk;

import java.util.stream.Collectors;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;

/**
 *
 * 人狼1日目用: 村人CO希望
 */
public class WhisperCOVillager extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getSelf().whisperList.stream().filter(x -> x.getTopic() == Topic.COMINGOUT && x.getRole() == Role.VILLAGER).collect(Collectors.toList()).isEmpty()) {
            return new ComingoutContentBuilder(game.getSelf().agent, Role.VILLAGER);
        }
        return null;
    }
}
