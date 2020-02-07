package com.gmail.romanesco2090.framework;

/**
 * 様々なGameEventを受け取るリスナー
 */
public interface GameEventListenr {

    void handleEvent(Game g, GameEvent e);

}
