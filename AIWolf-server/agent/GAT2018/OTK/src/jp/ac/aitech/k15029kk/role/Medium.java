package jp.ac.aitech.k15029kk.role;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import jp.ac.aitech.k15029kk.GameResult;
import jp.ac.aitech.k15029kk.TalkFactory;
import jp.ac.aitech.k15029kk.base.DemoRole;

public class Medium extends DemoRole {

	public Medium(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void turn0Action15() {
		Judge judge = gameInfo.getMediumResult();

		if(judge == null && !gameInfo.getAliveAgentList().contains(me)) {
			return;
		}
		//1日目は村人語り
		if(getDate()==1) {
			voteAgent = randomSelect(grayList);
			talkQueue.offer(TalkFactory.voteRemark(voteAgent));
		}
		//2日目以降は霊媒師
		else {
			if(!MediumCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(gameInfo.getAgent(), Role.MEDIUM));
				MediumCO=true;
			}
			//identRemarkしていなかったら霊媒発表
			else if(!identRemark) {
				talkQueue.offer(TalkFactory.identRemark(judge.getTarget(), judge.getResult()));
				identRemark=true;

			}
		}
	}


	@Override
	public void dayStartAction15() {
		this.MyConection();

	}

	/**前夜の霊媒結果からラインを確認*/
	public void MyConection() {
		Judge judge = gameInfo.getMediumResult();


		//Connection判定はjudge結果なしの時は以下の動作はしない
		if(judge == null) {
			return;
		}

		IdentMap.put(judge.getTarget(), judge.getResult());

		for(Agent agent: AgentDataMap.keySet()) {
			//占い師側のAgentDataのConectionList
			Agent ag = AgentDataMap.get(agent).SeerConection(me,judge.getTarget(),judge.getResult());
			//霊媒師側のAgentDataのConectionList
			if(ag != null && ag!=me && Seer!=null) {
				myConnectionList.add(ag);
				//本物の占い師認定
				Seer = ag;
			}
			else {
				myConnectionList.remove(ag);
			}
		}

		//もし占い師が吊られた
		for(Agent agent: SeerList) {
			if(judge.getTarget()==agent) {

				//ラインがあって白だった場合
				if(Seer!=null && Seer != agent && judge.getResult() == Species.HUMAN) {
					Possessed = judge.getTarget();

					//あぶれた偽物は全て人狼確定
					for(Agent ag: blackList) {
						if(ag!=Possessed) {
							WerewolfList.add(ag);
						}
					}
					//brackListにいなくても残りの占い師を人狼確定
					for(Agent ag: SeerList) {
						if(ag!=Seer && ag!=Possessed) {
							WerewolfList.add(ag);
						}
					}
				}

				//ラインがあって黒だった場合
				else if(Seer!=null && judge.getResult() == Species.WEREWOLF) {
					if(SeerList.size()==2) {
						for(Agent ag: MediumList) {
							if(ag!=me) {
								Possessed = ag;
							}
						}
					}

				}

				//ラインがなく白だし
				else if(judge.getResult() == Species.HUMAN){
					//brackListに入っていたら狂人確定(偽霊媒師とラインがあるから)
					if(blackList.contains(judge.getAgent())) {
						Possessed = judge.getAgent();
					}
				}
				//ラインがなく黒だし
				else if(judge.getResult() == Species.WEREWOLF) {
					//占い師2人だった場合は占い師と狂人確定
					if(SeerList.size()==2) {
						for(Agent ag: SeerList) {
							if(ag!=judge.getTarget() && Seer!=null) {
								Seer = ag;
								myConnectionList.add(Seer);
							}
						}
						for(Agent ag: MediumList) {
							if(ag!=me) {
								Possessed = ag;
							}
						}
					}
				}

			}
		}
	}



	@Override
	public void updateAction15(Talk talk, Content content) {

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
		}

		/*DIV発言時の行動*/
		if(content.getTopic() == Topic.DIVINED) {
			//自分に黒だしした人はbrackListかつ狂人認定
			if(content.getTarget()==me && content.getResult()==Species.WEREWOLF) {
				blackList.add(talk.getAgent());
				Possessed = talk.getAgent();
			}
		}

		/*ID発言時の行動*/
		if(content.getTopic() == Topic.IDENTIFIED) {

			if(talk.getAgent() != me){
				blackList.add(talk.getAgent());
				for(Agent agent: AgentDataMap.get(talk.getAgent()).connectionList) {
					blackList.add(agent);
				}
				//占い師3人，霊媒師2人の状況 or 狂人がすでに他に存在する
				if(SeerList.size()==3 || Possessed!=null && Possessed == talk.getAgent()) {
					WerewolfList.add(talk.getAgent());
				}
			}
		}


		/*真占い師判定*/
		List<Agent> list = new ArrayList<>();
		int count=0;
		int tmp=0;

		//占い師のなかで誰かが吊られていたら以下の動作はしない
		for(Agent agent: SeerList) {
			if(getDeadAgentList().contains(agent)) {
				return;
			}
		}
		//3回以上VOTEされたAgentは真占い師(本物の占い師は初めに人狼から投票されで多数決から吊られることが多い)
		for(Agent agent: SeerList) {
			if(AgentDataMap.get(agent).getVotedCount(agent)>3) {
				Seer=agent;
				list.add(agent);
				count++;
				if(count>=2) {
					for(Agent ag: list) {
						if(tmp<=AgentDataMap.get(ag).getVotedCount(agent)) {
							Seer=ag;
						}
					}
				}
			}
		}

	}

	@Override
	public void talkAction() {


	}


	@Override
	public void action15(Talk talk, Content content) {

		Judge judge = gameInfo.getMediumResult();
		List<Agent> voteList = new ArrayList<>();

		//最多VOTE数エージェント
		highestVotedAgent = getHighestVoteAgent();
		//最多VOTE数エージェントがgrayList外だった場合
		if(!grayList.contains(highestVotedAgent)) {
			highestVotedAgent = getHighestVoteAgent(highestVotedAgent);
		}

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

		/*2日目以降*/
		else {
			//まずは霊媒師
			if(!MediumCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(gameInfo.getAgent(), Role.MEDIUM));
				MediumCO=true;
			}

			//identRemarkしていなかったら霊媒発表
			if(!identRemark) {
				talkQueue.offer(TalkFactory.identRemark(judge.getTarget(), judge.getResult()));
				identRemark=true;

			}

			//とりあえず人狼ES
			if(!esWolf) {
				if(WerewolfList.size()!=0 && !esWolf) {
					Agent agent = randomSelect(WerewolfList);
					talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
					esWolf=true;
				}
				else{
					if(!blackList.isEmpty() && !esWolf) {
						Agent agent = randomSelect(blackList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
						esWolf=true;
					}
				}
			}
		}

		if(Seer!=null && !esSeer) {
			talkQueue.offer(TalkFactory.estimateRemark(Seer, Role.SEER));
			esSeer=true;
		}

		//ラインがある占い師がいればその人たちのbrackListからランダム
		if(myConnectionList != null) {
			for(Agent agent: myConnectionList) {
				if(!AgentDataMap.get(agent).SeerBlackList.isEmpty()) {
					for(Agent ag: AgentDataMap.get(agent).SeerBlackList) {
						voteList.add(ag);
					}
				}
			}
			voteAgent = randomSelect(voteList);
		}

		//ラインがなければ自分のWereWolfListからesWolfしたエージェントにVOTE
		else if(esWolfAgent!=null) {
			voteAgent = esWolfAgent;
		}
		//brackListが空でなければ
		else if(blackList.size() != 0) {
			voteAgent = randomSelect(blackList);
		}
		//brackListが空でなければ
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
