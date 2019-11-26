package tkg.aiwolf.target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;
import tkg.aiwolf.model.TFAFGameModel;

public class AttackWithoutPOSSESSED extends TFAFTargetTactic {

	private Set<GameAgent> seers = new HashSet<>();
	private Set<GameAgent> possesseds = new HashSet<>();

	@Override
	public GameAgent targetImpl(TFAFGameModel model, Game game) {
		GameAgent me = game.getSelf();
		List<GameAgent> others = game.getAliveOthers();
		List<GameAgent> others2 = game.getAliveOthers();
		Utils.sortByScore(others, model.getEvilScore(), false);
		GameAgent target = others.get(others.size() - 1);
		List<GameEvent> talks = game.getEventAtDay(EventType.TALK, game.getDay());
		for (GameEvent evt : talks) {
			for (GameTalk talk : evt.talks) {
				if (talk.getTopic() == Topic.DIVINED && talk.getTarget() == me
						&& talk.getResult() != Species.WEREWOLF) {
					// 自分に白判定した自称占い師は裏切者
					possesseds.add(talk.getTalker());
					others.remove(talk.getTalker());
				}
				if (talk.getTopic() == Topic.DIVINED && talk.getTarget() != me
						&& talk.getResult() == Species.WEREWOLF) {
					// 自分以外に黒判定した自称占い師は裏切者
					possesseds.add(talk.getTalker());
					others.remove(talk.getTalker());
				}
				if (talk.getTopic() == Topic.DIVINED && !possesseds.contains(talk.getTalker())
						&& others.contains(talk.getTalker())) {
					// 裏切者でない自称占い師を占い師と推定、生存者のみ候補に。
					seers.add(talk.getTalker());
				}
			}
		}

		List<GameAgent> list = new ArrayList<>(seers);
		if (list.size() == 2) {
			// 推定占い師が２人生存している場合、生かす。

			GameAgent seer1 = list.get(0);
			GameAgent seer2 = list.get(1);
			if (others.contains(seer1) && others.contains(seer2)) {
				others.remove(seer1);
				others.remove(seer2);
				Utils.sortByScore(others, model.getEvilScore(), false);
				GameAgent agent = others.get(0);
				return agent;
			} else {
				/*
				 * if (others.contains(seer1)) { return seer1; } if (others.contains(seer2)) {
				 * return seer2; }
				 */
				// 裏切者が確定しない場合は、裏切者ランクの低い方を倒す。。
				Utils.sortByScore(list, model.getRoleProbability(Role.POSSESSED), false);
				if (others2.contains(list.get(list.size() - 1))) {
					return list.get(list.size() - 1);
				}
			}
		}
		if (list.size() == 1) {
			/*
			 * Utils.sortByScore(others2, model.getRoleProbability(Role.POSSESSED), false);
			 * if (list.get(0) != others2.get(0)) { return list.get(0); }
			 */
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
