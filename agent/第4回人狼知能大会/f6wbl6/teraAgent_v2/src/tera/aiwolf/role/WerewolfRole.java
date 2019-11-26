package tera.aiwolf.role;

import tera.aiwolf.framework.Day;
import tera.aiwolf.framework.Game;
import tera.aiwolf.talk.TalkVoteLastMaxHate;
import tera.aiwolf.talk.TalkVoteWhiteMajority;
import tera.aiwolf.talk.TalkVoteWhiteWeak;
import tera.aiwolf.talk.WhisperAttackHateCount;
import tera.aiwolf.talk.WhisperAttackNotSeerMedium;
import tera.aiwolf.talk.WhisperCOVillager;
import tera.aiwolf.talk.WhisperEstimatePossessed;
import tera.aiwolf.target.AttackAccordingToMyself;
import tera.aiwolf.target.VoteAccordingToMyself;

public class WerewolfRole extends TFAFBaseRole {

    public WerewolfRole(Game game) {
        super(game);

        /* Whisper */
        // 0日目以降: [2] 村人CO宣言
        whisperTactics.add(new WhisperCOVillager(), 10000, Day.on(0));
        // TODO 0日目: 占い師を騙って人狼を占うという仲間がいたら、DISAGREE する。
        // 身内以外に黒出し / 身内に白出しした占い師がいたら estimate POSSESSED
        whisperTactics.add(new WhisperEstimatePossessed(), 9000, Day.any());
        // TODO 1日目: メタ読みで狩人っぽい奴がいたら、 estimate 狩人
        // TODO 人狼陣営へのvote を行ってくるエージェントのヘイトをカウントしてattack宣言
        whisperTactics.add(new WhisperAttackHateCount(), 8000, Day.any());
        // 狩人 > 占い師・霊媒師CO以外から狩人率高い者へattack宣言
        whisperTactics.add(new WhisperAttackNotSeerMedium(), 7000, Day.any());

        /* Attck */
        // 基本的に宣言通りにAttack
        // 人狼が1人の時はWhisperが発生しないので、
        // これまで自分たちへのvoteカウントの大きかった生き物attack
        attackTactics.add(new AttackAccordingToMyself());
        reattackTactics.add(new AttackAccordingToMyself());


        /* Talk */
 /*  村人エージェントに似せる */
        // 1日目1ターン目: 人狼、CO以外からメタ勝率が低い奴を vote 対象と宣言
        talkTactics.add(new TalkVoteWhiteWeak(), 10000, Day.on(1));
        // 2日目以降1ターン目: これまでのvoteカウントの大きかった生き物にvote宣言
        talkTactics.add(new TalkVoteLastMaxHate(), 10000, Day.on(2));
        /* 2ターン目以降: 
         * 身内以外のvote先をカウント。
         * 最多得票先が人狼なら、票が入っている村人にvote宣言、そうでなければ乗っかる。 */
        talkTactics.add(new TalkVoteWhiteMajority(), 9000, Day.any(), Repeat.MULTI);

        /* Vote */
        // 基本的に宣言通り
        voteTactics.add(new VoteAccordingToMyself());
        revoteTactics.add(new VoteAccordingToMyself());
    }

}
