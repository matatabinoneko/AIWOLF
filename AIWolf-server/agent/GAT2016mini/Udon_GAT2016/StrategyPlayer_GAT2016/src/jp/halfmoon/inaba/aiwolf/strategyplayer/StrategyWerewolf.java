package jp.halfmoon.inaba.aiwolf.strategyplayer;

import java.util.List;

import jp.halfmoon.inaba.aiwolf.lib.Common;
import jp.halfmoon.inaba.aiwolf.lib.Judge;
import jp.halfmoon.inaba.aiwolf.lib.ViewpointInfo;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.TemplateWhisperFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;


public class StrategyWerewolf extends AbstractBaseStrategyPlayer {


	@Override
	public void dayStart() {

		super.dayStart();

		// �x�蔻��̒ǉ�
		if( agi.fakeRole == Role.SEER ){
			if( agi.latestGameInfo.getDay() > 0 ){
				addFakeSeerJudge();
			}
		}

	}

	@Override
	public String talk() {

		try{

			// �j�]���̔���
			if( agi.selfViewInfo.wolfsidePatterns.isEmpty() ){

				// �Ƃ肠������CO
				if( !isCameOut ){
					isCameOut = true;

					String ret = TemplateTalkFactory.comingout( getMe(), Role.MEDIUM );
					return ret;
				}

				return TemplateTalkFactory.over();

			}


			// �x���E�̂b�n
			if( agi.fakeRole == Role.SEER ){
				if( !isCameOut ){
					isCameOut = true;

					String ret = TemplateTalkFactory.comingout( getMe(), agi.fakeRole );
					return ret;
				}
			}


			// CO�ς̏ꍇ
			if( isCameOut ){

				// ��CO��
				if( agi.fakeRole == Role.SEER ){

					// ���񍐂̌��ʂ�񍐂���
					if( agi.reportSelfResultCount < agi.selfInspectList.size() ){

						Judge reportJudge = agi.selfInspectList.get( agi.selfInspectList.size() - 1 );

						// �񍐍ς݂̌����𑝂₷
						agi.reportSelfResultCount++;

						// ���b
						String ret = TemplateTalkFactory.divined( Agent.getAgent(reportJudge.targetAgentNo), reportJudge.result );
						return ret;
					}

				}

			}

			// �^�����T�̐l���ȏ㌾���Ă��Ȃ���Θb��
	//		if( agi.talkedSuspicionAgentList.size() < agi.gameSetting.getRoleNumMap().get(Role.WEREWOLF) ){
	//			// �^�����b�����͂��擾���A�擾�ł��Ă���Θb��
	//			workString = getSuspicionTalkString();
	//			if( workString != null ){
	//				return workString;
	//			}
	//		}

			// ���[���ύX����ꍇ�A�V�������[���b��
			if( declaredPlanningVoteAgent != planningVoteAgent ){
				// ���[���ύX����
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// ���b
				String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
				return ret;
			}

			// �b�����������ꍇ�Aover��Ԃ�
			return TemplateTalkFactory.over();

		}catch(Exception ex){

			// �G���[����over��Ԃ�
			return TemplateTalkFactory.over();

		}

	}



	@Override
	public String whisper(){

		// �x���E�̕�
		if( declaredFakeRole != agi.fakeRole ){
			declaredFakeRole = agi.fakeRole;
			return TemplateWhisperFactory.comingout(getMe(), agi.fakeRole);
		}

		// ���ݐ�̕�
		if( declaredPlanningAttackAgent != actionUI.attackAgent && actionUI.attackAgent != null ){
			declaredPlanningAttackAgent = actionUI.attackAgent;
			return TemplateWhisperFactory.attack( Agent.getAgent(actionUI.attackAgent) );
		}

		return TemplateWhisperFactory.over();
	}


	@Override
	public Agent attack() {

		if( actionUI.attackAgent == null ){
			return null;
		}

//TODO null�ɂ��Ă�����ɏP���������ۂ��H�v����
//		// �����҂T�l�i���Y��S�l�j�ŋ�������
//		if( agi.latestGameInfo.getAliveAgentList().size() == 5 ){
//			return null;
//		}

		return Agent.getAgent(actionUI.attackAgent);

	}


	@Override
	public Agent vote() {

		// �錾�����ŉ������߂Ώ��Ă��Ԃ�
		Integer ppVoteAgentNo = getSuspectedPPVoteAgent();
		if( ppVoteAgentNo != null ){
			return Agent.getAgent(ppVoteAgentNo);
		}

		if( actionUI.voteAgent == null ){
			// ���[���錾�o���Ă��Ȃ��ꍇ�A���[���悤�Ǝv���Ă����҂ɓ��[
			if( planningVoteAgent == null ){
				return null;
			}
			return Agent.getAgent(planningVoteAgent);
		}
		return Agent.getAgent(actionUI.voteAgent);

	}


	/**
	 * �錾�����ŏ��Ă�ꍇ�A���[����擾����
	 * @return
	 */
	public Integer getSuspectedPPVoteAgent(){

		List<Integer> aliveWolfList = agi.getAliveWolfList();

		// ���ƂP�l����݂�Ώ��Ă��Ԃ�
		if( aliveWolfList.size() >= Common.getRestExecuteCount(agi.latestGameInfo.getAliveAgentList().size()) ){

			GameInfo gameInfo = agi.latestGameInfo;

			// ��i�s
			if( agi.latestGameInfo.getAliveAgentList().size() % 2 == 1 ){

				// �G�[�W�F���g���̓��[�\������擾����
				Integer[] voteTarget = new Integer[agi.gameSetting.getPlayerNum() + 1];
				for( Agent agent : gameInfo.getAliveAgentList() ){
					voteTarget[agent.getAgentIdx()] = agi.getSaidVoteAgent(agent.getAgentIdx());
					if( voteTarget[agent.getAgentIdx()] == null ){
						// ���錾�҂�����ꍇ�͕s�m��v�f������̂ł�߂Ă���
						return null;
					}
				}

				// �G�[�W�F���g���̔퓊�[�����擾����
				int[] voteReceiveNum = new int[agi.gameSetting.getPlayerNum() + 1];
				int[] voteReceiveNumWithoutMe = new int[agi.gameSetting.getPlayerNum() + 1];
				for( int i = 1; i < voteTarget.length; i++ ){
					// ���[�錾���J�E���g����
					if( voteTarget[i] != null ){
						voteReceiveNum[voteTarget[i]]++;
					}
					// �����ȊO�̓��[�錾���J�E���g����
					if( i != gameInfo.getAgent().getAgentIdx() && voteTarget[i] != null ){
						voteReceiveNumWithoutMe[voteTarget[i]]++;
					}
				}

				// �ő��[�̃G�[�W�F���g�̕[�����擾����
				int maxVoteCount = 0;
				for( int i = 1; i < voteTarget.length; i++ ){
					if( voteReceiveNumWithoutMe[i] > maxVoteCount ){
						maxVoteCount = voteReceiveNum[i];
					}
				}

				// �ő��[�𓾂Ă���G�[�W�F���g���擾
				for( int i = 1; i < voteReceiveNum.length; i++ ){
					if( voteReceiveNumWithoutMe[i] >= maxVoteCount ){
						// �����̓��[�������ƘT���݂�ꂻ���H
						if( aliveWolfList.contains(i) ){
							// ���ɍő��[�̐l�Ԃ�����Ȃ牟�����߂Ώ���
							for( int j = 1; j < voteReceiveNum.length; j++ ){
								if( voteReceiveNumWithoutMe[j] >= maxVoteCount && !aliveWolfList.contains(j) ){
									return j;
								}
							}
							// LW�����ɍő��[�����Ȃ�
							if( aliveWolfList.size() <= 1 ){
								for( int j = 1; j < voteReceiveNum.length; j++ ){
									// 1�[���̐l�Ԃ�����Ή�������Ń����_��
									if( voteReceiveNumWithoutMe[j] >= maxVoteCount - 1 && !aliveWolfList.contains(j) ){
										return j;
									}
								}
							}
						}
					}
				}

			}

		}

		return null;

	}


	/**
	 * �肢�����ǉ�����
	 */
	private void addFakeSeerJudge(){

		GameInfo gameInfo = agi.latestGameInfo;

		// �����E���茋�ʂ̉��ݒ�
		int inspectAgentNo = latestRequest.getMaxInspectRequest().agentNo;
		Species result = Species.HUMAN;

		// �P�����ꂽ�G�[�W�F���g�̎擾
		Agent attackedAgent = agi.latestGameInfo.getAttackedAgent();

		// �����o�����ꍇ�̎��_�����肷��
		ViewpointInfo future = new ViewpointInfo(agi.selfViewInfo);
		future.removeWolfPattern(inspectAgentNo);

		// ���o���œ��󂪔j�]����ꍇ�A���o�����s��
		if( future.wolfsidePatterns.isEmpty() ){
			result = Species.WEREWOLF;
		}

		// �肨���Ƃ����悪���܂ꂽ
		if( attackedAgent != null && attackedAgent.getAgentIdx() == inspectAgentNo ){
			// ���ݐ�ɂ͐l�Ԕ�����o��
			result = Species.HUMAN;
		}

		List<Integer> aliveWolfList = agi.getAliveWolfList();


		// ���ƂP�l����݂�Ώ��Ă��Ԃ�
		if( aliveWolfList.size() >= Common.getRestExecuteCount(agi.latestGameInfo.getAliveAgentList().size()) ){
			for( Agent agent : agi.latestGameInfo.getAliveAgentList() ){
				// ���Ԃ̘T�ł͂Ȃ��@���@�������_�m���ł͂Ȃ��@������ł͂Ȃ��@�l����
				if( agi.latestGameInfo.getRoleMap().get(agent) == null &&
				    !agi.selfViewInfo.isFixWhite(agent.getAgentIdx()) &&
				    agi.agentState[agent.getAgentIdx()].comingOutRole != Role.MEDIUM ){

					// �����o�����ꍇ�̎��_�����肷��
					future = new ViewpointInfo(agi.selfViewInfo);
					future.removePatternFromJudge( getMe().getAgentIdx(), agent.getAgentIdx(), Species.WEREWOLF );

					// �����o���Ĕj�]���Ȃ��Ȃ炻���ɍ����o��
					if( !future.wolfsidePatterns.isEmpty() ){
						inspectAgentNo = agent.getAgentIdx();
						result = Species.WEREWOLF;
						break;
					}
				}
			}
		}



		Judge newJudge = new Judge( getMe().getAgentIdx(),
		                            inspectAgentNo,
		                            result,
		                            null );

		agi.addFakeSeerJudge(newJudge);

	}


}
