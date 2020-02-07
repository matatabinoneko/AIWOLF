package com.gmail.romanesco2090.cedec2018impl.role;

import java.security.SecureRandom;

import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkCO;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivineBlackAttacktoNoSeer;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivineBlackAttacktoSeer;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkSlideCO;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteForFakeBlack;
import com.gmail.romanesco2090.cedec2018impl.target.VoteLast3PPTactic5ver;
import com.gmail.romanesco2090.cedec2018impl.target.VoteWByPo5ver;
import com.gmail.romanesco2090.cedec2018impl.target.VoteWithoutWolf;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;

public class PossessedRole5ver extends TFAFBaseRole {

	public PossessedRole5ver(Game game) {
		super(game);

		int kibun = new SecureRandom().nextInt(3);

		if (kibun == 0) {
			// 最初のターン、占いCO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			// COの次ターン、対抗占いに黒。対抗COがない場合は狼ランキング最下位に黒。
			talkTactics.add(new TalkDivineBlackAttacktoSeer(), 10000, Day.on(1));
			//黒判定を宣言したエージェントへVote宣言をする。
			talkTactics.add(new TalkVoteForFakeBlack(), 9000, Day.on(1));

			// 最終日狼CO
			talkTactics.add(new TalkSlideCO(Role.WEREWOLF), 10000, Day.on(2));

			// 狼COしていないものに投票
			voteTactics.add(new VoteLast3PPTactic5ver());
			revoteTactics.add(new VoteLast3PPTactic5ver());

			//自分の黒出しに投票
			voteTactics.add(new VoteWByPo5ver(), 10000);
			revoteTactics.add(new VoteWByPo5ver(), 10000);

			// 狼ランキングトップを避けて投票。
			voteTactics.add(new VoteWithoutWolf(), 9000);
			revoteTactics.add(new VoteWithoutWolf(), 9000);
		} else if(kibun == 1) {
			// 最初のターン、占いCO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			// COの次ターン、対抗占いに黒。対抗COがない場合は狼ランキング最下位に黒。
			talkTactics.add(new TalkDivineBlackAttacktoSeer(), 10000, Day.on(1));
			//黒判定を宣言したエージェントへVote宣言をする。
			talkTactics.add(new TalkVoteForFakeBlack(), 9000, Day.on(1));

			// 最終日狂人CO
			talkTactics.add(new TalkSlideCO(Role.POSSESSED), 10000, Day.on(2));

			// 狼COしていないものに投票
			voteTactics.add(new VoteLast3PPTactic5ver());
			revoteTactics.add(new VoteLast3PPTactic5ver());

			//自分の黒出しに投票
			voteTactics.add(new VoteWByPo5ver(), 10000);
			revoteTactics.add(new VoteWByPo5ver(), 10000);

			// 狼ランキングトップを避けて投票。
			voteTactics.add(new VoteWithoutWolf(), 9000);
			revoteTactics.add(new VoteWithoutWolf(), 9000);
		} else if(kibun == 2) {
			// 最初のターン、占いCO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			// COの次ターン、対抗占いに黒。対抗COがない場合は狼ランキング最下位に黒。
			talkTactics.add(new TalkDivineBlackAttacktoSeer(), 10000, Day.on(1));
			//黒判定を宣言したエージェントへVote宣言をする。
			talkTactics.add(new TalkVoteForFakeBlack(), 9000, Day.on(1));

			// 最終日狂人CO
			talkTactics.add(new TalkSlideCO(Role.POSSESSED), 10000, Day.on(2));

			// 狼COしていないものに投票
			voteTactics.add(new VoteLast3PPTactic5ver());
			revoteTactics.add(new VoteLast3PPTactic5ver());

			//自分の黒出しに投票
			voteTactics.add(new VoteWByPo5ver(), 10000);
			revoteTactics.add(new VoteWByPo5ver(), 10000);

			// 狼ランキングトップを避けて投票。
			voteTactics.add(new VoteWithoutWolf(), 9000);
			revoteTactics.add(new VoteWithoutWolf(), 9000);
		} else {
			// 最初のターン、占いCO
			talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
			// 占い師CO以外に黒出し
			talkTactics.add(new TalkDivineBlackAttacktoNoSeer(), 10000, Day.on(1));
			//黒判定を宣言したエージェントへVote宣言をする。
			talkTactics.add(new TalkVoteForFakeBlack(), 9000, Day.on(1));

			// 最終日狼CO
			talkTactics.add(new TalkSlideCO(Role.WEREWOLF), 10000, Day.on(2));

			// 狼COしていないものに投票
			voteTactics.add(new VoteLast3PPTactic5ver());
			revoteTactics.add(new VoteLast3PPTactic5ver());

			//自分の黒出しに投票
			voteTactics.add(new VoteWByPo5ver(), 10000);
			revoteTactics.add(new VoteWByPo5ver(), 10000);

			// 狼ランキングトップを避けて投票。
			voteTactics.add(new VoteWithoutWolf(), 9000);
			revoteTactics.add(new VoteWithoutWolf(), 9000);
		}
	}

}
