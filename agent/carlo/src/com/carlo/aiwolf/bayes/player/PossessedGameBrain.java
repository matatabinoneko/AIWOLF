package com.carlo.aiwolf.bayes.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

import com.carlo.aiwolf.bayes.trust.TrustLevel;
import com.carlo.aiwolf.bayes.trust.TrustListManager;
import com.carlo.aiwolf.lib.info.AbilityInfo;
import com.carlo.aiwolf.lib.info.AbilityResult;
import com.carlo.aiwolf.lib.info.DivinedType;
import com.carlo.aiwolf.lib.info.GameInfoManager;
import com.carlo.aiwolf.lib.talk.TalkCreator;

public class PossessedGameBrain extends GameBrain {

	public PossessedGameBrain(GameInfoManager gameInfoMgr, TrustListManager trustListMgr, TalkCreator talkCreater) {
		super(gameInfoMgr, trustListMgr, talkCreater);
		// TODO 自動生成されたコンストラクター・スタブ
	}
	@Override
	public Agent thinkVote(){
		searchTruthSeer();
		searchTruthMedium();
		List<Agent> voteTargets;
		List<Agent> dontVoteTargets=new ArrayList<Agent>();
		AbilityInfo abilityInfo=gameInfoMgr.getAbilityInfo();
		
		//自分の占い結果を取得し、人狼判定があるなら投票
		for(AbilityResult abilityResult:gameInfoMgr.getAbilityInfo().getAbilityResultManager(gameInfoMgr.getMyAgent()).getAbilityResultList()){
			if(abilityResult.getTopic()!=Topic.DIVINED) continue;
			if(abilityResult.getSpecies()==Species.WEREWOLF && this.gameInfoMgr.isAlive(abilityResult.getTarget())){
				return abilityResult.getTarget();
			}
			else{
				dontVoteTargets.add(abilityResult.getTarget());
			}
		}
		
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
		
		/** それ以降は信用度が高いものを */
		voteTargets= trustListMgr.getSortedAgentList(true);
		Collections.reverse(voteTargets);
		for(Agent target:voteTargets){
			//投票してはいけない相手でなければ
			if(dontVoteTargets.contains(target)==false) {
				//System.out.println("全員の中で信用度が高い人物に "+target);
				return target;
			}
		}
		
		return trustListMgr.getAgent(TrustLevel.HIGHEST, true);
	}

}
