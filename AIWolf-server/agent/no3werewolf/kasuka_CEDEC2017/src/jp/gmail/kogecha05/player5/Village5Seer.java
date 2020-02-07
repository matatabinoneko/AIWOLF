package jp.gmail.kogecha05.player5;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Const;
import jp.gmail.kogecha05.utils.Functions;

public class Village5Seer extends Village5BasePlayer {
	boolean isFakeCo;
	boolean isDivined;
	Judge divination;
	List<Agent> divinedAgents = new ArrayList<>();
	List<Judge> divinedResults = new ArrayList<>();
	List<Agent> whiteList = new ArrayList<>();
	List<Agent> blackList = new ArrayList<>();

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		isFakeCo = false;
		divinedAgents.clear();
		divinedResults.clear();
		whiteList.clear();
		blackList.clear();
	}

	public void dayStart() {
		super.dayStart();
		isDivined = false;
		divination = currentGameInfo.getDivineResult();

		if (divination != null) {
			switch(divination.getResult()) {
			case HUMAN:
				whiteList.add(divination.getTarget());
				break;
			case WEREWOLF:
				blackList.add(divination.getTarget());
				break;
			default:
				break;
			}
			divinedResults.add(divination);
		}

		// 初日最速CO（5人村では様子見をする理由がない）
		if (day == 1) {
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
		}

		// 2日目に生き残った場合は人狼陣営を誘導して人狼に投票させる
		if (day == 2) {
			Agent werewolf = Functions.randomSelect(blackList);
			if (werewolf == null) {
				double maxProbability = -Const.INF;
				for (Agent agent : aliveOthers) {
					if (whiteList.contains(agent)) continue;
					double p = estimator.getRoleProbability(agent, Role.WEREWOLF);
					if (p > maxProbability) {
						maxProbability = p;
						werewolf = agent;
					}
				}
			}
			Agent notWerewolf = null;
			for (Agent agent : aliveOthers) {
				if (agent == werewolf) continue;
				notWerewolf = agent;
			}
			voteCandidate = werewolf;

			if (numFirstCo == 1) {
				// 真1陣形
				// 狼が自分を噛んでこない理由がないので、2日目生存は絶望的
				// 万が一生き残ったら、狂人のふりをしておく
				// 生存者内訳：占村狼 or 占狂狼
				fakePossessed(werewolf, notWerewolf);
			} else if (numFirstCo == 2) {
				// 真狂陣形と考える（狂人がCOしないケースは考えない）
				if (numAliveCo == 1) {
					// 狂人が噛まれた
					// 狼は占いを噛む博打を打ったと考え、狂人のふりをしておく
					// 生存者内訳：占村狼
					fakePossessed(werewolf, notWerewolf);
				} else {
					// 狼は狂人噛みを避けて村人を噛んだ
					// 狼のふりをして狂人に真狼を噛むように誘導
					// 生存者内訳：占狂狼
					fakeWerewolf(werewolf, notWerewolf);
				}
			} else {
				// 真狂狼陣形
				if (numAliveCo == 1) {
					// 考察範囲外の謎パターン（村人が偽COでもしたかなにか）
					talkQueue.offer(new Content(new VoteContentBuilder(werewolf)));
					talkQueue.offer(new Content(new EstimateContentBuilder(werewolf, Role.WEREWOLF)));
					talkQueue.offer(new Content(
							new RequestContentBuilder(
									werewolf, new Content(
											new VoteContentBuilder(werewolf)))));
				} else if (numAliveCo == 2) {
					// 狂が吊られた
					// 生存者内訳：占村狼
					fakePossessed(werewolf, notWerewolf);
				} else {
					// 生存者内訳：占狂狼
					fakeWerewolf(werewolf, notWerewolf);
				}
			}
		}
	}

	private void fakePossessed(Agent werewolf, Agent notWerewolf) {
		talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.POSSESSED)));
		talkQueue.offer(new Content(new VoteContentBuilder(notWerewolf)));
		talkQueue.offer(new Content(
				new RequestContentBuilder(
						werewolf, new Content(
								new VoteContentBuilder(notWerewolf)))));
	}

	private void fakeWerewolf(Agent werewolf, Agent notWerewolf) {
		talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.WEREWOLF)));
		talkQueue.offer(new Content(new VoteContentBuilder(werewolf)));
		talkQueue.offer(new Content(
				new RequestContentBuilder(
						null, new Content(
								new VoteContentBuilder(werewolf)))));
	}

	public String talk() {
		// 占い戦略:
		// 初日占い結果が黒なら正直に申告
		// 白なら適当な相手に黒を出し、2日目があれば人狼陣営のふりをする
		if (day == 1) {
			if (talkQueue.isEmpty() && !isDivined) {
				List<Agent> candidates = new ArrayList<>();
				for (Agent agent : aliveOthers) {
					if (divinedAgents.contains(agent)) continue;
					candidates.add(agent);
				}
				double maxProbability = -Const.INF;
				for (Agent agent : candidates) {
					double p = estimator.getRoleProbability(agent, Role.WEREWOLF);
					if (p > maxProbability) {
						maxProbability = p;
						voteCandidate = agent;
					}
				}

				if (divination.getResult() == Species.WEREWOLF) {
					voteCandidate = divination.getTarget();
				}

				talkQueue.offer(new Content(
						new DivinedResultContentBuilder(voteCandidate, Species.WEREWOLF)));
				talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
				talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
				talkQueue.offer(new Content(
						new RequestContentBuilder(
								null, new Content(
										new VoteContentBuilder(voteCandidate)))));
			}
		}

		if (!talkQueue.isEmpty() && talkQueue.peekFirst().getTopic() == Topic.DIVINED) {
				isDivined = true;
		}

		return super.talk();
	}

	public Agent divine() {
		List<Agent> candidates = new ArrayList<>();
		for (Agent agent : aliveOthers) {
			if (divinedAgents.contains(agent)) continue;
			candidates.add(agent);
		}

		Agent target = null;
		double maxProbability = -Const.INF;
		for (Agent agent : candidates) {
			double p = estimator.getRoleProbability(agent, Role.WEREWOLF);
			if (p > maxProbability) {
				maxProbability = p;
				target = agent;
			}
		}
		if (target == null) Functions.randomSelect(candidates);
		divinedAgents.add(target);
		return target;
	}
}
