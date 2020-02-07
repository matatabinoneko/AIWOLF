package jp.or.plala.amail.rin0114.aiwolf.role;

import jp.or.plala.amail.rin0114.aiwolf.framework.Day;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteByAI;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteReadAirLittle2;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteWolf;
import jp.or.plala.amail.rin0114.aiwolf.target.GuardBasic;
import jp.or.plala.amail.rin0114.aiwolf.target.RevoteMajority;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteAsAnnouncedToLive;

public class BodyguardRole extends TFAFBaseRole {

    public BodyguardRole(Game game) {
        super(game);

        //会話戦略
        //初日最初は勝率の高いAIに投票宣言
        talkTactics.add(new TalkVoteByAI(), 1000, Day.on(1));
        //二日目以降最初は最も怪しいAgentに投票宣言
        talkTactics.add(new TalkVoteWolf(), 1000, Day.after(2));
        //以降は場の空気を読んでみる
        talkTactics.add(new TalkVoteReadAirLittle2(), 100, Day.any(), Repeat.MULTI);

        //投票戦略
        //投票宣言した相手に対して投票する
        voteTactics.add(new VoteAsAnnouncedToLive());

        //再投票では、マジョリティに対して投票する
        revoteTactics.add(new RevoteMajority());

        //守備戦術
        guardTactics.add(new GuardBasic());

    }

}
