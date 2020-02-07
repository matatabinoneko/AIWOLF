package army.sh.talk;

import org.aiwolf.client.lib.AgreeContentBuilder;
import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DisagreeContentBuilder;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.GuardCandidateContentBuilder;
import org.aiwolf.client.lib.GuardedAgentContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.OverContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import army.sh.gadget.Check;

public class TalkFactory {

	public static String comingout(Agent target, Role role) {
		if (Check.isNull(target) || Check.isNull(role)) {
			return null;
		}
		return new Content(new ComingoutContentBuilder(target, role)).getText();
	}

	public static String estimate(Agent target, Role role) {
		if (Check.isNull(target) || Check.isNull(role)) {
			return null;
		}
		return new Content(new EstimateContentBuilder(target, role)).getText();
	}

	public static String vote(Agent target) {
		if (Check.isNull(target)) {
			return null;
		}
		return new Content(new VoteContentBuilder(target)).getText();
	}

	public static String divination(Agent target) {
		if (Check.isNull(target)) {
			return null;
		}
		return new Content(new DivinationContentBuilder(target)).getText();
	}

	public static String agree(Talk talk) {
		if (Check.isNull(talk)) {
			return null;
		}
		return new Content(new AgreeContentBuilder(TalkType.TALK, talk.getDay(), talk.getIdx())).getText();
	}

	public static String disagree(Talk talk) {
		if (Check.isNull(talk)) {
			return null;
		}
		return new Content(new DisagreeContentBuilder(TalkType.TALK, talk.getDay(), talk.getIdx())).getText();
	}

	public static String attack(Agent target) {
		if (Check.isNull(target)) {
			return null;
		}
		return new Content(new AttackContentBuilder(target)).getText();
	}

	public static String divinedResult(Agent target, Species result) {
		if (Check.isNull(target) || Check.isNull(result)) {
			return null;
		}
		return new Content(new DivinedResultContentBuilder(target, result)).getText();
	}

	public static String guardCandidate(Agent target) {
		if (Check.isNull(target)) {
			return null;
		}
		return new Content(new GuardCandidateContentBuilder(target)).getText();
	}

	public static String guardedAgent(Agent target) {
		if (Check.isNull(target)) {
			return null;
		}
		return new Content(new GuardedAgentContentBuilder(target)).getText();
	}

	public static String ident(Agent target, Species result) {
		if (Check.isNull(target) || Check.isNull(result)) {
			return null;
		}
		return new Content(new IdentContentBuilder(target, result)).getText();
	}

	public static String skip() {
		return new Content(new SkipContentBuilder()).getText();
	}

	public static String over() {
		return new Content(new OverContentBuilder()).getText();
	}

	public static String requestAgree(Agent agent, Talk talk) {
		if (Check.isNull(agent) || Check.isNull(talk)) {
			return null;
		}
		Content content = new Content(new AgreeContentBuilder(TalkType.TALK, talk.getDay(), talk.getIdx()));
		return new Content(new RequestContentBuilder(agent, content)).getText();
	}
	
	// request囁き用に占い結果などもあったほうがよい

}
