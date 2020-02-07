package com.gmail.k14.itolab.aiwolf.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.gmail.k14.itolab.aiwolf.definition.Strategy;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.Debug;
import com.gmail.k14.itolab.aiwolf.util.StrategyInfo;

/**
 * ゲーム結果管理クラス
 * @author k14096kk
 *
 */
public class GameResult {
	
	/**村陣営勝利数*/
	 int villageWin = 0;
	/**人狼陣営勝利数*/
	 int wolfWin = 0;
	/**ゲーム数*/
	 int gameCount = 0;
	/**自分の役職ごとの勝利数*/
	 int[] myWin = new int[6];
	/**自分が村陣営所属時の勝利数*/
	 int myVillageWin = 0;
	/**自分が狼陣営所属時の勝利数*/
	 int myWolfWin = 0;
	/**自分の役職割当て回数*/
	 int[] assignCount = new int[6];
	 /**役職ごとに取った戦略の使用回数と勝利数 key:役職 value:戦略情報*/
	 Map<Role, List<StrategyInfo>> strategyMap = new HashMap<>();
	 
	/**
	 * ゲーム結果作成
	 */
	public GameResult() {
	}
	
	/**
	 * 勝利数を設定
	 * @param entityData :オブジェクトデータ
	 */
	public void setGameResult(EntityData entityData) {
		List<Role> aliveRoleList = new ArrayList<>();
		// 生存役職のリスト作成
		for(Agent agent: entityData.getOwnData().getGameInfo().getRoleMap().keySet()) {
			if(entityData.getOwnData().getGameInfo().getAliveAgentList().contains(agent)) {
				aliveRoleList.add(entityData.getOwnData().getGameInfo().getRoleMap().get(agent));
			}
		}
		// 人狼が生存していれば狼陣営勝利，いなければ村陣営勝利
		if(aliveRoleList.contains(Role.WEREWOLF)) {
			myWolfWin(entityData);
			setWolfStrategy(entityData);
			wolfWin++;
		}else {
			myVillageWin(entityData);
			setVillagerStrategy(entityData);
			villageWin++;
		}
		
		setAssignCount(entityData.getOwnData().getMyRole());
		gameCount++;
	}
	
	/*-----------------------------ゲーム回数----------------------------------------*/
	
	/**
	 * ゲーム回数取得
	 * @return ゲーム回数
	 */
	public int getGameCount() {
		return gameCount;
	}
	
	/*-----------------------------全体の陣営勝利数----------------------------------------*/
	
	/**
	 * 村陣営勝利数取得
	 * @return 村陣営勝利数
	 */
	public int getVillageWin() {
		return villageWin;
	}
	
	/**
	 * 狼陣営勝利数取得
	 * @return 狼陣営勝利数
	 */
	public int getWolfWin() {
		return wolfWin;
	}
	
	/*-----------------------------自分の陣営勝利数----------------------------------------*/
	
	/**
	 * 村陣営の勝利数取得
	 * @return 村陣営の勝利数
	 */
	public int getMyVillageWin() {
		return myVillageWin;
	}
	
	/**
	 * 狼陣営の勝利数取得
	 * @return 狼陣営の勝利数
	 */
	public int getMyWolfWin() {
		return myWolfWin;
	}
	
	/**
	 * 自分の役職ごと勝利数取得
	 * @param role :役職
	 * @return 役職ごとの勝利数
	 */
	public int getMyWin(Role role) {
		int win = 0;
		switch (role) {
		case VILLAGER:
			win = myWin[0];
			break;
		case SEER:
			win = myWin[1];
			break;
		case MEDIUM:
			win = myWin[2];
			break;
		case BODYGUARD:
			win = myWin[3];
			break;
		case POSSESSED:
			win = myWin[4];
			break;
		case WEREWOLF:
			win = myWin[5];
			break;
		default:
			break;		
		}
		
		return win;
	}
	
	/**
	 * 自分の勝利数増加
	 * @param num 役職配置
	 */
	public void addMyWin(int num) {
		myWin[num]++; 
	}
	
	/**
	 * 村人陣営での自分勝利数
	 * @param entityData :オブジェクトデータ
	 */
	public void myVillageWin(EntityData entityData) {
		switch (entityData.getOwnData().getMyRole()) {
		case VILLAGER:
			addMyWin(0);
			myVillageWin++;
			break;
		case SEER:
			addMyWin(1);
			myVillageWin++;
			break;
		case MEDIUM:
			addMyWin(2);
			myVillageWin++;
			break;
		case BODYGUARD:
			addMyWin(3);
			myVillageWin++;
			break;
		default:
			break;		
		}
	}
	
	/**
	 * 人狼陣営での自分勝利数
	 * @param entityData :オブジェクトデータ
	 */
	public void myWolfWin(EntityData entityData) {
		switch (entityData.getOwnData().getMyRole()) {
		case POSSESSED:
			addMyWin(4);
			myWolfWin++;
			break;
		case WEREWOLF:
			addMyWin(5);
			myWolfWin++;
			break;
		default:
			break;		
		}
	}
	
	/*-----------------------------役職割当て----------------------------------------*/
	
	/**
	 * 自分の役職ごと割当数取得
	 * @param role :役職
	 * @return 役職ごとの割当数
	 */
	public int getAssignCount(Role role) {
		int count = 0;
		switch (role) {
		case VILLAGER:
			count = assignCount[0];
			break;
		case SEER:
			count = assignCount[1];
			break;
		case MEDIUM:
			count = assignCount[2];
			break;
		case BODYGUARD:
			count = assignCount[3];
			break;
		case POSSESSED:
			count = assignCount[4];
			break;
		case WEREWOLF:
			count = assignCount[5];
			break;
		default:
			break;		
		}
		
		return count;
	}
	
	/**
	 * 自分の役職割当数増加
	 * @param num 役職配置
	 */
	public void addAssignCount(int num) {
		assignCount[num]++; 
	}
	
	/**
	 * 自分の役職割当数更新
	 * @param role 役職
	 */
	public void setAssignCount(Role role) {
		switch (role) {
		case VILLAGER:
			addAssignCount(0);
			break;
		case SEER:
			addAssignCount(1);
			break;
		case MEDIUM:
			addAssignCount(2);
			break;
		case BODYGUARD:
			addAssignCount(3);
			break;
		case POSSESSED:
			addAssignCount(4);
			break;
		case WEREWOLF:
			addAssignCount(5);
			break;
		default:
			break;		
		}
	}
	
	/*-----------------------------戦略の情報---------------------------------------*/
	
	/**
	 * 戦略の情報マップ取得
	 * @return 戦略の情報マップ
	 */
	public Map<Role, List<StrategyInfo>> getStrategyMap() {
		return strategyMap;
	}
	
	/**
	 * 役職が経験した戦略情報のリスト取得
	 * @param role :役職
	 * @return 戦略情報のリスト
	 */
	public List<StrategyInfo> getStrategyList(Role role) {
		List<StrategyInfo> strategyList = new ArrayList<>();
		if(Check.isNotNull(this.getStrategyMap().get(role))) {
			return this.getStrategyMap().get(role);
		}
		return strategyList;
	}
	
	/**
	 * 戦略情報の更新
	 * @param role :戦略をとった役職
	 * @param strategy :戦略
	 * @param win :勝利したかどうか(0or1)
	 */
	public void setStrategyMap(Role role, Strategy strategy, int win) {
		// 戦略情報を保持するリスト
		List<StrategyInfo> strategyList = new ArrayList<>();
		// 既に役職が登録されていれば戦略情報リストを取得
		if(!strategyMap.isEmpty() || strategyMap.containsKey(role)) {
			strategyList = this.getStrategyList(role);
		}
		
		// 中身が空でなければ，登録されている戦略を確認
		if(!strategyList.isEmpty()) {
			for(int i=0; i<strategyList.size(); i++) {
				// リスト内の戦略情報取得
				StrategyInfo si = strategyList.get(i);
				// 戦略が既に登録されている
				if(si.getStrategy() == strategy) {
					// 使用確率変動
					double use = si.getUse();
					// 使用回数と勝利数を更新(勝利ならば0.1加算，敗北ならば減産)
					if(Check.isNum(win, 1)) {
						use = calDecimal(use + 0.1);
					}else {
						if(use >= 0.1) {
							use = calDecimal(use - 0.1);
						}
					}
					// 勝利回数と敗北回数カウント
					int winCount = si.getWin() + win;
					int loseCount = si.getLose() + 1 - win;
					// 情報更新
					si.setInfo(use, winCount, loseCount);
					strategyList.set(i, si);
				}
			}
		}
		strategyMap.put(role, strategyList);
	}
	
	/**
	 * 村陣営が勝利時の戦略情報更新
	 * @param entityData :オブジェクトデータ
	 */
	public void setVillagerStrategy(EntityData entityData) {
		switch (entityData.getOwnData().getMyRole()) {
		case VILLAGER:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 1);
			break;
		case SEER:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 1);
			break;
		case MEDIUM:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 1);
			break;
		case BODYGUARD:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 1);
			break;
		case POSSESSED:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 0);
			break;
		case WEREWOLF:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 0);
			break;
		default:
			break;		
		}
	}

	/**
	 * 狼陣営が勝利時の戦略情報更新
	 * @param entityData :オブジェクトデータ
	 */
	public void setWolfStrategy(EntityData entityData) {
		switch (entityData.getOwnData().getMyRole()) {
		case VILLAGER:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 0);
			break;
		case SEER:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 0);
			break;
		case MEDIUM:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 0);
			break;
		case BODYGUARD:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 0);
			break;
		case POSSESSED:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 1);
			break;
		case WEREWOLF:
			this.setStrategyMap(entityData.getOwnData().getMyRole(), entityData.getOwnData().getStrategy(), 1);
			break;
		default:
			break;		
		}
	}
	
	/**
	 * 役職ごとの戦略情報を表示する
	 * @param role :役職
	 */
	public void showStrategy(Role role) {
		List<Strategy> notUseList = new ArrayList<>();
		for(StrategyInfo si: this.getStrategyList(role)) {
			if(Check.isNum(si.getWin(), 0) && Check.isNum(si.getLose(), 0)) {
				notUseList.add(si.getStrategy());
			}else{
			}
		}
		if(!notUseList.isEmpty()) {
		}
	}
	
	/**
	 * 初期起動時，役職ごとの戦略一覧作成
	 * @param role :役職
	 */
	public void createStrategyMap(Role role) {
		// マップが空または未作成ならば一覧作成
		if(strategyMap.isEmpty() || !strategyMap.containsKey(role)) {
			// 格納するリスト
			List<StrategyInfo> infoList = new ArrayList<>();
			for(Strategy strategy: Strategy.applyList(role)) {
				infoList.add(new StrategyInfo(strategy));
			}
			strategyMap.put(role, infoList);
		}
	}
	
	/**
	 * 役職ごとの戦略を適用確率からランダム選択する
	 * @param role :役職
	 * @return 適用される戦略
	 */
	public Strategy selectStrategy(Role role) {
		List<StrategyInfo> infoList = this.getStrategyList(role);
		double use = 0;
		for(StrategyInfo si: infoList) {
			use = calDecimal(use + si.getUse());
		}
		// 適用する戦略の値をランダム選択
		double decideUse = Math.random() * use;
		double baseUse = 0;
		for(StrategyInfo si: infoList) {
			if(baseUse <= decideUse && decideUse <= baseUse + si.getUse()) {
				return si.getStrategy();
			}
			// 適用範囲外ならば基準値更新
			baseUse = calDecimal(baseUse + si.getUse());
		}
		
		// 全て適用外ならばNONE
		return Strategy.NONE;
	}
	
	
	/*-----------------------------計算----------------------------------------*/
	
	/**
	 * 村陣営の勝利数に対して自分が村に所属した際の勝率
	 * @return 自分が村に所属した際の勝率
	 */
	public double percentageVillager() {
		double value = 0;
		if(!Check.isNum(getVillageWin(), 0)) {
			value = (double)getMyVillageWin()/getVillageWin();
		}
		return calDecimal(value);
	}
	
	/**
	 * 狼陣営の勝利数に対して自分が狼に所属した際の勝率
	 * @return 自分が狼に所属した際の勝率
	 */
	public double percentageWolf() {
		double value = 0;
		if(!Check.isNum(getWolfWin(), 0)) {
			value = (double)getMyWolfWin()/getWolfWin();
		};
		return calDecimal(value);
	}
	
	/**
	 * 所属した陣営が勝利した際の役職ごとの勝率
	 * @param role :役職
	 * @return 勝利陣営の役職ごと勝率
	 */
	public double percentageRole(Role role) {
		double value = 0;
		int win = getMyWin(role);
		int teamWin = 0;
		if(Check.isRole(role, Role.POSSESSED) || Check.isRole(role, Role.WEREWOLF)) {
			teamWin = getMyWolfWin();
		}else {
			teamWin = getMyVillageWin();
		}
		if(!Check.isNum(teamWin, 0)) {
			value = (double)win/teamWin;
		}
		return calDecimal(value);
	}
	
	/**
	 * 割当てられた役職の勝率(割当て回数に準ずる)
	 * @param role :割当てられた役職
	 * @return 割当て勝率
	 */
	public double percentageAssignRole(Role role) {
		double value = 0;
		int win = getMyWin(role);
		int assignCount = getAssignCount(role);
		if(!Check.isNum(assignCount, 0)) {
			value = (double)win/assignCount;
		}
		return calDecimal(value);
	}
	
	/**
	 * 少数整形
	 * @param value :値
	 * @return 整形済み値
	 */
	public double calDecimal(double value) {
		BigDecimal bd = new BigDecimal(value);
		BigDecimal bd2 = bd.setScale(2, BigDecimal.ROUND_HALF_UP); 
		return bd2.doubleValue();
	}
	
	/*-----------------------------表示---------------------------------------*/
	
	/**
	 * それぞれの勝利数を表示
	 */
	public void showWin() {
		Debug.print("ゲーム数 = " + gameCount + " 村陣営 = " + getVillageWin() + " : 狼陣営 = " + getWolfWin());
		Debug.print("自分所属時勝利数 ：村 = " + getMyVillageWin() + " 狼 = " + getMyWolfWin());
		Debug.print("自分所属村勝率 = " + percentageVillager() + " : 自分所属狼勝率 = " + percentageWolf());
		Debug.print("合計勝率 = " + calDecimal((double)(getMyVillageWin()+getMyWolfWin())/gameCount));
		
		Debug.print("村 : 勝利数 = " + getMyWin(Role.VILLAGER) + ", 割当て回数 = " + getAssignCount(Role.VILLAGER));
		Debug.print(" [割当て勝率 " + percentageAssignRole(Role.VILLAGER) + "]" + " [陣営所属勝率 " + percentageRole(Role.VILLAGER) + "]");
		showStrategy(Role.VILLAGER);
		Debug.print("占 : 勝利数 = " + getMyWin(Role.SEER) + ", 割当て回数 = " + getAssignCount(Role.SEER));
		Debug.print(" [割当て勝率 " + percentageAssignRole(Role.SEER) + "]" + " [陣営所属勝率 " + percentageRole(Role.SEER) + "]");
		showStrategy(Role.SEER);
		Debug.print("霊 : 勝利数 = " + getMyWin(Role.MEDIUM) + ", 割当て回数 = " + getAssignCount(Role.MEDIUM));
		Debug.print(" [割当て勝率 " + percentageAssignRole(Role.MEDIUM) + "]" + " [陣営所属勝率 " + percentageRole(Role.MEDIUM) + "]");
		showStrategy(Role.MEDIUM);
		Debug.print("狩 : 勝利数 = " + getMyWin(Role.BODYGUARD) + ", 割当て回数 = " + getAssignCount(Role.BODYGUARD));
		Debug.print(" [割当て勝率 " + percentageAssignRole(Role.BODYGUARD) + "]" + " [陣営所属勝率 " + percentageRole(Role.BODYGUARD) + "]");
		showStrategy(Role.BODYGUARD);
		Debug.print("狂 : 勝利数 = " + getMyWin(Role.POSSESSED) + ", 割当て回数 = " + getAssignCount(Role.POSSESSED));
		Debug.print(" [割当て勝率 " + percentageAssignRole(Role.POSSESSED) + "]" + " [所属所属陣営勝率 " + percentageRole(Role.POSSESSED) + "]");
		showStrategy(Role.POSSESSED);
		Debug.print("狼 : 勝利数 = " + getMyWin(Role.WEREWOLF) + ", 割当て回数 = " + getAssignCount(Role.WEREWOLF));
		Debug.print(" [割当て勝率 " + percentageAssignRole(Role.WEREWOLF) + "]" + " [所属所属陣営勝率 " + percentageRole(Role.WEREWOLF) + "]");
		showStrategy(Role.WEREWOLF);
	}
}
