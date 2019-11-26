package tkg.aiwolf.role;

import tkg.aiwolf.talk.TalkCo;
import tkg.aiwolf.talk.TalkIdentifiedResult;
import tkg.aiwolf.talk.TalkVoteReadAirLittle2;
import tkg.aiwolf.talk.TalkVoteWolf;
import tkg.aiwolf.target.RevoteMajority;
import tkg.aiwolf.target.VoteAsAnnouncedToLive;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Day;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;

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
