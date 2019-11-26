package tera.aiwolf.framework;

import org.aiwolf.client.lib.ContentBuilder;

/**
 * 会話の戦術
 */
public abstract class TalkTactic implements Tactic {

    /**
     * 会話内容を返す。nullを返すと、次の優先順位のTacticが実行される。
     *
     * @param turn
     * @param skip
     * @param utter
     * @param model
     * @param game
     * @return 会話内容
     */
    public abstract ContentBuilder talk(int turn, int skip, int utter, GameModel model, Game game);

    @Override
    public void handleEvent(Game g, GameEvent e) {
    }

}
