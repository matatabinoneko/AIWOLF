package tkg.aiwolf.role;

import java.util.HashSet;
import java.util.Set;

import tkg.aiwolf.talk.TalkCo;
import tkg.aiwolf.talk.TalkDivineWithEvilScore;
import tkg.aiwolf.talk.TalkVoteDivined;
import tkg.aiwolf.talk.TalkVoteReadAirLittle2;
import tkg.aiwolf.talk.TalkVoteWolf;
import tkg.aiwolf.target.DivineBasicAvoidSeer;
import tkg.aiwolf.target.DivineByAI;
import tkg.aiwolf.target.RevoteMajority;
import tkg.aiwolf.target.VoteAsAnnouncedToLive;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Day;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;

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
