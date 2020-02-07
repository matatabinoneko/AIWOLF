package tera.aiwolf.role;

import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Day;
import tera.aiwolf.framework.Game;
import tera.aiwolf.talk.TalkCO5PosLast3PP;
import tera.aiwolf.talk.TalkCo;
import tera.aiwolf.talk.TalkDivineBlackForMedium;
import tera.aiwolf.talk.TalkDivineBlackForWhite;
import tera.aiwolf.talk.TalkDivineBlackRandom;
import tera.aiwolf.talk.TalkDivineBlackSeerDay3;
import tera.aiwolf.talk.TalkDivineFakeBlackDay1;
import tera.aiwolf.talk.TalkVoteForFakeBlack;
import tera.aiwolf.talk.TalkVoteForWhite;
import tera.aiwolf.talk.TalkVoteMajority;
import tera.aiwolf.target.VoteAccordingToMyself;
import tera.aiwolf.target.VoteLast3PPTactic;

public class PossessedRole extends TFAFBaseRole {

    public PossessedRole(Game game) {
        super(game);
        // 初日1ターン目、占いCO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        // 初日2ターン目、対抗占いに黒。対抗COがない場合・2人以上の場合は非COにテキトーに黒。
        talkTactics.add(new TalkDivineFakeBlackDay1(), 9000, Day.on(1));
        //初日3ターン目黒出しした相手を vote 対象に指名。
        talkTactics.add(new TalkVoteForFakeBlack(), 7000, Day.on(1));

        //2日目1ターン: 別の占い師から白出しされた奴に黒出し
        talkTactics.add(new TalkDivineBlackForWhite(), 10000, Day.on(2));
        //2日目1ターン目: さもなければ 霊能者に黒出し
        talkTactics.add(new TalkDivineBlackForMedium(), 9000, Day.on(2));
        //2日目1ターン目: さもなければテキトーに黒出し
        talkTactics.add(new TalkDivineBlackRandom(), 8000, Day.on(2));
        //2日目2ターン目黒出しした相手を vote 対象に指名。
        talkTactics.add(new TalkVoteForFakeBlack(), 7000, Day.on(2));

        //3日目: まだ占い師COに黒出ししてなければ、どちらかに黒出し
        talkTactics.add(new TalkDivineBlackSeerDay3(), 10000, Day.on(3));
        //3日目: さもなければテキトーに黒出し
        talkTactics.add(new TalkDivineBlackRandom(), 9000, Day.on(3));

        //3日目まで: 多数派に従って投票対象を変更
        talkTactics.add(new TalkVoteMajority(), 5000, Day.before(3), Repeat.MULTI);

        //4日目以降: これまでの黒判定は無視。Modelで人狼っぽく無い者をvote宣言
        talkTactics.add(new TalkVoteForWhite(), 10000, Day.after(4), Repeat.MULTI);

        //残り人数が3人になったら、狂人CO
        talkTactics.add(new TalkCO5PosLast3PP(), 15000, Day.any());

        //投票
        voteTactics.add(new VoteAccordingToMyself(), 10000);
        revoteTactics.add(new VoteAccordingToMyself(), 10000);
        voteTactics.add(new VoteLast3PPTactic(), 9000);
        revoteTactics.add(new VoteLast3PPTactic(), 9000);
    }

}
