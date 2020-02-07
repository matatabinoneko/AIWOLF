package com.gmail.kanpyo2018.talk;


import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

/**
 * 会話を簡単に生成するためのクラス(ver0.4.4対応)
 * @author nono
 *
 */
public class TalkFactory {

	/**
	 * 予想発言作成
	 * @param target :予想先エージェント
	 * @param role :予想役職
	 * @return 予想発言「targetの役職はroleだと思う」
	 */
	public static String estimateRemark(Agent target, Role role) {
		if(Check.isNull(target) || Check.isNull(role)) return null;
		return ContentFactory.estimateContent(target, role).getText();
	}

	/**
	 * CO発言作成
	 * @param target :COするエージェント
	 * @param role :COする役職
	 * @return CO発言「targetの役職はroleだ」
	 */
	public static String comingoutRemark(Agent target, Role role) {
		if(Check.isNull(target) || Check.isNull(role)) return null;
		return ContentFactory.comingoutContent(target, role).getText();
	}

	/**
	 * 自分のCO発言作成
	 * @param role :COする役職
	 * @return 自分のCO発言「自分の役職はrole」
	 */
	@Deprecated
	public static String comingoutMyRemark(Role role) {
		return ContentFactory.comingoutMyContent(role).getText();
	}

	/**
	 * 占い先発言作成
	 * @param target :占い先エージェント
	 * @return 占い先発言「targetを占う」
	 */
	public static String divinationRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.divinationContent(target).getText();
	}

	/**
	 * 占い結果発言作成
	 * @param target :占い先エージェント
	 * @param result :占い結果
	 * @return 占い結果発言「targetを占った結果resultだった」
	 */
	public static String divinedResultRemark(Agent target, Species result) {
		if(Check.isNull(target) || Check.isNull(result)) return null;
		return ContentFactory.divinedResultContent(target, result).getText();
	}

	/**
	 * 霊媒結果発言作成
	 * @param target :霊能先エージェント
	 * @param result :霊能結果
	 * @return 霊媒結果発言「targetは霊媒の結果resultだった」
	 */
	public static String identRemark(Agent target, Species result) {
		if(Check.isNull(target) || Check.isNull(result)) return null;
		return ContentFactory.identContent(target, result).getText();
	}

	/**
	 * 護衛先候補発言作成
	 * @param target :護衛先候補エージェント
	 * @return 護衛先候補発言「targetを護衛する」
	 */
	public static String guardCandidateRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.guardCandidateContent(target).getText();
	}

	/**
	 * 護衛した先発言作成
	 * @param target :護衛先エージェント
	 * @return 護衛した先発言「targetを護衛した」
	 */
	public static String guardedAgentRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.guardedAgentContent(target).getText();
	}

	/**
	 * 投票発言作成
	 * @param target :投票先エージェント
	 * @return 投票発言「targetに投票する」
	 */
	public static String voteRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.voteContent(target).getText();
	}

	/**
	 * 襲撃発言作成
	 * @param target :襲撃先エージェント
	 * @return 襲撃発言「targetを襲撃投票する」
	 */
	public static String attackRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.attackContent(target).getText();
	}

	/**
	 * 同意発言作成
	 * @param talk :同意したい発言
	 * @return 同意発言「talkに同意する」
	 */
	public static String agreeRemark(Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.agreeContent(talk).getText();
	}

	/**
	 * 反対発言作成
	 * @param talk :反対したい発言
	 * @return 反対発言「talkに反対する」
	 */
	public static String disagreeRemark(Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.disagreeContent(talk).getText();
	}

	/**
	 * 同意囁き作成
	 * @param talk :同意したい囁き
	 * @return 同意囁き「talkに同意する」
	 */
	public static String agreeWhisperRemark(Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.agreeWhisperContent(talk).getText();
	}

	/**
	 * 反対囁き作成
	 * @param talk :反対したい囁き
	 * @return 反対囁き「talkに反対する」
	 */
	public static String disagreeWhisperRemark(Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.disagreeWhisperContent(talk).getText();
	}

	/**
	 * スキップ発言作成
	 * @return スキップ「スキップする」
	 */
	public static String skipRemark() {
		return ContentFactory.skipContent().getText();
	}

	/**
	 * オーバー発言作成
	 * @return オーバー「オーバーする」
	 */
	public static String overRemark() {
		return ContentFactory.overContent().getText();
	}

	/**
	 * 予想の要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :予想の対象
	 * @param role :予想する役職
	 * @return 予想発言の要求「agentにtargetをroleであると思ってもらえませんか?」
	 */
	public static String requestEstimateRemark(Agent agent, Agent target, Role role) {
		if(Check.isNull(target) || Check.isNull(role)) return null;
		return ContentFactory.requestEstimateContent(agent, target, role).getText();
	}

	/**
	 * 予想の要求発言作成(要求先不定)
	 * @param target :予想の対象
	 * @param role :予想する役職
	 * @return 予想発言の要求「全員にtargetがroleであると思ってもらえませんか?」
	 */
	public static String requestAllEstimateRemark(Agent target, Role role) {
		if(Check.isNull(target) || Check.isNull(role)) return null;
		return ContentFactory.requestAllEstimateContent(target, role).getText();
	}

	/**
	 * COの要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :COしてほしいエージェント
	 * @param role :COする役職
	 * @return CO発言の要求「agentさん，targetの役職はroleであると宣言しませんか?」
	 */
	public static String requestComingoutRemark(Agent agent, Agent target, Role role) {
		if(Check.isNull(target) || Check.isNull(role)) return null;
		return ContentFactory.requestComingoutContent(agent, target, role).getText();
	}

	/**
	 * COの要求発言作成(要求先不定)
	 * @param target :COしてほしいエージェント
	 * @param role :COする役職
	 * @return CO発言の要求「みなさん，argetの役職はroleであると宣言しませんか?」
	 */
	public static String requestAllComingoutRemark(Agent target, Role role) {
		if(Check.isNull(target) || Check.isNull(role)) return null;
		return ContentFactory.requestAllComingoutContent(target, role).getText();
	}

	/**
	 * COの要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param role :COしてほしい役職
	 * @return CO発言の要求「agentさん，role宣言しませんか?」
	 */
	@Deprecated
	public static String requestComingoutMyRemark(Agent agent, Role role) {
		return ContentFactory.requestComingoutMyContent(agent, role).getText();
	}

	/**
	 * COの要求発言作成(要求先不定)
	 * @param role :COしてほしい役職
	 * @return CO発言の要求「みなさん，role宣言しませんか?(指定役職COの誘導)」
	 */
	@Deprecated
	public static String requestAllComingoutMyRemark(Role role) {
		return ContentFactory.requestAllComingoutMyContent(role).getText();
	}

	/**
	 * 占い先の要求発言作成(要求先指定)
	 * @param agent :要求対象エージェント
	 * @param target :占い先エージェント
	 * @return 占い先発言の要求「agentさん，targetを占いましょう」
	 */
	public static String requestDivinationRemark(Agent agent, Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestDivineContent(agent, target).getText();
	}

	/**
	 * 占い先の要求発言作成(要求先不定)
	 * @param target :占い先エージェント
	 * @return 占い先発言の要求「みなさん，targetを占いましょう」
	 */
	public static String requestAllDivinationRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestAllDivineContent(target).getText();
	}

	/**
	 * 占い結果の要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :占い対象
	 * @param result :占い結果
	 * @return 占い結果の要求「agentさん，targetの占い結果はresultだったと宣言してほしい」
	 */
	public static String requestDivinedRemark(Agent agent, Agent target, Species result) {
		if(Check.isNull(target) || Check.isNull(result)) return null;
		return ContentFactory.requestDivinedContent(agent, target, result).getText();
	}

	/**
	 * 占い結果の要求発言作成(要求先不定)
	 * @param target :占い対象
	 * @param result :占い結果
	 * @return 占い結果の要求「targetの占い結果はresultだったと宣言してほしい」
	 */
	public static String requestAllDivinedRemark(Agent target, Species result) {
		if(Check.isNull(target) || Check.isNull(result)) return null;
		return ContentFactory.requestAllDivinedContent(target, result).getText();
	}

	/**
	 * 霊能結果の要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :霊媒対象
	 * @param result :霊媒結果
	 * @return 霊媒の結果の要求「agentさん，targetの霊媒結果はresultだったと宣言してほしい」
	 */
	public static String requestIdentRemark(Agent agent, Agent target, Species result) {
		if(Check.isNull(target) || Check.isNull(result)) return null;
		return ContentFactory.requestIdentContent(agent, target, result).getText();
	}

	/**
	 * 霊能結果の要求発言作成(要求先不定)
	 * @param target :霊媒対象
	 * @param result :霊媒結果
	 * @return 霊媒の結果の要求「targetの霊媒結果はresultだったと宣言してほしい」
	 */
	public static String requestAllIdentRemark(Agent target, Species result) {
		if(Check.isNull(target) || Check.isNull(result)) return null;
		return ContentFactory.requestAllIdentContent(target, result).getText();
	}

	/**
	 * 護衛先候補の要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :護衛先候補
	 * @return 護衛先候補の要求「agentさん，targetを護衛して欲しい」
	 */
	public static String requestGuardRemark(Agent agent, Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestGuardContent(agent, target).getText();
	}

	/**
	 * 護衛先候補の要求発言作成(要求先不定)
	 * @param target :護衛先候補
	 * @return 護衛先候補の要求「みなさん，targetを護衛しませんか」
	 */
	public static String requestAllGuardRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestAllGuardContent(target).getText();
	}

	/**
	 * 護衛した先の要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :護衛した先
	 * @return 護衛した先の要求「agentさん，targetを護衛したと宣言して欲しい」
	 */
	public static String requestGuardedRemark(Agent agent, Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestGuardedContent(agent, target).getText();
	}

	/**
	 * 護衛した先の要求発言作成(要求先不定)
	 * @param target :護衛した先
	 * @return 護衛した先の要求「targetを護衛したと誰かに言ってほしい」
	 */
	public static String requestAllGuardedRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestAllGuardedContent(target).getText();
	}

	/**
	 * 投票の要求発言作成(要求先指定)
	 * @param agent :要求先
	 * @param target :投票先
	 * @return 投票先の要求「agentさん，targetに投票して欲しい」
	 */
	public static String requestVoteRemark(Agent agent, Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestVoteContent(agent, target).getText();
	}

	/**
	 * 投票の要求発言作成(要求先不定)
	 * @param target :投票先
	 * @return 投票先の要求「みなさん，targetに投票しませんか」
	 */
	public static String requestAllVoteRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestAllVoteContent(target).getText();
	}

	/**
	 * 襲撃投票の要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :襲撃先エージェント
	 * @return 襲撃投票の要求「agentさん，targetに襲撃しましょう」
	 */
	public static String requestAttackRemark(Agent agent, Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestAttackContent(agent, target).getText();
	}

	/**
	 * 襲撃投票の要求発言作成(要求先不定)
	 * @param target :襲撃先エージェント
	 * @return 襲撃投票の要求「targetに襲撃しましょう」
	 */
	public static String requestAllAttackRemark(Agent target) {
		if(Check.isNull(target)) return null;
		return ContentFactory.requestAllAttackContent(target).getText();
	}

	/**
	 * 同意の要求発言作成(要求先指定)
	 * @param agent :要求先
	 * @param talk :同意する発言
	 * @return 同意の要求「agentさん，talkを認めてください」
	 */
	public static String requestAgreeRemark(Agent agent, Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.requestAgreeContent(agent, talk).getText();
	}


	/**
	 * 同意の要求発言作成(要求先不定)
	 * @param talk :同意する発言
	 * @return 同意の要求「みなさん，talkを認めてください」
	 */
	public static String requestAllAgreeRemark(Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.requestAllAgreeContent(talk).getText();
	}

	/**
	 * 反対の要求発言作成(要求先指定)
	 * @param agent :要求先
	 * @param talk :反対する発言
	 * @return 反対の要求「agentさん，talkを認めないでください」
	 */
	public static String requestDisagreeRemark(Agent agent, Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.requestDisagreeContent(agent, talk).getText();
	}

	/**
	 * 反対の要求発言作成(要求先不定)
	 * @param talk :反対する発言
	 * @return 反対の要求「みなさん，talkを認めないでください」
	 */
	public static String requestAllDisagreeRemark(Talk talk) {
		if(Check.isNull(talk)) return null;
		return ContentFactory.requestAllDisagreeContent(talk).getText();
	}

	/**
	 * スキップの要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @return スキップの要求「agentさん，今は見送ってください」
	 */
	public static String requestSkip(Agent agent) {
		return ContentFactory.requestSkipContent(agent).getText();
	}

	/**
	 * スキップの要求発言作成(要求先不定)
	 * @return スキップの要求「みなさん，今は見送ってください」
	 */
	public static String requestAllSkip() {
		return ContentFactory.requestAllSkipContent().getText();
	}

	/**
	 * オーバーの要求発言作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @return オーバーの要求「agentさん，もう話さないでください」
	 */
	public static String requestOver(Agent agent) {
		return ContentFactory.requestOverContent(agent).getText();
	}

	/**
	 * オーバーの要求発言作成(要求先不定)
	 * @return オーバーの要求「みなさん，もう話さないでください」
	 */
	public static String requestAllOver() {
		return ContentFactory.requestAllOverContent().getText();
	}

}
