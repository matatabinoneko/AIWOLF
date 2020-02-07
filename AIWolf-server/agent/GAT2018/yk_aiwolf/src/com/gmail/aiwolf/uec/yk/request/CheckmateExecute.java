package com.gmail.aiwolf.uec.yk.request;

import java.util.ArrayList;
import java.util.HashSet;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.guess.InspectedWolfsidePattern;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;
import com.gmail.aiwolf.uec.yk.lib.Common;


/**
 * 行動戦術「詰み進行」
 */
public final class CheckmateExecute extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// 縄数計算
		int executeNum = Common.getRestExecuteCount(gameInfo.getAliveAgentList().size());

		//TODO 他編成対応
		// 吊り数が充分or最終日なら計算は行わない
		if( executeNum > 4 || executeNum <= 1 ){
			return Requests;
		}

		// 人外パターンの最大スコア
		double maxScore = args.aguess.getMostValidPattern().score;

		// PP発生フラグ
		HashSet<Integer> PPFlag = new HashSet<Integer>();
		// LWフラグ
		HashSet<Integer> LWFlag = new HashSet<Integer>();

		// 全人外パターンを走査
		for( InspectedWolfsidePattern iPattern : args.aguess.getAllPattern().values() ){

			double score = iPattern.score;

			// スコアが薄いパターンは無視する
			if( score < maxScore * 0.2 ){
				continue;
			}

			// 生存人外数のカウント
			int aliveWolfSideNum = 0;
			int aliveWolfNum = 0;
			for( int wolf : iPattern.pattern.wolfAgentNo ){
				if( args.agi.agentState[wolf].causeofDeath == CauseOfDeath.ALIVE ){
					aliveWolfSideNum++;
					aliveWolfNum++;
				}
			}
			for( int possessed : iPattern.pattern.possessedAgentNo ){
				if( args.agi.agentState[possessed].causeofDeath == CauseOfDeath.ALIVE ){
					aliveWolfSideNum++;
				}
			}

			// 残狼１か
			if( aliveWolfNum == 1 ){
				for( int wolf : iPattern.pattern.wolfAgentNo ){
					if( args.agi.agentState[wolf].causeofDeath == CauseOfDeath.ALIVE ){
						LWFlag.add(wolf);
					}
				}
			}

			// 吊りミスでPPになるか（吊り数＝人外数であれば該当）
			if( executeNum == aliveWolfSideNum ){
				// 狼陣営に含まれない者を吊るとPP
				for( Agent agent : gameInfo.getAliveAgentList() ){
					int agentNo = agent.getAgentIdx();
					if( !iPattern.pattern.isWolfSide(agentNo) ){
						PPFlag.add(agentNo);
					}
				}
			}

		}

		// LWの内訳が存在、かつ吊ってもPPが発生しないなら吊る
		for( int agentNo : LWFlag ){
			if( !PPFlag.contains(PPFlag) ){
				workReq = new Request(agentNo);
				workReq.vote = 1.6;
				Requests.add(workReq);
			}
		}


		return Requests;

	}

}
