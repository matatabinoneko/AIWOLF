package com.gmail.k14.itolab.aiwolf.old;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.base.BaseRole;
import com.gmail.k14.itolab.aiwolf.data.GameResult;

/**
 * 狂人のベースクラス
 * @author k14096kk
 *
 */
public class BasePossessed extends BaseRole {

	public BasePossessed(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		// TODO 自動生成されたメソッド・スタブ
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
	public Agent vote() {
		// TODO 自動生成されたメソッド・スタブ
		return super.vote();
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
		// TODO 自動生成されたメソッド・スタブ
		super.dataReset();
	}
}
