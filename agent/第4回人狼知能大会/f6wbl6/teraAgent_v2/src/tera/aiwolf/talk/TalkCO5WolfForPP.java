package tera.aiwolf.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.model.TFAFGameModel;

public class TalkCO5WolfForPP extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        if (!game.getSelf().hasCO()) {
            if ((game.getAgentStream().anyMatch(ag -> ag.coRole == Role.POSSESSED && ag.isAlive)//狂人告白者が生存しているか
                || game.getAllTalks().anyMatch(t //狂人占いをした発言者が生きている場合(占い発言をした生存者で、1.自分に村人判定 or 2.自分以外に人狼判定した人がいた場合)
                    -> (t.getTopic() == Topic.DIVINED
                && t.getTalker().isAlive
                && ((t.getTarget().isSelf && t.getResult() == Species.HUMAN)
                || (!t.getTarget().isSelf && t.getResult() == Species.WEREWOLF))) //この場合, cndlベースでは白以外に黒判定を出すため無意味に人狼発言してしまう
                )) && game.getAllTalks().anyMatch(t -> (t.getDay()==2) && ((t.getRole()==Role.POSSESSED) || (t.getRole()==Role.WEREWOLF))))
            	// COは裏切りと人狼COを待つ
                return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED); //人狼COするよりも裏切りCOの方が安全
        }
        return null;
    }

}
