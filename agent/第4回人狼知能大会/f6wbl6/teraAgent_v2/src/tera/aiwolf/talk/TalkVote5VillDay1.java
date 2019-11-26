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

public class TalkVote5VillDay1 extends TFAFTalkTactic {

    private GameAgent currentTarget;
    // kasukaメソッド
    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        // 占い結果後に発言
        if (game.getDay() == 1) {
            if (currentTarget == null) {
                // 初期、最も強い人に投票宣言
                List<GameAgent> agents = game.getAliveOthers();
                double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount(); // 勝率を計算
                currentTarget = Collections.max(agents, Comparator.comparing(x -> winCount[x.getIndex()])); // 最高勝率のエージェント選択
//                log("Day1:初期殴り先：" + currentTarget);
//                return new VoteContentBuilder(currentTarget.agent);
                return null; // 初日に占いが始まるまでは発言しない
            } else {
                // 占い結果が判ったら、それに合わせて投票先を変える
                List<GameTalk> divine = game.getAllTalks()
                    .filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.DIVINED)
                    .collect(Collectors.toList());

                if (!divine.isEmpty()) {
                    GameAgent newTarget = null;
                    List<GameAgent> candidates = game.getAliveOthers();
                    firstDayCO = game.getAliveOthers();
                    firstDayCO.clear();

                    if (divine.size()==1 && divine.get(0).getResult()==Species.WEREWOLF) { // 占い師COが一人で黒判定の場合
                    	newTarget = divine.get(0).getTarget(); // 黒出しされた人に投票
                    	candidates.clear();
                    	numFirstCO = 1;
                    	firstDayCO.add(divine.get(0).getTalker());
                    }
                    else if (divine.size()==1 && divine.get(0).getResult()!=Species.WEREWOLF) { // 占い師COが一人だけど白判定の場合
                        GameAgent whitevillager = divine.get(0).getTarget(); // 白出しされた村人
                        GameAgent seer = divine.get(0).getTalker(); // 占い師
                        candidates.remove(whitevillager);
                        candidates.remove(seer);
                    	numFirstCO = 1;
                    	firstDayCO.add(divine.get(0).getTalker());
                    }
                    else if (divine.size()==2) { // 占い師COが二人の場合
                    	// 占い師COした二人を除く二人から選ぶ
                        GameAgent seer1 = divine.get(0).getTalker(); // 占い師CO1
                        GameAgent seer2 = divine.get(1).getTalker(); // 占い師CO2
                    	firstDayCO.add(seer1);
                    	firstDayCO.add(seer2);
                        candidates.remove(seer1);
                        candidates.remove(seer2);
                    	numFirstCO = 2;
                    }
                    else if (divine.size()==3) { // 占い師COが三人の場合
                    	// 占い師COした三人の中から選ぶ
                    	candidates.clear();
                    	for(GameTalk t : divine) {
                    		candidates.add(t.getTalker());
                    	}
                    	numFirstCO = 3;
                    	firstDayCO = candidates;
                    }
                    if (!candidates.isEmpty()) { // 占い師COが複数の場合にはEvil Scoreを利用(別にランダムでもいいけど)
                        double[] evilScore = model.getEvilScore();
                        Utils.sortByScore(candidates, evilScore, false);
                        newTarget = candidates.get(0);
                        log("Day1:EvilScore高めの人を殴る");
                    }

                    if (newTarget != null ) {//&& newTarget != currentTarget) { //初日に必ず投票先の発言をする
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
