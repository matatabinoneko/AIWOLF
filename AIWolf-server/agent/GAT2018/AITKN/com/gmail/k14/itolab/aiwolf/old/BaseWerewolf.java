package com.gmail.k14.itolab.aiwolf.old;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.base.BaseRole;
import com.gmail.k14.itolab.aiwolf.data.GameResult;

/**
 * 人狼のベースクラス
 * @author k14096kk
 *
 */
public class BaseWerewolf extends BaseRole {

	public BaseWerewolf(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		super.initialize(paramGameInfo, paramGameSetting);
	}

	@Override
	public void update(GameInfo paramGameInfo) {
		// TODO 自動生成されたメソッド・スタブ
		super.update(paramGameInfo);
	}

	@Override
	public void dayStart() {
		// TODO 自動生成されたメソッド・スタブ
		super.dayStart();
	}

	@Override
	public String talk() {
		// TODO 自動生成されたメソッド・スタブ
		return super.talk();
	}

	@Override
	public String whisper() {
		// TODO 自動生成されたメソッド・スタブ
		return super.whisper();
	}

	@Override
	public Agent vote() {
		// TODO 自動生成されたメソッド・スタブ
		return super.vote();
	}

	@Override
	public Agent attack() {
		// TODO 自動生成されたメソッド・スタブ
		return super.attack();
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ
		super.finish();
	}

	@Override
	public void decideBoat() {
		// TODO 自動生成されたメソッド・スタブ
		super.decideBoat();
	}

	@Override
	public void dataReset() {
		super.dataReset();
	}

}
