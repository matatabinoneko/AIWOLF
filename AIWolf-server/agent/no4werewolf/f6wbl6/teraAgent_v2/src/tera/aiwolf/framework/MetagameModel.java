package tera.aiwolf.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * ゲームを超えた情報やモデルを保持するためのオブジェクト。1連のゲームで1インスタンス。
 */
public class MetagameModel {

    int numberOfGames = 0;

    /**
     * 現在何試合目かを返す（0始まり）
     */
    public int getNumberOfGames() {
        return numberOfGames;
    }

    private List<MetagameEventListener> eventListeners = new ArrayList<>();

    /**
     * 追加するとstartGameとendGameを受け取れるようになる。
     */
    public void addMetagameEventListener(MetagameEventListener mel) {
        eventListeners.add(mel);
    }

    void startGame(Game g) {
        for (MetagameEventListener mm : eventListeners) {
            mm.startGame(g);
        }
    }

    void finishGame(Game g) {
        for (MetagameEventListener mm : eventListeners) {
            mm.endGame(g);
        }
    }

}
