package net.mchs_u.mc.aiwolf.dokin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.util.Pair;

import net.mchs_u.mc.aiwolf.common.AbstractEstimate;
import net.mchs_u.mc.aiwolf.common.AgentTargetResult;
import net.mchs_u.mc.aiwolf.common.Probabilities;
import net.mchs_u.mc.aiwolf.common.RoleCombination;

public class Estimate extends AbstractEstimate{
	
	/***********************************************
	 * 初期設定
	 */

	private Map<String,Double> rates = null;
	private Probabilities probs = null;
	
	private List<Agent> agents = null;
	
	private List<Agent> aliveAgents = null;
	private Map<Agent, Role> coMap = null;
	private Map<Agent, Role> definedRoleMap = null;
	private Map<Agent, Species> definedSpeciesMap = null;
	private Set<Agent> teamMemberWolves = null;
	private List<Agent> guardedAgentsWhenAttackFailure = null;
	private List<AgentTargetResult> divinedHistory = null;
	private List<AgentTargetResult> identifiedHistory = null;
	private List<Agent> attackedAgents = null;
	private List<Vote> voteHistory = null;
	
	private Map<Agent, Double> werewolfLikeness = null;
	private Map<Agent, Double> villagerTeamLikeness = null;
	private Map<Integer, Double> aliveWerewolvesNumberProbability = null;
	private Map<Integer, Double> alivePossessedsNumberProbability = null;
	private Map<Integer, Double> aliveVillagerTeamNumberProbability = null;
	
	private Map<Agent, Agent> todaysVotePlanMap = null;
	private Map<Agent, Pair<Integer, Agent>> todaysVoteRequestMap = null;
	
	public Estimate(List<Agent> agents, GameSetting gameSetting) {
		this.agents = agents;
		
		rates = new HashMap<>();

		rates.put("VOTE_POSSESSED_TO_WEREWOLF"          , 0.900d);
		rates.put("VOTE_WEREWOLF_TO_POSSESSED"          , 0.900d);
		rates.put("VOTE_WEREWOLF_TO_WEREWOLF"           , 0.900d);
		rates.put("FALSE_IDENTIFIED_FROM_VILLAGER_TEAM" , 0.010d);
		rates.put("FALSE_DIVINED_FROM_VILLAGER_TEAM"    , 0.010d);
		rates.put("BLACK_DIVINED_POSSESSED_TO_WEREWOLF" , 0.900d);
		rates.put("BLACK_DIVINED_WEREWOLF_TO_POSSESSED" , 0.500d);
		rates.put("BLACK_DIVINED_WEREWOLF_TO_WEREWOLF"  , 0.100d);
		rates.put("2_SEER_CO_FROM_VILLGER_TEAM"         , 0.001d);
		rates.put("2_MEDIUM_CO_FROM_VILLAGER_TEAM"      , 0.001d);
		rates.put("2_BODYGUARD_CO_FROM_VILLAGER_TEAM"   , 0.001d);
		rates.put("ONLY_SEER_CO_FROM_WEREWOLF_TEAM"     , 0.010d);
		rates.put("ONLY_MEDIUM_CO_FROM_WEREWOLF_TEAM"   , 0.010d);
		rates.put("GUARDED_WEREWOLF_WHEN_ATTACK_FAILURE", 0.100d);
		rates.put("POSSESSED_CO_FROM_OUTSIDE_POSSESSED" , 0.010d);
		rates.put("WEREWOLF_CO_FROM_OUTSIDE_WEREWOLF"   , 0.010d);
		
		rates.put("NO_WEREWOLVES"                       , 0.000d);
		rates.put("WEREWOLVES_ARE_MORE_THAN_HUMANS"     , 0.000d);
		rates.put("INCONSISTENT_WITH_MY_ABILITY_RESULT" , 0.000d);
		rates.put("INCONSISTENT_WITH_ROLE_I_KNOW"       , 0.000d);
		rates.put("ATTACKED_WEREWOLF"                   , 0.000d);
		
		rates.put("TEAM_MEMBER_WOLF"                    , 0.500d);
		
		rates.put("NUMBER_PROBABILITY_OF_CONVICTION"    , 0.800d); // 確率がいくら以上だったらその人数が残っていることを確信するか（0.5以下に設定してはいけない）
		rates.put("WEREWOLF_LIKENESS_OF_CONVICTION"     , 0.800d); // らしさがいくら以上だったらその人が人狼であることを確信するか
		
		coMap = new HashMap<>();
		definedRoleMap = new HashMap<>();
		definedSpeciesMap = new HashMap<>();
		teamMemberWolves = new HashSet<>();
		divinedHistory = new ArrayList<>();
		identifiedHistory = new ArrayList<>();
		guardedAgentsWhenAttackFailure = new ArrayList<>();
		attackedAgents = new ArrayList<>();
		voteHistory = new ArrayList<>();
		
		probs = new Probabilities(agents, gameSetting);
	}
	
	/***********************************************
	 * 状況のアップデート(public)
	 */
	
	public void dayStart(GameInfo gameInfo){		
		todaysVotePlanMap = new HashMap<>();
		todaysVoteRequestMap = new HashMap<>();
		updateAliveAgentList(gameInfo.getAliveAgentList());
		updateDeadAgentList(gameInfo.getLastDeadAgentList());
		updateVoteList(gameInfo.getVoteList());
	}
	
	//確定した役職（自分の役職、仲間の狼など）以外の確率をゼロにする
	public void updateDefinedRole(Agent agent, Role role){
		definedRoleMap.put(agent, role);
		
		for(RoleCombination rc: probs.getRoleCombinations()){
			if(role == Role.POSSESSED){
				if(!rc.isPossessed(agent))
					probs.update(rc, rates.get("INCONSISTENT_WITH_ROLE_I_KNOW"));
			} else if(role == Role.WEREWOLF) {
				if(!rc.isWerewolf(agent))
					probs.update(rc, rates.get("INCONSISTENT_WITH_ROLE_I_KNOW"));
			} else {
				if(!rc.isVillagerTeam(agent))
					probs.update(rc, rates.get("INCONSISTENT_WITH_ROLE_I_KNOW"));
			}
		}
	}
	
	//確定した人種（自分目線の占い結果など）以外の確率をゼロにする
	public void updateDefinedSpecies(Agent agent, Species species){
		definedSpeciesMap.put(agent, species);
		
		for(RoleCombination rc: probs.getRoleCombinations()){
			if(species == Species.WEREWOLF){
				if(!rc.isWerewolf(agent))
					probs.update(rc, rates.get("INCONSISTENT_WITH_MY_ABILITY_RESULT"));
			} else {
				if(rc.isWerewolf(agent))
					probs.update(rc, rates.get("INCONSISTENT_WITH_MY_ABILITY_RESULT"));
			}
		}
	}
	
	//仲間の狼の確率を下げる（身内切りのためゼロにはしない）（村人目線のときにつかう）
	public void updateTeamMemberWolf(List<Agent> agents){
		teamMemberWolves.addAll(agents);
		
		for(RoleCombination rc: probs.getRoleCombinations()){
			for(Agent a: agents){
				if(rc.isWerewolf(a)){
					probs.update(rc, rates.get("TEAM_MEMBER_WOLF"));
					break;
				}
			}
		}
	}
	
	//襲撃失敗の場合、守ったエージェントが狼である確率を下げる（狩人の主観のときのみに使う）
	public void updateGuardedResult(Agent guardedAgent, int deadAgentsCount) {
		if(guardedAgent == null)
			return;
		if(deadAgentsCount > 0)
			return;
		
		guardedAgentsWhenAttackFailure.add(guardedAgent);
		for(RoleCombination rc: probs.getRoleCombinations()){
			if(rc.isWerewolf(guardedAgent))
				probs.update(rc, rates.get("GUARDED_WEREWOLF_WHEN_ATTACK_FAILURE"));
		}
	}
	
	public void updateTalk(Talk talk){
		Content content = new Content(talk.getText());
		
		switch (content.getTopic()) {
		case COMINGOUT:
			if(!talk.getAgent().equals(content.getTarget())) // 自分自身のCOじゃない場合は無視
				break;
			if(coMap.get(content.getTarget()) == content.getRole()) // 同じ内容の2度目以降のCOは無視
				break;
			
			Set<Agent> beforeCoSeerSet = getCoSet(Role.SEER);
			coMap.put(content.getTarget(), content.getRole());
			
			if(content.getRole() == Role.BODYGUARD){
				for(RoleCombination rc: probs.getRoleCombinations()){
					if(rc.isVillagerTeam(talk.getAgent())){
						// 村人陣営から二人目の狩人CO
						if(rc.countVillagerTeam(getCoSet(Role.BODYGUARD)) == 2)
							probs.update(rc, rates.get("2_BODYGUARD_CO_FROM_VILLAGER_TEAM"));
					}
				}
			}else if(content.getRole() == Role.SEER){
				for(RoleCombination rc: probs.getRoleCombinations()){
					if(rc.isVillagerTeam(talk.getAgent())){
						// 村人陣営から二人目の占いCO(③)
						if(rc.countVillagerTeam(getCoSet(Role.SEER)) == 2)
							probs.update(rc, rates.get("2_SEER_CO_FROM_VILLGER_TEAM"));
						// 既に人狼陣営が占いCOしている状態での初めての村人陣営占いCO(①を解除)
						if(rc.countWerewolfTeam(getCoSet(Role.SEER)) > 0 && rc.countVillagerTeam(getCoSet(Role.SEER)) == 1)
							probs.restore(rc, rates.get("ONLY_SEER_CO_FROM_WEREWOLF_TEAM"));
					}else{
						// 村人陣営が占いCOしていない状態で初めての人狼陣営占いCO(①)
						if(rc.countVillagerTeam(getCoSet(Role.SEER)) < 1 && rc.countWerewolfTeam(getCoSet(Role.SEER)) == 1)
							probs.update(rc, rates.get("ONLY_SEER_CO_FROM_WEREWOLF_TEAM"));
					}
				}
			}else if(content.getRole() == Role.VILLAGER){
				for(RoleCombination rc: probs.getRoleCombinations()){
					// 占いCOの取り消しによって村人陣営から2人の占いCOだったのが1人になった(③を解除)
					if(rc.countVillagerTeam(beforeCoSeerSet) == 2 && rc.countVillagerTeam(getCoSet(Role.SEER)) == 1)
						probs.restore(rc, rates.get("2_SEER_CO_FROM_VILLGER_TEAM"));
				}
			}else if(content.getRole() == Role.MEDIUM){
				for(RoleCombination rc: probs.getRoleCombinations()){
					if(rc.isVillagerTeam(talk.getAgent())){
						// 村人陣営から二人目の霊能CO
						if(rc.countVillagerTeam(getCoSet(Role.MEDIUM)) == 2)
							probs.update(rc, rates.get("2_MEDIUM_CO_FROM_VILLAGER_TEAM"));
						// 既に人狼陣営が霊能COしている状態での初めての村人陣営霊能CO(②を解除)
						if(rc.countWerewolfTeam(getCoSet(Role.MEDIUM)) > 0 && rc.countVillagerTeam(getCoSet(Role.MEDIUM)) == 1)
							probs.restore(rc, rates.get("ONLY_MEDIUM_CO_FROM_WEREWOLF_TEAM"));
					}else{
						// 村人陣営が霊能COしていない状態で初めての人狼陣営霊能CO(②)
						if(rc.countVillagerTeam(getCoSet(Role.MEDIUM)) < 1 && rc.countWerewolfTeam(getCoSet(Role.MEDIUM)) == 1)
							probs.update(rc, rates.get("ONLY_MEDIUM_CO_FROM_WEREWOLF_TEAM"));
					}
				}
			}else if(content.getRole() == Role.WEREWOLF){
				for(RoleCombination rc: probs.getRoleCombinations()) {
					if(!rc.isWerewolf(content.getTarget()))
						probs.update(rc, rates.get("WEREWOLF_CO_FROM_OUTSIDE_WEREWOLF"));
				}
			}else if(content.getRole() == Role.POSSESSED){
				for(RoleCombination rc: probs.getRoleCombinations()) {
					if(!rc.isPossessed(content.getTarget()))
						probs.update(rc, rates.get("POSSESSED_CO_FROM_OUTSIDE_POSSESSED"));
				}
			}

			break;
		case DIVINED:
			divinedHistory.add(new AgentTargetResult(talk.getAgent(), content.getTarget(), content.getResult()));
			for(RoleCombination rc: probs.getRoleCombinations()){
				//狂人が人狼に黒出し
				if(rc.isPossessed(talk.getAgent()) && rc.isWerewolf(content.getTarget()) && content.getResult() == Species.WEREWOLF)
					probs.update(rc, rates.get("BLACK_DIVINED_POSSESSED_TO_WEREWOLF"));
				//人狼が狂人に黒出し
				else if(rc.isWerewolf(talk.getAgent()) && rc.isPossessed(content.getTarget()) && content.getResult() == Species.WEREWOLF)
					probs.update(rc, rates.get("BLACK_DIVINED_WEREWOLF_TO_POSSESSED"));
				//人狼が人狼に黒出し
				else if(rc.isWerewolf(talk.getAgent()) && rc.isWerewolf(content.getTarget()) && content.getResult() == Species.WEREWOLF)
					probs.update(rc, rates.get("BLACK_DIVINED_WEREWOLF_TO_WEREWOLF"));	
				//村人陣営が嘘の占い
				else if(rc.isVillagerTeam(talk.getAgent())){
					if(rc.isWerewolf(content.getTarget()) && content.getResult() == Species.HUMAN){
						probs.update(rc, rates.get("FALSE_DIVINED_FROM_VILLAGER_TEAM"));
					}else if(!rc.isWerewolf(content.getTarget()) && content.getResult() == Species.WEREWOLF){
						probs.update(rc, rates.get("FALSE_DIVINED_FROM_VILLAGER_TEAM"));
					}
				}
			}			
			break;
		case IDENTIFIED:
			identifiedHistory.add(new AgentTargetResult(talk.getAgent(), content.getTarget(), content.getResult()));
			for(RoleCombination rc: probs.getRoleCombinations()){
				//村人陣営が嘘の霊能
				if(rc.isVillagerTeam(talk.getAgent())){
					if(rc.isWerewolf(content.getTarget()) && content.getResult() == Species.HUMAN){
						probs.update(rc, rates.get("FALSE_IDENTIFIED_FROM_VILLAGER_TEAM"));
					}else if(!rc.isWerewolf(content.getTarget()) && content.getResult() == Species.WEREWOLF){
						probs.update(rc, rates.get("FALSE_IDENTIFIED_FROM_VILLAGER_TEAM"));
					}
				}
			}
			break;
		case VOTE:
			todaysVotePlanMap.put(talk.getAgent(), content.getTarget());
			break;
		case OPERATOR:
			Content c = content.getContentList().get(0);
			if(c.getTopic() == Topic.VOTE)
				todaysVoteRequestMap.put(talk.getAgent(), new Pair<Integer, Agent>(talk.getIdx(), c.getTarget()));
			break;
		default:
			break;
		}		
	}
	
	/***********************************************
	 * 状況のアップデート(private)
	 */
	
	//終了条件を満たしているパターン(狼が全滅してるのにゲームが終わってないなど)を削除
	private void updateAliveAgentList(List<Agent> agents){
		aliveAgents = agents;

		for(RoleCombination rc: probs.getRoleCombinations()){
			int countWerewolf = rc.countWerewolves(aliveAgents);
			if(countWerewolf == 0)
				probs.update(rc, rates.get("NO_WEREWOLVES")); // 狼が全滅
			else if(countWerewolf >= aliveAgents.size() - countWerewolf)
				probs.update(rc, rates.get("WEREWOLVES_ARE_MORE_THAN_HUMANS")); // 狼が人間と同数以上
		}
	}
	
	private void updateVoteList(List<Vote> voteList){
		voteHistory.addAll(voteList);
		for(Vote v: voteList){
			for(RoleCombination rc: probs.getRoleCombinations()){
				// 狂人から人狼への投票
				if(rc.isPossessed(v.getAgent()) && rc.isWerewolf(v.getTarget()))
					probs.update(rc, rates.get("VOTE_POSSESSED_TO_WEREWOLF"));
				// 人狼から狂人への投票
				else if(rc.isWerewolf(v.getAgent()) && rc.isPossessed(v.getTarget()))
					probs.update(rc, rates.get("VOTE_WEREWOLF_TO_POSSESSED"));
				// 人狼から人狼への投票
				else if(rc.isWerewolf(v.getAgent()) && rc.isWerewolf(v.getTarget()))
					probs.update(rc, rates.get("VOTE_WEREWOLF_TO_WEREWOLF"));
			}
		}
	}	
	
	private void updateDeadAgentList(List<Agent> agents){
		attackedAgents.addAll(agents);
		for(RoleCombination rc: probs.getRoleCombinations())
			for(Agent agent: agents)
				if(rc.isWerewolf(agent))
					probs.update(rc, rates.get("ATTACKED_WEREWOLF")); // 人狼が襲撃される
	}
	
	/***********************************************
	 * 再計算
	 */
	
	//らしさと人数の確率を再計算
	private void calcLikenessAndProbability(){
		probs.removeZeros();
		probs.resetUpdated();
		calcLikeness();
		calcProbability();
	}
	
	private void calcLikeness(){
		werewolfLikeness = new HashMap<>();
		villagerTeamLikeness = new HashMap<>();
		for(Agent a: agents){
			werewolfLikeness.put(a, 0d);
			villagerTeamLikeness.put(a, 0d);
		}
		
		double sum = 0;
		for(RoleCombination rc: probs.getRoleCombinations()){
			double d = probs.getProbability(rc);
			sum += d;
			for(Agent a: agents){
				if(rc.isWerewolf(a)){
					werewolfLikeness.put(a, werewolfLikeness.get(a) + d);
				}else if(!rc.isPossessed(a)){
					villagerTeamLikeness.put(a, villagerTeamLikeness.get(a) + d);
				}
			}
		}
		
		for(Agent a: agents){
			werewolfLikeness.put(a, werewolfLikeness.get(a) / sum);
			villagerTeamLikeness.put(a, villagerTeamLikeness.get(a) / sum);
		}
	}
	
	private void calcProbability(){
		aliveWerewolvesNumberProbability = new HashMap<>();
		aliveVillagerTeamNumberProbability = new HashMap<>();
		alivePossessedsNumberProbability = new HashMap<>();
		
		if(aliveAgents == null)
			return;
		
		int n = aliveAgents.size();
		for(int i = 0; i <= n; i++) {
			aliveWerewolvesNumberProbability.put((i), 0d);
			aliveVillagerTeamNumberProbability.put((i), 0d);
			alivePossessedsNumberProbability.put((i), 0d);
		}

		double sum = 0;
		for(RoleCombination rc: probs.getRoleCombinations()){
			double d = probs.getProbability(rc);
			sum += d;
			
			int w = rc.countWerewolves(aliveAgents);
			aliveWerewolvesNumberProbability.put(w, aliveWerewolvesNumberProbability.get(w) + d);
			
			int p = rc.countPossesseds(aliveAgents);
			alivePossessedsNumberProbability.put(p, alivePossessedsNumberProbability.get(p) + d);
			
			int v = rc.countVillagerTeam(aliveAgents);
			aliveVillagerTeamNumberProbability.put(v, aliveVillagerTeamNumberProbability.get(v) + d);
		}
		
		for(int i = 0; i <= n; i++){
			aliveWerewolvesNumberProbability.put(i , aliveWerewolvesNumberProbability.get(i) / sum);
			alivePossessedsNumberProbability.put(i, alivePossessedsNumberProbability.get(i) / sum);
			aliveVillagerTeamNumberProbability.put(i, aliveVillagerTeamNumberProbability.get(i) / sum);
		}		
	}
	
	/***********************************************
	 * 情報を返す(らしさ・確信人数)
	 */
	
	// エージェントごとの人狼らしさ
	public Map<Agent, Double> getWerewolfLikeness() {
		if(probs.isUpdated())
			calcLikenessAndProbability();
		return werewolfLikeness;
	}

	// エージェントごとの村人側らしさ
	public Map<Agent, Double> getVillagerTeamLikeness() {
		if(probs.isUpdated())
			calcLikenessAndProbability();
		return villagerTeamLikeness;
	}
	
	// 最低でもこれだけ生きていると確信できる人狼数
	public Integer getConvincedAliveWerewolvesNumber() {
		if(probs.isUpdated())
			calcLikenessAndProbability();
		return getConvincedNumber(aliveWerewolvesNumberProbability);
	}
	
	// 最低でもこれだけ生きていると確信できる狂人数
	public Integer getConvincedAlivePossessedsNumber() {
		if(probs.isUpdated())
			calcLikenessAndProbability();
		return getConvincedNumber(alivePossessedsNumberProbability);
	}
	
	// 最低でもこれだけ生きていると確信できる村人側数
	public Integer getConvincedAliveVillagerTeamNumber() {
		if(probs.isUpdated())
			calcLikenessAndProbability();
		return getConvincedNumber(aliveVillagerTeamNumberProbability);
	}
	
	/***********************************************
	 * 情報を返す(PP用)
	 */
	
	// PPできる状況か？(人狼 + 狂人 > 村人側 と確信できるか？)
	public boolean isPowerPlayPossible() {		
		Integer w = getConvincedAliveWerewolvesNumber();
		Integer p = getConvincedAlivePossessedsNumber();
		Integer v = getConvincedAliveVillagerTeamNumber();
		if(w == null || p == null || v == null)
			return false;
		
		if(w + p > v)
			return true;
		return false;
	}
	
	// PP突入してる？(人狼か狂人がCOした？)
	public boolean isPowerPlay() {
		return (getCoSet(Role.WEREWOLF).size() + getCoSet(Role.POSSESSED).size()) > 0;
	}
	
	// 人狼だと確信できる人が直近で投票してと言っているエージェント
	public Agent getLastVoteRequestTargetByWerewolves() {
		int maxTalk = -1;
		Agent target = null;
		for(Agent a: aliveAgents) {
			if(getWerewolfLikeness().get(a) > rates.get("WEREWOLF_LIKENESS_OF_CONVICTION")) {
				if(todaysVoteRequestMap.containsKey(a)){
					int x   = todaysVoteRequestMap.get(a).getKey();
					Agent t = todaysVoteRequestMap.get(a).getValue();
					if(x > maxTalk) {
						maxTalk = x;
						target = t;
					}
				}
			}
		}
		return target;
	}
	
	/***********************************************
	 * 情報を返す(Getter)
	 */
	
	public Map<Agent, Role> getCoMap() {
		return coMap;
	}

	public Map<Agent, Role> getDefinedRoleMap() {
		return definedRoleMap;
	}

	public Map<Agent, Species> getDefinedSpeciesMap() {
		return definedSpeciesMap;
	}

	public Set<Agent> getTeamMemberWolves() {
		return teamMemberWolves;
	}

	public List<Agent> getGuardedAgentsWhenAttackFailure() {
		return guardedAgentsWhenAttackFailure;
	}

	public List<AgentTargetResult> getDivinedHistory() {
		return divinedHistory;
	}

	public List<AgentTargetResult> getIdentifiedHistory() {
		return identifiedHistory;
	}

	public List<Agent> getAttackedAgents() {
		return attackedAgents;
	}

	public List<Vote> getVoteHistory() {
		return voteHistory;
	}

	public Map<Agent, Agent> getTodaysVotePlanMap() {
		return todaysVotePlanMap;
	}

	public Map<Agent, Pair<Integer, Agent>> getTodaysVoteRequestMap() {
		return todaysVoteRequestMap;
	}
	
	/***********************************************
	 * 情報を返す(他)
	 */
	
	// その役職にCOしている人のリスト(複数回COした場合新しい方のみ)
	public Set<Agent> getCoSet(Role role){
		Set<Agent> ret = new HashSet<>();
		for(Agent a: coMap.keySet()) {
			if(coMap.get(a) == role)
				ret.add(a);
		}
		return ret;
	}	

	// 今日一番投票宣言のターゲットが多いエージェントのリスト(同数の場合があるのでリスト)
	public List<Agent> getMostVotePlanedAgents(){
		List<Agent> ret = new ArrayList<>();
		Map<Agent, Integer> count = new HashMap<>();
		for(Agent a: todaysVotePlanMap.values()){
			if(!count.containsKey(a)){
				count.put(a, 1);
			} else {
				count.put(a, count.get(a) + 1);
			}
		}
		int max = -1;
		for(int x: count.values()){
			if(max < x){
				max = x;
			}
		}
		for(Agent a: count.keySet()){
			if(count.get(a) == max){
				ret.add(a);
			}
		}
		return ret;
	}
	
	/***********************************************
	 * 他
	 */

	public void print(){
		Map<Agent, Double> w = getWerewolfLikeness();
		Map<Agent, Double> v = getVillagerTeamLikeness();
		for(Agent a: agents){
			System.out.print("[" + a.getAgentIdx() + "]\t");
			System.out.printf("%.4f\t",w.get(a));
			System.out.printf("%.4f\t",v.get(a));
			System.out.printf("%.4f\n",1d - v.get(a) - w.get(a));
		}
	}
	
	private Integer getConvincedNumber(Map<Integer, Double> probability) {
		if(probability == null)
			return 0;
		
		Integer ret = null;
		for(int x: probability.keySet()) {
			if(probability.get(x) > rates.get("NUMBER_PROBABILITY_OF_CONVICTION"))
				ret = x;
		}
		return ret;
	}
}
