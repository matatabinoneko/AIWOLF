package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;

import java.util.ArrayList;

/**
 * 推理「１日目投票宣言」クラス
 */
public final class Learn_1dVoteSaid extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public Learn_1dVoteSaid(AgentStatistics agentStatistics){
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

			Integer target = args.agi.getSaidVoteAgent(agentNo, 1);

			// 無宣言の場合
			if( target == null ){

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("NoSaid1dVote", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guesses.add(guess);
				}

			}else{

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("Said1dVote", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guesses.add(guess);
				}

			}

		}

		// 推理リストを返す
		return guesses;
	}

}
