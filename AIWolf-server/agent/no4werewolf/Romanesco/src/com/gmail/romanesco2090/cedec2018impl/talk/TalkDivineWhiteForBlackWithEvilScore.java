package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;

/**
 *	占っていなくてEvilScoreの高い人に白出し
 */
public class TalkDivineWhiteForBlackWithEvilScore extends TFAFTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
		GameAgent me = game.getSelf();
		GameAgent tar = null;
		if (!me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED)
				.collect(Collectors.toList()).isEmpty()) {
			return null;
		}
		Set<GameAgent> my_divine = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED)
				.map(x -> x.getTarget()).collect(Collectors.toSet());

		/* 占っていない生きている人 */
		List<GameAgent> targets = game.getAliveOthers().stream().filter(x -> !my_divine.contains(x))
				.collect(Collectors.toList());
		/* evelscoreの高い生き物に占い */
		tar = Collections.max(targets, Comparator.comparing(x -> model.getEvilScore()[x.getIndex()]));

		if (tar != null) {
			return new DivinedResultContentBuilder(tar.agent, Species.HUMAN);
		}
		return null;
	}

}
