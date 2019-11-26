package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

/**
 * AbilityResultのリストを管理するクラス
 * @author carlo
 *
 */

public class AbilityResultManager {
	private ArrayList<AbilityResult> resultList=new ArrayList<AbilityResult>();
	/**
	 * 
	 * @param topic INQUESTED,DIVINED,GUARDED
	 * @param day 能力行使日 <br> -1:前回の結果からインクリメントした値を入れる(前回がなければ占いは0,霊媒は1) それ以外ならそのまま入れる
	 * @param talkedDay 発言した日
	 * @param target 能力行使先のエージェント
	 * @param species 能力行使結果。GUARDEDでは使わない。
	 */
	public void addAbilityResult(Topic topic,int day,int talkedDay,Agent agent,Agent target,Species species){
		if(day==-1 ){
			if(resultList.size()>0) day=resultList.get(resultList.size()-1).getDay()+1;
			else {
				if(topic==Topic.DIVINED) day=0;
				else if(topic==Topic.IDENTIFIED || topic==Topic.GUARDED) day=1;
			}
		}
		resultList.add(new AbilityResult(topic,day,talkedDay,agent,target,species));
		
	}
	public void printList(){
		for(AbilityResult result:resultList){
			System.out.println(result);
		}
	}
	/** resultのtargetが一致するものを探す 線形探索するよ */
	public AbilityResult getAbilityResult(Agent target){
		for(AbilityResult result:resultList){
			if(result.getTarget()==target) return result;
		}
		return null;
	}
	public List<AbilityResult> getAbilityResultList(){
		return resultList;
	}
	/**abilityResultのspeciesが一致するものを探してリストで返す。targetが重複しているのものは省く。  */
	public List<AbilityResult> searchAbilityResult(Species species){
		ArrayList<AbilityResult> list=new ArrayList<AbilityResult>(); 
		ArrayList<Agent> addAgentList=new ArrayList<Agent>();
		for(AbilityResult result:resultList){
			if(result.getSpecies()==species && addAgentList.contains(result.getTarget())==false) {
				list.add(result);
				addAgentList.add(result.getTarget());
			}
		}
		return list;
	}
	/**abilityResultのspeciesが一致するものを探してリストで返す。重複したものは省く。targetがCOしたのも省く  */
	public List<AbilityResult> searchAbilityResultNoCO(Species species,COInfo coInfo){
		ArrayList<AbilityResult> list=new ArrayList<AbilityResult>(); 
		ArrayList<Agent> addAgentList=new ArrayList<Agent>();
		for(AbilityResult result:resultList){
			if(result.getSpecies()==species && addAgentList.contains(result.getTarget())==false && coInfo.getCoRole(result.getTarget())==null) {
				list.add(result);
				addAgentList.add(result.getTarget());
			}
		}
		return list;
	}

}
