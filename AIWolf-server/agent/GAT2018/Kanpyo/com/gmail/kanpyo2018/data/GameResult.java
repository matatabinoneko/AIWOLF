package com.gmail.kanpyo2018.data;

import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import com.gmail.kanpyo2018.util.Debug;

public class GameResult {

	int gameCount;

	Map<Agent, Integer> winMap;

	boolean isInit = false;

	/**
	 * 初期化処理
	 *
	 * @param gameInfo
	 */
	public void init(GameInfo gameInfo) {
		if (isInit) {
			return;
		}
		gameCount = 0;
		winMap = new HashMap<>();
		gameInfo.getAgentList().stream().forEach(k -> winMap.put(k, 0));
		isInit = true;
	}

	/**
	 * ゲーム回数取得
	 *
	 * @return ゲーム回数
	 */
	public int getGameCount() {
		return this.gameCount;
	}

	/**
	 * ゲーム結果更新処理
	 *
	 * @param gameInfo:ゲーム情報
	 */
	public void updateDate(GameInfo gameInfo) {
		this.winCount(gameInfo);
	}

	/**
	 * 表示処理
	 */
	public void show() {
		Debug.print(winMap);
	}

	public void winCount(GameInfo gameInfo) {

		boolean winWolf = false;

		// 勝利判定
		for (Agent agent : gameInfo.getAliveAgentList()) {
			Role role = gameInfo.getRoleMap().get(agent);
			if (role == Role.WEREWOLF) {
				winWolf = true;
				break;
			}
		}

		// エージェントの勝利回数カウント
		for (Agent agent : gameInfo.getAliveAgentList()) {
			Role role = gameInfo.getRoleMap().get(agent);
			if (winWolf) {
				if (role == Role.WEREWOLF || role == Role.POSSESSED) {
					winMap.put(agent, winMap.get(agent) + 1);
				}
			} else {
				if (role != Role.WEREWOLF || role != Role.POSSESSED) {
					winMap.put(agent, winMap.get(agent) + 1);
				}
			}
		}
	}
}
