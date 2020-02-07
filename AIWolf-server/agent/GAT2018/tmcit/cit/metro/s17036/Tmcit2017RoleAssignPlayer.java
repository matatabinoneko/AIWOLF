package cit.metro.s17036;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class Tmcit2017RoleAssignPlayer extends AbstractRoleAssignPlayer {

	public Tmcit2017RoleAssignPlayer() {
		setSeerPlayer(new Tmcit2017Seer());
		setVillagerPlayer(new Tmcit2017Villager());
		setWerewolfPlayer(new Tmcit2017Werewolf());
		setPossessedPlayer(new Tmcit2017Possessed());
		setBodyguardPlayer(new Tmcit2017Bodyguard());
		setMediumPlayer(new Tmcit2017Medium());
	}


	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "tmcit2017";
	}

}
