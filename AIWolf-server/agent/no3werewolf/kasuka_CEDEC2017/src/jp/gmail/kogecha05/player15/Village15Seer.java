package jp.gmail.kogecha05.player15;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Functions;

public class Village15Seer extends Village15Villager {
	boolean isCameout;
	Deque<Judge> divinationQueue = new LinkedList<>();
	Map<Agent, Species> myDivinationMap = new HashMap<>();

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		isCameout = false;
		divinationQueue.clear();
		myDivinationMap.clear();
	}

	public void dayStart() {
		super.dayStart();

		if (day > 0) {
			Judge divination = currentGameInfo.getDivineResult();
			if (divination != null) {
				divinationQueue.offer(divination);
				if (divination.getResult() == Species.HUMAN) {
					divinedWhite.add(divination.getTarget());
					humans.add(divination.getTarget());
				} else {
					divinedBlack.add(divination.getTarget());
					werewolves.add(divination.getTarget());
				}
				myDivinationMap.put(divination.getTarget(), divination.getResult());
			}
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

		// 自分以外の占いCOは人狼陣営確定
		for (Agent agent: aliveOthers) {
			if (comingoutMap.get(agent) == Role.SEER) {
				fakeSeer.add(agent);
				estimator.setRoleProbability(agent, Role.WEREWOLF, 0.5);
				estimator.setRoleProbability(agent, Role.POSSESSED, 0.5);
			}
		}
	}

	public String talk() {
		// 初日にCO
		if (!isCameout) {
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
			isCameout = true;
		}

		if (isCameout) {
			while (!divinationQueue.isEmpty()) {
				Judge divination = divinationQueue.poll();
				if (divination.getTarget() == null || divination.getResult() == null)
					break;
				talkQueue.offer(new Content(
						new DivinedResultContentBuilder(divination.getTarget(), divination.getResult())));
			}
		}

		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

	public Agent divine() {
		List<Agent> candidates = new ArrayList<>();
		for (Agent agent : aliveOthers) {
			if (myDivinationMap.containsKey(agent)) continue;
			candidates.add(agent);
		}
		if (candidates.isEmpty())
			candidates.addAll(aliveOthers);

		Agent target = null;
		// 人狼陣営の可能性が高いエージェントを占う
		double maxProbability = 0.0;
		for (Agent agent : candidates) {
			double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
					+ estimator.getRoleProbability(agent, Role.POSSESSED);
			if (p > maxProbability) {
				maxProbability = p;
				target = agent;
			}
		}
		if (target == null)
			target = Functions.randomSelect(candidates);

		return target;
	}
}
