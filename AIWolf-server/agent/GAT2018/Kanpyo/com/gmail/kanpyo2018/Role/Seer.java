package com.gmail.kanpyo2018.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import com.gmail.kanpyo2018.base.BaseRole;
import com.gmail.kanpyo2018.data.GameResult;
import com.gmail.kanpyo2018.talk.TalkFactory;

public class Seer extends BaseRole {
	public Seer(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void action(Talk talk, Content content) {
	}

	@Override
	public void update(GameInfo arg0) {
		try {
			if (fiveActions) {
				gameInfo = arg0;
				fiveupdateAction();
			} else {
				this.gameInfo = arg0;
				List<Talk> talkList = this.gameInfo.getTalkList();
				for (int i = talkHead; i < talkList.size(); i++) {
					Talk talk = talkList.get(i);
					if (talk.getAgent() == gameInfo.getAgent()) {
						continue;
					}
					Content content = new Content(talk.getText());
					Agent talker = talk.getAgent();
					if (content.getTopic() == Topic.COMINGOUT) {
						if (content.getRole() == Role.SEER) {
							coMap.put(talker, Role.POSSESSED);
							if (!blackList.contains(talker)) {
								whiteList.remove(talk.getAgent());
								grayList.remove(talk.getAgent());
								blackList.add(talk.getAgent());
							}
							talkQueue.offer(TalkFactory.requestOver(talker));
							talkQueue.offer(TalkFactory.estimateRemark(talker, Role.POSSESSED));
						} else {
							if (content.getRole() == Role.MEDIUM) {
								if (coMap.get(talker) != Role.MEDIUM) {
									isMediumIdenMap.put(talker, false);
									mediumCount++;
								}
							}
							coMap.put(talk.getAgent(), content.getRole());
							if (!whiteList.contains(talk.getAgent())) {
								whiteList.add(talk.getAgent());
								grayList.remove(talk.getAgent());
								blackList.remove(talk.getAgent());
							}
						}
					}
					if (content.getTopic() == Topic.IDENTIFIED) {
						Agent target = content.getTarget();
						if (coMap.get(talk.getAgent()) == Role.MEDIUM) {
							Map<Agent, Species> inden = new HashMap<>();
							inden.put(target, content.getResult());
							mediumIdenMap.put(talker, inden);
							isMediumIdenMap.put(talker, true);
							if (mediumCount == 1) {
								if (content.getResult() == Species.WEREWOLF) {
									wolfCount--;
								}
							}
						}
					}
					if (content.getTopic() == Topic.DIVINED) {
						talkQueue.offer(TalkFactory.requestOver(talker));
					}
					if (content.getTopic() == Topic.VOTE) {
						List<Agent> list = new LinkedList<>();
						if (voteMap.get(talker) != null) {
							list = voteMap.get(talk.getAgent());
						}
						list.add(content.getTarget());
						voteMap.put(talk.getAgent(), list);
					}
					this.action(talk, content);
				}
				talkHead = talkList.size();
			}
		} catch (Exception e) {
			this.writeEx(e);
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
	 * 送ったエージェント以外を対象にするときに使う． COしていない人が複数いた場合ランダムで返す．
	 *
	 * @param AgentCO
	 * @return Agent
	 */
	public Agent getNotAgent(Agent AgentCO) {
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

	public void divinationBlack(Agent agent) {
		voteTarget = agent;
		talkQueue.offer(TalkFactory.estimateRemark(voteTarget, Role.WEREWOLF));
		talkQueue.offer(TalkFactory.voteRemark(voteTarget));
		talkQueue.offer(TalkFactory.requestAllVoteRemark(voteTarget));
	}

	public void divinationWhite(Agent agent) {
		voteTarget = agent;
		talkQueue.offer(TalkFactory.voteRemark(voteTarget));
		talkQueue.offer(TalkFactory.requestAllVoteRemark(voteTarget));
	}

	public boolean getDay(int day) {
		if (gameInfo.getDay() == day && !DayFinish) {
			return true;
		} else {
			return false;
		}
	}

	public Agent getDay2DivinationWereWolf() {
		Judge divintion = myDivinationQueue.poll();
		if (divintion.getResult() == Species.WEREWOLF) {
			return divintion.getTarget();
		} else {
			return getNotAgent(divintion.getTarget());
		}
	}

	@Override
	public void fiveinitializeAction(GameInfo arg0) {
		me = arg0.getAgent();// 自分のAgent情報
		grayList = new ArrayList<>(arg0.getAgentList());
		grayList.remove(me); // 自分を除外
		whiteList.clear();
		blackList.clear();
		wolfList.clear();
		SeerList.clear();
		myDivinationQueue.clear();
		talkQueue.clear();
		isCo = false;
		possessed = null;
		PossessedCO = false;
		WolfCO = false;
		DayFinish = false;
	}

	@Override
	public void fivedaystaerAction() {
		Judge divination = gameInfo.getDivineResult();
		if (divination != null) {// nullは大切
			myDivinationQueue.offer(divination);
			Agent target = divination.getTarget();
			Species result = divination.getResult();
			grayList.remove(target);
			if (result == Species.HUMAN) {
				whiteList.add(target);
			} else {
				wolfList.add(target);
			}
		}
		divineTarget = null;
		voteTarget = null;
		talkHead = 0;
		DayFinish = false;
	}

	@Override
	public Agent fivedivineAction() {
		if (divineTarget != null) {
			return divineTarget;
		} else {
			List<Agent> candidates = new ArrayList<>();
			// 生きている灰色のプレイヤーを候補者リストに加える
			for (Agent agent : grayList) {
				if (isAlive(agent)) {
					candidates.add(agent);
				}
			}
			return randomSelect(candidates);
		}
	}

	@Override
	public String fivetalkAction() {
		if (!isCo) {
			return sayComingout(Role.SEER);
		} else if (!myDivinationQueue.isEmpty() && getDay(1)) {
			if (SeerList.size() == 2) {
				// List<Agent> list = new ArrayList<>();
				// list = SeerList;
				PossessedCO = true;
				Judge divintion = myDivinationQueue.poll();
				if (divintion.getResult() == Species.HUMAN) {
					SeerList = SeerList.stream().filter(seer -> seer != divintion.getTarget())
							.collect(Collectors.toList());
					;
				}
				for (Agent agent : SeerList) {
					if (agent == divintion.getTarget()) {
						if (divintion.getResult() == Species.WEREWOLF) {
							voteTarget = agent;
						}
					}
				}
				if (voteTarget == null) {
					divinationBlack(randomSelect(SeerList));
				} else {
					divinationBlack(voteTarget);
				}
				return TalkFactory.divinedResultRemark(voteTarget, Species.WEREWOLF);
			} else if (!PossessedCO) {
				Judge divintion = myDivinationQueue.poll();
				if (divintion.getResult() == Species.WEREWOLF) {
					divinationBlack(divintion.getTarget());
				} else {
					divinationWhite(randomSelect(grayList));
				}
				return TalkFactory.divinedResultRemark(divintion.getTarget(), divintion.getResult());
			} else {
				myDivinationQueue.remove();
				divinationBlack(randomSelect(grayList));
				return TalkFactory.divinedResultRemark(voteTarget, Species.WEREWOLF);
			}
		} else if (getDay(2)) {
			if (WolfCO) {
				voteTarget = getDay2DivinationWereWolf();
				talkQueue.offer(TalkFactory.voteRemark(voteTarget));
				talkQueue.offer(TalkFactory.requestAllVoteRemark(voteTarget));
				DayFinish = true;
				return TalkFactory.comingoutRemark(me, Role.WEREWOLF);
			} else if (PossessedCO) {
				voteTarget = getDay2DivinationWereWolf();
				Agent NotTarget = getNotAgent(voteTarget);
				talkQueue.offer(TalkFactory.voteRemark(NotTarget));
				talkQueue.offer(TalkFactory.requestAllVoteRemark(NotTarget));
				DayFinish = true;
				return TalkFactory.comingoutRemark(me, Role.POSSESSED);
			} else {
				voteTarget = getDay2DivinationWereWolf();
				talkQueue.offer(TalkFactory.voteRemark(voteTarget));
				talkQueue.offer(TalkFactory.requestAllVoteRemark(voteTarget));
				DayFinish = true;
				return TalkFactory.divinedResultRemark(voteTarget, Species.WEREWOLF);
			}
		}
		if (!talkQueue.isEmpty()) {
			return talkQueue.pop();
		}
		return TalkFactory.overRemark();
	}

	@Override
	public void fiveupdateAction() {
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
				if (content.getRole() == Role.SEER) {
					SeerList.add(talker);
					if (!myDivinationQueue.isEmpty()) {
						Judge divintion = myDivinationQueue.getFirst();
						if (divintion.getTarget() == talker) {
							PossessedCO = true;
						}
					}
				}
				break;
			case DIVINED:
				// 占い結果
				break;
			case IDENTIFIED:
				// 霊媒
				break;
			default:
				break;
			}
		}
		talkHead = gameInfo.getTalkList().size();
	}

	@Override
	public Agent fivevoteAction() {
		if (voteTarget != null) {
			return voteTarget;
		}
		// 候補者リスト
		List<Agent> candidates = new ArrayList<>();
		// 人狼だと思う人を候補者リストへ加える
		for (Agent agent : blackList) {
			if (isAlive(agent)) {
				candidates.add(agent);
			}
		}
		// 黒リストが居ない場合は人狼かもしれないと思う人を候補者リストへ加える
		if (candidates.isEmpty()) {
			for (Agent agent : grayList) {
				if (isAlive(agent)) {
					candidates.add(agent);
				}
			}
		}
		if (candidates.isEmpty()) {
			for (Agent agent : whiteList) {
				if (isAlive(agent)) {
					candidates.add(agent);
				}
			}
		}
		// 候補者リストからランダムに投票先を選ぶ
		return randomSelect(candidates);
	}

}
