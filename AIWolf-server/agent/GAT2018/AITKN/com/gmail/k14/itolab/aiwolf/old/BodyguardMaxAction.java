package com.gmail.k14.itolab.aiwolf.old;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.action.GeneralAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;
import com.gmail.k14.itolab.aiwolf.util.TalkSelect;

/**
 * 狩人の行動(15人)
 * @author k14096kk
 *
 */
public class BodyguardMaxAction extends BaseRoleAction{
	
	public BodyguardMaxAction(EntityData entityData) {
		super(entityData);
		setEntityData();
	}
	
	@Override
	public void dayStart() {
		super.dayStart();
		
		setEntityData();
	}
	
	@Override
	public void guard() {
		super.guard();
		//  護衛対象が決まっていなければ決定処理をさせる
		if(Check.isNull(ownData.getGuardTarget())) {
			this.decideGuardTarget();
		}
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		normalAction();
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
	 * 護衛対象を決める
	 */
	public void decideGuardTarget() {
		/*占い師が確定役職にいるならば占い師を護衛(生存していれば)*/
		if(forecastMap.containConfirmRole(Role.SEER)) {
			Agent target = forecastMap.getConfirmRoleAgentList(Role.SEER).get(0);
			if(Check.isStatus(forecastMap.getStatus(target), Status.ALIVE)) {
				ownData.setGuardTarget(target);
				return;
			}
		}
		
		/*霊媒師が確定役職にいるならば霊媒師を護衛(生存していてば)*/
		if(forecastMap.containConfirmRole(Role.MEDIUM)) {
			Agent target = forecastMap.getConfirmRoleAgentList(Role.MEDIUM).get(0);
			if(Check.isStatus(forecastMap.getStatus(target), Status.ALIVE)) {
				ownData.setGuardTarget(target);
				return;
			}
		}
		
		/*占い師の中で黒判定を出したエージェント*/
		// 黒判定の占い結果リストを取得
		List<Judge> judgeList = forecastMap.getDivineJudgeSpecies(Species.WEREWOLF);
		if(!judgeList.isEmpty()) {
			List<Agent> seerList = new ArrayList<>();
			// 黒判定を出したエージェントの中で生存エージェントのみに限定
			for(Judge judge: judgeList) {
				Agent seer = judge.getAgent();
				if(Check.isStatus(forecastMap.getStatus(seer), Status.ALIVE)) {
					seerList.add(seer);
				}
			}
			// 占い師の中から疑い度が低い順に並び替えて一番最低のエージェントを護衛対象
			if(!seerList.isEmpty()) {
				seerList = forecastMap.fewerDoubtAgentList(seerList);
				ownData.setGuardTarget(seerList.get(0));
				return;
			}
		}
		
		/*占い師の中で自分に白判定を出したエージェント*/
		// 自分を対象とした占い結果を取得
		List<Judge> meJudges = forecastMap.getDivineJudgeTarget(ownData.getMe());
		if(!meJudges.isEmpty()) {
			// 自分を白判定したエージェントをリスト化
			List<Agent> seerList = new ArrayList<>();
			for(Judge judge: meJudges) {
				if(Check.isSpecies(judge.getResult(), Species.HUMAN)) {
					Agent seer = judge.getAgent();
					if(Check.isStatus(forecastMap.getStatus(seer), Status.ALIVE)) {
						seerList.add(seer);
					}
				}
			}
			// 占い師の中から疑い度が低い順に並び替えて一番最低のエージェントを護衛対象
			if(!seerList.isEmpty()) {
				seerList = forecastMap.fewerDoubtAgentList(seerList);
				ownData.setGuardTarget(seerList.get(0));
				return;
			}
		}
		
		/*疑い度が最低の暫定占い師を護衛対象*/
		// 暫定占い師をリスト化
		List<Agent> seerList = forecastMap.getProvRoleAgentList(Role.SEER);
		if(!seerList.isEmpty()) {
			seerList = forecastMap.fewerDoubtAgentList(seerList);
			ownData.setGuardTarget(seerList.get(0));
			return;
		}
		
		// 上記の条件に適しなければ疑い度が最も小さいエージェント
		ownData.setGuardTarget(forecastMap.fewerDoubtAgentList(ownData.getAliveOtherAgentList()).get(0));
		
	}
	
	/**
	 * 通常行動
	 */
	public void normalAction() {
		
		/*1日目*/
		if(ownData.currentDay(1)) {
			// 0ターン目
			if(turn.startTurn()) {
				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.MEDIUM));
			}
			// 2ターン目
			if(turn.currentTurn(2)) {
				List<Talk> coList = TalkSelect.topicList(ownData.getTalkList(), Topic.COMINGOUT);
				if(!coList.isEmpty()) {
					// 占い師COと霊媒師COがいるか確認
					List<Talk> seerTalks = TalkSelect.roleList(coList, Role.SEER);
					List<Talk> mediumTalks = TalkSelect.roleList(coList, Role.MEDIUM);
					if(!seerTalks.isEmpty() && !mediumTalks.isEmpty()) {
						/*SKIP*/
					}
				}
			}
			// 3ターン目
			if(turn.currentTurn(3)) {
				// COしていないエージェントの中で最も疑い度が高いエージェントを占い先リクエスト
				List<Agent> agents = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
				agents.removeAll(forecastMap.getComingoutAgentList());
				if(!agents.isEmpty()) {
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(agents.get(0)));
				}
			}
			// 4ターン目
			if(turn.currentTurn(4)) {
				// 護衛対象を決めて，護衛リクエスト
				this.decideGuardTarget();
				myTalking.addTalk(TalkFactory.requestAllGuardRemark(ownData.getGuardTarget()));
			}
			// 5ターン目
			if(turn.currentTurn(5)) {
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
                // 投票発言
                GeneralAction.sayVote(ownData, myTalking);
			}
			// 6ターン目
			if(turn.currentTurn(6)) {
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
			
			if(turn.currentTurn(8)) {
				ownData.setActFlagFinish();
			} 
		}
		
		/*2日目*/
		if(ownData.currentDay(2)) {
			// 1ターン目
			if(turn.currentTurn(1)) {
				// 占い師COしたエージェントの疑い度低い順のリスト
				List<Agent> coSeerList = forecastMap.comingoutRoleAgentList(ownData.getAliveOtherAgentList(), Role.SEER);
				List<Agent> seerList = forecastMap.fewerDoubtAgentList(coSeerList);
				// 占い師予想
				if(!seerList.isEmpty()) {
					myTalking.addTalk(TalkFactory.estimateRemark(seerList.get(0), Role.SEER));
				}
			}
			// 3ターン目
			if(turn.currentTurn(3)) {
				// 人狼予想発言のリスト
				List<Talk> estimateList = TalkSelect.topicList(ownData.getTalkList(), Topic.ESTIMATE);
				List<Talk> wolfTalkList = TalkSelect.roleList(estimateList, Role.WEREWOLF);
				// 予想先エージェントを確認
				if(!wolfTalkList.isEmpty()) {
					for(Talk talk: wolfTalkList) {
						Agent target = new Content(talk.getText()).getTarget();
						// 予想先エージェントの疑い度が0以下かつ話し手が1回以下しか話していないならば人狼予想
						if(forecastMap.getDoubt(target)<=0) {
							if(ownData.getGameInfo().getRemainTalkMap().get(talk.getAgent())>=9) {
								myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
								break;
							}
						}
					}
				}
			}
			// 4ターン目
			if(turn.currentTurn(4)) {
				// 灰色エージェントリスト
				List<Agent> grayList = ownData.getAliveOtherAgentList();
				grayList.remove(forecastMap.comingoutAliveAgentList());
				grayList = forecastMap.moreDoubtAgentList(grayList);
				// 疑い度が最大の灰色エージェントを占い先リクエスト
				if(!grayList.isEmpty()) {
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(grayList.get(0)));
				}
			}
			// 5ターン目
			if(turn.currentTurn(5)) {
				// 護衛リクエスト
				this.decideGuardTarget();
				myTalking.addTalk(TalkFactory.requestAllGuardRemark(ownData.getGuardTarget()));
			}
			// 6ターン目
			if(turn.currentTurn(6)) {
				// 投票便乗処理
				GeneralAction.decideAvailVote(forecastMap, ownData, voteCounter);
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);
			}
			// 7ターン目
			if(turn.currentTurn(7)) {
				// 投票リクエスト
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
		}
		
		/*3日目以降*/
		if(ownData.afterDay(3)) {
			// 1ターン目
			if(turn.currentTurn(1)) {
				// 確定占い師がいれば何もなし
				if(forecastMap.containConfirmRole(Role.SEER)) {
					return;
				}
				// 占い師COしたエージェントの疑い度低い順のリスト
				List<Agent> coSeerList = forecastMap.comingoutRoleAgentList(ownData.getAliveOtherAgentList(), Role.SEER);
				List<Agent> seerList = forecastMap.fewerDoubtAgentList(coSeerList);
				// 占い師予想
				if(!seerList.isEmpty()) {
					myTalking.addTalk(TalkFactory.estimateRemark(seerList.get(0), Role.SEER));
				}
			}
			// 2ターン目
			if(turn.currentTurn(2)) {
				
			}
			// 3ターン目
			if(turn.currentTurn(3)) {
				// 人狼予想発言のリスト
				List<Talk> estimateList = TalkSelect.topicList(ownData.getTalkList(), Topic.ESTIMATE);
				List<Talk> wolfTalkList = TalkSelect.roleList(estimateList, Role.WEREWOLF);
				// 予想先エージェントを確認
				if(!wolfTalkList.isEmpty()) {
					for(Talk talk: wolfTalkList) {
						Agent target = new Content(talk.getText()).getTarget();
						// 予想先エージェントの疑い度が0以下かつ話し手が1回以下しか話していないならば人狼予想
						if(forecastMap.getDoubt(target)<=0) {
							if(ownData.getGameInfo().getRemainTalkMap().get(talk.getAgent())>=9) {
								myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
								break;
							}
						}
					}
				}
			}
			// 4ターン目
			if(turn.currentTurn(4)) {
				// 灰色エージェントリスト
				List<Agent> grayList = ownData.getAliveOtherAgentList();
				grayList.remove(forecastMap.comingoutAliveAgentList());
				grayList = forecastMap.moreDoubtAgentList(grayList);
				// 疑い度が最大の灰色エージェントを占い先リクエスト
				if(!grayList.isEmpty()) {
					myTalking.addTalk(TalkFactory.requestAllDivinationRemark(grayList.get(0)));
				}
			}
			// 5ターン目
			if(turn.currentTurn(5)) {
				// 護衛リクエスト
				this.decideGuardTarget();
				myTalking.addTalk(TalkFactory.requestAllGuardRemark(ownData.getGuardTarget()));
			}
			// 6ターン目
			if(turn.currentTurn(6)) {
				// 投票便乗処理
				GeneralAction.decideAvailVote(forecastMap, ownData, voteCounter);
				// 投票発言
				GeneralAction.sayVote(ownData, myTalking);
			}
			// 7ターン目
			if(turn.currentTurn(7)) {
				// 投票リクエスト
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
		}
 		
		
		this.finishTalk();
	}

}
