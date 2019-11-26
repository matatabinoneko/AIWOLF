package tera.aiwolf.framework;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import static tera.aiwolf.util.Utils.*;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.SkipContentBuilder;

/**
 * Playerクラスの抽象実装。これを継承したクラスを作成し、Runnerのクラス名として指定するようにする。
 */
public abstract class AbstractPlayer implements Player {

    public AbstractPlayer() {
        this.meta = createMetagameModel();
    }

    protected abstract MetagameModel createMetagameModel();

    @Override
    public abstract String getName();

    public MetagameModel getMeta() {
        return meta;
    }

    public AbstractRole getRole() {
        return role;
    }

    public Game getGame() {
        return game;
    }

    private MetagameModel meta;
    private Game game;
    private AbstractRole role;
    private boolean vote_flag = false;
    private boolean attack_vote_flag = false;
    private int talkTurn = 0;
    private int whisperTurn = 0;

    @Override
    public void update(GameInfo gi) {
        try {
            log("[UPDATE]");
            game.updateWorld(gi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * そのゲームで使うAbstractRoleの実装を返す。例えば、5人村用の狼実装、等。
     *
     * @param game
     * @return そのゲームで使うAbstractRoleの実装を返す。
     */
    protected abstract AbstractRole selectRole(Game game);

    @Override
    public void initialize(GameInfo gi, GameSetting gs) {
        try {
            log("[INITIALIZE]");
            game = new Game(meta, gi, gs);
            role = selectRole(game);
            game.setCurrentRole(role);
            log("Selected Role", role);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dayStart() {
        try {
            log("[DAYSTART]");
            game.dayStart();
            this.talkTurn = 0;
            this.whisperTurn = 0;
            this.vote_flag = false;
            this.attack_vote_flag = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String talk() {
        try {
            String msg = role.talk(talkTurn);
            log("[TALK]", talkTurn, msg);
            talkTurn++;
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Content(new SkipContentBuilder()).getText();
    }

    @Override
    public String whisper() {
        try {
            String msg = role.whisper(whisperTurn);
            log("[WHISPER]", whisperTurn, msg);
            whisperTurn++;
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Content(new SkipContentBuilder()).getText();
    }

    @Override
    public Agent vote() {
        try {
            Agent target;
            if (vote_flag) {
                target = role.revote();
                game.notifyRevote();
                log("[REVOTE]", target);
            } else {
                target = role.vote();
                vote_flag = true;
                log("[VOTE]", target);
            }
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Agent attack() {
        try {
            Agent target;
            if (attack_vote_flag) {
                target = role.reattack();
                game.notifyAtkRevote();
                log("[REATTACK]", target);
            } else {
                target = role.attack();
                attack_vote_flag = true;
                log("[ATTACK]", target);
            }
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Agent divine() {
        try {
            Agent target = role.divine();
            log("[DIVINE]", target);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Agent guard() {
        try {
            Agent target = role.guard();
            log("[GUARD]", target);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void finish() {
        try {
            log("[FINISH]");
            this.game.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
