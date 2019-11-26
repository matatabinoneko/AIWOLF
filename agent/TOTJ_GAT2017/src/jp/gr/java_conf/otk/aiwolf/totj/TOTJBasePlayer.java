package jp.gr.java_conf.otk.aiwolf.totj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/** すべての役職のベースとなるクラス */
public class TOTJBasePlayer implements Player {

	Agent agent0 = Agent.getAgent(0);

	/** このエージェント */
	Agent me;
	/** 日付 */
	int day;
	/** talk()できるか時間帯か */
	boolean canTalk;
	/** whisper()できるか時間帯か */
	boolean canWhisper;
	/** 最新のゲーム情報 */
	GameInfo currentGameInfo;
	/** 自分以外のエージェント */
	List<Agent> others;
	/** 自分以外の生存エージェント */
	List<Agent> aliveOthers;
	/** 追放されたエージェント */
	List<Agent> executedAgents = new ArrayList<>();
	/** 殺されたエージェント */
	List<Agent> killedAgents = new ArrayList<>();
	/** 発言された占い結果報告のリスト */
	List<Judge> divinationList = new ArrayList<>();
	/** 発言された霊媒結果報告のリスト */
	List<Judge> identList = new ArrayList<>();
	/** 発言用待ち行列 */
	Deque<Content> talkQueue = new LinkedList<>();
	/** 囁き用待ち行列 */
	Deque<Content> whisperQueue = new LinkedList<>();
	/** 投票先候補 */
	Agent voteCandidate;
	/** 宣言済み投票先候補 */
	Agent declaredVoteCandidate;
	/** 襲撃投票先候補 */
	Agent attackVoteCandidate;
	/** 宣言済み襲撃投票先候補 */
	Agent declaredAttackVoteCandidate;
	/** カミングアウト状況 */
	Map<Agent, Role> comingoutMap = new HashMap<>();
	/** GameInfo.talkList読み込みのヘッド */
	int talkListHead;
	/** 1ゲームでの各エージェントの行動を記録するためのMap */
	Map<Agent, List<Action>> actionMap = new HashMap<>();
	/** エージェントの確率モデルを保持するMap */
	Map<Agent, PModel> modelMap;
	/** 現在までの行動より計算した，エージェントが役職である対数確率 */
	Map<Agent, Map<Role, Double>> probMap = new HashMap<>();
	// 標準モデルの学習か否か
	boolean isLearning = false;

	PModel baseModel;

	@Override
	public String getName() {
		return "MyBasePlayer";
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		day = -1;
		me = gameInfo.getAgent();
		others = new ArrayList<>(gameInfo.getAgentList());
		others.remove(me);
		aliveOthers = new ArrayList<>(others);
		executedAgents.clear();
		killedAgents.clear();
		divinationList.clear();
		identList.clear();
		comingoutMap.clear();
		actionMap.clear();
		probMap.clear();
		// 学習モード
		if (isLearning) {
			actionMap.put(agent0, new ArrayList<Action>());
		}
		// エージェントモデル
		for (Agent a : aliveOthers) {
			actionMap.put(a, new ArrayList<Action>());
			if (!modelMap.containsKey(a)) {
				modelMap.put(a, new PModel(gameSetting));
			}
			probMap.put(a, new HashMap<Role, Double>());
			double p = -Math.log10((double) gameInfo.getExistingRoles().size());
			for (Role role : gameInfo.getExistingRoles()) {
				probMap.get(a).put(role, p);
			}
		}
	}

	@Override
	public void dayStart() {
		canTalk = true;
		canWhisper = currentGameInfo.getRole() == Role.WEREWOLF ? true : false;
		talkQueue.clear();
		whisperQueue.clear();
		declaredVoteCandidate = null;
		voteCandidate = null;
		declaredAttackVoteCandidate = null;
		attackVoteCandidate = null;
		talkListHead = 0;
		// 前日に追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getExecutedAgent());
		// 昨夜死亡した（襲撃された）エージェントを登録
		if (!currentGameInfo.getLastDeadAgentList().isEmpty()) {
			addKilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
		}
		// 投票行動を登録
		for (Vote vote : currentGameInfo.getVoteList()) {
			if (vote.getAgent() != me) {
				Action action = Action.VOTE_VILLAGER;
				Agent target = vote.getTarget();
				if (isCo(target)) {
					if (comingoutMap.get(target) == Role.SEER) {
						action = Action.VOTE_SEER;
					} else if (comingoutMap.get(target) == Role.MEDIUM) {
						action = Action.VOTE_MEDIUM;
					}
				}
				actionMap.get(vote.getAgent()).add(action);
				if (isLearning) {
					actionMap.get(agent0).add(action);
				}
			}
		}
		// 確率マップ更新
		updateProb();
	}

	private void updateProb() {
		for (Agent a : others) {
			for (Role role : currentGameInfo.getExistingRoles()) {
				if (actionMap.get(a).size() > 0) {
					probMap.get(a).put(role, calcUniProb(a, role, actionMap.get(a), 1.0));
				}
			}
		}
	}

	// 役職のエージェントが指定した行動リストをとる確率（ユニグラムモデル）
	double calcUniProb(Agent agent, Role role, List<Action> actionList, double weight) {
		// double prob = aPriori.get(role); // 事前確率
		double prob = 0.0; // 確率1
		Map<Action, Double> probMap = modelMap.get(agent).model.get(role);
		Map<Action, Double> baseMap = baseModel.model.get(role);
		for (Action action : actionList) {
			if (baseMap != null) { // TODO This may be bug.
				prob += (1 - weight) * probMap.get(action) + weight * baseMap.get(action);
			} else {
				prob += (1 - weight) * probMap.get(action);
			}
		}
		return prob / (double) actionList.size();
	}

	private void addExecutedAgent(Agent executedAgent) {
		if (executedAgent != null) {
			aliveOthers.remove(executedAgent);
			if (!executedAgents.contains(executedAgent)) {
				executedAgents.add(executedAgent);
			}
		}
	}

	private void addKilledAgent(Agent killedAgent) {
		if (killedAgent != null) {
			aliveOthers.remove(killedAgent);
			if (!killedAgents.contains(killedAgent)) {
				killedAgents.add(killedAgent);
			}
		}
	}

	@Override
	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
		// 1日の最初の呼び出しはdayStart()の前なので何もしない
		if (currentGameInfo.getDay() == day + 1) {
			day = currentGameInfo.getDay();
			return;
		}
		// 2回目の呼び出し以降
		// （夜限定）追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getLatestExecutedAgent());
		// GameInfo.talkListからカミングアウト・占い報告・霊媒報告を抽出
		for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
			Talk talk = currentGameInfo.getTalkList().get(i);
			Agent talker = talk.getAgent();
			if (talker == me) {
				continue;
			}
			Content content = new Content(talk.getText());

			switch (content.getTopic()) {
			case COMINGOUT:
				comingoutMap.put(talker, content.getRole());
				// CO行動を登録
				Action action = null;
				Role role = content.getRole();
				if (role == Role.SEER) {
					action = Action.CO_SEER;
				} else if (role == Role.MEDIUM) {
					action = Action.CO_MEDIUM;
				}
				if (action != null) {
					actionMap.get(talker).add(action);
					if (isLearning) {
						actionMap.get(agent0).add(action);
					}
				}
				break;
			case DIVINED:
				divinationList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
				break;
			case IDENTIFIED:
				identList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
				break;
			default:
				break;
			}
		}
		talkListHead = currentGameInfo.getTalkList().size();
		// 確率マップ更新
		updateProb();
	}

	@Override
	public String talk() {
		chooseVoteCandidate();
		if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
			Content content4vote = new Content(new VoteContentBuilder(voteCandidate));
			talkQueue.offer(content4vote);
			talkQueue.offer(new Content(new RequestContentBuilder(null, content4vote)));
			declaredVoteCandidate = voteCandidate;
		}
		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

	@Override
	public String whisper() {
		chooseAttackVoteCandidate();
		if (attackVoteCandidate != null && attackVoteCandidate != declaredAttackVoteCandidate) {
			Content content4attack = new Content(new AttackContentBuilder(attackVoteCandidate));
			whisperQueue.offer(content4attack);
			whisperQueue.offer(new Content(new RequestContentBuilder(null, content4attack)));
			declaredAttackVoteCandidate = attackVoteCandidate;
		}
		return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
	}

	@Override
	public Agent vote() {
		canTalk = false;
		chooseVoteCandidate();
		return voteCandidate;
	}

	@Override
	public Agent attack() {
		canWhisper = false;
		chooseAttackVoteCandidate();
		canWhisper = true;
		return attackVoteCandidate;
	}

	@Override
	public Agent divine() {
		return null;
	}

	@Override
	public Agent guard() {
		return null;
	}

	@Override
	public void finish() {
		// update baseModel
		for (Agent a : others) {
			Role role = currentGameInfo.getRoleMap().get(a);
			modelMap.get(a).update(role, actionMap.get(a));
			if (isLearning) {
				modelMap.get(agent0).update(role, actionMap.get(a));
			}
		}
	}

	/** 投票先候補を選びvoteCandidateにセットする */
	protected void chooseVoteCandidate() {
	}

	/** 襲撃先候補を選びattackVoteCandidateにセットする */
	protected void chooseAttackVoteCandidate() {
	}

	/** エージェントが生きているかどうかを返す */
	protected boolean isAlive(Agent agent) {
		return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	/** エージェントが殺されたかどうかを返す */
	protected boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	/** エージェントがカミングアウトしたかどうかを返す */
	protected boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}

	/** 役職がカミングアウトされたかどうかを返す */
	protected boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	/** リストからランダムに選んで返す */
	protected <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	/**
	 * リストから役職である確率の最も高いエージェントを返す
	 * 
	 * @param candidates
	 * @return
	 */
	Agent probSelect(List<Agent> candidates, Role role) {
		if (candidates.isEmpty()) {
			return null;
		}
		Collections.sort(candidates, new ProbComparator(role));
		return candidates.get(0);
	}

	/**
	 * 確率の大きい順に並べる
	 */
	class ProbComparator implements Comparator<Agent> {

		Role role;

		public ProbComparator(Role role) {
			this.role = role;
		}

		@Override
		public int compare(Agent arg0, Agent arg1) {
			if (probMap.get(arg0).get(role) + Math.random() * 0.00001 < probMap.get(arg1).get(role)) {
				// if (probMap.get(arg0).get(role) < probMap.get(arg1).get(role)) {
				return 1;
			} else {
				return -1;
			}
		}
	}

}