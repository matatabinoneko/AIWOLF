package jp.ac.aitech.k15029kk.role;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import jp.ac.aitech.k15029kk.GameResult;
import jp.ac.aitech.k15029kk.TalkFactory;
import jp.ac.aitech.k15029kk.base.DemoRole;
import jp.ac.aitech.k15029kk.util.RandomSelect;

public class Bodyguard extends DemoRole {

	public Bodyguard(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void turn0Action15() {
		voteAgent = randomSelect(grayList);
		talkQueue.offer(TalkFactory.voteRemark(voteAgent));
	}

	@Override
	public void dayStartAction15() {
		/**もし誰も噛まれなかったら*/
		if(getAliveOthersList().size()-1==gameInfo.getAliveAgentList().size()) {
			Seer=ProvisionalSeer;
		}
	}


	@Override
	public void updateAction15(Talk talk, Content content) {

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
			//自分以外がCOした場合
			if(talk.getAgent() != me){
				if(content.getRole()==Role.SEER) {
					//1日目のSeerCOのタイミング
					if(getDate() == 1) {
						SeerCoTimingMap.put(talk.getAgent(), turn);
					}
				}
			}
		}

		/*DIV発言時の行動*/
		if(content.getTopic() == Topic.DIVINED) {
			//自分に黒だしした人はbrackListかつ狂人認定
			if(content.getTarget()==me && content.getResult()==Species.WEREWOLF) {
				blackList.add(talk.getAgent());
				Possessed = talk.getAgent();
			}

			else if(content.getResult() == Species.WEREWOLF) {
				blackList.add(content.getTarget());
			}

			//占COのすぐ後にDIVしていないエージェントは狂人認定()
			if(SeerCoTimingMap.containsKey(talk.getAgent()) && turn+1 >= SeerCoTimingMap.get(talk.getAgent())) {
				Possessed = talk.getAgent();
			}
		}

		/*ES発言時の行動*/
		if(content.getTopic() == Topic.ESTIMATE) {
			if(content.getRole() == Role.SEER && Possessed==null) {
				Possessed = talk.getAgent();
			}
			if(talk.getAgent()==me) {
				if(content.getRole()==Role.POSSESSED || content.getRole()==Role.WEREWOLF) {
					AgentDataMap.get(content.getTarget()).downScore();
				}
			}
		}

		/*VOTE発言時の行動*/
		if(content.getTopic() == Topic.VOTE) {
			if(talk.getAgent()!=me) {
				if(content.getTarget()==Possessed || WerewolfList.contains(content.getTarget())) {
					AgentDataMap.get(talk.getAgent()).upScore();
				}

			}
		}
	}


	@Override
	public void talkAction() {
	}

	@Override
	public void action15(Talk talk, Content content) {

		//最多VOTE数エージェント
		highestVotedAgent = getHighestVoteAgent();
		//最多VOTE数エージェントがgrayList外だった場合
		if(!grayList.contains(highestVotedAgent)) {
			highestVotedAgent = getHighestVoteAgent(highestVotedAgent);
		}

		/*1日目*/
		if(getDate()==1){
			if(turn>=2) {
				//占COのタイミングの整理
				List<Agent> turn0SeerList = new ArrayList<>();
				List<Agent> turn1SeerList = new ArrayList<>();
				List<Agent> turnSeerList = new ArrayList<>();
				Agent seeragent=null;

				for(Agent agent: SeerCoTimingMap.keySet()) {
					//初日のturn=0でCOした占い師
					if(SeerCoTimingMap.get(agent)==0) {
						turn0SeerList.add(agent);
					}
					//turn=0以降にCOした占い師
					else {
						if(SeerCoTimingMap.get(agent)==1) {
							turn1SeerList.add(agent);
						}
						else {
							turnSeerList.add(agent);
						}
					}
				}

				//SeerCOのタイミングが全体的に遅い場合
				if(turn0SeerList.isEmpty()) {
					if(turn1SeerList.isEmpty()) {
						turn0SeerList=turnSeerList;
					}
					else {
						turn0SeerList=turn1SeerList;
					}
				}
				else {
					turnSeerList=turn1SeerList;
				}

				/*turn=0占COが1人の場合*/
				if(turn0SeerList.size()==1) {
					//占CO1人(seeragent)
					if(turnSeerList.size()==0){
						//DIVしなかったら(狂人)
						seeragent = randomSelect(turn0SeerList);
						if(AgentDataMap.get(seeragent).SeerBlackList.size()==0 && AgentDataMap.get(seeragent).SeerWhiteList.size()==0 && !esPossessed){
							Possessed=seeragent;
							talkQueue.offer(TalkFactory.estimateRemark(seeragent, Role.POSSESSED));
							esPossessed=true;
						}
						//DIVしたら(真占い師)
						else {
							Seer=seeragent;
							if(AgentDataMap.get(Seer).SeerBlackList.size()!=0) {
								Agent agent = randomSelect(AgentDataMap.get(Seer).SeerBlackList);
								WerewolfList.add(agent);
								if(!esWolf) {
									talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
									esWolf=true;
								}
							}
							else {
								if(!AgentDataMap.get(Seer).SeerWhiteList.isEmpty() && !esVillager) {
									Agent agent = randomSelect(AgentDataMap.get(Seer).SeerWhiteList);
									talkQueue.offer(TalkFactory.estimateRemark(agent, Role.VILLAGER));
									esVillager=true;
								}
							}
						}
					}

					//占CO2人以上(後からきたエージェントが真占い師)
					else{
						seeragent = randomSelect(turn0SeerList);
						for(Agent agent: turnSeerList) {
							if(AgentDataMap.get(agent).SeerBlackList.size()==0 && AgentDataMap.get(agent).SeerWhiteList.size()==0){
								Possessed=agent;
							}
							else {
								Seer=agent;
								break;
							}
						}
						WerewolfList.add(seeragent);
						if(!esWolf) {
							talkQueue.offer(TalkFactory.estimateRemark(seeragent, Role.WEREWOLF));
							esWolf=true;
						}
					}
				}

				/*turn=0占COが1人以上の場合*/
				else {
					if(!blackList.isEmpty() && !esWolf) {
						Agent agent = randomSelect(blackList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
					}
				}

				/*狂人がいたらES*/
				if(Possessed!=null && esPossessed==false) {
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.VILLAGER));
					talkQueue.offer(TalkFactory.estimateRemark(Possessed, Role.POSSESSED));
				}
			}
		}

		/*2日目以降*/
		else if(getDate() >= 2){
			if(!blackList.isEmpty() && !esWolf) {
				Agent agent = randomSelect(blackList);
				talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
			}

		}

		/**もし本物のSeerがいたら
		 * まずBodygardCO
		 *1.他の占い師をbrackListに(毎ターン更新)
		 *2.その占い師とラインがある霊媒師もbrackListに
		 *3.SeerListが2人の場合，3人以上の場合
		*/
		if(Seer!=null && !BodygardCO) {

			talkQueue.offer(TalkFactory.comingoutRemark(me,Role.BODYGUARD));
			talkQueue.offer(TalkFactory.guardedAgentRemark(Seer));
			talkQueue.offer(TalkFactory.estimateRemark(Seer,Role.SEER));

			//偽占い師とその仲間をbrackListに
			for(Agent agent: SeerList) {
				if(agent!=Seer) {
					//1.他の偽占い師をbrackListに
					blackList.add(agent);
					//2.その占い師とラインがある霊媒師もbrackListに
					wolfConectionList=AgentDataMap.get(agent).connectionList;
					for(Agent ag: wolfConectionList) {
						blackList.add(ag);

						//3.SeerListが2人の場合(本物入れて)
						if(SeerList.size()==2 && !esPossessed) {
							talkQueue.offer(TalkFactory.estimateRemark(agent,Role.POSSESSED));
							Possessed=agent;
							talkQueue.offer(TalkFactory.estimateRemark(ag,Role.WEREWOLF));
							WerewolfList.add(ag);
						}
						//すでにPossessedがいる場合(自分に黒だししてきたやつは完全に狂人)
						else {
							talkQueue.offer(TalkFactory.estimateRemark(agent,Role.WEREWOLF));
							WerewolfList.add(agent);
						}
					}

				}
			}
		}

		/*brackListが空じゃなければ*/
		if(blackList.size() != 0) {
			Agent selectwolf = null;
			selectwolf = RandomSelect.get(WerewolfList,getDeadAgentList());

			if(WerewolfList!=null && selectwolf!=null) {
				voteAgent = selectwolf;
			}
			//Werewolfいない or Werewolfが死んでる場合blackListから点数の低いエージェントをランダム
			else {
				for(Agent agent: AgentDataMap.keySet()) {
					if(AgentDataMap.get(agent).score<0 && grayList.contains(agent)) {
						voteAgent = agent;
						break;
					}
				}
			}
		}

		if(voteAgent==null){
			voteAgent = highestVotedAgent;
		}

		/**VOTE処理*/
		if(voteTalkCount<3) {
			talkQueue.offer(TalkFactory.voteRemark(voteAgent));
			voteAgent=null;
			voteTalkCount++;
		}
		else {
			talkQueue.offer(TalkFactory.overRemark());
		}
	}

}
