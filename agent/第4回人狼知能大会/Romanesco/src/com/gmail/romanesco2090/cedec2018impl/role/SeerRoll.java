package com.gmail.romanesco2090.cedec2018impl.role;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkCO;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkCOFoundBlack;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivineWithEvilScore;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivinedResult;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivinedResultAll;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteDivined;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteReadAirLittle;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteWolf;
import com.gmail.romanesco2090.cedec2018impl.target.DivineBasicAvoidSeer;
import com.gmail.romanesco2090.cedec2018impl.target.DivineByAI;
import com.gmail.romanesco2090.cedec2018impl.target.RevoteMajority;
import com.gmail.romanesco2090.cedec2018impl.target.VoteAsAnnouncedToLive;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;

public class SeerRoll extends TFAFBaseRole {

	private Set<GameAgent> divined = new HashSet<>();

	public SeerRoll(Game game) {
		super(game);

		int kibun = new SecureRandom().nextInt(3);

		if (kibun == 0) {
			//会話戦略
			//とりあえず初日の1ターン目にCO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			//占い結果を最速でお伝えする,3日目以降は結果を偽る時がある
			talkTactics.add(new TalkDivineWithEvilScore(), 1000, Day.any());
			//占いで黒を発見したらVote宣言
			talkTactics.add(new TalkVoteDivined(), 100, Day.any());
			//最も怪しいAgentに投票宣言
			talkTactics.add(new TalkVoteWolf(), 90, Day.any());
			//以降は場の空気を少し読んでみる
			talkTactics.add(new TalkVoteReadAirLittle(), 50, Day.any(), Repeat.MULTI);

			//投票戦略
			//投票宣言した相手に対して投票する
			voteTactics.add(new VoteAsAnnouncedToLive());

			//再投票では、マジョリティに対して投票する
			revoteTactics.add(new RevoteMajority());

			//占い戦術
			//0日目は勝率が高い人を占う
			divineTactics.add(new DivineByAI(divined), Day.on(0));
			//1日目夜以降は、自称占い以外をスコアが高い順に占っていく
			divineTactics.add(new DivineBasicAvoidSeer(divined), Day.after(1));
		} else if(kibun == 1){
			//会話戦略
			//とりあえず初日の1ターン目にCO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			//正直に占い結果をお伝えする
			talkTactics.add(new TalkDivinedResult(), 1000, Day.any());
			//最も怪しいAgentに投票宣言
			talkTactics.add(new TalkVoteWolf(), 90, Day.any());
			//以降は場の空気を少し読んでみる
			talkTactics.add(new TalkVoteReadAirLittle(), 50, Day.any(), Repeat.MULTI);

			//投票戦略
			//投票宣言した相手に対して投票する
			voteTactics.add(new VoteAsAnnouncedToLive());

			//再投票では、マジョリティに対して投票する
			revoteTactics.add(new RevoteMajority());

			//占い戦術
			//0日目は勝率が高い人を占う
			divineTactics.add(new DivineByAI(divined), Day.on(0));
			//1日目夜以降は、自称占い以外をスコアが高い順に占っていく
			divineTactics.add(new DivineBasicAvoidSeer(divined), Day.after(1));
		} else {
			//会話戦略
			//人狼を発見したら占いCO
			talkTactics.add(new TalkCOFoundBlack(Role.SEER), 10000, Day.any());
			//正直に今までの占い結果をお伝えする
			talkTactics.add(new TalkDivinedResultAll(), 1000, Day.any(), Repeat.MULTI);
			//最も怪しいAgentに投票宣言
			talkTactics.add(new TalkVoteWolf(), 90, Day.any());
			//以降は場の空気を少し読んでみる
			talkTactics.add(new TalkVoteReadAirLittle(), 50, Day.any(), Repeat.MULTI);

			//投票戦略
			//投票宣言した相手に対して投票する
			voteTactics.add(new VoteAsAnnouncedToLive());

			//再投票では、マジョリティに対して投票する
			revoteTactics.add(new RevoteMajority());

			//占い戦術
			//0日目は勝率が高い人を占う
			divineTactics.add(new DivineByAI(divined), Day.on(0));
			//1日目夜以降は、自称占い以外をスコアが高い順に占っていく
			divineTactics.add(new DivineBasicAvoidSeer(divined), Day.after(1));
		}
	}

}
