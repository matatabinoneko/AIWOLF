package com.gmail.k14.itolab.aiwolf.old;

import java.util.List;


import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.action.GeneralAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 霊媒師の行動(15人)
 * @author k14096kk
 *
 */
public class MediumMaxAction extends BaseRoleAction {

	/**予想対象*/
	Agent estimateTarget = null;

	public MediumMaxAction(EntityData entityData) {
		super(entityData);
		setEntityData();
	}

	@Override
	public void dayStart() {
		super.dayStart();

		// 霊媒結果が存在する
		if(Check.isNotNull(ownData.getGameInfo().getMediumResult())) {
			ownData.setIdentResultMap(ownData.getGameInfo().getMediumResult());
			// 霊媒対象を占った結果をリスト化
			List<Judge> judgeList = forecastMap.getDivineJudgeTarget(ownData.getGameInfo().getMediumResult().getTarget());

			// 霊媒結果が人間判定ならば疑い度減少，狼ならば疑い度上昇
			if(Check.isSpecies(ownData.getGameInfo().getMediumResult().getResult(), Species.HUMAN)) {
				// 疑い度減少
				forecastMap.minusDoubt(ownData.getGameInfo().getMediumResult().getTarget(), 2);

				// 占い結果出したエージェントを判断
				for(Judge judge: judgeList) {
					// 占い結果が黒：占い師が偽物
					if(Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
						forecastMap.plusDoubt(judge.getAgent(), 0.3);
						forecastMap.setProvRole(judge.getAgent(), Role.POSSESSED);
					}else { // 占い結果が白
						forecastMap.minusDoubt(judge.getAgent(), 0.2);						
					}
				}
			}else {
				// 疑い度上昇
				forecastMap.plusDoubt(ownData.getGameInfo().getMediumResult().getTarget(), 2);
				// 暫定と確定役職に人狼
				forecastMap.setProvRole(ownData.getGameInfo().getMediumResult().getTarget(), Role.WEREWOLF);
				forecastMap.setConfirmRole(ownData.getGameInfo().getMediumResult().getTarget(), Role.WEREWOLF);

				// 占い結果出したエージェントを判断
				for(Judge judge: judgeList) {
					// 占い結果が黒
					if(Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
						forecastMap.minusDoubt(judge.getAgent(), 0.2);

					}else { // 占い結果が白：占い師は偽物
						forecastMap.plusDoubt(judge.getAgent(), 0.3);
						forecastMap.setProvRole(judge.getAgent(), Role.POSSESSED);
					}
				}
			}	
		}

		setEntityData();
		ownData.reverseActFlagIdent();
	}

	@Override
	public void selectAction() {
		super.selectAction();
		normalAction();
		ownData.rejectReaction();
		setEntityData();
	}

	@Override
	public void talkAction(Talk talk, Content content) {
		super.talkAction(talk, content);
		setEntityData();
	}

	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		// 1日目にCOリクエストが1/4以上ならCOしちゃえ(仮FO)
		if(ownData.currentDay(1)) {
			// 0~3ターン目まで

			/*
			if(turn.beforeTurn(3)) {
				if(!ownData.isCO()) {
					if(Check.isTopic(reqContent.getTopic(), Topic.COMINGOUT) && Check.isRole(reqContent.getRole(), Role.MEDIUM)) {
						GeneralAction.sayComingout(ownData, myTalking, Role.MEDIUM);
					}
				}
			}
			 */
		}

		setEntityData();
	}

	/**
	 * 通常行動
	 */
	public void normalAction() {
		List<Agent> coList = forecastMap.getComingoutRoleAgentList(Role.SEER);
		//List<Agent> doubtList = forecastMap.moreDoubtAgentList(coList);

		coList.remove(ownData.getMe());
		if(!forecastMap.getComingoutRoleAgentList(Role.MEDIUM).isEmpty() && !ownData.isCO()){
			myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.MEDIUM));
			ownData.setActFlagCO();
			if(!coList.isEmpty()){
				myTalking.addTalk(TalkFactory.estimateRemark(coList.get(0), Role.WEREWOLF));
			}
		}
		
		if(Check.isNotNull(ownData.getGameInfo().getDivineResult())){
			if(Check.isSpecies(ownData.getGameInfo().getMediumResult().getResult(), Species.WEREWOLF)){
				if(ownData.getGameInfo().getDivineResult().getTarget() == ownData.getMe()){
					myTalking.addTalk(TalkFactory.estimateRemark(ownData.getGameInfo().getDivineResult().getAgent(), Role.WEREWOLF));
				}
			}
		}

		// 1日目
		if(ownData.currentDay(1)) {
			// 1ターン目 / 2ターン目
			if(turn.currentTurn(0)){
				// FO
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.SEER));
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.MEDIUM));
			}
			// 3ターン目
			if(turn.currentTurn(2)){
				/* skip */
				myTalking.addTalk(TalkFactory.skipRemark());
			}
			// 4ターン目
			if(turn.currentTurn(3)){
				/* skip */
			}
			// 5ターン目
			if(turn.currentTurn(4)){
				// 生存エージェントからCOしているエージェントを外して、占い対象選択
				List<Agent> divineList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
				divineList.removeAll(forecastMap.getComingoutAgentList());
				if(!divineList.isEmpty())
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(divineList.get(0)));
			}
			// 6ターン目
			if(turn.currentTurn(5)){
				// ガードリクエスト
				if(ownData.isCO())
					myTalking.addTalk(TalkFactory.requestAllGuardRemark(ownData.getMe()));
				else
					myTalking.addTalk(TalkFactory.requestAllGuardRemark(forecastMap.getProvAgent(Role.SEER)));
			}
			// 7ターン目 /8ターン目
			if(turn.currentTurn(6)){
				estimateTarget = forecastMap.getProvAgent(Role.WEREWOLF);

				// 投票とリクエストの合計回数が多い順のリスト
				List<CountAgent> voteCountList = voteCounter.moreCountList(voteCounter.getAllMap());
				voteCountList.remove(ownData.getMe());

				if(!voteCountList.isEmpty()){
					// 生存エージェントの半数が投票しているか
					if(voteCountList.get(0).count >= (ownData.getAliveAgentList().size()/2)) {
						// 対象が疑い度が低めであれば，便乗して投票対象決定
						if(forecastMap.getDoubt(voteCountList.get(0).agent) >= 0.2) {
							ownData.setVoteTarget(voteCountList.get(0).agent);
						}
					}
				}
				else if(estimateTarget==null){
					ownData.setVoteTarget(forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList()).get(0));
				}
				else{
					ownData.setVoteTarget(estimateTarget);
				}

				GeneralAction.sayVote(ownData, myTalking);
			}

		}

		else if(ownData.currentDay(4)){
			// PPの判断用
			// 生存人数が７　生存エージェントの暫定役職に狂人がいる　死亡リストに人狼がいない
			if(ownData.getGameInfo().getAliveAgentList().size()==9 
					&& !forecastMap.getProvRoleAgentList(Role.POSSESSED).isEmpty()
					&& !ownData.getDeadAgentList().contains(forecastMap.getProvRoleAgentList(Role.WEREWOLF))){
				ownData.setVoteTarget(forecastMap.getProvAgent(Role.POSSESSED));
				GeneralAction.sayVote(ownData, myTalking);
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(forecastMap.getProvAgent(Role.POSSESSED)));
				myTalking.addTalk(TalkFactory.overRemark());
			}
		}
		else if(ownData.currentDay(5)){
			// 生存人数が５　生存エージェントの暫定役職に狂人がいる　死亡リストに人狼が含まれている
			if(ownData.getGameInfo().getAliveAgentList().size()==7 
					&& !forecastMap.getProvRoleAgentList(Role.POSSESSED).isEmpty()
					&& ownData.getDeadAgentList().contains(forecastMap.getProvRoleAgentList(Role.WEREWOLF))){
				ownData.setVoteTarget(forecastMap.getProvAgent(Role.POSSESSED));
				GeneralAction.sayVote(ownData, myTalking);
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(forecastMap.getProvAgent(Role.POSSESSED)));
				myTalking.addTalk(TalkFactory.overRemark());
			}
		}
		else if(ownData.currentDay(6)){
			// 生存人数が３　生存エージェントの暫定役職に狂人がいる
			if(ownData.getGameInfo().getAliveAgentList().size()==5 
					&& !forecastMap.getProvRoleAgentList(Role.POSSESSED).isEmpty()){
				ownData.setVoteTarget(forecastMap.getProvAgent(Role.POSSESSED));
				GeneralAction.sayVote(ownData, myTalking);
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(forecastMap.getProvAgent(Role.POSSESSED)));
				myTalking.addTalk(TalkFactory.overRemark());
			}
		}
		
		// 2日目以降
		else if(ownData.afterDay(2)) {
			if(!ownData.isIdent() && !ownData.isCO() && Check.isSpecies(ownData.getGameInfo().getMediumResult().getResult(), Species.WEREWOLF)){
				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.MEDIUM));
				myTalking.addTalk(TalkFactory.identRemark(ownData.getGameInfo().getMediumResult().getTarget(),ownData.getGameInfo().getMediumResult().getResult()));
				ownData.setActFlagCO();
				ownData.setActFlagIdent();
			}
			// 1ターン目
			if(turn.currentTurn(0)){
				myTalking.addTalk(TalkFactory.identRemark(ownData.getGameInfo().getMediumResult().getTarget(),ownData.getGameInfo().getMediumResult().getResult()));
				ownData.setActFlagIdent();
			}

			// 2ターン目
			if(turn.currentTurn(1)){
				/* skip */
			}
			// 3ターン目
			if(turn.currentTurn(2)){
				// 疑い度が最も低い占いCOエージェントを占い師と予想
				if(!forecastMap.getProvRoleAgentList(Role.SEER).isEmpty()){
					myTalking.addTalk(TalkFactory.estimateRemark(forecastMap.fewerDoubtAgentList(forecastMap.getProvRoleAgentList(Role.SEER)).get(0), Role.SEER));
				}

			}
			// 4ターン目
			if(turn.currentTurn(3)){
				if(Check.isNotNull(ownData.getGameInfo().getMediumResult()) && Check.isNotNull(ownData.getGameInfo().getDivineResult())){
					// ownData.setIdentResultMap(ownData.getGameInfo().getMediumResult());
					// 霊媒対象を占った結果をリスト化
					List<Judge> judgeList = forecastMap.getDivineJudgeTarget(ownData.getGameInfo().getMediumResult().getTarget());

					if(Check.isSpecies(ownData.getGameInfo().getMediumResult().getResult(), Species.WEREWOLF)){
						if(ownData.getGameInfo().getDivineResult().getTarget() == ownData.getMe()){
							myTalking.addTalk(TalkFactory.estimateRemark(ownData.getGameInfo().getDivineResult().getAgent(), Role.WEREWOLF));
						}
						else{
							for(Judge judge: judgeList) {
								// 占い結果が黒：占い師が偽物
								if(Check.isSpecies(judge.getResult(), Species.HUMAN)) {
									myTalking.addTalk(TalkFactory.estimateRemark(judge.getAgent(), Role.WEREWOLF));
								}
							}
						}
					}
					else if(Check.isSpecies(ownData.getGameInfo().getMediumResult().getResult(), Species.HUMAN)) {
						for(Judge judge: judgeList) {
							// 占い結果が黒：占い師が偽物
							if(Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
								myTalking.addTalk(TalkFactory.estimateRemark(judge.getAgent(), Role.WEREWOLF));
							}
						}
					}
				}
			}

			// 5ターン目
			if(turn.currentTurn(4)){
				//占いリクエスト
				List<Agent> divineList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
				divineList.removeAll(forecastMap.getComingoutAgentList());
				if(!divineList.isEmpty())
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(divineList.get(0)));
				else
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(ownData.getAliveOtherAgentList().get(0)));
			}
			// 6ターン目
			if(turn.currentTurn(5)){
				// ガードリクエスト

				if(forecastMap.getConfirmRoleAgentList(Role.SEER).size()>=1){
					// 確定占い師がいるならそちらへ護衛要求
					myTalking.addTalk(TalkFactory.requestAllGuardRemark(forecastMap.getConfirmRoleAgentList(Role.SEER).get(0)));
				}
				else if(ownData.isCO()){
					// 自分がCOしていて、確定占い師がいないなら自分へ護衛リクエスト
					myTalking.addTalk(TalkFactory.requestAllGuardRemark(ownData.getMe()));
				}
				else{
					myTalking.addTalk(TalkFactory.requestAllGuardRemark(forecastMap.getProvAgent(Role.SEER)));
				}
			}
			// 7ターン目
			if(turn.currentTurn(6)){
				// PP阻止 
				/**
				 * PPflug
				 * 	if(ownData.getAliveAgentList().size()==9 && 狂人生存（暫定、確定どちらでも） && 死亡リストに黒が０人)　または
				 *  if(ownData.getAliveAgentList().size()==7 && 狂人生存（暫定、確定どちらでも） && 死亡リストに黒が１人)　または
				 *  if(ownData.getAliveAgentList().size()==5 && 狂人生存（暫定、確定どちらでも） && 死亡リストに黒が２人)　のとき
				 *  
				 *  flug = true; 
				 */
				boolean PPflug = false;
				if(PPflug){
					Agent voteTarget = forecastMap.getProvAgent(Role.POSSESSED);
					ownData.setVoteTarget(voteTarget);
					GeneralAction.sayVote(ownData, myTalking);
				}
				else{
					// 占い結果と疑い度を元に人狼予想して，予想先を保持
					estimateTarget = GeneralAction.sayEstimateWolf(forecastMap, ownData, myTalking);

					// 投票とリクエストの合計回数が多い順のリスト
					List<CountAgent> voteCountList = voteCounter.moreCountList(voteCounter.getAllMap());
					voteCountList.remove(ownData.getMe());

					if(!voteCountList.isEmpty()) {
						// 生存人数の半分以上投票とリクエストされているか
						if(voteCountList.get(0).count >= (ownData.getAliveAgentList().size()/2)) {
							// 対象が疑い度が低めであれば，便乗して投票対象決定
							if(forecastMap.getDoubt(voteCountList.get(0).agent) >= 0.2) {
								ownData.setVoteTarget(voteCountList.get(0).agent);
							}
						}
					}
					// 投票対象がいない(便乗投票なし)ならば予想先を投票
					else if(Check.isNull(ownData.getVoteTarget())) {
						ownData.setVoteTarget(forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList()).get(0));
					}
					else{
						ownData.setVoteTarget(forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList()).get(0));
					}
					// 投票発言
					GeneralAction.sayVote(ownData, myTalking);
				}
				// 疑い度最大(投票対象を除く)のエージェントを占い対象リクエスト
				// GeneralAction.sayDoubtRequestDivine(forecastMap, ownData, myTalking, ownData.getVoteTarget());
			}	
		}
	}
}
