package com.carlo.aiwolf.bayes.player;

import java.util.*;

import org.aiwolf.sample.lib.*;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.aiwolf.base.lib.MyAbstractMedium;
import com.carlo.aiwolf.bayes.trust.*;
import com.carlo.aiwolf.lib.info.*;
import com.carlo.aiwolf.lib.talk.COTalkCreator;

public class BayesMedium extends MyAbstractMedium {
	
	private GameInfoManager gameInfoMgr;
	private TrustListManager trustListManager;
	private COTalkCreator talkCreater;
	private GameBrain gameBrain;
	
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		
		gameInfoMgr=new GameInfoManager(gameInfo,me);
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, gameInfoMgr);
		talkCreater=new COTalkCreator(this);
		gameBrain=new GameBrain(gameInfoMgr,trustListManager,talkCreater);
		//inquestList.clear();
	}
	@Override
	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		
		gameInfoMgr.update(gameInfo);
		trustListManager.update();
		talkCreater.setVoteTarget(gameBrain.thinkVote());
	}

	@Override
	public void dayStart() {
		super.dayStart();
		// 霊媒結果を待ち行列に入れる
		if (currentGameInfo.getMediumResult() != null) {
			//使ってない
			//inquestList.add(currentGameInfo.getMediumResult());
			
			talkCreater.dayStart(currentGameInfo.getMediumResult());
		}
		
		gameInfoMgr.dayStart();
		trustListManager.dayStart();
		
		//2日目になったらCO
		if(day>1){
			talkCreater.doComingOut();
		}
		
	}

	@Override
	public void finish() {
		//if(truthSeer!=null) System.out.println(","+this.getLatestDayGameInfo().getRoleMap().get(truthSeer));
	}


	@Override
	public String talk() {
		return talkCreater.talk();
	}
	
	@Override
	public Agent vote() {
		return talkCreater.getVoteTarget();
	}
	


}
