package com.carlo.aiwolf.bayes.player;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractVillager;

import com.carlo.aiwolf.base.lib.MyAbstractVillager;
import com.carlo.aiwolf.bayes.trust.*;
import com.carlo.aiwolf.lib.info.*;
import com.carlo.aiwolf.lib.talk.TalkCreator;
/**
 * @author carlo
 *
 */
public class BayesVillager extends MyAbstractVillager {
	
	protected GameInfoManager gameInfoMgr;
	protected TrustListManager trustListManager;
	
	private TalkCreator talkCreater;
	GameBrain gameBrain;
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		BayesPlayer.printGameNum();
		super.initialize(gameInfo, gameSetting);
		try{
		gameInfoMgr=new GameInfoManager(gameInfo,this.getMe());
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, gameInfoMgr);
		talkCreater=new TalkCreator();
		gameBrain=new GameBrain(gameInfoMgr,trustListManager,talkCreater);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	public void update(GameInfo gameInfo){
		try{
		super.update(gameInfo);
		gameInfoMgr.update(gameInfo);
		trustListManager.update();
		talkCreater.setVoteTarget(gameBrain.thinkVote());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void dayStart() {
		try{
		gameInfoMgr.dayStart();
		trustListManager.dayStart();
		talkCreater.dayStart();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void finish() {
		//gameBrain.finish(getLatestDayGameInfo());
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
