package info.tt.hibagon;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class MyRoleAssignPlayer extends AbstractRoleAssignPlayer {
	public MyRoleAssignPlayer(){
		super.setSeerPlayer(new HSeer());
//		super.setBodyguardPlayer(new HBodyguard());
//		super.setMediumPlayer(new HMedium());
//		super.setPossessedPlayer(new HPossessed());
//		super.setVillagerPlayer(new HVillager());
//		super.setWerewolfPlayer(new HWerewolf());
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
