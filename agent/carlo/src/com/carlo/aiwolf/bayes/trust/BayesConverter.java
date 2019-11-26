package com.carlo.aiwolf.bayes.trust;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

/**
 *  人狼知能のデータ形式をwekaで使うデータ形式に変換
 * @author carlo
 *
 */
public class BayesConverter {
	public static String convert(int day){
		switch(day){
		case 1: return "1";
		case 2: return "2";
		case 3: return "3";
		case 4: return "4";
		default : return "later";
		}
	}
	public static String convert(Species species){
		switch(species){
		case HUMAN:
			return "human";
		case WEREWOLF:
			return "werewolf";
		}
		return null;
	}
	public static String convert(Correct correct){
		if(correct==Correct.YES) return "yes";
		else return "no";
	}
	public static String convert(Role role){
		if(role==null) return "none";
		switch(role){
		case SEER:
			return "seer";
		case MEDIUM:
			return "medium";
		case BODYGUARD:
			return "bodyguard";
		case POSSESSED:
			return "possessed";
		case WEREWOLF:
			return "werewolf";
		case VILLAGER:
			return "villager";
		default:
			break;
		}
		return "none";
	}

}
