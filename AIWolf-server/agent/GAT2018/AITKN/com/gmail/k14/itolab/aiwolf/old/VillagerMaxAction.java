package com.gmail.k14.itolab.aiwolf.old;

import java.util.List;

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
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;
import com.gmail.k14.itolab.aiwolf.util.TalkSelect;

/**
 * 村人の行動(15人)
 * @author k14096kk
 *
 */
public class VillagerMaxAction extends BaseRoleAction {
	
	/**予想対象*/
	Agent estimateTarget = null;
	/**リクエストに応えるかどうか*/
	boolean isResponseVote = false;

	public VillagerMaxAction(EntityData entityData) {
		super(entityData);
//		ownData.setStrategy(Strategy.FO);
		ownData.permitReaction();
		setEntityData();
	}
	
	@Override
	public void dayStart() {
		super.dayStart();
		if(ownData.afterDay(2)) {
			startProveRole();
		}
		// 変数初期化
		estimateTarget = null;
		isResponseVote = false;
		
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
		//  暫定役職割当て
		talkProveRole(talk, content);
		
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
	/**
	 * 日付変更時に暫定役職割当て
	 */
	public void startProveRole() {
		
		// 被襲撃者の暫定役職が決まっていない
		if(forecastMap.getProvRole(ownData.getAttackedAgent())==null) {
			forecastMap.setProvRole(ownData.getAttackedAgent(), Role.VILLAGER);
		}

		if(ownData.isFinish()) {
			// 残りの暫定役職
			List<Agent> agentList = forecastMap.getProvRoleAgentList(null);
			forecastMap.setRemainDoubtRole(ownData.getRoleNumMap(), agentList);
		}
	}
	
	/**
	 * 発言ごとの暫定役職設定
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public void talkProveRole(Talk talk, Content content) {
		
		if(Check.isTopic(content.getTopic(), Topic.DIVINED)) {
			// 自分を狼判定したら暫定狂人
			if(Check.isAgent(content.getTarget(), ownData.getMe())) {
				if(Check.isSpecies(content.getResult(), Species.WEREWOLF)) {
					forecastMap.setProvRole(talk.getAgent(), Role.POSSESSED);
				}
			}
			
			// 占い先の今までのJudgeを取得
			List<Judge> judgeList = forecastMap.getDivineJudgeTarget(content.getTarget());
			int humanCount = 0;
			// 白判定の結果をリスト化
			for(int i=0; i<judgeList.size(); i++) {
				if(judgeList.get(0).getResult()==Species.HUMAN) {
					humanCount++;
				}
			}
			
			// 占い師の人数分ならば確定白とする
			if(humanCount>=forecastMap.countComingoutRole(Role.SEER)) {
				forecastMap.minusDoubt(content.getTarget(), 1.5);
			}
		}
	}
	
	/**
	 * 村人基本行動
	 */
	public void normalAction() {
		
	}
	
	/**
	 * フルオープン行動
	 */
	public void fullOpen() {
		// 1日目
		if(ownData.currentDay(1)) {
			
			ownData.permitReaction();
			
			// 0ターン目
			if(turn.startTurn()) {
				// 占い師COリクエスト & 霊媒師COリクエスト
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.SEER));
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.MEDIUM));
			}
			// 2ターン目
			if(turn.currentTurn(2)) {
				/*観察*/
			}
			// 3ターン目
			if(turn.currentTurn(3)) {
				/*自由（占いCOした人の予想（真・狂・狼））*/
				// CO発言のリスト作成
				List<Talk> coTalks = TalkSelect.topicList(ownData.getTalkList(), Topic.COMINGOUT);
				// 占い師COの会話リスト
				List<Talk> seerTalks = TalkSelect.roleList(coTalks, Role.SEER);
				// 霊媒師CO発言の会話リスト
				List<Talk> mediumTalks = TalkSelect.roleList(coTalks, Role.MEDIUM);
				
				if(!seerTalks.isEmpty()) {
					/*ここで暫定役職決める感じ?*/
				}
				
				if(!mediumTalks.isEmpty()) {
					/*ここで暫定役職決める感じ?*/
				}	
				
			}
			// 4ターン目
			if(turn.currentTurn(4)) {
				/*自由（requestなどへの対応）*/
			}
			// 5ターン目
			if(turn.currentTurn(5)) {
				/*自由枠（request(Estimate(COエージェント,Role.狂人)))*/
			}
			// 6ターン目
			if(turn.currentTurn(6)) {
				/*自由枠（Estimate(COエージェント,Role.占い師))*/
			}
			// 7ターン目
			if(turn.currentTurn(7)) {
				// COしていないエージェントの中で最も疑い度が高いエージェントを占い先リクエスト
				List<Agent> agents = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
				agents.removeAll(forecastMap.getComingoutAgentList());
				if(!agents.isEmpty()) {
					estimateTarget = agents.get(0);
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(estimateTarget));
				}
				
			}
			// 8ターン目
			if(turn.currentTurn(8)) {
				/*request(Guard)*/
				ownData.rejectReaction();
			}
			// 9ターン目
			if(turn.currentTurn(9)) {
				// 投票対象の多数決確認
				GeneralAction.decideAvailVote(forecastMap, ownData, voteCounter);
				
				// 多数決でも決まっていなければ灰色に投票
				if(Check.isNull(ownData.getVoteTarget())) {
					// 疑い度が最大のエージェントに投票(占い対象以外)
					List<Agent> voteList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
					voteList.remove(estimateTarget);
					if(!voteList.isEmpty()) {
						ownData.setVoteTarget(voteList.get(0));
					}
				}
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);	
			}
 		}
		
		// 2日目以降
		if(ownData.afterDay(2)) {
			
			// 0ターン目
			if(turn.startTurn()) {
				
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
