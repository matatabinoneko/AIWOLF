package org.aiwolf.ReGEX.H.WindMill;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class WindMillSeer extends WindMillVillager1{

  int comingoutDay;
  boolean isCameout;
  Deque<Judge> divinationQueue = new LinkedList<>();
  Map<Agent, Species> myDivinationMap = new HashMap<>();
  List<Agent> whiteList = new ArrayList<>();
  List<Agent> blackList = new ArrayList<>();
  List<Agent> grayList;
  List<Agent> semiWolves = new ArrayList<>();
  List<Agent> possessedList = new ArrayList<>();
  Agent me;

  public void initialize(GameInfo gameInfo, GameSetting gameSetting){
    super.initialize(gameInfo,gameSetting);
    comingoutDay = 1; //(int)(Math.random() * 3 + 1);
    isCameout = false;
    divinationQueue.clear();
    myDivinationMap.clear();
    whiteList.clear();
    blackList.clear();
    grayList = new ArrayList<>();
    semiWolves.clear();
    possessedList.clear();
    me = gameInfo.getAgent();
  }

  public void dayStart(){
    super.dayStart();
    Judge divination = currentGameInfo.getDivineResult();
    if(divination != null){
      divinationQueue.offer(divination);
      grayList.remove(divination.getTarget());
      if(divination.getResult() == Species.HUMAN){
        whiteList.add(divination.getTarget());
      }else{
        blackList.add(divination.getTarget());
      }
      myDivinationMap.put(divination.getTarget(), divination.getResult());
    }
  }

  public void chooseVoteCandidate(){
    List<Agent> aliveWolves = new ArrayList<>();
    for(Agent a : blackList){
      if(isAlive(a)){
        aliveWolves.add(a);
      }
    }
    if(!aliveWolves.isEmpty()){
      if(!aliveWolves.contains(voteCandidate)){
        voteCandidate = randomSelect(aliveWolves);
        if(canTalk){
          talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new VoteContentBuilder(voteCandidate)))));
        }
      }
      return;
    }
    werewolves.clear();
    for(Agent a : aliveOthers){
      if(comingoutMap.get(a) == Role.SEER){
        werewolves.add(a);
      }
    }
    for(Judge j : identList){
      Agent agent = j.getAgent();
      if((myDivinationMap.containsKey(j.getTarget()) && j.getResult() != myDivinationMap.get(j.getTarget()))){
        if(isAlive(agent) && !werewolves.contains(agent)){
          werewolves.add(agent);
        }
      }
    }
    possessedList.clear();
    semiWolves.clear();
    for(Agent a : werewolves){
      if(whiteList.contains(a)){
        if(!possessedList.contains(a)){
          talkQueue.offer(new Content(new EstimateContentBuilder(a, Role.POSSESSED)));
          possessedList.add(a);
        }
      }else{
        semiWolves.add(a);
      }
    }


  if(!semiWolves.isEmpty()){
      if(!semiWolves.contains(voteCandidate)){
        voteCandidate = randomSelect(semiWolves);
      }
    }else{
    	if(!maybeVoted.contains(me) && day > 3){
			//ここから下を生存人数ごとに分ける。
			//15~13人の場合:　グレロラ
			if(aliveOthers.size() >= 13){
				if(grayNoRoles.isEmpty()){
					if(!aliveOthers.contains(voteCandidate)){
						voteCandidate = randomSelect(aliveOthers);
					}
				}else{
					if(!grayNoRoles.contains(voteCandidate)){
						voteCandidate = randomSelect(grayNoRoles);
						if(canTalk){
							//talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
							//talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
						}
					}
				}
				//10 ~ 6: 占ロラ　これはいいのかな、占い師的には。自分抜いた占い師に投票してくれたまえって感じ？
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
							//talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
						}
					}
				}
			}else{
				//6人未満
				//せっかくなのでwerewolvesをつかいます。いなきゃどうせランダムなので。最終的にwerewolvesに霊媒師もいれたいですね。
				if(werewolves.isEmpty()){
					if(!aliveOthers.contains(voteCandidate)){
						voteCandidate = randomSelect(aliveOthers);
					}
				}else{
					if(!werewolves.contains(voteCandidate)){
						voteCandidate = randomSelect(werewolves);
						if(canTalk){
							talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
							//talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
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
						//talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
					}
				}
			}
		}
      }
  }

  public String talk(){
	  //cominfoutDay = 1により、初日COします。
    if(!isCameout && (day >= comingoutDay || (!divinationQueue.isEmpty() && divinationQueue.peekLast().getResult() == Species.WEREWOLF) || isCo(Role.SEER))){
      talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
      isCameout = true;
    }
    if(isCameout){
      while(!divinationQueue.isEmpty()){
        Judge ident = divinationQueue.poll();
        talkQueue.offer(new Content(new DivinedResultContentBuilder(ident.getTarget(), ident.getResult())));
      }
    }
    return super.talk();
  }


  public Agent divine(){

	  //発言数ない(必然的に役職COしていない人になると思う)→自分を疑っている回数が最多のエージェント)→グレー→他の人→占わない(生存者ゼロ)
		for(Agent subject : DoubtMeNow){
			if(DoubtMeCounter.containsKey(subject)){
				int value = DoubtMeCounter.get(subject) + 1;
				DoubtMeCounter.put(subject, value);
			}else{
				DoubtMeCounter.put(subject, 1);
			}
		}
		//最も投票されそうな人の得票数を取得する。
		int maxDoubtMe = 0;
		for(int i : DoubtMeCounter.values()){
			if(i > maxDoubtMe){
				maxDoubtMe = i;
			}
		}
		//得票数の最も多いエージェンをmaybeVotedリストに格納する。
		for(Map.Entry<Agent, Integer> i : DoubtMeCounter.entrySet()){
			if(i.getValue() >= maxDoubtMe){
				DoubtMeAgent.add(i.getKey());
			}
		}

		grayNoRoles.clear();
		for (Agent agent : aliveOthers){
			if(comingoutMap.get(agent) != Role.SEER && comingoutMap.get(agent) != Role.MEDIUM){
				Agent candidate = agent;
				if(isAlive(candidate) && !grayNoRoles.contains(candidate)){
					grayNoRoles.add(agent);
				}
			}
		}

	  if(!SilentAgent.isEmpty()){
		  return randomSelect(SilentAgent);
	  }else if(!DoubtMeAgent.isEmpty()){
		  return randomSelect(DoubtMeAgent);
	  }else if(!grayNoRoles.isEmpty()){
		  return randomSelect(grayNoRoles);
	  }else{
		    List<Agent> candidates = new ArrayList<>();
		    for(Agent a : aliveOthers){
		      if(!myDivinationMap.containsKey(a)){
		        candidates.add(a);
		      }
		    }
		    if(candidates.isEmpty()){
		        return null;
		      }

		    return randomSelect(candidates);
	  }

  }

}
