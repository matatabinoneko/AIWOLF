package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;

/**
 * uÓ©ÝvNX
 */
public final class AttackObstacle_Guess extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// Xg
		ArrayList<Guess> guesses = new ArrayList<Guess>();


		// SÄÌ[ððmF·é(ñ[=1úÚ)
		for( int day = 1; day < args.agi.latestGameInfo.getDay(); day++ ){
			for( Vote vote : args.agi.getVoteList(day) ){
				int agentNo = vote.getAgent().getAgentIdx();

				// Pµ½ÒÌ[©
				if( args.agi.agentState[agentNo].causeofDeath == CauseOfDeath.ATTACKED ){
					// ¢COEºCOÒ©
					if( args.agi.agentState[agentNo].comingOutRole == null || args.agi.agentState[agentNo].comingOutRole == Role.VILLAGER ){

						// í[ÒªTÌÂ\«ðZ­©é
						AbstractCondition targetWolf = RoleCondition.getRoleCondition( vote.getTarget().getAgentIdx(), Role.WEREWOLF );

						Guess guess = new Guess();
						guess.condition = targetWolf;
						guess.correlation = 1.03;
						guess.info.put(4, 1);
						guesses.add(guess);

					}
				}
			}
		}

		// XgðÔ·
		return guesses;
	}

}
