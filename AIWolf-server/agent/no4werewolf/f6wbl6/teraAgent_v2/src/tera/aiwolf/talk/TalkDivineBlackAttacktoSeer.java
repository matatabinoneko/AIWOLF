package tera.aiwolf.talk;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.model.TFAFGameModel;
import tera.aiwolf.util.Utils;

public class TalkDivineBlackAttacktoSeer extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (game.getSelf().hasCO()) {
            // 対抗占いを真占いと推定し、狼判定を出す。対抗がいなければEvilDcore最下位に黒
            Agent seer = null;
            /* SEER としてCOしている者たち */
            Set<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toSet());
            if (seerCOs.size() == 1) {
                for (GameAgent gameAgent : seerCOs) {
                    seer = gameAgent.agent;
                }
            }
            if (seer != null) {
                return new DivinedResultContentBuilder(seer, Species.WEREWOLF);
            } else {
                List<GameAgent> list = game.getAliveOthers();
                Utils.sortByScore(list, model.getEvilScore(), false);
                GameAgent wolfCand = list.get(list.size() - 1);
                return new DivinedResultContentBuilder(wolfCand.agent, Species.WEREWOLF);
            }
        }
        return null;
    }
}
