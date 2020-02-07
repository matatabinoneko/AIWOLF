package com.gmail.aiwolf.uec.yk.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.guess.InspectedWolfsidePattern;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;
import com.gmail.aiwolf.uec.yk.lib.Common;


/**
 * 行動戦術「バランス進行」
 */
public final class BalanceExecute extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// 縄数計算
		int executeNum = Common.getRestExecuteCount(gameInfo.getAliveAgentList().size());

		//TODO 他編成対応
		// 序盤or最終日なら計算は行わない
		if( gameInfo.getDay() < 3 || executeNum <= 1 ){
			return Requests;
		}

		// 人外パターンの最大スコア
		double maxScore = args.aguess.getMostValidPattern().score;

		// PP発生フラグ
		HashMap<Integer, Double> PPMaxScore = new HashMap<Integer, Double>();

		// 全人外パターンを走査
		for( InspectedWolfsidePattern iPattern : args.aguess.getAllPattern().values() ){

			double score = iPattern.score;

			// スコアが薄いパターンは無視する
			if( score < maxScore * 0.2 ){
				continue;
			}

			// 生存人外数のカウント
			int aliveWolfSideNum = 0;
			for( int wolf : iPattern.pattern.wolfAgentNo ){
				if( args.agi.agentState[wolf].causeofDeath == CauseOfDeath.ALIVE ){
					aliveWolfSideNum++;
				}
			}
			for( int possessed : iPattern.pattern.possessedAgentNo ){
				if( args.agi.agentState[possessed].causeofDeath == CauseOfDeath.ALIVE ){
					aliveWolfSideNum++;
				}
			}

			// 吊りミスでPPになるか（吊り数＝人外数であれば該当）
			if( executeNum == aliveWolfSideNum ){
				// 狼陣営に含まれない者を吊るとPP
				for( Agent agent : gameInfo.getAliveAgentList() ){
					int agentNo = agent.getAgentIdx();
					if( !iPattern.pattern.isWolfSide(agentNo) ){
						// PPが発生する最大のスコアを取得
						if( !PPMaxScore.containsKey(agentNo) || PPMaxScore.get(agentNo) < score ){
							PPMaxScore.put(agentNo, score);
						}
					}
				}
			}

		}

		// 吊るとPPが発生しそうな人物はなるべくスルーする
		if( PPMaxScore.size() != gameInfo.getAliveAgentList().size() ){
			for( Map.Entry<Integer, Double> set : PPMaxScore.entrySet() ){
				workReq = new Request(set.getKey());
				workReq.vote = 0.4;
				Requests.add(workReq);
			}
		}


		return Requests;

	}

}
