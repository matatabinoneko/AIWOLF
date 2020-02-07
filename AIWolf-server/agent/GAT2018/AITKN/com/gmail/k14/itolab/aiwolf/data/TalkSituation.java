package com.gmail.k14.itolab.aiwolf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;

import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.CountAgentComparator;
import com.gmail.k14.itolab.aiwolf.util.Debug;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;

/**
 * 発言とリクエストの発言者と対象の現状を管理するクラス<br>
 * ＜key=発言者:value=発言対象＞のマップで管理されている<br>
 * 発言対象は最新のエージェントとなる<br>
 * 投票，占い先，護衛先，襲撃先で使用可能<br>
 * それぞれに対応するインスタンスを生成することで使用できる<br>
 * @author k14096kk
 *
 */
public class TalkSituation {
	
	/**発言の発言者を格納する．key=発言者，value=発言対象*/
	Map<Agent, Agent> remarkMap = new HashMap<>();
	/**リクエストの発言対象を格納する．key=発言者，value=発言対象*/
	Map<Agent, Agent> requestMap = new HashMap<>();

	/**
	 * 発言現状のマップ作成
	 */
	public TalkSituation() {
		remarkMap = new HashMap<>();
		requestMap = new HashMap<>();
	}
	
	/**
	 * 発言マップ取得
	 * @return 発言マップ
	 */
	public Map<Agent, Agent> getRemarkMap() {
		return this.remarkMap;
	}
	
	/**
	 * リクエストマップ作成
	 * @return リクエストマップ
	 */
	public Map<Agent, Agent> getRequestMap() {
		return this.requestMap;
	}
	
	/**
	 * 発言マップに登録
	 * @param talker :発言者
	 * @param target :発言対象
	 */
	public void setRemarkMap(Agent talker, Agent target) {
		remarkMap.put(talker, target);
	}
	
	/**
	 * リクエストマップに登録
	 * @param talker :発言者
	 * @param target :発言対象
	 */
	public void setRequestMap(Agent talker, Agent target) {
		requestMap.put(talker, target);
	}
	
	/**
	 * マップ初期化
	 */
	public void clearMap() {
		remarkMap.clear();
		requestMap.clear();
	}
	
	/**
	 * 指定エージェントが発言の対象としたエージェントを取得する
	 * @param agent :発言者
	 * @return 対象
	 */
	public Agent getRemarkTarget(Agent agent) {
		return HandyGadget.getMapValue(this.getRemarkMap(), agent);
	}
	
	/**
	 * 指定エージェントがリクエストの対象としたエージェントを取得する
	 * @param agent :発言者
	 * @return 対象
	 */
	public Agent getRequestTarget(Agent agent) {
		return HandyGadget.getMapValue(this.getRequestMap(), agent);
	}
	
	/**
	 * 指定エージェントの発言された回数取得
	 * @param target :投票対象
	 * @return 発言された回数
	 */
	public int geRemarkCount(Agent target) {
		int count = 0;
		for(Agent agent: this.getRemarkMap().keySet()) {
			if(Check.isAgent(this.getRemarkTarget(agent), target)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 指定エージェントのリクエストされた回数取得
	 * @param target :投票対象
	 * @return リクエストされた回数
	 */
	public int getRequestCount(Agent target) {
		int count = 0;
		for(Agent agent: this.getRequestMap().keySet()) {
			if(Check.isAgent(this.getRequestTarget(agent), target)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 渡されたマップから，対象の存在する数(発言の対象となった回数)をカウントしてマップに格納
	 * @param map :＜発言者:対象＞のマップ
	 * @return カウントマップ＜key=投票対象:value=回数＞
	 */
	private Map<Agent, Integer> getCountMap(Map<Agent, Agent> map) {
		// <投票対象:回数>のマップ
		Map<Agent, Integer> countMap = new HashMap<>();
		
		for(Agent agent: map.keySet()) {
			// 投票対象
			Agent target = map.get(agent);
			int count = 1;
			// 投票対象がもう登録されていれば，その回数分増加
			if(countMap.containsKey(target)) {
				count += countMap.get(target);
			}
			// 回数更新
			countMap.put(target, count);
		}
		
		return countMap;
	}
	
	/**
	 * 発言された回数が少ない順のCountAgentリスト
	 * @param map :＜発言者:対象＞のマップ
	 * @return エージェントリスト
	 */
	private List<CountAgent> fewerCountList(Map<Agent, Agent> map) {
		List<CountAgent> countAgents = new ArrayList<>();
		Map<Agent, Integer> countMap = this.getCountMap(map);
		for(Agent target: countMap.keySet()) {
			countAgents.add(new CountAgent(countMap.get(target), target));
		}
		// 昇順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		
		return countAgents;
	}
	
	/**
	 * 発言された回数が多い順のCountAgentリスト
	 * @param map :＜発言者:対象＞のマップ
	 * @return エージェントリスト
	 */
	@SuppressWarnings("unused")
	private List<CountAgent> moreCountList(Map<Agent, Agent> map) {
		List<CountAgent> countAgents = fewerCountList(map);
		// 降順に
		Collections.reverse(countAgents);
		return countAgents;
	}
	
	/**
	 * 指定されたエージェントの中から発言された回数が少ない順のCountAgentリスト
	 * @param map :回数系マップ
	 * @param agents :指定されたエージェントリスト
	 * @return エージェントリスト
	 */
	private List<CountAgent> fewerCountList(Map<Agent, Agent> map, List<Agent> agents) {
		List<CountAgent> countAgents = new ArrayList<>();
		Map<Agent, Integer> countMap = this.getCountMap(map);
		
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
	 * 指定されたエージェントの中から発言された回数が多い順のCountAgentリスト
	 * @param map :回数系マップ
	 * @param agents :指定されたエージェントリスト
	 * @return エージェントリスト
	 */
	private List<CountAgent> moreCountList(Map<Agent, Agent> map, List<Agent> agents) {
		List<CountAgent> countAgents = fewerCountList(map, agents);
		// 降順に
		Collections.reverse(countAgents);
		
		return countAgents;
	}
	
	/**
	 * 指定されたエージェントリストの中から発言された回数が最大のエージェント
	 * @param agents :指定するエージェントリスト
	 * @return 発言された回数が最大のエージェント
	 */
	public Agent maxRemarkCountAgent(List<Agent> agents) {
		Agent agent = null;
		List<CountAgent> countAgents = moreCountList(this.getRemarkMap(), agents);
		if(!countAgents.isEmpty()) {
			agent = countAgents.get(0).agent;
		}
		return agent;
	}
	
	/**
	 * 指定されたエージェントリストの中からリクエストされた回数が最大のエージェント
	 * @param agents :指定するエージェントリスト
	 * @return リクエストされた回数が最大のエージェント
	 */
	public Agent maxRequestCountAgent(List<Agent> agents) {
		Agent agent = null;
		List<CountAgent> countAgents = moreCountList(this.getRequestMap(), agents);
		if(!countAgents.isEmpty()) {
			agent = countAgents.get(0).agent;
		}
		return agent;
	}
	
	/**
	 * 指定されたエージェントリストの中から発言とリクエストされた回数が最大のエージェント
	 * @param agents :指定するエージェントリスト
	 * @return 発言とリクエストされた回数が最大のエージェント
	 */
	public Agent maxAllCountAgent(List<Agent> agents) {
		// 最大エージェント
		Agent agent = null;
		// 全部の合計値のカウントマップ
		Map<Agent, Integer> countMap = new HashMap<>();
		
		// 投票発言のカウントマップ
		Map<Agent, Integer> countRemarkMap = this.getCountMap(this.getRemarkMap());
		// 投票リクエストのカウントマップ
		Map<Agent, Integer> countRequestMap = this.getCountMap(this.getRequestMap());
		
		// 投票発言のカウントを登録
		for(Agent target: countRemarkMap.keySet()) {
			countMap.put(target, countRemarkMap.get(target));
		}
		
		
		// 投票リクエストのカウントを登録(すでにエージェントが存在すれば加算)
		for(Agent target: countRequestMap.keySet()) {
			int count = countRequestMap.get(target);
			if(countMap.containsKey(target)) {
				count += countMap.get(target);
			}
			countMap.put(target, count);
		}
		
		// カウントの順番を管理するリスト
		List<CountAgent> countAgents = new ArrayList<>();
		// 指定リストに存在するエージェントのみをcountAgentsに格納
		for(Agent target: agents) {
			if(countMap.containsKey(target)) {
				countAgents.add(new CountAgent(countMap.get(target), target));
			}
		}
		
		// 昇順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		// 降順に
		Collections.reverse(countAgents);
		
		// 投票が最大のエージェント
		if(!countAgents.isEmpty()) {
			agent = countAgents.get(0).agent;
		}
		
		return agent;
	}
	
	
	
}
