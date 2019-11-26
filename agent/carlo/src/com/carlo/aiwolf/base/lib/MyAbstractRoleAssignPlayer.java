/**
 * AbstractRoleAssignPlayer.java
 * 
 * Copyright (c) 2016 人狼知能プロジェクト
 */
package com.carlo.aiwolf.base.lib;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.player.SampleBodyguard;
import org.aiwolf.sample.player.SampleMedium;
import org.aiwolf.sample.player.SamplePossessed;
import org.aiwolf.sample.player.SampleSeer;
import org.aiwolf.sample.player.SampleVillager;
import org.aiwolf.sample.player.SampleWerewolf;

import com.carlo.aiwolf.bayes.player.BayesBodyguard;
import com.carlo.aiwolf.bayes.player.BayesMedium;
import com.carlo.aiwolf.bayes.player.BayesPossessed;
import com.carlo.aiwolf.bayes.player.BayesSeer;
import com.carlo.aiwolf.bayes.player.BayesVillager;
import com.carlo.aiwolf.player.CarloWerewolf;

/**
 * <div lang="ja">役職ごとに実際に使用するプレイヤーを切り替えるプレイヤーの抽象クラス</div>
 *
 * <div lang="en">Abstract player class which switches player actually used according to its role.</div>
 */
public abstract class MyAbstractRoleAssignPlayer implements Player {

	// 各役職で実際に使用するAbstructRoleクラスのプレイヤーインスタンスを生成して下さい．
	// 例えば村人プレイヤーだけ自作のプレイヤーにしたい場合は
	// AbstractVillager villagerPlayer = new SampleVillagerPlayer();
	// を
	// AbstractVillager villagerPlayer = new [自作プレイヤークラスのコンストラクタ];
	// と変更すれば，村人の役職が割り振られた時は自作のプレイヤーで
	// それ以外の役職になった時はサンプルプレイヤーでプレイします．
	private MyAbstractVillager villagerPlayer = new BayesVillager();
	private MyAbstractSeer seerPlayer = new BayesSeer();
	private MyAbstractMedium mediumPlayer = new BayesMedium();
	private MyAbstractBodyguard bodyguardPlayer = new BayesBodyguard();
	private MyAbstractPossessed possessedPlayer = new BayesPossessed();
	private MyAbstractWerewolf werewolfPlayer = new CarloWerewolf();

	private Player rolePlayer;

	/**
	 * <div lang="ja">村人用プレイヤーを返す．</div>
	 *
	 * <div lang="en">Returns the player actually used in case of villager.</div>
	 * 
	 * @return <div lang="ja">村人用プレイヤー</div>
	 *
	 *         <div lang="en">the player actually used in case of villager</div>
	 */
	public final MyAbstractVillager getVillagerPlayer() {
		return villagerPlayer;
	}

	/**
	 * <div lang="ja">村人用プレイヤーをセットする．</div>
	 *
	 * <div lang="en">Sets the player actually used in case of villager.</div>
	 * 
	 * @param villagerPlayer
	 *            <div lang="ja">実際の村人プレイヤー</div>
	 *
	 *            <div lang="en">the actual villager player</div>
	 */
	public final void setVillagerPlayer(MyAbstractVillager villagerPlayer) {
		this.villagerPlayer = villagerPlayer;
	}

	/**
	 * <div lang="ja">占い師用プレイヤーを返す．</div>
	 *
	 * <div lang="en">Returns the player actually used in case of seer.</div>
	 * 
	 * @return <div lang="ja">占い師用プレイヤー</div>
	 *
	 *         <div lang="en">the player actually used in case of seer</div>
	 */
	public final MyAbstractSeer getSeerPlayer() {
		return seerPlayer;
	}

	/**
	 * <div lang="ja">占い師用プレイヤーをセットする．</div>
	 *
	 * <div lang="en">Sets the player actually used in case of seer.</div>
	 * 
	 * @param seerPlayer
	 *            <div lang="ja">実際の占い師プレイヤー</div>
	 *
	 *            <div lang="en">the actual seer player</div>
	 */
	public final void setSeerPlayer(MyAbstractSeer seerPlayer) {
		this.seerPlayer = seerPlayer;
	}

	/**
	 * <div lang="ja">霊媒師用プレイヤーを返す．</div>
	 *
	 * <div lang="en">Returns the player actually used in case of medium.</div>
	 * 
	 * @return <div lang="ja">霊媒師用プレイヤー</div>
	 *
	 *         <div lang="en">the player actually used in case of medium</div>
	 */
	public final MyAbstractMedium getMediumPlayer() {
		return mediumPlayer;
	}

	/**
	 * <div lang="ja">霊媒師用プレイヤーをセットする．</div>
	 *
	 * <div lang="en">Sets the player actually used in case of medium.</div>
	 * 
	 * @param mediumPlayer
	 *            <div lang="ja">実際の霊媒師プレイヤー</div>
	 *
	 *            <div lang="en">the actual medium player</div>
	 */
	public final void setMediumPlayer(MyAbstractMedium mediumPlayer) {
		this.mediumPlayer = mediumPlayer;
	}

	/**
	 * <div lang="ja">狩人用プレイヤーを返す．</div>
	 *
	 * <div lang="en">Returns the player actually used in case of bodyguard.</div>
	 * 
	 * @return <div lang="ja">狩人用プレイヤー</div>
	 *
	 *         <div lang="en">the player actually used in case of bodyguard</div>
	 */
	public final MyAbstractBodyguard getBodyguardPlayer() {
		return bodyguardPlayer;
	}

	/**
	 * <div lang="ja">狩人用プレイヤーをセットする．</div>
	 *
	 * <div lang="en">Sets the player actually used in case of bodyguard.</div>
	 * 
	 * @param bodyguardPlayer
	 *            <div lang="ja">実際の狩人プレイヤー</div>
	 *
	 *            <div lang="en">the actual bodyguard player</div>
	 */
	public final void setBodyguardPlayer(MyAbstractBodyguard bodyGuardPlayer) {
		this.bodyguardPlayer = bodyGuardPlayer;
	}

	/**
	 * <div lang="ja">裏切り者用プレイヤーを返す．</div>
	 *
	 * <div lang="en">Returns the player actually used in case of possessed.</div>
	 * 
	 * @return <div lang="ja">裏切り者用プレイヤー</div>
	 *
	 *         <div lang="en">the player actually used in case of possessed</div>
	 */
	public final MyAbstractPossessed getPossessedPlayer() {
		return possessedPlayer;
	}

	/**
	 * <div lang="ja">裏切り者用プレイヤーをセットする．</div>
	 *
	 * <div lang="en">Sets the player actually used in case of possessed.</div>
	 * 
	 * @param possessedPlayer
	 *            <div lang="ja">実際の裏切り者プレイヤー</div>
	 *
	 *            <div lang="en">the actual possessed player</div>
	 */
	public final void setPossessedPlayer(MyAbstractPossessed possesedPlayer) {
		this.possessedPlayer = possesedPlayer;
	}

	/**
	 * <div lang="ja">人狼用プレイヤーを返す．</div>
	 *
	 * <div lang="en">Returns the player actually used in case of werewolf.</div>
	 * 
	 * @return <div lang="ja">人狼用プレイヤー</div>
	 *
	 *         <div lang="en">the player actually used in case of werewolf</div>
	 */
	public final MyAbstractWerewolf getWerewolfPlayer() {
		return werewolfPlayer;
	}

	/**
	 * <div lang="ja">人狼用プレイヤーをセットする．</div>
	 *
	 * <div lang="en">Sets the player actually used in case of werewolf.</div>
	 * 
	 * @param werewolfPlayer
	 *            <div lang="ja">実際の人狼プレイヤー</div>
	 *
	 *            <div lang="en">the actual werewolf player</div>
	 */
	public final void setWerewolfPlayer(MyAbstractWerewolf werewolfPlayer) {
		this.werewolfPlayer = werewolfPlayer;
	}

	@Override
	public abstract String getName();

	@Override
	public final void update(GameInfo gameInfo) {
		rolePlayer.update(gameInfo);
	}

	@Override
	public final void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		Role myRole = gameInfo.getRole();
		switch (myRole) {
		case VILLAGER:
			rolePlayer = villagerPlayer;
			break;
		case SEER:
			rolePlayer = seerPlayer;
			break;
		case MEDIUM:
			rolePlayer = mediumPlayer;
			break;
		case BODYGUARD:
			rolePlayer = bodyguardPlayer;
			break;
		case POSSESSED:
			rolePlayer = possessedPlayer;
			break;
		case WEREWOLF:
			rolePlayer = werewolfPlayer;
			break;
		default:
			rolePlayer = villagerPlayer;
			break;
		}
		rolePlayer.initialize(gameInfo, gameSetting);
	}

	@Override
	public final void dayStart() {
		rolePlayer.dayStart();
	}

	@Override
	public final String talk() {
		return rolePlayer.talk();
	}

	@Override
	public final String whisper() {
		return rolePlayer.whisper();
	}

	@Override
	public final Agent vote() {
		return rolePlayer.vote();
	}

	@Override
	public final Agent attack() {
		return rolePlayer.attack();
	}

	@Override
	public final Agent divine() {
		return rolePlayer.divine();
	}

	@Override
	public final Agent guard() {
		return rolePlayer.guard();
	}

	@Override
	public final void finish() {
		rolePlayer.finish();
	}

}
