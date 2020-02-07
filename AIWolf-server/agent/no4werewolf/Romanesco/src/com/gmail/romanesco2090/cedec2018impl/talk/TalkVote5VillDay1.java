package com.gmail.romanesco2090.cedec2018impl.talk;

import static com.gmail.romanesco2090.util.Utils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;

import com.gmail.romanesco2090.cedec2018impl.metagame.TFAFMetagameModel;
import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameTalk;
import com.gmail.romanesco2090.util.Utils;

public class TalkVote5VillDay1 extends TFAFTalkTactic {

    private GameAgent currentTarget;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        // 占い結果後に発言
        if (game.getDay() == 1) {
            if (currentTarget == null) {
                // 初期、最も強い人に投票宣言
                List<GameAgent> agents = game.getAliveOthers();
                double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
                currentTarget = Collections.max(agents, Comparator.comparing(x -> winCount[x.getIndex()]));
                log("Day1:初期殴り先：" + currentTarget);
                return new VoteContentBuilder(currentTarget.agent);
            } else {
                // 占い結果が判ったら、それに合わせて投票先を変える
                List<GameTalk> divine = game.getAllTalks()
                    .filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.DIVINED)
                    .collect(Collectors.toList());
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
                        log("Day1:新しいターゲット：" + currentTarget);
                        return new VoteContentBuilder(currentTarget.agent);
                    }
                }
            }
        }
        return null;

    }

}
