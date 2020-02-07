package cit.metro.s17036;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

public class VillagerLikeSelect {

	/* その日に誰かに黒判定された奴と狼esitmateされた奴から選ぶだけ
	 * */

	Agent me;
	public  VillagerLikeSelect(Agent self) {
		me = self;
	}

	<T> T randomSelect(List<T> list) {
		if(list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	public Agent predict(GameInfo gameInfo) {
		int talkIndex = 0;
		List<Agent> voteCandidateList = new ArrayList<Agent>();
		List<Agent> aliveList = gameInfo.getAliveAgentList();
		aliveList.remove(me);

		List<Talk> talkList = gameInfo.getTalkList();
		for(; talkIndex < talkList.size(); talkIndex++) {
			Talk talk = talkList.get(talkIndex);
			Content content = new Content(talk.getText());

			Agent subject, target;
			Role role;
			Species species;
			String restring;
			int id, day;

			Species result = content.getResult();
			Topic topic = content.getTopic();
			subject = content.getSubject();
			target = content.getTarget();
			role = content.getRole();
			species = content.getResult();
			day = content.getTalkDay();
			id = content.getTalkDay();

			if(aliveList.contains(target)) {
				if(Species.WEREWOLF.equals(result)) {
					voteCandidateList.add(target);
				}
				if(Topic.ESTIMATE.equals(topic) && Species.WEREWOLF.equals(species)) {
					voteCandidateList.add(target);
				}
			}
		}

		if(voteCandidateList.size() != 0) {
			return randomSelect(voteCandidateList);
		}
		else {
			return null;
		}
	}
}
