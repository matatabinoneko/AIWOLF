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
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import com.gmail.kanpyo2018.base.BaseRole;
import com.gmail.kanpyo2018.data.GameResult;
import com.gmail.kanpyo2018.talk.TalkFactory;
import com.gmail.kanpyo2018.util.RandomSelect;

public class Possessed extends BaseRole {

	public Possessed(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void action(Talk talk, Content content) {
	}

	@Override
	public void lieSeerAction() {
		if (gameInfo.getDay() == 1) {
			isCo = false;
			Agent black = RandomSelect.get(grayList);
			grayList.remove(black);
			powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
			powerTalkQueue.offer(TalkFactory.divinedResultRemark(black, Species.WEREWOLF));
			powerTalkQueue.offer(TalkFactory.voteRemark(black));
			powerTalkQueue.offer(TalkFactory.requestAllVoteRemark(black));
			voteTarget = black;
		} else {
			if (divCount % 2 == 1) {
				Agent white = RandomSelect.get(grayList);
				grayList.remove(white);
				powerTalkQueue.offer(TalkFactory.divinedResultRemark(white, Species.HUMAN));
				Agent vote = RandomSelect.get(grayList);
				powerTalkQueue.offer(TalkFactory.voteRemark(vote));
				voteTarget = vote;
				divCount++;
			} else {
				Agent black = RandomSelect.get(grayList);
				grayList.remove(black);
				powerTalkQueue.offer(TalkFactory.divinedResultRemark(black, Species.WEREWOLF));
				powerTalkQueue.offer(TalkFactory.voteRemark(black));
				powerTalkQueue.offer(TalkFactory.requestAllVoteRemark(black));
				voteTarget = black;
				divCount++;
			}
		}
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
		} catch (

		Exception e) {
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

	/**
	 * 入れられた数値の日でDayFinishがtrueじゃないときにtrueを返す
	 *
	 * @param day
	 * @return boolean
	 */
	public boolean isDay(int day) {
		if (gameInfo.getDay() == day && !DayFinish) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * COした人以外を対象にするときに使う． COしていない人が複数いた場合ランダムで返す．
	 *
	 * @param agent
	 * @return Agent
	 */
	public Agent getNotAgent(Agent agent) {
		List<Agent> AliveList = new ArrayList<>();
		AliveList = gameInfo.getAliveAgentList();
		AliveList.remove(me);
		AliveList.remove(agent);
		return randomSelect(AliveList);
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

	@Override
	public void fiveinitializeAction(GameInfo arg0) {
		me = arg0.getAgent();// 自分のAgent情報
		grayList = new ArrayList<>(arg0.getAgentList());
		grayList.remove(me);
		whiteList.clear();
		talkQueue.clear();
		seerCount = 0;
		sameVoteAgent = null;
		wereWolf = null;
		seerDivination.clear();
		SeerList.clear();
		possessed = null;
		turn = 0;
		killSeer = true;
		seerCO = true;
	}

	@Override
	public void fivedaystaerAction() {
		voteTarget = null;
		talkHead = 0;
		skipCount = 0;
		oneAction = true;
		DayFinish = false;
		talkQueue.clear();
		turn = 0;
		if (SeerList.size() == 2) {
			killSeer = false;
		}
		// Listを生きているエージェントだけに更新
		SeerList = SeerList.stream().filter(seer -> isAlive(seer)).collect(Collectors.toList());
	}

	@Override
	public String fivetalkAction() {
		turn++;
		if (!talkQueue.isEmpty()) {
			return talkQueue.pop();
		}
		// 占い師COして黒出し特攻する場合
		// 1日目のとき
		if (isDay(1)) {
			if (gameCount < 50) {
				if (oneAction) {
					oneAction = false;
					// 占い師COをする
					return TalkFactory.comingoutRemark(me, Role.SEER);
				} else {
					// getDayから出るためにtrue
					DayFinish = true;
					List<Agent> candidates = new ArrayList<>();
					// 占いCOしていない人からランダム投票する．
					for (Agent agent : grayList) {
						// 生きているかどうかの判定
						if (isAlive(agent)) {
							// 占いCOしていない場合
							if (!SeerList.contains(agent)) {
								candidates.add(agent);
							}
						}
					}
					voteTarget = randomSelect(candidates);
					talkQueue.offer(TalkFactory.voteRemark(voteTarget));
					talkQueue.offer(TalkFactory.requestAllVoteRemark(voteTarget));
					return TalkFactory.divinedResultRemark(voteTarget, Species.WEREWOLF);
				}
			} else {
				if (oneAction) {
					oneAction = false;
					// 占い師COをする
					return TalkFactory.comingoutRemark(me, Role.SEER);
				} else {
					// getDayから出るためにtrue
					DayFinish = true;
					List<Agent> candidates = new ArrayList<>();
					// 占いCOしていない人からランダム投票する．
					for (Agent agent : grayList) {
						// 生きているかどうかの判定
						if (isAlive(agent)) {
							// 占いCOしていない場合
							if (!SeerList.contains(agent)) {
								candidates.add(agent);
							}
						}
					}
					sameVoteAgent = randomSelect(candidates);
					return TalkFactory.divinedResultRemark(sameVoteAgent, Species.HUMAN);
				}
			}
			// 2日目のとき
		} else if (isDay(2)) {
			if (oneAction) {
				oneAction = false;
				// 人狼CO
				return TalkFactory.comingoutRemark(me, Role.WEREWOLF);
			} else {
				DayFinish = true;
				// 占い師が残ってたらそいつに投票
				if (!SeerList.isEmpty()) {
					if (killSeer) {
						voteTarget = SeerList.get(0);
					} else {
						Agent agent = SeerList.get(0);
						List<Agent> list = aliveListRemoveMe();
						list.remove(agent);
						voteTarget = randomSelect(list);
					}
					// 裏切り者COした人がいたらその人に投票
				} else if (possessed != null) {
					voteTarget = possessed;
					// ランダム投票
				} else {
					voteTarget = randomSelect(aliveListRemoveMe());
				}
				return TalkFactory.voteRemark(voteTarget);
			}
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
				if (content.getRole() == Role.SEER) {
					// 初めてCOしたエージェントだった場合
					if (!SeerList.contains(talker)) {
						SeerList.add(talker);
						grayList.remove(talker);
						seerDivination.put(talker, 0);
					}
				}
				if (content.getRole() == Role.WEREWOLF) {
					// 2日目でまだ人狼COした人がいない場合
					if (gameInfo.getDay() == 2 && wereWolf == null) {
						wereWolf = talker;
					}
				}
				if (content.getRole() == Role.POSSESSED) {
					// 2日目でまだ裏切り者COした人がいない場合
					if (gameInfo.getDay() == 2 && possessed == null) {
						possessed = talker;
					}
				}
				break;
			case DIVINED:
				// 占い結果
				target = content.getTarget();
				result = content.getResult();
				break;
			case IDENTIFIED:
				// 霊媒
				break;
			case VOTE:
				if (sameVoteAgent == talker) {
					voteTarget = content.getTarget();
					talkQueue.offer(TalkFactory.voteRemark(voteTarget));
				}
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
		for (Agent agent : SeerList) {
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
