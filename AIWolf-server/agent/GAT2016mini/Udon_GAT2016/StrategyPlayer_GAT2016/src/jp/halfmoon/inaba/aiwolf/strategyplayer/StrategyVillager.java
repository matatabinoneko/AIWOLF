package jp.halfmoon.inaba.aiwolf.strategyplayer;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;


public class StrategyVillager extends AbstractBaseStrategyPlayer {


	@Override
	public String talk() {

		try{
			String workString;

			// ���[��������Ă��Ȃ��ꍇ�A�V�������[���b��
			if( declaredPlanningVoteAgent == null ){
				// ���[���ύX����
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// �ŏI���͘T�̕֏�[��h�����ߐ錾���Ȃ��B�錾�͍ŏI���ȊO�Ƃ���
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// ���b
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// �^�����T�̐l���ȏ㌾���Ă��Ȃ���Θb��
			if( agi.talkedSuspicionAgentList.size() < agi.gameSetting.getRoleNumMap().get(Role.WEREWOLF) ){
				// �^�����b�����͂��擾���A�擾�ł��Ă���Θb��
				workString = getSuspicionTalkString();
				if( workString != null ){
					return workString;
				}
			}

			// ���[���ύX����ꍇ�A�V�������[���b��
			if( declaredPlanningVoteAgent != planningVoteAgent ){
				// ���[���ύX����
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// �ŏI���͘T�̕֏�[��h�����ߐ錾���Ȃ��B�錾�͍ŏI���ȊO�Ƃ���
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// ���b
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// �M�p���b�����͂��擾���A�擾�ł��Ă���Θb��
			workString = getTrustTalkString();
			if( workString != null ){
				return workString;
			}

			// �b�����������ꍇ�Aover��Ԃ�
			return TemplateTalkFactory.over();

		}catch(Exception ex){

			// �G���[����over��Ԃ�
			return TemplateTalkFactory.over();

		}


	}



}
