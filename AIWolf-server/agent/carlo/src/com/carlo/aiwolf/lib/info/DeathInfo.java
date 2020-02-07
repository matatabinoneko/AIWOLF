package com.carlo.aiwolf.lib.info;

import java.util.HashMap;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;

public class DeathInfo {
	protected GameInfoManager agentInfo;
	public DeathInfo(GameInfoManager agentInfo){
		this.agentInfo=agentInfo;
	}
	/** エージェントの死亡日,死因マップ */
	protected HashMap<Integer,HashMap<CauseOfDeath,Agent>> deadAgentMap=new HashMap<Integer,HashMap<CauseOfDeath,Agent>>();

	public void addDeathData(int yesterday,Agent executedAgent,Agent attackedAgent){
		deadAgentMap.put(yesterday,new HashMap<CauseOfDeath,Agent>());	
		deadAgentMap.get(yesterday).put(CauseOfDeath.EXECUTED,executedAgent);
		deadAgentMap.get(yesterday).put(CauseOfDeath.ATTACKED,attackedAgent);
		//deadAgentMap.get(yesterday).put(CauseOfDeath.EXECUTED,gameInfo.getExecutedAgent());
		//deadAgentMap.get(yesterday).put(CauseOfDeath.ATTACKED,gameInfo.getAttackedAgent());
	}
	
	/** 死亡日、死因から死んだエージェントを取得する。なければnull */
	public Agent getDeadAgent(int deadDay,CauseOfDeath cause){
		return deadAgentMap.get(deadDay).get(cause);
	}
	/** agentが死んだ日を返す */
	public int getDayAgentDied(Agent agent){
		if(agentInfo.isAlive(agent)) return -1;
		for(Entry<Integer, HashMap<CauseOfDeath, Agent>> entry : deadAgentMap.entrySet()) {
			for(Entry<CauseOfDeath, Agent> subEntry : entry.getValue().entrySet()){
				if(subEntry.getValue()==agent) return entry.getKey();
			}
		}
		return -1;
	}
	
	public void printDeadAgentMap(){
		for(int i=0;i<agentInfo.getDay();i++){
			HashMap<CauseOfDeath,Agent> map=deadAgentMap.get(i);
			System.out.println(i+"day attacked agent:"+map.get(CauseOfDeath.ATTACKED));
			System.out.println(i+"day executed agent:"+map.get(CauseOfDeath.EXECUTED));
		}
	}
}
