package com.carlo.aiwolf.bayes.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractPossessed;

import com.carlo.aiwolf.base.lib.MyAbstractPossessed;
import com.carlo.aiwolf.bayes.trust.*;
import com.carlo.aiwolf.lib.ExRandom;
import com.carlo.aiwolf.lib.info.*;
import com.carlo.aiwolf.lib.talk.FakeCOTalkCreator;
import com.carlo.aiwolf.lib.talk.TalkCreator;
/**
 * @author carlo
 *
 */
public class BayesPossessed extends MyAbstractPossessed {
	
	protected GameInfoManager gameInfoMgr;
	protected TrustListManager trustListManager;
	
	private FakeCOTalkCreator talkCreator;
	private  ArrayList<Judge> fakeJudgeList;
	PossessedGameBrain gameBrain;
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		BayesPlayer.printGameNum();
		super.initialize(gameInfo, gameSetting);

		gameInfoMgr=new GameInfoManager(gameInfo,this.getMe());
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, gameInfoMgr);
		talkCreator=new FakeCOTalkCreator(this);
		gameBrain=new PossessedGameBrain(gameInfoMgr,trustListManager,talkCreator);
		
		talkCreator.setFakeRole(Role.SEER);
		fakeJudgeList=new ArrayList<Judge>();

	}
	@Override
	public void update(GameInfo gameInfo){
		try{
		super.update(gameInfo);
		gameInfoMgr.update(gameInfo);
		trustListManager.update();
		talkCreator.setVoteTarget(gameBrain.thinkVote());
		talkCreator.doComingOut();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void dayStart() {
		//System.out.println("dayStart");
		try{
		gameInfoMgr.dayStart();
		trustListManager.dayStart();
		
		Judge fakeJudge=createFakeJudge();
		if(fakeJudge!=null) fakeJudgeList.add(fakeJudge);
		talkCreator.dayStart(fakeJudgeList);
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
		return talkCreator.talk();
	}

	@Override
	public Agent vote() {
		return talkCreator.getVoteTarget();
	}
	
	private Judge createFakeJudge(){
		Judge judge=null;
		if(gameInfoMgr.getDay()>0 && talkCreator.getComingoutRole()==Role.SEER){
			Agent target = null;
			Species result;
			//黒出し
			if(gameInfoMgr.getDay()==1){
				result=Species.WEREWOLF;
				target=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST,null, true);
				//System.out.println("黒出し "+target+" "+gameInfoMgr.getCOInfo().getCoRole(target));
			}
			//囲い狙い
			else{
				result=Species.HUMAN;
				//黒判定を出されていない、信用度が低い者を占う
				List<Agent> targets= trustListManager.getSortedAgentList(true);
				for(Agent t:targets){
					if(gameInfoMgr.getAbilityInfo().getDivinedResult(t).getDivinedType()!=DivinedType.BLACK){
						target=t;
						break;
					}
				}
				if(target==null) target=trustListManager.getRoleCOAgent(TrustLevel.LOWEST,null, true);
			}
			if(target==null) target=trustListManager.getAgent(TrustLevel.LOWEST, true);
			judge=new Judge(getDay(),getMe(),target,result);
			
		}
		return judge;
	}


}
