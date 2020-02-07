package jp.halfmoon.inaba.aiwolf.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Role;

import jp.halfmoon.inaba.aiwolf.condition.OrCondition;
import jp.halfmoon.inaba.aiwolf.condition.RoleCondition;
import jp.halfmoon.inaba.aiwolf.lib.ComingOut;

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
					guesses.add(guess);
				}
			}

			// �z���ɑ��݂��Ȃ�����CO�����҂͋^��
			if( args.agi.gameSetting.getRoleNum(co.role) < 1 ){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 3.0;
				guesses.add(guess);
			}

			// ���݂̂̐���
			if( co.role == Role.SEER || co.role == Role.MEDIUM ){

				// ��������󂯂Ă�����CO�����҂͋^��
				if( args.agi.isReceiveWolfJudge(co.agentNo, co.commingOutTalk.getDay(), co.commingOutTalk.getIdx()) ){
					Guess guess = new Guess();
					guess.condition = wolfCondition;
					guess.correlation = 1.2;
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
				final double hoge[] = { 1.0, 1.0, 1.05, 1.1, 1.15, 1.2, 1.3, 1.4, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5 };

				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = hoge[co.commingOutTalk.getDay()];
				guesses.add(guess);

//TODO 2016�~�j���ł͎g�p���Ȃ��B���I����ɖ߂�
//				// �O���܂łɓ�����E��CO������΋^���x���グ��
//				for( ComingOut refCo : args.agi.comingOutList ){
//					if( refCo.role == co.role && refCo.commingOutTalk.getDay() < co.commingOutTalk.getDay() ){
//						switch(co.role){
//							case SEER:
//								guess.correlation *= 1.1;
//								break;
//							case MEDIUM:
//								guess.correlation *= 1.5;
//								break;
//							default:
//								break;
//						}
//						break;
//					}
//				}

			}


		}


		// �������X�g��Ԃ�
		return guesses;
	}

}
