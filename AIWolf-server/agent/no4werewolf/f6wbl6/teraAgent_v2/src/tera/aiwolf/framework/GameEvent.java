package tera.aiwolf.framework;

import java.util.List;

import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Species;

import tera.aiwolf.util.Utils;

/**
 * 発生するイベント情報を保持する構造体
 */
public class GameEvent {

    /**
     * イベント種別
     *
     * @see EventType
     */
    public EventType type;
    /**
     * イベントの起きた日
     */
    public int day;
    /**
     * イベントの対象エージェントを返す（ATTACK/EXECUTE/DIVINE/MEDIUM/VICTIM_DECIDED/GUARD;ATTACK/EXECUTE以外は当該役職以外の人には判らない）。
     */
    public GameAgent target;
    /**
     * 判定結果（DIVINE/MEDIUM）。
     */
    public Species species;
    /**
     * 投票内容（VOTE/ATTACK_VOTE）
     */
    public List<GameVote> votes;
    /**
     * 会話内容（TALK/WHISPER）
     */
    public List<GameTalk> talks;

    GameEvent(EventType type, int day) {
        this.type = type;
        this.day = day;
    }

    GameEvent(EventType type, int day, List<GameVote> votes) {
        this.type = type;
        this.day = day;
        this.votes = votes;
    }

    GameEvent(EventType type, List<GameTalk> talks) {
        this.type = type;
        this.day = talks.get(0).getDay();
        this.talks = talks;
    }

    GameEvent(EventType type, Game game, Judge judge) {
        this.type = type;
        this.day = judge.getDay() - 1;
        this.species = judge.getResult();
        this.target = game.toGameAgent(judge.getTarget());
    }

    GameEvent(EventType type, int day, GameAgent target) {
        this.type = type;
        this.day = day;
        this.target = target;
    }

    @Override
    public String toString() {
        return Utils.toString(this);
    }

}
