package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics.Statistics;

import java.util.ArrayList;

/**
 * 推理「統計」クラス
 */
public final class FromAgentStatistics_B extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public FromAgentStatistics_B(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();
		Guess guess;


		for( Agent agent : args.agi.latestGameInfo.getAgentList() ){

			AbstractCondition agentWolf = RoleCondition.getRoleCondition( agent, Role.WEREWOLF );
			AbstractCondition agentPossessed = RoleCondition.getRoleCondition( agent, Role.POSSESSED );

			int agentNo = agent.getAgentIdx();
			Statistics statistics = agentStatistics.statistics.get(agentNo);
			Role coRole = args.agi.agentState[agentNo].comingOutRole;

			//TODO 1.0が基準となるよう、人数比を考慮して計算する
			if( coRole != null && coRole != Role.VILLAGER ){

				if( statistics.roleCount.get(Role.WEREWOLF) >= 5 ){
					if( statistics.COCount.get(Role.WEREWOLF).get(coRole) <= 0){
						guess = new Guess();
						guess.condition = agentWolf;
						guess.correlation = 0.8;
						guesses.add(guess);
					}
				}

				if( statistics.roleCount.get(Role.POSSESSED) >= 2 ){
					if( statistics.COCount.get(Role.POSSESSED).get(coRole) <= 0){
						guess = new Guess();
						guess.condition = agentPossessed;
						guess.correlation = 0.8;
						guesses.add(guess);
					}
				}

			}else{

//				if( statistics.roleCount.get(Role.WEREWOLF) >= 5 ){
//					int count = statistics.roleCount.get(Role.WEREWOLF);
//					count -= statistics.COCount.get(Role.WEREWOLF).get(Role.SEER);
//					count -= statistics.COCount.get(Role.WEREWOLF).get(Role.MEDIUM);
//					count -= statistics.COCount.get(Role.WEREWOLF).get(Role.BODYGUARD);
//					if( count <= 0){
//						guess = new Guess();
//						guess.condition = agentWolf;
//						guess.correlation = 0.8;
//						guesses.add(guess);
//					}
//				}
//
//				if( statistics.roleCount.get(Role.POSSESSED) >= 2 ){
//					int count = statistics.roleCount.get(Role.POSSESSED);
//					count -= statistics.COCount.get(Role.POSSESSED).get(Role.SEER);
//					count -= statistics.COCount.get(Role.POSSESSED).get(Role.MEDIUM);
//					count -= statistics.COCount.get(Role.POSSESSED).get(Role.BODYGUARD);
//					if( count <= 0){
//						guess = new Guess();
//						guess.condition = agentPossessed;
//						guess.correlation = 0.8;
//						guesses.add(guess);
//					}
//				}
			}


		}

		// 推理リストを返す
		return guesses;
	}

}
