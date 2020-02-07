package org.aiwolf.ReGEX.H.WindMill;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class WindMillBodyguard extends WindMillVillager1{
  Agent guardedAgent;
  boolean isCameout;
  boolean GJ;
  boolean DoubtMe;
  List<Agent> GJList = new ArrayList<>();
  Deque<Agent> GJQueue = new LinkedList<>();

  public void initialize(GameInfo gameInfo, GameSetting gameSetting){
	super.initialize(gameInfo, gameSetting);
	isCameout = false;
	GJ = false;
    guardedAgent = null;
    GJList.clear();
    GJQueue.clear();
  }

  public Agent guard(){
    Agent guardCandidate = null;
    //さっきGJを出したら同じ人を守ることにします。ただし役持ちでない（村人想定）をのぞく。
    if(guardedAgent != null && currentGameInfo.getLastDeadAgentList().isEmpty()){
//    	GJ = true;
    	GJList.add(guardedAgent);
    	GJQueue.offer(guardedAgent);
    }
    if(guardedAgent != null && currentGameInfo.getLastDeadAgentList().isEmpty() && isAlive(guardedAgent) && (comingoutMap.get(guardedAgent) == Role.SEER || comingoutMap.get(guardedAgent) == Role.MEDIUM)){
    	guardCandidate = guardedAgent;
    	GJ = true;
    }else{
      List<Agent> candidates = new ArrayList<>();
      for (Agent agent : aliveOthers){
        if(comingoutMap.get(agent) == Role.SEER && !werewolves.contains(agent)){
        	if(guardedAgent != agent){
        		candidates.add(agent);
        	}
        }
      }
      if(candidates.isEmpty()){
        for(Agent agent : aliveOthers){
          if(comingoutMap.get(agent) == Role.MEDIUM && !werewolves.contains(agent)){
        	  if(guardedAgent != agent){
        		  candidates.add(agent);
        	  }
          }
        }
      }
      if(candidates.isEmpty()){
        for(Agent agent : aliveOthers){
          if(!werewolves.contains(agent)){
        	  if(guardedAgent != agent){
        		  candidates.add(agent);
        	  }
          }
        }
      }
      if(candidates.isEmpty()){
        candidates.addAll(aliveOthers);
      }

      guardCandidate = randomSelect(candidates);
    }
    guardedAgent = guardCandidate;
    return guardCandidate;
  }
//  public String talk(){
//	    if(!isCameout && ((GJ || DoubtMeQ >= 3 ) && day > 3)){
//	      talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.BODYGUARD)));
//	      isCameout = true;
//	    }
//	    if(isCameout){
//	        while(!GJQueue.isEmpty()){
//	        	Agent GJHuman = GJQueue.poll();
//	        	talkQueue.offer(new Content(new GuardedAgentContentBuilder(GJHuman)));
//	        }
//	      }
//	    return super.talk();
//	}
}
