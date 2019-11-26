package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;

/**
 * 票数を数えるクラス
 * @author carlo
 *
 */

public class VoteCounter {
	Map<Agent,Integer> countMap;
	public VoteCounter(){
		countMap=new HashMap<Agent,Integer>();
	}
	/** 投票対象を追加 */
	public void addTarget(Agent target){
		if(countMap.containsKey(target)==false) countMap.put(target, 1);
		else{
			int addNum=countMap.get(target)+1;
			countMap.put(target,addNum);
		}
	}
	/** 得票数がトップのエージェントを返す。(トップが複数のことも考えてリストで) */
	public List<Agent> getTargetsMostVoted(){
		//得票最大のAgentを探し出す
		int max=0;
		List<Agent> agents=new ArrayList<Agent>();
		Iterator<Entry<Agent, Integer>> entries = countMap.entrySet().iterator();
		while(entries.hasNext()) {
			Map.Entry<Agent,Integer> entry = entries.next();

			if(entry.getValue()>max){
				agents=new ArrayList<Agent>();
				max=entry.getValue();
				agents.add(entry.getKey());
			}
			else if(entry.getValue()==max){
				agents.add(entry.getKey());
			}
		}
		return agents;
	}

}
