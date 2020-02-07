package com.gmail.romanesco2090.cedec2018impl.talk;

import static com.gmail.romanesco2090.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameTalk;
import com.gmail.romanesco2090.util.Utils;

public class TalkVote5VillDay2 extends TFAFTalkTactic {

    private GameAgent currentTarget;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getDay() == 2) {
            List<GameTalk> divine = game.getAllTalks().filter(t -> t.getTopic() == Topic.DIVINED).collect(Collectors.toList());
            if (!divine.isEmpty()) {
                GameAgent newTarget = null;
                List<GameAgent> candidates = game.getAliveOthers();
                if (!candidates.isEmpty()) {
                    double[] evilScore = model.getEvilScore();
                    Utils.sortByScore(candidates, evilScore, false);
                    newTarget = candidates.get(0);
                    log("Day1:EvilScore高めの人を殴る");
                }

                if (newTarget != null && newTarget != currentTarget) {
                    currentTarget = newTarget;
                    log("Day2：新しいターゲット：" + currentTarget);
                    return new VoteContentBuilder(currentTarget.agent);
                }
            }
        }
        return null;

    }

}
