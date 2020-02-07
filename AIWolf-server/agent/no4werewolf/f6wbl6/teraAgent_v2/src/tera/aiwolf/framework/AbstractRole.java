package tera.aiwolf.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.common.data.Agent;

import tera.aiwolf.util.Utils;

/**
 * 具体的な各役職の実装の抽象クラス。このクラスを拡張して役職を実装する。createModelで、利用するGameModelを返す必要がある。
 */
public abstract class AbstractRole implements GameEventListenr {

    private Game game;
    private GameModel model;

    /**
     * ゲームモデルを返す。必要に応じてキャストして返す。
     *
     */
    public <T extends GameModel> T getModel() {
        return (T) model;
    }

    public AbstractRole(Game game) {
        this.game = game;
        model = createModel(game);
        game.addGameEventListener(this);//EventListenerに登録
    }

    /**
     * ゲームモデルを生成して返す。AbstractRoleのコンストラクタ内で呼ばれ、このゲーム中に使うGameModelが決まる。
     */
    protected abstract GameModel createModel(Game game);

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DAYSTART) {
            //一日一回発言のカウンタをリセット
            allTacticTimings.forEach(tt -> tt.startDay());
        }
    }

    private List<TacticTiming> allTacticTimings = new ArrayList<>();

    public enum Repeat {
        ONCE, MULTI

    }

    public class TacticList<T extends Tactic> {

        private List<TacticTiming<T>> tacticTimings = new ArrayList<>();

        /**
         * 優先度最大、日付指定無し、繰り返し無しでTacticを追加する
         */
        public void add(T e) {
            add(e, Integer.MAX_VALUE, null, Repeat.ONCE);
        }

        /**
         * 日付指定、優先度最大、繰り返し無しでTacticを追加する
         */
        public void add(T e, Day day) {
            add(e, Integer.MAX_VALUE, day, Repeat.ONCE);
        }

        /**
         * 優先度指定、日付指定無し、繰り返し無しでTacticを追加する
         */
        public void add(T e, int priority) {
            add(e, priority, null, Repeat.ONCE);
        }

        /**
         * 優先度・日付指定、繰り返し無しでTacticを追加する
         */
        public void add(T e, int priority, Day day) {
            add(e, priority, day, Repeat.ONCE);
        }

        /**
         * 優先度・日付・繰り返しを指定してTacticを追加する
         */
        public void add(T e, int priority, Day day, Repeat repeat) {
            game.addGameEventListener(e);
            FixedTacticTiming<T> timing = new FixedTacticTiming<>(e);
            timing.day = day;
            timing.isOnceADay = repeat == Repeat.ONCE;
            tacticTimings.add(timing);
            allTacticTimings.add(timing);
        }

        /**
         * 優先度順位・起動条件を定める自前クラスでTacticを追加する
         */
        public void add(TacticTiming<T> timing) {
            game.addGameEventListener(timing.tactic);
            tacticTimings.add(timing);
            allTacticTimings.add(timing);
        }

    }

    protected final TacticList<TalkTactic> talkTactics = new TacticList<>();
    protected final TacticList<TalkTactic> whisperTactics = new TacticList<>();
    protected final TacticList<TargetTactic> divineTactics = new TacticList<>();
    protected final TacticList<TargetTactic> voteTactics = new TacticList<>();
    protected final TacticList<TargetTactic> revoteTactics = new TacticList<>();
    protected final TacticList<TargetTactic> attackTactics = new TacticList<>();
    protected final TacticList<TargetTactic> reattackTactics = new TacticList<>();
    protected final TacticList<TargetTactic> guardTactics = new TacticList<>();

    private int skipCount = 0;
    private int utterCount = 0;

    private String genContent(int turn, TacticList<TalkTactic> talkTactics) {
        int day = game.getDay();
        Collections.sort(talkTactics.tacticTimings, (c1, c2) -> c1.getPriority(day, turn, skipCount, utterCount, model, game) - c2.getPriority(day, turn, skipCount, utterCount, model, game));
        for (TacticTiming<TalkTactic> tt : talkTactics.tacticTimings) {
            try {
                if (tt.willWork(day, turn, day, turn, model, game)) {
                    ContentBuilder content = tt.tactic.talk(turn, skipCount, utterCount, model, game);
                    if (content != null) {
                        skipCount = 0;
                        utterCount++;
                        tt.worked();
                        return new Content(content).getText();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        skipCount++;
        return new Content(new SkipContentBuilder()).getText();
    }

    private Agent selectAgent(TacticList<TargetTactic> targetTactics) {
        int day = game.getDay();
        Collections.sort(targetTactics.tacticTimings, (c1, c2) -> c1.getPriority(day, 0, 0, 0, model, game) - c2.getPriority(day, 0, 0, 0, model, game));
        for (TacticTiming<TargetTactic> tt : targetTactics.tacticTimings) {
            try {
                if (tt.willWork(day, 0, 0, 0, model, game)) {
                    GameAgent target = tt.tactic.target(model, game);
                    if (target != null) return target.agent;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return Utils.getRandom(game.getAliveOthers()).agent;
    }

    String talk(int turn) {
        return genContent(turn, talkTactics);
    }

    String whisper(int turn) {
        return genContent(turn, whisperTactics);
    }

    Agent vote() {
        return selectAgent(voteTactics);
    }

    Agent revote() {
        return selectAgent(revoteTactics);
    }

    Agent attack() {
        return selectAgent(attackTactics);
    }

    Agent reattack() {
        return selectAgent(reattackTactics);
    }

    Agent divine() {
        return selectAgent(divineTactics);
    }

    Agent guard() {
        return selectAgent(guardTactics);
    }
}
