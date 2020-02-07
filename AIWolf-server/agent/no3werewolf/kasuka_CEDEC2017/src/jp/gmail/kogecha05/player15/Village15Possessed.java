package jp.gmail.kogecha05.player15;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Functions;

public class Village15Possessed extends Village15Seer {
	int numWolves;
	boolean canPowerplay;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);
		canPowerplay = false;
	}

	private Judge getFakeDivination() {
		List<Agent> candidates = new ArrayList<>();
		for (Agent agent : aliveOthers) {
			if (myDivinationMap.containsKey(agent)
					|| comingoutMap.get(agent) == Role.SEER)
				continue;

			candidates.add(agent);
		}
		if (candidates.isEmpty())
			Functions.randomSelect(aliveOthers);

		Agent target = null;
		// 最も狼度の低いエージェントを占う
		double maxProbability = 0.0;
		for (Agent agent : candidates)
		{
			double p = 1.0 - estimator.getRoleProbability(agent, Role.WEREWOLF)
					- estimator.getRoleProbability(agent, Role.POSSESSED);
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
		if (nFakeWolves < (numWolves - 1) && Math.random() < 0.5) {
			result = Species.WEREWOLF;
		}

		return new Judge(day, me, target, result);
	}

	public void dayStart() {
		super.dayStart();

		if (day > 0) {
			// 偽占い
			Judge divination = getFakeDivination();
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

		// パワープレイが可能か判定
		if (!fakeSeer.isEmpty() && (maxNumWerewolves + 1) > (aliveOthers.size() - maxNumWerewolves)) {
			canPowerplay = true;
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
			if (day <= 3 && divinedWhite.contains(agent) || humans.contains(agent))
				candidates.remove(agent);
		}

		// 候補が残らなかったら全員から
		if (candidates.isEmpty())
			candidates = new ArrayList<>(aliveOthers);

		// 投票先を設定
		if (!blackCandidates.isEmpty()) {
			double maxProbability = 0.0;
			for (Agent agent : blackCandidates) {
				double p = 1.0 - estimator.getRoleProbability(agent, Role.WEREWOLF)
						- estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p > maxProbability) {
					maxProbability = p;
					voteCandidate = agent;
				}
			}
			if (voteCandidate == null)
				voteCandidate = Functions.randomSelect(blackCandidates);
		} else {
			// 最も狼度の低いエージェントに投票する
			double maxProbability = 0.0;
			for (Agent agent : candidates) {
				double p = 1.0 - estimator.getRoleProbability(agent, Role.WEREWOLF)
						- estimator.getRoleProbability(agent, Role.POSSESSED);
				if (p > maxProbability) {
					maxProbability = p;
					voteCandidate = agent;
				}
			}
			if (voteCandidate == null)
				voteCandidate = Functions.randomSelect(candidates);
		}

		// パワープレイに対応する
		// MEMO: ログで事例を確認できなかったので正しく動くか不明
		for (Agent agent : aliveOthers) {
			if (canPowerplay && comingoutMap.get(agent) == Role.WEREWOLF) {
				voteCandidate = suspicionTarget.get(agent);
				if (comingoutMap.get(me) == Role.POSSESSED) {
					talkQueue.clear();
					talkQueue.add(new Content(new ComingoutContentBuilder(me, Role.POSSESSED)));
				}
			}
		}

		// 投票先を宣言
		if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
			talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
			declaredVoteCandidate = voteCandidate;
		}
	}
}
