package com.gmail.k14.itolab.aiwolf.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.data.GameResult;

/**
 * デバッグ用クラス
 * @author k14096kk
 *
 */
public class Debug {
	
	/**デバッグの表示の有無*/
	private static boolean isDebug = false;
	/**エラーの表示の有無<br>isDebugがfalseのときのみ使用*/
	private static boolean isCheckError = false;
	
	/**ゲーム数格納リスト*/
//	private static List<Integer> passGameCount = new ArrayList<>();
	/**日付格納リスト*/
	private static List<Integer> passGameDay = new ArrayList<>();
	/**クラス格納リスト*/
	private static List<String> passClass = new ArrayList<>();
	/**行番号格納リスト*/
	private static List<Integer> passLine = new ArrayList<>();
	/**表示文字列格納リスト*/
	private static List<String> passString = new ArrayList<>();
	/**エラー文格納リスト*/
	private static List<String> passError = new ArrayList<>();
	
	private static EntityData entityData;
	
	
	/**
	 * デバッグ表示するかどうか
	 * @return 表示するならばtrue, しなければfalse
	 */
	public static boolean isDebug() {
		return isDebug;
	}
	
	/**
	 * エラー表示するかどうか
	 * @return 表示するならばtrue, しなければfalse
	 */
	public static boolean isCheckError() {
		return isCheckError;
	}
	
	/**
	 * デバッグ表示するならば，System.out.println()を実行
	 * @param text :表示する文字列
	 */
	public static <T> void print(T text) {
		if(isDebug) System.out.println(text);
	}
	
	/**
	 * デバッグ表示するならば，System.err.println()を実行
	 * @param text :表示する文字列
	 */
	public static <T> void error(T text) {
		if(isDebug) System.err.println(text);
	}
	
	/**
	 * デバッグ表示
	 * @param e : インスタンスデータ
	 * @param gameResult : ゲーム結果
	 */
	public static void show(EntityData e, GameResult gameResult){
		entityData = e;
		
		print("--------------デバッグ-------------------");
		print("ゲーム数 = " + gameResult.getGameCount());
		print(entityData.getOwnData().getMe().getAgentIdx() + " = " + entityData.getOwnData().getGameInfo().getStatusMap().get(entityData.getOwnData().getMe()));
		print("役職 = " + entityData.getOwnData().getGameInfo().getRoleMap().get(entityData.getOwnData().getMe()));
		print("日付 = " + entityData.getOwnData().getGameInfo().getDay());
		print("吊り手 = " + entityData.getOwnData().getCountHang());
		print("生存人数 = " + entityData.getOwnData().getAliveAgentList().size());
		print("フラグ状況 = " + entityData.getOwnData().getActFlag());
		print("投票対象 = " + entityData.getOwnData().getVoteTarget());
		print("占い対象 = " + entityData.getOwnData().getDivineTarget());
		print("襲撃対象 = " + entityData.getOwnData().getAttackTarget());
		print("追放 -> " + entityData.getOwnData().getExecutedAgent());
		print("襲撃 -> " + entityData.getOwnData().getAttackedAgent());
		print("---VOTE---");
		for(Vote vote: entityData.getOwnData().getGameInfo().getVoteList()) {
			print(vote.getAgent() + "->" + vote.getTarget());
		}
		print("---ForecastMap---");
		for(Agent agent: entityData.getForecastMap().getMap().keySet()) {
			print(agent + " : " + entityData.getForecastMap().getMap().get(agent));
		}
		print("CO履歴 : " + entityData.getCoHistory().getComingoutHistoryMap());
		for(Agent agent: entityData.getOwnData().getDivineResultMap().keySet()) {
			print("DIVINE : " + agent + " = " + entityData.getOwnData().getDivineResultSpecies(agent));
		}
		print("VotedCount");
		for(Agent agent: entityData.getVoteCounter().getAllMap().keySet()) {
			print(" " + agent + " : VotedCount = " + entityData.getVoteCounter().getAllMap().get(agent));
		}
		if(Check.isRole(entityData.getOwnData().getMyRole(), Role.BODYGUARD)) {
			print("護衛結果一覧");
			print(entityData.getOwnData().getGuardResultMap());
		}
		if(Check.isRole(entityData.getOwnData().getMyRole(), Role.WEREWOLF)) {
			print("襲撃結果一覧");
			print(entityData.getOwnData().getAttackResultMap());
		}
		print("----------------------------------");
	}
	
	/**
	 * ゲーム勝利数表示
	 * @param gameResult : ゲーム結果
	 */
	public static void showWin(GameResult gameResult) {
		gameResult.showWin();
	}
	
	/**
	 * 実行中のクラス名取得
	 * @return クラス名
	 */
	public static String getClassName() {
		return Thread.currentThread().getStackTrace()[2].getClassName();
	}
	
	/**
	 * 実行中のメソッド名取得
	 * @return メソッド名
	 */
	public static String getMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}
	
	/**
	 * 実行中の行番号取得
	 * @return 行番号
	 */
	public static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getLineNumber();
	}
	
	/**
	 * 呼び出しもとを表示
	 * @param day :ゲーム日数
	 * @param c :呼び出しクラス
	 * @param line :呼び出し行番号
	 */
	public static void setPass(int day, String c, int line) {
//		passGameCount.add(GameResult.getGameCount());
		passGameDay.add(day);
		passClass.add(c);
		passLine.add(line);
		setPassString("Not Appoint");
	}
	
	/**
	 * 呼び出しもとを表示
	 * @param day :ゲーム日数
	 * @param c :呼び出しクラス
	 * @param line :呼び出し行番号
	 * @param object :表示オブジェクト
	 */
	public static void setPass(int day, String c, int line, Object object) {
//		passGameCount.add(GameResult.getGameCount());
		passGameDay.add(day);
		passClass.add(c);
		passLine.add(line);
		setPassString(object);
	}
	
	/**
	 * オブジェクトを文字列で設定する
	 * @param object :オブジェクト
	 */
	public static void setPassString(Object object) {
		if(Check.isNotNull(object)) {
			passString.add(object.toString());
		}else {
			passString.add("NULL");
		}
	}
	
	
	
	/**
	 * 経由したチェックを確認
	 */
	public static void checkPass() {
		/*
		 * 使い方
		 * Debug.setPass(entityData.getOwnData().getDay(), Debug.getClassName(), Debug.getLineNumber(), "DEBUG");
		 * を確認したい行に記述
		 * */
		print("-----------PASS--------------");
		for(int i=0; i<passClass.size(); i++) {
			print(passGameDay.get(i) + "Day"+ " : " + passClass.get(i) + " = " + passLine.get(i));
			print(" -> " + passString.get(i));
		}
	}
	
	/**
	 * 通過情報をクリア
	 */
	public static void cleaerCheck() {
//		passGameCount = new ArrayList<>();
		passGameDay = new ArrayList<>();
		passClass = new ArrayList<>();
		passLine = new ArrayList<>();
		passString = new ArrayList<>();
	}
	
	/**
	 * エラー文をリストにためる
	 * @param sw :表示文
	 */
	public static void stackError(StringWriter sw) {
		passError.add(sw.toString());
	}
	
	/**
	 * 確認できたエラーを表示
	 */
	public static  void showError() {
		print("-----------ERROR--------------");
		for(int i=0; i<passError.size(); i++) {
			print(entityData.getOwnData().getDay() + "Day" + entityData.getTurn().getTurn() + "Turn \n" + passError.get(i));
		}
	}

}
