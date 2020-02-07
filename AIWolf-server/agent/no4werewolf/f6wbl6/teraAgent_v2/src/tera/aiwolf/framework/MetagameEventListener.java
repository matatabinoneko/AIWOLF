package tera.aiwolf.framework;

/**
 * ゲーム終了と開始のイベントを受け取るリスナー
 */
public interface MetagameEventListener {

    /**
     * ゲーム終了時に1回呼ばれる。GameAgentでは全ての役職が判るようになっている。
     *
     * @param g
     */
    void endGame(Game g);

    /**
     * ゲーム開始時に1回呼ばれる。
     *
     * @param g
     */
    void startGame(Game g);
}
