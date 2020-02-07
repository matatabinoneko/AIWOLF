package jp.ac.aitech.k15029kk;

import org.aiwolf.client.lib.AgreeContentBuilder;
import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DisagreeContentBuilder;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.GuardCandidateContentBuilder;
import org.aiwolf.client.lib.GuardedAgentContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

/**
 * 会話のコンテンツを簡単に生成するためのクラス(ver0.4.4対応)
 * @author k14096kk
 *
 */
public class ContentFactory {

	/**
	 * 渡された会話からコンテンツを取得する
	 * @param talk :会話
	 * @return 会話コンテンツ
	 */
	public static Content getContent(Talk talk) {
		Content content = new Content(talk.getText());
		return content;
	}

	/**
	 * 予想発言コンテンツ作成
	 * @param target :予想先エージェント
	 * @param role :予想役職
	 * @return 予想コンテンツ(Content型)
	 */
	public static Content estimateContent(Agent target, Role role) {
		EstimateContentBuilder estimate = new EstimateContentBuilder(target, role);
		Content content = new Content(estimate);
		return content;
	}

	/**
	 * カミングアウト発言コンテンツ作成
	 * @param target :COするエージェント
	 * @param role :COする役職
	 * @return CO発言(Content型)
	 */
	public static Content comingoutContent(Agent target, Role role) {
		ComingoutContentBuilder comingout = new ComingoutContentBuilder(target, role);
		Content content = new Content(comingout);
		return content;
	}

	/**
	 * 自分のCO発言コンテンツ作成
	 * @param role :COする役職
	 * @return 自分のCO発言(Content型)「自分の役職はrole」
	 */
	@Deprecated
	public static Content comingoutMyContent(Role role) {
		ComingoutContentBuilder comingout = new ComingoutContentBuilder(null, role);
		Content content = new Content(comingout);
		return content;
	}

	/**
	 * 占い先発言コンテンツ作成
	 * @param target :占い先エージェント
	 * @return 占い先発言(Content型)
	 */
	public static Content divinationContent(Agent target) {
		DivinationContentBuilder divination = new DivinationContentBuilder(target);
		Content content = new Content(divination);
		return content;
	}

	/**
	 * 占い結果発言コンテンツ作成
	 * @param target :占い先エージェント
	 * @param result :占い結果
	 * @return 占い結果発言(Content型)
	 */
	public static Content divinedResultContent(Agent target, Species result) {
		DivinedResultContentBuilder divined = new DivinedResultContentBuilder(target, result);
		Content content = new Content(divined);
		return content;
	}

	/**
	 * 霊媒結果発言コンテンツ作成
	 * @param target :霊能先エージェント
	 * @param result :霊能結果
	 * @return 霊媒結果発言(Content型)
	 */
	public static Content identContent(Agent target, Species result) {
		IdentContentBuilder ident = new IdentContentBuilder(target, result);
		Content content = new Content(ident);
		return content;
	}

	/**
	 * 護衛先候補発言コンテンツ作成
	 * @param target :護衛先候補エージェント
	 * @return 護衛先候補発言(Content型)
	 */
	public static Content guardCandidateContent(Agent target) {
		GuardCandidateContentBuilder guard = new GuardCandidateContentBuilder(target);
		Content content = new Content(guard);
		return content;
	}

	/**
	 * 護衛した先発言,コンテンツ作成
	 * @param target :護衛先エージェント
	 * @return 護衛した先発言(Content型)
	 */
	public static Content guardedAgentContent(Agent target) {
		GuardedAgentContentBuilder guarded = new GuardedAgentContentBuilder(target);
		Content content = new Content(guarded);
		return content;
	}

	/**
	 * 投票発言コンテンツ作成
	 * @param target :投票先エージェント
	 * @return 投票発言(Content型)
	 */
	public static Content voteContent(Agent target) {
		VoteContentBuilder vote = new VoteContentBuilder(target);
		Content content = new Content(vote);
		return content;
	}

	/**
	 * 襲撃発言コンテンツ作成
	 * @param target :襲撃先エージェント
	 * @return 襲撃発言(Content型)
	 */
	public static Content attackContent(Agent target) {
		AttackContentBuilder attack = new AttackContentBuilder(target);
		Content content = new Content(attack);
		return content;
	}

	/**
	 * 同意発言コンテンツ作成
	 * @param talk :同意したい発言
	 * @return 同意発言(Content型)
	 */
	public static Content agreeContent(Talk talk) {
		AgreeContentBuilder agree = new AgreeContentBuilder(TalkType.TALK, talk.getDay(), talk.getIdx());
		Content content = new Content(agree);
		return content;
	}

	/**
	 * 反対発言コンテンツ作成
	 * @param talk :反対したい発言
	 * @return 反対発言(Content型)
	 */
	public static Content disagreeContent(Talk talk) {
		DisagreeContentBuilder disagree = new DisagreeContentBuilder(TalkType.TALK, talk.getDay(), talk.getIdx());
		Content content = new Content(disagree);
		return content;
	}

	/**
	 * 同意囁きコンテンツ作成
	 * @param talk :同意したい囁き
	 * @return 同意囁き(Content型)
	 */
	public static Content agreeWhisperContent(Talk talk) {
		AgreeContentBuilder agree = new AgreeContentBuilder(TalkType.WHISPER, talk.getDay(), talk.getIdx());
		Content content = new Content(agree);
		return content;
	}

	/**
	 * 反対囁きコンテンツ作成
	 * @param talk :反対したい囁き
	 * @return 反対囁き(Content型)
	 */
	public static Content disagreeWhisperContent(Talk talk) {
		DisagreeContentBuilder disagree = new DisagreeContentBuilder(TalkType.WHISPER, talk.getDay(), talk.getIdx());
		Content content = new Content(disagree);
		return content;
	}

	/**
	 * スキップのコンテンツ作成
	 * @return スキップ(Content型)
	 */
	public static Content skipContent() {
		Content content = Content.SKIP;
		return content;
	}

	/**
	 * オーバーのコンテンツ作成
	 * @return オーバー(Content型)
	 */
	public static Content overContent() {
		Content content = Content.OVER;
		return content;
	}

	/**
	 * 予想の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :予想の対象
	 * @param role :予想する役職
	 * @return 予想発言の要求「agentにtargetをroleであると思ってもらえませんか?」
	 */
	public static Content requestEstimateContent(Agent agent, Agent target, Role role) {
		RequestContentBuilder request = new RequestContentBuilder(agent, estimateContent(target, role));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 予想の要求コンテンツ作成(要求先不定)
	 * @param target :予想の対象
	 * @param role :予想する役職
	 * @return 予想発言の要求「全員にtargetがroleであると思ってもらえませんか?」
	 */
	public static Content requestAllEstimateContent(Agent target, Role role) {
		RequestContentBuilder request = new RequestContentBuilder(null, estimateContent(target, role));
		Content content = new Content(request);
		return content;
	}

	/**
	 * COの要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :COしてほしいエージェント
	 * @param role :COする役職
	 * @return CO発言の要求「agentさん，targetの役職はroleであると宣言しませんか?」
	 */
	public static Content requestComingoutContent(Agent agent, Agent target, Role role) {
		RequestContentBuilder request = new RequestContentBuilder(agent, comingoutContent(target, role));
		Content content = new Content(request);
		return content;
	}

	/**
	 * COの要求コンテンツ作成(要求先不定)
	 * @param target :COしてほしいエージェント
	 * @param role :COする役職
	 * @return CO発言の要求「みなさん，argetの役職はroleであると宣言しませんか?」
	 */
	public static Content requestAllComingoutContent(Agent target, Role role) {
		RequestContentBuilder request = new RequestContentBuilder(null, comingoutContent(target, role));
		Content content = new Content(request);
		return content;
	}

	/**
	 * COの要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param role :COしてほしい役職
	 * @return CO発言の要求「agentさん，role宣言しませんか?」
	 */
	@Deprecated
	public static Content requestComingoutMyContent(Agent agent, Role role) {
		RequestContentBuilder request = new RequestContentBuilder(agent, comingoutMyContent(role));
		Content content = new Content(request);
		return content;
	}

	/**
	 * COの要求コンテンツ作成(要求先不定)
	 * @param role :COしてほしい役職
	 * @return CO発言の要求「みなさん，role宣言しませんか?(指定役職COの誘導)」
	 */
	@Deprecated
	public static Content requestAllComingoutMyContent(Role role) {
		RequestContentBuilder request = new RequestContentBuilder(null, comingoutMyContent(role));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 占い先の要求コンテンツ作成(要求先指定)
	 * @param agent :要求対象エージェント
	 * @param target :占い先エージェント
	 * @return 占い先発言の要求「agentさん，targetを占いましょう」
	 */
	public static Content requestDivineContent(Agent agent, Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(agent, divinationContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 占い先の要求コンテンツ作成(要求先不定)
	 * @param target :占い先エージェント
	 * @return 占い先発言の要求「みなさん，targetを占いましょう」
	 */
	public static Content requestAllDivineContent(Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(null, divinationContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 占い結果の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :占い対象
	 * @param result :占い結果
	 * @return 占い結果の要求「agentさん，targetの占い結果はresultだったと宣言してほしい」
	 */
	public static Content requestDivinedContent(Agent agent, Agent target, Species result) {
		RequestContentBuilder request = new RequestContentBuilder(agent, divinedResultContent(target, result));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 占い結果の要求コンテンツ作成(要求先不定)
	 * @param target :占い対象
	 * @param result :占い結果
	 * @return 占い結果の要求「targetの占い結果はresultだったと宣言してほしい」
	 */
	public static Content requestAllDivinedContent(Agent target, Species result) {
		RequestContentBuilder request = new RequestContentBuilder(null, divinedResultContent(target, result));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 霊能結果の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :霊媒対象
	 * @param result :霊媒結果
	 * @return 霊媒の結果の要求「agentさん，targetの霊媒結果はresultだったと宣言してほしい」
	 */
	public static Content requestIdentContent(Agent agent, Agent target, Species result) {
		RequestContentBuilder request = new RequestContentBuilder(agent, identContent(target, result));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 霊能結果の要求コンテンツ作成(要求先不定)
	 * @param target :霊媒対象
	 * @param result :霊媒結果
	 * @return 霊媒の結果の要求「targetの霊媒結果はresultだったと宣言してほしい」
	 */
	public static Content requestAllIdentContent(Agent target, Species result) {
		RequestContentBuilder request = new RequestContentBuilder(null, identContent(target, result));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 護衛先候補の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :護衛先候補
	 * @return 護衛先候補の要求「agentさん，targetを護衛して欲しい」
	 */
	public static Content requestGuardContent(Agent agent, Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(agent, guardCandidateContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 護衛先候補の要求コンテンツ作成(要求先不定)
	 * @param target :護衛先候補
	 * @return 護衛先候補の要求「みなさん，targetを護衛しませんか」
	 */
	public static Content requestAllGuardContent(Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(null, guardCandidateContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 護衛した先の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @param target :護衛した先
	 * @return 護衛した先の要求「agentさん，targetを護衛したと宣言して欲しい」
	 */
	public static Content requestGuardedContent(Agent agent, Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(agent, guardedAgentContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 護衛した先の要求コンテンツ作成(要求先不定)
	 * @param target :護衛した先
	 * @return 護衛した先の要求「targetを護衛したと誰かに言ってほしい」
	 */
	public static Content requestAllGuardedContent(Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(null, guardedAgentContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 投票の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先
	 * @param target :投票先
	 * @return 投票先の要求「agentさん，targetに投票して欲しい」
	 */
	public static Content requestVoteContent(Agent agent, Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(agent, voteContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 投票の要求コンテンツ作成(要求先不定)
	 * @param target :投票先
	 * @return 投票先の要求「みなさん，targetに投票しませんか」
	 */
	public static Content requestAllVoteContent(Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(null, voteContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 襲撃投票の要求コンテンツ作成(要求先指定)
	 * @param agent : 要求先エージェント
	 * @param target : 襲撃先エージェント
	 * @return 襲撃投票の要求「agentさん，targetに襲撃しましょう」
	 */
	public static Content requestAttackContent(Agent agent, Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(agent, attackContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 襲撃投票の要求コンテンツ作成(要求先不定)
	 * @param target : 襲撃先エージェント
	 * @return 襲撃投票の要求「targetに襲撃しましょう」
	 */
	public static Content requestAllAttackContent(Agent target) {
		RequestContentBuilder request = new RequestContentBuilder(null, attackContent(target));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 同意の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先
	 * @param talk :同意する発言
	 * @return 同意の要求「agentさん，talkを認めてください」
	 */
	public static Content requestAgreeContent(Agent agent, Talk talk) {
		RequestContentBuilder request = new RequestContentBuilder(agent, agreeContent(talk));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 同意の要求コンテンツ作成(要求先不定)
	 * @param talk :同意する発言
	 * @return 同意の要求「みなさん，talkを認めてください」
	 */
	public static Content requestAllAgreeContent(Talk talk) {
		RequestContentBuilder request = new RequestContentBuilder(null, agreeContent(talk));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 反対の要求コンテンツ作成(要求先指定)
	 * @param agent :要求先
	 * @param talk :反対する発言
	 * @return 反対の要求「agentさん，talkを認めないでください」
	 */
	public static Content requestDisagreeContent(Agent agent, Talk talk) {
		RequestContentBuilder request = new RequestContentBuilder(agent, disagreeContent(talk));
		Content content = new Content(request);
		return content;
	}

	/**
	 * 反対の要求コンテンツ作成(要求先不定)
	 * @param talk :反対する発言
	 * @return 反対の要求「みなさん，talkを認めないでください」
	 */
	public static Content requestAllDisagreeContent(Talk talk) {
		RequestContentBuilder request = new RequestContentBuilder(null, disagreeContent(talk));
		Content content = new Content(request);
		return content;
	}

	/**
	 * スキップの要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @return スキップ要求「agentさん，今は見送ってください」
	 */
	public static Content requestSkipContent(Agent agent) {
		RequestContentBuilder request = new RequestContentBuilder(agent, skipContent());
		Content content = new Content(request);
		return content;
	}

	/**
	 * スキップの要求コンテンツ作成(要求先不定)
	 * @return スキップ要求「みなさん，今は見送ってください」
	 */
	public static Content requestAllSkipContent() {
		RequestContentBuilder request = new RequestContentBuilder(null, skipContent());
		Content content = new Content(request);
		return content;
	}

	/**
	 * オーバーの要求コンテンツ作成(要求先指定)
	 * @param agent :要求先エージェント
	 * @return オーバー要求「agentさん，もう話さないでください」
	 */
	public static Content requestOverContent(Agent agent) {
		RequestContentBuilder request = new RequestContentBuilder(agent, overContent());
		Content content = new Content(request);
		return content;
	}

	/**
	 * オーバーの要求コンテンツ作成(要求先不定)
	 * @return オーバー要求「みなさん，もう話さないでください」
	 */
	public static Content requestAllOverContent() {
		RequestContentBuilder request = new RequestContentBuilder(null, overContent());
		Content content = new Content(request);
		return content;
	}

}
