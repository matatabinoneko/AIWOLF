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

public class Werewolf extends DemoRole {

	public Werewolf(GameResult gameResult) {
		super(gameResult);

	}

	@Override
	public void turn0Action() {
		voteAgent = randomSelect(grayList);
		talkQueue.offer(TalkFactory.voteRemark(voteAgent));
	}

	@Override
	public void turn0Action15() {
		voteAgent = randomSelect(grayList);
		talkQueue.offer(TalkFactory.voteRemark(voteAgent));
	}

	@Override
	public void dayStartAction() {
		dayStartAction15();
		//2日目にPPできる狂人を特定(自分に黒だしした占い師以外)
		if(getDate()==2 && notPPAgent!=null) {
			for(Agent agent: SeerList){
				if(agent!=notPPAgent) {
					PPAgent=agent;
				}
			}
		}
	}

	@Override
	public void dayStartAction15() {

		/**毎日AttackList(生きてる)を更新*/
		for(Agent agent: AttackList) {
			if(!isAlive(agent)) {
				AttackList.remove(agent);
			}
		}

		//釣られたエージェントが味方人狼だった場合
		for(Agent agent: WerewolfList) {
			if(agent == outcast) {
				wolfCount = wolfCount-1;
				//潜伏人狼が1人でも釣られたら
				if(HidingWolfList.contains(agent)) {
					hidingWerewolfDied = true;
				}
			}
		}

		//人狼なしのgrayList
		for(Agent agent: grayList) {
			if(!WerewolfList.contains(agent)) {
				grayListWithoutWolf.add(agent);
			}
		}

	}

	@Override
	public void updateAction(Talk talk, Content content) {

		/**
		 * 村人と全く同じ動作エージェント
		 */

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
			//自分以外がCOした場合
			if(talk.getAgent()!=me) {
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
			if(content.getResult()==Species.WEREWOLF) {
				if(content.getTarget()==me) {
					blackList.add(talk.getAgent());
					Possessed = talk.getAgent();
					notPPAgent = talk.getAgent();
				}
				//自分以外に黒だししたエージェント(後々PP)
				else {
					PPAgent=talk.getAgent();
				}
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

			/*狂人がいなかった場合
			 * ・初めにVOTE発言した人に便乗
			 * ・初めに自分にVOTEしてきたらその人にVOTE
			*/
			if(PPAgent!=null && getDate()==2 && turn==0 && voteAgent==null) {
				if(talk.getAgent()!=me) {
					if(content.getTarget()==me) {
						voteAgent=content.getTarget();
					}
					else {
						voteAgent=talk.getAgent();
					}
				}
			}
		}

	}

	@Override
	public void updateAction15(Talk talk, Content content) {

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
			//自分が役職をCOしたらフラグを立てる
			if(talk.getAgent()==me) {
				if(content.getRole() == Role.SEER) {
					SeerCO=true;
				}
				else if(content.getRole() == Role.MEDIUM){
					MediumCO=true;
				}
				else {
					VillagerCO=true;
				}
			}
			//人狼がCOしたら(村人以外)
			if(WerewolfList.contains(talk.getAgent()) && content.getRole()==Role.VILLAGER) {
				HidingWolfList.remove(talk.getAgent());
			}
		}

		/*DIV発言時の行動*/
		if(content.getTopic() == Topic.DIVINED) {
			//自分に黒だしした人はbrackListかつ狂人認定
			if(content.getTarget()==me && content.getResult()==Species.WEREWOLF) {
				blackList.add(talk.getAgent());
				Possessed = talk.getAgent();
			}
			//潜伏人狼がDIVされたら
			if(HidingWolfList.contains(content.getTarget())) {
				if(content.getTarget()!=me) {
					divinedHidingWolf = content.getTarget();
				}
			}
		}

		/*VOTE発言時の行動*/
		if(content.getTopic() == Topic.VOTE) {


		}
	}

	@Override
	public void talkAction() {
	}

	@Override
	public void action(Talk talk, Content content) {
		//最多VOTE数エージェント
		highestVotedAgent = getHighestVoteAgent();

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
					if(!blackList.isEmpty() && !esVillager) {
						Agent agent = randomSelect(AgentDataMap.get(Seer).SeerWhiteList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.VILLAGER));
						esVillager=true;
					}
				}

				/*狂人がいたらES*/
				if(Possessed!=null && esPossessed==false) {
					talkQueue.offer(TalkFactory.estimateRemark(Possessed, Role.POSSESSED));
					esPossessed=true;
				}
			}
		}

		/*2日目以降*/
		else if(getDate() == 2){

			/*狂人が生きていたら場合
			 * 1.占CO
			 * 2.狂人以外のエージェントに黒だし&VOTE
			 */
			if(PPAgent!=null && isAlive(PPAgent)) {
				talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
				SeerCO=true;
				for(Agent agent: getAliveOthersList()) {
					if(agent!=PPAgent) {
						talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.WEREWOLF));
						voteAgent=agent;
						break;
					}
				}
			}

			/*狂人がいなかった場合(updateActionにて)
			 * ・初めにVOTE発言した人に便乗
			 * ・初めに自分にVOTEしてきたらその人にVOTE
			*/

		}

		/*1日目のvoteAgent登録*/
		if(voteAgent==null) {
			//brackListが空でなければ
			if(blackList.size() != 0) {
				voteAgent = randomSelect(blackList);
			}
			//brackListが空であれば
			else{
				for(Agent agent: AgentDataMap.keySet()) {
					if(AgentDataMap.get(agent).score<0 && grayList.contains(agent)) {
						voteAgent = agent;
						break;
					}
				}
				if(voteAgent==null){
					voteAgent = highestVotedAgent;
				}
			}
		}

		/*2日目のvoteAgent登録(発言時に)*/

		if(voteAgent==null){
			voteAgent = highestVotedAgent;
		}

		/**VOTE処理*/
		if(voteTalkCount<3) {
			talkQueue.offer(TalkFactory.voteRemark(voteAgent));
			voteAgent=null;
			voteTalkCount++;
		}
		else{
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


		/**初日のCO動作(1日目 or 村人)
		 *・CO動作(1日目)
		 *・村人語り
		*/

		if(!isCO) {
			if(turn==0) {
				talkQueue.offer(TalkFactory.voteRemark(randomSelect(grayList)));
			}
			else if(turn == 1){
				//味方人狼がCOしていなかったら
				for(Agent agent: WerewolfList) {
					if(!SeerList.contains(agent)) {
						talkQueue.offer(TalkFactory.comingoutRemark(me,Role.SEER));
						SeerCO=true;
						Agent ag = randomSelect(grayList);
						talkQueue.offer(TalkFactory.divinedResultRemark(ag,Species.HUMAN));
						divineRemark=true;
					}
					else if(!MediumList.contains(agent) && getDate() == 2) {
						talkQueue.offer(TalkFactory.comingoutRemark(me,Role.MEDIUM));
						MediumCO=true;
						if(wolfCount<3) {
							talkQueue.offer(TalkFactory.identRemark(outcast,Species.WEREWOLF));
							identRemark=true;
						}
						else {
							talkQueue.offer(TalkFactory.identRemark(outcast,Species.HUMAN));
							identRemark=true;
						}
					}
					else {
						//村人のフリ
						if(!OverFlag) {
							talkQueue.offer(TalkFactory.voteRemark(highestVotedAgent));
							voteTalkCount++;
						}
						else {
							talkQueue.offer(TalkFactory.overRemark());
						}
					}
				}
			}

			/*村人のフリ*/
			else {
				//誰かがOVERするまで発言を続ける
				if(!OverFlag) {
					talkQueue.offer(TalkFactory.voteRemark(highestVotedAgent));
					voteTalkCount++;
				}
				else {
					talkQueue.offer(TalkFactory.overRemark());
				}
			}
		}


		/**動作の基準は潜伏人狼の状況(潜伏人狼を守る)
		 *1.占い師語り
		 *2.霊媒師語り
		 */
		else {
			/*占い師語り*/
			if(SeerCO) {
				/*潜伏人狼が生きてたら*/
				if(!hidingWerewolfDied) {

					/*潜伏人狼がまだ潜伏している(DIVされていない)*/
					if(divinedHidingWolf == null) {

						/*DIV動作*/
						if(!divineRemark) {
							List<Agent> list = new ArrayList<>();
							for(Agent agent: DivineMap.keySet()) {
								list.add(agent);
							}
							//信用あり(自分がDIVした相手が釣られた) || 3日目
							if(list.contains(outcast) || getDate() == 3) {
								//潜伏人狼を白だし
								if(!HidingWolfList.isEmpty()) {
									Agent ag=randomSelect(HidingWolfList);
									talkQueue.offer(TalkFactory.divinedResultRemark(ag, Species.HUMAN));
								}
							}
							//まだ信用なし
							else {
								//grayListWithoutWolfからランダム白だし
								if(!grayListWithoutWolf.isEmpty()) {
									Agent ag = randomSelect(grayListWithoutWolf);
									talkQueue.offer(TalkFactory.divinedResultRemark(ag, Species.HUMAN));
								}
							}
						}

						/*VOTE動作*/
						else {
							if(voteTalkCount<3) {
								voteAgent = highestVotedAgent;
								talkQueue.offer(TalkFactory.voteRemark(voteAgent));
								voteTalkCount++;
							}
							else {
								talkQueue.offer(TalkFactory.overRemark());
							}
						}

					}

					/*潜伏人狼がDIV済み*/
					else if(divinedHidingWolf!=null && AgentDataMap.get(divinedHidingWolf).DivinedResultMap.get(Species.WEREWOLF)==1) {
						/*DIV動作*/
						if(!divineRemark) {
							if(!grayListWithoutWolf.isEmpty()) {
								voteAgent = randomSelect(grayListWithoutWolf);
								talkQueue.offer(TalkFactory.divinedResultRemark(voteAgent, Species.WEREWOLF));
								talkQueue.offer(TalkFactory.voteRemark(voteAgent));
								talkQueue.offer(TalkFactory.overRemark());
							}
							else if(voteAgent==null) {
								voteAgent = highestVotedAgent;
								talkQueue.offer(TalkFactory.voteRemark(voteAgent));
								talkQueue.offer(TalkFactory.overRemark());
							}
						}
						/*VOTE動作*/
						else {
							if(voteTalkCount<3) {
								voteAgent = highestVotedAgent;
								talkQueue.offer(TalkFactory.voteRemark(voteAgent));
								voteTalkCount++;
							}
							else {
								talkQueue.offer(TalkFactory.overRemark());
							}
						}

					}
					/*潜伏人狼がDIV済み(byme)*/
					else if(divinedHidingWolf!=null && AgentDataMap.get(divinedHidingWolf).DivinedResultMap.get(Species.HUMAN)==1) {
						/*DIV動作*/
						if(!divineRemark) {
							Agent agent = randomSelect(grayList);
							talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
						}
						/*VOTE動作*/
						else {
							if(voteTalkCount<3) {
								voteAgent = highestVotedAgent;
								talkQueue.offer(TalkFactory.voteRemark(voteAgent));
								voteTalkCount++;
							}
							else {
								talkQueue.offer(TalkFactory.overRemark());
							}
						}
					}

				}
				/*潜伏人狼が生きていなかったら*/
				else {
					/*DIV動作*/
					if(!divineRemark) {
						//ずっと白出し(信用させるため)
						Agent agent = randomSelect(grayListWithoutWolf);
						talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					}
					/*VOTE動作*/
					else {
						talkQueue.offer(TalkFactory.identRemark(outcast,Species.WEREWOLF));
					}

				}
			}

			/*霊媒師語り(3日目以降)*/
			else if(MediumCO) {
				/*IDENTIED動作*/
				if(!identRemark) {
					if(!WerewolfList.contains(outcast)) {
						talkQueue.offer(TalkFactory.identRemark(outcast,Species.WEREWOLF));
					}
					else{
						talkQueue.offer(TalkFactory.identRemark(outcast,Species.HUMAN));
					}
				}
				/*VOTE動作*/
				else {
					if(voteTalkCount<3) {
						voteAgent = highestVotedAgent;
						talkQueue.offer(TalkFactory.voteRemark(voteAgent));
						voteTalkCount++;
					}
					else {
						talkQueue.offer(TalkFactory.overRemark());
					}
				}
			}
		}

		//自分に黒だしする人がいたら
		if(Possessed!=null && esPossessed==false) {
			talkQueue.offer(TalkFactory.estimateRemark(Possessed, Role.POSSESSED));
		}

		/**brackListが空だったらVOTE数が多いエージェント
		 * VOTE処理をまとめるか
		 */
//		else{
//			voteAgent = highestVotedAgent;
//			talkQueue.offer(TalkFactory.voteRemark(voteAgent));
//		}


	}

}
