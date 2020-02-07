package com.gmail.aplain52lab;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class AplainRoleAssignPlayer extends AbstractRoleAssignPlayer {

	public AplainRoleAssignPlayer() {
		setVillagerPlayer(new AplainVillager());
		setSeerPlayer(new AplainSeer());
		setMediumPlayer(new AplainMedium());
		setBodyguardPlayer(new AplainBodyguard());
		setPossessedPlayer(new AplainPossessed());
		setWerewolfPlayer(new AplainWerewolf());
	}
	
	@Override
	public String getName() {
		return "Aplain";
	}
	
}
