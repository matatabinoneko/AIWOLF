package jp.gmail.kogecha05.player5;

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
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Const;
import jp.gmail.kogecha05.utils.Functions;

public class Village5Possessed extends Village5BasePlayer {
	boolean isTrueCo;
	boolean isDivined;
	boolean powerplay;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		isTrueCo = false;
		isDivined = false;
		powerplay = false;
	}

	public void dayStart() {
		super.dayStart();

		// 初日に即占いCOで適当な相手に黒判定
		if (day == 1) {
			voteCandidate = Functions.randomSelect(aliveOthers);
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
		}

		if (day == 2) {
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.POSSESSED)));
			isTrueCo = true;

			// 人狼じゃなさそうなエージェントを吊る
			double minProbability = INF;
			for (Agent agent : aliveOthers) {
				double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
						+ estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p < minProbability) {
					minProbability = p;
					voteCandidate = agent;
				}
			}

			Agent notWerewolf = null;
			Agent werewolf = null;

			if (numFirstCo == 2) {
				if (numAliveCo == 1) {
					// 狂狼村　ランダム

				} else if (numAliveCo == 2) {
					// 狂狼占　CO吊り
					for (Agent agent : aliveOthers) {
						if (firstDayCo.contains(agent))
							notWerewolf = agent;
						else
							werewolf = agent;
					}
				}
			} else if (numFirstCo == 3) {
				if (numAliveCo == 1) {
					// 考察範囲外
				} else if (numAliveCo == 2) {
					// 狂狼村　非CO吊り
					for (Agent agent : aliveOthers) {
						if (!firstDayCo.contains(agent))
							notWerewolf = agent;
						else
							werewolf = agent;
					}
				} else if (numAliveCo == 3) {
					// 狂狼占　ランダム
				}
			}

			// 黒が処刑されたのに2日目になっていればその占いは狼
			Agent fakeSeer = null;
			for (Judge j : divinationList) {
				if (j.getTarget() == currentGameInfo.getLatestExecutedAgent()
						&& j.getResult() == Species.WEREWOLF) {
					fakeSeer = j.getAgent();
				}
			}
			if (fakeSeer != null && numFirstCo == 3) {
				for (Agent agent : aliveOthers) {
					if (agent == fakeSeer)
						werewolf = agent;
					else
						notWerewolf = agent;
				}
			}

			if (notWerewolf != null && werewolf != null) {
				voteCandidate = notWerewolf;
			}

			talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
			talkQueue.offer(new Content(
					new RequestContentBuilder(
							null, new Content(
									new VoteContentBuilder(voteCandidate)))));
		}
	}

	public String talk() {
		if (day == 1) {
			if (talkQueue.isEmpty() && !isDivined) {
				double minProbability = Const.INF;
				for (Agent agent : aliveOthers) {
					double p = estimator.getRoleProbability(agent, Role.WEREWOLF)
								+ estimator.getRoleProbability(agent, Role.POSSESSED);
					if (p < minProbability) {
						minProbability = p;
						voteCandidate = agent;
					}
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

		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}

}
