package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;
import com.gmail.aiwolf.uec.yk.condition.OrCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics.Statistics;
import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

/**
 * 推理「統計」クラス
 */
public final class FromAgentStatistics extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public FromAgentStatistics(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();
		Guess guess;


//		for( Agent agent : args.agi.latestGameInfo.getAgentList() ){
//
//			AbstractCondition agentWolf = RoleCondition.getRoleCondition( agent, Role.WEREWOLF );
//			AbstractCondition agentPossessed = RoleCondition.getRoleCondition( agent, Role.POSSESSED );
//
//			int agentNo = agent.getAgentIdx();
//			Statistics statistics = agentStatistics.statistics.get(agentNo);
//			Role coRole = args.agi.agentState[agentNo].comingOutRole;
//
//			//TODO 1.0が基準となるよう、人数比を考慮して計算する
//			if( coRole != null && coRole != Role.VILLAGER ){
//
//				// 値が振れすぎないようにするための嵩増し値
//				int padNum = 80;
//
//				double rate;
//
//				// 人狼統計
//				rate = getRate(statistics.wolfCOCount.get(coRole), statistics.wolfCount, padNum);
//
//				guess = new Guess();
//				guess.condition = agentWolf;
//				guess.correlation = rate;
//				guesses.add(guess);
//
//				// 狂人統計
//				rate = getRate(statistics.posCOCount.get(coRole), statistics.posCount, padNum);
//
//				guess = new Guess();
//				guess.condition = agentPossessed;
//				guess.correlation = rate;
//				guesses.add(guess);
//
//			}else{
//
//				// 値が振れすぎないようにするための嵩増し値
//				int padNum = 200;
//
//				double rate;
//
//				// 人狼統計
//				int count = statistics.wolfCount;
//				count -= statistics.wolfCOCount.get(Role.SEER);
//				count -= statistics.wolfCOCount.get(Role.MEDIUM);
//				count -= statistics.wolfCOCount.get(Role.BODYGUARD);
//				rate = getRate(count, statistics.wolfCount, padNum);
//
//				guess = new Guess();
//				guess.condition = agentWolf;
//				guess.correlation = rate;
//				guesses.add(guess);
//
//				// 狂人統計
//				count = statistics.posCount;
//				count -= statistics.posCOCount.get(Role.SEER);
//				count -= statistics.posCOCount.get(Role.MEDIUM);
//				count -= statistics.posCOCount.get(Role.BODYGUARD);
//				rate = getRate(count, statistics.posCount, padNum);
//
//				guess = new Guess();
//				guess.condition = agentPossessed;
//				guess.correlation = rate;
//				guesses.add(guess);
//			}
//
//
//		}

		// 推理リストを返す
		return guesses;
	}



	private double getRate(double fakeCOCount, double allCount, int padCount){
		double rate;

		// 騙り確率
		rate = (fakeCOCount + padCount) / (allCount + padCount);

		return rate;
	}


}
