package tkg.aiwolf.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.util.Utils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;
import tkg.aiwolf.metagame.TFAFMetagameModel;
import tkg.aiwolf.model.TFAFGameModel;

public class TalkVote5WolfDay1 extends TFAFTalkTactic {

    private GameAgent currentTarget;

    int i = 0;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        // 占い結果後に発言
        if (game.getDay() == 1) {
            if (currentTarget == null) {
                //初期、最も強い人に投票宣言
                List<GameAgent> agents = game.getAliveOthers();
                double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
                currentTarget = Collections.max(agents, Comparator.comparing(x -> winCount[x.getIndex()]));
                //log("Day1:初期殴り先：" + currentTarget);
                return null;
                //return new VoteContentBuilder(currentTarget.agent);
            } else {
                //占い結果が判ったら、それに合わせて投票先を変える
                List<GameTalk> divine = game.getAllTalks().filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.DIVINED).collect(Collectors.toList());
                if (!divine.isEmpty()) {
                    GameAgent possessed = null;
                    GameAgent newTarget = null;
                    //自分に黒出しをしてきた占い師を殴る
                    for (GameTalk t : divine) {
                        if (t.getTarget().isSelf && t.getResult() == Species.WEREWOLF) {
                            log("Day1:黒出し占い師を殴る");
                            newTarget = t.getTalker();
                            break;
                        }
                    }
                    //他に黒出しをされた人がいればそちら優先
                    for (GameTalk t : divine) {
                        if (!t.getTarget().isSelf && t.getResult() == Species.WEREWOLF) {
                            newTarget = t.getTarget();
                            possessed = t.getTalker();
                            log("Day1:黒出しされた人を殴る");
                            break;
                        }
                    }
                    //白出し狂人の探索
                    for (GameTalk t : divine) {
                        if (t.getTarget().isSelf && t.getResult() == Species.HUMAN) {
                            possessed = t.getTalker();
                            break;
                        }
                    }
                    //黒出しが無い場合、EvilScoreで判定、ただし狂人候補は除く
                    if (newTarget == null) {
                        List<GameAgent> candidates = game.getAliveOthers();
                        candidates.remove(possessed);
                        if (!candidates.isEmpty()) {
                            double[] evilScore = model.getEvilScore();
                            Utils.sortByScore(candidates, evilScore, false);
                            newTarget = candidates.get(0);
                            log("Day1:EvilScore高めの人を殴る");
                        }
                    }

                    if (newTarget != null && newTarget != currentTarget) {
                        currentTarget = newTarget;
                        log("Day1:新しいターゲット：" + currentTarget);
                        return new VoteContentBuilder(currentTarget.agent);
                    }

                    //もし一番強いエージェントとnewTargetが同じなら、一度だけ発言する
                    if(newTarget != null && newTarget == currentTarget && i==0) {
                    	i=1;
                    	return new VoteContentBuilder(currentTarget.agent);
                    }
                }
            }
        }
        return null;

    }

}
