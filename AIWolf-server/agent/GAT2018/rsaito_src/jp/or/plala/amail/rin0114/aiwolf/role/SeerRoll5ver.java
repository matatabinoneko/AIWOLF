package jp.or.plala.amail.rin0114.aiwolf.role;

import java.util.HashSet;
import java.util.Set;

import jp.or.plala.amail.rin0114.aiwolf.framework.Day;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.GameAgent;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkCo;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineWithEvilScore5ver;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteWolfbySeer5ver;
import jp.or.plala.amail.rin0114.aiwolf.target.DivineBasic;
import jp.or.plala.amail.rin0114.aiwolf.target.DivineByAI;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteWolfbySeer;

import org.aiwolf.common.data.Role;

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
