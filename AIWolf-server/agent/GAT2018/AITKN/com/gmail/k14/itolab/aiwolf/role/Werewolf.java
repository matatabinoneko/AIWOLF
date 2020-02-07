package com.gmail.k14.itolab.aiwolf.role;


import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.action.WerewolfAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRole;
import com.gmail.k14.itolab.aiwolf.data.GameResult;

/**
 * 人狼
 * @author k14096kk
 *
 */
public class Werewolf extends BaseRole {
	
	public Werewolf(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		super.initialize(paramGameInfo, paramGameSetting);
		
		action = new WerewolfAction(entityData);
	}

	@Override
	public void update(GameInfo paramGameInfo) {
		super.update(paramGameInfo);
	}

	@Override
	public void dayStart() {
		super.dayStart();
	}

	@Override
	public String talk() {
		return super.talk();
	}

	@Override
	public String whisper() {
		return super.whisper();
	}

	@Override
	public Agent vote() {
		return super.vote();
	}

	@Override
	public Agent attack() {
		return super.attack();
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void dataReset() {
		entityData.getMyWhisper().clearTalk();
		super.dataReset();
	}

}
