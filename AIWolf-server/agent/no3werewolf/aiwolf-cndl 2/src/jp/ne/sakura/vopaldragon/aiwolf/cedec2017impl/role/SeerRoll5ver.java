package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role;

import java.util.HashSet;
import java.util.Set;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk.TalkCo;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk.TalkDivineWithEvilScore5ver;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk.TalkVoteWolfbySeer5ver;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target.DivineBasic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target.DivineByAI;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target.VoteWolfbySeer;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Day;
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
