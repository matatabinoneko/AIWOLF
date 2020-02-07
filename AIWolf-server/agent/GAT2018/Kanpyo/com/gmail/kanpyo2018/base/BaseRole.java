package com.gmail.kanpyo2018.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.kanpyo2018.data.GameResult;
import com.gmail.kanpyo2018.talk.TalkFactory;
import com.gmail.kanpyo2018.util.Debug;
import com.gmail.kanpyo2018.util.RandomSelect;

public class BaseRole implements Player {
	// ゲーム情報
	protected GameInfo gameInfo;

	protected boolean DayFinish = false;

	protected boolean isPassFinish;

	protected boolean fiveActions;

	protected int turn, wisperturn;

	protected boolean seerGuard;

	protected int divCount;

	protected int posCount;

	protected List<Agent> aliveList;

	protected Agent sameVoteAgent;

	protected int test = 0;

	protected Agent wereWolfAgent;

	protected boolean oneAction;

	protected boolean PossessedCO = false;

	protected boolean WolfCO = false;

	protected Agent attackAgent;

	protected boolean isAliveMedium;

	protected Agent divineTarget = null;

	protected boolean isCo;

	protected boolean killmedium;

	protected boolean mediunCO;

	protected List<Agent> deadSeerWhiteList;

	protected Map<Agent, Integer> voteCountMap;

	protected int talkHead, whisperHead;

	protected GameResult gameResult;

	protected boolean killSeer;

	protected boolean noChange;

	protected int tacticsNum;

	protected Deque<String> talkQueue = new LinkedList<>();

	protected Deque<String> powerTalkQueue = new LinkedList<>();

	protected Deque<Judge> myDivinationQueue = new LinkedList<>();

	protected Deque<Judge> myIdentifiedQueue = new LinkedList<>();

	protected Deque<String> myLieIdentifiedQueue = new LinkedList<>();

	protected Deque<String> whisperQueue = new LinkedList<>();

	protected Map<Agent, Role> coMap;

	protected String co;

	protected List<Agent> whiteList = new ArrayList<>();

	protected List<Agent> blackList = new ArrayList<>();

	protected List<Agent> grayList = new ArrayList<>();

	protected List<Agent> possessedList = new ArrayList<>();

	protected int seerCount;

	protected Map<Agent, Boolean> isSeerDivinedMap;

	protected Map<Agent, Boolean> isMediumIdenMap;

	protected Map<Agent, Map<Agent, Species>> mediumIdenMap;

	protected Map<Agent, List<Agent>> voteMap;

	protected Agent voteTarget;

	protected Boolean SeerTalk = false;

	protected List<Agent> attackTarget = new ArrayList<>();

	protected Map<Agent, Integer> PossessedPoint = new TreeMap<>();

	protected Agent guardTaeget;

	protected Agent me;

	protected Agent attackedAgent;

	protected int wolfCount;

	protected int mediumCount, voteCount;

	protected int possessedCount;

	protected int gameCount = 0;

	protected Map<Agent, Integer> seerDivination = new HashMap<>();

	protected List<Agent> SeerList = new ArrayList<>();

	protected Agent possessed = null;

	protected Agent wereWolf = null;

	protected boolean isSeerEst;

	protected boolean foolCO = false;

	protected boolean seerCO = false;

	protected List<Agent> wolfList = new ArrayList<>();

	protected Map<Agent, Map<Agent, Species>> SeerDivinedMap;

	protected int skipCount;

	boolean isVoteMax;

	public BaseRole(GameResult gameResult) {
		this.gameResult = gameResult;
	}

	@Override
	public void initialize(GameInfo arg0, GameSetting arg1) {
		try {
			gameInfo = arg0;
			isPassFinish = false;
			gameCount++;
			List<Agent> list = arg0.getAgentList();
			if (list.size() == 5) {
				fiveActions = true;
				me = arg0.getAgent();
				this.gameResult.init(arg0);
				fiveinitializeAction(arg0);
			} else {
				fiveActions = false;
				attackAgent = null;
				noChange = false;
				attackedAgent = null;
				co = null;
				wereWolfAgent = null;
				isAliveMedium = false;
				me = arg0.getAgent();
				grayList = new ArrayList<>(arg0.getAgentList());
				grayList.remove(me);
				aliveList = new ArrayList<>();
				whiteList = new ArrayList<>();
				blackList = new ArrayList<>();
				this.gameInfo = arg0;
				voteMap = new HashMap<>();
				coMap = new HashMap<>();
				voteTarget = null;
				guardTaeget = null;
				seerGuard = false;
				deadSeerWhiteList = new ArrayList<>();
				talkHead = 0;
				whisperHead = 0;
				isPassFinish = false;
				powerTalkQueue.clear();
				talkQueue.clear();
				seerCount = 0;
				turn = 0;
				tacticsNum = 40;
				wisperturn = 0;
				skipCount = 0;
				wolfCount = 3;
				mediumCount = 0;
				divCount = 0;
				isSeerEst = true;
				oneAction = true;
				mediunCO = false;
				SeerDivinedMap = new HashMap<>();
				mediumIdenMap = new HashMap<>();
				isSeerDivinedMap = new HashMap<>();
				isMediumIdenMap = new HashMap<>();
				voteCountMap = new HashMap<>();
				myDivinationQueue.clear();
				myLieIdentifiedQueue.clear();
				whisperQueue.clear();
				isVoteMax = true;
				isCo = true;
				killmedium = false;
				wolfList = new ArrayList<>();
				Map<Agent, Role> map = arg0.getRoleMap();
				for (Agent agent : map.keySet()) {
					if (map.get(agent) == Role.WEREWOLF) {
						wolfList.add(agent);
					}
				}
				this.gameResult.init(arg0);
			}
		} catch (Exception e) {
			this.writeEx(e);
		}
	}

	/**
	 * エージェントが生きているかどうか
	 *
	 * @param agent
	 * @return 生存していたらtrue
	 */
	protected boolean isAlive(Agent agent) {
		return gameInfo.getAliveAgentList().contains(agent);
	}

	@Override
	public void dayStart() {
		try {
			if (fiveActions) {
				fivedaystaerAction();
			} else {
				grayList = grayList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
				whiteList = whiteList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
				blackList = blackList.stream().filter(agent -> isAlive(agent)).collect(Collectors.toList());
				List<Agent> list = gameInfo.getAliveAgentList();
				for (Agent agent : list) {
					aliveList.remove(agent);
				}
				aliveList.remove(gameInfo.getExecutedAgent());
				if (aliveList.isEmpty()) {
					attackedAgent = null;
				} else {
					attackedAgent = aliveList.get(0);
				}
				aliveList = gameInfo.getAliveAgentList();
				whisperQueue.clear();
				skipCount = 0;
				whisperHead = 0;
				turn = 0;
				wisperturn = 0;
				talkHead = 0;
				talkQueue.clear();
				powerTalkQueue.clear();
				voteMap.clear();
				attackAgent = null;
				isAliveMedium = true;
				isVoteMax = true;
				Judge divination = gameInfo.getDivineResult();
				lieSeerAction();
				if (mediunCO) {
					lieMediumAction();
				}
				if (!isAlive(voteTarget)) {
					voteTarget = null;
				}
				if (divination != null) {// nullは大切
					myDivinationQueue.offer(divination);
					Agent target = divination.getTarget();
					Species result = divination.getResult();
					grayList.remove(target);
					if (result == Species.HUMAN) {
						whiteList.add(target);
					} else {
						blackList.add(target);
					}
					if (gameInfo.getDay() == 1) {
						if (isCo) {
							isCo = false;
							powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.SEER));
							Judge div = myDivinationQueue.pop();
							if (div.getResult() == Species.WEREWOLF) {
								powerTalkQueue
										.offer(TalkFactory.divinedResultRemark(div.getTarget(), Species.WEREWOLF));
								powerTalkQueue.offer(TalkFactory.voteRemark(div.getTarget()));
								powerTalkQueue.offer(TalkFactory.requestAllVoteRemark(div.getTarget()));
								voteTarget = div.getTarget();
							} else {
								Agent lietarget = RandomSelect.get(grayList);
								grayList.remove(lietarget);
								blackList.add(lietarget);
								powerTalkQueue.offer(TalkFactory.divinedResultRemark(lietarget, Species.WEREWOLF));
								powerTalkQueue.offer(TalkFactory.voteRemark(lietarget));
								powerTalkQueue.offer(TalkFactory.requestAllVoteRemark(lietarget));
								voteTarget = lietarget;
							}
						}
					}
					if (gameInfo.getDay() > 1) {
						powerTalkQueue.offer(TalkFactory.divinedResultRemark(target, result));
						if (result == Species.WEREWOLF) {
							powerTalkQueue.offer(TalkFactory.voteRemark(target));
							powerTalkQueue.offer(TalkFactory.requestAllVoteRemark(target));
							voteTarget = target;
						}
					}
				}
				Judge identified = gameInfo.getMediumResult();
				if (identified != null) {
					if (gameInfo.getDay() == 3 && isCo) {
						isCo = false;
						powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
					}
					if (!isCo) {
						Agent target = identified.getTarget();
						Species result = identified.getResult();
						powerTalkQueue.offer(TalkFactory.identRemark(target, result));
					} else {
						myIdentifiedQueue.offer(identified);
					}
					if (identified.getResult() == Species.WEREWOLF) {
						if (isCo) {
							isCo = false;
							powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
						}
					}
				}
				if (!isAlive(voteTarget)) {
					voteTarget = null;
				}
				if (coMap.get(attackedAgent) == Role.SEER) {
					Map<Agent, Species> div = SeerDivinedMap.get(attackedAgent);
					for (Agent agent : div.keySet()) {
						if (div.get(agent) == Species.WEREWOLF) {
							if (!blackList.contains(agent)) {
								blackList.add(agent);
								whiteList.remove(agent);
								grayList.remove(agent);
							}
							if (voteTarget == null) {
								talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
								talkQueue.offer(TalkFactory.voteRemark(agent));
								talkQueue.offer(TalkFactory.requestAllVoteRemark(agent));
								voteTarget = agent;
							}
						} else {
							if (!blackList.contains(agent)) {
								if (!whiteList.contains(agent)) {
									blackList.remove(agent);
									whiteList.add(agent);
									grayList.remove(agent);
								}
							}
						}
					}
				}
				List<Agent> removeList = new ArrayList<>();
				for (Agent agent : isSeerDivinedMap.keySet()) {
					if (isAlive(agent)) {
						isSeerDivinedMap.put(agent, false);
					} else {
						if (deadSeerWhiteList.isEmpty()) {
							guardTaeget = null;
							Map<Agent, Species> div = SeerDivinedMap.get(agent);
							for (Agent agent2 : div.keySet()) {
								if (div.get(agent2) == Species.HUMAN) {
									deadSeerWhiteList.add(agent2);
								}
							}
						}
						removeList.add(agent);
					}
				}
				for (Agent agent : removeList) {
					SeerDivinedMap.remove(agent);
					isSeerDivinedMap.remove(agent);
				}
				removeList = new ArrayList<>();
				for (Agent agent : isMediumIdenMap.keySet()) {
					if (isAlive(agent)) {
						isMediumIdenMap.put(agent, false);
					} else {
						removeList.add(agent);
						if (mediumCount > 1) {
							killmedium = true;
						}
					}
				}
				for (Agent agent : removeList) {
					mediumIdenMap.remove(agent);
					isMediumIdenMap.remove(agent);
				}
				removeList = new ArrayList<>();
				for (Agent agent : coMap.keySet()) {
					if (isAlive(agent)) {
						continue;
					} else {
						removeList.add(agent);
					}
				}
				for (Agent agent : removeList) {
					coMap.remove(agent);
				}
				for (Agent agent : gameInfo.getAliveAgentList()) {
					voteMap.put(agent, null);
				}
				for (Agent agent : gameInfo.getAliveAgentList()) {
					voteCountMap.put(agent, 0);
				}
				for (Agent agent : coMap.keySet()) {
					if (coMap.get(agent) == Role.MEDIUM) {
						isAliveMedium = true;
					}
				}
				if (killmedium) {
					boolean one = true;
					removeList = new ArrayList<>();
					for (Agent agent : coMap.keySet()) {
						if (coMap.get(agent) != Role.MEDIUM) {
							continue;
						} else {
							removeList.add(agent);
							if (!blackList.contains(agent)) {
								blackList.add(agent);
								grayList.remove(agent);
								whiteList.remove(agent);
							}
							if (one) {
								talkQueue.offer(TalkFactory.estimateRemark(agent, Role.WEREWOLF));
								talkQueue.offer(TalkFactory.voteRemark(agent));
								talkQueue.offer(TalkFactory.requestAllVoteRemark(agent));
								voteTarget = agent;
								one = false;
							}
						}
					}
					for (Agent agent : removeList) {
						coMap.remove(agent);
					}
					if (!isAlive(wereWolfAgent)) {
						wereWolfAgent = null;
					}
				}
			}
		} catch (Exception e) {
			this.writeEx(e);
		}

	}

	@Override
	public Agent divine() {
		try {
			if (fiveActions) {
				return fivedivineAction();
			} else {
				return RandomSelect.get(grayList);
			}
		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}

	@Override
	public void finish() {
		try {
			if (!isPassFinish) {
				isPassFinish = true;
				return;
			}
			gameResult.updateDate(gameInfo);

		} catch (Exception e) {
			this.writeEx(e);
		}
		Debug.showError();
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "Kanpyo";
	}

	@Override
	public Agent guard() {
		try {
			List<Agent> seerList = new ArrayList<>();
			for (Agent agent : coMap.keySet()) {
				if (coMap.get(agent) == Role.SEER) {
					if (isAlive(agent)) {
						seerList.add(agent);
					}
				}
			}
			if (seerGuard) {
				if (attackedAgent != null) {
					guardTaeget = null;
					seerGuard = false;
					seerList.remove(guardTaeget);
					if (!seerList.isEmpty()) {
						seerGuard = true;
						guardTaeget = seerList.get(0);
					}
				}
			}
			if (attackedAgent != null) {
				guardTaeget = null;
			}
			if (guardTaeget != null) {
				return guardTaeget;
			}
			if (!deadSeerWhiteList.isEmpty()) {
				return RandomSelect.get(deadSeerWhiteList);
			}
			if (!seerList.isEmpty()) {
				guardTaeget = RandomSelect.get(seerList);
				return guardTaeget;
			}
			return RandomSelect.get(grayList);
		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}

	@Override
	public String talk() {
		try {
			if (fiveActions) {
				return fivetalkAction();
			} else {
				turn++;
				if (isCo) {
					if (co != null) {
						isCo = false;
						return co;
					}
				}
				if (!powerTalkQueue.isEmpty()) {
					return powerTalkQueue.pop();
				}
				if (gameInfo.getDay() == 1 && turn == 1) {
					talkQueue.offer(TalkFactory.voteRemark(RandomSelect.get(grayList)));
					oneAction = false;
				}
				if (turn == 3 && isSeerEst) {
					if (seerCount == 1) {
						for (Agent agent : coMap.keySet()) {
							if (coMap.get(agent) == Role.SEER) {
								isSeerEst = false;
								return TalkFactory.estimateRemark(agent, Role.SEER);
							}
						}
					}
				}
				if (turn == 4) {
					List<Agent> removeAgent = new ArrayList<>();
					for (Agent agent : isSeerDivinedMap.keySet()) {
						if (!isSeerDivinedMap.get(agent)) {
							talkQueue.offer(TalkFactory.estimateRemark(agent, Role.POSSESSED));
							coMap.put(agent, Role.POSSESSED);
							removeAgent.add(agent);
							if (whiteList.contains(agent)) {
								whiteList.remove(agent);
								blackList.add(agent);
							}
							if (voteTarget == null) {
								talkQueue.offer(TalkFactory.voteRemark(agent));
								talkQueue.offer(TalkFactory.requestAllVoteRemark(agent));
								voteTarget = agent;
							}
						}
					}
					for (Agent agent : removeAgent) {
						isSeerDivinedMap.remove(agent);
						SeerDivinedMap.remove(agent);
					}
					removeAgent = new ArrayList<>();
					if (gameInfo.getDay() != 1) {
						for (Agent agent : isMediumIdenMap.keySet()) {
							if (!isMediumIdenMap.get(agent)) {
								talkQueue.offer(TalkFactory.estimateRemark(agent, Role.POSSESSED));
								coMap.put(agent, Role.POSSESSED);
								if (whiteList.contains(agent)) {
									whiteList.remove(agent);
									blackList.add(agent);
								}
								if (voteTarget == null) {
									talkQueue.offer(TalkFactory.voteRemark(agent));
									talkQueue.offer(TalkFactory.requestAllVoteRemark(agent));
									voteTarget = agent;
								}
							}
						}
						for (Agent agent : removeAgent) {
							isMediumIdenMap.remove(agent);
							mediumIdenMap.remove(agent);
						}
					}
				}
				if (turn > 5 && talkQueue.isEmpty() && gameInfo.getDay() < 5) {
					int max = 0;
					Agent voteAgent = null;
					for (Agent agent : gameInfo.getAliveAgentList()) {
						if (voteMap.get(agent) == null) {
							continue;
						}
						List<Agent> list = voteMap.get(agent);
						int size = list.size() - 1;
						Agent vote = list.get(size);
						voteCountMap.put(vote, (voteCountMap.get(vote) + 1));
					}
					for (Agent agent : gameInfo.getAliveAgentList()) {
						int i = voteCountMap.get(agent);
						if (max < i) {
							max = i;
							voteAgent = agent;
						}
					}
					if (voteAgent == me) {
					} else {
						if (isVoteMax) {
							isVoteMax = false;
							talkQueue.offer(TalkFactory.voteRemark(voteAgent));
							voteTarget = voteAgent;
						}
					}

				}
				if (!talkQueue.isEmpty()) {
					skipCount = 0;
					return talkQueue.pop();
				}
				if (skipCount == 2) {
					return TalkFactory.overRemark();
				} else {
					skipCount++;
					return TalkFactory.skipRemark();
				}
			}
		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
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
							if (gameInfo.getDay() < 4 && turn < 3) {
								if (coMap.get(talker) != Role.SEER) {
									isSeerDivinedMap.put(talk.getAgent(), false);
									talkQueue.offer(TalkFactory.estimateRemark(talker, Role.SEER));
									seerCount++;
									coMap.put(talk.getAgent(), content.getRole());
									if (!whiteList.contains(talk.getAgent())) {
										whiteList.add(talk.getAgent());
										grayList.remove(talk.getAgent());
										blackList.remove(talk.getAgent());
									}
								}
							} else {
								if (!blackList.contains(talk.getAgent())) {
									whiteList.remove(talk.getAgent());
									grayList.remove(talk.getAgent());
									blackList.add(talk.getAgent());
									if (seerCount == 2) {
										talkQueue.offer(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
									} else {
										talkQueue.offer(TalkFactory.estimateRemark(talk.getAgent(), Role.POSSESSED));
									}
								}
							}
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
					if (content.getTopic() == Topic.DIVINED) {
						Agent target = content.getTarget();
						if (coMap.get(talk.getAgent()) == Role.SEER) {
							isSeerDivinedMap.put(talk.getAgent(), true);
							Map<Agent, Species> div = new HashMap<>();
							div.put(target, content.getResult());
							SeerDivinedMap.put(talk.getAgent(), div);
							if (seerCount == 1) {
								if (target != me) {
									if (content.getResult() == Species.WEREWOLF) {
										if (coMap.get(content.getTarget()) == Role.SEER) {
											talkQueue.offer(TalkFactory.estimateRemark(talker, Role.WEREWOLF));
											talkQueue.offer(TalkFactory.voteRemark(talker));
											talkQueue.offer(TalkFactory.requestAllVoteRemark(talker));
											voteTarget = talker;
										}
										if (!blackList.contains(target)) {
											whiteList.remove(target);
											grayList.remove(target);
											blackList.add(target);
										}
										talkQueue.offer(TalkFactory.estimateRemark(target, Role.WEREWOLF));
										talkQueue.offer(TalkFactory.voteRemark(target));
										talkQueue.offer(TalkFactory.requestAllVoteRemark(target));
										voteTarget = target;
									} else {
										if (!whiteList.contains(target)) {
											whiteList.add(target);
											grayList.remove(target);
											blackList.remove(target);
										}
										talkQueue.offer(TalkFactory.estimateRemark(target, Role.VILLAGER));
									}
								} else {
									if (content.getResult() == Species.WEREWOLF) {
										isSeerEst = false;
										coMap.put(talk.getAgent(), Role.POSSESSED);
										isSeerDivinedMap.remove(talker);
										Map<Agent, Species> divs = SeerDivinedMap.get(talk.getAgent());
										if (!divs.isEmpty()) {
											for (Agent agent : divs.keySet()) {
												if (whiteList.contains(agent)) {
													whiteList.remove(agent);
													grayList.add(agent);
												}
												if (blackList.contains(agent)) {
													blackList.remove(agent);
													grayList.add(agent);
												}
											}
										}
										SeerDivinedMap.remove(talker);
										talkQueue.push(TalkFactory.requestOver(talk.getAgent()));
										talkQueue.push(TalkFactory.requestAllVoteRemark(talk.getAgent()));
										talkQueue.push(TalkFactory.voteRemark(talk.getAgent()));
										talkQueue.push(TalkFactory.estimateRemark(talk.getAgent(), Role.POSSESSED));
										voteTarget = talk.getAgent();
									}
								}
							} else if (seerCount == 2) {
								if (target != me) {
									if (content.getResult() == Species.WEREWOLF) {
										if (isAliveMedium) {
											talkQueue.offer(TalkFactory.agreeRemark(talk));
											talkQueue.offer(TalkFactory.estimateRemark(target, Role.WEREWOLF));
											talkQueue.offer(TalkFactory.voteRemark(target));
											talkQueue.offer(TalkFactory.requestAllVoteRemark(target));
										} else {
											talkQueue.offer(TalkFactory.disagreeRemark(talk));
											for (Agent agent : coMap.keySet()) {
												if (agent == talker) {
													continue;
												}
												if (coMap.get(agent) == Role.SEER) {
													talkQueue.offer(TalkFactory.requestDivinationRemark(agent, target));
												}
											}
										}
									}
								} else {
									if (content.getResult() == Species.WEREWOLF) {
										coMap.put(talker, Role.POSSESSED);
										isSeerDivinedMap.remove(talker);
										SeerDivinedMap.remove(talker);
										if (!blackList.contains(talker)) {
											blackList.add(talker);
											whiteList.remove(talker);
											grayList.remove(talker);
										}
									} else {
										talkQueue.offer(TalkFactory.agreeRemark(talk));
									}
								}
							} else {

							}
						} else {
							talkQueue.offer(TalkFactory.requestOver(talker));
							if (!blackList.contains(talk.getAgent())) {
								whiteList.remove(talk.getAgent());
								grayList.remove(talk.getAgent());
								blackList.add(talk.getAgent());
								talkQueue.offer(TalkFactory.estimateRemark(talk.getAgent(), Role.POSSESSED));
								if (voteTarget == null) {
									talkQueue.offer(TalkFactory.voteRemark(target));
									talkQueue.offer(TalkFactory.requestAllVoteRemark(target));
									voteTarget = target;
								}
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

	public void action(Talk talk, Content content) {

	}

	@Override
	public Agent vote() {
		try {
			if (fiveActions) {
				return fivevoteAction();
			} else {
				if (wereWolfAgent != null) {
					return wereWolfAgent;
				}
				if (voteTarget != null) {
					return voteTarget;
				}
				if (!blackList.isEmpty()) {
					return RandomSelect.get(blackList);
				}
				if (!grayList.isEmpty()) {
					return RandomSelect.get(grayList);
				}
				return null;
			}
		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}

	@Override
	public String whisper() {
		try {
			wisperturn++;
			if (!whisperQueue.isEmpty()) {
				return whisperQueue.pop();
			}
			if (gameInfo.getDay() == 0 && wisperturn == 1) {
				mediunCO = true;
				return TalkFactory.comingoutRemark(me, Role.MEDIUM);
			}
			if (mediunCO) {
				if (isCo) {
					if (gameInfo.getDay() == 2) {
						co = TalkFactory.comingoutRemark(me, Role.MEDIUM);
					}
				}
			}
			return TalkFactory.overRemark();

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}

	@Override
	public Agent attack() {
		try {
			if (fiveActions) {
				return fiveattackAction();
			} else {
				if (attackAgent != null) {
					return attackAgent;
				}
				if (!whiteList.isEmpty()) {
					return RandomSelect.get(whiteList);
				}
				if (!grayList.isEmpty()) {
					return RandomSelect.get(grayList);
				}
			}
		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}

	public void writeEx(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		Debug.stackError(stringWriter);
	}

	public void lieMediumAction() {

	}

	public void lieSeerAction() {

	}

	public void fiveinitializeAction(GameInfo arg0) {

	}

	public void fivedaystaerAction() {

	}

	public Agent fivedivineAction() {
		return null;
	}

	public String fivetalkAction() {
		return null;
	}

	public void fiveupdateAction() {

	}

	public Agent fivevoteAction() {
		return null;
	}

	public Agent fiveattackAction() {
		return null;
	}
}
