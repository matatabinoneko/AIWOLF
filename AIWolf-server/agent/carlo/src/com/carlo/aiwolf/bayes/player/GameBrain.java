package com.carlo.aiwolf.bayes.player;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.aiwolf.bayes.trust.TrustLevel;
import com.carlo.aiwolf.bayes.trust.TrustListManager;
import com.carlo.aiwolf.lib.ExRandom;
import com.carlo.aiwolf.lib.info.*;
import com.carlo.aiwolf.lib.talk.TalkCreator;

/**
 * 各Roleクラスで使う共通部分をくくりだしたクラス
 * @author carlo
 *
 */

public class GameBrain {
	protected GameInfoManager gameInfoMgr;
	protected TrustListManager trustListMgr;
	protected TalkCreator talkCreater;
	/** 真占い */
	protected Agent truthSeer;
	protected Agent truthMedium;
	public GameBrain(GameInfoManager gameInfoMgr,TrustListManager trustListMgr,TalkCreator talkCreater){
		truthSeer=null;
		truthMedium=null;
		this.gameInfoMgr=gameInfoMgr;
		this.trustListMgr=trustListMgr;
		this.talkCreater=talkCreater;
		//System.out.println("Call newGameBrain()");
	}
	/** 投票先を考えて返す */
	public Agent thinkVote(){

		/*System.out.println("In thinkVote() day:"+gameInfoMgr.getDay());
		
		System.out.println("占いCO:"+gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.SEER)+
				" 霊能CO:"+gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM)+
				" 狩人CO:"+gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.BODYGUARD));*/
				
		searchTruthSeer();
		searchTruthMedium();
		
		//System.out.println("In GameBrain.thinkVote day:"+this.gameInfoMgr.getDay()+" role:"+this.gameInfoMgr.getMyRole());
		//trustListMgr.printTrustListByForce();
		
		List<Agent> voteTargets;
		List<Agent> dontVoteTargets=new ArrayList<Agent>();
		AbilityInfo abilityInfo=gameInfoMgr.getAbilityInfo();
		//真占いがいるなら
		if(truthSeer!=null){
			//真占いの黒は投票
			voteTargets=abilityInfo.searchSeerDivinedAgents(truthSeer, Species.WEREWOLF);
			if(voteTargets.size()>0) return ExRandom.select(voteTargets, talkCreater.getVoteTarget());
			//白には投票しない
			dontVoteTargets.add(truthSeer);
			dontVoteTargets.addAll(abilityInfo.searchSeerDivinedAgents(truthSeer, Species.HUMAN));
		}
		//真霊能がいるなら
		if(truthMedium!=null){
			dontVoteTargets.add(truthMedium);
		}
		
		//破綻者に投票
		voteTargets=abilityInfo.getNonVillagerTeamAgents();
		for(Agent agent:voteTargets){
			if(gameInfoMgr.isAlive(agent)){
				//System.out.println("破綻者に投票 "+agent);
				return agent;
			}
		}
		/**霊能3COならローラー */
		
		if(gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM)>2){
			Agent target=trustListMgr.getRoleCOAgent(TrustLevel.LOWEST, Role.MEDIUM, true);
			if(target!=gameInfoMgr.getMyAgent() && target!=null){
				//System.out.println("霊ロラ "+target);
				return target;
			}
		}

		//信用度30以下は投票
		voteTargets=trustListMgr.getSortedAgentList(true, 30);
		for(Agent target:voteTargets){
			//投票してはいけない相手でなければ
			if(dontVoteTargets.contains(target)==false){
				//System.out.println("信用度が相当低い "+target);
				return target;
			}
		}
		
		/** 4日目までに暫定黒がいれば投票 */
		if(gameInfoMgr.getDay()>0 && gameInfoMgr.getDay()<5){
			for(Agent agent:gameInfoMgr.getCOInfo().getCOAgentList(null, true, true)){
				if(abilityInfo.getDivinedResult(agent).getDivinedType()==DivinedType.BLACK){
					//System.out.println("暫定黒に投票 "+agent);
					return agent;
				}
			}
		}

		
		/** 1,2日目は純粋にグレラン。いなければ次へ */
		
		if(gameInfoMgr.getDay()<3){
			voteTargets=abilityInfo.searchDivinedAgents(DivinedType.NONE);
			for(Agent target:voteTargets){
				//投票してはいけない相手でなければ
				if(dontVoteTargets.contains(target)==false) {
					//System.out.println("グレラン "+target);
					return target;
				}
			}
		}
		
		/** 3,4日目は非CO者から信用度が低いものを。いなければ次へ */
		if(gameInfoMgr.getDay()<5) {
			voteTargets=trustListMgr.getSortedRoleCOAgentList(null, true);
			for(Agent target:voteTargets){
				//投票してはいけない相手でなければ
				if(dontVoteTargets.contains(target)==false) {
					//System.out.println("非CO者から信用度が低い人物に "+target);
					return target;
				}
			}
		}
		/** それ以降は信用度が低いものを */
		voteTargets= trustListMgr.getSortedAgentList(true);
		for(Agent target:voteTargets){
			//投票してはいけない相手でなければ
			if(dontVoteTargets.contains(target)==false) {
				//System.out.println("全員の中で信用度が低い人物に "+target);
				return target;
			}
		}
	
		return trustListMgr.getAgent(TrustLevel.LOWEST, true);
	}
	public Agent getTruthSeer(){
		return truthSeer;
	}
	public Agent getTruthMedium(){
		return truthMedium;
	}
	/** 正答率確かめ用 */
	public void finish(GameInfo gameInfo){
/*		System.out.println("finish "+gameInfoMgr.getMyAgent()+" "+gameInfoMgr.getMyRole());
		trustListMgr.printTrustListByForce();
		if(truthSeer!=null) System.out.println(gameInfo.getRole()+"estimate SEER, result:"+gameInfo.getRoleMap().get(truthSeer));
		if(truthMedium!=null) System.out.println(gameInfo.getRole()+"estimate MEDIUM, result:"+gameInfo.getRoleMap().get(truthMedium));*/
	}
	
	/** 真占いを探す */
	protected void searchTruthSeer(){
		if(truthSeer==null){
			if(gameInfoMgr.getMyRole()==Role.SEER) {
				truthSeer=gameInfoMgr.getMyAgent();
				return;
			}
			Agent mostTrustedSeer=trustListMgr.getRoleCOAgent(TrustLevel.HIGHEST, Role.SEER,false);
			if(mostTrustedSeer!=null &&  trustListMgr.getTrustPoint(mostTrustedSeer)>85){
				truthSeer=mostTrustedSeer;
				talkCreater.setEstimate(truthSeer, Role.SEER);
				//System.out.println("真占い "+truthSeer);
				return;
			}
		}
	}
	/** 真霊能を探す */
	protected void searchTruthMedium(){
		if(truthMedium==null){
			//自分が霊能なら、真霊能は自分
			if(gameInfoMgr.getMyRole()==Role.MEDIUM) {
				truthMedium=gameInfoMgr.getMyAgent();
				return;
			}
			
			Agent mostTrustedMedium=trustListMgr.getRoleCOAgent(TrustLevel.HIGHEST, Role.MEDIUM, false);
			//信用度が70超えてれば真認定
			if(mostTrustedMedium!=null && trustListMgr.getTrustPoint(mostTrustedMedium)>75){
				truthMedium=mostTrustedMedium;
				trustListMgr.setTruthMedium(truthMedium);
				//System.out.print(truthMedium);
				talkCreater.setEstimate(truthMedium, Role.MEDIUM);
				return;
			}
			//4日目以降で一人COかつ信用度50超えなら真認定
			if(mostTrustedMedium!=null && gameInfoMgr.getDay()>3 && gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM)==1 &&
					trustListMgr.getTrustPoint(mostTrustedMedium)>50){
				truthMedium=mostTrustedMedium;
				trustListMgr.setTruthMedium(truthMedium);
				//System.out.println("真霊能 "+truthMedium);
				talkCreater.setEstimate(truthMedium, Role.MEDIUM);
				return;
			}
		}
	}

}
