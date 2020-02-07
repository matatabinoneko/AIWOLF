package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;

/**
 * 発言管理クラス<br>
 * Vote,Estimateについて扱う。
 * COに関してはCOInfoに
 * 能力結果報告に関してはAbilityInfoに
 */
public class TalkInfo {
	/** 今日の投票先発言まとめ(最後に発言したもののみ) <発言者,投票先> */
	private Map<Agent,Agent> todaysVoteMap;
	/** 今日のTalkまとめ（SKIP,OVER除く） */
	private Map<Agent,ArrayList<Talk>> todaysTalkMap;
	/** 投票先発言をした人数 */
	private int countTodaysVotes;
	protected GameInfoManager gameInfoMgr;
	
	/** 各エージェントがどのエージェントをどの役職だと思っているかのマップ */
	protected Map<Agent,Map<Agent,Role>> estimateMap;
	
	public TalkInfo(GameInfoManager gameInfoMgr){
		this.gameInfoMgr=gameInfoMgr;
		//初期化
		todaysVoteMap=new HashMap<Agent,Agent>();
		for(Agent agent:gameInfoMgr.getAgentList()){
			todaysVoteMap.put(agent, null);
		}
		countTodaysVotes=0;
		todaysTalkMap=new HashMap<Agent,ArrayList<Talk>>();
		estimateMap=new HashMap<Agent,Map<Agent,Role>>();
	}
	public void dayStart(){
		//初期化
		todaysVoteMap=new HashMap<Agent,Agent>();
		for(Agent agent:gameInfoMgr.getAgentList()){
			todaysVoteMap.put(agent, null);
		}
		countTodaysVotes=0;
		todaysTalkMap=new HashMap<Agent,ArrayList<Talk>>();
	}
	public void addTalk(Talk talk){
		Content content=new Content(talk.getText());
		//System.out.println(content.getText());
		//System.out.println("Topic:"+content.getTopic()+",Target"+content.getTarget());
		//Utterance utterance=new Utterance(talk.getContent());
		//SkipOver以外なら保持
		if(content.getTopic()!=Topic.SKIP && content.getTopic()!=Topic.OVER){
			if(todaysTalkMap.containsKey(talk.getAgent())){
				todaysTalkMap.get(talk.getAgent()).add(talk);
			}
			else{
				ArrayList<Talk> talkList=new ArrayList<Talk>();
				talkList.add(talk);
				todaysTalkMap.put(talk.getAgent(), talkList);
			}
		}
		//投票発言なら投票マップに追加
		if(content.getTopic()==Topic.VOTE) setVote(talk.getAgent(),content.getTarget());
		//Estimateならestimateマップに追加
		if(content.getTopic()==Topic.ESTIMATE){
			Agent speaker=talk.getAgent();
			Agent target=content.getTarget();
			Role estimateRole=content.getRole();
			if(estimateMap.containsKey(speaker)){
				estimateMap.get(speaker).put(target, estimateRole);
			}
			else{
				Map<Agent,Role> map=new HashMap<Agent,Role>();
				map.put(target,estimateRole);
				estimateMap.put(speaker, map);
			}
		}
	}
	
	/** agentが今日話したTalkを返す(SKIP,OVER)を除く<br>*/
	public List<Talk> getTodaysTalkList(Agent agent){
		return todaysTalkMap.get(agent);
	}
	
	/** speakerが今日最後に投票すると話した対象を返す */
	public Agent getVote(Agent speaker){
		return todaysVoteMap.get(speaker);
	}
	public Map<Agent,Agent> getTodaysVoteMap(){
		return todaysVoteMap;
	}
	/**
	 *  今日発言数が最も少ないエージェントのリストを返す
	 * @param isExceptMe 自分を除いて探すかどうか
	 * @return
	 */
	public List<Agent> searchAgentFewestTalkNum(boolean isExceptMe){
		ArrayList<Agent> agentList=new ArrayList<Agent>();
		int minNum=1000;
		for(Agent agent:gameInfoMgr.getAliveAgentList()){
			if(isExceptMe && agent==gameInfoMgr.getMyAgent()) continue;
			int agentTalkNum=0;
			if(todaysTalkMap.containsKey(agent)) agentTalkNum=todaysTalkMap.get(agent).size();
			if(agentTalkNum<minNum){
				minNum=agentTalkNum;
				agentList.clear();
				agentList.add(agent);
			}
			else if(agentTalkNum==minNum){
				agentList.add(agent);
			}
		}
		return agentList;
	}
	
	/**
	 *  今日発言数が最も多いエージェントのリストを返す
	 * @param isExceptMe 自分を除いて探すかどうか
	 * @return
	 */
	public List<Agent> searchAgentMostTalkNum(boolean isExceptMe){
		ArrayList<Agent> agentList=new ArrayList<Agent>();
		int maxNum=0;
		for(Agent agent:gameInfoMgr.getAliveAgentList()){
			if(isExceptMe && agent==gameInfoMgr.getMyAgent()) continue;
			int agentTalkNum=0;
			if(todaysTalkMap.containsKey(agent)) agentTalkNum=todaysTalkMap.get(agent).size();
			if(agentTalkNum>maxNum){
				maxNum=agentTalkNum;
				agentList.clear();
				agentList.add(agent);
			}
			else if(agentTalkNum==maxNum){
				agentList.add(agent);
			}
		}
		return agentList;
	}
	
	/** 投票されそうなエージェント(投票先発言を見て、得票トップ)のリストを返す */
	public List<Agent> searchAgentListMostVoted(){
		//エージェントごとに票数を数える
		VoteCounter voteCounter=new VoteCounter();
		for(Map.Entry<Agent, Agent> entry:todaysVoteMap.entrySet()){
			Agent target=entry.getValue();
			if(target==null) continue;
			voteCounter.addTarget(target);
		}
		return voteCounter.getTargetsMostVoted();
	}
	
	/** 投票先について発言した人数を返す */
	public int getCountTodayVotes(){
		return countTodaysVotes;
	}
	
	public Map<Agent,Map<Agent,Role>> getEstimateMap(){
		return estimateMap;
	}
	/** agentがestimateしている対象とその役職のMapを返す */
	public Map<Agent,Role> getEstimateMap(Agent agent){
		return estimateMap.get(agent);
	}
	
	
	
	
	public void printEstimateMap(){
		for(Entry<Agent,Map<Agent,Role>> entry:estimateMap.entrySet()){
			for(Entry<Agent,Role> e:entry.getValue().entrySet()){
				System.out.println(entry.getKey()+" estimate "+e.getKey()+"="+e.getValue());
			}
		}
	}
	
	private void setVote(Agent agent,Agent voteTarget){
		if(todaysVoteMap.get(agent)==null) countTodaysVotes++;
		todaysVoteMap.put(agent, voteTarget);
	}
	

}
