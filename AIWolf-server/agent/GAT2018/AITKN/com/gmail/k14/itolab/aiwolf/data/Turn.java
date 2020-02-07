package com.gmail.k14.itolab.aiwolf.data;

/**
 * 会話のターンを管理するクラス<br>
 * 0~10までの値をもつ．0:0ターン前，1~9:talk.getTurn()の前(1ならばtalk.getTurn()==0のとき)
 * @author k14096kk
 *
 */
public class Turn {

	/**ターン*/
	int turn = 0;
	
	/**
	 * ターン処理の作成
	 */
	public Turn() {
		this.turn = 0;
	}
	
	/**
	 * 指定ターンの更新
	 * @param t :ターン
	 */
	public void setTurn(int t) {
		turn = t;
	}
	
	/**
	 * 現在のターンを取得
	 * @return 現在ターン
	 */
	public int getTurn() {
		return turn;
	}
	
	/**
	 * ターンの更新(1ターン進む)
	 */
	public void updateTurn() {
		turn++;
	}
	
	/**
	 * 現在ターンが指定ターンかどうか<br>
	 * 指定ターンは会話を見るターンなので，指定ターンの次ターン以降の発言に影響する
	 * @param t :指定ターン(会話を見るターン)
	 * @return 指定ターンならばtrue,異なればfalse
	 */
	public boolean currentTurn(int t) {
		if(turn == t) {
			return true;
		}
		return false;
	}
	
	/**
	 * 現在ターンが指定ターン以前かどうか(指定ターンを含む)
	 * @param t :指定ターン(会話を見るターン)
	 * @return 以降ならばtrue,以前ならばfalse
	 */
	public boolean beforeTurn(int t) {
		if(turn <= t) {
			return true;
		}
		return false;
	}
	
	/**
	 * 現在ターンが指定ターン以降かどうか(指定ターンを含む)
	 * @param t :指定ターン(会話を見るターン)
	 * @return 以降ならばtrue,以前ならばfalse
	 */
	public boolean afterTurn(int t) {
		if(turn >= t) {
			return true;
		}
		return false;
	}
	
	/**
	 * 引数間のターンかどうか(引数のターンは含まない)
	 * @param before :何日から
	 * @param after :何日まで
	 * @return 間にあればtrue,なければfalse
	 */
	public boolean whileTurn(int before, int after) {
		if(turn > before && turn < after) {
			return true;
		}
		return false;
	}
	
	/**
	 * 0ターン目の前かどうか
	 * @return 0ターン目の前ならばtrue,異なればfalse
	 */
	public boolean startTurn() {
		if(turn==0) {
			return true;
		}
		return false;
	}
	
	/**
	 * ターンをTalk前の状態に戻す
	 */
	public void resetTurn() {
		turn = 0;
	}
}
