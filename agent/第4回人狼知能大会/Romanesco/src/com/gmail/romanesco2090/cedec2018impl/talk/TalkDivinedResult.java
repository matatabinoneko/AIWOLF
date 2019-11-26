package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.HashSet;
import java.util.Set;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;

/**
 * COしてたら正直に占い結果を言う
 */
public class TalkDivinedResult extends TFAFTalkTactic {

    private Set<GameAgent> divinedAnnounced = new HashSet<>();

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (divineTarget != null && game.getSelf().hasCO()) {
                //素直に結果を報告
                divinedAnnounced.add(divineTarget);
                return new DivinedResultContentBuilder(divineTarget.agent, result);
        }
        return null;
    }

    private GameAgent divineTarget;
    private Species result;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DIVINE) {
            divineTarget = e.target;
            result = e.species;
        }
    }

}
