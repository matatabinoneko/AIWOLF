package jp.halfmoon.inaba.aiwolf.guess;

import java.util.ArrayList;

import org.aiwolf.common.data.Role;

import jp.halfmoon.inaba.aiwolf.condition.OrCondition;
import jp.halfmoon.inaba.aiwolf.condition.RoleCondition;
import jp.halfmoon.inaba.aiwolf.lib.ComingOut;

/**
 * 推理「CO」クラス
 */
public final class COTiming extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {
		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();

		for( ComingOut co : args.agi.comingOutList ){

			RoleCondition wolfCondition = RoleCondition.getRoleCondition( co.agentNo, Role.WEREWOLF );
			RoleCondition posCondition = RoleCondition.getRoleCondition( co.agentNo, Role.POSSESSED );

			// 無効になっているCOがある者は疑う
			if( !co.isEnable() ){
				// CO中の役職を再度COした場合は対象外
				if( co.role != args.agi.getCORole(co.agentNo, co.commingOutTalk.getDay(), co.commingOutTalk.getIdx()) ){
					Guess guess = new Guess();
					guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
					guess.correlation = 1.3;
					guesses.add(guess);
				}
			}

			// 配役に存在しない役をCOした者は疑う
			if( args.agi.gameSetting.getRoleNum(co.role) < 1 ){
				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = 3.0;
				guesses.add(guess);
			}

			// 占霊のみの推理
			if( co.role == Role.SEER || co.role == Role.MEDIUM ){

				// 黒判定を受けてから占霊COした者は疑う
				if( args.agi.isReceiveWolfJudge(co.agentNo, co.commingOutTalk.getDay(), co.commingOutTalk.getIdx()) ){
					Guess guess = new Guess();
					guess.condition = wolfCondition;
					guess.correlation = 1.2;
					guesses.add(guess);
				}

				//TODO 疑うのは自発COに限定し、対抗COは許可する（判断面倒なので締め切り的に断念）
	//			// n順目以降でCOした者は疑う
	//			if( co.commingOutTalk.getIdx() >= args.agi.gameSetting.getPlayerNum() * 5 ){
	//				Guess guess = new Guess();
	//				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
	//				guess.correlation = 1.1;
	//				guesses.add(guess);
	//			}

				//TODO 他編成対応
				// COしたタイミングによって疑い度を上げる
				final double hoge[] = { 1.0, 1.0, 1.05, 1.1, 1.15, 1.2, 1.3, 1.4, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5 };

				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = hoge[co.commingOutTalk.getDay()];
				guesses.add(guess);

//TODO 2016ミニ大会では使用しない。大会終了後に戻す
//				// 前日までに同じ役職のCOがあれば疑い度を上げる
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


		// 推理リストを返す
		return guesses;
	}

}
