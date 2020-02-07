package jp.or.plala.amail.rin0114.aiwolf.role;

import jp.or.plala.amail.rin0114.aiwolf.framework.Day;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkCO5PosLast3PP;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkCo;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineBlackForMedium;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineBlackForWhite;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineBlackRandom;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineBlackSeerDay3;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkDivineFakeBlackDay1;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteForFakeBlack;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteForWhite;
import jp.or.plala.amail.rin0114.aiwolf.talk.TalkVoteMajority;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteAccordingToMyself;
import jp.or.plala.amail.rin0114.aiwolf.target.VoteLast3PPTactic;

import org.aiwolf.common.data.Role;

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
