package com.carlo.aiwolf.bayes.player;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractSeer;

import com.carlo.aiwolf.base.lib.MyAbstractSeer;
import com.carlo.aiwolf.bayes.trust.TrustLevel;
import com.carlo.aiwolf.bayes.trust.TrustListManager;
import com.carlo.aiwolf.lib.info.*;
import com.carlo.aiwolf.lib.talk.COTalkCreator;

public class BayesSeer extends MyAbstractSeer {

	private GameInfoManager gameInfoMgr;
	private TrustListManager trustListManager;

	/** 占ったエージェント */
	private ArrayList<Agent> myJudgeAgents;

	COTalkCreator talkCreater;
	GameBrain gameBrain;

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		BayesPlayer.printGameNum();
		super.initialize(gameInfo, gameSetting);
		gameInfoMgr=new GameInfoManager(gameInfo,this.getMe());
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, gameInfoMgr);
		myJudgeAgents=new ArrayList<Agent>();

		talkCreater=new COTalkCreator(this);
		gameBrain=new GameBrain(gameInfoMgr,trustListManager,talkCreater);
	}
	@Override
	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		//readTalkList();
		gameInfoMgr.update(gameInfo);
		trustListManager.update();

		talkCreater.setVoteTarget(gameBrain.thinkVote());

	}

	@Override
	public void dayStart() {
		super.dayStart();

		gameInfoMgr.dayStart();
		trustListManager.dayStart();
		System.out.println(getLatestDayGameInfo().getDivineResult());
		talkCreater.dayStart(getLatestDayGameInfo().getDivineResult());

		if(this.getDay()>0){
			Judge yesterdayJudge=getLatestDayGameInfo().getDivineResult();
			if(yesterdayJudge!=null){
				myJudgeAgents.add(yesterdayJudge.getTarget());
			}
		}

		//0日目でCO
		if(this.getDay()>=0){
			talkCreater.doComingOut();
		}
	}

	@Override
	public void finish() {
		//trustListManager.printTrustList();
	}


	@Override
	public String talk() {
		return talkCreater.talk();
	}

	@Override
	public Agent vote() {
		return talkCreater.getVoteTarget();
	}
	@Override
	public Agent divine() {
		//非CO者から選ぶ
		List<Agent> candidates;
		if(getDay()<5){
			candidates=trustListManager.getSortedRoleCOAgentList(null, true);
			candidates.removeAll(myJudgeAgents);
			if(gameInfoMgr.getTalkInfo().getCountTodayVotes()>gameInfoMgr.getAliveAgentList().size()/2){
				candidates.removeAll(gameInfoMgr.getTalkInfo().searchAgentListMostVoted());
			}
			if(candidates.size()>0) return candidates.get(0);
		}

		//5日目以降はCO者も含める
		candidates=trustListManager.getSortedAgentList(true);
		candidates.removeAll(myJudgeAgents);
		if(gameInfoMgr.getTalkInfo().getCountTodayVotes()>gameInfoMgr.getAliveAgentList().size()/2){
			candidates.removeAll(gameInfoMgr.getTalkInfo().searchAgentListMostVoted());
		}
		if(candidates.size()>0) return candidates.get(0);

		return trustListManager.getAgent(TrustLevel.LOWEST, true);
	}
	/*
	private Agent thinkVote(){
		//人狼を見つけたら人狼から選ぶ
		for(Judge judge:getMyJudgeList()){
			if(judge.getResult()==Species.WEREWOLF && gameInfoMgr.isAlive(judge.getTarget())){
				return judge.getTarget();
			}
		}
		//投票しないリスト
		List<Agent> dontVoteTargets=new ArrayList<Agent>();
		dontVoteTargets.addAll(gameInfoMgr.getAbilityInfo().searchSeerDivinedAgents(getMe(), Species.HUMAN));

		//最初は非CO者から選ぶ
		List<Agent> candidates=trustListManager.getSortedRoleCOAgentList(null, true);
		if(getDay()<4){
			for(Agent agent:candidates){
				//System.out.println(trustListManager.getTrustPoint(agent)+" "+agent);
				//自分が占っていない候補者を選ぶ
				if(dontVoteTargets.contains(agent)==false) return agent;
			}
		}
		
		
		//4日目以降は対抗COがいたら投票
		candidates=gameInfoMgr.getCOInfo().getCOAgentList(Role.SEER, true, true);
		if(candidates.size()>0) return candidates.get(0);
		
		//いなければ全員から
		candidates=trustListManager.getSortedAgentList(true);
		for(Agent agent:candidates){
			//System.out.println(trustListManager.getTrustPoint(agent)+" "+agent);
			//自分が占っていない候補者を選ぶ
			if(dontVoteTargets.contains(agent)==false) return agent;
		}


		return trustListManager.getAgent(TrustLevel.LOWEST, true);
	}
	*/

	/*
	private void readTalkList(){
		List<Talk> talkList=this.getLatestDayGameInfo().getTalkList();
		for(int i=readTalkNum;i<talkList.size();i++){
			Talk talk=talkList.get(i);
			Utterance utterance=new Utterance(talk.getContent());
			switch (utterance.getTopic()){
			case COMINGOUT:
				switch (utterance.getRole()){
				case MEDIUM:
					break;
				default:
					break;
				}
				break;
			case DIVINED:
				break;
			case INQUESTED:
				break;
			default:
				break;
			}
			readTalkNum++;
		}
	}
	*/

}
