package jp.gmail.kogecha05.player5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aiwolf.client.lib.Content;
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

public class Village5BasePlayer implements Player {
	final double INF = 11451419190721810893.0;
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

	// 会話に関する情報
	int talkListHead;
	Deque<Content> talkQueue = new LinkedList<>();
	Deque<Content> whisperQueue = new LinkedList<>();

	// 行動の記録
	List<Vote> voteList = new ArrayList<>();
	List<Talk> talkList = new ArrayList<>();
	List<Talk> whisperList = new ArrayList<>();
	List<Vote> voteListLog = new ArrayList<>();
	List<Talk> talkListLog = new ArrayList<>();
	List<Talk> whisperListLog = new ArrayList<>();

	// 行動の対象
	Agent voteCandidate;
	Agent declaredVoteCandidate;
	Agent attackVoteCandidate;
	Agent declaredAttackVoteCandidate;

	// 追加情報
	int numFirstCo;
	int numAliveCo;
	Map<Agent, Agent> voteTarget = new HashMap<>();
	Set<Agent> firstDayCo = new HashSet<>();
	Map<Agent, Boolean> voteWhite = new HashMap<>();
	Map<Agent, Boolean> voteBlack = new HashMap<>();

	static private int gameCount = 0;
	static protected RoleEstimator estimator = new RoleEstimator();

	protected static double pFakeCo = 0.5;
	int coef = 0;

	// 補助メソッド
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

		comingoutMap.clear();
		divinedWhite.clear();
		divinedBlack.clear();

		numFirstCo = 0;
		numAliveCo = 0;
		firstDayCo.clear();

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

		Collections.shuffle(aliveOthers);

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
				voteTarget.put(talker, content.getTarget());
				break;
			case COMINGOUT:
				// 初回ターン以降のCOは狼だと判断する
//				// 2日目の占いCOは聞き流す
//				// ログを見る限り占い即COの前提が崩れてるので一旦ボツ
//				if (talk.getTurn() > 0 && content.getRole() == Role.SEER || day == 2)
//					break;
				comingoutMap.put(talker, content.getRole());
				break;
			case DIVINED:
				// 占いCOしていないエージェントの占い結果は無視
				if (comingoutMap.get(talker) != Role.SEER)
					break;
				divinationList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
				break;
			case OPERATOR:
				break;
			default:
				break;
			}
		}
		talkListHead = talkList.size();

		// 占い結果を処理
		divinedBlack.clear();
		divinedWhite.clear();
		for (Judge j : divinationList) {
			if (!isAlive(j.getTarget())) continue;

			if (j.getResult() == Species.WEREWOLF && j.getTarget() != me) {
				divinedBlack.add(j.getTarget());
			}

			if (j.getResult() == Species.HUMAN) {
				divinedWhite.add(j.getTarget());
			}
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

		// 初日のCO状況
		if (day == 1) {
			numFirstCo = 0;
			for (Agent agent : currentGameInfo.getAgentList()) {
				if (comingoutMap.get(agent) == Role.SEER) numFirstCo++;
			}
		}

		// 生き残りCO数
		numAliveCo = 0;
		for (Agent agent : currentGameInfo.getAliveAgentList()) {
			if (comingoutMap.get(agent) == Role.SEER) numAliveCo++;
		}

		// 1日目のCO状況
		if (day <= 1) {
			for (Agent agent : currentGameInfo.getAgentList()) {
				if (comingoutMap.get(agent) == Role.SEER)
					firstDayCo.add(agent);
			}
		}

		// 役職推定
		estimator.update(gameInfo);

		Functions.debugPrintln("update elapsed: " + String.valueOf(updateTimer.getElapsedTime()) + "[ms]");
	}

	public void dayStart() {
		day = currentGameInfo.getDay();

		// 前日の記録を保存
		voteListLog.addAll(voteList);
		talkListLog.addAll(talkList);
		whisperListLog.addAll(whisperList);

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

		// 生存者を更新
		addExecutedAgent(currentGameInfo.getExecutedAgent());
		if (!currentGameInfo.getLastDeadAgentList().isEmpty()) {
			addkilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
		}
		Collections.shuffle(aliveOthers);

		voteTarget.clear();
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
		// 時間計測
		Functions.debugPrintln("Elapsed Time: " + String.valueOf(timer.getElapsedTime()) + "[ms]");
		Functions.debugPrintln("**********************************************************");

		// 狼の騙り率を勝敗から調整する
		if (myRole == Role.WEREWOLF) {
			int win = isAlive(me) ? 0 : -1;
			pFakeCo = Math.max(0.0, Math.min(pFakeCo + coef * win * 0.25, 1.0));
		}
		Functions.debugPrintln("pFakeCo" + String.valueOf(pFakeCo));
	}

	public String talk() {
		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

	public String whisper() {
		return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
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
