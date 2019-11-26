package tera.aiwolf.role;

import java.util.HashSet;
import java.util.Set;

import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Day;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.talk.TalkCo;
import tera.aiwolf.talk.TalkDivineWithEvilScore5ver;
import tera.aiwolf.talk.TalkVoteWolfbySeer5ver;
import tera.aiwolf.target.DivineBasic;
import tera.aiwolf.target.DivineByAI;
import tera.aiwolf.target.VoteWolfbySeer;

public class SeerRoll5ver extends TFAFBaseRole {

    private Set<GameAgent> divined = new HashSet<>();

    public SeerRoll5ver(Game game) {
        super(game);

        //初日CO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        //白判定時、自分のモデルを信じて黒として伝える。
        talkTactics.add(new TalkDivineWithEvilScore5ver(), 9000, Day.any());
        talkTactics.add(new TalkVoteWolfbySeer5ver(), 8000, Day.any());

        //0日目は勝率の高いAIを占う
        divineTactics.add(new DivineByAI(divined), Day.on(0));
        //1日目以降はEvilScoreの高いエージェントを占う
        divineTactics.add(new DivineBasic(divined), Day.after(1));

        // 自分の黒判定に投票。白を除くもっとも狼らしいエージェントに投票
        voteTactics.add(new VoteWolfbySeer());
        revoteTactics.add(new VoteWolfbySeer());
    }

}
