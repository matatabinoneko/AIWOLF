package army.sh;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;
import org.aiwolf.sample.player.SampleBodyguard;
import org.aiwolf.sample.player.SampleMedium;

import army.sh.role.Bodyguard;
import army.sh.role.Medium;
import army.sh.role.Possessed;
import army.sh.role.Seer;
import army.sh.role.Villager;
import army.sh.role.Werewolf;

public class ArmyRoleAssignPlayer extends AbstractRoleAssignPlayer {

	

	public ArmyRoleAssignPlayer(){

		try{
			this.setVillagerPlayer(new Villager());
			this.setSeerPlayer(new Seer());
			this.setMediumPlayer(new Medium());
			this.setBodyguardPlayer(new Bodyguard());
			this.setPossessedPlayer(new Possessed());
			this.setWerewolfPlayer(new Werewolf());

		}catch(Exception e){
			//System.out.println("ArmyRoleAssignPlayer:error");
		}

	}

	@Override
	public String getName() {
		return "Army";
	}



}
