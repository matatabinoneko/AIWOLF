package org.aiwolf.ReGEX.H.WindMill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class WindMillPossessed extends WindMillVillager1{
  int numWolves;
  boolean isCameout;
  int i = 0;
  int j;
// 占いCO用
  List<Judge> fakeDivinationList = new ArrayList<>();
  Deque<Judge> fakeDivinationQueue = new LinkedList<>();
  List<Agent> divinedAgents = new ArrayList<>();
// 霊媒CO用
  Map<Agent, Species> fakeIdentMap = new HashMap<>();
  List<Judge> fakeIdentList = new ArrayList<>();
  Deque<Judge> fakeIdentQueue = new LinkedList<>();
  List<Agent> identifiedAgents = new ArrayList<>();
  Role fakeRole;
  int comingoutDay;
  int comingoutTurn;
  Map<Agent, Species> fakeJudgeMap = new HashMap<>();
  Deque<Judge> fakeJudgeQueue = new LinkedList<>();
  List<Agent> werewolves = new ArrayList<>();
  List<Agent> humans;
  List<Agent> villagers;
 int talkTurn;

  public void initialize(GameInfo gameInfo, GameSetting gameSetting){
    super.initialize(gameInfo, gameSetting);
    numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);
    List<Role> roles = new ArrayList<>();
    for(Role r : Arrays.asList(/*Role.VILLAGER,*/ Role.SEER, Role.MEDIUM)){
        if(gameInfo.getExistingRoles().contains(r)){
            roles.add(r);
        }
    }
    fakeRole = randomSelect(roles);
    comingoutDay = 1;
    comingoutTurn = 0;
    fakeJudgeMap.clear();
    fakeJudgeQueue.clear();
    isCameout = false;
    fakeDivinationList.clear();
    fakeDivinationQueue.clear();
    divinedAgents.clear();
    fakeIdentList.clear();
    fakeIdentQueue.clear();
    identifiedAgents.clear();
  }

  private Judge getFakeDivination(){
    Agent target = null;
    List<Agent> candidates = new ArrayList<>();
    for(Agent a : aliveOthers){
      if(!divinedAgents.contains(a) && comingoutMap.get(a) != Role.SEER){
        candidates.add(a);
      }
    }
    if(!candidates.isEmpty()){
      target = randomSelect(candidates);
    }else{
      target = randomSelect(aliveOthers);
    }

    Species result = Species.HUMAN;
    int nFakeWolves = 0;
    for(Judge j : fakeDivinationList){
      if(j.getResult() == Species.WEREWOLF){
        nFakeWolves++;
      }
    }
    if(nFakeWolves < numWolves && Math.random() < 0.3){
      result = Species.WEREWOLF;
    }
    return new Judge(day, me, target, result);
  }

  public void dayStart(){
    super.dayStart();
    if(day > 0){
      Judge judge = getFakeDivination();
      if(judge != null){
        fakeDivinationList.add(judge);
        fakeDivinationQueue.offer(judge);
        divinedAgents.add(judge.getTarget());
        j = 0;
      }
    }

  }

  protected void chooseVoteCandidate(){
	  if(fakeRole == Role.SEER){
    werewolves.clear();
    List<Agent> candidates = new ArrayList<>();
    for(Judge j : divinationList){
      if(j.getResult() == Species.WEREWOLF && (j.getTarget() == me || isKilled(j.getTarget()))){
        if(!werewolves.contains(j.getAgent())){
          werewolves.add(j.getAgent());
        }
      }
    }
    for(Agent a : aliveOthers){
      if(!werewolves.contains(a) && comingoutMap.get(a) == Role.SEER){
        candidates.add(a);
      }
    }
    List<Agent> fakeHumans = new ArrayList<>();
    for(Judge j : fakeDivinationList){
      if(j.getResult() == Species.HUMAN){
        if(!fakeHumans.contains(j.getTarget())){
          fakeHumans.add(j.getTarget());
        }
      }else{
        if(!candidates.isEmpty()){
          candidates.add(j.getTarget());
        }
      }
    }
    for(Agent a : aliveOthers){
       if(candidates.isEmpty()){
    	  if(!werewolves.contains(a) && !fakeHumans.contains(a)){
    	  candidates.add(a);
    	}
      }
    }
    if(candidates.isEmpty()){
      for(Agent a : aliveOthers){
        if(!werewolves.contains(a)){
          candidates.add(a);
        }
      }
    }
    if(!candidates.contains(voteCandidate)){
      voteCandidate = randomSelect(candidates);
      if(canTalk){
        talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
        talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
      }
    }
	  }else{
		    werewolves.clear();
		    for(Agent agent : aliveOthers){
		      if (comingoutMap.get(agent) == Role.MEDIUM){
		        werewolves.add(agent);
		      }
		    }
		    for(Judge j : divinationList){
		      Agent agent = j.getAgent();
		      Agent target = j.getTarget();
		      if(j.getResult() == Species.WEREWOLF && (target == me || isKilled(target)) || (fakeIdentMap.containsKey(target) && j.getResult() != fakeIdentMap.get(target))){
		        if(isAlive(agent) && !werewolves.contains(agent)){
		          werewolves.add(agent);
		        }
		      }
		    }

		    if(werewolves.isEmpty()){
		    	if(!aliveOthers.contains(voteCandidate)){
		    		voteCandidate = randomSelect(aliveOthers);
		    	}
		    }else{
		    	if(!werewolves.contains(voteCandidate)){
		    		voteCandidate = randomSelect(werewolves);
		    		if(canTalk){
//		    			talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
		    			talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
		    		}
		    	}
		    }
//		  voteCandidate = randomSelect(aliveOthers);
	  }
  }

  public String talk(){
	  if(!isCameout && fakeRole == Role.SEER){
		  isCameout = true;
		  talkQueue.offer(new Content(new ComingoutContentBuilder(me, fakeRole)));
	  }else if(!isCameout && fakeRole == Role.MEDIUM && day >= 2){
		  isCameout = true;
		  talkQueue.offer(new Content(new ComingoutContentBuilder(me, fakeRole)));
	  }
	  if(isCameout) {
		  if(fakeRole == Role.SEER){
			  while(!fakeDivinationQueue.isEmpty()) {
				  Judge divination = fakeDivinationQueue.poll();
				  talkQueue.offer(new Content(new DivinedResultContentBuilder(divination.getTarget(), divination.getResult())));
			  }
		  }else if(fakeRole == Role.MEDIUM){

			  if(!identList.isEmpty()){
				  if(day == 2 && i == 0){
					  for(Judge j : identList){
						  if(j.getResult() == Species.WEREWOLF){
							  Judge judge = new Judge(2, me, j.getTarget(), Species.HUMAN);
							  fakeIdentList.add(judge);
							  talkQueue.offer(new Content(new IdentContentBuilder(j.getTarget(), Species.HUMAN)));
							  fakeIdentMap.put(j.getTarget(), Species.HUMAN);
						  }else{
							  Judge judge = new Judge(2, me, j.getTarget(), Species.WEREWOLF);
							  fakeIdentList.add(judge);
							  talkQueue.offer(new Content(new IdentContentBuilder(j.getTarget(), Species.WEREWOLF)));
							  fakeIdentMap.put(j.getTarget(), Species.WEREWOLF);
						  }
					  }
					  i++;
				  }else if(j == 0){
						//二日目以降の霊媒師の結果言わせるの未実装。
				}
			}

			while(!fakeIdentQueue.isEmpty()) {
				  Judge identification = fakeIdentQueue.poll();
				  talkQueue.offer(new Content(new IdentContentBuilder(identification.getTarget(), identification.getResult())));
			  }
		  }
	  }
      return super.talk();
  }
}