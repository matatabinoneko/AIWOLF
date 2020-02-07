
package jp.ac.aitech.k15029kk.player;

import jp.ac.aitech.k15029kk.GameResult;
import jp.ac.aitech.k15029kk.role.Bodyguard;
import jp.ac.aitech.k15029kk.role.Medium;
import jp.ac.aitech.k15029kk.role.Possessed;
import jp.ac.aitech.k15029kk.role.Seer;
import jp.ac.aitech.k15029kk.role.Villager;
import jp.ac.aitech.k15029kk.role.Werewolf;

public class DemoPlayer extends AbstractDemoRoleAssignPlayer {

	public DemoPlayer() {

		GameResult gameResult = new GameResult();

		this.setSeerPlayer(new Seer(gameResult));  //占い師エージェント
		this.setWerewolfPlayer(new Werewolf(gameResult)); //人狼エージェント
		this.setPossessedPlayer(new Possessed(gameResult)); //裏切り者エージェント
		this.setVillagerPlayer(new Villager(gameResult)); //村人エージェント
		this.setMediumPlayer(new Medium(gameResult)); //霊能者エージェント
		this.setBodyguardPlayer(new Bodyguard(gameResult)); //狩人エージェント

	}

	public String getName() {

		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
