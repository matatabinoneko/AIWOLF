package org.aiwolf.ReGEX.H.WindMill;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;


public class WindMillMedium extends WindMillVillager1{
//  int comingoutDay; //使いません。
  boolean isCameout;
  Deque<Judge> identQueue = new LinkedList<>();
  Map<Agent, Species> myIdentMap = new HashMap<>();
  Agent me;

  public void initialize(GameInfo gameInfo, GameSetting gameSetting){
    super.initialize(gameInfo, gameSetting);
    isCameout = false;
    identQueue.clear();
    myIdentMap.clear();
    me = gameInfo.getAgent();
  }
  public void dayStart(){
    super.dayStart();
    Judge ident = currentGameInfo.getMediumResult();
    if(ident != null){
      identQueue.offer(ident);
      myIdentMap.put(ident.getTarget(), ident.getResult());
    }
  }
  protected void chooseVoteCandidate(){

	super.chooseVoteCandidate();

    werewolves.clear();
    for(Agent agent : aliveOthers){
      if (comingoutMap.get(agent) == Role.MEDIUM){
        werewolves.add(agent);
      }
    }
    for(Judge j : divinationList){
      Agent agent = j.getAgent();
      Agent target = j.getTarget();
      if(j.getResult() == Species.WEREWOLF && (target == me || isKilled(target)) || (myIdentMap.containsKey(target) && j.getResult() != myIdentMap.get(target))){
        if(isAlive(agent) && !werewolves.contains(agent)){
          werewolves.add(agent);
        }
      }
    }

    if(!maybeVoted.contains(me) && day >= 3){
    	  if(aliveOthers.size() >= 13){
    	    if(grayNoRoles.isEmpty()){
    	    	if(!aliveOthers.contains(voteCandidate)){
    	    		voteCandidate = randomSelect(aliveOthers);
    	    	}
    	    }else{
    	    	if(!grayNoRoles.contains(voteCandidate)){
    	    		voteCandidate = randomSelect(grayNoRoles);
    	    		if(canTalk){
    	    			talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
    	    			talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
    	    		}
    	    	}
    	    }

    	  }else if(aliveOthers.size() >= 6){
    		  if(werewolvesSeer.isEmpty()){
    			  if(!aliveOthers.contains(voteCandidate)){
    				  voteCandidate = randomSelect(aliveOthers);
    			  }
    		  }else{
    			  if(!werewolvesSeer.contains(voteCandidate)){
    				  voteCandidate = randomSelect(werewolvesSeer);
    				  if(canTalk){
    					  talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
    					  talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
    				  }
    			  }
    		  }
    	  }else{

    		  if(werewolves.isEmpty()){
    			  if(!aliveOthers.contains(voteCandidate)){
    				  voteCandidate = randomSelect(aliveOthers);
    			  }
    		  }else{
    			  if(!werewolves.contains(voteCandidate)){
    				  voteCandidate = randomSelect(werewolves);
    				  if(canTalk){
    					  talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
    					  talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
    				  }
    			  }
    		  }
    	  }

    	}else{
    		if(maybeVoted.size() <= 1){
    			if(!aliveOthers.contains(voteCandidate)){
    	    		voteCandidate = randomSelect(aliveOthers);
    	    	}
    		}else{
    	    	if(!maybeVoted.contains(voteCandidate)){
    	    		voteCandidate = randomSelect(maybeVoted);
    	    		if(canTalk){
    	    			talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
    	    			talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
    	    		}
    	    	}
    		}
    	}


  }

  public String talk(){
	  //COしてなくて、昨日の霊媒結果が狼である。または「投票する」ってたくさん言われてしまったら。もしくは対抗霊媒師がいた場合。
    if(!isCameout && ((!identQueue.isEmpty() && identQueue.peekLast().getResult() == Species.WEREWOLF) || maybeVoted.contains(me) || isCo(Role.MEDIUM))){
      talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.MEDIUM)));
      isCameout = true;
    }
    if(isCameout){
      while(!identQueue.isEmpty()){
      Judge ident = identQueue.poll();
      talkQueue.offer(new Content(new IdentContentBuilder(ident.getTarget(), ident.getResult())));
      }
    }
    return super.talk();
  }
}
