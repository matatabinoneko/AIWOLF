package jp.or.plala.amail.rin0114.aiwolf.role;

import org.aiwolf.common.data.Role;

import jp.or.plala.amail.rin0114.aiwolf.framework.Day;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkCo;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineBlackAttacktoSeer;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteForFakeBlack;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteLast3PPTactic5ver;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteWByPo5ver;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteWithoutWolf;

public class PossessedRole5ver extends TFAFBaseRole {

    public PossessedRole5ver(Game game) {
        super(game);
        // 最初のターン、占いCO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        // COの次ターン、対抗占いに黒。対抗COがない場合は狼ランキング最下位に黒。
        talkTactics.add(new TalkDivineBlackAttacktoSeer(), 10000, Day.on(1));
        //黒判定を宣言したエージェントへVote宣言をする。
        talkTactics.add(new TalkVoteForFakeBlack(), 9000, Day.on(1));

        // 最終日狼CO
        talkTactics.add(new TalkCo(Role.WEREWOLF), 10000, Day.on(2));

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
