package com.gmail.k14.itolab.aiwolf.util;


import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;

/**
 * if文の条件をまとめたクラス
 * @author k14096kk
 *
 */
public class Check {

	/**
	 * 役職が比較役職と同じかどうか
	 * @param role :役職
	 * @param comRole :比較役職
	 * @return 同じならばtrue,異なればfalse
	 */
	public static boolean isRole(Role role, Role comRole) {
		if (role == comRole) {
			return true;
		}
		return false;
	}
	
	/**
	 * 種族が比較種族と同じかどうか
	 * @param spe :種族
	 * @param comSpe :比較種族
	 * @return 同じならばtrue,異なるならばfalse
	 */
	public static boolean isSpecies(Species spe, Species comSpe) {
		if(spe == comSpe) {
			return true;
		}
		return false;
	}
	
	/**
	 * トピックが比較トピックと同じかどうか
	 * @param topic :トピック
	 * @param comTopic :比較トピック
	 * @return 同じならばtrue,異なるならばfalse
	 */
	public static boolean isTopic(Topic topic, Topic comTopic) {
		if(topic == comTopic) {
			return true;
		}
		return false;
	}
	
	/**
	 * エージェントが比較エージェントと同じかどうか
	 * @param agent :エージェント
	 * @param comAgent :比較エージェント
	 * @return 同じならばtrue,異なるならばfalse
	 */
	public static boolean isAgent(Agent agent, Agent comAgent) {
		if(agent == comAgent) {
			return true;
		}
		return false;
	}
	
	/**
	 * ステータスが比較ステータスと同じかどうか
	 * @param status :ステータス
	 * @param comStatus :比較ステータス
	 * @return 同じならばtrue,異なればfalse
	 */
	public static boolean isStatus(Status status, Status comStatus) {
		if(status == comStatus) {
			return true;
		}
		return false;
	}
	
	/**
	 * 値が比較値と同じかどうか
	 * @param num :値
	 * @param comNum :比較値
	 * @return 同じならばtrue,異なればfalse
	 */
	public static boolean isNum(int num, int comNum) {
		if (num == comNum) {
			return true;
		}
		return false;
	}
	
	/**
	 * 引数がnullかどうか
	 * @param obj :引数
	 * @return nullならばtrue,違うならばfalse
	 */
	public static boolean isNull(Object obj) {
		if (obj == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 引数がnullではないかどうか
	 * @param obj :引数
	 * @return nullでないならばtrue,nullならばfalse
	 */
	public static boolean isNotNull(Object obj) {
		if (obj == null) {
			return false;
		}
		return true;
	}
}
