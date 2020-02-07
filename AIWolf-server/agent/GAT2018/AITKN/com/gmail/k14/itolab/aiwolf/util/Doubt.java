package com.gmail.k14.itolab.aiwolf.util;

import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.data.EntityData;

/**
 * 疑い度の変動を行うクラス<br>
 * 暫定役職の設定などは行わないつもり<br>
 * @author k14096kk
 *
 */
public class Doubt {
	
	/**
	 * 発話に対する疑い度変動させる
	 * @param entityData　:インスタンスデータ
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public static void talkDoubt(EntityData entityData, Talk talk, Content content) {
		
		// 自分のTalkListに含まれている内容を発言したエージェント
		if(entityData.getMyTalking().containTalk(talk.getText())) {
			entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.5);
		}
		
		switch (content.getTopic()) {
		case ESTIMATE:
			changeEstimate(entityData, talk, content);
			break;
		case COMINGOUT:
			changeComingout(entityData, talk, content);
			break;
		case VOTE:
			changeVote(entityData, talk, content);
			break;
		case DIVINATION:
			changeDivination(entityData, talk, content);
			break;
		case DIVINED:
			changeDivined(entityData, talk, content);
			break;
		case IDENTIFIED:
			changeIdentified(entityData, talk, content);
			break;
		case GUARD:
			changeGuard(entityData, talk, content);
			break;
		case GUARDED:
			changeGuarded(entityData, talk, content);
			break;
		case AGREE:
			changeAgree(entityData, talk, content);
			break;
		case DISAGREE:
			changeDisagree(entityData, talk, content);
			break;
		case OPERATOR:
			changeRequest(entityData, talk, content);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 予想発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeEstimate(EntityData entityData, Talk talk, Content content) {
		// 自分のことを人狼だと予想するエージェント
		if(Check.isRole(content.getRole(), Role.WEREWOLF)) {
			entityData.getForecastMap().plusDoubt(talk.getAgent(), 0.5);
		}
	}
	
	/**
	 * CO発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeComingout(EntityData entityData, Talk talk, Content content) {
		// 村人COしたエージェント
		if(Check.isRole(content.getRole(), Role.VILLAGER)) {
			entityData.getForecastMap().plusDoubt(talk.getAgent(), 0.5);
		}
		
		// CO役職が自分と同じ(自分が話し手でない)
		if(!Check.isAgent(talk.getAgent(), entityData.getForecastMap().getMe()) && Check.isRole(content.getRole(), entityData.getForecastMap().getMyRole())) {
			// 自分が占い師，霊媒師ならば暫定狂人扱い
			if(Check.isRole(entityData.getForecastMap().getMyRole(), Role.SEER) || Check.isRole(entityData.getForecastMap().getMyRole(), Role.MEDIUM)) {
				entityData.getForecastMap().plusDoubt(talk.getAgent(), 2);
			}else if(Check.isRole(entityData.getForecastMap().getMyRole(), Role.BODYGUARD)) { // 狩人ならば疑い度変動
				entityData.getForecastMap().plusDoubt(talk.getAgent(), 1);
			}
		}
	}
	
	/**
	 * 投票発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeVote(EntityData entityData, Talk talk, Content content) {
		// 自分が暫定狼だとしているエージェントをVote対象にしたエージェント
		if(Check.isRole(entityData.getForecastMap().getProvRole(content.getTarget()), Role.WEREWOLF)) {
			entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.5);
		}
	}
	
	/**
	 * 占い先発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeDivination(EntityData entityData, Talk talk, Content content) {
		
	}
	
	/**
	 * 占い結果発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeDivined(EntityData entityData, Talk talk, Content content) {
		// 占い対象が自分
		if(Check.isAgent(content.getTarget(), entityData.getForecastMap().getMe())) {
			// 結果が狼
			if (Check.isSpecies(content.getResult(), Species.WEREWOLF)) {
				// 自分が狼ならば減少，以外ならば増加
				if (Check.isRole(entityData.getForecastMap().getMyRole(), Role.WEREWOLF)) {
					entityData.getForecastMap().minusDoubt(talk.getAgent(), 1);
				} else {
					entityData.getForecastMap().plusDoubt(talk.getAgent(), 1);
				}
			}
		}else { // 対象が自分以外
			// 狼ならば対象を上昇，人間ならば対象を減少
			if(Check.isSpecies(content.getResult(), Species.WEREWOLF)) {
				entityData.getForecastMap().plusDoubt(content.getTarget(), 0.5);
			}else {
				entityData.getForecastMap().minusDoubt(content.getTarget(), 0.1);
			}
		}
		
	}
	
	/**
	 * 霊媒結果発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeIdentified(EntityData entityData, Talk talk, Content content) {
		

		// 霊媒対象を占った結果をリスト化
		List<Judge> judgeList = entityData.getForecastMap().getDivineJudgeTarget(content.getTarget());
		
		// 霊媒結果が黒
		if(Check.isSpecies(content.getResult(), Species.WEREWOLF)) {
			
			for(Judge judge: judgeList) {
				// 占い結果が黒だった(霊媒と占いの結果が同じ)
				if(Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
					entityData.getForecastMap().plusDoubt(judge.getTarget(), 1);   // 死亡者
					entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.5);  // 霊媒師
					entityData.getForecastMap().minusDoubt(judge.getAgent(), 0.5); // 占い師
					
				}else { //占い結果が白だった(霊媒と占いの結果が異なる)
					entityData.getForecastMap().minusDoubt(judge.getTarget(), 1); // 死亡者
					entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.2); // 霊媒師
					entityData.getForecastMap().plusDoubt(judge.getAgent(), 0.2); // 占い師
				}
			}
			
		}else { // 霊媒結果が白
			
			for(Judge judge: judgeList) {
				// 占い結果が黒だった(霊媒と占いの結果が異なる)
				if(Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
					entityData.getForecastMap().minusDoubt(judge.getTarget(), 1); // 死亡者
					entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.2); // 霊媒師
					entityData.getForecastMap().plusDoubt(judge.getAgent(), 0.2); // 占い師		
				}else { //占い結果が白だった(霊媒と占いの結果が同じ)
					entityData.getForecastMap().minusDoubt(judge.getTarget(), 1);  // 死亡者
					entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.5);  // 霊媒師
					entityData.getForecastMap().minusDoubt(judge.getAgent(), 0.5); //占い師
				}
			}
		}
	}
	
	/**
	 * 護衛先発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeGuard(EntityData entityData, Talk talk, Content content) {
		
	}
	
	/**
	 * 護衛結果発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeGuarded(EntityData entityData, Talk talk, Content content) {
		
	}
	
	/**
	 * 同意発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeAgree(EntityData entityData, Talk talk, Content content) {
		// 同意先の発言取得
		Talk toTalk = entityData.getTalkDataBase().getToTalk(content.getTalkDay(), content.getTalkID());
		
		// 自分の発言ならば減少
		if(Check.isNotNull(toTalk) && Check.isAgent(toTalk.getAgent(), entityData.getForecastMap().getMe())) {
			entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.2);
		}
		// 自分の反対先発言への同意発言がある場合
		if(entityData.getMyTalking().containTalk(TalkFactory.disagreeRemark(toTalk))) {
			entityData.getForecastMap().plusDoubt(talk.getAgent(), 0.2);
		}
		// 自分が暫定狼だとしているエージェントの発言にagreeしたエージェント
		if(Check.isNotNull(toTalk) && Check.isRole(entityData.getForecastMap().getProvRole(toTalk.getAgent()), Role.WEREWOLF)) {
			entityData.getForecastMap().plusDoubt(talk.getAgent(), 0.2);
		}
	}
	
	/**
	 * 反対発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeDisagree(EntityData entityData, Talk talk, Content content) {
		// 反対先の発言取得
		Talk toTalk = entityData.getTalkDataBase().getToTalk(content.getTalkDay(), content.getTalkID());
		
		// 自分の同意先発言への反対発言がある場合
		if(entityData.getMyTalking().containTalk(TalkFactory.agreeRemark(toTalk))) {
			entityData.getForecastMap().plusDoubt(talk.getAgent(), 0.3);
		}
		
		// 自分の発言へ反対したエージェント
		if(Check.isNotNull(toTalk) && Check.isAgent(toTalk.getAgent(), entityData.getForecastMap().getMe())) {
			entityData.getForecastMap().plusDoubt(talk.getAgent(), 0.2);
		}
		
//		・FOを推奨しないエージェント(リクエストCOに対する反対があれば?)
		
	}
	
	/**
	 * リクエスト発言に対する疑い度変動
	 * @param entityData :インスタンスデータ
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public static void changeRequest(EntityData entityData, Talk talk, Content content) {
		if(OperatorElement.isRequest(content)) {
			// FOを推奨したエージェント (仮に占い師or霊媒師 COのリクエストを対象とした)
			if(Check.isTopic(OperatorElement.getTopic(content), Topic.COMINGOUT)) {
				if(Check.isRole(OperatorElement.getRole(content), Role.SEER) || Check.isRole(OperatorElement.getRole(content), Role.MEDIUM)) {
					entityData.getForecastMap().minusDoubt(talk.getAgent(), 0.3);
				}
			}
		}
	}

}
