package com.gmail.k14.itolab.aiwolf.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.gmail.k14.itolab.aiwolf.util.CoInfo;

/**
 * 各エージェントのCOの履歴を管理するクラス
 * @author k14096kk
 *
 */
public class CoHistory {

	/**CO履歴のMap*/
	public Map<Agent, List<CoInfo>> comingoutHistoryMap;
	
	/**
	 * CO履歴作成
	 * @param agentList :参加エージェントリスト
	 */
	public CoHistory(List<Agent> agentList) {
		comingoutHistoryMap = new HashMap<>();
		for(Agent agent: agentList) {
			comingoutHistoryMap.put(agent, new ArrayList<>());
		}
	}
	
	/**
	 * マップ取得
	 * @return CO履歴マップ
	 */
	public Map<Agent, List<CoInfo>> getComingoutHistoryMap() {
		return comingoutHistoryMap;
	}
	
	/**
	 * エージェントのCO履歴取得
	 * @param agent :エージェント
	 * @return CO履歴
	 */
	public List<CoInfo> getInfoList(Agent agent) {
		return getComingoutHistoryMap().get(agent);
	}
	
	/**
	 * エージェントが今まで指定役職のCOをしたかどうか
	 * @param agent :エージェント
	 * @param role :役職
	 * @return していればtrue,していなければfalse
	 */
	public boolean containRole(Agent agent, Role role) {
		List<CoInfo> storeList = getInfoList(agent);
		for(CoInfo coInfo: storeList) {
			if(coInfo.getRole()==role) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * エージェントのCO情報追加
	 * @param agent :エージェント
	 * @param coInfo :CO情報
	 */
	public void add(Agent agent, CoInfo coInfo) {
		List<CoInfo> storeList = getInfoList(agent);
		storeList.add(coInfo);
		comingoutHistoryMap.put(agent, storeList);
	}
}
