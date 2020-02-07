package com.gmail.k14.itolab.aiwolf.util;

import org.aiwolf.common.data.Agent;

/**
 * 能力者の能力発動結果を管理するクラス<br>
 * 「狩人の護衛結果」「人狼の襲撃結果」の管理に用いる
 * @author k14096kk
 *
 */
public class AbilityResultInfo {
	
	/**能力の発動日付*/
	public int day;
	/**能力の対象エージェント(護衛先，襲撃先)*/
	public Agent target;
	/**能力結果の成否*/
	public boolean success;
	
	/**
	 * 能力の発動結果
	 * @param day :発動日
	 * @param target :能力の発動対象
	 * @param success :能力の発動結果(成功or失敗)
	 */
	public AbilityResultInfo(int day, Agent target, boolean success) {
		this.day = day;
		this.target = target;
		this.success = success;
	}
	
	/**
	 * 能力の発動日を取得する
	 * @return 発動日
	 */
	public int getDay() {
		return this.day;
	}
	
	/**
	 * 能力の対象エージェントを取得する
	 * @return 対象エージェント
	 */
	public Agent getTarget() {
		return this.target;
	}
	
	/**
	 * 能力の結果を取得する
	 * @return 成功ならばtrue,失敗ならばfalse
	 */
	public boolean getSuccess() {
		return this.success;
	}
	
	@Override
	public String toString() {
		String text = this.target + "->" + this.success;
		return text;
	}
	
}
