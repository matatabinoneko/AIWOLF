package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;
import java.util.HashSet;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.net.GameSetting;

import com.gmail.aiwolf.uec.yk.condition.OrCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;
import com.gmail.aiwolf.uec.yk.lib.Judge;

/**
 * 推理「ノイズ」クラス
 */
public final class Noise extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();

		GameSetting gameSetting = args.agi.gameSetting;


		// 該当するエージェントを格納する変数
		HashSet<Agent> othersCO = new HashSet();		// CO対象が自分以外
		HashSet<Agent> enemySideCO = new HashSet();		// PPが発生しない状況での人外CO
		HashSet<Agent> invalidAgent = new HashSet();	// 無効なエージェントへの発言
		HashSet<Agent> invalidAction = new HashSet();	// 無効な行動宣言
		HashSet<Agent> emptyTalk = new HashSet();		// 無意味な発言
		HashSet<Agent> invalidJudge = new HashSet();	// 無効な判定出し


		// 全ての発言履歴を確認する
		for( int day = 0; day <= args.agi.latestGameInfo.getDay(); day++ ){
			for( Talk talk : args.agi.getTalkList(day) ){

				double correlation = 1.0;

				// 発言の詳細の取得
				Utterance utterance = args.agi.getUtterance(talk.getContent());

				// 同意発言の場合、発言の意味を取得
				if( utterance.getTopic() == Topic.AGREE ){
					utterance = args.agi.getMeanFromAgreeTalk(talk, 0);
					// 解析不能
					if( utterance == null ){
						invalidAction.add(talk.getAgent());
						continue;
					}
				}

				switch( utterance.getTopic() ){
					case COMINGOUT:
						// CO対象が自分以外
						if( utterance.getTarget().getAgentIdx() != talk.getAgent().getAgentIdx() ){
							othersCO.add(talk.getAgent());
						}
						// PPが発生しない状況での人外CO
						if( utterance.getRole().getTeam() != Team.VILLAGER ){
							int wolfSideNum = gameSetting.getRoleNum(Role.WEREWOLF) + gameSetting.getRoleNum(Role.POSSESSED);
							if( args.agi.dayInfoList.get(day).aliveAgentList.size() > wolfSideNum * 2 ){
								enemySideCO.add(talk.getAgent());
							}
						}
						break;
					case VOTE:
						// 対象が存在しない者
						if( !args.agi.isValidAgentNo( utterance.getTarget().getAgentIdx() ) ){
							invalidAgent.add(talk.getAgent());
							break;
						}
						// 対象が自分
						if( utterance.getTarget().getAgentIdx() == talk.getAgent().getAgentIdx() ){
							invalidAction.add(talk.getAgent());
						}
						// 発言時点で対象が死亡している
						if( args.agi.getCauseOfDeath( utterance.getTarget().getAgentIdx(), talk.getDay() ) != CauseOfDeath.ALIVE  ){
							invalidAction.add(talk.getAgent());
						}
						break;
					case ESTIMATE:
						// 対象が存在しない者
						if( utterance.getTarget().getAgentIdx() < 1 || utterance.getTarget().getAgentIdx() > args.agi.gameSetting.getPlayerNum() ){
							invalidAgent.add(talk.getAgent());
							break;
						}
						// 対象が自分
						if( utterance.getTarget().getAgentIdx() == talk.getAgent().getAgentIdx() ){
							emptyTalk.add(talk.getAgent());
						}
						break;
					case DISAGREE:
						// 発言の意味が解析不能
						emptyTalk.add(talk.getAgent());
						break;
					default:
						break;
				}
			}
		}


		// 全ての占判定を確認する
		for( Judge judge : args.agi.getSeerJudgeList() ){
			// 不正な判定出し(COせずに判定出しなど)
			if( judge.talk.equals(judge.cancelTalk) ){
				invalidJudge.add(Agent.getAgent(judge.agentNo));
			}
		}


		// 全ての霊判定を確認する
		for( Judge judge : args.agi.getMediumJudgeList() ){
			// 不正な判定出し(COせずに判定出しなど)
			if( judge.talk.equals(judge.cancelTalk) ){
				invalidJudge.add(Agent.getAgent(judge.agentNo));
			}
		}



		for( Agent agent : args.agi.latestGameInfo.getAgentList() ){

			double correlation = 1.0;
			RoleCondition wolfCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.WEREWOLF );
			RoleCondition posCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.POSSESSED );
			Guess guess = new Guess();
			guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
			
			// CO対象が自分以外
			if( othersCO.contains(agent) ){
				correlation *= 1.50;
				guess.info.put(13, 1);
			}
			// PPが発生しない状況での人外CO
			if( enemySideCO.contains(agent) ){
				correlation *= 1.50;
				guess.info.put(14, 1);
			}
			// 無効なエージェントへの発言
			if( invalidAgent.contains(agent) ){
				correlation *= 1.30;
			}
			// 無効な行動宣言
			if( invalidAction.contains(agent) ){
				correlation *= 1.05;
			}
			// 無意味な発言
			if( emptyTalk.contains(agent) ){
				correlation *= 1.02;
			}
			// 無効な判定出し
			if( invalidJudge.contains(agent) ){
				correlation *= 1.60;
			}

			// 係数に変化があった場合、推理を追加する
			/*if( Double.compare(correlation, 1.0) != 0 ){
				// 対象が狼or狂のパターンを濃く見る
				RoleCondition wolfCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.WEREWOLF );
				RoleCondition posCondition = RoleCondition.getRoleCondition( agent.getAgentIdx(), Role.POSSESSED );

				Guess guess = new Guess();
				guess.condition = new OrCondition().addCondition(wolfCondition).addCondition(posCondition);
				guess.correlation = correlation;
				guesses.add(guess);
			}*/
			guesses.add(guess);
		}

		// 推理リストを返す
		return guesses;
	}

}
