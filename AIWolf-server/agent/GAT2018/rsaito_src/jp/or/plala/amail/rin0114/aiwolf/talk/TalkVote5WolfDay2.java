package jp.or.plala.amail.rin0114.aiwolf.talk;

import java.util.List;
import java.util.stream.Collectors;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameTalk;
import jp.or.plala.amail.rin0114.aiwolf.model.TFAFGameModel;
import jp.or.plala.amail.rin0114.aiwolf.util.Utils;
import static jp.or.plala.amail.rin0114.aiwolf.util.Utils.*;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

public class TalkVote5WolfDay2 extends TFAFTalkTactic {

    private GameAgent currentTarget;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getDay() == 2) {
            List<GameTalk> divine = game.getAllTalks().filter(t -> t.getTopic() == Topic.DIVINED).collect(Collectors.toList());
            if (!divine.isEmpty()) {
                GameAgent newTarget = null;
                List<GameAgent> candidates = game.getAliveOthers();
                //狂人が生き残っている場合、それ以外を殴る
                for (GameTalk t : divine) {
                    if ((t.getTarget().isSelf && t.getResult() != Species.WEREWOLF) || (!t.getTarget().isSelf && t.getResult() == Species.WEREWOLF)) {
                        if (t.getTalker().isAlive) {
                            GameAgent possessed = t.getTalker();
                            candidates.remove(possessed);
                            if (!candidates.isEmpty()) {
                                newTarget = candidates.get(0);
                                log("Day2：狂人以外を殴る");
                                break;
                            }
                        }
                    }
                }

                //占いが生き残っている場合、占いを殴る
                if (newTarget == null) {
                    for (GameTalk t : divine) {
                        if (t.getTalker().isAlive) {
                            newTarget = t.getTalker();
                            log("Day2：残占いを殴る");
                            break;
                        }
                    }
                }

                //村村の場合、EvilScore高い方を殴る
                if (newTarget == null) {
                    if (!candidates.isEmpty()) {
                        double[] evilScore = model.getEvilScore();
                        Utils.sortByScore(candidates, evilScore, false);
                        newTarget = candidates.get(0);
                        log("Day2：EvilScore高めの人を殴る");
                    }
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
