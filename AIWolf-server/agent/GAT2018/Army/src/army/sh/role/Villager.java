package army.sh.role;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import army.sh.base.BaseRole;
import army.sh.talk.TalkFactory;

public class Villager extends BaseRole {

	Deque<Agent> voteQueue;

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		voteQueue = new ArrayDeque<>();
	}

	@Override
	public void dayStart() {
		super.dayStart();
		if (!isCO) {
			talkQueue.add(TalkFactory.comingout(me, myRole));
		}
		if (gameInfo.getDay() == 2) {
			List<Agent> list = getBitList((short) (invBit(bitCO) & invBit(bitMe) & bitAlive));
			list.forEach(agent -> voteQueue.add(agent));
		}

	}

	@Override
	public void update(GameInfo gameInfo) {
		super.update(gameInfo);

		if (!voteQueue.isEmpty()) {
			talkQueue.add(TalkFactory.vote(voteQueue.poll()));
		}

	}

	@Override
	public void finish() {
		super.finish();
		voteQueue.clear();
	}

}
