package com.gmail.k14.itolab.aiwolf.util;


import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;

/**
 * COの情報を管理するクラス
 * @author k14096kk
 *
 */
public class CoInfo {
	
	/**発話者*/
	public Agent agent;
	/**CO対象*/
	public Agent subject;
	/**CO役職*/
	public Role role;
	/**COした日付*/
	public int day;
	/**COした発話ID*/
	public int id;
	/**COしたターン*/
	public int turn;
	

	/**
	 * CO情報作成
	 * @param agent :エージェント
	 * @param subject :CO対象エージェント
	 * @param role :CO役職
	 * @param day :COした日付
	 * @param id :COした発話ID
	 * @param turn :COしたターン
	 */
	public CoInfo(Agent agent, Agent subject, Role role, int day, int id, int turn) {
		this.agent = agent;
		if(subject==null) {
			this.subject = agent;
		}else {
			this.subject = subject;
		}
		this.role = role;
		this.day = day;
		this.id = id;
		this.turn = turn;
	}
	
	/**
	 * CO情報作成
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public CoInfo(Talk talk, Content content) {
		this.agent = talk.getAgent();
		if(content.getSubject()==null) {
			this.subject = talk.getAgent();
		}else {
			this.subject = content.getSubject();
		}
		this.role = content.getRole();
		this.day = talk.getDay();
		this.id = talk.getIdx();
		this.turn = talk.getTurn();
	}
	
	/**
	 * 発話者取得
	 * @return 発話したエージェント
	 */
	public Agent getAgent() {
		return this.agent;
	}
	
	/**
	 * CO対象取得
	 * @return COしたエージェント
	 */
	public Agent getSubject() {
		return this.subject;
	}
	
	/**
	 * CO役職取得
	 * @return COした役職
	 */
	public Role getRole() {
		return this.role;
	}
	
	/**
	 * COした日付取得
	 * @return COした日付
	 */
	public int getDay() {
		return this.day;
	}
	
	/**
	 * COした発話ID取得
	 * @return COした発話ID
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * COしたターン取得
	 * @return COしたターン
	 */
	public int getTurn() {
		return this.turn;
	}
	
	/**
	 * CO情報表示
	 */
	public String toString() {
		//return "CO発話者" + this.agent + "(" + this.subject + ") " + this.role + "->DAY" + this.day + ":ID" + this.id + ":TURN" + this.turn;
		return "CO:" + this.role;
	}
	
}
