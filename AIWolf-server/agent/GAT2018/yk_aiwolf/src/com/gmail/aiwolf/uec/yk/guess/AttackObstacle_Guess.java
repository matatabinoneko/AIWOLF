package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;

/**
 * �����u�ӌ����݁v�N���X
 */
public final class AttackObstacle_Guess extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// �������X�g
		ArrayList<Guess> guesses = new ArrayList<Guess>();


		// �S�Ă̓��[�������m�F����(���񓊕[=1����)
		for( int day = 1; day < args.agi.latestGameInfo.getDay(); day++ ){
			for( Vote vote : args.agi.getVoteList(day) ){
				int agentNo = vote.getAgent().getAgentIdx();

				// �P���������҂̓��[��
				if( args.agi.agentState[agentNo].causeofDeath == CauseOfDeath.ATTACKED ){
					// ��CO�E��CO�҂�
					if( args.agi.agentState[agentNo].comingOutRole == null || args.agi.agentState[agentNo].comingOutRole == Role.VILLAGER ){

						// �퓊�[�҂��T�̉\����Z������
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

		// �������X�g��Ԃ�
		return guesses;
	}

}
