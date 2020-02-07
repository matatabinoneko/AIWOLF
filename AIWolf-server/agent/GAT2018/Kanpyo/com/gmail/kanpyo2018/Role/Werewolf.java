package com.gmail.kanpyo2018.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import com.gmail.kanpyo2018.base.BaseRole;
import com.gmail.kanpyo2018.data.GameResult;
import com.gmail.kanpyo2018.talk.TalkFactory;
import com.gmail.kanpyo2018.util.RandomSelect;

public class Werewolf extends BaseRole {
	public Werewolf(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void action(Talk talk, Content content) {
		List<Talk> whisperList = gameInfo.getWhisperList();
		for (int i = whisperHead; i < whisperList.size(); i++) {
			Talk whisper = whisperList.get(i);
			if (whisper.getAgent() == gameInfo.getAgent()) {
				continue;
			}
			Content whisperContent = new Content(whisper.getText());
			if (whisperContent.getTopic() == Topic.COMINGOUT) {
				if (whisperContent.getRole() == Role.MEDIUM) {
					mediunCO = false;
					whisperQueue.offer(TalkFactory.comingoutRemark(me, Role.VILLAGER));
				}
			}
			if (whisperContent.getTopic() == Topic.ATTACK) {
				attackAgent = whisperContent.getTarget();
			}
		}
		whisperHead = whisperList.size();
		if (mediunCO) {
			Mediumaction(talk, content);
		} else {
			if (content.getTopic() == Topic.COMINGOUT) {
				if (content.getRole() == Role.WEREWOLF && gameInfo.getAliveAgentList().size() != 3) {
					if (!blackList.contains(talk.getAgent())) {
						whiteList.remove(talk.getAgent());
						grayList.remove(talk.getAgent());
						blackList.add(talk.getAgent());
					}
					talkQueue.push(TalkFactory.requestAllVoteRemark(talk.getAgent()));
					talkQueue.push(TalkFactory.voteRemark(talk.getAgent()));
					talkQueue.push(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
					voteTarget = talk.getAgent();
				} else if (content.getRole() == Role.POSSESSED && gameInfo.getAliveAgentList().size() != 3) {
					if (!blackList.contains(talk.getAgent())) {
						whiteList.remove(talk.getAgent());
						grayList.remove(talk.getAgent());
						blackList.add(talk.getAgent());
					}
					talkQueue.offer(TalkFactory.estimateRemark(talk.getAgent(), Role.POSSESSED));
					if (voteTarget == null) {
						talkQueue.offer(TalkFactory.voteRemark(talk.getAgent()));
						talkQueue.offer(TalkFactory.requestAllVoteRemark(talk.getAgent()));
						voteTarget = talk.getAgent();
					}
				}
			}
		}
	}

	public void Mediumaction(Talk talk, Content content) {
		if (!isCo) {
			if (!myLieIdentifiedQueue.isEmpty()) {
				powerTalkQueue.offer(myLieIdentifiedQueue.pop());
			}
		}
		if (content.getTopic() == Topic.COMINGOUT) {
			if (content.getRole() == Role.MEDIUM) {
				coMap.put(talk.getAgent(), Role.WEREWOLF);
				if (isCo) {
					isCo = false;
					powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
				}
				powerTalkQueue.offer(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
				wereWolfAgent = talk.getAgent();
			}
		}
	}

	@Override
	public void lieMediumAction() {
		if (gameInfo.getDay() != 1) {
			Agent target = gameInfo.getExecutedAgent();
			Species result = null;
			int ram = RandomSelect.getNum(100);
			if (ram > 50) {
				result = Species.HUMAN;
			} else {
				result = Species.WEREWOLF;
				if (isCo) {
					isCo = false;
					powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
				}
			}
			if (!isCo) {
				powerTalkQueue.offer(TalkFactory.identRemark(target, result));
			} else {
				myLieIdentifiedQueue.offer(TalkFactory.identRemark(target, result));
			}
		}
	}

	/**
	 * リストからランダムに選んで返す
	 *
	 * @param list
	 * @return ランダムで選んだ値
	 */
	<T> T randomSelect(List<T> list) {

		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	/**
	 * COしていない場合CO
	 *
	 * @param role
	 * @return String
	 */
	public String sayComingout(Role role) {
		if (!isCo) {
			isCo = true;
			return TalkFactory.comingoutRemark(me, role);
		} else {
			return null;
		}
	}

	/**
	 * COした人以外を対象にするときに使う． COしていない人が複数いた場合ランダムで返す．
	 *
	 * @param AgentCO
	 * @return Agent
	 */
	public Agent getNotCO(Agent AgentCO) {
		List<Agent> AliveList = new ArrayList<>();
		AliveList = gameInfo.getAliveAgentList();
		AliveList.remove(AgentCO);
		AliveList.remove(me);
		return randomSelect(AliveList);
	}

	/**
	 * 自分を抜いた生きているエージェントのListを返す
	 *
	 * @param list
	 * @return 自分を抜いた生きているエージェントのList
	 */
	<T> List<Agent> aliveListRemoveMe() {
		List<Agent> AliveList = new ArrayList<>();
		AliveList = gameInfo.getAliveAgentList();
		AliveList.remove(me);
		return AliveList;
	}

	@Override
	public void fiveinitializeAction(GameInfo arg0) {
		grayList = new ArrayList<>(arg0.getAgentList());
		grayList.remove(me);
		whiteList.clear();
		blackList.clear();
		wolfList.clear();
		possessedList.clear();
		attackTarget.clear();
		PossessedPoint.clear();
		oneAction = true;
		talkQueue.clear();
		skipCount = 0;
		posCount = 0;
		SeerList.clear();
		possessed = null;
		turn = 0;
		WolfCO = false;
		SeerTalk = false;
		for (Agent Pos : grayList) {
			PossessedPoint.put(Pos, 0);
		}
	}

	@Override
	public void fivedaystaerAction() {
		voteTarget = null;
		talkHead = 0;
		skipCount = 0;
		oneAction = true;
		voteCount = 0;
		talkQueue.clear();
		turn = 0;
		SeerList = SeerList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
	}

	@Override
	public Agent fiveattackAction() {
		grayList = grayList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
		possessedList = possessedList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
		SeerList = SeerList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
		//		if (possessedList.isEmpty()) {
		//			if (!SeerList.isEmpty()) {
		//				return randomSelect(SeerList);
		//			}
		//		} else if (possessedList.size() == 1) {
		//			SeerList.remove(possessedList.get(0));
		//			if (!SeerList.isEmpty()) {
		//				return randomSelect(SeerList);
		//			}
		//		}
		if (SeerList.size() == 1) {
			return SeerList.get(0);
		}
		return randomSelect(grayList);
	}

	@Override
	public String fivetalkAction() {
		turn++;
		if (gameInfo.getDay() == 1 && turn == 1) {
			return TalkFactory.comingoutRemark(me, Role.VILLAGER);
		}
		if (gameInfo.getDay() == 2 && turn == 1 && !SeerList.isEmpty()) {
			voteTarget = randomSelect(aliveListRemoveMe());
			talkQueue.offer(TalkFactory.voteRemark(voteTarget));
			return TalkFactory.comingoutRemark(me, Role.WEREWOLF);
		}
		if (gameInfo.getDay() == 2 && turn == 1 && SeerList.isEmpty()) {
			voteTarget = randomSelect(aliveListRemoveMe());
			talkQueue.offer(TalkFactory.voteRemark(voteTarget));
		}
		if (gameInfo.getDay() == 1 && turn == 2) {
			return TalkFactory.voteRemark(randomSelect(grayList));
		}
		if (turn == 3 && voteTarget == null && gameInfo.getDay() == 1) {
			voteTarget = randomSelect(grayList);
			return TalkFactory.voteRemark(voteTarget);
		}
		if (!talkQueue.isEmpty()) {
			return talkQueue.pop();
		}
		return TalkFactory.overRemark();
	}

	@Override
	public void fiveupdateAction() {
		Agent target = null;
		Species result;
		for (int i = talkHead; i < gameInfo.getTalkList().size(); i++) {
			// 内容
			Talk talk = gameInfo.getTalkList().get(i);
			// 発言者
			Agent talker = talk.getAgent();
			if (talker == me) {
				// 自分の発言はスキップ
				continue;
			}
			// 発話内容をcontent型へ
			Content content = new Content(talk.getText());
			// 会話内容を取り込むように修正
			switch (content.getTopic()) {
			case COMINGOUT:
				// カミングアウト
				if (turn == 1) {
					if (content.getRole() == Role.SEER) {
						if (!SeerList.contains(talker)) {
							whiteList.add(talker);
							grayList.remove(talker);
							SeerList.add(talker);
						}
					}
				}
				break;
			case DIVINED:
				// 占い結果
				if (SeerList.size() != 3) {
					Boolean SeerTalk = false;
					Boolean seerdivi = false;
					for (Agent seer : SeerList) {
						if (seer == talker) {
							SeerTalk = true;
						}
					}
					if (SeerTalk) {
						target = content.getTarget();
						result = content.getResult();
						if (target != me && result == Species.WEREWOLF) {
							possessedList.add(talker);
							for (Agent seer : SeerList) {
								if (seer == target) {
									seerdivi = true;
								}
							}
							if (seerdivi) {
							} else {
								if (voteTarget == null) {
									if (!grayList.contains(target)) {
										grayList.add(target);
										blackList.remove(target);
									}
									if (voteTarget == null) {
										voteTarget = target;
										talkQueue.offer(TalkFactory.voteRemark(target));
									}
								}
							}
						} else if (target == me && result == Species.WEREWOLF && voteTarget == null) {
							whiteList.remove(talker);
							grayList.remove(talker);
							blackList.add(talker);
							voteTarget = talker;
							talkQueue.offer(TalkFactory.estimateRemark(talker, Role.POSSESSED));
							talkQueue.offer(TalkFactory.voteRemark(talker));
						} else if (target == me && result == Species.HUMAN) {
							possessedList.add(talker);
						}
					}
				}
				break;
			case IDENTIFIED:
				// 霊媒
				break;
			case VOTE:
				if (SeerList.size() == 3) {
					Boolean SeerTalk = false;
					for (Agent seer : SeerList) {
						if (seer == talker) {
							SeerTalk = true;
						}
					}
					if (!SeerTalk) {
						target = content.getTarget();
						if (target != me) {
							talkQueue.offer(TalkFactory.voteRemark(target));
							voteTarget = target;
						}
					}
				}
			default:
				break;
			}
		}
		talkHead = gameInfo.getTalkList().size();
	}

	@Override
	public Agent fivevoteAction() {
		voteCount++;
		if (voteCount == 2 && gameInfo.getDay() == 2) {
			return getNotCO(voteTarget);
		}
		if (voteTarget != null) {
			return voteTarget;
		}
		return null;
	}

}
