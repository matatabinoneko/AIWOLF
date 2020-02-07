package com.gmail.naglfar.the.on.framework;

/**
 * 様々なGameEventを受け取るリスナー
 */
public interface GameEventListenr {

    void handleEvent(Game g, GameEvent e);

}
