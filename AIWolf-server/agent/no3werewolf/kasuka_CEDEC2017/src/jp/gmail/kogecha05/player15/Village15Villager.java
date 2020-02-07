package jp.gmail.kogecha05.player15;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Functions;

public class Village15Villager extends Village15BasePlayer {
	boolean isEstimateTalk;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
	}

	public void dayStart() {
		super.dayStart();

		isEstimateTalk = false;
	}

	protected void chooseVoteCandidate() {
		List<Agent> candidates = new ArrayList<>(aliveOthers);

		// 黒を出されたエージェントを優先的に吊る
		List<Agent> blackCandidates = new ArrayList<>();
		for (Agent agent : candidates) {
			if (divinedBlack.contains(agent) || werewolves.contains(agent))
				blackCandidates.add(agent);
		}

		// 序盤は白は避ける
		for (Agent agent: aliveOthers) {
			if (day <= 3 && divinedWhite.contains(agent) || humans.contains(agent))
				candidates.remove(agent);
		}

		// 候補が残らなかったら全員から
		if (candidates.isEmpty())
			candidates = new ArrayList<>(aliveOthers);

		// 投票先を設定
		if (!blackCandidates.isEmpty()) {
			// 狼陣営の可能性が最も高いエージェントに投票
			double maxProbability = 0.0;
			for (Agent agent : blackCandidates) {
				double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
						+ estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p > maxProbability) {
					maxProbability = p;
					voteCandidate = agent;
				}
			}
			if (voteCandidate == null)
				voteCandidate = Functions.randomSelect(blackCandidates);
		} else {
			double maxProbability = 0.0;
			for (Agent agent : candidates) {
				double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
						+ estimator.getRoleProbability(agent, Role.POSSESSED);
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
		// 自分の推理を発言
		if (!isEstimateTalk) {
			// 狼っぽいエージェントを挙げる
			Map<Agent, Double> pWolfMap = new HashMap<>();
			for (Agent agent : currentGameInfo.getAgentList()) {
				if (agent == me) continue;
				if (isKilled(agent));
				double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
						+ estimator.getRoleProbability(agent, Role.POSSESSED);
				pWolfMap.put(agent, p);
			}

			List<Agent> suspectedAgent = new ArrayList<>();
			pWolfMap.entrySet().stream()
					.sorted(Collections.reverseOrder(java.util.Map.Entry.comparingByValue()))
					.forEach(entry -> suspectedAgent.add(entry.getKey()));

			dangerAgent.clear();
			for (int i = 0; i < 3; i++) {
				talkQueue.offer(new Content(new EstimateContentBuilder(suspectedAgent.get(i), Role.WEREWOLF)));
				dangerAgent.add(suspectedAgent.get(i));
			}

			isEstimateTalk = true;
		}

		if (talkQueue.isEmpty())
			chooseVoteCandidate();

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