package jp.gr.java_conf.otk.aiwolf.totj;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

/** 村人役エージェントクラス */
public class TOTJVillager extends TOTJBasePlayer {

	protected List<Agent> werewolves = new ArrayList<>();

	@Override
	protected void chooseVoteCandidate() {
		werewolves.clear();
		for (Judge j : divinationList) {
			// 自分あるいは殺されたエージェントを人狼と判定していて，生存している自称占い師を投票先候補とする
			if (j.getResult() == Species.WEREWOLF && (j.getTarget() == me || isKilled(j.getTarget()))) {
				Agent candidate = j.getAgent();
				if (isAlive(candidate) && !werewolves.contains(candidate)) {
					werewolves.add(candidate);
				}
			}
		}
		// 候補がいない場合は確率順
		if (werewolves.isEmpty()) {
			if (!aliveOthers.contains(voteCandidate)) {
				voteCandidate = probSelect(aliveOthers, Role.WEREWOLF);
			}
		} else {
			if (!werewolves.contains(voteCandidate)) {
				voteCandidate = probSelect(werewolves, Role.WEREWOLF);
				// 以前の投票先から変わる場合，新たに推測発言と占い要請をする
				if (canTalk) {
					talkQueue.offer(new Content(new EstimateContentBuilder(voteCandidate, Role.WEREWOLF)));
					talkQueue.offer(new Content(new RequestContentBuilder(null, new Content(new DivinationContentBuilder(voteCandidate)))));
				}
			}
		}
	}

	@Override
	public String whisper() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent attack() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent divine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent guard() {
		throw new UnsupportedOperationException();
	}
}
