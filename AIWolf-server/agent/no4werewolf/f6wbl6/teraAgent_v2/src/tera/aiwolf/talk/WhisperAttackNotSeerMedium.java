package tera.aiwolf.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.EventType;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameEvent;
import tera.aiwolf.model.TFAFGameModel;

/**
 * 占い師・霊媒師CO、推定裏切り者以外から推定狩人率高い者にattack宣言
 */
public class WhisperAttackNotSeerMedium extends TFAFTalkTactic {

    private GameAgent possessed = null;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        /* すでにvote宣言をしたか */
        long vote_targets = me.whisperList.stream().filter(x -> x.getTopic() == Topic.ATTACK
            && x.getDay() == game.getDay() && x.getTarget().isAlive).count();

        if (game.getDay() == 0 || vote_targets > 0) {
            return null; // 0日目は襲撃できない、すでにattack宣言していたらしない。
        }
        List<GameAgent> alives = game.getAliveOthers().stream().filter(x -> x.role != Role.WEREWOLF && x.coRole != Role.SEER && x.coRole != Role.MEDIUM).collect(Collectors.toList());
        alives.remove(possessed);
        if (alives.isEmpty()) return null;
        double[] bodyGuardProbability = model.getRoleProbability(Role.BODYGUARD);
        GameAgent tar = Collections.max(alives, Comparator.comparing(ag -> bodyGuardProbability[ag.getIndex()]));
        return new AttackContentBuilder(tar.agent);
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.WHISPER) {
            e.talks.stream().filter(x -> x.getTopic() == Topic.ESTIMATE && x.getRole() == Role.POSSESSED)
                .forEach(x -> possessed = x.getTarget());
        }
    }

}
