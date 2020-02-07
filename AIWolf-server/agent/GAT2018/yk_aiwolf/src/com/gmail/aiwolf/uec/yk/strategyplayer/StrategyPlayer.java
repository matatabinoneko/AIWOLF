package com.gmail.aiwolf.uec.yk.strategyplayer;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;
import org.aiwolf.sample.player.*;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;

public class StrategyPlayer extends AbstractRoleAssignPlayer {

	/** 繧?��繝ｼ繧?��繧?��繝ｳ繝域?��弱?��?��邨?��險域ュ蝣?�� */
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
