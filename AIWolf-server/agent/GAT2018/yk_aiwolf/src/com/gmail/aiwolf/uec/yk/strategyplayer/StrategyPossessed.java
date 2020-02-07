package com.gmail.aiwolf.uec.yk.strategyplayer;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;
import com.gmail.aiwolf.uec.yk.lib.Judge;
import com.gmail.aiwolf.uec.yk.lib.ViewpointInfo;


public class StrategyPossessed extends AbstractBaseStrategyPlayer {


	/**
	 * コンストラクタ
	 * @param agentStatistics
	 */
	public StrategyPossessed(AgentStatistics agentStatistics) {
		super(agentStatistics);
	}


	@Override
	public void dayStart() {

	    try{
			super.dayStart();

			// 騙り判定の追加
			if( agi.latestGameInfo.getDay() > 0 ){
				addFakeSeerJudge();
			}
	    }catch(Exception ex){
	        // 例外を再スローする
	        if(isRethrowException){
	            throw ex;
	        }

	        // 以下、例外発生時の代替処理を行う(戻り値があるメソッドの場合は戻す)
	        // Do nothing
	    }

	}

	@Override
	public String talk() {

		try{
			String workString;

			// PP時の発言
			if( agi.isEnablePowerPlay_Possessed() ){
				// 投票先を変更する
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 発話
				String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
				return ret;
			}

			// 未COの場合
			if( !isCameOut ){
				isCameOut = true;

				String ret = TemplateTalkFactory.comingout( me, fakeRole );
				return ret;
			}

			// CO済の場合
			if( isCameOut ){

				// 未報告の結果を報告する
				if( agi.reportSelfResultCount < agi.selfInspectList.size() ){

					Judge reportJudge = agi.selfInspectList.get( agi.selfInspectList.size() - 1 );

					// 報告済みの件数を増やす
					agi.reportSelfResultCount++;

					// 発話
					String ret = TemplateTalkFactory.divined( Agent.getAgent(reportJudge.targetAgentNo), reportJudge.result );
					return ret;
				}

			}

			// 投票先を言っていない場合、新しい投票先を話す
			if( declaredPlanningVoteAgent == null ){
				// 投票先を変更する
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 最終日は狼の便乗票を防ぐため宣言しない。宣言は最終日以外とする（という名目で村と動きをあわせる）
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// 発話
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// 疑い先を狼の人数以上言っていなければ話す
			if( agi.talkedSuspicionAgentList.size() < agi.gameSetting.getRoleNumMap().get(Role.WEREWOLF) ){
				// 疑い先を話す文章を取得し、取得できていれば話す
				workString = getSuspicionTalkString();
				if( workString != null ){
					return workString;
				}
			}

			// 投票先を変更する場合、新しい投票先を話す
			if( declaredPlanningVoteAgent != planningVoteAgent ){
				// 投票先を変更する
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 最終日は狼の便乗票を防ぐため宣言しない。宣言は最終日以外とする（という名目で村と動きをあわせる）
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// 発話
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// 疑い先を話す文章を取得し、取得できていれば話す
			workString = getSuspicionTalkString();
			if( workString != null ){
				return workString;
			}

			// 信用先を話す文章を取得し、取得できていれば話す
			workString = getTrustTalkString();
			if( workString != null ){
				return workString;
			}

			// 話す事が無い場合、overを返す
			return TemplateTalkFactory.over();

		}catch(Exception ex){
	        // 例外を再スローする
	        if(isRethrowException){
	            throw ex;
	        }

	        // 以下、例外発生時の代替処理を行う(戻り値があるメソッドの場合は戻す)

			// エラー時はoverを返す
			return TemplateTalkFactory.over();

		}

	}


	/** 騙り役職を設定する */
	@Override
	protected void setFakeRole(){
		setFakeRole(Role.SEER);
	}


	/**
	 * 占い判定を追加する
	 */
	private void addFakeSeerJudge(){

		GameInfo gameInfo = agi.latestGameInfo;

		// 判定先・判定結果の仮設定
		int inspectAgentNo = latestRequest.getMaxInspectRequest().agentNo;
		Species result = Species.HUMAN;

		// 襲撃されたエージェントの取得
		Agent attackedAgent = agi.latestGameInfo.getAttackedAgent();

		List<Integer> seers = agi.getEnableCOAgentNo(Role.SEER);
		List<Integer> mediums = agi.getEnableCOAgentNo(Role.MEDIUM);

		// 破綻時
		if( agi.selfViewInfo.wolfsidePatterns.isEmpty() ){
			// 生きている狂視点の確白を探す
			List<Integer> whiteList = new ArrayList<Integer>();
			for( int i = 1; i <= agi.gameSetting.getPlayerNum(); i++ ){
				if( agi.agentState[i].causeofDeath == CauseOfDeath.ALIVE && agi.selfRealRoleViewInfo.isFixWhite(i) ){
					whiteList.add(i);
				}
			}
			// 確白に黒出し
			if( !whiteList.isEmpty() ){
				inspectAgentNo = whiteList.get(0);
				result = Species.WEREWOLF;

				Judge newJudge = new Judge( me.getAgentIdx(),
                        inspectAgentNo,
                        result,
                        null );

				agi.addFakeSeerJudge(newJudge);
				return;
			}

		}

		//TODO 一旦これで勝率見て判断
//		// 初回占は黒出しで狂アピ(4CO以下の場合)
//		List<Integer> seers = agi.getEnableCOAgentNo(Role.SEER);
//		List<Integer> mediums = agi.getEnableCOAgentNo(Role.MEDIUM);
//		if( agi.latestGameInfo.getDay() == 1 && seers.size() + mediums.size() <= 4 ){
//			result = Species.WEREWOLF;
//		}


//		// 初回霊能に黒出し
//		if( agi.latestGameInfo.getDay() == 1 && mediums.size() == 1 ){
//			for( Integer medium : mediums ){
//				if( agi.agentState[medium].causeofDeath == CauseOfDeath.ALIVE &&
//					!agi.selfViewInfo.isFixWhite(medium) &&
//					!agi.selfViewInfo.isFixBlack(medium) ){
//					inspectAgentNo = medium;
//					result = Species.WEREWOLF;
//				}
//			}
//		}
//
//		// 対抗に黒出し
//		if( agi.latestGameInfo.getDay() > 1 && seers.size() == 2 ){
//			for( Integer seer : seers ){
//				if( agi.agentState[seer].causeofDeath == CauseOfDeath.ALIVE &&
//					!agi.selfViewInfo.isFixWhite(seer) &&
//					!agi.selfViewInfo.isFixBlack(seer) ){
//					inspectAgentNo = seer;
//					result = Species.WEREWOLF;
//				}
//			}
//		}

//		// 霊能に黒出し
//		if( agi.latestGameInfo.getDay() == 2 && mediums.size() == 1 ){
//			for( Integer medium : mediums ){
//				if( agi.agentState[medium].causeofDeath == CauseOfDeath.ALIVE &&
//					!agi.selfViewInfo.isFixWhite(medium) &&
//					!agi.selfViewInfo.isFixBlack(medium) ){
//					inspectAgentNo = medium;
//					result = Species.WEREWOLF;
//				}
//			}
//		}
//
//		// 対抗に黒出し
//		if( agi.latestGameInfo.getDay() == 1 && seers.size() == 2 ){
//			for( Integer seer : seers ){
//				if( agi.agentState[seer].causeofDeath == CauseOfDeath.ALIVE &&
//					!agi.selfViewInfo.isFixWhite(seer) &&
//					!agi.selfViewInfo.isFixBlack(seer) ){
//					inspectAgentNo = seer;
//					result = Species.WEREWOLF;
//				}
//			}
//		}



		// 白を出した場合の視点を仮定する
		ViewpointInfo future = new ViewpointInfo(agi.selfViewInfo);
		future.removeWolfPattern(inspectAgentNo);

		// 白出しで内訳が破綻する場合、黒出しを行う
		if( future.wolfsidePatterns.isEmpty() ){
			result = Species.WEREWOLF;
		}


		// 占おうとした先が噛まれた
		if( attackedAgent != null && attackedAgent.getAgentIdx() == inspectAgentNo ){
			// 噛み先には人間判定を出す
			result = Species.HUMAN;
		}


		// 霊能が死亡
		boolean mediumAlive = false;
		if( !mediums.isEmpty() ){
			for( int medium : mediums ){
				if( agi.agentState[medium].causeofDeath == CauseOfDeath.ALIVE ){
					// 1人でも生きてれば生存を見る
					mediumAlive = true;
				}else{
					// 死亡した霊が真確定か
					if( agi.selfRealRoleViewInfo.isFixWhite(medium) ){
						mediumAlive = false;
						break;
					}
				}
			}
			// 霊能が全滅
			if( mediumAlive == false ){
				// 40%で白に黒出し
				if( Math.random() < 0.4 ){
					// 生きている確白を探す
					List<Integer> whiteList = new ArrayList<Integer>();
					for( int i = 1; i <= agi.gameSetting.getPlayerNum(); i++ ){
						if( agi.agentState[i].causeofDeath == CauseOfDeath.ALIVE && agi.selfRealRoleViewInfo.isFixWhite(i) ){
							whiteList.add(i);
						}
					}

					// 自分狂視点の確白に黒を出す
					for( Integer white : whiteList ){
						// 自分占視点で色が確定している場合は占わない
						if( agi.selfViewInfo.isFixWhite(white) ||
						    agi.selfViewInfo.isFixBlack(white) ){
							continue;
						}

						// 黒を出した場合の視点を仮定する
						future = new ViewpointInfo(agi.selfViewInfo);
						future.removePatternFromJudge( me.getAgentIdx(), white, Species.WEREWOLF );

						// 黒を出して破綻しないならそこに黒を出す
						if( !future.wolfsidePatterns.isEmpty() ){
							inspectAgentNo = white;
							result = Species.WEREWOLF;
							break;
						}
					}
				}
			}
		}



		// 生存者５名以下（村を１人吊ればPP確定）
		if( gameInfo.getAliveAgentList().size() <= 5 ){

			// 生きている確白を探す
			List<Integer> whiteList = new ArrayList<Integer>();
			for( int i = 1; i <= agi.gameSetting.getPlayerNum(); i++ ){
				if( agi.agentState[i].causeofDeath == CauseOfDeath.ALIVE && agi.selfRealRoleViewInfo.isFixWhite(i) ){
					whiteList.add(i);
				}
			}

			// 自分狂視点の確白に黒を出す
			for( Integer white : whiteList ){
				// 自分占視点で色が確定している場合は占わない
				if( agi.selfViewInfo.isFixWhite(white) ||
				    agi.selfViewInfo.isFixBlack(white) ){
					continue;
				}

				// 黒を出した場合の視点を仮定する
				future = new ViewpointInfo(agi.selfViewInfo);
				future.removePatternFromJudge( me.getAgentIdx(), white, Species.WEREWOLF );

				// 黒を出して破綻しないならそこに黒を出す
				if( !future.wolfsidePatterns.isEmpty() ){
					inspectAgentNo = white;
					result = Species.WEREWOLF;
					break;
				}
			}

		}

		// 確霊とラインが割れるなら判定を反転する
		if( mediums.size() == 1 &&  agi.agentState[mediums.get(0)].causeofDeath == CauseOfDeath.ALIVE ){
			if( result == Species.HUMAN ){
				// 白を出した場合の視点を仮定する
				future = new ViewpointInfo(agi.selfViewInfo);
				future.removeWolfPattern(inspectAgentNo);
				// 未来視点で霊能が確定人外ならラインが割れるため判定反転
				if( future.isFixWolfSide(mediums.get(0)) ){
					result = Species.WEREWOLF;
				}
			}else{
				// 黒を出した場合の視点を仮定する
				future = new ViewpointInfo(agi.selfViewInfo);
				future.removePatternFromJudge(agi.latestGameInfo.getAgent().getAgentIdx(), inspectAgentNo, Species.WEREWOLF);
				// 未来視点で霊能が確定人外ならラインが割れるため判定反転
				if( future.isFixWolfSide(mediums.get(0)) ){
					result = Species.HUMAN;
				}
			}
		}

		Judge newJudge = new Judge( me.getAgentIdx(),
		                            inspectAgentNo,
		                            result,
		                            null );

		agi.addFakeSeerJudge(newJudge);

	}



}
