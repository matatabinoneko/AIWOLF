package army.sh.role;

import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import army.sh.base.BaseRole;
import army.sh.gadget.Check;
import army.sh.talk.TalkFactory;

public class Seer extends BaseRole {


	short bitDivined = 0;

	@Override
	public void dayStart() {
		super.dayStart();
		if (gameInfo.getDay() == 3 && !isCO) {
			talkQueue.offer(TalkFactory.comingout(me, Role.SEER));
		}

		Judge judge = gameInfo.getDivineResult();
		if (Check.isNotNull(judge)) {
			if (judge.getResult() == Species.WEREWOLF) {
				voteTarget = judge.getTarget();
				if (!isCO) {
					talkQueue.add(TalkFactory.comingout(me, Role.SEER));
					isCO = true;
				}
				talkQueue.add(TalkFactory.divinedResult(judge.getTarget(), judge.getResult()));
			}
			bitDivined = (short) (bitDivined | getAgentBit(judge.getTarget()));
		}
		

	}

	@Override
	public Agent divine() {
		if (Check.isNull(divineTarget)) {
			List<Agent> list = getBitList((short) (bitAlive & invBit(bitCO) & invBit(bitMe)&invBit(bitDivined)));
			if(!list.isEmpty()){
				return list.get(0);
			}
		}
		return super.divine();
	}

	@Override
	public void finish() {
		super.finish();
		bitDivined = 0;
	}
}
