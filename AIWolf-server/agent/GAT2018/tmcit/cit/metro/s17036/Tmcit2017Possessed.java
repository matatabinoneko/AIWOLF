package cit.metro.s17036;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractPossessed;

public class Tmcit2017Possessed extends AbstractPossessed {
	/* 初手占いCOして占いCOした奴以外の全員に対して白を吐き続けるだけ
	 * */


	Agent me;
	GameInfo currentGameInfo;
	int talkListHead;

	boolean isCO;
	boolean todayDivineTalked;
	VillagerLikeSelect brain;
	List<Agent> divinedList;
	List<Agent> seerCOerList;

	boolean isAlive(Agent agent) {
		return currentGameInfo.getAliveAgentList().contains(agent);
	}

	<T> T randomSelect(List<T> list) {
		if(list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	public Agent randomDivineSelect(){
		List<Agent> aliveList = currentGameInfo.getAliveAgentList();
		for(Agent agent: divinedList){
			aliveList.remove(agent);
		}
		for(Agent seer: seerCOerList){
			aliveList.remove(seer);
		}
		aliveList.remove(me);
		if(aliveList.size() != 0){
			Agent result = randomSelect(aliveList);
			divinedList.add(result);
			return result;
		}
		else{
			return null;
		}
	}


	@Override
	public void dayStart() {
		if(currentGameInfo.getDay() == 0){
			todayDivineTalked = true;
		}
		else{
			todayDivineTalked = false;
		}
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
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		me=gameInfo.getAgent();
		talkListHead = 0;
		isCO = false;
		todayDivineTalked = false;

		brain = new VillagerLikeSelect(me);
		divinedList=new ArrayList<>();
		seerCOerList = new ArrayList<>();
	}

	@Override
	public String talk() {
		if(!isCO) {
			isCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me, Role.SEER);
			Content content = new Content(builder);
			return content.getText();
		}
		else {
			if(!todayDivineTalked) {
				todayDivineTalked = true;
				Agent agent = randomDivineSelect();
				if(agent != null){
					ContentBuilder builder=new DivinedResultContentBuilder(agent, Species.HUMAN);
					return new Content(builder).getText();
				}
				else{
					return Talk.OVER;
				}
			}
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
			Topic topic = content.getTopic();
			Role role = content.getRole();
			if(Topic.COMINGOUT.equals(topic) && Role.SEER.equals(role)){
				seerCOerList.add(talk.getAgent());
			}
		}
	}

	@Override
	public Agent vote() {
		List<Agent> aliveList = new ArrayList<Agent>(currentGameInfo.getAliveAgentList());
		for(Agent seer: seerCOerList){
			aliveList.remove(seer);
		}
		for(Agent divined: divinedList){
			aliveList.remove(divined);
		}
		if(aliveList.isEmpty()){
			return null;
		}
		else{
			return randomSelect(aliveList);
		}
	}
}
