package com.gmail.aiwolf.uec.yk.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.net.GameSetting;

import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.guess.Guess;
import com.gmail.aiwolf.uec.yk.lib.AdvanceGameInfo;
import com.gmail.aiwolf.uec.yk.lib.Judge;
import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;


public class AgentStatistics {

	static final int AGENT_MAX = 15;

	/** エージェント毎の統計情報 */
	public Map<Integer, Statistics> statistics = new HashMap<Integer, Statistics>();



	public AgentStatistics(){

		// エージェント毎の統計情報を初期化
		for( int i = 1; i <= AGENT_MAX; i++ ){
			Statistics stat = new Statistics();
			stat.init(i);
			statistics.put( i, stat );
		}

	}


	public void addStatictics(AdvanceGameInfo agi){

 		for(Entry<Agent, Role> entry : agi.latestGameInfo.getRoleMap().entrySet()){
 			int agentNo = entry.getKey().getAgentIdx();

			// ゲーム回数のカウント
			statistics.get(agentNo).gameCount++;

			// 役職になった回数のカウント
			statistics.get(agentNo).roleCount.put( entry.getValue(), statistics.get(agentNo).roleCount.getOrDefault(entry.getValue(), 0) + 1 );

			// ＣＯカウントの増加
 			statistics.get(agentNo).increaseCOCount(entry.getValue(), agi.agentState[agentNo].comingOutRole);
 		}


		for( Agent agent : agi.latestGameInfo.getAgentList() ) {

			int agentNo = agent.getAgentIdx();
			Role agentRole = agi.latestGameInfo.getRoleMap().get(agent);

			// 1dVote宣言するか
			Integer target = agi.getSaidVoteAgent(agentNo, 1);
			if( target == null ){
				statistics.get(agentNo).increaseEventCount(agentRole, "NoSaid1dVote");
			}else{
				statistics.get(agentNo).increaseEventCount(agentRole, "Said1dVote");
			}

			// 0dEstimate宣言するか・身内庇いするか
			boolean isSaidWerewolf = false;
			boolean isSaidVillager = false;
			boolean isProtectCompany = false;
			for( Talk talk : agi.getTalkList(0) ){

				if( talk.getAgent().getAgentIdx() == agentNo ){
					Utterance ut = agi.getUtterance(talk.getContent());
					if( ut.getTopic() == Topic.ESTIMATE  ){
						if( ut.getRole().getTeam() == Team.WEREWOLF ){
							isSaidWerewolf = true;
						}else{
							isSaidVillager = true;
							if( agi.latestGameInfo.getRoleMap().get(ut.getTarget()) == Role.WEREWOLF ){
								isProtectCompany = true;
							}
						}
					}
				}
			}
			if( isSaidWerewolf ){
				statistics.get(agentNo).increaseEventCount(agentRole, "Said0dEstimateWolf");
			}else{
				statistics.get(agentNo).increaseEventCount(agentRole, "NoSaid0dEstimateWolf");
			}
			if( isSaidVillager ){
				statistics.get(agentNo).increaseEventCount(agentRole, "Said0dEstimateVillager");
			}else{
				statistics.get(agentNo).increaseEventCount(agentRole, "NoSaid0dEstimateVillager");
			}
			if( isProtectCompany ){
				statistics.get(agentNo).increaseEventCount(agentRole, "0dProtectCompany");
			}

			// 日毎のCOなしの内訳
			for( int day = 0; day <= 5; day++ ){
				// 生存時の情報のみ集計
				if( agi.agentState[agentNo].deathDay == null || agi.agentState[agentNo].deathDay > day ){
					Role role = agi.getCORole(agentNo, day+1, 0);
					if( role == null || role == Role.VILLAGER ){
						statistics.get(agentNo).increaseEventCount(agentRole, "NotCO_" + day + "d");
					}
				}
			}

			// 初回占い先が役かグレーか
			for( Judge judge : agi.getSeerJudgeList() ){
				if( judge.agentNo == agentNo ){
					if( agi.getCORole(judge.targetAgentNo, 1, 0) != null ){
						// 役持ちを占い
						statistics.get(agentNo).increaseEventCount(agentRole, "1dCompetitionDevine");
					}else{
						// グレーを占い
						statistics.get(agentNo).increaseEventCount(agentRole, "1dNotCompetitionDevine");
					}
					break;
				}
			}

		}

		// 投票（全員分）
		for( int day = 2; day < agi.latestGameInfo.getDay(); day++ ){
			VoteAnalyzer vaResult = new VoteAnalyzer(agi.getVoteList(day-1));
			VoteAnalyzer vaSaid = VoteAnalyzer.loadSaidVote(agi, day-1);

			for( Agent agent : agi.latestGameInfo.getAgentList() ){
				Agent target = vaResult.getVoteTarget(agent);
				if( target != null ){
					statistics.get(agent.getAgentIdx()).increaseEventCount(agi.latestGameInfo.getRoleMap().get(agent), "Vote");
					if( vaSaid.getMaxReceiveVoteAgent().contains(target) ){
						statistics.get(agent.getAgentIdx()).increaseEventCount(agi.latestGameInfo.getRoleMap().get(agent), "VoteToMostVote");
					}
				}
			}
		}

	}



	public class Statistics{

		/** エージェント番号 */
		public int agentNo;

		/** ゲーム回数 */
		public int gameCount = 0;

		/** 役職になった回数 */
		public HashMap<Role, Integer> roleCount = new HashMap<Role, Integer>();

		/** 各役職で終了時に各村役をＣＯしていた回数 */
		public HashMap<Role, HashMap<Role, Integer>> COCount = new HashMap<Role, HashMap<Role, Integer>>();

		/** 役職毎のイベント回数 */
		public HashMap<Role, HashMap<String, Integer>> eventCount = new HashMap<Role, HashMap<String, Integer>>();;

		/** 推理の有効度 */
		public HashMap<String, Double> weightOfGuess = new HashMap<String, Double>();

		/**
		 * 初期化
         */
		public void init(int agentNo){

			this.agentNo = agentNo;

			for(Role role : Role.values()){
				roleCount.put(role, 0);
				COCount.put(role, new HashMap<Role, Integer>());
				eventCount.put(role, new HashMap<String, Integer>());
				for(Role role2 : Role.values()){
					COCount.get(role).put(role2, 0);
				}
			}

		}

		/**
		 * ＣＯカウントの増加
		 * @param role 本当の役職
		 * @param fakeRole 騙りCOした役職(何もCOしない場合Null、未COと村COは区別する)
		 */
		public void increaseCOCount(Role role, Role fakeRole){

			// ＣＯ回数のカウント
			if( fakeRole != null ){
				COCount.get(role).put( fakeRole, COCount.get(role).getOrDefault(fakeRole, 0) + 1 );
			}

		}

		/**
		 * イベントカウントの増加
		 * @param role 本当の役職
		 * @param eventCode イベントのコード
		 */
		public void increaseEventCount(Role role, String eventCode){

			// イベント回数のカウント
			eventCount.get(role).put( eventCode, eventCount.get(role).getOrDefault(eventCode, 0) + 1 );

		}



		public ArrayList<Guess> getGuessFromEvent(String eventCode, GameSetting gameSetting){

			// 推理リスト
			ArrayList<Guess> guesses = new ArrayList<Guess>();

			double wolfRate = 1.0;
			double posRate = 1.0;

			// 村人をプレイ済の場合のみ計算を行う(村人の理由はなんとなく)
			if( roleCount.getOrDefault(Role.VILLAGER, 0) > 0 ){

				int allEventCount = 0;
				for(Role role : Role.values()){
					allEventCount += eventCount.get(role).getOrDefault(eventCode, 0);
				}

				// イベントが一定回数以上発生している場合のみ計算可能
				if( allEventCount >= 2 ){

					// 狼をプレイ済の場合のみ計算を行う
					if( roleCount.getOrDefault(Role.WEREWOLF, 0) > 0 ){
						double score = 1.0;
						int wolfEventCount = eventCount.get(Role.WEREWOLF).getOrDefault(eventCode, 0);

						double eventWolfRate = (double)wolfEventCount / allEventCount;
						double measurementCountWolfRate = (double)roleCount.get(Role.WEREWOLF) / gameCount;
						double theoreticalCountWolfRate = (double)gameSetting.getRoleNum(Role.WEREWOLF) / gameSetting.getPlayerNum();

						wolfRate = eventWolfRate / measurementCountWolfRate;

						if( Double.compare(wolfRate, 1.0) != 0 ){
							double weight = Math.min( gameCount * 0.02 , 0.5 );

							RoleCondition wolfCondition = RoleCondition.getRoleCondition( agentNo, Role.WEREWOLF );
							Guess guess = new Guess();
							guess.condition = wolfCondition;
							guess.correlation = Math.pow( Math.max(wolfRate, 0.3) , weight );
							guesses.add(guess);
						}
					}

					// 狂をプレイ済の場合のみ計算を行う
					if( roleCount.getOrDefault(Role.POSSESSED, 0) > 0 ){
						double score = 1.0;
						int posEventCount = eventCount.get(Role.POSSESSED).getOrDefault(eventCode, 0);

						double eventPosRate = (double)posEventCount / allEventCount;
						double measurementCountPosRate = (double)roleCount.get(Role.POSSESSED) / gameCount;
						double theoreticalCountPosRate = (double)gameSetting.getRoleNum(Role.POSSESSED) / gameSetting.getPlayerNum();

						posRate = eventPosRate / measurementCountPosRate;

						if( Double.compare(posRate, 1.0) != 0 ){
							double weight = Math.min( gameCount * 0.02 , 0.5 );

							RoleCondition posCondition = RoleCondition.getRoleCondition( agentNo, Role.POSSESSED );
							Guess guess = new Guess();
							guess.condition = posCondition;
							guess.correlation = Math.pow( Math.max(posRate, 0.3) , weight );
							guesses.add(guess);
						}
					}

				}

			}

			return guesses;

		}



	}


}
