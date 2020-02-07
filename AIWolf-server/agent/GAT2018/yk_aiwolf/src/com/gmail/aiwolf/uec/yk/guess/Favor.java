package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.condition.OrCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.AgentParameterItem;
import com.gmail.aiwolf.uec.yk.lib.Common;


/**
 * �����u�ۛ��v�N���X
 */
public final class Favor extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// �������X�g
		ArrayList<Guess> guesses = new ArrayList<Guess>();

		GameInfo gameInfo = args.agi.latestGameInfo;


		// �����T���A�c�菈�Y��
		int aliveWolfNum = args.agi.getAliveWolfList().size();
		int restExecuteNum = Common.getRestExecuteCount(gameInfo.getAliveAgentList().size());


		// �l�T�𔒂�����
		for( Agent agent : gameInfo.getAgentList() ){
			Role role = gameInfo.getRoleMap().get(agent);
			if( role == Role.WEREWOLF){

				RoleCondition wolfCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.WEREWOLF );
				RoleCondition posCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.POSSESSED );

				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.0 - args.agentParam.getParam(AgentParameterItem.FAVOR_RATE, 0.50) * ((double)aliveWolfNum / restExecuteNum);
				guesses.add(guess);

			}
		}


		// �������X�g��Ԃ�
		return guesses;
	}

}
