package tera.aiwolf.talk;

import static tera.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameTalk;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.Utils;

public class TalkVote5VillDay2 extends TFAFTalkTactic {

    private GameAgent currentTarget;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getDay() == 2) {
        	List<GameAgent> aliveList = game.getAliveOthers();
        	List<GameAgent> candidates = null;
        	GameAgent newTarget = null;
    		Role fakeWolf = null;
        	// 占いCO1人 & 生存
        	if (numFirstCO == 1 && aliveList.contains(firstDayCO.get(0))) {
        		GameAgent seer = firstDayCO.get(0);
        		// 占い師の投票先に合わせる
                List<GameTalk> voteSeer = game.getAllTalks()
                        .filter(t -> t.getTalker()==seer &&
                        		t.getDay() == game.getDay() &&
                        		t.getTopic() == Topic.VOTE)
                        .collect(Collectors.toList());
                if (!voteSeer.isEmpty()) {
                	candidates.add(voteSeer.get(0).getTarget());
                }
        	} // voteSeer（占い師の投票先）が空のとき（占い師がまだVOTEしていない段階の時）は最後に記述
        	// 占い師CO1人 & 死亡
        	else if (numFirstCO == 1 && !aliveList.contains(firstDayCO.get(0))) {
        		// 偽人狼CO
        		fakeWolf = Role.WEREWOLF;
        		candidates = aliveList;
        	}
        	// 占い師CO2人 & 生存
        	else if (numFirstCO ==2 && aliveList.contains(firstDayCO.get(0)) && aliveList.contains(firstDayCO.get(1))){
        		// 偽人狼CO、投票は占い師以外の2人
        		fakeWolf = Role.WEREWOLF;
                candidates = aliveList;
                candidates.remove(firstDayCO.get(0));
                candidates.remove(firstDayCO.get(1));
        	}
        	// 占い師CO2人 & 片方死亡
        	else if (numFirstCO ==2 && (aliveList.contains(firstDayCO.get(0)) && !aliveList.contains(firstDayCO.get(1)))
        								||(!aliveList.contains(firstDayCO.get(0)) && aliveList.contains(firstDayCO.get(1)))) {
        		// 偽人狼CO、投票は占い師以外の2人
        		fakeWolf = Role.WEREWOLF;
                candidates = aliveList;
                GameAgent aliveSeer = null;
                // どっちかは死んでるはず
                if (aliveList.contains(firstDayCO.get(0))) {
                	aliveSeer = firstDayCO.get(1);
                }
                else if(aliveList.contains(firstDayCO.get(1))) {
                	aliveSeer = firstDayCO.get(0);
                }
                candidates.remove(aliveSeer);
        	}
        	// 占い師2人CO & 両方死亡
        	else if (numFirstCO == 2 && !aliveList.contains(firstDayCO.get(0)) && !aliveList.contains(firstDayCO.get(1))) {
        		// Scoreで決める
        		candidates = aliveList;
        	}
        	/* 偽人狼COを実装したら村人実装は終わり */

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
        return null;

    }

}
