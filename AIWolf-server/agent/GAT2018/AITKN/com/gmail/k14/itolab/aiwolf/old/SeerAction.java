package com.gmail.k14.itolab.aiwolf.old;


import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.action.GeneralAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;
import com.gmail.k14.itolab.aiwolf.util.TalkSelect;

/**
 * 占い師の行動(5人)
 * @author k14096kk
 *
 */
public class SeerAction extends BaseRoleAction{

	/**狼見つけたフラグ*/
	private boolean findWolf;
	/**仮フラグ(狂人COの確認フラグ)*/
	private boolean betaFlag;

	
	public SeerAction(EntityData entityData) {
		super(entityData);
//		ownData.setStrategy(Strategy.HIDE);
		ownData.rejectReaction();
		setEntityData();
	}
	
	/**
	 * 一日のはじめの行動(整理行動)
	 */
	@Override
	public void dayStart() {
		super.dayStart();
		this.setProve();
		// 占い結果が存在すれば保持
		if(ownData.getGameInfo().getDivineResult()!=null) {
			ownData.setDivineResultMap(ownData.getGameInfo().getDivineResult());
		}
		if(ownData.isFinish()) {
			//this.testProve();
		}
		
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		// 戦略に乗っ取った発言
		switch (ownData.getStrategy()) {
		case HIDE:
			this.hideSeerAction();
			break;
		default:
			this.hideSeerAction();
			break;
		}
		
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
	@Override
	public void divine() {
		super.divine();
		decideVoteTarget();
		setEntityData();
	}
	
	public void decideVoteTarget() {
		// 0日目は占い先ランダム
		if(ownData.currentDay(0)) {
			Agent target = RandomSelect.randomAgentSelect(ownData.getAliveAgentList(), ownData.getMe());
			ownData.setDivineTarget(target);
		}
		// 1日目は占い先ランダム
		if(ownData.currentDay(1)) {
			Agent target = RandomSelect.randomAgentSelect(ownData.getAliveAgentList(), ownData.getMe());
			ownData.setDivineTarget(target);
		}
		
		setEntityData();
	}
	
	/**
	 * 暫定役職決定処理
	 */
	public void setProve() {
		//　占った結果が狼ならば暫定役職，確定役職を狼
		if(ownData.getDivineResultSpecies(ownData.getDivineTarget())==Species.WEREWOLF) {
			forecastMap.setProvRole(ownData.getDivineTarget(), Role.WEREWOLF);
			forecastMap.setConfirmRole(ownData.getDivineTarget(), Role.WEREWOLF);
		}
		// 2日目の朝
		if(ownData.currentDay(2)) {
			
			// 狼を見つけていない
			if(!findWolf){
				// 1CO(自分以外)ならば暫定狂人
				if(ownData.getCountCO(Role.SEER)==1) {
					// COしたエージェントのリスト
					List<Agent> coSeerList = forecastMap.getComingoutRoleAgentList(Role.SEER);
					forecastMap.setProvRole(coSeerList.get(0), Role.POSSESSED);
				}
				// 3CO(自分以外に占い師CO2)
				if(ownData.getCountCO(Role.SEER)==3) {
					
				}
				// COなし 発話数が少ない順に狼，村，狂割当て
				if(ownData.getCountCO(Role.SEER)==0) {
					this.testProve();
				}
			}
		}
	}
	
	/**
	 * 暫定役職設定(テスト)
	 */
	public void testProve() {
		List<Agent> agents = talkDataBase.fewerTalkAgentList(ownData.getAliveAgentList(), ownData.getDay()-1);
		agents.remove(ownData.getMe());
		for(int i=0; i<agents.size(); i++) {
			agents = forecastMap.setRemainRoleOrder(ownData.getRoleNumMap(), agents, Role.WEREWOLF);
			agents = forecastMap.setRemainRoleOrder(ownData.getRoleNumMap(), agents, Role.VILLAGER);
			agents = forecastMap.setRemainRoleOrder(ownData.getRoleNumMap(), agents, Role.POSSESSED);
		}
	}
	
	/**
	 * 潜伏占い師の行動
	 */
	public void hideSeerAction() {
		// 1日目
		if(ownData.currentDay(1)) {
			// 最初ターン
			if(turn.startTurn()) {
				// 狼を見つけた場合
				if(ownData.containDivineResultSpecies(Species.WEREWOLF) && !ownData.isCO()) {
					// 占い師CO
					GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
					this.findWolf = true;
				}
			}
			if(turn.currentTurn(1)) { 
				// 狼を見つけたら結果報告
				if(this.findWolf) {
					myTalking.addTalk(TalkFactory.divinedResultRemark(ownData.getDivineTarget(), ownData.getDivineResultSpecies(ownData.getDivineTarget())));
					ownData.setActFlagDivine();
					ownData.setVoteTarget(ownData.getDivineTarget());
					GeneralAction.sayVote(ownData, myTalking);
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
				}else {
					// 占い師CO数2以上(自分以外に2人CO)
					if(ownData.getCountCO(Role.SEER)>=2) {
						// 占い師CO
						GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
						myTalking.addTalk(TalkFactory.divinedResultRemark(ownData.getDivineTarget(), ownData.getDivineResultSpecies(ownData.getDivineTarget())));
						ownData.setActFlagDivine();
						
						// COしたエージェントのリスト
						List<Agent> coSeerList = forecastMap.getComingoutRoleAgentList(Role.SEER);
						coSeerList.remove(ownData.getMe());
						// 投票リクエストが最大のエージェント
						Agent target = voteCounter.maxCountAgent(voteCounter.getRequestMap(), coSeerList);
						// 投票リクエストが最大のエージェントがいれば，そのエージェントに投票
						if(Check.isNotNull(target)) {
							ownData.setVoteTarget(target);
							GeneralAction.sayVote(ownData, myTalking);
						}else { // いなければCOしたエージェントにランダムに投票
							target = RandomSelect.randomAgentSelect(coSeerList);
							ownData.setVoteTarget(target);
							GeneralAction.sayVote(ownData, myTalking);
						}
					}
				}
			}
		}
		
		/*2日目*/
		if(ownData.currentDay(2)) {
			/*(村，占，狼)*/
			if(ownData.getBoardArrange().meet(1, 1, 0, 0, 0, 1)) {
				if(turn.startTurn()) {
					// 1日目狼未発見 & 2日占い結果白 
					if(!findWolf && Check.isSpecies(ownData.getDivineResultJudge(ownData.getDay()).getResult(), Species.HUMAN)) {
						// 占った人を暫定，確定村人
						forecastMap.setProvRole(ownData.getDivineResultJudge(ownData.getDay()).getTarget(), Role.VILLAGER);
						forecastMap.setConfirmRole(ownData.getDivineResultJudge(ownData.getDay()).getTarget(), Role.VILLAGER);
						// 自分，占った対象を除外
						List<Agent> aliveAgentList = ownData.getAliveOtherAgentList();
						aliveAgentList.remove(ownData.getDivineTarget());
						// 残った生存者が狼
						Agent wolf = aliveAgentList.get(0);
						forecastMap.setProvRole(wolf, Role.WEREWOLF);
						forecastMap.setConfirmRole(wolf, Role.WEREWOLF);
						// 残った人の占い結果が狼発言
						myTalking.addTalk(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
						// 狼に投票とリクエスト
						ownData.setVoteTarget(ownData.getDivineTarget());
						GeneralAction.sayVote(ownData, myTalking);
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getDivineTarget()));
					}else { // 黒発見
						// 占い先を暫定，確定狼
						Agent wolf = ownData.getDivineTarget();
						forecastMap.setProvRole(wolf, Role.WEREWOLF);
						forecastMap.setConfirmRole(wolf, Role.WEREWOLF);
						// 占い結果報告
						myTalking.addTalk(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
						ownData.setVoteTarget(ownData.getDivineTarget());
						// 占い先を投票，投票リクエスト
						GeneralAction.sayVote(ownData, myTalking);
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getDivineTarget()));
					}
				}
			}
			/*(占，狂，狼)*/
			if(ownData.getBoardArrange().meet(0, 1, 0, 0, 1, 1)) {
//				// 最初ターン
//				if(turn.startTurn()) {
//					// 狼を見つけていなくて，2回目の占い結果が白
//					if(!findWolf && Check.isSpecies(ownData.getDivineResultJudge(ownData.getDay()).getResult(), Species.HUMAN)) {
//						// 占った人を暫定，確定村人
//						forecastMap.setProvRole(ownData.getDivineTarget(), Role.VILLAGER);
//						forecastMap.setConfirmRole(ownData.getDivineTarget(), Role.VILLAGER);
//						// 自分，占った対象を除外
//						List<Agent> aliveAgentList = ownData.getAliveOtherAgentList();
//						aliveAgentList.remove(ownData.getDivineTarget());
//						// 残った生存者が狼
//						Agent wolf = aliveAgentList.get(0);
//						forecastMap.setProvRole(wolf, Role.WEREWOLF);
//						forecastMap.setConfirmRole(wolf, Role.WEREWOLF);
//						myTalking.addTalk(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
//						ownData.setVoteTarget(ownData.getDivineTarget());
//						myTalking.addTalk(TalkFactory.voteRemark(ownData.getDivineTarget()));
//						
//						ownData.setActFlagVote();
//						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getDivineTarget()));
//					}else { // 黒発見
//						// 占い先を暫定，確定狼
//						Agent wolf = ownData.getDivineTarget();
//						forecastMap.setProvRole(wolf, Role.WEREWOLF);
//						forecastMap.setConfirmRole(wolf, Role.WEREWOLF);
//						// 占い結果報告
//						myTalking.addTalk(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
//						ownData.setVoteTarget(ownData.getDivineTarget());
//						// 占い先を投票，投票リクエスト
//						GeneralAction.sayVote(ownData, myTalking);
//						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getDivineTarget()));
//					}
//				}
				// ターン1 人狼CO
				if(turn.currentTurn(1)) {
					myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.WEREWOLF));
				}
				// ターン2 狂人COなしならばリクエスト
				if(turn.currentTurn(2)) {
					if(Check.isNotNull(talkDataBase.getComingoutRoleAgent(ownData.getTalkList(), Role.POSSESSED))) {
						/*盤面整理*/
						betaFlag = true;
					}else {
						List<Agent> aliveAgentList = ownData.getAliveAgentList();
						aliveAgentList.remove(ownData.getMe()); // 自分排除
						aliveAgentList.remove(forecastMap.getComingoutRoleAgentList(Role.POSSESSED).get(0)); // 狂人CO1人排除
						myTalking.addTalk(TalkFactory.voteRemark(aliveAgentList.get(0)));
					}
				}
				// ターン3
				if(turn.currentTurn(3)) {
					if(betaFlag) {
						List<Agent> coPoss = talkDataBase.getComingoutRoleAgent(TalkSelect.topicList(ownData.getTalkList(), Topic.COMINGOUT), Role.POSSESSED);
						if(!coPoss.isEmpty()) {
							List<Agent> aliveAgentList = ownData.getAliveOtherAgentList();
							if(!forecastMap.getComingoutRoleAgentList(Role.POSSESSED).isEmpty()) {
								// 狂人CO1人排除
								aliveAgentList.remove(forecastMap.getComingoutRoleAgentList(Role.POSSESSED).get(0));
							}
							myTalking.addTalk(TalkFactory.voteRemark(aliveAgentList.get(0)));
							betaFlag = false;
						}else {
							myTalking.addTalk(TalkFactory.skipRemark());
						}
					}
				}
				// ターン4 狂人COがいなければ自分の人狼COを撤回
				if(turn.currentTurn(4)) {
					List<Agent> coPoss = talkDataBase.getComingoutRoleAgent(TalkSelect.topicList(ownData.getTalkList(), Topic.COMINGOUT), Role.POSSESSED);
					if(coPoss.isEmpty()) {
						List<Talk> cotalkList = TalkSelect.topicList(ownData.getTalkList(), Topic.COMINGOUT);
						List<Talk> storeList = TalkSelect.speakerList(cotalkList, ownData.getMe());
						if(!storeList.isEmpty()) {
							myTalking.addTalk(TalkFactory.disagreeRemark(storeList.get(0)));
						}
						/*盤面整理*/
					}else {
						myTalking.addTalk(TalkFactory.skipRemark());
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
