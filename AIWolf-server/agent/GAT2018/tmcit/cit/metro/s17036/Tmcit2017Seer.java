package cit.metro.s17036;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractSeer;

public class Tmcit2017Seer extends AbstractSeer {
	/* 参考資料っぽい実装。自分以外に占いCOした奴がいたらそいつは占い対象から外す
	 *
	 */

	Agent me;
	GameInfo currentGameInfo;
	Deque<Judge> myDivinationQueue;
	List<Agent> whiteList;
	List<Agent> blackList;
	List<Agent> grayList;
	List<Agent> seerCOerList;

	boolean isCO;
	int talkListHead;

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


	@Override
	public void dayStart() {
		Judge divination = currentGameInfo.getDivineResult();
		if(divination != null) {
			myDivinationQueue.offer(divination);
			Agent target = divination.getTarget();
			Species result = divination.getResult();

			grayList.remove(target);
			if (result == Species.HUMAN){
				whiteList.add(target);
			}
			else{
				blackList.add(target);
			}
		}
		talkListHead = 0;
	}

	@Override
	public Agent divine() {
		List<Agent> candidates = new ArrayList<>();

		for(Agent agent: grayList){
			if (isAlive(agent)){
				candidates.add(agent);
			}
		}
		for(Agent seerCOer: seerCOerList){
			if(candidates.contains(seerCOer)){
				candidates.remove(seerCOer);
			}
		}

		if(candidates.isEmpty()){
			return null;
		}
		return randomSelect(candidates);
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
		talkListHead = 0;
		isCO = false;
		grayList = new ArrayList<>(gameInfo.getAgentList());
		grayList.remove(me);
		whiteList = new ArrayList<>();
		blackList = new ArrayList<>();
		seerCOerList = new ArrayList<>();
		myDivinationQueue = new LinkedList<>();
	}

	public String talk() {
		if(!isCO) {
			isCO=true;
			ContentBuilder builder = new ComingoutContentBuilder(me,Role.SEER);
			return new Content(builder).getText();
		}
		else {
			if(!myDivinationQueue.isEmpty()) {
				Judge divination = myDivinationQueue.poll();
				ContentBuilder builder=new DivinedResultContentBuilder(divination.getTarget(), divination.getResult());
			    return new Content(builder).getText();
			}
		}

		return Content.OVER.getText();
	}

	@Override
	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
		List<Talk> talkList = currentGameInfo.getTalkList();
		for(; talkListHead < talkList.size(); talkListHead++){
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
		List<Agent> candidates = new ArrayList<>();
		for(Agent agent: blackList){
			if (isAlive(agent)){
				candidates.add(agent);
			}
		}
		if(candidates.isEmpty()){
			for(Agent agent: grayList){
				if (isAlive(agent)){
					candidates.add(agent);
				}
			}
		}
		if(candidates.isEmpty()){
			return null;
		}
		return randomSelect(candidates);
	}

}
