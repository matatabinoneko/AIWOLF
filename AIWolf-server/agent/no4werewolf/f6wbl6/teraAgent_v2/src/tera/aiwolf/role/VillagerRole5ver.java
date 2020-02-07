package tera.aiwolf.role;

import tera.aiwolf.framework.Day;
import tera.aiwolf.framework.Game;
import tera.aiwolf.talk.TalkVote5VillDay2;
import tera.aiwolf.talk.TalkVote5VillDay1;
import tera.aiwolf.target.VoteForWolf5ver;
import tera.aiwolf.target.VoteLast3PPTacticbyVil;

public class VillagerRole5ver extends TFAFBaseRole {
    public VillagerRole5ver(Game game) {
        super(game);

        // オリジナル
//        talkTactics.add(new TalkVote5VillDay1(), 10000, Day.on(1), Repeat.MULTI);
//        talkTactics.add(new TalkVote5VillDay2(), 10000, Day.on(2), Repeat.MULTI);

        // kasukaメソッド実装（発言戦略）
        talkTactics.add(new TalkVote5VillDay1(), 10000, Day.on(1), Repeat.MULTI);
        talkTactics.add(new TalkVote5VillDay2(), 10000, Day.on(2), Repeat.MULTI);

        // 投票戦略
        voteTactics.add(new VoteForWolf5ver(), Day.on(1));
        revoteTactics.add(new VoteForWolf5ver(), Day.on(1));

        // 最終日裏切者COを避けて投票する。
        voteTactics.add(new VoteLast3PPTacticbyVil(), Day.on(2));
        revoteTactics.add(new VoteLast3PPTacticbyVil(), Day.on(2));
    }
}
