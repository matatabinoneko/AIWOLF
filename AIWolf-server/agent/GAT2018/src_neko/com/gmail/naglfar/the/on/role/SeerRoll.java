package com.gmail.naglfar.the.on.role;

import java.util.HashSet;
import java.util.Set;

import org.aiwolf.common.data.Role;

import com.gmail.naglfar.the.on.framework.Day;
import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.GameAgent;
import com.gmail.naglfar.the.on.talk.TalkCo;
import com.gmail.naglfar.the.on.talk.TalkDivineWithEvilScore;
import com.gmail.naglfar.the.on.talk.TalkVoteDivined;
import com.gmail.naglfar.the.on.talk.TalkVoteReadAirLittle2;
import com.gmail.naglfar.the.on.talk.TalkVoteWolf;
import com.gmail.naglfar.the.on.target.DivineBasicAvoidSeer;
import com.gmail.naglfar.the.on.target.DivineByAI;
import com.gmail.naglfar.the.on.target.RevoteMajority;
import com.gmail.naglfar.the.on.target.VoteAsAnnouncedToLive;

public class SeerRoll extends TFAFBaseRole {

    private Set<GameAgent> divined = new HashSet<>();

    public SeerRoll(Game game) {
        super(game);
        //会話戦略
        //とりあえず初日の1ターン目にCO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        //占い結果を最速でお伝えする
        talkTactics.add(new TalkDivineWithEvilScore(), 1000, Day.any());
        //占いで黒を発見したらVote宣言
        talkTactics.add(new TalkVoteDivined(), 100, Day.any());
        //最も怪しいAgentに投票宣言
        talkTactics.add(new TalkVoteWolf(), 90, Day.any());
        //以降は場の空気を少し読んでみる
        talkTactics.add(new TalkVoteReadAirLittle2(), 50, Day.any(), Repeat.MULTI);

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
