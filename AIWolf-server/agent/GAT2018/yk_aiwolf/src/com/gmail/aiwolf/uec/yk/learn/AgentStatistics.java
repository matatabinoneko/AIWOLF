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

	/** �G�[�W�F���g���̓��v��� */
	public Map<Integer, Statistics> statistics = new HashMap<Integer, Statistics>();



	public AgentStatistics(){

		// �G�[�W�F���g���̓��v����������
		for( int i = 1; i <= AGENT_MAX; i++ ){
			Statistics stat = new Statistics();
			stat.init(i);
			statistics.put( i, stat );
		}

	}


	public void addStatictics(AdvanceGameInfo agi){

 		for(Entry<Agent, Role> entry : agi.latestGameInfo.getRoleMap().entrySet()){
 			int agentNo = entry.getKey().getAgentIdx();

			// �Q�[���񐔂̃J�E���g
			statistics.get(agentNo).gameCount++;

			// ��E�ɂȂ����񐔂̃J�E���g
			statistics.get(agentNo).roleCount.put( entry.getValue(), statistics.get(agentNo).roleCount.getOrDefault(entry.getValue(), 0) + 1 );

			// �b�n�J�E���g�̑���
 			statistics.get(agentNo).increaseCOCount(entry.getValue(), agi.agentState[agentNo].comingOutRole);
 		}


		for( Agent agent : agi.latestGameInfo.getAgentList() ) {

			int agentNo = agent.getAgentIdx();
			Role agentRole = agi.latestGameInfo.getRoleMap().get(agent);

			// 1dVote�錾���邩
			Integer target = agi.getSaidVoteAgent(agentNo, 1);
			if( target == null ){
				statistics.get(agentNo).increaseEventCount(agentRole, "NoSaid1dVote");
			}else{
				statistics.get(agentNo).increaseEventCount(agentRole, "Said1dVote");
			}

			// 0dEstimate�錾���邩�E�g���݂����邩
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

			// ������CO�Ȃ��̓���
			for( int day = 0; day <= 5; day++ ){
				// �������̏��̂ݏW�v
				if( agi.agentState[agentNo].deathDay == null || agi.agentState[agentNo].deathDay > day ){
					Role role = agi.getCORole(agentNo, day+1, 0);
					if( role == null || role == Role.VILLAGER ){
						statistics.get(agentNo).increaseEventCount(agentRole, "NotCO_" + day + "d");
					}
				}
			}

			// ����肢�悪�����O���[��
			for( Judge judge : agi.getSeerJudgeList() ){
				if( judge.agentNo == agentNo ){
					if( agi.getCORole(judge.targetAgentNo, 1, 0) != null ){
						// ��������肢
						statistics.get(agentNo).increaseEventCount(agentRole, "1dCompetitionDevine");
					}else{
						// �O���[��肢
						statistics.get(agentNo).increaseEventCount(agentRole, "1dNotCompetitionDevine");
					}
					break;
				}
			}

		}

		// ���[�i�S�����j
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

		/** �G�[�W�F���g�ԍ� */
		public int agentNo;

		/** �Q�[���� */
		public int gameCount = 0;

		/** ��E�ɂȂ����� */
		public HashMap<Role, Integer> roleCount = new HashMap<Role, Integer>();

		/** �e��E�ŏI�����Ɋe�������b�n���Ă����� */
		public HashMap<Role, HashMap<Role, Integer>> COCount = new HashMap<Role, HashMap<Role, Integer>>();

		/** ��E���̃C�x���g�� */
		public HashMap<Role, HashMap<String, Integer>> eventCount = new HashMap<Role, HashMap<String, Integer>>();;

		/** �����̗L���x */
		public HashMap<String, Double> weightOfGuess = new HashMap<String, Double>();

		/**
		 * ������
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
		 * �b�n�J�E���g�̑���
		 * @param role �{���̖�E
		 * @param fakeRole �x��CO������E(����CO���Ȃ��ꍇNull�A��CO�Ƒ�CO�͋�ʂ���)
		 */
		public void increaseCOCount(Role role, Role fakeRole){

			// �b�n�񐔂̃J�E���g
			if( fakeRole != null ){
				COCount.get(role).put( fakeRole, COCount.get(role).getOrDefault(fakeRole, 0) + 1 );
			}

		}

		/**
		 * �C�x���g�J�E���g�̑���
		 * @param role �{���̖�E
		 * @param eventCode �C�x���g�̃R�[�h
		 */
		public void increaseEventCount(Role role, String eventCode){

			// �C�x���g�񐔂̃J�E���g
			eventCount.get(role).put( eventCode, eventCount.get(role).getOrDefault(eventCode, 0) + 1 );

		}



		public ArrayList<Guess> getGuessFromEvent(String eventCode, GameSetting gameSetting){

			// �������X�g
			ArrayList<Guess> guesses = new ArrayList<Guess>();

			double wolfRate = 1.0;
			double posRate = 1.0;

			// ���l���v���C�ς̏ꍇ�̂݌v�Z���s��(���l�̗��R�͂Ȃ�ƂȂ�)
			if( roleCount.getOrDefault(Role.VILLAGER, 0) > 0 ){

				int allEventCount = 0;
				for(Role role : Role.values()){
					allEventCount += eventCount.get(role).getOrDefault(eventCode, 0);
				}

				// �C�x���g�����񐔈ȏ㔭�����Ă���ꍇ�̂݌v�Z�\
				if( allEventCount >= 2 ){

					// �T���v���C�ς̏ꍇ�̂݌v�Z���s��
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

					// �����v���C�ς̏ꍇ�̂݌v�Z���s��
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
