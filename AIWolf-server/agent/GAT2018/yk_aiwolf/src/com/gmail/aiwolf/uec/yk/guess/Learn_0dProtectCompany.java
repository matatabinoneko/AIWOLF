package com.gmail.aiwolf.uec.yk.guess;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;

import com.gmail.aiwolf.uec.yk.condition.AndCondition;
import com.gmail.aiwolf.uec.yk.condition.OrCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 推理「０日目身内庇い」クラス
 */
public final class Learn_0dProtectCompany extends AbstractGuessStrategy {

	AgentStatistics agentStatistics;

	public Learn_0dProtectCompany(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();


		// 0日目は行わない
		if( args.agi.latestGameInfo.getDay() == 0 ){
			return guesses;
		}


		for( int agentNo = 1; agentNo <= args.agi.gameSetting.getPlayerNum(); agentNo++ ){

			// 村側予想した先
			HashSet<Integer> protectAgent = new HashSet<Integer>();

			boolean isSaidVillager = false;
			for( Talk talk : args.agi.getTalkList(0) ){

				if( talk.getAgent().getAgentIdx() == agentNo ){
					Utterance ut = args.agi.getUtterance(talk.getContent());
					if( ut.getTopic() == Topic.ESTIMATE  ){
						if( args.agi.latestGameInfo.getAgentList().contains(ut.getTarget()) ){
							if( ut.getRole().getTeam() == Team.VILLAGER ) {
								protectAgent.add(ut.getTarget().getAgentIdx());
							}
						}
					}
				}
			}

			//TODO 他編成対応
			// 村予想が1～12人の場合に推理を行う（13人だと囲い率100%）
			if( !protectAgent.isEmpty() && protectAgent.size() <= 12 ){

				int estimateVillagerCount = agentStatistics.statistics.get(agentNo).eventCount.get(Role.WEREWOLF).getOrDefault("Said0dEstimateVillager", 0);
				int protectCompanyCount = agentStatistics.statistics.get(agentNo).eventCount.get(Role.WEREWOLF).getOrDefault("0dProtectCompany", 0);

				// 情報が５件以上あれば推理を行う
				if( estimateVillagerCount > 5 ){
					// １狼も推理で庇ってない実測割合
					double measurementNotProtectRate = 1.0 - (double)protectCompanyCount / estimateVillagerCount;

					// 庇ってない確率の理論値
					double theoreticalNotProtectRate = 1.0;
					for( int i = 0; i < protectAgent.size(); i++ ){
						theoreticalNotProtectRate *= ( (double)(12 - i)  / (14 - i) );
					}

					// 庇ってない実測の理論値に対する倍率
					double rate = measurementNotProtectRate / theoreticalNotProtectRate;

					if( rate > 1.0 ){
						// 庇ってない倍率が高い
						RoleCondition wolfCondition = RoleCondition.getRoleCondition( agentNo, Role.WEREWOLF );
						OrCondition subCondition = new OrCondition();
						for( Integer i : protectAgent ){
							subCondition.addCondition(RoleCondition.getRoleCondition( i, Role.WEREWOLF ));
						}
						Guess guess = new Guess();
						guess.condition = new AndCondition().addCondition(wolfCondition).addCondition(subCondition);
						guess.correlation = Math.pow( Math.max(1.0 / rate, 0.5) , 1.0 );
						guesses.add(guess);
					}else{
						// 庇ってる倍率が高い
						RoleCondition wolfCondition = RoleCondition.getRoleCondition( agentNo, Role.WEREWOLF );
						AndCondition subCondition = new AndCondition();
						for( Integer i : protectAgent ){
							subCondition.addCondition(RoleCondition.getNotRoleCondition( i, Role.WEREWOLF ));
						}
						Guess guess = new Guess();
						guess.condition = new AndCondition().addCondition(wolfCondition).addCondition(subCondition);
						guess.correlation = Math.pow( Math.max(rate, 0.5) , 1.0 );
						guesses.add(guess);
					}

				}

			}

		}

		// 推理リストを返す
		return guesses;
	}

}
