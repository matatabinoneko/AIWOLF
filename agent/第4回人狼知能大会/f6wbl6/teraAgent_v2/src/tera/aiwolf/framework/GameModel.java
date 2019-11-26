package tera.aiwolf.framework;

/**
 * 各種のモデルや、Tactics間で共有する状態を保持するオブジェクト。1試合につき1インスタンス。
 */
public abstract class GameModel {

    protected Game game;

    public GameModel(Game game) {
        this.game = game;
    }

}
