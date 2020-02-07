package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Vote;

public class AttackVoteInfo {
	/** 各投票日ごとの投票リスト index:1が1日目の投票リスト。*/
	//protected ArrayList<List<Vote>> voteLists=new ArrayList<List<Vote>>();
	protected GameInfoManager gameInfoMgr;
	public AttackVoteInfo(GameInfoManager gameInfoMgr){
		this.gameInfoMgr=gameInfoMgr;
	}
	
	/*
	public void addList(List<Vote> voteList){
		voteLists.add(voteList);
	}
	*/
	/**今朝(昨晩)人狼が襲撃した可能性のあるAgentのリストを返す*/
	public List<Agent> searchAttackVotedTargets(){
		VoteCounter counter=new VoteCounter();
		for(Vote vote :gameInfoMgr.getGameInfo().getAttackVoteList()){
			counter.addTarget(vote.getTarget());
		}
		return counter.getTargetsMostVoted();
	}
	/** 今朝（昨晩）護衛されていたでろうAgentを返す。<br>同数がいて判断がつかない時、襲撃成功・処刑によりいなくなって襲撃失敗した時はnullを返す */
	public Agent searchGuardedAgent(){
		if(gameInfoMgr.getGameInfo().getAttackedAgent()==null){
			List<Agent> attackTargets=searchAttackVotedTargets();
			if(attackTargets.size()==1){
				//処刑も考慮
				return (attackTargets.get(0)!=gameInfoMgr.getGameInfo().getExecutedAgent()) ? attackTargets.get(0): null ;
			}
		}
		return null;
	}

}
