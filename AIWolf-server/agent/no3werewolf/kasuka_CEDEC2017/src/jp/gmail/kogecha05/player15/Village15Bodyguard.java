package jp.gmail.kogecha05.player15;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Const;

public class Village15Bodyguard extends Village15Villager {

	Agent guardedAgent;
	Set<Agent> successGuard = new HashSet<>();

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		guardedAgent = null;
		successGuard.clear();
	}

	public void dayStart() {
		super.dayStart();

		List<Agent> deadAgentList = currentGameInfo.getLastDeadAgentList();
		// 護衛成功
		if (deadAgentList.isEmpty()) {
			humans.add(guardedAgent);
			successGuard.add(guardedAgent);
		}
	}

	public void update(GameInfo gameInfo) {
		super.update(gameInfo);

		// 護衛に成功した相手は人狼ではない
		for (Agent agent: successGuard) {
			estimator.setRoleProbability(agent, Role.WEREWOLF, 0.0);
		}
	}

	public Agent guard() {
		Agent guardCandidate = null;
		List<Agent> candidates = new ArrayList<>();

		// 占いを守る
		for (Agent agent : aliveOthers) {
			if (comingoutMap.get(agent) == Role.SEER
					&& !fakeSeer.contains(agent)
					&& !dangerAgent.contains(agent)) {
				candidates.add(agent);
			}
		}

		// 霊媒を守る
		if (candidates.isEmpty()) {
			for (Agent agent : aliveOthers) {
				if (comingoutMap.get(agent) == Role.MEDIUM
						&& !divinedBlack.contains(agent)
						&& !dangerAgent.contains(agent)) {
					candidates.add(agent);
				}
			}
		}

		// 黒以外から適当に
		if (candidates.isEmpty()) {
			for (Agent agent : aliveOthers) {
				if (!divinedBlack.contains(agent)
						&& !dangerAgent.contains(agent)) {
					candidates.add(agent);
				}
			}
		}

		// 全員から
		if (candidates.isEmpty()) {
			candidates.addAll(aliveOthers);
		}

		// 一番狼陣営ではなさそうなエージェントを護衛
		double minProbability = Const.INF;
		for (Agent agent : candidates) {
			double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
					+ estimator.getRoleProbability(agent, Role.POSSESSED);
			if (p < minProbability) {
				minProbability = p;
				guardCandidate = agent;
			}
		}

		guardedAgent = guardCandidate;
		return guardCandidate;
	}
}
