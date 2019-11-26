package com.carlo.aiwolf.player;

import java.util.*;

import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractWerewolf;

import com.carlo.aiwolf.base.lib.MyAbstractWerewolf;
import com.carlo.aiwolf.bayes.player.BayesPlayer;
import com.carlo.aiwolf.lib.ExRandom;
import com.carlo.aiwolf.lib.info.AbilityResult;
import com.carlo.aiwolf.lib.info.DivinedType;
import com.carlo.aiwolf.lib.info.GameInfoManager;
import com.carlo.aiwolf.lib.talk.*;

public class CarloWerewolf extends MyAbstractWerewolf{
	protected GameInfoManager gameInfoMgr;
	private WerewolfTalkCreator talkCreater;
	
	private  ArrayList<Judge> fakeJudgeList;
	private Agent possessedAgent;
	private Agent mediumAgent;
	private Agent seerAgent;
	/** 占い騙りの時囲いをしたか */
	
	List<Agent> wolfList; // 人狼リスト
	
	private boolean isEncloseWerewolf;
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		BayesPlayer.printGameNum();
		super.initialize(gameInfo, gameSetting);
		gameInfoMgr=new GameInfoManager(gameInfo,this.getMe());
		fakeJudgeList=new ArrayList<Judge>();
		talkCreater=new WerewolfTalkCreator(this);
		wolfList = new ArrayList<>(gameInfo.getRoleMap().keySet());
		
		possessedAgent=null;
		mediumAgent=null;
		seerAgent=null;
		isEncloseWerewolf=false;
	}
	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		gameInfoMgr.update(gameInfo);
		talkCreater.setVoteTarget(thinkVote());
		//talkCreater.setWhisperVoteTarget(thinkVote());
		thinkPossessed();
		thinkMedium();
		thinkSeer();
		talkCreater.setAttackTarget(thinkAttack());
		
		talkCreater.setFakeRole(thinkFakeRole());
		//1日目になったらCO
		if(talkCreater.getComingoutRole()!=null && talkCreater.getComingoutRole()!=Role.VILLAGER  && getDay()>0) talkCreater.doComingOut();
		
		//System.out.println(gameInfo);
	}

	@Override
	public void dayStart() {
		gameInfoMgr.dayStart();
		
		Judge judge=thinkFakeJudge();
		if(judge!=null) fakeJudgeList.add(judge);
		talkCreater.dayStart(fakeJudgeList);
		
		Agent guardTarget=thinkFakeGuardTarget();
		//System.out.println("guard:"+guardTarget);
		if(guardTarget!=null){
			talkCreater.addMyGuardTarget(guardTarget);;
		}
		
		//System.out.println("護衛:"+gameInfoMgr.getAttackVoteInfo().searchGuardedAgent());

	}

	@Override
	public String talk() {
		return talkCreater.talk();
	}

	@Override
	public Agent vote() {
		//仲間の投票先に合わせる
		List<Agent> targets=gameInfoMgr.getWhisperInfo().searchTargetsMostVoted();
		if(targets.size()>0) return ExRandom.select(targets);
		//特になければ処刑されそうな人に合わせる
		targets=gameInfoMgr.getTalkInfo().searchAgentListMostVoted();
		if(targets.size()>0){
			//できるだけ人狼は省く
			for(Agent agent:targets){
				if(getWolfList().contains(agent)==false) return agent;
			}
			return targets.get(0);
		}
		
		//なければ普通に発言したのに投票
		return talkCreater.getVoteTarget();
		
	}
	private List<Agent> getWolfList() {
		return wolfList;
	}

	@Override
	public Agent attack() {
		return talkCreater.getAttackTarget();
	}
	@Override
	public String whisper() {
		// TODO 自動生成されたメソッド・スタブ
		return talkCreater.whisper();
	}
	private Agent thinkFakeGuardTarget(){
		if(getDay()>1){
			List<Agent> candidates=gameInfoMgr.getCOInfo().getCOAgentList(Role.SEER, true, true);
			if(candidates.size()>0) return ExRandom.select(candidates);
			candidates=gameInfoMgr.getCOInfo().getCOAgentList(Role.MEDIUM, true, true);
			if(candidates.size()>0) return ExRandom.select(candidates);
			
			candidates=gameInfoMgr.getAgentListExceptMe(true);
			return ExRandom.select(candidates);
		}
		return null;
	}
	private Judge thinkFakeJudge(){
		//偽の占い結果作成
		if(getDay()>0){
			//囲っていなければ占われていない仲間を1人囲う
			if(isEncloseWerewolf==false && ExRandom.checkHit(1, 2)){
				for(Agent werewolf:getWolfList()){
					if(werewolf==getMe() || gameInfoMgr.isAlive(werewolf)==false) continue;
					
					if(gameInfoMgr.getAbilityInfo().getDivinedResult(werewolf).getDivinedType()==DivinedType.NONE){
						isEncloseWerewolf=true;
						return new Judge(getDay(),getMe(),werewolf,Species.HUMAN);
					}
				}
			}
			//囲い以外は人狼を除いて適当に
			Agent target=null;
			
			List<Agent> candidates=gameInfoMgr.getAliveAgentList();
			candidates.removeAll(getWolfList());
			if(candidates.size()>0){
				 target=ExRandom.select(candidates);
				 //Species species=ExRandom.checkHit(1, 3) ? Species.WEREWOLF : Species.HUMAN;
				 Species species=Species.HUMAN;
				 //System.out.println(species);
				 return new Judge(getDay(),getMe(),target,species);
			}
			
			target=ExRandom.select(gameInfoMgr.getAgentListExceptMe(true));
			return new Judge(getDay(),getMe(),target,Species.HUMAN);
		}
		return null;
	}
	private void thinkPossessed(){
		//占いCO・霊能COした中で仲間を除いて間違った結果をCOしたやつを探す
		if(possessedAgent==null){
			ArrayList<Agent> agentList=new ArrayList<Agent>();
			agentList.addAll(gameInfoMgr.getCOInfo().getCOAgentList(Role.SEER, false, false));
			agentList.addAll(gameInfoMgr.getCOInfo().getCOAgentList(Role.MEDIUM, false, false));
			
			for(Agent agent:agentList){
				if(getWolfList().contains(agent)) continue;
				for(AbilityResult abilityResult: gameInfoMgr.getAbilityInfo().getAbilityResultManager(agent).getAbilityResultList()){
					if(abilityResult.getSpecies()==Species.HUMAN){
						//人狼を占って人間と言ってれば狂人
						if(getWolfList().contains(abilityResult.getTarget())){
							possessedAgent=abilityResult.getAgent();
							talkCreater.setWhisperEstimate(possessedAgent, Role.POSSESSED);
							//System.out.println("狂人"+possessedAgent);
							return;
						}
					}
					else if(abilityResult.getSpecies()==Species.WEREWOLF){
						//人間を占って人狼と言ってれば狂人
						if(getWolfList().contains(abilityResult.getTarget())==false){
							possessedAgent=abilityResult.getAgent();
							talkCreater.setWhisperEstimate(possessedAgent, Role.POSSESSED);
							//System.out.println("狂人"+possessedAgent);
							return;
						}
					}
				}
			}
		}
		
	}
	private void thinkMedium(){
		//狂人が見つかっていれば、人狼と狂人を引いた残りが真
		if(mediumAgent==null){
			for(Agent medium:gameInfoMgr.getCOInfo().getCOAgentList(Role.MEDIUM, false, false)){
				if(getWolfList().contains(medium) || (possessedAgent!=null && possessedAgent==medium) ) continue;
				if(possessedAgent!=null) {
					//System.out.println("霊能"+medium);
					mediumAgent=medium;
					talkCreater.setWhisperEstimate(medium, Role.MEDIUM);
					return;
				}
			}
		}
	}
	private void thinkSeer(){
		//狂人が見つかっていれば、人狼と狂人を引いた残りが真
		if(seerAgent==null){
			for(Agent seer:gameInfoMgr.getCOInfo().getCOAgentList(Role.SEER, false, false)){
				if(getWolfList().contains(seer) || (possessedAgent!=null && possessedAgent==seer) ) continue;
				if(possessedAgent!=null) {
					//System.out.println("占い"+seer);
					seerAgent=seer;
					talkCreater.setWhisperEstimate(seer, Role.SEER);
					return;
				}
			}
		}
	}
	private Role thinkFakeRole(){
		/*
		//誰も占いを騙らないなら騙る
		if(gameInfoMgr.getWhisperInfo().countNumOfFakeCO(Role.SEER)==0){
			return Role.SEER;
		}
		*/
		//1~2日目に処刑されそうなら占いを騙る(占い3CO以内の時)
		if(getDay()>0 && getDay()<3 && gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.SEER)<4 &&
						 gameInfoMgr.getTalkInfo().getCountTodayVotes()>gameInfoMgr.getAliveAgentList().size()/2 &&
						 gameInfoMgr.getTalkInfo().searchAgentListMostVoted().contains(getMe())){
			 return Role.SEER;
		}
		//それ以降で処刑されそうなら狩人CO
		else if(getDay()>0 && gameInfoMgr.getTalkInfo().getCountTodayVotes()>gameInfoMgr.getAliveAgentList().size()/2 &&
				gameInfoMgr.getTalkInfo().searchAgentListMostVoted().contains(getMe())){
			return Role.BODYGUARD;
		}
		else{
			return Role.VILLAGER;
			//return null でも同じようなもの
		}
	}
	
	private Agent thinkAttack(){
		//襲撃しない場所
		List<Agent> dontAttackedTargets=new ArrayList<Agent>();
		dontAttackedTargets.addAll(getWolfList());
		if(possessedAgent!=null) dontAttackedTargets.add(possessedAgent);
		//処刑されそうなとこも襲撃を省く
		if(gameInfoMgr.getTalkInfo().getCountTodayVotes()>gameInfoMgr.getAliveAgentList().size()/2){
			dontAttackedTargets.addAll(gameInfoMgr.getTalkInfo().searchAgentListMostVoted());
		}
		//System.out.println(dontAttackedTargets);
		
		//仲間の襲撃先に合わせる
		List<Agent> fellowsTargets=gameInfoMgr.getWhisperInfo().searchTargetsMostAttacked();
		for(Agent target:fellowsTargets){
			//襲撃してはいけないところでないなら
			if(dontAttackedTargets.contains(target)==false) return target;
		}
		//真占いが判明かつ昨夜占い護衛されていなかったら襲撃
		if(seerAgent!=null && gameInfoMgr.isAlive(seerAgent) && gameInfoMgr.getAttackVoteInfo().searchGuardedAgent()!=seerAgent){
			return seerAgent;
		}
		//真霊能が判明かつ昨夜霊能護衛されていなかったら襲撃
		if(mediumAgent!=null && gameInfoMgr.isAlive(seerAgent) && gameInfoMgr.getAttackVoteInfo().searchGuardedAgent()!=mediumAgent){
			return mediumAgent;
		}
		
		//仲間を除いて選択。
		List<Agent> targets=new ArrayList<Agent>();
		targets.addAll(gameInfoMgr.getAgentListExceptMe(true));
		targets.removeAll(dontAttackedTargets);
		if(targets.size()>0) return targets.get(targets.size()-1);
		
		//リストが空なら
		targets=new ArrayList<Agent>();
		targets.addAll(gameInfoMgr.getAgentListExceptMe(true));
		targets.removeAll(dontAttackedTargets);
		if(targets.size()>0) return targets.get(targets.size()-1);
		
		return this.getMe();
	}
	
	private Agent thinkVote(){
		List<Agent> voteTargets;
		
		//占い師が4人以上でたらローラー
		if(gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.SEER)>=4){
			voteTargets=gameInfoMgr.getCOInfo().getCOAgentList(Role.SEER, true,true);
			if(voteTargets.size()>0) return voteTargets.get(0);
		}
		//霊能者が2人以上でたらローラー
		if(gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM)>=2){
			voteTargets= gameInfoMgr.getCOInfo().getCOAgentList(Role.MEDIUM, true,true);
			if(voteTargets.size()>0) return voteTargets.get(0);
		}
		
		//3日までは非CO者からランダムに選択
		if(getDay()<4){
			voteTargets=gameInfoMgr.getCOInfo().getCOAgentList(null, true,true);
			voteTargets.removeAll(getWolfList());
			if(voteTargets.size()>0) return voteTargets.get(0);
		}
		//上記条件に引っかからなければ全員からランダムに選択
		voteTargets=gameInfoMgr.getAgentListExceptMe(true);
		voteTargets.removeAll(getWolfList());
		if(voteTargets.size()>0) return voteTargets.get(0);
		else {
			return getMe();
		}
	}

}
