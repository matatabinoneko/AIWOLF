package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;

import java.util.ArrayList;

/**
 * 推理「ＣＯなしと日数」クラス
 */
public final class Learn_COAndDay extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public Learn_COAndDay(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();

		// 初日は行わない
		if( args.agi.latestGameInfo.getDay() < 1 ){
			return guesses;
		}

		// 1日目0発言では行わない
		if( args.agi.latestGameInfo.getDay() == 1 && args.agi.getMyTalkNum() == 0 ){
			return guesses;
		}

		for( int agentNo = 1; agentNo <= args.agi.gameSetting.getPlayerNum(); agentNo++ ){

			int day = args.agi.latestGameInfo.getDay();

			Role role = args.agi.getCORole(agentNo, day, 0);

			// ＣＯなし、もしくは村ＣＯが対象
			if( args.agi.agentState[agentNo].comingOutRole == null || args.agi.agentState[agentNo].comingOutRole == Role.VILLAGER ){

				// TODO 該当者が当日未発言なら前日を基準にする

				// 死亡時は最後に生存していた日を基準にする
				if( args.agi.agentState[agentNo].deathDay != null ){
					day = args.agi.agentState[agentNo].deathDay - 1;
				}

				// 4日目までに丸める
				day = Math.min(day, 4);

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("NotCO_" + day + "d", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guess.correlation = Math.pow( guess.correlation, 0.7 );
					guesses.add(guess);
				}

			}

		}

		// 推理リストを返す
		return guesses;
	}

}
