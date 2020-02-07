package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.condition.OrCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.ComingOut;

/**
 * �����uCO�v�N���X
 */
public final class COTiming extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		// �������X�g
		ArrayList<Guess> guesses = new ArrayList<Guess>();

		for( ComingOut co : args.agi.comingOutList ){

			RoleCondition wolfCondition = RoleCondition.getRoleCondition( co.agentNo, Role.WEREWOLF );
			RoleCondition posCondition = RoleCondition.getRoleCondition( co.agentNo, Role.POSSESSED );

			// �����ɂȂ��Ă���CO������҂͋^��
			if( !co.isEnable() ){
				// CO���̖�E���ēxCO�����ꍇ�͑ΏۊO
				if( co.role != args.agi.getCORole(co.agentNo, co.commingOutTalk.getDay(), co.commingOutTalk.getIdx()) ){
					Guess guess = new Guess();
					guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
					guess.correlation = 1.3;
					guess.info.put(5, 1);
					guesses.add(guess);

				}
			}
			if(co.role == Role.VILLAGER){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.00;
				guess.info.put(19, 1);
				guesses.add(guess);
			}else if(co.role == Role.BODYGUARD){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.00;
				guess.info.put(20, 1);
				guesses.add(guess);
			}else if(co.role == Role.MEDIUM){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.00;
				guess.info.put(21, 1);
				guesses.add(guess);
			}else if(co.role == Role.SEER){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.00;
				guess.info.put(22, 1);
				guesses.add(guess);
			}else if(co.role == Role.POSSESSED){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.00;
				guess.info.put(23, 1);
				guesses.add(guess);
			}else if(co.role == Role.WEREWOLF){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 1.00;
				guess.info.put(24, 1);
				guesses.add(guess);
			}
			// �z���ɑ��݂��Ȃ�����CO�����҂͋^��
			if( args.agi.gameSetting.getRoleNum(co.role) < 1 ){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 3.0;
				guess.info.put(6, 1);
				guesses.add(guess);
				
			}

			// ���݂̂̐���
			if( co.role == Role.SEER || co.role == Role.MEDIUM ){

				// ��������󂯂Ă�����CO�����҂͋^��
				if( args.agi.isReceiveWolfJudge(co.agentNo, co.commingOutTalk.getDay(), co.commingOutTalk.getIdx()) ){
					Guess guess = new Guess();
					guess.condition = wolfCondition;
					guess.correlation = 1.5;

					if(co.role == Role.SEER){
						guess.info.put(7, 1);
					}else{
						guess.info.put(8,1);
					}
					guesses.add(guess);
				}

				//TODO �^���͎̂���CO�Ɍ��肵�A�΍RCO�͋�����i���f�ʓ|�Ȃ̂Œ��ߐ؂�I�ɒf�O�j
	//			// n���ڈȍ~��CO�����҂͋^��
	//			if( co.commingOutTalk.getIdx() >= args.agi.gameSetting.getPlayerNum() * 5 ){
	//				Guess guess = new Guess();
	//				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
	//				guess.correlation = 1.1;
	//				guesses.add(guess);
	//			}

				//TODO ���Ґ��Ή�
				// CO�����^�C�~���O�ɂ���ċ^���x���グ��
				final double hoge[] = { 1.0, 1.0, 1.0, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5 };

				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = hoge[co.commingOutTalk.getDay()];
				guess.info.put(9, co.commingOutTalk.getDay());
				guesses.add(guess);

				// �O���܂łɓ�����E��CO������΋^���x���グ��
				for( ComingOut refCo : args.agi.comingOutList ){
					if( refCo.role == co.role && refCo.commingOutTalk.getDay() < co.commingOutTalk.getDay() ){
						switch(co.role){
							case SEER:
								guess.correlation *= 1.05;
								guess.info.put(10,1);
								guesses.add(guess);
								break;
							case MEDIUM:
								guess.correlation *= 1.2;
								guess.info.put(10,1);
								guesses.add(guess);
								break;
							default:
								break;
						}
						break;
					}
				}

			}


		}


		// �������X�g��Ԃ�
		return guesses;
	}

}
