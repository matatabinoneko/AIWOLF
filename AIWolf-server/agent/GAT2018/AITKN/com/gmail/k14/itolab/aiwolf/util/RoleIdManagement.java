package com.gmail.k14.itolab.aiwolf.util;

import com.gmail.k14.itolab.aiwolf.definition.RoleId;

/**
 * 役職のID情報
 * @author k14096kk
 *
 */
public class RoleIdManagement {

	/**
	 * 役職IDの合計値
	 * 適用する役職の引数に1をいれて使用する
	 * @param v :村人
	 * @param s :占い師
	 * @param m :霊媒師
	 * @param b :狩人
	 * @param p :狂人
	 * @param w :人狼
	 * @return ID合計値
	 */
	public static int sumId(int v, int s, int m, int b, int p, int w) {
		int sum = 0;
		
		if(v>=1) {
			sum += RoleId.VILLAGER_ID.getId();
		}
		if(s>=1) {
			sum += RoleId.SEER_ID.getId();
		}
		if(m>=1) {
			sum += RoleId.MEDIUM_ID.getId();
		}
		if(b>=1) {
			sum += RoleId.BODYGUARD_ID.getId();
		}
		if(p>=1) {
			sum += RoleId.POSSESSED_ID.getId();
		}
		if(w>=1) {
			sum += RoleId.WEREWOLF_ID.getId();
		}
		
		return sum;
	}
	
}
