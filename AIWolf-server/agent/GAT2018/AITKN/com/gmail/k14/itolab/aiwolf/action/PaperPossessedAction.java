package com.gmail.k14.itolab.aiwolf.action;


import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 紙ベース人狼での狂人の行動
 * @author k14096kk
 *
 */
public class PaperPossessedAction extends BaseRoleAction{
	
	Agent seer = null;
	
	String actionTalk = null;
	
	boolean strategy = true;
	
	

	/**
	 * 狂人行動のコンストラクタ
	 * @param entityData :オブジェクトデータ
	 */
	public PaperPossessedAction(EntityData entityData) {
		super(entityData);
		/*ここに処理*/
		ownData.rejectReaction();
		
		if(RandomSelect.randomInt(1) == 0) {
			strategy = true;
		}else {
			strategy = false;
		}
		/**/
		setEntityData();
	}

	
	/**
	 * 1日のはじめ
	 */
	@Override
	public void dayStart() {
		super.dayStart();
		
		actionTalk = null;

		setEntityData();
	}

	
	/**
	 * 投票時に呼ぶ
	 */
	@Override
	public void vote() {
		super.vote();

		setEntityData();
	}
	
	
	/**
	 * 行動選択
	 */
	@Override
	public void selectAction() {
		super.selectAction();
		if(strategy) {
			this.nomalPossessedAciton();
		}else {
			
		}
		setEntityData();
	}

	
	/**
	 * 他人の発言に対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	@Override
	public void talkAction(Talk talk, Content content) {
		super.talkAction(talk, content);
		// 自分の発言
		if (talk.getAgent() == ownData.getMe()) {
			setEntityData();
			return;
		}
		
		// パターン2
		if(!strategy && turn.beforeTurn(2)) {
			// 便乗投票を行う(対象が自分の場合も)
			if(Check.isNull(ownData.getVoteTarget())) {
				if(Check.isTopic(content.getTopic(), Topic.VOTE)) {
					ownData.setVoteTarget(content.getTarget());
				}
			}
			return;
		}
		
		// パターン1
		if (Check.isTopic(content.getTopic(), Topic.COMINGOUT)) {
			// はじめに占い師COしたエージェントを占い師登録
			if(Check.isNull(seer) && Check.isRole(content.getRole(), Role.SEER)) {
				seer = talk.getAgent();
			}
		}
		
		if(Check.isNull(actionTalk)) {
			switch (content.getTopic()) {
			case VOTE:
				if(content.getTarget() == ownData.getVoteTarget()) {
					myTalking.addTalk(TalkFactory.agreeRemark(talk));
				}
				break;
			case ESTIMATE:
				if(content.getTarget() == ownData.getMe() && content.getRole() == Role.SEER) {
					myTalking.addTalk(TalkFactory.agreeRemark(talk));
				}
				break;
			default:
				break;
			}
		}
		
		setEntityData();
	}

	
	/**
	 * リクエストに対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 * @param reqContent :リクエストコンテンツ
	 */
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		// 自分の発言
		if (talk.getAgent() == ownData.getMe()) {
			setEntityData();
			return;
		}
		
		if(Check.isNull(actionTalk)) {
			// 占い先リクエスト
			if(Check.isTopic(content.getTopic(), Topic.DIVINATION)) {
				myTalking.addTalk(TalkFactory.divinationRemark(content.getTarget()));
			}
		}
		
			
		setEntityData();
	}
	
	public void addTalkMap(int id, String talk) {
		
	}

	
	/**
	 * 基本行動
	 */
	public void nomalPossessedAciton() {
		// 1日目
		if(Check.isNum(ownData.getDay(), 1)) {
			// 0ターン目占い師CO
			if(turn.currentTurn(0)) {
				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
			}
			
			// 占い結果と投票発言，投票リクエスト
			if(turn.currentTurn(1)) {
				List<Agent> targetList = new ArrayList<>(ownData.getAliveOtherAgentList());
				targetList.remove(seer);
				Agent target = RandomSelect.randomAgentSelect(targetList);
				ownData.setVoteTarget(target);
				myTalking.addTalk(TalkFactory.divinedResultRemark(ownData.getVoteTarget(), Species.WEREWOLF));
				myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
			
			if(turn.currentTurn(4)) {
				if(Check.isNotNull(actionTalk)) {
					myTalking.addTalk(TalkFactory.skipRemark());
				}else {
					myTalking.addTalk(actionTalk);
				}
			}
			
			if(turn.afterTurn(5)) {
				myTalking.addTalk(TalkFactory.overRemark());
			}
		}
		
		if(Check.isNum(ownData.getDay(), 2)) {
			// 狂人CO
			if(turn.currentTurn(0)) {
				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.POSSESSED));
			}
			
			if(turn.currentTurn(1)) {
				List<Agent> coWolfs = forecastMap.comingoutRoleAgentList(ownData.getAliveOtherAgentList(), Role.WEREWOLF);
				switch (coWolfs.size()) {
				case 0:
					myTalking.addTalk(TalkFactory.skipRemark());
					break;
				case 1:
					List<Agent> remainList = ownData.getAliveOtherAgentList();
					remainList.removeAll(coWolfs);
					ownData.setVoteTarget(HandyGadget.getListValue(remainList, 0));
					myTalking.addTalk(TalkFactory.voteRemark(HandyGadget.getListValue(remainList, 0)));
					break;
				case 2:
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getMe()));
					break;	
				default:
					break;
				}
			}
		}
	}
	
	public void wolfAction() {
		if(turn.currentTurn(0)) {
			myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.WEREWOLF));
			Agent target = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
			myTalking.addTalk(TalkFactory.attackRemark(target));
			myTalking.addTalk(TalkFactory.requestAllAttackRemark(target));
		}
		
		if(turn.currentTurn(3)) {
			if(Check.isNotNull(ownData.getVoteTarget())) {
				myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
			}else {
				myTalking.addTalk(TalkFactory.skipRemark());
			}
		}
		
		if(turn.currentTurn(4)) {
			myTalking.addTalk(TalkFactory.overRemark());
		}
	}
	
}