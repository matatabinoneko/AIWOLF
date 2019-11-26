package com.gmail.romanesco2090.cedec2018impl.role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteByAI;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteReadAirLittle;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteWolf;
import com.gmail.romanesco2090.cedec2018impl.target.GuardBasic;
import com.gmail.romanesco2090.cedec2018impl.target.RevoteMajority;
import com.gmail.romanesco2090.cedec2018impl.target.VoteAsAnnouncedToLive;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;

public class BodyguardRole extends TFAFBaseRole {

    public BodyguardRole(Game game) {
        super(game);

        //会話戦略
        //初日最初は勝率の高いAIに投票宣言
        talkTactics.add(new TalkVoteByAI(), 1000, Day.on(1));
        //二日目以降最初は最も怪しいAgentに投票宣言
        talkTactics.add(new TalkVoteWolf(), 1000, Day.after(2));
        //以降は場の空気を読んでみる
        talkTactics.add(new TalkVoteReadAirLittle(), 100, Day.any(), Repeat.MULTI);

        //投票戦略
        //投票宣言した相手に対して投票する
        voteTactics.add(new VoteAsAnnouncedToLive());

        //再投票では、マジョリティに対して投票する
        revoteTactics.add(new RevoteMajority());

        //守備戦術
        guardTactics.add(new GuardBasic());

    }

}
