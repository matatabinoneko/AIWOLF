package com.gmail.k14.itolab.aiwolf.player;

import com.gmail.k14.itolab.aiwolf.base.AbstractRoleAssignPlayer;
import com.gmail.k14.itolab.aiwolf.data.GameResult;
import com.gmail.k14.itolab.aiwolf.role.Bodyguard;
import com.gmail.k14.itolab.aiwolf.role.Medium;
import com.gmail.k14.itolab.aiwolf.role.Possessed;
import com.gmail.k14.itolab.aiwolf.role.Seer;
import com.gmail.k14.itolab.aiwolf.role.Villager;
import com.gmail.k14.itolab.aiwolf.role.Werewolf;


/**
 * 自作プレイヤー
 * @author k14096kk
 *
 */
public class MyPlayer extends AbstractRoleAssignPlayer {
	
	GameResult gameResult;
	boolean flag = false;
	
	public MyPlayer() {
		try {
			// 最初のゲームならばゲーム結果を作成
			if(!flag) {
				gameResult = new GameResult();
				flag = true;
			}
			
			//村人適用
			this.setVillagerPlayer(new Villager(gameResult));
			//占い師適用
			this.setSeerPlayer(new Seer(gameResult));
			//霊媒師適用
			this.setMediumPlayer(new Medium(gameResult));
			//狩人適用
			this.setBodyguardPlayer(new Bodyguard(gameResult));
			//狂人適用
			this.setPossessedPlayer(new Possessed(gameResult));
			//人狼適用
			this.setWerewolfPlayer(new Werewolf(gameResult));
		} catch (Exception e) {
		}
		
	}

	@Override
	public String getName() {
		return "AITKN";
	}
}
