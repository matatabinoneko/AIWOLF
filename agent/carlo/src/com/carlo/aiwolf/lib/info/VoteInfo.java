package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Vote;
/**
 * 投票管理クラス
 * 投票についての様々な情報を入手するメソッドを持つ
 * @author carlo
 *
 */
public class VoteInfo {
	/** 各投票日ごとの投票リスト index:1が1日目の投票リスト。*/
	protected ArrayList<List<Vote>> voteLists=new ArrayList<List<Vote>>();
	//protected GameInfoManager gameInfoMgr;
	
	public void addList(List<Vote> voteList){
		voteLists.add(voteList);
	}
	
	
	/** targetに投票したエージェントを全て探して返す。複数回投票していたら、その回数分リストに入れる。 */
	
	public List<Agent> searchVoter(Agent target){
		
		ArrayList<Agent> voter=new ArrayList<Agent>();
		for(List<Vote> voteList:voteLists){
			for(Vote vote:voteList){
				if(vote.getTarget()==target){
					voter.add(vote.getAgent());
				}
			}
		}
		return voter;
	}
	/** targetに投票したことのあるエージェントを全て探して返す。複数回投票していたら、その回数分リストに入れる。
	 * @return Map<投票したことのあるエージェント,その日> */
	public Map<Agent,Integer> searchVoterMap(Agent target){
		
		//ArrayList<Agent> voter=new ArrayList<Agent>();
		HashMap<Agent,Integer> map=new HashMap<>();
		for(List<Vote> voteList:voteLists){
			for(Vote vote:voteList){
				if(vote.getTarget()==target){
					map.put(vote.getAgent(), vote.getDay());
				}
			}
		}
		return map;
	}
	
	/** day日にtargetに投票したエージェントの配列を返す */
	public List<Agent> searchVoter(int day,Agent target){
		ArrayList<Agent> voters=new ArrayList<Agent>();
		for(Vote vote:voteLists.get(day)){
			if(vote.getTarget()==target){
				voters.add(vote.getAgent());
			}
		}
		return voters;
	}
	/** agentがday日に投票したターゲットを返す */
	public Agent getVoteTarget(int day,Agent agent){
		for(Vote vote:voteLists.get(day)){
			if(vote.getAgent()==agent){
				return vote.getTarget();
			}
		}
		return null;
	}
	
	
	public List<List<Vote>> getVoteLists(){
		return voteLists;
	}

}
