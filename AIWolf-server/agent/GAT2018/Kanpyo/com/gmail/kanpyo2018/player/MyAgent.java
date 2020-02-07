package com.gmail.kanpyo2018.player;

import com.gmail.kanpyo2018.Role.Bodyguard;
import com.gmail.kanpyo2018.Role.Medium;
import com.gmail.kanpyo2018.Role.Possessed;
import com.gmail.kanpyo2018.Role.Seer;
import com.gmail.kanpyo2018.Role.Villager;
import com.gmail.kanpyo2018.Role.Werewolf;
import com.gmail.kanpyo2018.data.GameResult;

public class MyAgent extends AbstractDemoRoleAssignPlayer {

	public MyAgent() {
		GameResult gameResult = new GameResult();
		// 狩人
		this.setBodyguardPlayer(new Bodyguard(gameResult));
		// 霊能者
		this.setMediumPlayer(new Medium(gameResult));
		// 裏切り者
		this.setPossessedPlayer(new Possessed(gameResult));
		// 占い師
		this.setSeerPlayer(new Seer(gameResult));
		// 村人
		this.setVillagerPlayer(new Villager(gameResult));
		// 人狼
		this.setWerewolfPlayer(new Werewolf(gameResult));
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "Kanpyo";
	}

}
