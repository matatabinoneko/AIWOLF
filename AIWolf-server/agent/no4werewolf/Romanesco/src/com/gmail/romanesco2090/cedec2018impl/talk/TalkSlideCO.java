package com.gmail.romanesco2090.cedec2018impl.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

import com.gmail.romanesco2090.cedec2018impl.model.TFAFGameModel;
import com.gmail.romanesco2090.framework.Game;

/**
 * COしててもCOする
 */
public class TalkSlideCO extends TFAFTalkTactic {

	private Role role;

	public TalkSlideCO(Role role) {
		this.role = role;
	}

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, TFAFGameModel model, Game game) {
		return new ComingoutContentBuilder(game.getSelf().agent, role);
	}

}
