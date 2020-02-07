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

public class Possessed extends DemoRole {

	public Possessed(GameResult gameResult) {
		super(gameResult);

	}

	@Override
	public void turn0Action() {
		if(!SeerCO) {
			talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
			SeerCO=true;
		}
		else {
			//生きているエージェント全てに黒だし(吊ってもらうために)
			for(Agent agent: getAliveOthersList()) {
				talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.WEREWOLF));
				divineRemark=true;
			}
		}

	}

	@Override
	public void turn0Action15() {

		/*占い師語り*/
		if(seerMovement) {
			//CO動作
			if(!SeerCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
			}
			//DIV動作
			else {
				if(maybeHidingWolfList.size()!=0) {
					Agent agent = randomSelect(maybeHidingWolfList);
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					divineRemark=true;
				}
				else {
					Agent agent = randomSelect(grayList);
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					divineRemark=true;
				}
			}
		}
		/*霊媒語り*/
		else {
			//1-2日目(村人)
			if(getDate() <= 2) {
				voteAgent = randomSelect(grayList);
				talkQueue.offer(TalkFactory.voteRemark(voteAgent));
			}
			//3日目以降(霊媒師)
			else {
				//CO動作
				if(!MediumCO) {
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
					MediumCO=true;
					MediumList.remove(me);
				}
				//ID動作
				else {
					if(getDate() == 3) {
						if(!MediumList.isEmpty()) {
							Medium = randomSelect(MediumList);
							Seer = randomSelect(AgentDataMap.get(Medium).connectionList);
						}
						if(Seer==null) {
							Seer = randomSelect(SeerList);
						}
					}
					if(Medium!=null && AgentDataMap.get(Medium).MediumWhiteList.contains(outcast)) {
						talkQueue.offer(TalkFactory.identRemark(outcast, Species.WEREWOLF));
						identRemark=true;
					}
					else {
						talkQueue.offer(TalkFactory.identRemark(outcast, Species.HUMAN));
						identRemark=true;
					}
				}

			}
		}

	}

	@Override
	public void dayStartAction() {
	}

	@Override
	public void dayStartAction15() {

	}

	@Override
	public void updateAction(Talk talk, Content content) {

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
			//自分以外がCOしたら
			if(talk.getAgent() != me){
				if(content.getRole() == Role.SEER) {
					Seer=talk.getAgent();
					//1日目のSeerCOのタイミング
					if(getDate() == 1) {
						SeerCoTimingMap.put(talk.getAgent(), turn);
					}
				}
			}
		}

		/*DIV発言時の行動*/
		if(content.getTopic() == Topic.DIVINED) {
			//偽占い師がDIVしたら
			if(talk.getAgent() != me){
				grayList.remove(content.getTarget());
				provisionalWhiteList.add(content.getTarget());
			}
		}

		/*ES発言時の行動*/
		/*
		 *1.upScore
		 *・meに占い師ES
		 *・me以外に狂人ES
		 *2.downScore
		 *・meに狂人ES
		 *・me以外に占い師ES
		 */
		if(content.getTopic() == Topic.ESTIMATE) {
			if(talk.getAgent()!=me) {
				if(content.getTarget()!=me) {
					if(content.getRole()==Role.POSSESSED) {
						AgentDataMap.get(talk.getAgent()).upScore();
					}
					else if(content.getRole()==Role.SEER) {
						AgentDataMap.get(talk.getAgent()).downScore();
					}
				}
				else {
					if(content.getRole()==Role.SEER) {
						AgentDataMap.get(talk.getAgent()).upScore();
					}
					else if(content.getRole()==Role.POSSESSED) {
						AgentDataMap.get(talk.getAgent()).downScore();
					}
				}
			}
		}

		/*VOTE発言時*/
		if(content.getTopic() == Topic.VOTE) {
			if(talk.getAgent()!=me && content.getTarget() == me) {
				myVoteCountByTurn++;
				agentVotingMe = talk.getAgent();
			}
		}

	}

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
				AgentDataMap.get(Possessed).downScore();
			}

			else if(content.getResult() == Species.WEREWOLF) {
				blackList.add(content.getTarget());
			}

			//占COのすぐ後にDIVしていないエージェントは狂人認定()
			if(SeerCoTimingMap.containsKey(talk.getAgent()) && turn+1 >= SeerCoTimingMap.get(talk.getAgent())) {
				Possessed = talk.getAgent();
				AgentDataMap.get(Possessed).downScore();
			}
		}

		/*ES発言時の行動*/
		if(content.getTopic() == Topic.ESTIMATE) {
			if(content.getRole() == Role.SEER && Possessed==null) {
				Possessed = talk.getAgent();
				AgentDataMap.get(Possessed).downScore();
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
	public void action(Talk talk, Content content) {

		/*1日目*/
		if(getDate() == 1) {
			if(!SeerCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
				SeerCO=true;

			}

			//偽占い師がturn=0にCOしていた場合
			else if(Seer!=null && SeerCoTimingMap.get(Seer)==0) {
				if(!divineRemark) {
					talkQueue.offer(TalkFactory.divinedResultRemark(Seer, Species.HUMAN));
					divineRemark=true;
					talkQueue.offer(TalkFactory.estimateRemark(Seer, Role.POSSESSED));
					esPossessed=true;
				}
			}
			//偽占い師がCOしてこない,turn=0にCOしていない場合
			else {
				Agent agent= randomSelect(grayList);
				if(!divineRemark) {
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					divineRemark=true;
				}
				//後から占い師COしてきた場合
				if(!esPossessed && Seer!=null) {
					talkQueue.offer(TalkFactory.estimateRemark(Seer, Role.POSSESSED));
					esPossessed=true;
				}
			}
			voteAgent = getLowestScoreAgent();
		}

		/*2日目*/
		else{
			if(!divineRemark) {
				//生きているエージェント全てに黒だし(吊ってもらうために)
				for(Agent agent: getAliveOthersList()) {
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.WEREWOLF));
					divineRemark=true;
				}
			}

			/*voteAgent決定*/
			if(myVoteCountByTurn == 1) {
				voteAgent = agentVotingMe;
			}
			else {
				voteAgent = randomSelect(getAliveOthersList());
			}
		}


		if(voteAgent==null){
			voteAgent = getLowestScoreAgent();;
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

	@Override
	public void action15(Talk talk, Content content) {


		//最多VOTE数エージェント
		highestVotedAgent = getHighestVoteAgent();
		//最多VOTE数エージェントがgrayList外だった場合
		if(!grayList.contains(highestVotedAgent)) {
			highestVotedAgent = getHighestVoteAgent(highestVotedAgent);
		}

		/*占い師動作*/
		if(seerMovement) {
			//まずは占い師CO
			if(!SeerCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
				SeerCO=true;
			}
			else if(!divineRemark){
				if(maybeHidingWolfList.size()!=0) {
					Agent agent = randomSelect(maybeHidingWolfList);
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					divineRemark=true;
				}
				else {
					Agent agent = randomSelect(grayList);
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					divineRemark=true;
				}
			}

			//霊媒師とのラインがあってまだ霊媒師ESしていなかったら
			if(myConnectionList != null && doutMedium == null && !esMedium) {
				doutMedium = randomSelect(myConnectionList);
				talkQueue.offer(TalkFactory.estimateRemark(doutMedium, Role.MEDIUM));
				esMedium=true;
			}
		}

		/*霊媒師動作*/
		/*
		 *1.初ターンで占い師が1人の場合
		 * -後から占COしたエージェント＝真占い師
		 * -初めに占COしたエージェント＝人狼
		 *2.初ターンで占い師が2人以上
		 * -
		 */
		else {
			/*1日目(村人のフリ)*/
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
							esWolf=true;
						}
					}

					/*狂人がいたらES*/
					if(Possessed!=null && esPossessed==false) {
						talkQueue.offer(TalkFactory.comingoutRemark(me, Role.VILLAGER));
						VillagerCO=true;
						talkQueue.offer(TalkFactory.estimateRemark(Possessed, Role.POSSESSED));
						esPossessed=true;
					}
				}
			}

			/*2日目(村人のフリ)*/
			else if(getDate() == 2){
				if(!esWolf) {
					if(!blackList.isEmpty() && !esWolf) {
						Agent agent = randomSelect(blackList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
						esWolf=true;
					}
				}
			}

			/*3日目以降(霊媒師CO)*/
			else{
				//まずCO
				if(!MediumCO) {
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
					MediumCO=true;
					MediumList.remove(me);
				}
				//次IDENTFIED
				else if(!identRemark){
					if(getDate() == 3) {
						MediumList.remove(me);
						if(!MediumList.isEmpty()) {
							Medium = randomSelect(MediumList);
							if(!AgentDataMap.get(Medium).connectionList.isEmpty()) {
								Seer = randomSelect(AgentDataMap.get(Medium).connectionList);
							}
						}
						if(Seer==null) {
							Seer = randomSelect(SeerList);
						}
					}
					if(Medium!=null && AgentDataMap.get(Medium).MediumWhiteList.contains(outcast)) {
						talkQueue.offer(TalkFactory.identRemark(outcast, Species.WEREWOLF));
						identRemark=true;
					}
					else {
						talkQueue.offer(TalkFactory.identRemark(outcast, Species.HUMAN));
						identRemark=true;
					}

				}

				//まだESしていなかった場合(doutWerewolf=Medium)
				if(!esWolf && identRemark) {
					talkQueue.offer(TalkFactory.estimateRemark(Medium, Role.WEREWOLF));
					esWolf=true;
					doutWerewolf = Medium;

				}
				//Mediumが死んでいた場合(doutWerewole=Seer)
				else if(identRemark){
					if(!isAlive(doutWerewolf)) {
						doutWerewolf = Seer;
						if(!isAlive(Seer)) {
							if(Medium!=null && !AgentDataMap.get(Medium).connectionList.isEmpty()) {
								for(Agent agent: AgentDataMap.get(Medium).connectionList) {
									if(isAlive(agent)) {
										Seer = randomSelect(AgentDataMap.get(Medium).connectionList);
										break;
									}
								}
							}
							//Seerすべて死んでいた場合(doutWerewolf=null)
							doutWerewolf=null;
						}
					}
				}
			}
		}

		/**VOTE処理
		 * 1.占い師語り
		 * 2.霊媒師語り
		 * -村人のフリ(1-2日目)
		 * -霊媒師のフリ(3日目以降)
		 * 3.その他
		 */


		//偽人狼が生きていたら(
		if(doutWerewolf!=null) {
			voteAgent = doutWerewolf;
		}
		//brackListが空でなければ(村人のフリをしている時)
		else if(blackList.size() != 0) {
			voteAgent = randomSelect(blackList);
		}
		//brackListが空であれば(村人のフリをしている時)
		else{
			for(Agent agent: AgentDataMap.keySet()) {
				if(AgentDataMap.get(agent).score<0 && grayList.contains(agent)) {
					voteAgent = agent;
					break;
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
