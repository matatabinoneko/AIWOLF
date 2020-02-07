package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.lib.Judge;

import java.util.ArrayList;

/**
 * 推理「１日目投票宣言」クラス
 */
public final class Learn_1dCompetitionDevine extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public Learn_1dCompetitionDevine(AgentStatistics agentStatistics){
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

		for( int seer : args.agi.getEnableCOAgentNo(Role.SEER) ){
			for( Judge judge : args.agi.getSeerJudgeList() ){
				if( judge.agentNo == seer ){
					if( args.agi.getCORole(judge.targetAgentNo, 1, 0) != null ){
						// 役持ちを占い
						ArrayList<Guess> rguesses = agentStatistics.statistics.get(judge.agentNo).getGuessFromEvent("1dCompetitionDevine", args.agi.gameSetting);

						for( Guess guess : rguesses ){
							guess.correlation = Math.pow(guess.correlation, 0.6);
							guesses.add(guess);
						}
					}else{
						// グレーを占い
						ArrayList<Guess> rguesses = agentStatistics.statistics.get(judge.agentNo).getGuessFromEvent("1dNotCompetitionDevine", args.agi.gameSetting);

						for( Guess guess : rguesses ){
							guess.correlation = Math.pow(guess.correlation, 0.6);
							guesses.add(guess);
						}
					}
					break;
				}
			}
		}

		// 推理リストを返す
		return guesses;
	}

}
