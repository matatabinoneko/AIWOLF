package jp.gmail.kogecha05.player5;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Functions;

public class Village5Villager extends Village5BasePlayer {
	boolean isFakeCo;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		isFakeCo = false;
	}

	// 1日目
	protected void firstDayTalk() {

		List<Agent> candidates = new ArrayList<>(aliveOthers);

		// 2CO以下なら非CO吊り、3COならCO吊り
		if (numFirstCo <= 2) {
			for (Agent agent : aliveOthers) {
				if (comingoutMap.get(agent) == Role.SEER)
					candidates.remove(agent);
			}
		} else {
			for (Agent agent : aliveOthers) {
				if (comingoutMap.get(agent) != Role.SEER)
					candidates.remove(agent);
			}
		}

		// 黒を出されたエージェントを優先的に吊る
		List<Agent> blackCandidates = new ArrayList<>();
		for (Agent agent : candidates) {
			if (divinedBlack.contains(agent))
				blackCandidates.add(agent);
		}

		// 白は避ける
		for (Agent agent: aliveOthers) {
			if (divinedWhite.contains(agent))
				candidates.remove(agent);
		}

		// 候補が残らなかったら全員から
		if (candidates.isEmpty())
			candidates = new ArrayList<>(aliveOthers);

		// 投票先を設定
		if (!blackCandidates.isEmpty()) {
			double maxProbability = 0.0;
			for (Agent agent : blackCandidates) {
				double p = estimator.getRoleProbability(agent, Role.WEREWOLF);
				if (p > maxProbability) {
					maxProbability = p;
					voteCandidate = agent;
				}
			}
			if (voteCandidate == null)
				voteCandidate = Functions.randomSelect(blackCandidates);
		} else {
			// 最も狼度の高いエージェントに投票する
			double maxProbability = 0.0;
			for (Agent agent : candidates) {
				double p = estimator.getRoleProbability(agent, Role.WEREWOLF);
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
			talkQueue.clear();
			talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
			talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
			talkQueue.offer(new Content(
					new RequestContentBuilder(
							null, new Content(
									new VoteContentBuilder(voteCandidate)))));
			declaredVoteCandidate = voteCandidate;
		}
	}

	// 2日目
	protected void secondDayTalk() {
		// 狼陣営のフリをしてパワープレイを妨害する
		Role fakeRole = null;
		List<Agent> candidates = new ArrayList<>(aliveOthers);

		if (numFirstCo == 1) {
			// 真占いしか出ていない
			for (Agent agent: aliveOthers) {
				if (firstDayCo.contains(agent)) continue;
				candidates.add(agent);
			}
		} else if (numFirstCo == 2) {
			// CO内訳　真狂
			// 非CO吊り
			for (Agent agent: aliveOthers) {
				if (firstDayCo.contains(agent)) continue;
				candidates.add(agent);
			}

			// 生き残りが狼狂パターンのときのパワープレイを防ぐため人狼CO
			if (numAliveCo > 0) {
				fakeRole = Role.WEREWOLF;
			}
		} else if (numFirstCo == 3) {
			if (numAliveCo == 1) {
				// 村村狼
				// CO吊り
				for (Agent agent: aliveOthers) {
					if (!firstDayCo.contains(agent)) continue;
					candidates.add(agent);
				}
			} else if (numAliveCo == 2) {
				// 村狼狂　または村狼占
				// 狼狂パターンによるパワープレイを防ぐため人狼CO
				fakeRole = Role.WEREWOLF;
			}
		}

		// 候補が残らなかったら全員から
		if (candidates.isEmpty())
			candidates = new ArrayList<>(aliveOthers);

		// 投票先を設定
		double maxProbability = 0.0;
		for (Agent agent : candidates) {
			double p = estimator.getRoleProbability(agent, Role.WEREWOLF);
			if (p > maxProbability) {
				maxProbability = p;
				voteCandidate = agent;
			}
		}
		if (voteCandidate == null)
			voteCandidate = Functions.randomSelect(candidates);

		// 発言をセット
		talkQueue.clear();
		if (fakeRole != null) {
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, fakeRole)));
			talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
			talkQueue.offer(new Content(
					new RequestContentBuilder(
							null, new Content(
									new VoteContentBuilder(voteCandidate)))));
		}
	}

	public void dayStart() {
		super.dayStart();
		if (day == 2) secondDayTalk();
	}

	public String talk() {
		if (day == 1) firstDayTalk();

		return super.talk();
	}

	public String whisper() {
		throw new UnsupportedOperationException();
	}

	public Agent attack() {
		throw new UnsupportedOperationException();
	}

	public Agent divine() {
		throw new UnsupportedOperationException();
	}

	public Agent guard() {
		throw new UnsupportedOperationException();
	}
}