package com.gmail.romanesco2090.cedec2018impl.target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aiwolf.client.lib.Topic;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;
import com.gmail.romanesco2090.framework.GameTalk;
import com.gmail.romanesco2090.util.Utils;

public class AttackWithoutCOSeer extends TFAFTargetTactic {

	private Set<GameAgent> seers = new HashSet<>();

	@Override
	public GameAgent targetImpl(TFAFGameModel model, Game game) {
		List<GameAgent> others = game.getAliveOthers();
		Utils.sortByScore(others, model.getEvilScore(), false);
		GameAgent target = others.get(others.size() - 1);
		List<GameEvent> talks = game.getEventAtDay(EventType.TALK, game.getDay());
		for (GameEvent evt : talks) {
			for (GameTalk talk : evt.talks) {
				if (talk.getTopic() == Topic.DIVINED) {
					// 自分に白判定した自称占い師は裏切者
					seers.add(talk.getTalker());
					others.remove(talk.getTalker());
				}
			}
		}

		List<GameAgent> list = new ArrayList<>(seers);

		if (list.size() == 2) {
			// 推定占い師が２人生存している場合、生かす。
			GameAgent seer1 = list.get(0);
			GameAgent seer2 = list.get(1);
			if (!others.isEmpty()) {
				if (others.contains(seer1) && others.contains(seer2)) {
					others.remove(seer1);
					others.remove(seer2);
					Utils.sortByScore(others, model.getEvilScore(), false);
					GameAgent agent = others.get(0);
					return agent;
				}
				GameAgent agent = others.get(0);
				return agent;
			}
		}
		if (list.size() == 1) {
			// 推定占い師が１人生存時、襲撃してしまう。
			return list.get(0);
		}

		if (!others.isEmpty()) {
			// 特定裏切者をのぞいてもっとも村らしい村人を襲撃する。
			Utils.sortByScore(others, model.getEvilScore(), false);
			GameAgent agent = others.get(others.size() - 1);
			return agent;
		} else {
			return target;
		}
	}
}
