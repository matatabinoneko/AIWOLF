package com.gmail.k14.itolab.aiwolf.old;

import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.action.GeneralAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.OperatorElement;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;
import com.gmail.k14.itolab.aiwolf.util.TalkSelect;

/**
 * 狂人の行動(5人)
 * @author k14096kk
 *
 */
public class PossessedAction extends BaseRoleAction{

	/**仮フラグ(5人人狼.2日目.暫定人狼更新フラグ)*/
	boolean betaFlag;
	
	
	public PossessedAction(EntityData entityData) {
		super(entityData);
//		ownData.setStrategy(Strategy.FAKESEER);
		setEntityData();
	}
	
	/**
	 * 一日のはじめの行動(整理行動)
	 */
	@Override
	public void dayStart() {
		super.dayStart();
		this.setProveRole();
		
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		switch (ownData.getStrategy()) {
		case FAKESEER:
			this.swindleSeerAction();
			break;
		default:
			this.swindleSeerAction();
			break;
		}
		
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
	/**
	 * 暫定役職を設定する
	 */
	public void setProveRole() {
		// 占い師が暫定役職にいない
		if(!forecastMap.containProvRole(Role.SEER)) {
			List<Agent> seerList = forecastMap.getComingoutRoleAgentList(Role.SEER);
			if(seerList.size()>=2) { // 占い師COが2人以上
				Agent seer = talkDataBase.getEaliestComingout(Role.SEER, ownData.getGameInfo().getDay(), ownData.getMe());
				forecastMap.setProvRole(seer, Role.SEER); //暫定役職に占い師
			}
		}
		// 被襲撃者の暫定役職が決まっていない
		if(forecastMap.getProvRole(ownData.getAttackedAgent())==null) {
			forecastMap.setProvRole(ownData.getAttackedAgent(), Role.VILLAGER);
		}
		
		if(ownData.isFinish()) {
			// 残りの暫定役職
			List<Agent> agentList = forecastMap.getProvRoleAgentList(null);
			forecastMap.setRemainRoleRandom(ownData.getRoleNumMap(), agentList);
		}
	}
	
	/**
	 * 占い師騙りの行動
	 */
	public void swindleSeerAction() {
		
		// 1日目
		if (ownData.currentDay(1)) {
			// 0ターン目
			if (turn.startTurn()) {
				// 1日目 占い師CO
				if(!ownData.isCO() && ownData.currentDay(1)) {
					GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
				}
			}else {
				// 占い師COした状態で，占い結果を話していない
				if (ownData.compareComingoutRole(Role.SEER) && !ownData.isDivine()) {
					// 占いCOしたエージェントを取得(removeで自分を除く)
					List<Agent> storeList = forecastMap.getComingoutRoleAgentList(Role.SEER);
					storeList.remove(ownData.getGameInfo().getAgent());
					// フラグの切り替え
					ownData.setActFlagDivine();
					// 占い先
					Agent target = null;

					// 他に占い師COがいる状態はその占い師を黒判定，いなければランダムに黒判定
					if (!storeList.isEmpty()) {
						target = storeList.get(0);
						forecastMap.setProvRole(target, Role.SEER);
					} else {
						target = RandomSelect.randomAgentSelect(ownData. getAliveOtherAgentList());
					}
					ownData.setVoteTarget(target);

					// 占い結果報告と投票，投票リクエスト
					myTalking.addTalk(TalkFactory.divinedResultRemark(target, Species.WEREWOLF));
					GeneralAction.sayVote(ownData, myTalking);
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(target));
				}
			}
		}

		// 2日目
		if (ownData.currentDay(2)) {
			// (村，狂，狼)
			if (ownData.getBoardArrange().meet(1, 0, 0, 0, 1, 1)) {
				// 自分のみ占い師CO
				if (forecastMap.comingoutRoleDayAgentList(Role.SEER, 1).size() == 1) {
					// 0ターン目
					if(turn.startTurn()) {
						// 人狼COリクエスト(仮) 現状だと[Agent[5]は人狼だとCOしませんか?]になる
						myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.WEREWOLF));
					}
					// ターン1 SKIP
					if (turn.currentTurn(1)) {
						myTalking.addTalk(TalkFactory.skipRemark());
					}
					// ターン2 SKIP
					if (turn.currentTurn(2)) {
						myTalking.addTalk(TalkFactory.skipRemark());
					}
					// ターン3 狂人CO
					if (turn.currentTurn(3)) {
						GeneralAction.sayComingout(ownData, myTalking, Role.WEREWOLF);
					}
					// ターン4 生存者から暫定村人を選んで投票対象へ
					// PP可能かどうか(未実装)
					if (turn.currentTurn(4)) {
						ownData.setVoteTarget(forecastMap.getProvAgent(ownData.getAliveOtherAgentList(), Role.VILLAGER));
						if (Check.isNotNull(ownData.getVoteTarget())) {
							GeneralAction.sayVote(ownData, myTalking);
						}
					}

					if (!betaFlag) {
						// 占い師COと狂人COのエージェントリストを格納
						List<Agent> storeSeerList = talkDataBase.getComingoutRoleAgent(ownData.getTalkList(), Role.SEER);
						List<Agent> storePossList = talkDataBase.getComingoutRoleAgent(ownData.getTalkList(), Role.POSSESSED);
						// COしたエージェントがいれば暫定人狼
						if (!storeSeerList.isEmpty()) {
							forecastMap.setProvRole(storeSeerList.get(0), Role.WEREWOLF);
							betaFlag = true;
						}
						if (!storePossList.isEmpty()) {
							forecastMap.setProvRole(storePossList.get(0), Role.WEREWOLF);
							betaFlag = true;
						}
					}

					// 人狼COが2人
					if (ownData.getCountCO(Role.WEREWOLF) == 2) {
						// 0ターン目
						if(turn.startTurn()) {
							// 占い師COしたエージェントが生存
							if(forecastMap.getComingoutRoleAliveAgent(ownData.getAliveAgentList(), Role.SEER)!=null) {
								// PPリクエスト(仮)
								Agent poss = forecastMap.getProvAgent(ownData.getAliveAgentList(), Role.POSSESSED);
								if(Check.isNotNull(poss)) {
									myTalking.addTalk(TalkFactory.requestAllComingoutRemark(poss, Role.POSSESSED));
								}
							}
						}
						// 村人も人狼騙り
						if (!OperatorElement.getRequestTalkList(ownData.getTalkList()).isEmpty()) {
							Agent requestAgent = TalkSelect.getEaliestRequest(OperatorElement.getRequestTalkList(ownData.getTalkList()), ownData.getMe());
							Agent coAgent = TalkSelect.getEaliestComingout(ownData.getTalkList(), Role.WEREWOLF, ownData.getMe());
							// リクエストが最も早いエージェントと人狼COが最も早いエージェントが異なる(nullは保険)
							if (requestAgent != null && coAgent != null) {
								if (requestAgent != coAgent) {
									forecastMap.setProvRole(requestAgent, Role.WEREWOLF);
									forecastMap.setProvRole(coAgent, Role.VILLAGER);
								}
							}
						}
					}
				}
				// 占い師CO 2人
				if (ownData.getCountCO(Role.SEER) == 2) {
					// ターン1 狂人CO
					if (turn.currentTurn(1)) {
						myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.POSSESSED));
					}
					// ターン2 生存者から暫定村人を選んで投票対象へ
					if (turn.currentTurn(2)) {
						ownData.setVoteTarget(forecastMap.getProvAgent(ownData.getAliveAgentList(), Role.VILLAGER));
						if (ownData.getVoteTarget() != null) {
							myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
							ownData.setActFlagVote();
						}
					}
					// ターン4 OVER
					if (turn.currentTurn(4)) {
						ownData.setActFlagFinish();
					}
				}

				// 占い師CO 3人 (占い師2と同様)
				if (ownData.getCountCO(Role.SEER) == 3) {
					
					if(turn.startTurn()) {
						// 占い師COしたエージェントが生存
						if(forecastMap.getComingoutRoleAliveAgent(ownData.getAliveAgentList(), Role.SEER)!=null) {
							//PPリクエスト(未実装)
						}
					}
					// ターン1 狂人CO
					if (turn.currentTurn(1)) {
						myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.POSSESSED));
					}
					// ターン2 生存者から暫定村人を選んで投票対象へ
					if (turn.currentTurn(2)) {
						ownData.setVoteTarget(forecastMap.getProvAgent(ownData.getAliveAgentList(), Role.VILLAGER));
						if (ownData.getVoteTarget() != null) {
							myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
							ownData.setActFlagVote();
						}
					}
					// ターン4 OVER
					if (turn.currentTurn(4)) {
						ownData.setActFlagFinish();
					}
				}
			}

			// (占，狂，狼)
			if (ownData.getBoardArrange().meet(0, 1, 0, 0, 1, 1)) {
				// 自分のみ占い師CO
				if (ownData.getCountCO(Role.SEER) == 1) {
					// ターン1 狂人CO
					if (turn.currentTurn(1)) {
						myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.POSSESSED));
					}
					// ターン2 生存者から暫定村人を選んで投票対象へ
					if (turn.currentTurn(2)) {
						ownData.setVoteTarget(forecastMap.getProvAgent(ownData.getAliveAgentList(), Role.VILLAGER));
						if (ownData.getVoteTarget() != null) {
							myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
							ownData.setActFlagVote();
						}
					}
					// ターン4 OVER
					if (turn.currentTurn(4)) {
						ownData.setActFlagFinish();
					}
				}

				// 占い師CO 2人
				if (ownData.getCountCO(Role.SEER) == 2) {
					// 人狼COエージェントがいるかどうか
					if (!forecastMap.getComingoutRoleAgentList(Role.WEREWOLF).isEmpty()) {
						// 人狼COエージェント==暫定占い師エージェント
						if (forecastMap.getComingoutRoleAgentList(Role.WEREWOLF).get(0) == forecastMap
								.getComingoutRoleAgentList(Role.SEER).get(0)) {
							// 確定占い師へ
							forecastMap.setConfirmRole(
									forecastMap.getComingoutRoleAgentList(Role.WEREWOLF).get(0), Role.SEER);
							// リクエストPP(未実装)
							ownData
									.setVoteTarget(forecastMap.getComingoutRoleAgentList(Role.WEREWOLF).get(0));
							myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
						}
					} else {
						// 投票先
						Agent voteT = null;
						// 暫定役職に占い師がいればそいつを投票対象
						if(!forecastMap.getProvRoleAgentList(Role.SEER).isEmpty()) {
							voteT = forecastMap.getProvRoleAgentList(Role.SEER).get(0);
						}
						ownData.setVoteTarget(voteT);
						myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
					}
				}
			}

		}
		
		
		
		
		//6ターン目ならば発話終了状態に切り替え
		if(turn.currentTurn(6)) {
			ownData.setActFlagFinish();
		}
		
		if(ownData.isFinish()) {
			myTalking.addTalk(TalkFactory.overRemark());
		}
		
	}
	
}
