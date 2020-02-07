package jp.gmail.kogecha05.player15;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Const;
import jp.gmail.kogecha05.utils.Functions;

public class Village15Werewolf extends Village15Villager {
	int numWolves;
	Role fakeRole;
	boolean isCameout;
	boolean isWhisperCameout;
	int whisperTurn;
	Role requestedFakeRole;

	boolean gjGuarded;
	boolean findPossessed;
	List<Agent> possessedList = new ArrayList<>();
	List<Agent> villagers;
	Map<Agent, Role> fakeRoleMap = new HashMap<>();
	Map<Agent, Agent> atackTargetMap;
	boolean canPowerplay;
	int numAliveWerewolves;

	// 占い関連
	Deque<Judge> divinationQueue = new LinkedList<>();
	Map<Agent, Species> myDivinationMap = new HashMap<>();
	List<Agent> whiteList = new ArrayList<>();
	List<Agent> blackList = new ArrayList<>();
	List<Agent> grayList;

	// 霊能関連
	Deque<Judge> identQueue = new LinkedList<>();
	Map<Agent, Species> myIdentMap = new HashMap<>();
	boolean blackDivination = false;

	int talkTurn;
	int whisperListHead;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo,  gameSetting);

		// 初期化
		numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);
		werewolves = new HashSet<>(gameInfo.getRoleMap().keySet());
		humans = new HashSet<>();
		for (Agent a : aliveOthers) {
			if (!werewolves.contains(a)) {
				humans.add(a);
			}
		}
		villagers = new ArrayList<>();
		possessedList.clear();
		findPossessed = false;

		fakeRoleMap.clear();
		for (Agent agent : werewolves) {
			fakeRoleMap.put(agent, Role.VILLAGER);
		}
		atackTargetMap = new HashMap<>();
		requestedFakeRole = null;
		canPowerplay = false;
		numAliveWerewolves = 3;

		isCameout = false;
		isWhisperCameout = false;

		divinationQueue.clear();
		myDivinationMap.clear();
		whiteList.clear();
		blackList.clear();
		grayList = new ArrayList<>();

		identQueue.clear();
		myIdentMap.clear();

		// 騙る役職を決定する
		if (Math.random() < pFakeCo) {
			fakeRole = Role.SEER;
		} else {
			fakeRole = Role.VILLAGER;
		}
	}

	public void update(GameInfo gameInfo) {
		super.update(gameInfo);

		// 自分以外の占い結果は除外
		for (Judge judge : divinationList) {
			if (judge.getAgent() != me) {
				divinedBlack.remove(judge.getTarget());
				divinedWhite.remove(judge.getTarget());
			}
		}

		villagers.clear();
		for (Agent agent : aliveOthers) {
			if (!werewolves.contains(agent) && !fakeSeer.contains(agent)) {
				villagers.add(agent);
			}
		}

		possessedList.clear();
		for (Agent agent : fakeSeer) {
			if (!werewolves.contains(agent) && isAlive(agent)) {
				possessedList.add(agent);
			}
		}
	}

	private Judge getFakeDivination() {
		List<Agent> candidates = new ArrayList<>();
		for (Agent agent : aliveOthers) {
			if (myDivinationMap.containsKey(agent))
				continue;

			candidates.add(agent);
		}
		if (candidates.isEmpty())
			Functions.randomSelect(aliveOthers);

		Agent target = null;
		// 狼陣営ではないっぽいエージェントを占う
		double maxProbability = 0.0;
		for (Agent agent : candidates) {
			if (werewolves.contains(agent)) continue;
			double p = 1.0 - estimator.getRoleProbability(agent, Role.POSSESSED)
					 - estimator.getRoleProbability(agent, Role.WEREWOLF);
			if (p > maxProbability) {
				maxProbability = p;
				target = agent;
			}
		}
		if (target == null)
			Functions.randomSelect(aliveOthers);

		// 結果をでっちあげる
		Species result = Species.HUMAN;
		int nFakeWolves = 0;
		for (Species s : myDivinationMap.values()) {
			if (s == Species.WEREWOLF) {
				nFakeWolves++;
			}
		}
		double pDivinateWolf = numAliveWerewolves / (double)(aliveOthers.size() - 1);
		if (!werewolves.contains(target) && nFakeWolves < (numWolves - 1) && Math.random() < pDivinateWolf) {
			result = Species.WEREWOLF;
		}

		return new Judge(day, me, target, result);
	}

	private Judge getFakeIdent() {
		double pIdentWolf = numAliveWerewolves / (double)aliveOthers.size();

		Agent target = currentGameInfo.getLatestExecutedAgent();
		Species result = Math.random() < pIdentWolf ? Species.WEREWOLF : Species.HUMAN;
		return target == null ? null : new Judge(day, me, target, result);
	}

	public void dayStart() {
		super.dayStart();

		// 仲間が吊られた
		Agent lastExecutedTarget = currentGameInfo.getLatestExecutedAgent();
		if (lastExecutedTarget != null) {
			if (werewolves.contains(lastExecutedTarget)) {
				numAliveWerewolves -= 1;
			}
		}

		// 護衛をされたか判定
		List<Agent> lastAttackTarget = currentGameInfo.getLastDeadAgentList();
		gjGuarded = (lastAttackTarget.isEmpty()) && (day >= 2);

		// 偽占いを実行
		if (day > 0 && fakeRole == Role.SEER) {
			Judge divination = getFakeDivination();
			if (divination != null) {
				divinationQueue.offer(divination);
				grayList.remove(divination.getTarget());
				if (divination.getResult() == Species.HUMAN) {
					divinedWhite.add(divination.getTarget());
				} else {
					divinedBlack.add(divination.getTarget());
				}
				myDivinationMap.put(divination.getTarget(), divination.getResult());
			}
		}

		// 偽霊能を実行
		if (fakeRole == Role.MEDIUM) {
			Judge ident = getFakeIdent();
			if (ident != null) {
				identQueue.offer(ident);
				myIdentMap.put(ident.getTarget(), ident.getResult());
			}

			// 黒判定をくらったエージェントが吊られた
			blackDivination = false;
			for (Judge j : divinationList) {
				if (j.getDay() != (day - 1)) continue;
				if (divinedBlack.contains(currentGameInfo.getLatestExecutedAgent()))
					blackDivination = true;
			}
		}

		// パワープレイが可能か判定
		if (!possessedList.isEmpty() && (numAliveWerewolves + 1) > villagers.size()) {
			canPowerplay = true;
		}

		whisperTurn = -1;
		whisperListHead = 0;
		atackTargetMap.clear();
	}

	protected void chooseVoteCandidate() {
		List<Agent> candidates = new ArrayList<>(aliveOthers);

		// 黒を出されたエージェントを優先的に吊る
		List<Agent> blackCandidates = new ArrayList<>();
		for (Agent agent : candidates) {
			if (divinedBlack.contains(agent))
				blackCandidates.add(agent);
		}

		// 序盤は白は避ける
		for (Agent agent: aliveOthers) {
			if (day <= 3 && divinedWhite.contains(agent))
				candidates.remove(agent);
		}

		// 候補が残らなかったら全員から
		if (candidates.isEmpty())
			candidates = new ArrayList<>(villagers);

		// 投票先を設定
		if (!blackCandidates.isEmpty()) {
			double maxProbability = -Const.INF;
			for (Agent agent : blackCandidates) {
				double p = 1.0 - estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p > maxProbability) {
					maxProbability = p;
					voteCandidate = agent;
				}
			}
			if (voteCandidate == null)
				voteCandidate = Functions.randomSelect(blackCandidates);
		} else {
			// 裏切り者ではないっぽいエージェントに投票する
			double maxProbability = -Const.INF;
			for (Agent agent : candidates) {
				if (werewolves.contains(agent)) continue;
				double p = 1.0 - estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p > maxProbability) {
					maxProbability = p;
					voteCandidate = agent;
				}
			}
			if (voteCandidate == null)
				voteCandidate = Functions.randomSelect(candidates);
		}

		// 投票先を宣言
		if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
			talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
			declaredVoteCandidate = voteCandidate;
		}
	}

	public String talk() {
		if (fakeRole == Role.SEER) {
			// 初日にCO
			if (!isCameout) {
				talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
				isCameout = true;
			}

			if (isCameout) {
				while (!divinationQueue.isEmpty()) {
					Judge ident = divinationQueue.poll();
					if (ident.getTarget() == null || ident.getResult() == null)
						break;
					talkQueue.offer(new Content(
							new DivinedResultContentBuilder(ident.getTarget(), ident.getResult())));
				}
			}

			if (talkQueue.isEmpty())
				chooseVoteCandidate();
		}

		if (fakeRole == Role.MEDIUM) {
			if (!isCameout &&
					((!identQueue.isEmpty() && identQueue.peekLast().getResult() == Species.WEREWOLF)
					|| isCo(Role.MEDIUM)
					|| blackDivination
					|| divinedBlack.contains(me))) {
				talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.MEDIUM)));
				isCameout = true;
			}

			if (isCameout) {
				while (!identQueue.isEmpty()) {
					Judge ident = identQueue.poll();
					talkQueue.offer(new Content(
							new IdentContentBuilder(ident.getTarget(), ident.getResult())));
				}
			}
		}

		if (fakeRole == Role.VILLAGER || fakeRole == Role.MEDIUM) {
			// 自分の推理を発言
			if (!isEstimateTalk) {
				// 狼っぽいエージェントを挙げるふり
				Map<Agent, Double> pWolfMap = new HashMap<>();
				for (Agent agent : currentGameInfo.getAgentList()) {
					if (agent == me) continue;
					double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
								+ estimator.getRoleProbability(agent, Role.POSSESSED);
					pWolfMap.put(agent, p);
				}
				List<Agent> suspectedAgent = new ArrayList<>();
				pWolfMap.entrySet().stream()
						.sorted(java.util.Map.Entry.comparingByValue())
						.forEach(entry -> suspectedAgent.add(entry.getKey()));
				for (int i = 0; i < 3; i++) {
					talkQueue.add(new Content(new EstimateContentBuilder(suspectedAgent.get(i), Role.WEREWOLF)));
				}

				isEstimateTalk = true;
			}

			if (talkQueue.isEmpty())
				chooseVoteCandidate();
		}

		// パワープレイに対応する
		// MEMO: 正しく機能するか確認できず
		for (Agent agent : werewolves) {
			if (canPowerplay && comingoutMap.get(agent) == Role.WEREWOLF) {
				voteCandidate = suspicionTarget.get(agent);
			}
		}

		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

	protected void whisperFakeRole() {
		if (whisperTurn == 0) {
			whisperQueue.add(new Content(new ComingoutContentBuilder(me, fakeRole)));
		} else {
			Map<Role, Integer> fakeRoleCount = new HashMap<>();
			for (Role role : fakeRoleMap.values()) {
				Integer count = fakeRoleCount.get(role);
				if (count == null) count = 0;
				fakeRoleCount.put(role, count + 1);
			}

			// 役職騙りの重複があるか
			if (fakeRole != Role.VILLAGER && fakeRoleCount.get(fakeRole) >= 2) {
				// 自分が騙りをやめる
				if (requestedFakeRole == Role.VILLAGER || Math.random() < 0.75) {
					fakeRole = Role.VILLAGER;
					whisperQueue.clear();
					whisperQueue.add(new Content(new ComingoutContentBuilder(me, fakeRole)));
				}
			}

			// 誰も占いの騙りがいなければ調整
			if (fakeRoleCount.get(Role.SEER) == null) {
				// 自分が騙る
				if (requestedFakeRole == Role.SEER || Math.random() < 0.25) {
					fakeRole = Role.SEER;
					whisperQueue.clear();
					whisperQueue.add(new Content(new ComingoutContentBuilder(me, fakeRole)));
				}
			}
		}
	}

	protected void chooseAttackVoteCandidate() {
		if (declaredAttackVoteCandidate != null) return;

		List<Agent> candidates = new ArrayList<>();

		// 役職候補を優先的に襲う
		for (Agent agent : villagers) {
			if (comingoutMap.get(agent) == Role.BODYGUARD) {
				candidates.add(agent);
			}
		}
		if (candidates.isEmpty() && !gjGuarded) {
			for (Agent agent : villagers) {
				if (gjGuarded && currentGameInfo.getLastDeadAgentList().contains(agent)) continue;
				if (comingoutMap.get(agent) == Role.SEER && !fakeSeer.contains(agent)) {
					candidates.add(agent);
				}
			}
		}
		if (candidates.isEmpty()) {
			for (Agent agent : villagers) {
				if (gjGuarded && currentGameInfo.getLastDeadAgentList().contains(agent)) continue;
				if (comingoutMap.get(agent) == Role.MEDIUM) {
					candidates.add(agent);
				}
			}
		}
		if (candidates.isEmpty())
			candidates.addAll(villagers);
		if (candidates.isEmpty())
			candidates.addAll(humans);

		if (!candidates.isEmpty()) {
			// 裏切り者ではなさそうなエージェントを襲う
			double maxProbability = -Const.INF;
			for (Agent agent : candidates) {

				double p = 1.0 - estimator.getRoleProbability(agent, Role.WEREWOLF)
							- estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p > maxProbability) {
					maxProbability = p;
					attackVoteCandidate = agent;
				}
			}
			if (attackVoteCandidate == null)
				attackVoteCandidate = Functions.randomSelect(candidates);

			whisperQueue.add(new Content(new AttackContentBuilder(attackVoteCandidate)));
		}
	}

	public String whisper() {
		whisperTurn++;
		requestedFakeRole = null;

		// 囁きの処理
		whisperList = currentGameInfo.getWhisperList();
		for (int i = whisperListHead; i < whisperList.size(); i++) {
			Talk whisper = whisperList.get(i);
			Agent talker = whisper.getAgent();
			Content content = new Content(whisper.getText());
			Topic topic = content.getTopic();

			switch (topic) {
			case ATTACK:
				atackTargetMap.put(talker, content.getTarget());
				break;
			case COMINGOUT:
				fakeRoleMap.put(talker, content.getRole());
				break;
			case OPERATOR:
				if (content.getOperator() == Operator.REQUEST && content.getTarget() == me) {
					for (Content c : content.getContentList()) {
						if (c.getTopic() == Topic.COMINGOUT) {
							Role role = c.getRole();
							requestedFakeRole = role;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		whisperListHead = whisperList.size();

		// 狂人を見つけた
		if (!findPossessed && possessedList.size() == 1) {
			whisperQueue.add(new Content(new EstimateContentBuilder(possessedList.get(0), Role.POSSESSED)));
			findPossessed = true;
		}

		if (day == 0)
			whisperFakeRole();

		if (day > 0 && attackVoteCandidate == null)
			chooseAttackVoteCandidate();

		return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
	}

	public Agent attack() {
		return attackVoteCandidate;
	}
}
