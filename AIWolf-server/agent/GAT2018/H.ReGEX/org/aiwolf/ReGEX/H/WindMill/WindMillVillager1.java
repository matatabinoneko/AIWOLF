package org.aiwolf.ReGEX.H.WindMill;
//最初に作成した、グレラン→占ロラ→怪しみorランダム投票 の基本村人エージェト

import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;


//werewolves:矛盾した占い師を格納したリスト
//grayNoRoles:グレーの人たちを格納したリスト
//werewolvesSeer:占いカミングアウトした人を全てを格納したリスト
//werewolvesRole:役職COした人を全て格納したリスト
//werewolvesSuspected:「狼だと思う」と言われた人の中で数が最も多かった人を格納したリスト 自分込み
//maybeVoted:「投票します」と言われた人の中で数が最も多かった人を格納したリスト 自分込み
//PleaseDivine: 占ってカウンター(未実装)(継承用)
//SilentAgent: 寡黙カウンター(未実装)(村人にも実装するかも)
//DoubtMeAgent: 自分を疑っている人カウンター(どうだろう)


public class WindMillVillager1 extends WindMillBasePlayer{

	int DoubtMeQ = 0;
	Agent PleaseDivine;
	Agent PleaseDivined;
	Agent me;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting){
		  super.initialize(gameInfo,gameSetting);
		  me = gameInfo.getAgent();
		}

	protected void chooseVoteCandidate(){

		//占って欲しい発言カウンター。　//未実装

		//投票されそうな人----------------------------------------------------------
		//投票されそうな人をカウントする。
		for(Agent target : SuspectNow.values()){
			if(SuspectedCounter.containsKey(target)){
				int value = SuspectedCounter.get(target) + 1;
				SuspectedCounter.put(target, value);
			}else{
				SuspectedCounter.put(target, 1);
			}
		}
		//最も投票されそうな人の得票数を取得する。
		int maxSuspect = 0;
		for(int i : SuspectedCounter.values()){
			if(i > maxSuspect){
				maxSuspect = i;
			}
		}
		//得票数の最も多いエージェンをmaybeVotedリストに格納する。
		for(Map.Entry<Agent, Integer> i : SuspectedCounter.entrySet()){
			if(i.getValue() >= maxSuspect){
				werewolvesSuspected.add(i.getKey());
			}
		}
		//----------------------------------------------------------------------

		//さっきと同じ要領で、あの人に投票します。と言われた人の数を取得します。

		//投票されそうな人----------------------------------------------------------
		//投票されそうな人をカウントする。
		for(Agent target : wantVote.values()){
			if(VotedCounter.containsKey(target)){
				int value = VotedCounter.get(target) + 1;
				VotedCounter.put(target, value);
			}else{
				VotedCounter.put(target, 1);
			}
		}
		//最も投票されそうな人の得票数を取得する。
		int maxVote = 0;
		for(int i : VotedCounter.values()){
			if(i > maxVote){
				maxVote = i;
			}
		}
		//得票数の最も多いエージェンをmaybeVotedリストに格納する。
		for(Map.Entry<Agent, Integer> i : VotedCounter.entrySet()){
			if(i.getValue() >= maxVote){
				maybeVoted.add(i.getKey());
			}
		}
		//----------------------------------------------------------------------


		//自分を疑ってきたエージェント-------------------------------------------------
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
		//-----------------------------------------------------------------------

		//寡黙エージェント----------------------------------------------------------
		//とりあえず寡黙吊りはせず、占い師に占ってもらおうか。機能してる気がしないがまあ、残しておこう。
		if(!SilentAgent.isEmpty()){
			if(PleaseDivined != PleaseDivine){
				talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(PleaseDivine)))));
				PleaseDivined = PleaseDivine;
			}
		}

		//矛盾した占い師を取得
		werewolves.clear();
		for(Judge j : divinationList){
			if(j.getResult() == Species.WEREWOLF && (j.getTarget()) == me || isKilled(j.getTarget())){
				Agent candidate = j.getAgent();
				if(isAlive(candidate) && !werewolves.contains(candidate)){
					werewolves.add(candidate);
				}
			}
		}

		//占い師のみ取得
		werewolvesSeer.clear();
		for (Agent agent : aliveOthers){
			if(comingoutMap.get(agent) == Role.SEER){
				Agent candidate = agent;
				if(isAlive(candidate) && !werewolvesSeer.contains(candidate)){
					werewolvesSeer.add(agent);
				}
			}
		}

		//役職CO(占い師・霊媒師)した人を取得
		werewolvesRole.clear();
		for (Agent agent : aliveOthers){
			if(comingoutMap.get(agent) == Role.SEER || comingoutMap.get(agent) == Role.MEDIUM){
				Agent candidate = agent;
				if(isAlive(candidate) && !werewolvesRole.contains(candidate)){
					werewolvesRole.add(agent);
				}
			}
		}

		//グレーの人たち取得
		grayNoRoles.clear();
		for (Agent agent : aliveOthers){
			if(comingoutMap.get(agent) != Role.SEER && comingoutMap.get(agent) != Role.MEDIUM){
				Agent candidate = agent;
				if(isAlive(candidate) && !grayNoRoles.contains(candidate)){
					grayNoRoles.add(agent);
				}
			}
		}

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

				//10 ~ 6: 占ロラ
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
			DoubtMeQ++;
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

	public String whisper(){
		throw new UnsupportedOperationException();
	}
	public Agent attack(){
		throw new UnsupportedOperationException();
	}
	public Agent divine(){
		throw new UnsupportedOperationException();
	}
	public Agent guard(){
		throw new UnsupportedOperationException();
	}
}
