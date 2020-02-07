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

public class Village5Werewolf extends Village5BasePlayer {
	boolean isDivined;
	List<Agent> possessedCandidate = new ArrayList<>();
	boolean seerPlay;

	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		isDivined = false;

		possessedCandidate.clear();
		seerPlay = (Math.random() < pFakeCo);
		coef = seerPlay ? 1 : -1;
	}

	public void dayStart() {
		super.dayStart();

		// 初日に即占いCOで適当な相手に黒判定
		if (day == 1 && seerPlay) {
			talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.SEER)));
		}

		if (day == 2) {
			if (seerPlay) {
				// 騙りをしている
				if (numAliveCo == 2) {
					// 狼狂村　または狼占村
					// 狼COパワープレイ　狼占村パターンは無理
					for (Agent agent: aliveOthers) {
						if (!firstDayCo.contains(agent))
							voteCandidate = agent;
					}

					talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.WEREWOLF)));
					talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
					talkQueue.offer(new Content(
							new RequestContentBuilder(
									null, new Content(
											new VoteContentBuilder(voteCandidate)))));

				} else if (numAliveCo == 3) {
					// 狼狂占
					// 狼COパワープレイ
					voteCandidate = Functions.randomSelect(aliveOthers);

					talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.WEREWOLF)));
					talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
					talkQueue.offer(new Content(
							new RequestContentBuilder(
									null, new Content(
											new VoteContentBuilder(voteCandidate)))));
				}
			} else {
				// 潜伏している
				if (numAliveCo == 1) {
					// 狼狂村　狼占村パターンもあるけど諦める
					// 狼COパワープレイ
					voteCandidate = Functions.randomSelect(aliveOthers);

					talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.WEREWOLF)));
					talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
					talkQueue.offer(new Content(
							new RequestContentBuilder(
									null, new Content(
											new VoteContentBuilder(voteCandidate)))));
				} else if (numAliveCo == 2) {
					// 狼狂占
					voteCandidate = Functions.randomSelect(aliveOthers);

					talkQueue.offer(new Content(new ComingoutContentBuilder(me, Role.WEREWOLF)));
					talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
					talkQueue.offer(new Content(
							new RequestContentBuilder(
									null, new Content(
											new VoteContentBuilder(voteCandidate)))));
				} else {
					// 狼村村
					voteCandidate = Functions.randomSelect(aliveOthers);
				}
			}
		}
	}

	public void update(GameInfo gameInfo) {
		super.update(gameInfo);

		// 裏切り者を特定する
		possessedCandidate.clear();
		for (Judge j : divinationList) {
			if (possessedCandidate.contains(j.getAgent()));
			// 自分以外に黒判定をした場合裏切り者
			if (j.getTarget() != me && j.getResult() == Species.WEREWOLF) {
				possessedCandidate.add(j.getAgent());
			}

			// 自分に白判定をした場合裏切り者
			if (j.getTarget() == me && j.getResult() == Species.HUMAN) {
				possessedCandidate.add(j.getAgent());
			}
		}
	}

	// 1日目: 村人偽装（ただし狼に有利なように一部を変更しておく）
	protected void firstDayTalk() {
		List<Agent> candidates = new ArrayList<>(aliveOthers);

		// 非CO吊り
		for (Agent agent : aliveOthers) {
			if (comingoutMap.get(agent) == Role.SEER)
				candidates.remove(agent);
		}
		if (candidates.isEmpty())
			candidates = new ArrayList<>(aliveOthers);

		// 投票先を設定 裏切り者じゃなさそうなエージェントを吊る
		double minProbability = INF;
		for (Agent agent : candidates) {
			double p = estimator.getRoleProbability(agent, Role.POSSESSED);
			if (p < minProbability) {
				minProbability = p;
				voteCandidate = agent;
			}
		}
		if (voteCandidate == null)
			voteCandidate = Functions.randomSelect(candidates);

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

	public String talk() {
		// 1日目は村人または占いとほぼ同じように動く
		if (day == 1 && !seerPlay) {
			// 潜伏
			firstDayTalk();
		} else if (day == 1 && seerPlay) {
			// 占い騙り
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

		// 2日目パワープレイ
		if (day == 2) {
			// 狂CO待ち
			for (Agent agent : aliveOthers) {
				if (comingoutMap.get(agent) == Role.POSSESSED
						&& firstDayCo.contains(agent)
						&& !possessedCandidate.contains(agent))
					possessedCandidate.add(agent);
			}

			// 狂人が特定できているとき
			if (possessedCandidate.size() == 1) {
				for (Agent agent : aliveOthers) {
					if (!possessedCandidate.contains(agent)) {
						voteCandidate = agent;
					}
				}
			}

			// 便乗票
			if (!voteTarget.containsValue(voteCandidate)) {
				for (Agent agent : aliveOthers) {
					if (voteTarget.containsValue(agent)) {
						voteCandidate = agent;
					}
				}
			}
		}

		return super.talk();
	}

	protected void chooseAttackVoteCandidate() {
		List<Agent> candidates = new ArrayList<>();
		if (seerPlay) {
			// 自分が騙っているのでCO内訳は真狂狼
			// 狂噛みを避けるために村人を噛む
			// COが1人吊られている場合、真が残れば詰むけどそれは諦める
			for (Agent agent: aliveOthers) {
				if (possessedCandidate.contains(agent)) continue;
				if (!firstDayCo.contains(agent))
					candidates.add(agent);
			}
		} else {
			// 潜伏　CO内訳は真狂
			if (numAliveCo == 1) {
				// 真占が残って詰むのを防ぐためにCO噛み
				for (Agent agent: aliveOthers) {
					if (possessedCandidate.contains(agent)) continue;
					if (firstDayCo.contains(agent))
						candidates.add(agent);
				}
			} else if (numAliveCo == 2) {
				// 狂噛みを避けるために村人を噛む
				for (Agent agent: aliveOthers) {
					if (possessedCandidate.contains(agent)) continue;
					if (!firstDayCo.contains(agent))
						candidates.add(agent);
				}
			}
		}
		if (candidates.isEmpty()) {
			for (Agent agent: aliveOthers) {
				if (possessedCandidate.contains(agent)) continue;
				candidates.add(agent);
			}
		}

		double minProbability = INF;
		for (Agent agent : candidates) {
			double p = estimator.getRoleProbability(agent, Role.POSSESSED)
						+ estimator.getRoleProbability(agent, Role.WEREWOLF);
			if (p < minProbability) {
				minProbability = p;
				attackVoteCandidate = agent;
			}
		}
		if (attackVoteCandidate == null)
			attackVoteCandidate = Functions.randomSelect(aliveOthers);
	}

}
