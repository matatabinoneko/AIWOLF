package com.gmail.k14.itolab.aiwolf.role;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.action.SeerAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRole;
import com.gmail.k14.itolab.aiwolf.data.GameResult;
import com.gmail.k14.itolab.aiwolf.util.Check;

/**
 * 占い師
 * @author k14096kk
 *
 */
public class Seer extends BaseRole {
	
	public Seer(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		super.initialize(paramGameInfo, paramGameSetting);
		
		action = new SeerAction(entityData);
	}

	@Override
	public void update(GameInfo paramGameInfo) {
		super.update(paramGameInfo);
		
	}

	@Override
	public void dayStart() {
		super.dayStart();
		// 0日目以外で占い結果格納処理がくれば，全てnullのジャッジを作成して格納する
		if (!Check.isNum(entityData.getOwnData().getDay(), 0) && entityData.getOwnData().getLatestDivineResult() == null) {
			Judge emptyJudge = new Judge(entityData.getOwnData().getDay(), null, null, null);
			entityData.getOwnData().setDivineResultMap(emptyJudge);
		}
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
	public Agent divine() {
		return super.divine();
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
