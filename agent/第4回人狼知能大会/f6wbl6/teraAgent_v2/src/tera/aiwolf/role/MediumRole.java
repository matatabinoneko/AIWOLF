package tera.aiwolf.role;

import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Day;
import tera.aiwolf.framework.Game;
import tera.aiwolf.talk.TalkCo;
import tera.aiwolf.talk.TalkIdentifiedResult;
import tera.aiwolf.talk.TalkVoteReadAirLittle2;
import tera.aiwolf.talk.TalkVoteWolf;
import tera.aiwolf.target.RevoteMajority;
import tera.aiwolf.target.VoteAsAnnouncedToLive;

public class MediumRole extends TFAFBaseRole {

    public MediumRole(Game game) {
        super(game);
        //会話戦略
        //とりあえず初日の1ターン目にCO
        talkTactics.add(new TalkCo(Role.MEDIUM), 1000, Day.on(1));
        //二日目以降、霊媒結果を最速でお伝えする
        talkTactics.add(new TalkIdentifiedResult(), 1000, Day.after(2));
        //最も怪しいAgentに投票宣言
        talkTactics.add(new TalkVoteWolf(), 500, Day.any());
        //以降は場の空気を少し読んでみる
        talkTactics.add(new TalkVoteReadAirLittle2(), 100, Day.any(), Repeat.MULTI);

        //投票戦略
        //投票宣言した相手に対して投票する
        voteTactics.add(new VoteAsAnnouncedToLive());

        //再投票では、マジョリティに対して投票する
        revoteTactics.add(new RevoteMajority());

    }

}
