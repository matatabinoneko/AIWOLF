package com.carlo.aiwolf.bayes.player;

import org.aiwolf.sample.lib.*;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.aiwolf.base.lib.MyAbstractBodyguard;
import com.carlo.aiwolf.bayes.trust.TrustLevel;
import com.carlo.aiwolf.bayes.trust.TrustListManager;
import com.carlo.aiwolf.lib.info.*;
import com.carlo.aiwolf.lib.talk.*;

public class BayesBodyguard extends MyAbstractBodyguard {
	private GameInfoManager gameInfoMgr;
	private TrustListManager trustListManager;
	
	private COTalkCreator talkCreater;
	private GameBrain gameBrain;
	
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		gameInfoMgr=new GameInfoManager(gameInfo,gameInfo.getAgent());
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, gameInfoMgr);
		talkCreater=new COTalkCreator(this);
		gameBrain=new GameBrain(gameInfoMgr,trustListManager,talkCreater);
	}
	@Override
	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		gameInfoMgr.update(gameInfo);
		trustListManager.update();
		talkCreater.setVoteTarget(gameBrain.thinkVote());
		//CO条件設定
		TalkInfo talkInfo=gameInfoMgr.getTalkInfo();
		//1日目以降かつ半数以上が投票先発言をしていて、投票数トップならCO
		if(getDay()>0 && talkInfo.getCountTodayVotes()>gameInfo.getAliveAgentList().size()/2 && talkInfo.searchAgentListMostVoted().contains(getMe())){
			talkCreater.doComingOut();
		}
	}

	@Override
	public void dayStart() {
		gameInfoMgr.dayStart();
		trustListManager.dayStart();
		talkCreater.dayStart(this.getLatestDayGameInfo().getGuardedAgent());

	}

	@Override
	public void finish() {
		gameBrain.finish(getLatestDayGameInfo());
		trustListManager.printTrustList(getLatestDayGameInfo());
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
	public Agent guard() {
		//真占いが生きていれば真占い
		Agent truthSeer=gameBrain.getTruthSeer();
		if(truthSeer!=null && gameInfoMgr.isAlive(truthSeer)) return truthSeer;
		//真霊能が生きていれば真霊能
		Agent truthMedium=gameBrain.getTruthMedium();
		if(truthMedium!=null && gameInfoMgr.isAlive(truthMedium)) return truthMedium;
		
		//真占いの白が生きていれば護衛
		if(truthSeer!=null){
			 for(Agent agent:gameInfoMgr.getAbilityInfo().searchSeerDivinedAgents(truthSeer, Species.HUMAN)){
				 if(gameInfoMgr.isAlive(agent)) return agent;
			 }
		}
		
		//信用度高い占いが生きていれば護衛
		Agent target=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.SEER, false);
		//信用度高い霊能が生きていれば護衛
		if(gameInfoMgr.isAlive(target)==false) target=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.MEDIUM, true);
		
		for(Agent agent:gameInfoMgr.getAbilityInfo().searchDivinedAgents(DivinedType.WHITE)){
			if(gameInfoMgr.isAlive(agent)) return agent;
		}
		
		if(gameInfoMgr.isAlive(target)==false) target=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, null, true);
		return target;
	}
}
