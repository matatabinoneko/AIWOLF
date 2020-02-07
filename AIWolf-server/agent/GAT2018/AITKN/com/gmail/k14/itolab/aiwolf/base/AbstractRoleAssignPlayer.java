package com.gmail.k14.itolab.aiwolf.base;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.role.Bodyguard;
import com.gmail.k14.itolab.aiwolf.role.Medium;
import com.gmail.k14.itolab.aiwolf.role.Possessed;
import com.gmail.k14.itolab.aiwolf.role.Seer;
import com.gmail.k14.itolab.aiwolf.role.Villager;
import com.gmail.k14.itolab.aiwolf.role.Werewolf;

/**
 * プレイヤのAbstractクラス
 * プレイヤはこれを継承して実装する
 * @author k14096kk
 *
 */
public abstract class AbstractRoleAssignPlayer implements Player {
	
	/**村人エージェント*/
	private Villager villagerPlayer = new Villager(null);
	/**占い師エージェント*/
	private Seer seerPlayer = new Seer(null);
	/**霊媒師エージェント*/
	private Medium mediumPlayer = new Medium(null);
	/**狩人エージェント*/
	private Bodyguard bodyguardPlayer = new Bodyguard(null);
	/**狂人エージェント*/
	private Possessed possessedPlayer = new Possessed(null);
	/**人狼エージェント*/
	private Werewolf werewolfPlayer = new Werewolf(null);
	/**プレイヤ*/
	private Player rolePlayer;

	public AbstractRoleAssignPlayer() {
	}

	/**
	 * 村人エージェントの取得
	 * @return 村人
	 */
	public final Villager getVillagerPlayer() {
		return this.villagerPlayer;
	}

	/**
	 * 村人エージェントの設定
	 * @param villagerPlayer :村人
	 */
	public final void setVillagerPlayer(Villager villagerPlayer) {
		this.villagerPlayer = villagerPlayer;
	}

	/**
	 * 占い師エージェントの取得
	 * @return 占い師
	 */
	public final Seer getSeerPlayer() {
		return this.seerPlayer;
	}

	/**
	 * 占い師エージェントの設定
	 * @param seerPlayer :占い師
	 */
	public final void setSeerPlayer(Seer seerPlayer) {
		this.seerPlayer = seerPlayer;
	}

	/**
	 * 霊媒師エージェントの取得
	 * @return 霊媒師
	 */
	public final Medium getMediumPlayer() {
		return this.mediumPlayer;
	}

	/**
	 * 霊媒師エージェントの設定
	 * @param mediumPlayer :霊媒師
	 */
	public final void setMediumPlayer(Medium mediumPlayer) {
		this.mediumPlayer = mediumPlayer;
	}

	/**
	 * 狩人エージェントの取得
	 * @return 狩人
	 */
	public final Bodyguard getBodyguardPlayer() {
		return this.bodyguardPlayer;
	}

	/**
	 * 狩人エージェントの設定
	 * @param bodyGuardPlayer :狩人
	 */
	public final void setBodyguardPlayer(Bodyguard bodyGuardPlayer) {
		this.bodyguardPlayer = bodyGuardPlayer;
	}

	/**
	 * 狂人エージェントの取得
	 * @return 狂人
	 */
	public final Possessed getPossessedPlayer() {
		return this.possessedPlayer;
	}

	/**
	 * 狂人エージェントの設定
	 * @param possesedPlayer :狂人
	 */	
	public final void setPossessedPlayer(Possessed possesedPlayer) {
		this.possessedPlayer = possesedPlayer;
	}

	/**
	 * 人狼エージェントの取得
	 * @return 人狼
	 */
	public final Werewolf getWerewolfPlayer() {
		return this.werewolfPlayer;
	}

	/**
	 * 人狼エージェントの設定
	 * @param werewolfPlayer :人狼
	 */
	public final void setWerewolfPlayer(Werewolf werewolfPlayer) {
		this.werewolfPlayer = werewolfPlayer;
	}

	/**
	 * エージェント名の取得
	 */
	public abstract String getName();

	/**
	 * ゲーム更新
	 */
	public final void update(GameInfo gameInfo) {
		this.rolePlayer.update(gameInfo);
	}

	/**
	 * 初期化
	 */
	public final void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		Role myRole = gameInfo.getRole();
		switch (myRole) {
		case VILLAGER:
			this.rolePlayer = this.villagerPlayer;
			break;
		case SEER:
			this.rolePlayer = this.seerPlayer;
			break;
		case MEDIUM:
			this.rolePlayer = this.mediumPlayer;
			break;
		case BODYGUARD:
			this.rolePlayer = this.bodyguardPlayer;
			break;
		case POSSESSED:
			this.rolePlayer = this.possessedPlayer;
			break;
		case WEREWOLF:
			this.rolePlayer = this.werewolfPlayer;
			break;
		default:
			this.rolePlayer = this.villagerPlayer;
		}

		this.rolePlayer.initialize(gameInfo, gameSetting);
	}

	/**
	 * 1日のはじめ
	 */
	public final void dayStart() {
		this.rolePlayer.dayStart();
	}

	/**
	 * 会話
	 */
	public final String talk() {
		return this.rolePlayer.talk();
	}

	/**
	 * 囁き
	 */
	public final String whisper() {
		return this.rolePlayer.whisper();
	}

	/**
	 * 投票
	 */
	public final Agent vote() {
		return this.rolePlayer.vote();
	}

	/**
	 * 襲撃
	 */
	public final Agent attack() {
		return this.rolePlayer.attack();
	}

	/**
	 * 占い
	 */
	public final Agent divine() {
		return this.rolePlayer.divine();
	}

	/**
	 * 護衛
	 */
	public final Agent guard() {
		return this.rolePlayer.guard();
	}

	/**
	 * ゲーム終了
	 */
	public final void finish() {
		this.rolePlayer.finish();
	}

}
