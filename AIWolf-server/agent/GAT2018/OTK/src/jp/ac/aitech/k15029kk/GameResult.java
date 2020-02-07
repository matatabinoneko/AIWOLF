package jp.ac.aitech.k15029kk;

import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import jp.ac.aitech.k15029kk.util.Debug;


public class GameResult {

	int gameCount;

	Map<Agent, Integer> winMap;

	 boolean isInit = false;

	 public void init(GameInfo gameInfo) {

		 if(isInit) {
			return;
		 }
		 gameCount = 0;
		 winMap = new HashMap<>();

		 //for拡張と同様処理
		 gameInfo.getAgentList().stream().forEach(k -> winMap.put(k,0));

		 isInit = true;
	 }

	 //ゲーム結果更新処理
	 public void updateData(GameInfo gameInfo) {
		 this.winCount(gameInfo);
	 }


	 public int getGameCount() {
		return gameCount;
	}


	 public Map<Agent, Integer> getWinMap() {
		return winMap;
	}

	public void winCount(GameInfo gameInfo) {

		boolean winWolf = false;

		//人狼が生き残ってるかどうか
		for(Agent agent: gameInfo.getAliveAgentList()) {
			Role role = gameInfo.getRoleMap().get(agent);
			if(role == Role.WEREWOLF) {
				winWolf = true;
				break;
			}
		}

		//エージェントの勝利回数カウント
		for(Agent agent: gameInfo.getAliveAgentList()) {
			Role role = gameInfo.getRoleMap().get(agent);
			if(winWolf){
				if(role == Role.WEREWOLF || role == Role.POSSESSED) {
					winMap.put(agent, winMap.get(agent)+1);
				}
			}else {
				if(role != Role.WEREWOLF && role != Role.POSSESSED){
					winMap.put(agent, winMap.get(agent)+1);
				}
			}
		}
	}

	//
	public void show() {
		Debug.print(winMap);
	}

}
