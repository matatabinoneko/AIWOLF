package com.gmail.k14.itolab.aiwolf.action;


import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.CoInfo;
import com.gmail.k14.itolab.aiwolf.util.Doubt;
import com.gmail.k14.itolab.aiwolf.util.OperatorElement;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;


/**
 * 共通の行動<br>
 * データの更新や登録，疑い度変動などが行われる<br>
 * 同意や反対などの全役職で共通での行動も管理する
 * @author k14096kk
 *
 */
public class CommonAction {
	
	EntityData entityData;
	
	/**会話*/
	Talk talk;
	/**コンテンツ*/
	Content content;
	
	/**
	 * 共通に行動
	 * @param entityData: オブジェクトデータ
	 */
	public CommonAction(EntityData entityData) {
		this.entityData = entityData;
	}
	
	/**
	 * ゲーム情報と自分の情報の更新
	 * @param entityData: オブジェクトデータ
	 */
	public void setDataUpdate(EntityData entityData) {
		this.entityData = entityData;
	}
	
	/**
	 * 発言に対する行動やデータ更新を一括で管理
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public void talkControl(Talk talk, Content content) {
		// 会話とコンテンツ更新
		this.talk = talk;
		this.content = content;
		
		// 一覧にデータ格納
		this.setComingoutAgent();
		this.setDivineResult();
		this.setIdentResult();
		this.setVoteCount(talk, content);
		
		// 発言による疑い度の変動
		Doubt.talkDoubt(entityData, talk, content);
		
		// 共通行動が却下状態ならば処理抜け
		if(!entityData.getOwnData().isReaction()) {
			return;
		}
		
		// 発言に対する共通行動呼び出し(全役職で共通の行動AGREEなど)
		this.talkAction(talk, content);
		
		// リクエストに対する共通行動呼び出し(全役職で共通の行動AGREEなど)
		if(OperatorElement.isRequest(content)) {
			this.requestAction(talk, content, OperatorElement.getRequestContent(content));
		}
	}
	
	/**
	 * 一日のはじめに呼ばれる共通処理
	 */
	public void dayStart() {
		this.setDeadInfo();
	}
	
	/**
	 * COしたエージェントのCO役職更新
	 */
	public void setComingoutAgent() {
		
		if(Check.isTopic(content.getTopic(), Topic.COMINGOUT)) {
			// CO情報の登録
			entityData.getForecastMap().setComingoutRole(talk, content);
			entityData.getOwnData().addCountCO(content.getRole());
			entityData.getCoHistory().add(talk.getAgent(), new CoInfo(talk, content));
			
			// COが自分ならば処理抜け
			if(Check.isAgent(talk.getAgent(), entityData.getOwnData().getMe())) {
				return;
			}
			
			// CO役職が自分と同じ
			if(Check.isRole(content.getRole(), entityData.getOwnData().getMyRole())) {
				// 自分が占い師ならば暫定狂人扱い
				if(Check.isRole(entityData.getOwnData().getMyRole(), Role.SEER)) {
					if(!entityData.getForecastMap().containProvRole(Role.POSSESSED)) {
						// 狂人がいなければ狂人割当て
						entityData.getForecastMap().setProvRole(talk.getAgent(), Role.POSSESSED);
					}else {
						// 狂人が存在していれば人狼割当て
						entityData.getForecastMap().setProvRole(talk.getAgent(), Role.WEREWOLF);
					}
				}
				// 自分が霊媒師ならば暫定人狼
				if(Check.isRole(entityData.getOwnData().getMyRole(), Role.MEDIUM)) {
					entityData.getForecastMap().setProvRole(talk.getAgent(), Role.WEREWOLF);
				}
				
			}else { // 自分の役職と異なるならば暫定役職にも入れる
				// 1日目は先着順
				if(entityData.getOwnData().currentDay(1)) {
					
					// 自分が人狼かつ5人村ならreturn
					if(Check.isRole(entityData.getOwnData().getMyRole(), Role.WEREWOLF)) {
						if(entityData.getOwnData().getGameSetting().getPlayerNum()<=7) {
							return;
						}
					}
					
					if(!entityData.getForecastMap().containProvRole(content.getRole())) {
						// CO役職が暫定に存在しないならば割当て
						entityData.getForecastMap().setProvRole(talk.getAgent(), content.getRole());
					}else if(!entityData.getForecastMap().containProvRole(Role.POSSESSED)) {
						// CO役職が存在していて，狂人がいなければ狂人割当て
						entityData.getForecastMap().setProvRole(talk.getAgent(), Role.POSSESSED);
					}else {
						// CO役職と狂人が存在していれば人狼割当て
						entityData.getForecastMap().setProvRole(talk.getAgent(), Role.WEREWOLF);
					}
				}else {
					entityData.getForecastMap().setProvRole(talk.getAgent(), content.getRole());
				}
			}
		}
	}
	
	/***
	 * 占い判定を出したエージェントと結果を更新
	 */
	public void setDivineResult() {
		if(Check.isTopic(content.getTopic(), Topic.DIVINED)) {
			entityData.getForecastMap().setDivineJudgeList(talk, content);
		}
	}
	
	/***
	 * 霊媒判定を出したエージェントと結果を更新
	 */
	public void setIdentResult() {
		if(Check.isTopic(content.getTopic(), Topic.IDENTIFIED)) {
			entityData.getForecastMap().setIdentJudgeList(talk, content);
		}
	}
	
	/**
	 * 死亡情報の更新
	 */
	public void setDeadInfo() {
		entityData.getForecastMap().setAttackedStatus(entityData.getOwnData().getAttackedAgent());
		entityData.getForecastMap().setExecutedStatus(entityData.getOwnData().getLatestExecutedAgent());
	}
	
	/**
	 * 投票とリクエスト回数を格納
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public void setVoteCount(Talk talk, Content content) {
		// 投票リクエスト回数更新
		if(OperatorElement.isRequest(content)) {
			if(Check.isTopic(OperatorElement.getTopic(content), Topic.VOTE)) {
				if(entityData.getOwnData().getAliveAgentList().contains(OperatorElement.getTarget(content))) {
					entityData.getVoteCounter().addRequest(OperatorElement.getTarget(content));
				}
			}
		}
		// 投票回数更新
		if(Check.isTopic(content.getTopic(), Topic.VOTE)) {
			// 投票対象が生存していればカウントする
			if(entityData.getOwnData().getAliveAgentList().contains(content.getTarget())) {
				entityData.getVoteCounter().addRemark(content.getTarget());
			}
		}
	}
	
	/**
	 * 他人の発言に対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public void talkAction(Talk talk, Content content) {
		// 発話者が自分ならば抜ける
		if(Check.isAgent(talk.getAgent(), entityData.getOwnData().getMe())) {
			return;
		}
		
		// 占い先
		if(Check.isTopic(content.getTopic(), Topic.DIVINATION)) {
			// 自分が対象ならば反対
			if(Check.isAgent(content.getTarget(), entityData.getOwnData().getMe())) {
				entityData.getSubTalking().addTalk(TalkFactory.disagreeRemark(talk));
			}
		}

		// 予想
		if(Check.isTopic(content.getTopic(), Topic.ESTIMATE)) {
			// 暫定役職と同じならば同意
			if(Check.isRole(content.getRole(),entityData.getForecastMap().getProvRole(content.getTarget()))) {
				entityData.getSubTalking().addTalk(TalkFactory.agreeRemark(talk));
			}

			// 疑い度が最小のエージェントへ人狼予想は反対
			List<Agent> doubtList = entityData.getForecastMap().fewerDoubtAgentList(entityData.getOwnData().getAliveOtherAgentList());
			if(!doubtList.isEmpty()) {
				if(Check.isAgent(content.getTarget(), doubtList.get(0))) {
					if(Check.isRole(content.getRole(), Role.WEREWOLF)) {
						entityData.getSubTalking().addTalk(TalkFactory.disagreeRemark(talk));
					}
				}
			}

			// 自分を人狼予想しているならば反対
			if(Check.isAgent(content.getTarget(), entityData.getOwnData().getMe())) {
				if(Check.isRole(content.getRole(), Role.WEREWOLF)) {
					entityData.getSubTalking().addTalk(TalkFactory.disagreeRemark(talk));
				}
			}
		}

		// 投票
		if(Check.isTopic(content.getTopic(), Topic.VOTE)) {
			// 疑い度降順のエージェントリスト
			List<Agent> doubtList = entityData.getForecastMap().moreDoubtAgentList(entityData.getOwnData().getAliveOtherAgentList());
			if(!doubtList.isEmpty()) {
				// 疑い度最大のエージェントor自分の投票対象が対象ならば同意
				if(Check.isAgent(content.getTarget(), doubtList.get(0)) || Check.isAgent(content.getTarget(), entityData.getOwnData().getVoteTarget())) {
					entityData.getSubTalking().addTalk(TalkFactory.agreeRemark(talk));
				}
			}
		}

		// 占い結果
		if(Check.isTopic(content.getTopic(), Topic.DIVINED)) {
			// 確定占い師の占い結果ならば同意
			List<Agent> seerList = entityData.getForecastMap().getConfirmRoleAgentList(Role.SEER);
			if(!seerList.isEmpty()) {
				if(seerList.contains(talk.getAgent())) {
					entityData.getSubTalking().addTalk(TalkFactory.agreeRemark(talk));
				}
			}
		}
	}
	
	/**
	 * リクエストに対する共通の行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 * @param reqContent :リクエストコンテンツ
	 */
	public void requestAction(Talk talk, Content content, Content reqContent) {
		
		// 発話者が自分ならば抜ける
		if(Check.isAgent(talk.getAgent(), entityData.getOwnData().getMe())) {
			return;
		}

		// 予想リクエスト
		if(Check.isTopic(reqContent.getTopic(), Topic.ESTIMATE)) {
			// 暫定役職と同じならば同意
			if (Check.isRole(reqContent.getRole(),entityData.getForecastMap().getProvRole(reqContent.getTarget()))) {
				entityData.getSubTalking().addTalk(TalkFactory.agreeRemark(talk));
			}
		}

		// 投票リクエスト
		if(Check.isTopic(reqContent.getTopic(), Topic.VOTE)) {
			// 疑い度降順のエージェントリスト
			List<Agent> doubtList = entityData.getForecastMap().moreDoubtAgentList(entityData.getOwnData().getAliveOtherAgentList());
			if(!doubtList.isEmpty()) {
				// 疑い度最大のエージェントor自分の投票対象が対象ならば同意
				if(Check.isAgent(reqContent.getTarget(), doubtList.get(0)) || Check.isAgent(reqContent.getTarget(), entityData.getOwnData().getVoteTarget())) {
					entityData.getSubTalking().addTalk(TalkFactory.agreeRemark(talk));
				}
			}
		}
	}

}
