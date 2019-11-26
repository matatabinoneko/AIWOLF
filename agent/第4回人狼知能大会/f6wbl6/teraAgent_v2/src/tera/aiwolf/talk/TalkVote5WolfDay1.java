package tera.aiwolf.talk;

import static tera.aiwolf.util.Utils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameTalk;
import tera.aiwolf.metagame.TFAFMetagameModel;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.Utils;

public class TalkVote5WolfDay1 extends TFAFTalkTactic {

    private GameAgent currentTarget;
    private boolean targetFlg = false;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        // 占い結果後に発言
        if (game.getDay() == 1) {
            if (currentTarget == null) {
                //初期、最も強い人に投票宣言
                List<GameAgent> agents = game.getAliveOthers();
                double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
                currentTarget = Collections.max(agents, Comparator.comparing(x -> winCount[x.getIndex()]));
//                if (Math.random() > 0.2) { //確率遷移、人狼は20%の確率で殴り先を宣言
//                    log("Day1:初期殴り先：" + currentTarget);
//                    return new VoteContentBuilder(currentTarget.agent);
//                }
                return null; // 80%で占い師が出るまでは発言しない
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

                    if (newTarget != null && ((newTarget != currentTarget) || (!targetFlg))) { // 確率戦略で発言しなかった場合, 全く発言せずに吊り対象を決めて沈黙することになるため仕様変更
                        currentTarget = newTarget;
                        log("Day1:新しいターゲット：" + currentTarget);
                        targetFlg = true;
                        return new VoteContentBuilder(currentTarget.agent); //初日は必ずなんらかの発言をする
                    }
                }
            }
        }
        return null;

    }

}
