package tera.aiwolf.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.metagame.TFAFMetagameModel;
import tera.aiwolf.model.TFAFGameModel;

/**
 *
 * 人狼、CO以外からメタ勝率が低い奴を vote 対象と宣言
 */
public class TalkVoteWhiteWeak extends TFAFTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
        GameAgent me = game.getSelf();
        /* このターン vote していない時のみ発動 */
        if (me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE).collect(Collectors.toList()).isEmpty()) {
            List<GameAgent> agents = game.getAgents();
            agents.removeIf(x -> x.role == Role.WEREWOLF);
            double[] winCount = ((TFAFMetagameModel) game.getMeta()).winCountModel.getWinCount();
            GameAgent weak = Collections.max(agents, Comparator.comparing(x -> winCount[x.getIndex()]));
            return new VoteContentBuilder(weak.agent);
        }
        return null;
    }

}
