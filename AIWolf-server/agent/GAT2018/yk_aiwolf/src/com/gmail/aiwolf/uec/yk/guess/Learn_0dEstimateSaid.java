package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;

import java.util.ArrayList;

/**
 * 推理「１日目推理宣言」クラス
 */
public final class Learn_0dEstimateSaid extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public Learn_0dEstimateSaid(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();


		// 0日目2発言目までは行わない
		if( args.agi.latestGameInfo.getDay() == 0 && args.agi.getMyTalkNum() < 2 ){
			return guesses;
		}

		for( int agentNo = 1; agentNo <= args.agi.gameSetting.getPlayerNum(); agentNo++ ){

			boolean isSaidWerewolf = false;
			boolean isSaidVillager = false;
			for( Talk talk : args.agi.getTalkList(0) ){

				if( talk.getAgent().getAgentIdx() == agentNo ){
					Utterance ut = args.agi.getUtterance(talk.getContent());
					if( ut.getTopic() == Topic.ESTIMATE  ){
						if( ut.getRole().getTeam() == Team.WEREWOLF ){
							isSaidWerewolf = true;
						}else{
							isSaidVillager = true;
						}
					}
				}
			}


			if( isSaidWerewolf ){

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("Said0dEstimateWolf", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guess.correlation = Math.pow( guess.correlation, 0.8 );
					guesses.add(guess);
				}

			}else{

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("NoSaid0dEstimateWolf", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guess.correlation = Math.pow( guess.correlation, 0.8 );
					guesses.add(guess);
				}

			}

			if( isSaidVillager ){

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("Said0dEstimateVillager", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guess.correlation = Math.pow( guess.correlation, 0.8 );
					guesses.add(guess);
				}

			}else{

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("NoSaid0dEstimateVillager", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guess.correlation = Math.pow( guess.correlation, 0.8 );
					guesses.add(guess);
				}

			}

		}

		// 推理リストを返す
		return guesses;
	}

}
