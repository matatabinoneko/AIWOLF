package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
/**
 *  ささやき発言を管理するクラス
 * @author carlo
 *
 */
public class WhisperInfo {
	protected GameInfoManager gameInfoMgr;
	
	/** 最新のみ保存 <発言者,投票先> */
	private Map<Agent,Agent> todaysVoteMap;
	/** 最新のみ保存 <発言者,襲撃先>  */
	private Map<Agent,Agent> todaysAttackMap;
	/** 誰がどの役職を騙るつもりなのか <発言者,役職><br>人狼COは省く */
	private Map<Agent,Role> fakeCOMap;
	/** 各エージェントがどのエージェントをどの役職だと思っているかのマップ */
	protected Map<Agent,Map<Agent,Role>> estimateMap;
	
	public WhisperInfo(GameInfoManager gameInfoMgr){
		this.gameInfoMgr=gameInfoMgr;
		this.estimateMap=new HashMap<Agent,Map<Agent,Role>>();
		
		//dayStartで初期化すると不具合が...
		todaysVoteMap=new HashMap<Agent,Agent>();
		todaysAttackMap=new HashMap<Agent,Agent>();
		fakeCOMap=new HashMap<Agent,Role>();
	}
	
	public void dayStart(){
		todaysVoteMap=new HashMap<Agent,Agent>();
		todaysAttackMap=new HashMap<Agent,Agent>();
	}
	public void addWhisperTalk(Talk talk){
		Content utterance=new Content(talk.getText());
		Agent speaker=talk.getAgent();
		switch (utterance.getTopic()){
		case COMINGOUT:
			if(utterance.getRole()!=Role.WEREWOLF) fakeCOMap.put(speaker, utterance.getRole());
			break;
		case VOTE:
			todaysVoteMap.put(speaker, utterance.getTarget());
			break;
		case ATTACK:
			todaysAttackMap.put(speaker, utterance.getTarget());
			break;
		case ESTIMATE:
			Agent target=utterance.getTarget();
			Role estimateRole=utterance.getRole();
			if(estimateMap.containsKey(speaker)){
				estimateMap.get(speaker).put(target, estimateRole);
			}
			else{
				Map<Agent,Role> map=new HashMap<Agent,Role>();
				map.put(target,estimateRole);
				estimateMap.put(speaker, map);
			}
			break;
		default:
			break;

		}
	}
	
	
	public Map<Agent,Agent> getTodaysVoteMap(){
		return todaysVoteMap;
	}
	
	public Map<Agent,Agent> getTodaysAttackMap(){
		return todaysAttackMap;
	}
	
	public Map<Agent,Role> getFakeCOMap(){
		return fakeCOMap;
	}
	/** 自分を除いて、coRoleをCOする予定の人狼の数を返す */
	public int countNumOfFakeCO(Role coRole){
		int count=0;
		for(Entry<Agent,Role> entry:fakeCOMap.entrySet()){
			if(entry.getKey()==gameInfoMgr.getMyAgent()) continue;
			if(entry.getValue()==coRole){
				count++;
			}
		}
		return count;
	}
	
	/** 今日仲間がささやいた投票予定対象の中で、もっとも数が多かったAgentを返す<br>自分の発言は省く*/
	public List<Agent> searchTargetsMostVoted(){
		VoteCounter voteCounter=new VoteCounter();
		for(Map.Entry<Agent, Agent> entry :todaysVoteMap.entrySet()){
			if(entry.getKey()==gameInfoMgr.getMyAgent()) continue;
			voteCounter.addTarget(entry.getValue());
		}
		return voteCounter.getTargetsMostVoted();
	}
	/** 今日仲間がささやいた襲撃予定対象の中で、もっとも数が多かったAgentを返す<br>自分の発言は省く*/
	public List<Agent> searchTargetsMostAttacked(){
		VoteCounter voteCounter=new VoteCounter();
		for(Map.Entry<Agent, Agent> entry :todaysAttackMap.entrySet()){
			if(entry.getKey()==gameInfoMgr.getMyAgent()) continue;
			voteCounter.addTarget(entry.getValue());
		}
		return voteCounter.getTargetsMostVoted();
	}
	
	public Map<Agent,Map<Agent,Role>> getEstimateMap(){
		return estimateMap;
	}
	/** agentがestimateしている対象とその役職のMapを返す */
	public Map<Agent,Role> getEstimateMap(Agent agent){
		return estimateMap.get(agent);
	}

}
