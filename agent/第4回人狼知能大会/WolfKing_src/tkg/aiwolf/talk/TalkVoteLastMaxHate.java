package tkg.aiwolf.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameEvent;
import tkg.aiwolf.model.TFAFGameModel;

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
