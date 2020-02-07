package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.stream.Collectors;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;

/**
 * 人狼1日目用:  占い師CO希望
 */
public class WhisperCOSeer extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getSelf().whisperList.stream().filter(x -> x.getTopic() == Topic.COMINGOUT && x.getRole() == Role.SEER).collect(Collectors.toList()).isEmpty()) {
            return new ComingoutContentBuilder(game.getSelf().agent, Role.SEER);
        }
        return null;
    }
}
