package jp.ac.shibaura_it.ma15082.player;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractPossessed;

public class WasabiPossessed extends AbstractPossessed {
	final Player player;

	WasabiPossessed(Player p) {
		player = p;
	}

	@Override
	public void dayStart() {
		player.dayStart();
	}

	@Override
	public void finish() {
		player.finish();
	}

	@Override
	public String getName() {
		return player.getName();
	}

	@Override
	public void initialize(GameInfo arg0, GameSetting arg1) {
		player.initialize(arg0, arg1);

	}

	@Override
	public String talk() {
		return player.talk();
	}

	@Override
	public void update(GameInfo arg0) {
		player.update(arg0);

	}

	@Override
	public Agent vote() {
		return player.vote();
	}
}
