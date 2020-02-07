package com.gmail.frostcolumnofsilverywhite;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class KurageRoleAssignPlayer extends AbstractRoleAssignPlayer {

	public KurageRoleAssignPlayer() {
		setSeerPlayer(new KurageSeer());
		setBodyguardPlayer(new KurageBodyguard());
		setMediumPlayer(new KurageMedium());
		setPossessedPlayer(new KuragePossessed());
		setVillagerPlayer(new KurageVillager());
		setWerewolfPlayer(new KurageWerewolf());
	}
	
	@Override
	public String getName() {
	    return "KurageRoleAssignPlayer";
	}

}
