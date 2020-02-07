package com.gmail.aiwolf.uec.yk.strategyplayer;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;
import org.aiwolf.sample.player.*;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;

public class StrategyPlayer extends AbstractRoleAssignPlayer {

	/** ç¹§?½¨ç¹ï½¼ç¹§?½¸ç¹§?½§ç¹ï½³ç¹åŸŸ?½¯å¼±?¿½?½®é‚¨?½±éšªåŸŸãƒ¥è£?½± */
	protected AgentStatistics agentStatistics = new AgentStatistics();

	public StrategyPlayer() {
		setVillagerPlayer(new StrategyVillager(agentStatistics));
		setSeerPlayer(new StrategySeer(agentStatistics));
		setMediumPlayer(new StrategyMedium(agentStatistics));
		setBodyguardPlayer(new StrategyBodyGuard(agentStatistics));
		setPossessedPlayer(new StrategyPossessed(agentStatistics));
		setWerewolfPlayer(new StrategyWerewolf(agentStatistics));
	}


	@Override
	public String getName() {
//		return StrategyPlayer.class.getSimpleName();
		return "Udon";
	}


}
