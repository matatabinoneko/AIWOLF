package army.sh.role;

import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import army.sh.base.BaseRole;

public class Werewolf extends BaseRole {

	@Override
	public Agent attack() {
		if (gameInfo.getDay() == 1) {
			List<Agent> list = getBitList((short) (bitAlive & invBit(bitMe) & bitCO));
			if (!list.isEmpty()) {
				return list.get(0);
			}
		} else if (gameInfo.getDay() == 2) {
			List<Agent> list = getBitList((short) (bitAlive & invBit(bitMe) & invBit(bitCO)));
			if (!list.isEmpty()) {
				return list.get(0);
			}
		}
		return super.attack();
	}

}
