package com.carlo.aiwolf.bayes.player;

import org.aiwolf.common.data.Player;
import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

import com.carlo.aiwolf.base.lib.MyAbstractRoleAssignPlayer;
import com.carlo.aiwolf.player.CarloWerewolf;
/**
 *  ベイジアンネットワークを利用して作成したエージェント
 * @author carlo
 *
 */
public class BayesPlayer extends MyAbstractRoleAssignPlayer {
	public static int gameNum=0;
	public BayesPlayer(){
		setVillagerPlayer(new BayesVillager());
		setBodyguardPlayer(new BayesBodyguard());
		setMediumPlayer(new BayesMedium());
		setSeerPlayer(new BayesSeer());
		
		setWerewolfPlayer(new CarloWerewolf());
		setPossessedPlayer(new BayesPossessed());
		
	}
	public String getName() {
		return "carlo";
	}
	public static void printGameNum(){
		System.out.println("\nStartGame "+gameNum++);
	}


}
