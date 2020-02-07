package com.gmail.k14.itolab.aiwolf.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.aiwolf.common.data.Agent;

/**
 * ランダム系の処理のクラス
 * @author k14096kk
 * 
 */
public class RandomSelect {

	/**
	 * 渡されたエージェントリストからランダムにエージェントを返す
	 * @param agentList :選択対象のエージェントリスト
	 * @return ランダムで選んだエージェント
	 */
	public static Agent randomAgentSelect(List<Agent> agentList) {
		Agent agent = null;
		if(agentList.isEmpty()) {
			return null;
		}
		int r = new Random().nextInt(agentList.size());
		agent = agentList.get(r);
		return agent;
	}
	
	/**
	 * 渡されたエージェントリストから自分以外をランダムにエージェントを返す
	 * @param agentList :選択対象のエージェントリスト
	 * @param me :自分
	 * @return 自分以外のランダム選択エージェント
	 */
	public static Agent randomAgentSelect(List<Agent> agentList, Agent me) {
		Agent agent = null;
		agentList.remove(me); //自分を除外
		if(agentList.isEmpty()) {
			return null;
		}
		int r = new Random().nextInt(agentList.size());
		agent = agentList.get(r);
		return agent;
	}
	
	/**
	 * 渡されたエージェントリストから指定リスト以外をランダムに返す
	 * @param agentList :選択対象のエージェントリスト
	 * @param removeList :選択から除くエージェントリスト
	 * @return ランダム選択されたエージェント
	 */
	public static Agent randomAgentSelect(List<Agent> agentList, List<Agent> removeList) {

		Agent agent = null;
		agentList.removeAll(removeList);
//		for(Agent ra: removeList) {
//			agentList.remove(ra);
//		}
		if(agentList.isEmpty()) {
			return null;
		}

		int r = new Random().nextInt(agentList.size());
		agent = agentList.get(r);
		return agent;
	}
	
	/**
	 * 渡されたエージェントリストから指定リストと自分以外をランダムに返す
	 * @param agentList :選択対象のエージェントリスト
	 * @param removeList :選択から除くエージェントリスト
	 * @param me :自分
	 * @return ランダム選択されたエージェント
	 */
	public static Agent randomAgentSelect(List<Agent> agentList, List<Agent> removeList, Agent me) {

		Agent agent = null;
		agentList.removeAll(removeList);
//		for(Agent ra: removeList) {
//			agentList.remove(ra);
//		}
		agentList.remove(me); //自分を除外
		if(agentList.isEmpty()) {
			return null;
		}

		int r = new Random().nextInt(agentList.size());
		agent = agentList.get(r);
		return agent;
	}
	
	/**
	 * 渡されたリストから他のリストを全て除外したエージェントのリストを順番
	 * @param agentList :選択対象のエージェントリスト
	 * @param removelists :排除するリスト(複数可能)
	 * @return ランダム選択されたエージェント
	 */
	@SafeVarargs
	public static Agent randomAgentSelect(List<Agent> agentList, List<Agent>... removelists) {
		
		Agent agent = null;
		
		List<Agent> al = randomAgentList(agentList, removelists);
		
		if(al.isEmpty()) {
			return null;
		}
		
		int r = new Random().nextInt(al.size());
		agent = al.get(r);
		
		return agent;
	}
	
	/**
	 * 渡されたリストから他のリストを全て除外したエージェントのリストを順番をランダムにして返す
	 * @param agentList :選択対象のエージェントリスト 
	 * @param removelists :排除するリスト(複数可能)
	 * @return 順番がランダムにされたエージェントリスト
	 */
	@SafeVarargs
	public static List<Agent> randomAgentList(List<Agent> agentList, List<Agent>... removelists) {
		
		for(List<Agent> rl: removelists) {
			agentList.removeAll(rl);
		}
		
		if(agentList.isEmpty()) {
			return new ArrayList<>();
		}
		
		Collections.shuffle(agentList);
		
		return agentList;
	}
	
	/**
	 * 0~引数-1までのint型乱数作成
	 * @param value : 乱数設定値
	 * @return　int型乱数
	 */
	public static int randomInt(int value) {
		int randomValue = 0;
		randomValue = new Random().nextInt(value);
		return randomValue;
	}
}
