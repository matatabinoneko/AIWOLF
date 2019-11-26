package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.*;
/**
 * 誰が誰を占って結果はどうだったかの表
 * @author carlo
 *
 */

public class SeerDivineTable {
	/** <Seer,<Target,Result>> */
	protected HashMap<Agent,HashMap<Agent,Species>> divineMap=new HashMap<Agent,HashMap<Agent,Species>>();
	public SeerDivineTable(){
	}
	
	public void addDivineResult(Agent seer,Agent target,Species result){
		//System.out.println(seer+" "+target+" "+result);
		//初めて追加する場合は
		if(divineMap.containsKey(seer)==false){
			HashMap<Agent,Species> map=new HashMap<Agent,Species>();
			map.put(target, result);
			divineMap.put(seer,map);
		}
		else{
			divineMap.get(seer).put(target, result);
		}
	}
	
	public DivinedResult getDivinedResult(Agent target){
		ArrayList<Agent> blackSeer=new ArrayList<Agent>();
		ArrayList<Agent> whiteSeer=new ArrayList<Agent>();
		for(Entry<Agent,HashMap<Agent,Species>> entry:divineMap.entrySet()){
			if(entry.getValue().containsKey(target)){
				Species result=entry.getValue().get(target);
				if(result==Species.WEREWOLF) blackSeer.add(entry.getKey());
				else if(result==Species.HUMAN) whiteSeer.add(entry.getKey());
			}
		}
		return new DivinedResult(target,blackSeer,whiteSeer);
	}

}
