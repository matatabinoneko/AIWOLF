package com.carlo.aiwolf.lib.info;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;
/**
 * 能力結果 <br>
 * INQUESTED,DIVINED,GUARDEDの結果情報をまとめたもの <br><br>
 * ほとんどJUDGEクラスみたいな感じ
 * @author carlo
 *
 */
public class AbilityResult {
	/** INQUESTED,DIVINED,GUARDEDのみ */
	private Topic topic;
	/** その能力を実行したと思われる日付。発言した日ではない */
	private int day;
	/** 発言した日 */
	private int talkedDay;
	/** 能力行使者 */
	private Agent agent;
	/** 能力行使先 */
	private Agent target;
	/** 結果。護衛結果では使わない */
	private Species species;
	
	public AbilityResult(Topic topic,int day,int talkedDay,Agent agent,Agent target,Species species){
		this.agent=agent;
		this.topic=topic;
		this.day=day;
		this.talkedDay=talkedDay;
		this.target=target;
		this.species=species;
	}
	public int getTalkedDay(){
		return talkedDay;
	}
	public int getDay(){
		return day;
	}
	public Topic getTopic(){
		return topic;
	}
	public Agent getAgent(){
		return agent;
	}
	public Agent getTarget(){
		return target;
	}
	public Species getSpecies(){
		return species;
	}
	
	public String toString(){
		if(species!=null) return "agent:"+agent+" talkedDay:"+talkedDay+" "+"day:"+day+" topic:"+topic+" target:"+target+" species:"+species;
		else return "agent:"+agent+"talkedDay:"+talkedDay+""+"day:"+day+" topic:"+topic+" target:"+target;
	}

}
