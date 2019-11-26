package tkg.aiwolf.role;

import jp.ne.sakura.vopaldragon.aiwolf.framework.Day;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import tkg.aiwolf.talk.TalkCO5WolfForPP;
import tkg.aiwolf.talk.TalkVote5WolfDay1;
import tkg.aiwolf.talk.TalkVote5WolfDay2;
import tkg.aiwolf.target.AttackWithoutPOSSESSED;
import tkg.aiwolf.target.RevoteToLive5Wolf;
import tkg.aiwolf.target.VoteToLive5Wolf;

public class WerewolfRole5ver extends TFAFBaseRole {

    public WerewolfRole5ver(Game game) {
        super(game);
        //最終日に裏切者COがあったら狼CO
        talkTactics.add(new TalkCO5WolfForPP(), 10000, Day.on(2));

        talkTactics.add(new TalkVote5WolfDay1(), 9000, Day.on(1), Repeat.MULTI);
        talkTactics.add(new TalkVote5WolfDay2(), 9000, Day.on(2), Repeat.MULTI);

        //自分以外に黒がいれば投票。最大得票数に乗っかる
        voteTactics.add(new VoteToLive5Wolf());
        //再投票なら最大得票数に乗っかる
        revoteTactics.add(new RevoteToLive5Wolf());
        //特定裏切者を避けて襲撃
        attackTactics.add(new AttackWithoutPOSSESSED());
    }
}
