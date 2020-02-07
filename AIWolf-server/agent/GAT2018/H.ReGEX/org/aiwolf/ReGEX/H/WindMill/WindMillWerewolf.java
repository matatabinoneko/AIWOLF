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
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;


public class WindMillWerewolf extends WindMillBasePlayer{
	int numWolves;
	Role fakeRole;
	int comingoutDay;
	int comingoutTurn;
	boolean isCameout;
	Map<Agent, Species> fakeJudgeMap = new HashMap<>();
	Deque<Judge> fakeJudgeQueue = new LinkedList<>();
	List<Agent> possessedList = new ArrayList<>();
	List<Agent> werewolves;
	List<Agent> humans;
	List<Agent> villagers;
	int talkTurn;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);
		werewolves = new ArrayList<>(gameInfo.getRoleMap().keySet());
		humans = new ArrayList<>();
		villagers = new ArrayList<>();
		for(Agent a : aliveOthers){
			if(!werewolves.contains(a)){
				humans.add(a);
			}
		}
		List<Role> roles = new ArrayList<>();
		for(Role r : Arrays.asList(Role.VILLAGER, Role.SEER /*, Role.MEDIUM*/)){
			if(gameInfo.getExistingRoles().contains(r)){
				roles.add(r);
			}
		}
		//------------------ComingOutDay-------------------
		fakeRole = randomSelect(roles);
		comingoutDay = 1;//(int)(Math.random() * 3 + 1);
		comingoutTurn = 0;//(int)(Math.random() * 5);
		isCameout = false;
		fakeJudgeMap.clear();
		fakeJudgeQueue.clear();
		possessedList.clear();
	}

	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		for(Judge j : divinationList){
			Agent agent = j.getAgent();
			if(!werewolves.contains(agent) && ((humans.contains(j.getTarget()) && j.getResult() == Species.WEREWOLF) || (werewolves.contains(j.getTarget()) && j.getResult() == Species.HUMAN))){
				if(!possessedList.contains(agent)){
					possessedList.add(agent);
					whisperQueue.offer(new Content(new EstimateContentBuilder(agent, Role.POSSESSED)));
				}
			}
		}
		villagers.clear();
		for(Agent agent : aliveOthers){
			if(!werewolves.contains(agent) && !possessedList.contains(agent)){
				villagers.add(agent);
			}
		}
	}

	private Judge getFakeJudge(){
		Agent target = null;
		if(fakeRole == Role.SEER){
			List<Agent> candidates = new ArrayList<>();
			for(Agent a : aliveOthers){
				if(!fakeJudgeMap.containsKey(a) && comingoutMap.get(a) != Role.SEER){
					candidates.add(a);
				}
			}
			if(candidates.isEmpty()){
				target = randomSelect(aliveOthers);
			}else{
				target = randomSelect(candidates);
			}
			//        }else if(fakeRole == Role.MEDIUM){
			//            target = currentGameInfo.getExecutedAgent();
		}
		if(target != null){
			Species result = Species.HUMAN;
			if(humans.contains(target)){
				int nFakeWolves = 0;
				for(Agent a : fakeJudgeMap.keySet()){
					if(fakeJudgeMap.get(a) == Species.WEREWOLF){
						nFakeWolves++;
					}
				}
				if(nFakeWolves < numWolves){
					if(possessedList.contains(target) || !isCo(target)){

						if(!isCo(Role.SEER)){
							result = Species.HUMAN;
						}else{
							if(Math.random() < 0.1){
								result = Species.WEREWOLF;
							}
						}
					}else{
						result = Species.WEREWOLF;
					}
				}
			}
			return new Judge(day, me, target, result);
		}
		return null;
	}

	public void dayStart(){
		super.dayStart();
		talkTurn = -1;
		if(day == 0){
			whisperQueue.offer(new Content(new ComingoutContentBuilder(me, fakeRole)));
		}else{
			Judge judge = getFakeJudge();
			if(judge != null){
				fakeJudgeQueue.offer(judge);
				fakeJudgeMap.put(judge.getTarget(), judge.getResult());
			}
		}
	}
	protected void chooseVoteCandidate(){
		List<Agent> candidates = new ArrayList<>();
		if(fakeRole != Role.VILLAGER){
			for(Agent a : villagers){
				if(comingoutMap.get(a) == fakeRole || fakeJudgeMap.get(a) == Species.WEREWOLF){
					candidates.add(a);
				}
			}
			if(candidates.isEmpty()){
				for(Agent a : villagers){
					if(fakeJudgeMap.get(a) != Species.HUMAN){
						candidates.add(a);
					}
				}
			}
		}
		if(candidates.isEmpty()){
			candidates.addAll(villagers);
		}
		if(candidates.isEmpty()){
			candidates.addAll(possessedList);
		}
		if(!candidates.isEmpty()){
			if(!candidates.contains(voteCandidate)){
				voteCandidate = randomSelect(candidates);
				if(canTalk){
					talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
				}
			}
		}else{
			voteCandidate = null;
		}
	}

	public String talk(){
		//五人人狼でCOさせないために、とりあえずnumWolvesが三人以上じゃないとCO呼び出さないとかでどうだろう。いけそう。
		if(numWolves >= 3){
			talkTurn++;
			if(fakeRole != Role.VILLAGER){
				if(!isCameout){
					int fakeSeerCo = 0;
					//int fakeMediumCo = 0;
					for(Agent a : werewolves){
						if(comingoutMap.get(a) == Role.SEER){
							fakeSeerCo++;
							//                    }else if(comingoutMap.get(a) == Role.MEDIUM){
							//                        fakeMediumCo++;
						}
					}
					if(fakeRole == Role.SEER && fakeSeerCo > 0 /* || fakeRole == Role.MEDIUM && fakeMediumCo > 0*/){

						fakeRole = Role.VILLAGER;
						whisperQueue.offer(new Content(new ComingoutContentBuilder(me, fakeRole)));
					}else{
						for(Agent a : humans){
							if(comingoutMap.get(a) == fakeRole){
								comingoutDay = day;
							}
						}
						if(day >= comingoutDay && talkTurn >= comingoutTurn){
							isCameout = true;
							talkQueue.offer(new Content(new ComingoutContentBuilder(me, fakeRole)));
						}
					}
				}else{
					while(!fakeJudgeQueue.isEmpty()){
						Judge judge = fakeJudgeQueue.poll();
						if(fakeRole == Role.SEER){
							talkQueue.offer(new Content(new DivinedResultContentBuilder(judge.getTarget(), judge.getResult())));
							//                    }else if(fakeRole == Role.MEDIUM){
							//                        talkQueue.offer(new Content(new IdentContentBuilder(judge.getTarget(), judge.getResult())));
						}
					}
				}
			}
		}
		return super.talk();
	}

	protected void chooseAttackVoteCandidate(){
		if(day > 0){
			List<Agent> candidates = new ArrayList<>();
			for(Agent a : villagers){
				if(isCo(a)){
					candidates.add(a);
				}
			}
			if(candidates.isEmpty()){
				candidates.addAll(villagers);
			}
			if(candidates.isEmpty()){
				candidates.addAll(possessedList);
			}

			if(!candidates.isEmpty()){
				if(!aliveOthers.contains(attackVoteCandidate)){
					attackVoteCandidate = randomSelect(candidates);
				}
			}else{
				attackVoteCandidate = null;
			}
		}
	}
}

