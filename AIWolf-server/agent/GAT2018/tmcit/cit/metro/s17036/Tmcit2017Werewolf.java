package cit.metro.s17036;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractWerewolf;

public class Tmcit2017Werewolf extends AbstractWerewolf {

	/* 味方以外をランダムに襲う。狂人判定もし、狂人確定したら占い師を狙う
	 *
	 * */

	Agent me;
	GameInfo currentGameInfo;
	int talkListHead, whisperListHead;
	List<Agent> wolfList;
	List<Agent> seerCOerList;
	Agent possessed;
	int possessedTalkStat;
	boolean isVillagerCO, isWolfCO;



	<T> T randomSelect(List<T> list) {
		if(list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	@Override
	public Agent attack() {
		List<Agent> aliveAgentList = currentGameInfo.getAliveAgentList();
		for(Agent wolf: wolfList){
			if(aliveAgentList.contains(wolf)){
				aliveAgentList.remove(wolf);
			}
		}
		aliveAgentList.remove(me);

		if(possessed != null){
			List<Agent> seersList = new ArrayList<Agent>(seerCOerList);
			seersList.remove(possessed);
			for(Agent wolf: wolfList){
				if(seersList.contains(wolf)){
					seersList.remove(wolf);
				}
			}
			List<Agent> liveWhiteSeerList = new ArrayList<Agent>();
			for(Agent seer: seersList){
				if(currentGameInfo.getAliveAgentList().contains(seer)){
					liveWhiteSeerList.add(seer);
				}
			}
			return randomSelect(liveWhiteSeerList);
		}
		return randomSelect(aliveAgentList);
	}

	@Override
	public void dayStart() {
		talkListHead = 0;
		whisperListHead = 0;
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
		wolfList = new ArrayList<>();
		seerCOerList = new ArrayList<>();
		talkListHead = 0;
		whisperListHead = 0;
		isVillagerCO = false;
		isWolfCO = false;
		possessed = null;
		possessedTalkStat = 0;
	}

	@Override
	public String talk() {
		if(!isVillagerCO){
			isVillagerCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me,Role.VILLAGER);
			return new Content(builder).getText();
		}
		return Talk.OVER;
	}

	@Override
	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
		List<Talk> whisperList = currentGameInfo.getWhisperList();
		for(; whisperListHead < whisperList.size(); whisperListHead++){
			Talk whisper = whisperList.get(whisperListHead);
			if(!wolfList.contains(whisper.getAgent())){
				wolfList.add(whisper.getAgent());
			}
			Content content = new Content(whisper.getText());
			Topic topic = content.getTopic();
			Role role = content.getRole();
			Species species = content.getResult();
			Agent target = content.getTarget();
			if(Topic.ESTIMATE.equals(topic) && Role.POSSESSED.equals(role) && target != null){
				possessed = target;
				possessedTalkStat = 2;
			}
			if(Topic.COMINGOUT.equals(topic) && Role.POSSESSED.equals(role) && target != null){
				possessed = target;
				possessedTalkStat = 2;
			}
		}

		List<Talk> talkList = currentGameInfo.getTalkList();
		for(; talkListHead < talkList.size(); talkListHead++){
			Talk talk = talkList.get(talkListHead);
			Content content = new Content(talk.getText());
			Topic topic = content.getTopic();
			Role role = content.getRole();
			Species species = content.getResult();
			Agent target = content.getTarget();
			if(Topic.DIVINED.equals(topic) && Species.HUMAN.equals(species) && target != null && target == me
					&& !wolfList.contains(talk.getAgent())){
				possessed = talk.getAgent();
			}
			if(Topic.COMINGOUT.equals(topic) && Role.SEER.equals(role)){
				seerCOerList.add(talk.getAgent());
			}
		}
	}

	@Override
	public Agent vote() {
		List<Agent> liveAgentList = currentGameInfo.getAliveAgentList();
		for(Agent wolf: wolfList){
			if(liveAgentList.contains(wolf)){
				liveAgentList.remove(wolf);
			}
		}
		liveAgentList.remove(me);
		if(possessed != null &&liveAgentList.contains(possessed)){
			liveAgentList.remove(possessed);
		}

		return randomSelect(liveAgentList);
	}

	@Override
	public String whisper() {
		if(!isWolfCO){
			isWolfCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me, Role.WEREWOLF);
			return new Content(builder).getText();
		}
		if(possessed != null && possessedTalkStat == 0){
			possessedTalkStat = 1;
			ContentBuilder builder = new EstimateContentBuilder(possessed, Role.POSSESSED);
			return new Content(builder).getText();
		}
		if(possessed != null && possessedTalkStat == 1){
			possessedTalkStat = 2;
			ContentBuilder builder = new ComingoutContentBuilder(possessed, Role.POSSESSED);
			return new Content(builder).getText();
		}
		else{
			return Talk.OVER;
		}
	}

}
