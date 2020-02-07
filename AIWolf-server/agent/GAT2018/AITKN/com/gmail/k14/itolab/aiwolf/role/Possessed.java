package com.gmail.k14.itolab.aiwolf.role;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.action.PaperPossessedAction;
//import com.gmail.k14.itolab.aiwolf.action.PossessedAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRole;
import com.gmail.k14.itolab.aiwolf.data.GameResult;

/**
 * 狂人
 * @author k14096kk
 *
 */
public class Possessed extends BaseRole {

	public Possessed(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		super.initialize(paramGameInfo, paramGameSetting);
		
//		action = new PossessedAction(entityData);
		action = new PaperPossessedAction(entityData);
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
	public Agent vote() {
		return super.vote();
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
