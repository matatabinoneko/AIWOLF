package tkg.aiwolf.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;
import tkg.aiwolf.model.TFAFGameModel;

public class TalkVote5VillDay2 extends TFAFTalkTactic {

    private GameAgent currentTarget;

    int coflag = 0;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getDay() == 2) {

        	//2占いCOで2日目に突入した場合、かつ、占い師が1人でも生きている場合、狼陣営のフリをしてパワープレイを妨害する
        	List<GameTalk> divined = game.getAllTalks().filter(t -> t.getDay() == 1 && t.getTopic() == Topic.DIVINED).collect(Collectors.toList());
    		if(divined.size()==2) { //占い師CO２なら
    			List<GameAgent> candidates = game.getAliveOthers();
    			if(coflag==0) {
    				coflag = 1;
    				return new ComingoutContentBuilder(game.getSelf().agent, Role.WEREWOLF);
    			}
    		else if(coflag == 1) {
    				coflag = 2;
	    			//生き残ってるエージェントで占い師COしていない人に投票する
	    			for (GameTalk t : divined) {
	    				for(GameAgent a:candidates) {
	    					if(t.getTalker()!=a) {
	    						return new VoteContentBuilder(a.agent);
	    					}
	    				}
	    			}
    			}
    		}

    		else {
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
        }
        return null;

    }

}
