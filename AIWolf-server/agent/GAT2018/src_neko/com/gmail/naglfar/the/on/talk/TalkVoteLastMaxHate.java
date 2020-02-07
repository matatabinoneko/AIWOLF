package com.gmail.naglfar.the.on.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.naglfar.the.on.framework.EventType;
import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.framework.GameEvent;
import com.gmail.naglfar.the.on.model.TFAFGameModel;

public class TalkVoteLastMaxHate extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameEvent voteEvent = game.getLastEventOf(EventType.VOTE);
        Map<GameAgent, Integer> votes = new HashMap<>();
        game.getAliveOthers().stream().forEach(x -> {
            /* 2日目までは CO勢には投票しない */
            if (game.getDay() > 2 || x.coRole == null) {
                votes.put(x, 0);
            }
        });

        voteEvent.votes.stream().map(v -> v.target).filter(x -> votes.containsKey(x)).forEach(x -> {
            votes.put(x, votes.get(x) + 1);
        });
        GameAgent tar = Collections.max(votes.keySet(), Comparator.comparing(x -> votes.get(x)));
        return new VoteContentBuilder(tar.agent);
    }

}
