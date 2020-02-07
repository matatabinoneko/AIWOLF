package com.gmail.kanpyo2018.Role;

import java.util.ArrayList;
import java.util.List;

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

public class Villager extends BaseRole {

	public Villager(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void action(Talk talk, Content content) {
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
		AliveList.remove(me);
		AliveList.remove(AgentCO);
		return randomSelect(AliveList);
	}

	@Override
	public void fiveinitializeAction(GameInfo arg0) {
		grayList = new ArrayList<>(arg0.getAgentList());
		grayList.remove(me);
		whiteList.clear();
		blackList.clear();
		wolfList.clear();
		talkQueue.clear();
		skipCount = 0;
		SeerList.clear();
		possessed = null;
		wereWolf = null;
		turn = 0;
	}

	@Override
	public void fivedaystaerAction() {
		voteTarget = null;
		talkHead = 0;
		skipCount = 0;
		oneAction = true;
		talkQueue.clear();
	}

	@Override
	public String fivetalkAction() {
		turn++;
		if (oneAction) {
			// 1日目は村人COをする
			if (gameInfo.getDay() == 1) {
				oneAction = false;
				return TalkFactory.comingoutRemark(me, Role.VILLAGER);
			}
			// 2日目は狂人COをする
			else {
				oneAction = false;
				return TalkFactory.comingoutRemark(me, Role.POSSESSED);
			}
		} else {
			// 先にCOした方は狂人者,後にした方は占い師だろう発言．
			if (SeerList.size() == 3 && turn == 2 && gameInfo.getDay() == 1) {
				talkQueue.offer(TalkFactory.voteRemark(randomSelect(SeerList)));
			}
			if (gameInfo.getDay() == 1 && turn == 2) {
				return TalkFactory.voteRemark(randomSelect(grayList));
			}
			if (!talkQueue.isEmpty()) {
				skipCount = 0;
				return talkQueue.poll();
			} else {
				if (gameInfo.getDay() == 2 && skipCount == 2 && voteTarget == null) {
					talkQueue.offer(TalkFactory.overRemark());
					List<Agent> AliveList = new ArrayList<>();
					AliveList = gameInfo.getAliveAgentList();
					AliveList.remove(me);
					voteTarget = randomSelect(AliveList);
					return TalkFactory.voteRemark(voteTarget);
				} else {
					skipCount++;
					return TalkFactory.skipRemark();
				}
			}
		}
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
				if (turn == 1) {
					if (content.getRole() == Role.SEER) {
						if (!SeerList.contains(talker)) {
							whiteList.add(talker);
							grayList.remove(talker);
							SeerList.add(talker);
						}
					}
				}
				if (content.getRole() == Role.POSSESSED) {
					if (gameInfo.getDay() == 2 && voteTarget == null) {
						WolfCO = true;
						voteTarget = getNotCO(talker);
						talkQueue.offer(sayComingout(Role.WEREWOLF));// nullが入る可能性あり
						talkQueue.offer(TalkFactory.voteRemark(getNotCO(talker)));
					}
				}
				if (content.getRole() == Role.WEREWOLF) {
					if (gameInfo.getDay() == 2 && voteTarget == null && !WolfCO) {
						voteTarget = talker;
						talkQueue.offer(TalkFactory.voteRemark(getNotCO(talker)));
					} else {
						voteTarget = talker;
						talkQueue.offer(TalkFactory.voteRemark(talker));
					}
				}
				break;
			case DIVINED:
				// 占い結果
				if (SeerList.size() != 3) {
					Boolean SeerTalk = false;
					for (Agent seer : SeerList) {
						if (seer == talker) {
							SeerTalk = true;
						}
					}
					if (SeerTalk) {
						Agent target = content.getTarget();
						Species result = content.getResult();
						if (target != me && result == Species.WEREWOLF) {
							if (SeerList.contains(target)) {
							} else {
								whiteList.remove(target);
								grayList.remove(target);
								blackList.add(target);
								voteTarget = target;
								talkQueue.offer(TalkFactory.voteRemark(target));
							}
						} else if (target == me && result == Species.WEREWOLF && voteTarget == null) {
							whiteList.remove(talker);
							grayList.remove(talker);
							blackList.add(talker);
							voteTarget = talker;
							talkQueue.offer(TalkFactory.estimateRemark(talker, Role.POSSESSED));
							talkQueue.offer(TalkFactory.voteRemark(talker));
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
						Agent target = content.getTarget();
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
		if (!wolfList.isEmpty()) {
			return wolfList.get(0);
		}
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
