package com.carlo.aiwolf.bayes.trust;

import org.aiwolf.common.data.Role;
import com.carlo.aiwolf.bayes.trust.Correct;

/**
 * AIWolfMarkで生成したログとBayesネットワークで使うバリューのコンバート
 * 15人用
 * @author carlo
 *
 */


public class BayesConverter15 {
	public static String convertDay(int day){
		if(day>7) return "later";
		else return String.valueOf(day);
	}
	public static String convertRoleToSpecies(Role role){
		if(role==Role.WEREWOLF) return "werewolf";
		else return "human";
	}
	public static String convert(Correct correct){
		if(correct==Correct.YES) return "true";
		else if(correct==Correct.NO) return "false";
		else  return "";
	}
	public static String convertRole(Role role){
		if(role==null) return "null";
		else return role.toString();
	}
}
