package com.gmail.k14.itolab.aiwolf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;

import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.CountAgentComparator;

/**
 * 投票発言の回数を管理するクラス<br>
 * ＜key=投票先:value=回数＞のマップで管理されている
 * @author k14096kk
 *
 */
public class VoteCounter {
	
	/*key=投票対象, value=回数のバージョン*/

	/**投票発言の回数を格納する．key=対象，value=投票発言された回数*/
	 Map<Agent, Integer> voteMap = new HashMap<>();
	/**投票リクエストの回数を格納する．key=対象，value=投票発言された回数*/
	 Map<Agent, Integer> voteReqMap = new HashMap<>();
	/**投票とリクエストの合計回数を格納する．key=対象，value=投票発言された回数*/
	 Map<Agent, Integer> voteAllMap = new HashMap<>();
	 
	 
	public VoteCounter() {
		voteMap = new HashMap<>();
		voteReqMap = new HashMap<>();
		voteAllMap = new HashMap<>();
	}
	
	/**
	 * 投票発言のマップ取得
	 * @return 投票発言のマップ
	 */
	public  Map<Agent, Integer> getRemarkMap() {
		return voteMap;
	}
	
	/**
	 * 投票リクエストのマップ取得
	 * @return 投票発言のマップ
	 */
	public  Map<Agent, Integer> getRequestMap() {
		return voteReqMap;
	}
	
	/**
	 * 投票とリクエストの合計マップ取得
	 * @return 合計マップ
	 */
	public  Map<Agent, Integer> getAllMap() {
		return voteAllMap;
	}
	
	/**
	 * 投票発言回数を更新
	 * @param target :投票対象
	 */
	public void addRemark(Agent target) {
		int count = 0;
		if(voteMap.containsKey(target)) {
			count = voteMap.get(target);
		}
		count++;
		voteMap.put(target, count);
		addAll(target);
	}
	
	
	/**
	 * リクエスト投票発言回数を更新
	 * @param target :投票対象
	 */
	public void addRequest(Agent target) {
		int count = 0;
		if(voteReqMap.containsKey(target)) {
			count = voteReqMap.get(target);
		}
		count++;
		voteReqMap.put(target, count);
		addAll(target);
	}
	
	/**
	 * 投票とリクエスト投票の合計回数を更新
	 * @param target :投票対象
	 */
	public void addAll(Agent target) {
		int voteCount = 0;
		int requestCount = 0;
		if(voteMap.containsKey(target)) {
			voteCount = voteMap.get(target);
		}
		if(voteReqMap.containsKey(target)) {
			requestCount = voteReqMap.get(target);
		}
		int count = voteCount + requestCount;
		voteAllMap.put(target, count);
	}
	
	/**
	 * 投票発言回数初期化
	 */
	public void clearVoteCount() {
		voteMap.clear();
		voteReqMap.clear();
		voteAllMap.clear();
	}
	
	/**
	 * 指定エージェントの投票発言された回数取得
	 * @param agent エージェント
	 * @return 投票発言された回数
	 */
	public int getRemarkCount(Agent agent) {
		if(voteMap.containsKey(agent)) {
			return voteMap.get(agent);
		}
		return 0;
	}
	
	/**
	 * 指定エージェントの投票リクエストされた回数取得
	 * @param agent :エージェント
	 * @return 投票発言された回数
	 */
	public int getRequestCount(Agent agent) {
		if(voteReqMap.containsKey(agent)) {
			return voteReqMap.get(agent);
		}
		return 0;
	}
	
	/**
	 * 指定エージェントの投票と投票リクエストされた回数取得
	 * @param agent :エージェント
	 * @return 投票発言された回数
	 */
	public int getCount(Agent agent) {
		if(voteAllMap.containsKey(agent)) {
			return voteAllMap.get(agent);
		}
		return 0;
	}
	
	/**
	 * 投票発言された回数が少ない順のCountAgentリスト
	 * @param countMap :投票回数系マップ(voteCounterのgetメソッドを引数に)
	 * @return エージェントリスト
	 */
	public List<CountAgent> fewerCountList(Map<Agent, Integer> countMap) {
		List<CountAgent> countAgents = new ArrayList<>();
		for(Agent target: countMap.keySet()) {
			countAgents.add(new CountAgent(countMap.get(target), target));
		}
		// 昇順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		
		return countAgents;
	}
	
	/**
	 * 投票発言された回数が多い順のCountAgentリスト
	 * @param countMap :投票回数系マップ(voteCounterのgetメソッドを引数に)
	 * @return エージェントリスト
	 */
	public List<CountAgent> moreCountList(Map<Agent, Integer> countMap) {
		List<CountAgent> countAgents = fewerCountList(countMap);
		// 降順に
		Collections.reverse(countAgents);
		return countAgents;
	}
	
	/**
	 * 指定されたエージェントの中から投票発言された回数が少ない順のCountAgentリスト
	 * @param countMap :投票回数系マップ(voteCounterのgetメソッドを引数に)
	 * @param agents :指定されたエージェントリスト
	 * @return エージェントリスト
	 */
	public List<CountAgent> fewerCountList(Map<Agent, Integer> countMap, List<Agent> agents) {
		List<CountAgent> countAgents = new ArrayList<>();
		for(Agent target: agents) {
			if(countMap.containsKey(target)) {
				countAgents.add(new CountAgent(countMap.get(target), target));
			}
		}
		// 昇順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		return countAgents;
	}
	
	/**
	 * 指定されたエージェントの中から投票発言された回数が多い順のCountAgentリスト
	 * @param countMap :投票回数系マップ(voteCounterのgetメソッドを引数に)
	 * @param agents :指定されたエージェントリスト
	 * @return エージェントリスト
	 */
	public List<CountAgent> moreCountList(Map<Agent, Integer> countMap, List<Agent> agents) {
		List<CountAgent> countAgents = fewerCountList(countMap, agents);
		// 降順に
		Collections.reverse(countAgents);
		
		return countAgents;
	}
	
	/**
	 * 指定されたエージェントリストの中から投票回数が最大のエージェント
	 * @param countMap :投票回数系マップ(voteCounterのgetメソッドを引数に)
	 * @param agents :指定されたエージェントリスト
	 * @return 投票された回数が最大
	 */
	public Agent maxCountAgent(Map<Agent, Integer> countMap, List<Agent> agents) {
		Agent agent = null;
		List<CountAgent> countAgents = moreCountList(countMap, agents);
		if(!countAgents.isEmpty()) {
			agent = countAgents.get(0).agent;
		}
		return agent;
	}
}
