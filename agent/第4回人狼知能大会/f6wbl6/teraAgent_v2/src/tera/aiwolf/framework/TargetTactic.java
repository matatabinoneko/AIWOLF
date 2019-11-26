package tera.aiwolf.framework;

/**
 * 行動のターゲット（投票先、襲撃先、占い先etc）を決定する戦術
 */
public abstract class TargetTactic implements Tactic {

    /**
     * 行動のターゲット（投票先、襲撃先、占い先etc）を返す。nullを返すと、次の優先順位のTacticが実行される。
     *
     * @param model
     * @param game
     * @return 対象のGameAgent
     */
    public abstract GameAgent target(GameModel model, Game game);

    @Override
    public void handleEvent(Game g, GameEvent e) {
    }

}
