package com.gmail.romanesco2090.cedec2018impl.talk;

import java.util.ArrayDeque;
import java.util.Queue;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.common.data.Species;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.GameAgent;
import com.gmail.romanesco2090.framework.GameEvent;

/**
 * COしていたら今までの結果を全て報告
 */
public class TalkIdentifiedResultAll extends TFAFTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
		if (game.getSelf().hasCO() && !targets.isEmpty() && !results.isEmpty()) {
			IdentContentBuilder icb = new IdentContentBuilder(targets.poll().agent, results.poll());
			return icb;
		}
		return null;
	}

	private Queue<GameAgent> targets = new ArrayDeque<>();
	private Queue<Species> results = new ArrayDeque<>();

	@Override
	public void handleEvent(Game g, GameEvent e) {
		if (e.type == EventType.MEDIUM) {
			targets.offer(e.target);
			results.offer(e.species);
		}
	}

}
