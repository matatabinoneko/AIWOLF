package com.gmail.k14.itolab.aiwolf.util;

import org.aiwolf.common.data.Agent;

/**
 * 回数(発話数など)とエージェントの関係クラス(並び替え用)
 * @author k14096kk
 *
 */
public class CountAgent {

	/**回数*/
	public double count;
	/**エージェント*/
	public Agent agent;
	
	/**
	 * エージェントと値の関係を作成
	 * @param count :値
	 * @param agent :エージェント
	 */
	public CountAgent(double count, Agent agent) {
		this.count = count;
		this.agent = agent;
	}
	
	/**
	 * エージェントを取得
	 * @return エージェント
	 */
	public Agent getAgent() {
		return this.agent;
	}
	
	/**
	 * 値を取得
	 * @return 値
	 */
	public double getCount() {
		return this.count;
	}
	
	@Override
	public String toString() {
		return agent + "=" + count;
	}
}
