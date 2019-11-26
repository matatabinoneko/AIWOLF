package com.carlo.aiwolf.base.lib;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
/**
 * Ver3.X系のクラス設計のままで運用したかったので、MyAbstractRoleを作成し
 * AbstractVillagerなどはこのクラスを継承させている
 * @author carlo
 *
 */
public abstract class MyAbstractRole implements Player{

	protected int day;
	protected Agent me;
	protected Role myRole;
	protected GameInfo currentGameInfo;
	
	public GameInfo getLatestDayGameInfo() {
		return currentGameInfo;
	}
	public Agent getMe(){
		return me;
	}
	public int getDay() {
		return day;
	}
	public Role getMyRole() {
		return myRole;
	}
	@Override
	public void dayStart() {
		System.out.println("dayStart day:"+getDay());
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		// TODO 自動生成されたメソッド・スタブ
		day = -1;
		me = gameInfo.getAgent();
		myRole = gameInfo.getRole();
	}

	@Override
	public void update(GameInfo gameInfo) {
		// TODO 自動生成されたメソッド・スタブ
		currentGameInfo = gameInfo;
		day = currentGameInfo.getDay();
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "";
	}
}
