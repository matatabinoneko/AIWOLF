package org.aiwolf.ReGEX.H.WindMill;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class WindMillRoleAssignPlayer extends AbstractRoleAssignPlayer {

	public WindMillRoleAssignPlayer(){
		setVillagerPlayer(new WindMillVillager1());
        setBodyguardPlayer(new WindMillBodyguard());
        setMediumPlayer(new WindMillMedium());
        setSeerPlayer(new WindMillSeer());
        setPossessedPlayer(new WindMillPossessed());
        setWerewolfPlayer(new WindMillWerewolf());
	}
	public String getName() {
		return "WindMillRoleAssignPlayer";
	}

}
