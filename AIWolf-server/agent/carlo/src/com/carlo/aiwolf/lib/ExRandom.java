package com.carlo.aiwolf.lib;

import java.util.List;
import java.util.Random;

import org.aiwolf.common.data.Agent;
/**
 * Randomを利用するメソッドをまとめたクラス
 * @author carlo
 *
 */
public class ExRandom {
	/** listからランダムに選択したagentを返す <br><br>
	 * 投票先を決めてtalkCreatorに渡す（talkCreater.setVoteTarget()する）場合はこちらのメソッドではなく、
	 * select(List<Agent> agentList,Agent nowSelectedAgent)を使うべきです。 */
	public static Agent select(List<Agent> agentList){
		int num=new Random().nextInt(agentList.size());
		return agentList.get(num);
	}
	/** nowSelectedAgentがagentListに含まれている場合、nowSelectedAgentを返す。<br>それ以外ならlistからランダムで選択して返す<br><br>
	 * 投票先を決めてtalkCreatorに渡す（talkCreater.setVoteTarget()する）場合はこのメソッドを使うべきです。（毎回対象が変わると発言しなおすので） */
	public static Agent select(List<Agent> agentList,Agent nowSelectedAgent){
		if(agentList.contains(nowSelectedAgent)) return nowSelectedAgent;
		else {
			int num=new Random().nextInt(agentList.size());
			return agentList.get(num);
		}
	}
	/** numerator/denominator の確率で当たるクジを行い、当たればtrueを返す */
	public static boolean checkHit(int numerator,int denominator){
		int num=new Random().nextInt(denominator);
		if(num<numerator) return true;
		else return false;
	}

}
