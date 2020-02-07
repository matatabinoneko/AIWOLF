package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.condition.OrCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;
import com.gmail.aiwolf.uec.yk.lib.ComingOut;

import org.aiwolf.common.data.Judge;

public final class AgentSta extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		
		ArrayList<Guess> guesses = new ArrayList<Guess>();
		
		int seerNum = 0;
		int mediumNum = 0;
		for( Agent agent : args.agi.latestGameInfo.getAgentList() ){
			double correlation = 1.0;
			RoleCondition wolfCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.WEREWOLF );
			RoleCondition posCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.POSSESSED );
			Guess guess = new Guess();
			guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
			guess.correlation = correlation;
			guess.info.put(1, args.agi.latestGameInfo.getDay());
			if(args.agi.agentState[agent.getAgentIdx()].causeofDeath == CauseOfDeath.ALIVE){
				guess.info.put(2, 0);
			}else if(args.agi.agentState[agent.getAgentIdx()].causeofDeath == CauseOfDeath.EXECUTED){
				guess.info.put(2, 1);
				guess.info.put(3, 1);
			}else{
				guess.info.put(2, 1);
				guess.info.put(3, -1);
			}
			if(args.agi.agentState[agent.getAgentIdx()].comingOutRole == Role.SEER){
				seerNum++;
			}else if(args.agi.agentState[agent.getAgentIdx()].comingOutRole == Role.MEDIUM){
				mediumNum++;
			}
			guesses.add(guess);
		}
		for( Agent agent : args.agi.latestGameInfo.getAgentList() ){
			double correlation = 1.0;
			RoleCondition wolfCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.WEREWOLF );
			RoleCondition posCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.POSSESSED );
			Guess guess = new Guess();
			guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
			guess.correlation = correlation;
			guess.info.put(25,seerNum);
			guess.info.put(26,mediumNum);
			guesses.add(guess);
		}
		return guesses;
	}

}
