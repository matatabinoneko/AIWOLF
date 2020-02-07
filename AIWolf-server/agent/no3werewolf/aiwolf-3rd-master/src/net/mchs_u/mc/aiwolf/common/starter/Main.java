package net.mchs_u.mc.aiwolf.common.starter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Role;

import net.mchs_u.mc.aiwolf.common.starter.component.PlayerDefinition;

public class Main {	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		int set = 600;
		int times = 100;
		int playerNum = 15;
		boolean isVisualize = false;
		boolean isLog = false;
		boolean isSaveResult = true;
		Role role = null;
		
		List<PlayerDefinition> playerDefinitions = new ArrayList<>();
		playerDefinitions.add(new PlayerDefinition(net.mchs_u.mc.aiwolf.dokin.McrePlayer.class, isVisualize, role, "dokin"));
		int count = 0;
		while(playerDefinitions.size() < playerNum){
			playerDefinitions.add(new PlayerDefinition(org.aiwolf.sample.player.SampleRoleAssignPlayer.class, false, null, String.format("smpl%02d", count++)));
		}
		
		DirectStarter ds = new DirectStarter(playerDefinitions, times, set, isVisualize, isLog, isSaveResult);
		ds.start();
	}
}
