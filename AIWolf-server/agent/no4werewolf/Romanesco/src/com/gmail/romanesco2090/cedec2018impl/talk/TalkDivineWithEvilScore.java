package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;
import com.gmail.romanesco2090.util.Utils;

/**
 * COしてたら占い結果を言う
 * 3日目以降、結果が人間だった場合、怪しいやつを適当に黒出し
 */
public class TalkDivineWithEvilScore extends TFAFTalkTactic {

	private Set<GameAgent> divinedAnnounced = new HashSet<>();

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
		if (divineTarget != null && game.getSelf().hasCO()) {
			if (game.getDay() <= 3) {
				//3日目までは素直に結果を報告
				divinedAnnounced.add(divineTarget);
				return new DivinedResultContentBuilder(divineTarget.agent, result);
			} else {
				List<GameAgent> meidum = game.getAgentStream().filter(ag -> ag.coRole == Role.MEDIUM)
						.collect(Collectors.toList());
				boolean mediumActive = meidum.size() == 1 && meidum.get(0).isAlive;
				//3日目以降、結果が人間だった場合、怪しいやつを適当に黒出し;ただし、Mediumが生きている場合には控える。また、WEREWOLFを2匹以上見つけている場合には、素直に結果を言う
				if (result == Species.HUMAN && !mediumActive && wereWolfFound <= 1 && game.getDay() <= 5) {
					List<GameAgent> alives = game.getAliveOthers();
					alives.removeAll(divinedAnnounced);//既に占い宣言済みの人は対象にしない
					double[] evilScore = model.getEvilScore();
					Utils.sortByScore(alives, evilScore, false);
					GameAgent target = alives.get(0);
					model.currentVoteTarget = target;
					divinedAnnounced.add(target);
					return new DivinedResultContentBuilder(target.agent, Species.WEREWOLF);
				} else {
					if (result == Species.WEREWOLF) {
						model.currentVoteTarget = divineTarget;//黒出しした人に責任持って投票する
					}
					divinedAnnounced.add(divineTarget);
					return new DivinedResultContentBuilder(divineTarget.agent, result);
				}
			}
		}
		return null;
	}

	private GameAgent divineTarget;
	private Species result;
	private int wereWolfFound;

	@Override
	public void handleEvent(Game g, GameEvent e) {
		if (e.type == EventType.DIVINE) {
			divineTarget = e.target;
			result = e.species;
			if (result == Species.WEREWOLF)
				wereWolfFound++;
		}
	}

}