package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.util.Utils;

/**
 * EvilDcore最下位に黒発言をする
 */
public class TalkDivineBlackAttacktoNoSeer extends TFAFTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
		if (game.getSelf().hasCO()) {
			/* SEER としてCOしている者たち */
			Set<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER)
					.collect(Collectors.toSet());

			List<GameAgent> list = game.getAliveOthers().stream().filter(ag -> !seerCOs.contains(ag))
					.collect(Collectors.toList());

			Utils.sortByScore(list, model.getEvilScore(), false);
			GameAgent wolfCand = list.get(list.size() - 1);
			return new DivinedResultContentBuilder(wolfCand.agent, Species.WEREWOLF);
		}
		return null;
	}
}
