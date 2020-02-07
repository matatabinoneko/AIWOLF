package tkg.aiwolf.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameTalk;
import tkg.aiwolf.model.TFAFGameModel;

public class VoteLast3PPTacticbyVil extends TFAFTargetTactic {

    @Override
    public GameAgent targetImpl(TFAFGameModel model, Game game) {

        /*
         * 裏切者ではない者を返す
         */
        List<GameAgent> others = game.getAliveOthers();
        List<GameTalk> divined = game.getAllTalks().filter(t -> t.getDay() == 1 && t.getTopic() == Topic.DIVINED).collect(Collectors.toList());

        //裏切り者候補(人外CO)が何人いるか数える
        int possessed_num = 0;
        for(GameAgent gp : game.getAlives()) {
            if (gp.coRole == Role.POSSESSED || gp.coRole == Role.WEREWOLF) {
                possessed_num++;
            }
        }
        //もし裏切り者候補が2人いる場合、前日に占いCOしてる方を投票先から外す
        if(possessed_num ==2) {
        	for (GameAgent gp : game.getAlives()) {
        		for (GameTalk t : divined) {
        			if (gp==t.getTalker()) {
        				others.remove(gp);
        			}
        		}
        	}
        }
        //もし裏切り者COが2人いなければ、前日占いCOしてない人を投票先から外す
        else {
        	for (GameAgent gp : game.getAlives()) {
        		for (GameTalk t : divined) {
        			if (gp==t.getTalker()) {
        				others.remove(gp);
        			}
        		}
            }
        }
        if (others.size() == 1) {
            GameAgent agent = others.get(0);
            return agent;
        }

        return null;
    }

}
