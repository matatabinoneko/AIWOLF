package com.gmail.k14.itolab.aiwolf.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
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
import com.gmail.k14.itolab.aiwolf.util.OperatorElement;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;
import com.gmail.k14.itolab.aiwolf.util.TalkSelect;

/**
 * 占い師の行動(15人)
 * @author k14096kk
 *
 */
public class SeerMaxAction extends BaseRoleAction {
	
	/**占い先リスクエストのマップ (占い先：リクエストされた回数)*/
	public Map<Agent, Integer> requestTargetList;
	/**リクエストに答えたかどうか*/
	public boolean isResponse;
	
	/**予想対象*/
	Agent estimateTarget = null;

	public SeerMaxAction(EntityData entityData) {
		super(entityData);
//		ownData.setStrategy(Strategy.FO);
		ownData.rejectReaction();
		setEntityData();
	}
	
	@Override
	public void dayStart() {
		super.dayStart();
		// 占い結果が存在すれば
		if(Check.isNotNull(ownData.getGameInfo().getDivineResult())) {
			// 占い結果を保持
			ownData.setDivineResultMap(ownData.getGameInfo().getDivineResult());
			// 占いが人間判定ならば疑い度減少，狼ならば疑い度上昇
			if(Check.isSpecies(ownData.getGameInfo().getDivineResult().getResult(), Species.HUMAN)) {
				forecastMap.minusDoubt(ownData.getGameInfo().getDivineResult().getTarget(), 2);
			}else {
				forecastMap.plusDoubt(ownData.getGameInfo().getDivineResult().getTarget(), 2);
				// 暫定と確定役職に人狼
				forecastMap.setProvRole(ownData.getGameInfo().getDivineResult().getTarget(), Role.WEREWOLF);
				forecastMap.setConfirmRole(ownData.getGameInfo().getDivineResult().getTarget(), Role.WEREWOLF);
			}
		}
		
		// データ初期化
		requestTargetList = new HashMap<>();
		isResponse = false;
		
		setEntityData();
	}
	
	@Override
	public void divine() {
		super.divine();
		// 占い先が決まっていないならばランダム
		if(Check.isNull(ownData.getDivineTarget())) {
			List<Agent> targetList = ownData.remainDivineAgentList(ownData.getAliveAgentList());
			ownData.setDivineTarget(RandomSelect.randomAgentSelect(targetList, ownData.getMe()));
		}
		
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		switch (ownData.getStrategy()) {
		case FO:
			this.fullOpen();
			break;
		default:
			this.normalAction();
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
		// 占い先リクエスト
		if(Check.isTopic(reqContent.getTopic(), Topic.DIVINATION)) {
			if(ownData.isCO()) {
				// リクエスト対象が自分or全員ならば
				if(Check.isAgent(content.getTarget(), ownData.getMe()) || Check.isNull(content.getTarget())) {
					/*リクエスト先を格納して回数を確認する処理*/
					int requestCount = 1;
					if(requestTargetList.containsKey(reqContent.getTopic())) {
						requestCount += requestTargetList.get(reqContent.getTarget());
					}
					// リクエスト回数更新
					requestTargetList.put(reqContent.getTarget(), requestCount);
					// 3人以上からリクエストされていれば次回の占い対象(リクエスト未解答状態)
					if(!isResponse && requestTargetList.get(reqContent.getTarget())>=3) {
						myTalking.addTalk(TalkFactory.divinationRemark(reqContent.getTarget()));
						ownData.setDivineTarget(reqContent.getTarget());
						isResponse = true;
					}
				}
			}
		}
		
		setEntityData();
	}
	
	/**
	 * 通常行動
	 */
	public void normalAction() {
		// 1日目
		if(ownData.currentDay(1)) {
			// 占い師COして，
			if(turn.startTurn()) {
//				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
//				Judge judge = ownData.getDivineResultJudge(ownData.getDay());
//				myTalking.addTalk(TalkFactory.divinedResultRemark(judge.getTarget(), judge.getResult()));
			}
		}
		
		// 2日目以降
		if(ownData.afterDay(2)) {
			
		}
	}
	
	/*村人の行動fullOpenからコピペ*/
	/**
	 * フルオープン行動
	 */
	public void fullOpen() {
		// 1日目
		if(ownData.currentDay(1)) {
			// 0ターン目
			if(turn.startTurn()) {
				// 占い師COリクエスト & 霊媒師COリクエスト
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.SEER));
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.MEDIUM));
			}
			// 4ターン目
			if(turn.currentTurn(4)) {
				if(!ownData.isCO()) {
					int count = 0;
					
					// 占い師COリクエストのリスト
					List<Talk> requestCoList = new ArrayList<>();
					
					// 占い師COのリクエストがあればカウント増加
					for(Talk talk: TalkSelect.requestList(ownData.getTalkList())) {
						Content content = new Content(talk.getText());
						if(Check.isTopic(OperatorElement.getTopic(content), Topic.COMINGOUT)) {
							if(Check.isRole(OperatorElement.getRole(content), Role.SEER)) {
								count++;
								requestCoList.add(talk);
							}
						}
					}
					// 同意先の発言がCOリクエストならばカウント増加
					for(Talk talk: TalkSelect.topicList(ownData.getTalkList(), Topic.AGREE)) {
						Content content = new Content(talk.getText());
						// 同意先の発話日付が本日
						if(Check.isNum(content.getTalkDay(), ownData.getDay())) {
							// 同意先の発話取得
							Talk targetTalk = ownData.getTalkList().get(content.getTalkID());
							// 同意先の発言がCOリクエストならばカウント増加
							if(requestCoList.contains(targetTalk)) {
								count++;
							}
						}
					}
					// 半数以上がFO賛成ならばCO&結果報告
					if(count>=(ownData.getAliveAgentList().size()/2)) {
						GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
						Judge judge = ownData.getDivineResultJudge(ownData.getDay());
						myTalking.addTalk(TalkFactory.divinedResultRemark(judge.getTarget(), judge.getResult()));
					}else {
						// 対抗COする際の処理
						List<Talk> coList = TalkSelect.topicList(ownData.getTalkList(), Topic.COMINGOUT);
						List<Talk> seerCoList = TalkSelect.roleList(coList, Role.SEER);
						// 対抗CO&結果報告
						if(!seerCoList.isEmpty()) {
							GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
							Judge judge = ownData.getDivineResultJudge(ownData.getDay());
							myTalking.addTalk(TalkFactory.divinedResultRemark(judge.getTarget(), judge.getResult()));
						}
					}
				}
			}
			// 5ターン目
			if(turn.currentTurn(5)) {
				if(ownData.isCO()) {
					// 占い対象が決まっていない(リクエストがない)ならば疑い度が最大のエージェントを占い先に
					if(Check.isNull(ownData.getDivineTarget())) {
						// COしていないエージェントの中で最も疑い度が高いエージェントを占い先
						List<Agent> agents = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
						agents.removeAll(forecastMap.getComingoutAgentList());
						if(!agents.isEmpty()) {
							isResponse = true; // 占いリクエストはもう受け付けない
							ownData.setVoteTarget(agents.get(0));
						}
					}
					// 占い先を発言
					myTalking.addTalk(TalkFactory.divinationRemark(ownData.getDivineTarget()));
				}
			}
			// 6ターン目
			if(turn.currentTurn(6)) {
				
			}
			// 7ターン目
			if(turn.currentTurn(7)) {
				// 投票対象の多数決確認
				GeneralAction.decideAvailVote(forecastMap, ownData, voteCounter);
				// 多数決でも決まっていなければ灰色に投票
				if(Check.isNull(ownData.getVoteTarget())) {
					// 疑い度が最大のエージェントに投票(占い対象以外)
					List<Agent> voteList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
					voteList.remove(ownData.getDivineTarget());
					if(!voteList.isEmpty()) {
						ownData.setVoteTarget(voteList.get(0));
					}
				}
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);	
			}
			// 8ターン目
			if(turn.currentTurn(8)) {
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
 		}
		
		// 2日目以降
		if(ownData.afterDay(2)) {
			
			// 0ターン目
			if(turn.startTurn()) {
				if(ownData.isCO()) {
					Judge judge = ownData.getDivineResultJudge(ownData.getDay());
					myTalking.addTalk(TalkFactory.divinedResultRemark(judge.getTarget(), judge.getResult()));
				}
			}
			// 2ターン目
			if(turn.currentTurn(2)) {
				// 占い結果と疑い度を元に人狼予想して，予想先を保持
				estimateTarget = GeneralAction.sayEstimateWolf(forecastMap, ownData, myTalking);
			}
			// 4ターン目
			if(turn.currentTurn(4)) {
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
				if(Check.isNull(ownData.getVoteTarget())) {
					ownData.setVoteTarget(estimateTarget);
				}
				
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);

				// 疑い度最大(投票対象を除く)のエージェントを占い対象リクエスト
				GeneralAction.sayDoubtRequestDivine(forecastMap, ownData, myTalking, ownData.getVoteTarget());
			}
		}
	}

}
