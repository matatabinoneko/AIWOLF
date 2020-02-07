package jp.gmail.kogecha05.player15;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.estimator.RoleEstimator;
import jp.gmail.kogecha05.utils.Functions;
import jp.gmail.kogecha05.utils.Timer;

public class Village15BasePlayer implements Player {
	Timer timer = new Timer();

	GameSetting currentGameSetting;
	GameInfo currentGameInfo;

	boolean gameOver;
	int day;
	Agent me;
	Role myRole;

	// 生存に関する情報
	List<Agent> aliveOthers;
	List<Agent> executedAgents = new ArrayList<>();
	List<Agent> killedAgents = new ArrayList<>();

	// 役職に関する情報
	Map<Agent, Role> comingoutMap = new HashMap<>();
	List<Agent> divinedWhite = new ArrayList<>();
	List<Agent> divinedBlack = new ArrayList<>();

	List<Judge> divinationList = new ArrayList<>();
	List<Judge> identList = new ArrayList<>();
	Agent trueSeer;

	Set<Agent> humans = new HashSet<>();
	Set<Agent> werewolves = new HashSet<>();
	int maxNumWerewolves;

	// 会話に関する情報
	int talkListHead;
	Deque<Content> talkQueue = new LinkedList<>();
	Deque<Content> whisperQueue = new LinkedList<>();

	// 行動の記録
	List<Vote> voteList = new ArrayList<>();
	List<Talk> talkList = new ArrayList<>();
	List<Talk> whisperList = new ArrayList<>();
	Map<Integer, List<Vote>> voteListLog = new HashMap<>();
	Map<Integer, List<Talk>> talkListLog = new HashMap<>();

	// 行動の対象
	Agent voteCandidate;
	Agent declaredVoteCandidate;
	Agent attackVoteCandidate;
	Agent declaredAttackVoteCandidate;

	// 追加情報
	Map<Agent, Agent> suspicionTarget = new HashMap<>();
	Set<Agent> fakeSeer = new HashSet<>();
	Map<Agent, Boolean> voteWhite = new HashMap<>();
	Map<Agent, Boolean> voteBlack = new HashMap<>();
	List<Agent> dangerAgent = new ArrayList<>();

	static private int gameCount = 0;
	static protected RoleEstimator estimator = new RoleEstimator();

	protected static double pFakeCo = 0.2;
	int coef = 0;

	protected boolean isAlive(Agent agent) {
		return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	protected boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	protected boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}

	protected boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	private void addExecutedAgent(Agent executedAgent) {
		if (executedAgent != null) {
			aliveOthers.remove(executedAgent);
			if (!executedAgents.contains(executedAgent)) {
				executedAgents.add(executedAgent);
			}
		}
	}

	private void addkilledAgent(Agent killedAgent) {
		if (killedAgent != null) {
			aliveOthers.remove(killedAgent);
			if (!killedAgents.contains(killedAgent)) {
				killedAgents.add(killedAgent);
			}
		}
	}

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		timer.timerStart();
		gameCount++;
		gameOver = false;
		currentGameSetting = gameSetting;
		currentGameInfo = gameInfo;

		day = -1;
		me = gameInfo.getAgent();
		myRole = gameInfo.getRole();

		aliveOthers = new ArrayList<>(gameInfo.getAliveAgentList());
		aliveOthers.remove(me);
		executedAgents.clear();
		killedAgents.clear();

		divinationList.clear();
		identList.clear();
		trueSeer = null;
		humans.clear();
		werewolves.clear();
		maxNumWerewolves = gameSetting.getRoleNum(Role.WEREWOLF);

		voteListLog.clear();
		talkListLog.clear();

		comingoutMap.clear();
		divinedWhite.clear();
		divinedBlack.clear();

		fakeSeer.clear();

		voteWhite.clear();
		voteBlack.clear();
		for (Agent agent: currentGameInfo.getAgentList()) {
			voteWhite.put(agent, false);
			voteBlack.put(agent, false);
		}

		estimator.initialize(gameSetting, gameInfo);
	}

	public void update(GameInfo gameInfo) {
		Timer updateTimer = new Timer();
		updateTimer.timerStart();

		currentGameInfo = gameInfo;

		// 初日は何もしない
		if (currentGameInfo.getDay() == 0) {
				return;
		}

		addExecutedAgent(currentGameInfo.getLatestExecutedAgent());

		// 会話の処理
		talkList = currentGameInfo.getTalkList();
		for (int i = talkListHead; i < talkList.size(); i++) {
			Talk talk = talkList.get(i);
			Agent talker = talk.getAgent();
			Content content = new Content(talk.getText());
			Topic topic = content.getTopic();

			// 発言回数を記録
			estimator.putTalkLog(talk);

			switch (topic) {
			case AGREE:
				break;
			case DISAGREE:
				break;
			case VOTE:
				suspicionTarget.put(talker, content.getTarget());
				break;
			case ESTIMATE:
				if (content.getRole() == Role.WEREWOLF)
					suspicionTarget.put(talker, content.getTarget());
				break;
			case COMINGOUT:
				comingoutMap.put(talker, content.getRole());
				if (content.getRole() == Role.WEREWOLF) {
					werewolves.add(talker);
				}
				break;
			case GUARDED:
				break;
			case DIVINED:
				if (comingoutMap.get(talker) != Role.SEER)
					break;
				divinationList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
				break;
			case IDENTIFIED:
				if (comingoutMap.get(talker) != Role.MEDIUM)
					break;
				identList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
			case OPERATOR:
				if (content.getOperator() == Operator.REQUEST) {
					for (Content c : content.getContentList()) {
						if (c.getTopic() == Topic.VOTE) {
							suspicionTarget.put(talker, content.getTarget());
						}
					}
				}
			default:
				break;
			}
		}
		talkListHead = talkList.size();

		// 役職推定
		estimator.update(currentGameInfo);

		// 人狼確定者の確率を変更しておく
		for (Agent agent: werewolves) {
			estimator.setRoleProbability(agent, Role.WEREWOLF, 1.0);
		}

		// 占い結果を処理
		divinedBlack.clear();
		divinedWhite.clear();
		for (Judge j : divinationList) {
			if (fakeSeer.contains(j.getAgent())) continue;
			if (!isAlive(j.getTarget())) continue;

			if (j.getResult() == Species.WEREWOLF && !dangerAgent.contains(j.getAgent())) {
				divinedBlack.add(j.getTarget());
			}

			if (j.getResult() == Species.HUMAN && !dangerAgent.contains(j.getAgent())) {
				divinedWhite.add(j.getTarget());
			}
		}

		// 破綻者を検出する
		for (Judge j : divinationList) {
			// 黒を出した相手が噛まれた
			if (j.getResult() == Species.WEREWOLF && isKilled(j.getTarget())) {
				fakeSeer.add(j.getAgent());
			}
			// 人狼でないとき、自分を黒判定したら偽占い
			if (myRole != Role.WEREWOLF) {
				if (j.getResult() == Species.WEREWOLF && j.getTarget() == me) {
					fakeSeer.add(j.getAgent());
				}
			}
			// 人狼のとき、狼以外に黒を出したか、狼に白を出したら偽占い
			if (myRole == Role.WEREWOLF) {
				if ((j.getResult() == Species.WEREWOLF && humans.contains(j.getTarget()))
						|| (j.getResult() == Species.HUMAN && werewolves.contains(j.getTarget()))) {
					fakeSeer.add(j.getAgent());
				}
			}
		}

		// 破綻者の確率を変更しておく
		for (Agent agent: fakeSeer) {
			estimator.setRoleProbability(agent, Role.WEREWOLF, 0.5);
			estimator.setRoleProbability(agent, Role.POSSESSED, 0.5);
		}

		// 囁きの処理
		whisperList = currentGameInfo.getWhisperList();

		// 投票の処理
		voteList = currentGameInfo.getVoteList();
		for (Vote vote : voteList) {
			if (divinedWhite.contains(vote.getTarget())) {
				voteWhite.put(vote.getAgent(), true);
			}
			if (divinedBlack.contains(vote.getTarget())) {
				voteBlack.put(vote.getAgent(), true);
			}
		}

		Functions.debugPrintln("update elapsed: " + String.valueOf(updateTimer.getElapsedTime()) + "[ms]");
	}

	public void dayStart() {
		day = currentGameInfo.getDay();
		maxNumWerewolves = Math.min(3, (aliveOthers.size() + 1) / 2);

		// 前日の記録を保存
		voteListLog.put(day-1, voteList);
		talkListLog.put(day-1,talkList);

		// 初期化
		voteList.clear();
		talkList.clear();
		whisperList.clear();

		talkListHead = 0;
		talkQueue.clear();
		whisperQueue.clear();

		declaredVoteCandidate = null;
		voteCandidate = null;
		declaredAttackVoteCandidate = null;
		attackVoteCandidate = null;

		suspicionTarget.clear();

		// 生存者を更新
		addExecutedAgent(currentGameInfo.getExecutedAgent());
		if (!currentGameInfo.getLastDeadAgentList().isEmpty()) {
			addkilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
		}
		Collections.shuffle(aliveOthers);
	}

	public void finish() {
		if (gameOver) return;
		gameOver = true;
		estimator.updateActionLog();

		Functions.debugPrintln("****************** GameOver: DEBUG INFO ******************");
		Functions.debugPrintln("Game: " + String.valueOf(gameCount));
		Functions.debugPrintln("My Role" + String.valueOf(myRole));
		// DEBUG: 役職推定結果
		Functions.debugPrintln("--------------- Role Estimator ---------------");
		for (Agent agent: currentGameInfo.getAgentList()) {
			Role role = currentGameInfo.getRoleMap().get(agent);
			Functions.debugPrintln(String.valueOf(agent) + " "
					+ String.valueOf(isAlive(agent) ? "Alive" : "Dead") + " "
					+ String.valueOf(role));
			Functions.debugPrintln(estimator.getAgentResult(agent));
		}
		Functions.debugPrintln("----------------------------------------------");
		Functions.debugPrintln("");

		if (myRole == Role.WEREWOLF) {
			boolean isWin = false;
			for (Entry<Agent, Role> entry: currentGameInfo.getRoleMap().entrySet()) {
				if (entry.getValue() == Role.WEREWOLF && isAlive(entry.getKey())) {
					isWin = true;
				}
			}

			int winCoef = isWin ? 0 : -1;
			pFakeCo = Math.max(0.0, Math.min(pFakeCo + coef * winCoef * 0.20, 1.0));
		}
		Functions.debugPrintln("pFakeCo" + String.valueOf(pFakeCo));

		// 時間計測
		Functions.debugPrintln("Elapsed Time: " + String.valueOf(timer.getElapsedTime()) + "[ms]");
		Functions.debugPrintln("**********************************************************");
	}

	public String talk() {
		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

	public String whisper() {
		return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
	}

	protected void chooseVoteCandidate() {

	}

	public Agent vote() {
		return voteCandidate != null ? voteCandidate : Functions.randomSelect(aliveOthers);
	}

	protected void chooseAttackVoteCandidate() {

	}

	public Agent attack() {
		chooseAttackVoteCandidate();
		return attackVoteCandidate;
	}

	public Agent divine() {
		return null;
	}

	public Agent guard() {
		return null;
	}

	public String getName() {
		return "kasuka";
	}
}
