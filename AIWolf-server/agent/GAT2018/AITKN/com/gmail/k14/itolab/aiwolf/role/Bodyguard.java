package com.gmail.k14.itolab.aiwolf.role;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.action.BodyguardAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRole;
import com.gmail.k14.itolab.aiwolf.data.GameResult;

/**
 * 狩人
 * @author k14096kk
 *
 */
public class Bodyguard extends BaseRole {
	
	public Bodyguard(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		super.initialize(paramGameInfo, paramGameSetting);
		
		action = new BodyguardAction(entityData);
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
	public Agent vote() {
		return super.vote();
	}

	@Override
	public Agent guard() {
		return super.guard();
	}

	@Override
	public void finish() {
		super.finish();
	}
	
	@Override
	public void dataReset() {
		super.dataReset();
	}
	
}
