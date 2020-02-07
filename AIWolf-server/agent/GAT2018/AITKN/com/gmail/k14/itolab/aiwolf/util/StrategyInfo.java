package com.gmail.k14.itolab.aiwolf.util;

import com.gmail.k14.itolab.aiwolf.definition.Strategy;

/**
 * 戦略情報
 * @author k14096kk
 *
 */
public class StrategyInfo {

	/**戦略*/
	public Strategy strategy;
	/**使用確率*/
	public double use = 0;
	/**勝利回数*/
	public int win = 0;
	/**敗北回数*/
	public int lose = 0;
	
	/**
	 * 戦略ごとの使用確率と勝利数,敗北数を管理する
	 * @param strategy :戦略
	 */
	public StrategyInfo(Strategy strategy) {
		this.strategy = strategy;
		this.use = 1;
		this.win = 0;
		this.lose = 0;
	}
	
	/**
	 * 戦略取得
	 * @return　戦略
	 */
	public Strategy getStrategy() {
		return this.strategy;
	}
	
	/**
	 * 使用確率取得
	 * @return 使用回数
	 */
	public double getUse() {
		return this.use;
	}
	
	/**
	 * 勝利回数取得
	 * @return 勝利数
	 */
	public int getWin() {
		return this.win;
	}
	
	/**
	 * 敗北回数取得
	 * @return 敗北回数
	 */
	public int getLose() {
		return this.lose;
	}
	
	/**
	 * 使用回数を更新
	 * @param use :使用回数
	 */
	public void setUse(double use) {
		this.use = use;
	}
	
	/**
	 * 勝利数を更新
	 * @param win :勝利数
	 */
	public void setWin(int win) {
		this.win = win;
	}
	
	/**
	 * 敗北数を更新
	 * @param lose :敗北数
	 */
	public void setLose(int lose) {
		this.lose = lose;
	}
	
	/**
	 * 戦略に関する情報の更新
	 * @param use :使用回数
	 * @param win :勝利数
	 * @param lose :敗北数
	 */
	public void setInfo(double use, int win, int lose) {
		setUse(use);
		setWin(win);
		setLose(lose);
	}
	
	@Override
	public String toString() {
		return strategy + " -> Use=" + use + " Win=" + win + " Lose=" + lose;
	}
}
