package com.gmail.romanesco2090.cedec2018impl.role;

import java.util.HashSet;
import java.util.Set;

import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.talk.TalkCO;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkDivineWithEvilScore5ver;
import com.gmail.romanesco2090.cedec2018impl.talk.TalkVoteWolfbySeer5ver;
import com.gmail.romanesco2090.cedec2018impl.target.DivineBasic;
import com.gmail.romanesco2090.cedec2018impl.target.DivineByAI;
import com.gmail.romanesco2090.cedec2018impl.target.VoteWolfbySeer;
import com.gmail.romanesco2090.framework.Day;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;

public class SeerRoll5ver extends TFAFBaseRole {

    private Set<GameAgent> divined = new HashSet<>();

    public SeerRoll5ver(Game game) {
        super(game);

        //初日CO
        talkTactics.add(new TalkCO(Role.SEER), 10000, Day.on(1));
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
