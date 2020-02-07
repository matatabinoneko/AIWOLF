package cit.metro.s17036;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractMedium;

public class Tmcit2017Medium extends AbstractMedium {
	/* 最初に人狼or占い師COerが死ぬか、他のやつに霊媒COされるまでは村人と一緒
	 * 人狼が死んだら霊媒師COして死ぬまで霊媒結果をつぶやき続ける。
	 * 占い師COerが死んだら霊媒師COして死ぬまで霊媒結果をつぶやき続ける。
	 * 他のやつに霊媒師COされても霊媒師COして死ぬまで霊媒結果をつぶやき続ける。
	 * */

	Agent me;
	GameInfo currentGameInfo;
	int talkListHead;
	VillagerLikeSelect brain;
	List<Agent> seerCOerList;

	boolean isVillagerCO, isMediumCO, isCOTiming;
	Deque<Judge> judgeQueue;


	@Override
	public void dayStart() {
		Judge judge = currentGameInfo.getMediumResult();
		if(judge != null){
			Species result = judge.getResult();
			if(Species.WEREWOLF.equals(result)){
				isCOTiming = true;
			}
			if(seerCOerList.contains(judge.getTarget())){
				isCOTiming = true;
			}
		}
		judgeQueue.add(judge);
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
		me = gameInfo.getAgent();
		brain = new VillagerLikeSelect(me);
		seerCOerList = new ArrayList<>();
		talkListHead = 0;
		isVillagerCO = false;
		isMediumCO = false;
		isCOTiming = false;

		judgeQueue = new LinkedList<>();
	}

	@Override
	public String talk() {
		if(!isVillagerCO){
			isVillagerCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me,Role.VILLAGER);
			return new Content(builder).getText();
		}
		if(isCOTiming && (!isMediumCO)){
			isMediumCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me,Role.MEDIUM);
			return new Content(builder).getText();
		}
		if(isMediumCO && !judgeQueue.isEmpty()){
			Judge aJudge =judgeQueue.pop();
			if(aJudge != null){
				ContentBuilder builder = new IdentContentBuilder(aJudge.getTarget(), aJudge.getResult());
				return new Content(builder).getText();
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
			if(Topic.COMINGOUT.equals(topic) && Role.MEDIUM.equals(role)){
				isCOTiming = true;
			}
			if(Topic.COMINGOUT.equals(topic) && Role.SEER.equals(role)){
				seerCOerList.add(talk.getAgent());
			}
		}
	}

	@Override
	public Agent vote() {
		return brain.predict(currentGameInfo);
	}
}

