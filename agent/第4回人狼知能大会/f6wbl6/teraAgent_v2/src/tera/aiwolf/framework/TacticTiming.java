package tera.aiwolf.framework;


/**
 * Tacticの動作タイミングを規定する抽象クラス。細かい制御をしたい場合、これを継承したクラスで実施すること。
 */
public abstract class TacticTiming<T extends Tactic> {

    public TacticTiming(T tactic) {
        this.tactic = tactic;
    }

    /**
     * 戦術の優先度を返す。これが最も高いものから順に実行される
     *
     * @return 優先度
     */
    protected abstract int getPriority(int day, int turn, int skip, int utter, GameModel model, Game game);

    /**
     * 戦術が実行されるかどうかを返す。falseの場合、当該戦術は実行されない。
     *
     * @return 優先度
     */
    protected abstract boolean willWork(int day, int turn, int skip, int utter, GameModel model, Game game);

    T tactic;

    protected void startDay() {
    }

    protected void worked() {
    }

}
