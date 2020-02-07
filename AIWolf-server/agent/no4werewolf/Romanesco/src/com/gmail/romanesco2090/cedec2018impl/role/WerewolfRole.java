package com.gmail.romanesco2090.cedec2018impl.role;

import java.security.SecureRandom;

import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkCO;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivineWhiteRandom;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkIdentifiedResultWhite;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteLastMaxHate;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteWhiteMajority;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteWhiteWeak;
import com.gmail.romanesco2090.cedec2018impl.talk.WhisperAttackHateCount;
import com.gmail.romanesco2090.cedec2018impl.talk.WhisperAttackNotSeerMedium;
import com.gmail.romanesco2090.cedec2018impl.talk.WhisperCOMedium;
import com.gmail.romanesco2090.cedec2018impl.talk.WhisperCOSeer;
import com.gmail.romanesco2090.cedec2018impl.talk.WhisperCOVillager;
import com.gmail.romanesco2090.cedec2018impl.talk.WhisperEstimatePossessed;
import com.gmail.romanesco2090.cedec2018impl.target.AttackAccordingToMyself;
import com.gmail.romanesco2090.cedec2018impl.target.VoteAccordingToMyself;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;

public class WerewolfRole extends TFAFBaseRole {

	public WerewolfRole(Game game) {
		super(game);

		int kibun = new SecureRandom().nextInt(4);

		if (kibun == 0) {
			/* Whisper */
			// 0日目以降:占い師CO宣言
			whisperTactics.add(new WhisperCOSeer(), 10000, Day.on(0));
			// 身内以外に黒出し / 身内に白出しした占い師がいたら estimate POSSESSED
			whisperTactics.add(new WhisperEstimatePossessed(), 9000, Day.any());
			whisperTactics.add(new WhisperAttackHateCount(), 8000, Day.any());
			// 狩人 > 占い師・霊媒師CO以外から狩人率高い者へattack宣言
			whisperTactics.add(new WhisperAttackNotSeerMedium(), 7000, Day.any());

			/* Attck */
			// 基本的に宣言通りにAttack
			// 人狼が1人の時はWhisperが発生しないので、
			// これまで自分たちへのvoteカウントの大きかった生き物attack
			attackTactics.add(new AttackAccordingToMyself());
			reattackTactics.add(new AttackAccordingToMyself());

			/* Talk */
			// 初日1ターン目:占い師CO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			// 初日は2ターン目, 2日目以降1ターン目:テキトーに占い結果白
			talkTactics.add(new TalkDivineWhiteRandom(), 500);
			// 1日目3ターン目: 人狼、CO以外からメタ勝率が低い奴を vote 対象と宣言
			talkTactics.add(new TalkVoteWhiteWeak(), 10000, Day.on(1));

			// 2日目以降2ターン目: これまでのvoteカウントの大きかった生き物にvote宣言
			talkTactics.add(new TalkVoteLastMaxHate(), 10000, Day.on(2));
			/* 3ターン目以降:
			 * 身内以外のvote先をカウント。
			 * 最多得票先が人狼なら、票が入っている村人にvote宣言、そうでなければ乗っかる。 */
			talkTactics.add(new TalkVoteWhiteMajority(), 9000, Day.any(), Repeat.MULTI);

			/* Vote */
			// 基本的に宣言通り
			voteTactics.add(new VoteAccordingToMyself());
			revoteTactics.add(new VoteAccordingToMyself());
		} else if (kibun == 1) {
			/* Whisper */
			// 0日目以降:霊能者CO宣言
			whisperTactics.add(new WhisperCOMedium(), 10000, Day.on(0));
			// 身内以外に黒出し / 身内に白出しした占い師がいたら estimate POSSESSED
			whisperTactics.add(new WhisperEstimatePossessed(), 9000, Day.any());
			whisperTactics.add(new WhisperAttackHateCount(), 8000, Day.any());
			// 狩人 > 占い師・霊媒師CO以外から狩人率高い者へattack宣言
			whisperTactics.add(new WhisperAttackNotSeerMedium(), 7000, Day.any());

			/* Attck */
			// 基本的に宣言通りにAttack
			// 人狼が1人の時はWhisperが発生しないので、
			// これまで自分たちへのvoteカウントの大きかった生き物attack
			attackTactics.add(new AttackAccordingToMyself());
			reattackTactics.add(new AttackAccordingToMyself());

			/* Talk */
			// 初日1ターン目、霊能者CO
			talkTactics.add(new TalkCO(Role.MEDIUM), 10000, Day.on(1));
			// 1日目2ターン目: 人狼、CO以外からメタ勝率が低い奴を vote 対象と宣言
			talkTactics.add(new TalkVoteWhiteWeak(), 10000, Day.on(1));
			// 2日目以降1ターン目:霊能結果白
			talkTactics.add(new TalkIdentifiedResultWhite(), 500, Day.after(2));
			// 2日目以降2ターン目: これまでのvoteカウントの大きかった生き物にvote宣言
			talkTactics.add(new TalkVoteLastMaxHate(), 10000, Day.on(2));
			/* 3ターン目以降:
			 * 身内以外のvote先をカウント。
			 * 最多得票先が人狼なら、票が入っている村人にvote宣言、そうでなければ乗っかる。 */
			talkTactics.add(new TalkVoteWhiteMajority(), 9000, Day.any(), Repeat.MULTI);

			/* Vote */
			// 基本的に宣言通り
			voteTactics.add(new VoteAccordingToMyself());
			revoteTactics.add(new VoteAccordingToMyself());
		} else {
			//潜伏戦略
			/* Whisper */
			// 0日目以降:村人CO宣言
			whisperTactics.add(new WhisperCOVillager(), 10000, Day.on(0));
			// 身内以外に黒出し / 身内に白出しした占い師がいたら estimate POSSESSED
			whisperTactics.add(new WhisperEstimatePossessed(), 9000, Day.any());
			whisperTactics.add(new WhisperAttackHateCount(), 8000, Day.any());
			// 狩人 > 占い師・霊媒師CO以外から狩人率高い者へattack宣言
			whisperTactics.add(new WhisperAttackNotSeerMedium(), 7000, Day.any());

			/* Attck */
			// 基本的に宣言通りにAttack
			// 人狼が1人の時はWhisperが発生しないので、
			// これまで自分たちへのvoteカウントの大きかった生き物attack
			attackTactics.add(new AttackAccordingToMyself());
			reattackTactics.add(new AttackAccordingToMyself());

			/* Talk */
			/*  村人エージェントに似せる */
			// 1日目1ターン目: 人狼、CO以外からメタ勝率が低い奴を vote 対象と宣言
			talkTactics.add(new TalkVoteWhiteWeak(), 10000, Day.on(1));
			// 2日目以降1ターン目: これまでのvoteカウントの大きかった生き物にvote宣言
			talkTactics.add(new TalkVoteLastMaxHate(), 10000, Day.on(2));
			/* 2ターン目以降:
			 * 身内以外のvote先をカウント。
			 * 最多得票先が人狼なら、票が入っている村人にvote宣言、そうでなければ乗っかる。 */
			talkTactics.add(new TalkVoteWhiteMajority(), 9000, Day.any(), Repeat.MULTI);

			/* Vote */
			// 基本的に宣言通り
			voteTactics.add(new VoteAccordingToMyself());
			revoteTactics.add(new VoteAccordingToMyself());
		}
	}

}
