package cit.metro.s17036;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractBodyguard;

public class Tmcit2017Bodyguard extends AbstractBodyguard {
	/* 生きてる占い師の中からランダムで守る
	 * それ以外は村人と一緒
	 * */


	Agent me;
	GameInfo currentGameInfo;
	int talkListHead;
	boolean isCO;
	VillagerLikeSelect brain;
	List<Agent> seerCOList;


	<T> T randomSelect(List<T> list) {
		if(list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	public Agent aliveSeerGuard(){
		List<Agent> selectList = new ArrayList<>();

		for(Agent aliveAgent :currentGameInfo.getAliveAgentList()){
			if(seerCOList.contains(aliveAgent)){
				selectList.add(aliveAgent);
			}
		}
		return randomSelect(selectList);
	}

	@Override
	public void dayStart() {
		talkListHead = 0;
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public String getName() {
		return "tmcit2017";
	}

	@Override
	public Agent guard() {
		return aliveSeerGuard();
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		me=gameInfo.getAgent();
		brain = new VillagerLikeSelect(me);
		talkListHead = 0;
		isCO = false;
		seerCOList = new ArrayList<>();;
	}

	@Override
	public String talk() {
		if(!isCO){
			isCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me,Role.VILLAGER);
			return new Content(builder).getText();
		}

		return Talk.OVER;
	}

	@Override
	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
		List<Talk> talkList = currentGameInfo.getTalkList();
		for(;talkListHead < talkList.size(); talkListHead++){
			Talk talk = talkList.get(talkListHead);
			Content content = new Content(talk.getText());
			if(content.getTopic() != null && Topic.COMINGOUT.equals(content.getTopic())
					&& Role.SEER.equals(content.getRole())){
				seerCOList.add(talk.getAgent());
			}
		}
	}

	@Override
	public Agent vote() {
		return brain.predict(currentGameInfo);
	}

}
