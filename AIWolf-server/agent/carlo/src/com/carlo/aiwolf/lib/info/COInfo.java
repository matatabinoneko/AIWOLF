package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * CO情報を管理するクラス<br>
 * COせずにした能力結果発言はCO扱いとする。
 * はじめてCOした、もしくは能力結果発言をした役職のみを登録する。
 * ギドラCOは認めない。
 * @author carlo
 *
 */

public class COInfo {
	protected HashMap<Agent,COData> coDataMap=new HashMap<Agent,COData>();
	/** その役職をCOした数 */
	protected HashMap<Role,Integer> numOfCOAgentMap=new HashMap<Role,Integer>();
	protected Agent myAgent;
	protected GameInfoManager gameInfoMgr;
	public COInfo(GameInfoManager gameInfoMgr){
		this.myAgent=gameInfoMgr.getMyAgent();
		this.gameInfoMgr=gameInfoMgr;
		for(Agent agent:gameInfoMgr.getAgentList()){
			//coRoleMap.put(agent,null);
			coDataMap.put(agent,null);
		}
		//初期化
		for(Role role:Role.values()){
			if(role==Role.FREEMASON) continue;
			numOfCOAgentMap.put(role, 0);
		}
		numOfCOAgentMap.put(null, gameInfoMgr.getAgentList().size());
	}
	/**
	 * agentのCODataを返す
	 * @param agent
	 * @return
	 */
	public COData getCOData(Agent agent){
		return coDataMap.get(agent);
	}
	public Map<Agent,COData> getCODataMap(){
		return coDataMap;
	}
	/**
	 * @param agnet
	 * @return agentが最後にCOした役職。なければnull
	 */
	public Role getCoRole(Agent agent){
		if(coDataMap.get(agent)==null) return null;
		else return coDataMap.get(agent).getCORole();
	}
	/**
	 * 
	 * @param coRole COした役職。nullならCOがない人を対象に。 
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @param isExceptMe 自分をリストから省くか
	 * @return  条件に合うエージェントのリストを返す
	 * 
	 */
	public List<Agent> getCOAgentList(Role coRole,boolean isAliveOnly,boolean isExceptMe){
		ArrayList<Agent> agents=new ArrayList<Agent>();
		for(Entry<Agent, COData> entry : coDataMap.entrySet()) {
			Agent agent=entry.getKey();
			COData coData=entry.getValue();
			if(isAliveOnly){
				if(gameInfoMgr.isAlive(entry.getKey())){
					if(coRole==null && coData==null) agents.add(agent);
					else if(coData!=null && coRole==coData.getCORole()) agents.add(agent);
				}
			}
			else{
				if(coRole==null && coData==null) agents.add(agent);
				else if(coData!=null && coRole==coData.getCORole()) agents.add(agent);
			}
		}
		
		if(isExceptMe) agents.remove(myAgent);
		return agents;
	}
	/** 役職roleをCOしたエージェントの数を返す（自身を含む)。role=nullは非CO者を対象。 */
	public int getNumOfCOAgent(Role role){
		return numOfCOAgentMap.get(role);
	}
	/**  役職roleをCOしたエージェントの数を返す（自身を含む)。非COは対象外。逐次探索を行うのであまり推奨しないが、生存オンリーで数える場合はこれしかない。 */
	public int countCoAgent(Role role,boolean isAliveOnly){
		int count=0;
		for(Entry<Agent, COData> entry : coDataMap.entrySet()) {
			if(entry.getValue()!=null && entry.getValue().getCORole()==role){
				if(isAliveOnly && gameInfoMgr.isAlive(entry.getKey()))  count++;
				else if(isAliveOnly==false) count++;
			}
		}
		return count;
	}
	
	/** デバッグ用のprintメソッド */
	
	public void printCoRoleMap(){
		for(Entry<Agent, COData> entry : coDataMap.entrySet()) {
			System.out.println(entry.getKey()+" "+entry.getValue());
		}
		for(Role role:Role.values()){
			if(role==Role.FREEMASON) continue;
			System.out.println(role+" "+numOfCOAgentMap.get(role));
		}
		System.out.println(null+" "+numOfCOAgentMap.get(null));
	}
	
	/** coRoleが村人ならputしない。１回目のみ追加する(別のCOは認めない)。
	 * TODO:別で数えて重複COした人は覚えておけるように */
	public void putCORole(Agent agent,Role coRole,int day,int talkIdx,boolean isComingOut){
		if(coRole!=Role.VILLAGER && coDataMap.containsKey(agent) && coDataMap.get(agent)==null){
			coDataMap.put(agent, new COData(agent,coRole,day,talkIdx,isComingOut));
			addCONum(coRole);
		}
		
	}
	/** CO数を数える */
	private void addCONum(Role role){
		if(role!=null){
			int beforeNum=numOfCOAgentMap.get(role);
			numOfCOAgentMap.put(role, beforeNum+1);
			
			int nonCONum=numOfCOAgentMap.get(null);
			numOfCOAgentMap.put(null, nonCONum-1);
		}
	}

}
