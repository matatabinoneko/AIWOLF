package army.sh.info;

import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class EntityData {
	OwnData ownData;
	Information info;
	
	public EntityData(GameInfo gameInfo, GameSetting gameSetting){
		this.ownData = new OwnData(gameInfo, gameSetting);
		this.info = new Information(gameInfo, gameSetting);
		
	}

	public OwnData getOwnData() {
		return ownData;
	}

	public void setOwnData(OwnData ownData) {
		this.ownData = ownData;
	}

	public Information getInfo() {
		return info;
	}

	public void setInfo(Information info) {
		this.info = info;
	}

}
