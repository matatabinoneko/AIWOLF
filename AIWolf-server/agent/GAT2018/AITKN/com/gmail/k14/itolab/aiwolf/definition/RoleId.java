package com.gmail.k14.itolab.aiwolf.definition;


/**
 * 役職のID番号を管理する
 * @author k14096kk
 *
 */
public enum RoleId {

	/**
	 * 村人番号 1
	 */
	VILLAGER_ID(1),
	
	/**
	 * 占い師番号 2
	 */
	SEER_ID(2),
	
	/**
	 * 霊媒師番号 4
	 */
	MEDIUM_ID(4),
	
	/**
	 * 狩人番号 8
	 */
	BODYGUARD_ID(8),
	
	/**
	 * 狂人番号 16
	 */
	POSSESSED_ID(16),
	
	/**
	 * 人狼番号 32
	 */
	WEREWOLF_ID(32);
	
	/**
	 * 役職のID番号
	 */
	private final int id;
	
	/**
	 * 役職のID番号設定
	 * @param id : 番号
	 */
	private RoleId(final int id) {
		this.id = id;
	}
	
	/**
	 * 役職のID番号取得
	 * @return 役職のID番号
	 */
	public int getId() {
		return this.id;
	}
}
