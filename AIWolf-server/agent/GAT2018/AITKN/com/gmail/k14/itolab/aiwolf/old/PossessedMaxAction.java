package com.gmail.k14.itolab.aiwolf.old;

import java.util.ArrayList;
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
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 *  狂人の行動(15人)
 * @author k14096kk
 *
 */
public class PossessedMaxAction extends BaseRoleAction {
	
	List<Agent> divineList = new ArrayList<>();

	public PossessedMaxAction(EntityData entityData) {
		super(entityData);
//		ownData.setStrategy(Strategy.FAKESEER);
		ownData.rejectReaction();
		setEntityData();
	}
	
	@Override
	public void dayStart() {
		super.dayStart();
		
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		// 戦略に乗っ取った発言
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
	public void talkAction(Talk talk, Content content) {
		super.talkAction(talk, content);
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
	/**
	 * 占い師騙りの行動
	 */
	public void swindleSeerAction() {
	
		/*1日目*/
		if(ownData.currentDay(1)) {
			
			// 0ターン目　FO
			if(turn.startTurn()) {
				// 占い師COリクエスト & 霊媒師COリクエスト
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.SEER));
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.MEDIUM));
			}
			// 2ターン目
			if(turn.currentTurn(2)) {
				// 占い師CO
				GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
				// 疑い度が高い順にエージェントリスト取得(占い済みエージェントを除く)
				List<Agent> doubtList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
				doubtList.removeAll(divineList);
				// 疑い度最大のエージェントの占い結果は白
				if(!doubtList.isEmpty()) {
					GeneralAction.sayDivine(ownData, myTalking, doubtList.get(0), Species.HUMAN);
					divineList.add(doubtList.get(0));
				}
			}
			
			// 6ターン目
			if(turn.currentTurn(6)) {
				// 投票対象の多数決確認
				GeneralAction.decideAvailVote(forecastMap, ownData, voteCounter);
				// 多数決でも決まっていなければ灰色に投票
				if(Check.isNull(ownData.getVoteTarget())) {
					// 疑い度が最小のエージェントに投票(占い対象以外)
					List<Agent> voteList = forecastMap.fewerDoubtAgentList(ownData.getAliveOtherAgentList());
					voteList.removeAll(divineList);
					if(!voteList.isEmpty()) {
						ownData.setVoteTarget(voteList.get(0));
					}
				}
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);	
			}
			// 7ターン目
			if(turn.currentTurn(7)) {
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
			
		}
		
		/*2日目以降*/
		if(ownData.afterDay(2)) {
			// 0ターン目
			if(turn.startTurn()) {
				// 疑い度が高い順にエージェントリスト取得
				List<Agent> doubtList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
				doubtList.removeAll(divineList);
				// 疑い度最大のエージェントの占い結果は白
				if(!doubtList.isEmpty()) {
					GeneralAction.sayDivine(ownData, myTalking, doubtList.get(0), Species.HUMAN);
					divineList.add(doubtList.get(0));
				}
			}
			
			// 6ターン目
			if(turn.currentTurn(6)) {
				// 投票対象の多数決確認
				GeneralAction.decideAvailVote(forecastMap, ownData, voteCounter);
				// 多数決でも決まっていなければ灰色に投票
				if(Check.isNull(ownData.getVoteTarget())) {
					// 疑い度が最小のエージェントに投票(占い対象以外)
					List<Agent> voteList = forecastMap.fewerDoubtAgentList(ownData.getAliveOtherAgentList());
					voteList.removeAll(divineList);
					if(!voteList.isEmpty()) {
						ownData.setVoteTarget(voteList.get(0));
					}
				}
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);	
			}
			// 7ターン目
			if(turn.currentTurn(7)) {
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
		}
	}

}
