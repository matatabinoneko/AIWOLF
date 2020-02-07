package com.gmail.k14.itolab.aiwolf.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aiwolf.common.data.Role;

import com.gmail.k14.itolab.aiwolf.util.RoleIdManagement;

/***
 * 戦略の条件分けに用いる列挙型
 * @author k14096kk
 *
 */
public enum Strategy {
	
	/**
	 * 戦略なし
	 */
	NONE(RoleIdManagement.sumId(1, 1, 1, 1, 1, 1)),
	
	/**
	 * フルオープン
	 */
	FO(RoleIdManagement.sumId(1, 1, 1, 1, 1, 1)),
	
	/**
	 * パワープレイ
	 */
	PP(RoleIdManagement.sumId(0, 0, 0, 0, 1, 1)),
	
	/**
	 * 占い師騙り
	 */
	FAKESEER(RoleIdManagement.sumId(0, 0, 0, 0, 1, 1)),
	
	/**
	 * 潜伏
	 */
	HIDE(RoleIdManagement.sumId(0, 1, 1, 1, 1, 1)),
	
	/**
	 * 統一占い
	 */
	UNIFYDIVINE(RoleIdManagement.sumId(1, 1, 1, 1, 1, 1)),
	
	/**
	 * 村人のPP阻止＿人狼CO
	 */
	VILLAGER_WEREWOLF_CO(RoleIdManagement.sumId(1, 0, 0, 0, 0, 0));
	
	/**戦略適用役職のビット*/
	private final int id;
	
	private Strategy(final int id) {
		this.id = id;
	}
	
	/**
	 * 戦略のID取得
	 * @return ID
	 */
	public int getId() {
		return this.id;
	} 
	
	/**
	 * 戦略とビットのANDを取得して比較
	 * @param strategy :戦略
	 * @param id :比較ビット
	 * @return ビットが存在すればtrue,存在しなければfalse
	 */
	public static boolean compareRole(Strategy strategy, RoleId id) {
		int bit = strategy.getId() & id.getId();
		if(bit == id.getId()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 戦略が村人に適用するかどうか
	 * @param strategy :戦略
	 * @return 適用すればtrue，しなければfalse
	 */
	public static boolean compareViilager(Strategy strategy) {
		if(compareRole(strategy, RoleId.VILLAGER_ID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 戦略が占い師に適用するかどうか
	 * @param strategy :戦略
	 * @return 適用すればtrue，しなければfalse
	 */
	public static boolean compareSeer(Strategy strategy) {
		if(compareRole(strategy, RoleId.SEER_ID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 戦略が霊媒師に適用するかどうか
	 * @param strategy :戦略
	 * @return 適用すればtrue，しなければfalse
	 */
	public static boolean compareMedium(Strategy strategy) {
		if(compareRole(strategy, RoleId.MEDIUM_ID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 戦略が狩人に適用するかどうか
	 * @param strategy :戦略
	 * @return 適用すればtrue，しなければfalse
	 */
	public static boolean compareBodyguard(Strategy strategy) {
		if(compareRole(strategy, RoleId.BODYGUARD_ID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 戦略が狂人に適用するかどうか
	 * @param strategy :戦略
	 * @return 適用すればtrue，しなければfalse
	 */
	public static boolean comparePossessed(Strategy strategy) {
		if(compareRole(strategy, RoleId.POSSESSED_ID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 戦略が人狼に適用するかどうか
	 * @param strategy :戦略
	 * @return 適用すればtrue，しなければfalse
	 */
	public static boolean compareWerewolf(Strategy strategy) {
		if(compareRole(strategy, RoleId.WEREWOLF_ID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 役職に適用する戦略リスト取得
	 * @param role :役職
	 * @return 戦略リスト
	 */
	public static List<Strategy> applyList(Role role) {
		// 役職に渡す適用戦略リスト
		List<Strategy> storeList = new ArrayList<>();
		// 役職ごとに適用しているか確認
		for(Strategy strategy: Strategy.values()) {
			switch (role) {
			case VILLAGER:
				if(compareViilager(strategy)) storeList.add(strategy);
				break;
			case SEER:
				if(compareSeer(strategy)) storeList.add(strategy);
				break;
			case MEDIUM:
				if(compareMedium(strategy)) storeList.add(strategy);
				break;
			case BODYGUARD:
				if(compareBodyguard(strategy)) storeList.add(strategy);
				break;
			case POSSESSED:
				if(comparePossessed(strategy)) storeList.add(strategy);
				break;
			case WEREWOLF:
				if(compareWerewolf(strategy)) storeList.add(strategy);
				break;
			default:
				break;
			}
		}
		return storeList;
	}
	
	/**
	 * 戦略一覧表示
	 */
	public static void show() {
	}
	
}
