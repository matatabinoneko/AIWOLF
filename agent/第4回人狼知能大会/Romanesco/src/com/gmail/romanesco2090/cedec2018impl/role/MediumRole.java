package com.gmail.romanesco2090.cedec2018impl.role;

import java.security.SecureRandom;

import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkCOFoundBlack;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkCO;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkIdentifiedResult;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkIdentifiedResultAll;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteReadAirLittle;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteWolf;
import com.gmail.romanesco2090.cedec2018impl.target.RevoteMajority;
import com.gmail.romanesco2090.cedec2018impl.target.VoteAsAnnouncedToLive;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;

public class MediumRole extends TFAFBaseRole {

	public MediumRole(Game game) {
		super(game);

		int kibun = new SecureRandom().nextInt(3);

		if (kibun == 0) {
			//とりあえず初日の1ターン目にCO
			talkTactics.add(new TalkCO(Role.MEDIUM), 1000, Day.on(1));
			//二日目以降、霊媒結果を最速でお伝えする
			talkTactics.add(new TalkIdentifiedResult(), 1000, Day.after(2));
			//最も怪しいAgentに投票宣言
			talkTactics.add(new TalkVoteWolf(), 500, Day.any());
			//以降は場の空気を少し読んでみる
			talkTactics.add(new TalkVoteReadAirLittle(), 100, Day.any(), Repeat.MULTI);

			//投票戦略
			//投票宣言した相手に対して投票する
			voteTactics.add(new VoteAsAnnouncedToLive());

			//再投票では、マジョリティに対して投票する
			revoteTactics.add(new RevoteMajority());
		} else if(kibun == 1) {
			//2日目の1ターン目にCO
			talkTactics.add(new TalkCO(Role.MEDIUM), 1000, Day.on(2));
			//二日目以降、霊媒結果を最速でお伝えする
			talkTactics.add(new TalkIdentifiedResult(), 1000, Day.after(2));
			//最も怪しいAgentに投票宣言
			talkTactics.add(new TalkVoteWolf(), 500, Day.any());
			//以降は場の空気を少し読んでみる
			talkTactics.add(new TalkVoteReadAirLittle(), 100, Day.any(), Repeat.MULTI);

			//投票戦略
			//投票宣言した相手に対して投票する
			voteTactics.add(new VoteAsAnnouncedToLive());

			//再投票では、マジョリティに対して投票する
			revoteTactics.add(new RevoteMajority());
		} else {
			//黒を見つけたらCO
			talkTactics.add(new TalkCOFoundBlack(Role.MEDIUM), 1000, Day.any());

			//COしたら今までの結果を報告
			talkTactics.add(new TalkIdentifiedResultAll(), 750, Day.any(), Repeat.MULTI);

			//最も怪しいAgentに投票宣言
			talkTactics.add(new TalkVoteWolf(), 500, Day.any());
			//以降は場の空気を少し読んでみる
			talkTactics.add(new TalkVoteReadAirLittle(), 100, Day.any(), Repeat.MULTI);

			//投票戦略
			//投票宣言した相手に対して投票する
			voteTactics.add(new VoteAsAnnouncedToLive());

			//再投票では、マジョリティに対して投票する
			revoteTactics.add(new RevoteMajority());
		}
	}

}
