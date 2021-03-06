package com.gmail.aiwolf.uec.yk.strategyplayer;

import java.util.List;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.lib.Judge;


public class StrategyMedium extends AbstractBaseStrategyPlayer {


	/**
	 * コンストラクタ
	 * @param agentStatistics
	 */
	public StrategyMedium(AgentStatistics agentStatistics) {
		super(agentStatistics);
	}


	@Override
	public String talk() {

		try{

			String workString;

			// 未COの場合
			if( !isCameOut ){

	//			// 回避COが必要なら回避COする
	//			if( isAvoidance() ){
	//				isCameOut = true;
	//
	//				// 発話
	//				workString = TemplateTalkFactory.comingout(getMe(), Role.MEDIUM);
	//				return workString;
	//			}
	//
	//			// 自発COが必要ならCOする
	//			if( isVoluntaryComingOut() ){
	//				isCameOut = true;
	//
	//				// 発話
	//				workString = TemplateTalkFactory.comingout(getMe(), Role.MEDIUM);
	//				return workString;
	//			}

				// COする
				isCameOut = true;

				// 発話
				workString = TemplateTalkFactory.comingout(me, Role.MEDIUM);
				return workString;
			}

			// CO済の場合
			if( isCameOut ){

				// 未報告の結果を報告する
				if( agi.reportSelfResultCount < agi.selfInquestList.size() ){

					Judge reportJudge = agi.selfInquestList.get( agi.selfInquestList.size() - 1 );

					// 報告済みの件数を増やす
					agi.reportSelfResultCount++;

					// 発話
					workString = TemplateTalkFactory.inquested( Agent.getAgent(reportJudge.targetAgentNo), reportJudge.result );
					return workString;
				}

			}

			// 投票先を言っていない場合、新しい投票先を話す
			if( declaredPlanningVoteAgent == null ){
				// 投票先を変更する
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 最終日は狼の便乗票を防ぐため宣言しない。宣言は最終日以外とする
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

				// 最終日は狼の便乗票を防ぐため宣言しない。宣言は最終日以外とする
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// 発話
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
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


	/**
	 * 自発的霊COするか
	 * @return
	 */
	private boolean isVoluntaryComingOut(){

		// 各役職のCO者を取得
		List<Integer> seers = agi.getEnableCOAgentNo(Role.SEER);
		List<Integer> mediums = agi.getEnableCOAgentNo(Role.MEDIUM);
		List<Integer> bodyguards = agi.getEnableCOAgentNo(Role.BODYGUARD);

		// 2日目
		if( day >= 2 ){
			// COする
			return true;
		}

		// 霊騙りが存在
		if( !mediums.isEmpty() ){
			// COする
			return true;
		}

		// 全露出
		if( seers.size() >= 5 ||
		    bodyguards.size() >= 5 ||
		    seers.size() + bodyguards.size() >= 6 ){
			// COする
			return true;
		}

		// 占われた（黒ならCCO、白なら噛まれやすいので乗っ取り防止）
		for( Judge judge : agi.getSeerJudgeList() ){
			if( judge.isEnable() && judge.targetAgentNo == me.getAgentIdx() ){
				// COする
				return true;
			}
		}

		return false;

	}

}
