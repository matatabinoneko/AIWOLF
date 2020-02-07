package army.sh.info;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class OwnData {

	GameInfo gameInfo;
	GameSetting gameSetting;
	Agent me;
	Role myRole;
	
	List<Talk> talkList;
	short actFlag;
	
	Agent voteTarget;
	Agent divineTarget;
	Agent guardTarget;
	Agent identTarget;
	Agent attackTarget;

	public OwnData(GameInfo gameInfo, GameSetting gameSetting) {
		this.gameInfo = gameInfo;
		this.gameSetting = gameSetting;
		this.me = gameInfo.getAgent();
		this.myRole = gameInfo.getRole();
		
		this.talkList = new ArrayList<>();
		this.actFlag = 0;
		
		voteTarget = null;
		divineTarget = null;
		guardTarget = null;
		identTarget = null;
		attackTarget = null;
	}

	public GameInfo getGameInfo() {
		return gameInfo;
	}

	public void setGameInfo(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
	}

	public GameSetting getGameSetting() {
		return gameSetting;
	}

	public void setGameSetting(GameSetting gameSetting) {
		this.gameSetting = gameSetting;
	}

	public Agent getMe() {
		return me;
	}

	public void setMe(Agent me) {
		this.me = me;
	}

	public Role getMyRole() {
		return myRole;
	}

	public void setMyRole(Role myRole) {
		this.myRole = myRole;
	}

	public void setTalkList() {

	}

	public List getTalkList() {
		return talkList;
	}

	public Agent getAttackTarget() {
		return this.attackTarget;
	}
}
