package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk.TalkCO5WolfForPP;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk.TalkVote5WolfDay1;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.talk.TalkVote5WolfDay2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target.AttackWithoutPOSSESSED;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target.RevoteToLive5Wolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.target.VoteToLive5Wolf;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Day;

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
        //特定裏切者を避けて襲撃
        attackTactics.add(new AttackWithoutPOSSESSED());
    }
}
