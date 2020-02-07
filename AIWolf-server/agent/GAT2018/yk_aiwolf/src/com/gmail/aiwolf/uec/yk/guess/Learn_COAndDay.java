package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;

import java.util.ArrayList;

/**
 * „—u‚b‚n‚È‚µ‚Æ“ú”vƒNƒ‰ƒX
 */
public final class Learn_COAndDay extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public Learn_COAndDay(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		// „—ƒŠƒXƒg
		ArrayList<Guess> guesses = new ArrayList<Guess>();

		// ‰“ú‚Ís‚í‚È‚¢
		if( args.agi.latestGameInfo.getDay() < 1 ){
			return guesses;
		}

		// 1“ú–Ú0”­Œ¾‚Å‚Ís‚í‚È‚¢
		if( args.agi.latestGameInfo.getDay() == 1 && args.agi.getMyTalkNum() == 0 ){
			return guesses;
		}

		for( int agentNo = 1; agentNo <= args.agi.gameSetting.getPlayerNum(); agentNo++ ){

			int day = args.agi.latestGameInfo.getDay();

			Role role = args.agi.getCORole(agentNo, day, 0);

			// ‚b‚n‚È‚µA‚à‚µ‚­‚Í‘º‚b‚n‚ª‘ÎÛ
			if( args.agi.agentState[agentNo].comingOutRole == null || args.agi.agentState[agentNo].comingOutRole == Role.VILLAGER ){

				// TODO ŠY“–Ò‚ª“–“ú–¢”­Œ¾‚È‚ç‘O“ú‚ğŠî€‚É‚·‚é

				// €–S‚ÍÅŒã‚É¶‘¶‚µ‚Ä‚¢‚½“ú‚ğŠî€‚É‚·‚é
				if( args.agi.agentState[agentNo].deathDay != null ){
					day = args.agi.agentState[agentNo].deathDay - 1;
				}

				// 4“ú–Ú‚Ü‚Å‚ÉŠÛ‚ß‚é
				day = Math.min(day, 4);

				ArrayList<Guess> rguesses = agentStatistics.statistics.get(agentNo).getGuessFromEvent("NotCO_" + day + "d", args.agi.gameSetting);

				for( Guess guess : rguesses ){
					guess.correlation = Math.pow( guess.correlation, 0.7 );
					guesses.add(guess);
				}

			}

		}

		// „—ƒŠƒXƒg‚ğ•Ô‚·
		return guesses;
	}

}
