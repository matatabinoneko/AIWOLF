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
import jp.ac.aitech.k15029kk.util.RandomSelect;

public class Seer extends DemoRole {

	public Seer(GameResult gameResult) {
		super(gameResult);
	}

	@Override
	public void turn0Action() {
		//1日目CO
		if(!SeerCO) {
			talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
			SeerCO=true;
		}
		//2日目CO
		else {
			/*人狼を当てている場合*/
			if(!WerewolfList.isEmpty()) {
				//狂人が生きている場合
				if(Possessed!=null && isAlive(Possessed) && !WolfCO) {
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.WEREWOLF));
					WolfCO=true;
					voteAgent = Possessed;
				}
				//狂人がいない場合
				else if(!WolfCO && !PossessedCO){
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.POSSESSED));
					PossessedCO=true;
					Agent wolf = randomSelect(WerewolfList);
					talkQueue.offer(TalkFactory.estimateRemark(wolf, Role.VILLAGER));
					esVillager=true;
					for(Agent ag: getAliveOthersList()) {
						if(ag!=wolf) {
							voteAgent = ag;
						}
					}
				}

			}
			/*まだ人狼を当てていない場合*/
			else {
				//狂人が生きている場合
				if(Possessed!=null && isAlive(Possessed) && !WolfCO) {
					//狂人以外のエージェントを人狼Listに
					for(Agent agent: getAliveOthersList()) {
						if(agent!=Possessed) {
							WerewolfList.add(agent);
						}
					}
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.WEREWOLF));
					WolfCO=true;
					voteAgent = Possessed;
				}
				//狂人がいない場合
				else if(!WolfCO && !PossessedCO){

					//meにvoteしたことないエージェントをList化(人狼の可能性あり)
					List<Agent> list = new ArrayList<>();
					for(Agent agent: getAliveOthersList()) {
						if(!AgentDataMap.get(agent).voteList.contains(me)) {
							list.add(agent);
						}
					}

					Agent wolf = randomSelect(list);
					WerewolfList.add(wolf);

					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.POSSESSED));
					PossessedCO=true;
					for(Agent ag: getAliveOthersList()) {
						if(ag!=wolf) {
							voteAgent = wolf;
						}
					}
				}
			}

		}
	}

	@Override
	public void turn0Action15() {
		Judge judge = gameInfo.getDivineResult();

		if(judge == null) {
			return;
		}

		if(!SeerCO) {
			talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
			SeerCO=true;
		}
		else {
			talkQueue.offer(TalkFactory.divinedResultRemark(judge.getTarget(),judge.getResult()));
			divineRemark=true;
		}
	}

	@Override
	public void dayStartAction() {
		Judge judge = gameInfo.getDivineResult();

		/* エラー : judgeのnullチェック! */

		if(judge == null) {
			return;
		}

		DivineMap.put(judge.getTarget(), judge.getResult());

		if(judge.getResult()==Species.WEREWOLF) {
			WerewolfList.add(judge.getTarget());
		}

	}

	@Override
	public void dayStartAction15() {
		Judge judge = gameInfo.getDivineResult();

		/* エラー : judgeのnullチェック! */

		if(judge == null) {
			return;
		}

		DivineMap.put(judge.getTarget(), judge.getResult());


		if(judge.getResult()==Species.WEREWOLF) {
			WerewolfList.add(judge.getTarget());
			hidingWerewolf = judge.getTarget();
		}

		/**潜伏人狼の生存確認*/
		if(!isAlive(hidingWerewolf)) {
			hidingWerewolf = null;
			hidingWerewolfDied = true;
		}

		//2日目移行自分以外の占い師が1人の時狂人確定
		if(getDate() >= 2) {
			if(SeerList.size() == 2) {
				for(Agent agent: SeerList) {
					if(agent!=me) {
						Possessed=agent;
					}
				}
			}
		}

	}

	@Override
	public void updateAction(Talk talk, Content content) {

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
			//自分以外がCOしたら
			if(talk.getAgent()!=me){
				if(content.getRole() == Role.SEER) {
					Possessed=talk.getAgent();
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


	}

	@Override
	public void updateAction15(Talk talk, Content content) {

		/*CO発言時の行動*/
		if(content.getTopic() == Topic.COMINGOUT) {
			//自分以外がCOしたら
			if(talk.getAgent()!=me){
				if(DivineMap.containsKey(talk.getAgent())) {
					if(content.getRole()==Role.SEER) {
						if(DivineMap.get(talk.getAgent())==Species.HUMAN) {
							blackList.add(talk.getAgent());
							Possessed=talk.getAgent();
						}
					}
					else if(content.getRole()==Role.MEDIUM) {
						if(DivineMap.get(talk.getAgent())==Species.HUMAN) {
							Medium=talk.getAgent();
							myConnectionList.add(talk.getAgent());
							AgentDataMap.get(talk.getAgent()).connectionList.add(me);
						}
					}
				}

				//偽占い師はbrackList
				if(content.getRole() == Role.SEER) {
					blackList.add(talk.getAgent());
				}

				//黒だった奴がCOしだしたら潜伏人狼から削除
				if(talk.getAgent() == hidingWerewolf) {
					hidingWerewolf = null;
				}
			}

		}

		/*DIV発言時の行動*/
		if(content.getTopic() == Topic.DIVINED) {
			//自分に黒だしした人はbrackListかつ狂人認定
			if(content.getTarget()==me && content.getResult()==Species.WEREWOLF) {
				blackList.add(talk.getAgent());
				if(Possessed==null) {
					Possessed = talk.getAgent();
				}
				else {
					WerewolfList.add(talk.getAgent());
				}
			}
			//偽占い師がDIVしたら
			else if(talk.getAgent() != me){
				grayList.remove(content.getTarget());
				provisionalWhiteList.add(content.getTarget());
			}
		}

		/*ID発言時の行動*/
		if(content.getTopic() == Topic.IDENTIFIED) {
			//meとのConectionListへの処理
			for(Agent ag: DivineMap.keySet()) {
				if(ag == content.getTarget() && DivineMap.get(ag) == content.getResult()) {
					myConnectionList.add(talk.getAgent());
					if(Medium!=null) {
						Medium = talk.getAgent();
					}
					AgentDataMap.get(talk.getAgent()).MediumConection(me);
				}
				else {
					myConnectionList.remove(talk.getAgent());
					Medium = null;
					AgentDataMap.get(talk.getAgent()).MediumNotConection(me);
				}
			}
		}

	}

	@Override
	public void talkAction() {

	}

	@Override
	public void action(Talk talk, Content content) {
		Judge judge = gameInfo.getDivineResult();

		//最多VOTE数エージェント
		highestVotedAgent = getHighestVoteAgent();

		/*1日目*/
		if(getDate() == 1) {
			if(!SeerCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
				SeerCO=true;
			}

			/*人狼を当ててしまった場合*/
			else if(!WerewolfList.isEmpty()) {
				//偽占い師がturn=0にCOしていた場合
				if(Possessed!=null && SeerCoTimingMap.get(Possessed)==0) {
					if(!divineRemark) {
						talkQueue.offer(TalkFactory.divinedResultRemark(Possessed, Species.WEREWOLF));
						divineRemark=true;
					}
					//偽占い師が白だしし次第esVillager
					if(!AgentDataMap.get(Possessed).SeerWhiteList.isEmpty() && !esVillager) {
						Agent agent = randomSelect(AgentDataMap.get(Possessed).SeerWhiteList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.VILLAGER));
						esVillager=true;
					}
					voteAgent = Possessed;
				}
				//偽占い師がCOしてこない,turn=0にCOしていない場合
				else {
					Agent wolf= randomSelect(WerewolfList);
					if(!divineRemark) {
						talkQueue.offer(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
						divineRemark=true;
					}
					voteAgent = wolf;
				}
			}

			/*まだ人狼を当てていない場合*/
			else {
				//偽占い師がturn=0にCOしていた場合
				if(Possessed!=null && SeerCoTimingMap.get(Possessed)==0) {
					/*DIVする相手を決定*/
					Agent wolf;
					//白だった村人!=Possessedの場合s
					if(Possessed != judge.getTarget()) {
						wolf = judge.getTarget();
					}
					//白だった村人=Possessedの場合
					else {
						wolf = randomSelect(grayList);
					}

					/*DIV動作*/
					if(!divineRemark) {
						talkQueue.offer(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
						divineRemark=true;
					}
					//偽占い師が白だしし次第esVillager
					if(!AgentDataMap.get(Possessed).SeerWhiteList.isEmpty() && !esVillager) {
						Agent agent = randomSelect(AgentDataMap.get(Possessed).SeerWhiteList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.VILLAGER));
						esVillager=true;
					}
					voteAgent = wolf;
				}
				//偽占い師がCOしてこない,turn=0にCOしていない場合
				else {
					Agent wolf = judge.getTarget();

					/*DIV動作*/
					if(!divineRemark) {
						talkQueue.offer(TalkFactory.divinedResultRemark(wolf, Species.WEREWOLF));
						divineRemark=true;
					}
					//偽占い師が白だしし次第esVillager
					if(Possessed!=null && !AgentDataMap.get(Possessed).SeerWhiteList.isEmpty() && !esVillager) {
						Agent agent = randomSelect(AgentDataMap.get(Possessed).SeerWhiteList);
						talkQueue.offer(TalkFactory.estimateRemark(agent, Role.VILLAGER));
						esVillager=true;
					}
					voteAgent = wolf;
				}

			}
		}
		/*2日目*/
		else if(getDate() == 2){
			/*人狼を当てている場合*/
			if(!WerewolfList.isEmpty()) {
				//狂人が生きている場合
				if(Possessed!=null && isAlive(Possessed) && !WolfCO) {
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.WEREWOLF));
					WolfCO=true;
					voteAgent = Possessed;
				}
				//狂人がいない場合
				else if(!WolfCO && !PossessedCO){
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.POSSESSED));
					PossessedCO=true;
					if(!WerewolfList.isEmpty() && !esVillager) {
						Agent wolf = randomSelect(WerewolfList);
						talkQueue.offer(TalkFactory.estimateRemark(wolf, Role.VILLAGER));
						esVillager=true;
						for(Agent ag: getAliveOthersList()) {
							if(ag!=wolf) {
								voteAgent = ag;
							}
						}
					}
				}

			}
			/*まだ人狼を当てていない場合*/
			else {
				//狂人が生きている場合
				if(Possessed!=null && isAlive(Possessed) && !WolfCO) {
					//狂人以外のエージェントを人狼Listに
					for(Agent agent: getAliveOthersList()) {
						if(agent!=Possessed) {
							WerewolfList.add(agent);
						}
					}
					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.WEREWOLF));
					WolfCO=true;
					voteAgent = Possessed;
				}
				//狂人がいない場合
				else if(!WolfCO && !PossessedCO){

					//meにvoteしたことないエージェントをList化(人狼の可能性あり)
					List<Agent> list = new ArrayList<>();
					for(Agent agent: getAliveOthersList()) {
						if(!AgentDataMap.get(agent).voteList.contains(me)) {
							list.add(agent);
						}
					}

					Agent wolf = randomSelect(list);
					WerewolfList.add(wolf);

					talkQueue.offer(TalkFactory.comingoutRemark(me, Role.POSSESSED));
					PossessedCO=true;
					for(Agent ag: getAliveOthersList()) {
						if(ag!=wolf) {
							voteAgent = wolf;
						}
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
			if(getDate()==2) {
				if(!WerewolfList.isEmpty()) {
					voteAgent = randomSelect(WerewolfList);
				}
			}
			talkQueue.offer(TalkFactory.overRemark());
		}
	}

	@Override
	public void action15(Talk talk, Content content) {

		Judge judge = gameInfo.getDivineResult();

		if(judge == null) {
			return;
		}

		//最多VOTE数エージェント
		highestVotedAgent = getHighestVoteAgent();
		//最多VOTE数エージェントがgrayList外だった場合
		if(!grayList.contains(highestVotedAgent)) {
			highestVotedAgent = getHighestVoteAgent(highestVotedAgent);
		}

		/*1日目*/
		if(getDate() == 1) {
			if(!SeerCO) {
				talkQueue.offer(TalkFactory.comingoutRemark(gameInfo.getAgent(), Role.SEER));
				SeerCO=true;
			}
			//占い先が潜伏人狼だった場合
			else if(hidingWerewolf != null) {
				if(!divineRemark) {
					talkQueue.offer(TalkFactory.divinedResultRemark(hidingWerewolf, Species.WEREWOLF));
					divineRemark=true;
				}
				voteAgent = hidingWerewolf;
			}
			//占い先が白の村人だった場合
			else if(!SeerList.contains(judge.getTarget()) && !MediumList.contains(judge.getTarget())) {
				if(!divineRemark) {
					talkQueue.offer(TalkFactory.divinedResultRemark(judge.getTarget(), judge.getResult()));
					divineRemark=true;
				}
				voteAgent = highestVotedAgent;

			}
			//占い先がCOしてしまった場合
			else {
				if(!divineRemark) {
					Agent agent = RandomSelect.get(grayList);
					provisionalWhiteList.add(agent);
					grayList.remove(agent);
					talkQueue.offer(TalkFactory.divinedResultRemark(agent, Species.HUMAN));
					divineRemark=true;
				}
				voteAgent = highestVotedAgent;
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

		/*2日目*/
		else if(getDate() >= 2){
			if(!divineRemark) {
				talkQueue.offer(TalkFactory.divinedResultRemark(judge.getTarget(),judge.getResult()));
				divineRemark=true;
			}

			//霊媒師COが1人であれば本物
			if(turn >= 1) {
				if(MediumList.size()==1 && Medium==null) {
					for(Agent agent: MediumList) {
						Medium = agent;
						myConnectionList.add(Medium);
					}
				}
			}

			/*もし本物のMediumがいたら(狂人が確定していない場合)
			 *1.他の霊媒師をwolfListとbrackListに
			 *2.占い師COが3人の場合，その霊媒師とラインがある占い師は人狼
			 *3.余った占い師は狂人
			*/
			if(Medium!=null && !SeerMediumArranged) {
				for(Agent agent: MediumList) {
					if(agent!=Medium) {
						blackList.add(agent);
						WerewolfList.add(agent);
						if(SeerList.size()==3 && Possessed==null) {
							for(Agent ag: AgentDataMap.get(agent).connectionList) {
								WerewolfList.add(ag);
							}
							for(Agent ag: blackList) {
								if(!WerewolfList.contains(ag) && ag!=me) {
									Possessed = ag;
								}
							}
						}
					}
				}
				SeerMediumArranged = true;
			}

			/*もし自分以外の占い師とラインがあるMediumがいたら(狂人が確定していない場合)
			 *1.偽霊媒師をwolfListとbrackListに
			 *2.ラインがある占い師は人狼
			 *3.余った占い師は狂人
			*/
			else{
				for(Agent agent: MediumList) {
					if(AgentDataMap.get(agent).connectionList != null && Possessed==null && !SeerMediumArranged) {
						blackList.add(agent);
						WerewolfList.add(agent);
						if(SeerList.size()==3) {
							for(Agent ag: AgentDataMap.get(agent).connectionList) {
								WerewolfList.add(ag);
							}
							for(Agent ag: blackList) {
								if(!WerewolfList.contains(ag) && ag!=me) {
									Possessed = ag;
								}
							}
						}
					}
				}
				SeerMediumArranged = true;
			}

			//もし狂人がいたら狂人ES
			if(Possessed!=null && esPossessed==false) {
				talkQueue.offer(TalkFactory.estimateRemark(Possessed, Role.POSSESSED));
				esPossessed=true;
				for(Agent agent: AgentDataMap.get(Possessed).connectionList) {
					WerewolfList.add(agent);
				}

			}

			if(!WerewolfList.isEmpty() && !esWolf) {
				for(Agent agent: WerewolfList) {
					talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
					esWolf=true;
				}
			}

			/*voteAgent決定
			 *1.潜伏人狼がいればそいつをVOTE
			 *2.潜伏人狼が死んでいればWerewolfListからランダムVOTE
			 *3.潜伏人狼がいなければ多数派VOTE
			*/
			if(hidingWerewolf!=null) {
				voteAgent = hidingWerewolf;
			}
			else if(hidingWerewolfDied) {
				voteAgent=randomSelect(WerewolfList);
			}
			else {
				voteAgent=highestVotedAgent;
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

}
