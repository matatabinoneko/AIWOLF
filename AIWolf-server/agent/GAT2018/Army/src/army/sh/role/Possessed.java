package army.sh.role;

import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import army.sh.base.BaseRole;
import army.sh.talk.TalkFactory;

public class Possessed extends BaseRole {
	


	@Override
	public void dayStart() {
		super.dayStart();
		if(!isCO){
			talkQueue.add(TalkFactory.comingout(me, Role.SEER));
		}
		List<Agent> list = getBitList((short) (bitAlive & invBit(bitMe)));
		talkQueue.add(TalkFactory.comingout(me, Role.SEER));
		if (!list.isEmpty()) {
			talkQueue.add(TalkFactory.divinedResult(list.get(0), Species.WEREWOLF));
		}

	}

}
