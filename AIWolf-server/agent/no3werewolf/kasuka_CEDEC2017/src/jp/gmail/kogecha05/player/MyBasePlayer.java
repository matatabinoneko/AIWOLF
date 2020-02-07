package jp.gmail.kogecha05.player;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class MyBasePlayer implements Player {
	Player player5;
	Player player15;

	GameSetting currentGameSetting;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		currentGameSetting = gameSetting;
		try {
			if (currentGameSetting.getPlayerNum() == 5) player5.initialize(gameInfo, gameSetting);
			else player15.initialize(gameInfo, gameSetting);
		}
		catch (Exception e) {

		}
	}

	public void update(GameInfo gameInfo) {
		try {
			if (currentGameSetting.getPlayerNum() == 5) player5.update(gameInfo);
			else player15.update(gameInfo);
		} catch (Exception e) {

		}
	}

	public void dayStart() {
		try {
			if (currentGameSetting.getPlayerNum() == 5) player5.dayStart();
			else player15.dayStart();
		} catch (Exception e) {

		}
	}

	public void finish() {
		try {
			if (currentGameSetting.getPlayerNum() == 5) player5.finish();
			else player15.finish();
		} catch (Exception e) {

		}
	}

	public String talk() {
		String res = null;
		try {
			if (currentGameSetting.getPlayerNum() == 5) res = player5.talk();
			else res = player15.talk();
		} catch (Exception e) {

		}
		return res;
	}

	public String whisper() {
		String res = null;
		try {
			if (currentGameSetting.getPlayerNum() == 5) res = player5.whisper();
			else res = player15.whisper();
		} catch (Exception e) {

		}
		return res;
	}

	public Agent vote() {
		Agent res = null;
		try {
			if (currentGameSetting.getPlayerNum() == 5) res = player5.vote();
			else res = player15.vote();
		} catch (Exception e) {

		}
		return res;
	}

	public Agent attack() {
		Agent res = null;
		try {
			if (currentGameSetting.getPlayerNum() == 5) res = player5.attack();
			else res = player15.attack();
		} catch (Exception e) {

		}
		return res;
	}

	public Agent divine() {
		Agent res = null;
		try {
			if (currentGameSetting.getPlayerNum() == 5) res = player5.divine();
			else res = player15.divine();
		} catch (Exception e) {

		}
		return res;
	}

	public Agent guard() {
		Agent res = null;
		try {
			if (currentGameSetting.getPlayerNum() == 5) res = player5.guard();
			else res = player15.guard();
		} catch (Exception e) {

		}
		return res;
	}

	public String getName() {
		return "kasuka";
	}
}

