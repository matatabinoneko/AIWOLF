package army.sh.info;


import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class Information {
	
	GameInfo gameInfo;
	GameSetting gameSetting;
	
	Talk talk;
	

	public Information(GameInfo gameInfo, GameSetting gameSetting) {
		this.gameInfo = gameInfo;
		this.gameSetting = gameSetting;
		
		talk = null;
		
	}
	
	public static void analysisTalk(Talk talk){
		Agent agent = talk.getAgent();
		if(agent == agent){
			
		}
	}
	

}
