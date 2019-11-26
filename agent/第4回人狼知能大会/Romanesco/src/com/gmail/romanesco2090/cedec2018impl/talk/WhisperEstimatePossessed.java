package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameTalk;

public class WhisperEstimatePossessed extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        if (me.whisperList.stream().filter(x -> x.getTopic() == Topic.ESTIMATE && x.getRole() == Role.POSSESSED).count() > 0) {
            return null;
        }
        /* 当日の占い結果 */
        List<GameTalk> divineResult = game.getAllTalks().filter(x -> x.getDay() == game.getDay() && x.getTalker().role != Role.WEREWOLF && x.getTopic() == Topic.DIVINED).collect(Collectors.toList());
        for (GameTalk gameTalk : divineResult) {
            if ((gameTalk.getTarget().role == Role.WEREWOLF && gameTalk.getResult() == Species.HUMAN)
                || gameTalk.getTarget().role != Role.WEREWOLF && gameTalk.getResult() == Species.WEREWOLF) {
                return new EstimateContentBuilder(gameTalk.getTalker().agent, Role.POSSESSED);
            }
        }
        return null;
    }

}
