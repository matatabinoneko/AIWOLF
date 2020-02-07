package com.gmail.romanesco2090.cedec2018impl.role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkCO5WolfForPP;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVote5WolfDay1;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVote5WolfDay2;
import com.gmail.romanesco2090.cedec2018impl.target.AttackWithoutCOSeer;
import com.gmail.romanesco2090.cedec2018impl.target.RevoteToLive5Wolf;
import com.gmail.romanesco2090.cedec2018impl.target.VoteToLive5Wolf;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;

public class WerewolfRole5ver extends TFAFBaseRole {

    public WerewolfRole5ver(Game game) {
        super(game);
        //最終日に裏切者COがあったら狼CO
        talkTactics.add(new TalkCO5WolfForPP(), 10000, Day.on(2));

        talkTactics.add(new TalkVote5WolfDay1(), 9000, Day.on(1), Repeat.MULTI);
        talkTactics.add(new TalkVote5WolfDay2(), 9000, Day.on(2), Repeat.MULTI);

        //最大得票数に乗っかる
        voteTactics.add(new VoteToLive5Wolf());
        //最大得票数に乗っかる
        revoteTactics.add(new RevoteToLive5Wolf());
        //占い師を避けて襲撃
        attackTactics.add(new AttackWithoutCOSeer());
    }
}
