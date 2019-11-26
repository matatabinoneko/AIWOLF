package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.*;

/**
 * 能力結果発言を管理するクラス
 * 
 * @author carlo
 *
 */

public class AbilityInfo {
	/** 各エージェントが発言した能力結果のリストのマップ */
	protected HashMap<Agent, AbilityResultManager> abilityResultMgrMap = new HashMap<Agent, AbilityResultManager>();
	protected SeerDivineTable seerDivineTable;
	protected GameInfoManager gameInfoMgr;
	/** 非村人陣営のリスト。0日目に占い結果を言うなど、破綻発言をしたら追加される。 */
	private List<Agent> nonVillaerTeamAgents;

	public AbilityInfo(GameInfoManager gameInfoMgr) {
		for (Agent agent : gameInfoMgr.getAgentList()) {
			abilityResultMgrMap.put(agent, new AbilityResultManager());
		}
		this.gameInfoMgr = gameInfoMgr;
		this.seerDivineTable = new SeerDivineTable();
		nonVillaerTeamAgents = new ArrayList<Agent>();
	}

	public void addAbilityResult(Agent speaker, Content content, int day) {
		
		if (content.getTopic() == Topic.DIVINED){
			seerDivineTable.addDivineResult(speaker, content.getTarget(), content.getResult());
		}
		abilityResultMgrMap.get(speaker).addAbilityResult(content.getTopic(), -1, day, speaker, content.getTarget(),
				content.getResult());
		checkImpossibleTalk(speaker, content, day);
	}

	/** 破綻した（非村人陣営）のエージェントリストを返す */
	public List<Agent> getNonVillagerTeamAgents() {
		return nonVillaerTeamAgents;
	}

	/** そのagentの能力行使結果をまとめたクラスを返す */
	public AbilityResultManager getAbilityResultManager(Agent agent) {
		return abilityResultMgrMap.get(agent);
	}

	public HashMap<Agent, AbilityResultManager> getAbilityResultManagerMap() {
		return abilityResultMgrMap;
	}

	/** targetの被占い結果まとめを返す */
	public DivinedResult getDivinedResult(Agent target) {
		return seerDivineTable.getDivinedResult(target);
	}

	/**
	 * targetを占った占い結果をAbilityResultのリストで返す 逐次探索しているので、たまに1msくらいかかる
	 * 
	 * @param target
	 * @return
	 */
	public List<AbilityResult> searchDivinedAbilityResults(Agent target) {
		ArrayList<AbilityResult> abilityResults = new ArrayList<AbilityResult>();

		// Targetが一致したAbilityResultを集めて返す
		for (Entry<Agent, AbilityResultManager> entry : abilityResultMgrMap.entrySet()) {
			if (gameInfoMgr.getCOInfo().getCoRole(entry.getKey()) != Role.SEER)
				continue;
			AbilityResult abilityResult = entry.getValue().getAbilityResult(target);
			if (abilityResult != null)
				abilityResults.add(abilityResult);
		}
		return abilityResults;
	}

	/**
	 * 自分以外で、生存中かつCO状態をしていない、占われ状態がtypeのエージェントのリストを返す。
	 * 
	 * @param type
	 *            NONE:グレーを返す BLACK:人狼とのみ占われた人物を返す WHITE:人間とのみ占われた人物を返す。
	 *            PANDA:２人以上の占い師から人狼、人間と占われた人物を返す
	 * @return
	 */
	public List<Agent> searchDivinedAgents(DivinedType type) {
		ArrayList<Agent> agentList = new ArrayList<Agent>();
		for (Agent agent : gameInfoMgr.getAliveAgentList()) {
			if (agent == gameInfoMgr.getMyAgent())
				continue;
			if (gameInfoMgr.getCOInfo().getCoRole(agent) == null && getDivinedResult(agent).getDivinedType() == type) {
				agentList.add(agent);
			}
		}
		return agentList;
	}

	/**
	 * seerが占ってresultだったエージェントのリストを返す
	 * 
	 * @param seer
	 * @return
	 */
	public List<Agent> searchSeerDivinedAgents(Agent seer, Species result) {
		List<Agent> agents = new ArrayList<Agent>();
		for (AbilityResult abilityResult : abilityResultMgrMap.get(seer).getAbilityResultList()) {
			if (abilityResult.getSpecies() == result)
				agents.add(abilityResult.getTarget());
		}
		return agents;
	}

	public void printAbilityResultList() {
		for (Entry<Agent, AbilityResultManager> entry : abilityResultMgrMap.entrySet()) {
			System.out.print(entry.getKey());
			entry.getValue().printList();
			System.out.println();
		}

	}

	/**
	 * 発言による破綻チェック
	 */
	private void checkImpossibleTalk(Agent speaker, Content content, int day) {
		// 破綻チェック
		if (nonVillaerTeamAgents.contains(speaker) == false) {
			// 0日目の占い,0・１日目の霊能結果発言は偽
			
			if ((content.getTopic() == Topic.DIVINED && day == 0)
					|| (content.getTopic() == Topic.IDENTIFIED && (day == 0 || day == 1))) {
				nonVillaerTeamAgents.add(speaker);
				//System.out.println("破綻 0日目の占い,0・１日目の霊能結果発言は偽 " + speaker);
				return;
			}
		}
	}
	/** 破綻チェック(updateTalkが終わったら呼ぶ) */
	public void checkNonVillagerTeam(){
		checkOutOfWerewolfCount();
	}
	/**
	 * 			// 結果＝人狼発言がゲームの人狼の数を超えていたら破綻
			//確定人狼露出人数+COしておらずspeakerに人狼判定された人物 > 人狼陣営の数
			//TODO:真霊確定による人狼の計算もできていない
	 */
	private void checkOutOfWerewolfCount(){
		ArrayList<Agent> coAgents=new ArrayList<Agent>();
		coAgents.addAll(gameInfoMgr.getCOInfo().getCOAgentList(Role.SEER, false, false));
		coAgents.addAll(gameInfoMgr.getCOInfo().getCOAgentList(Role.MEDIUM, false, false));
		for(Agent agent:coAgents){
			if(nonVillaerTeamAgents.contains(agent)) continue;
			
			//System.out.println("checkNonVillager"+utterance.getTarget()+" "+utterance.getResult());
			
			//int werewolfNum=abilityResultMgrMap.get(agent).searchAbilityResultNoCO(Species.WEREWOLF, gameInfoMgr.getCOInfo())
			//		.size() + gameInfoMgr.getNumOfWerewolfInCO();
			int werewolfNum=getNumOfOpenWerewolf(agent);
			//System.out.println("checkOutOfWerewolfCount "+agent+" num"+werewolfNum);
			if (werewolfNum > gameInfoMgr.getRoleNum(Role.WEREWOLF)) {
				nonVillaerTeamAgents.add(agent);
				//System.out.println("破綻 発言者視点の人狼数がゲームの人狼の数を超えた " + agent+" 視点人狼数:"+werewolfNum);
			}
		}
	}
	/** そのエージェントから見た露出人狼数を返します
	 * 占い師/霊能者CO:役職CO中にいる最低露出人狼数+そのエージェント視点での人狼（CO者を除く)
	 * それ以外:役職CO中にいる最低露出人狼数
     */
	public int getNumOfOpenWerewolf(Agent agent){
		Role coRole= this.gameInfoMgr.getCOInfo().getCoRole(agent);
		int werewolfNum;
		if(coRole==Role.SEER || coRole==Role.MEDIUM){
			//List<AbilityResult> list=abilityResultMgrMap.get(agent).searchAbilityResultNoCO(Species.WEREWOLF, gameInfoMgr.getCOInfo());
			//System.out.println(agent+" :"+list);
			werewolfNum=abilityResultMgrMap.get(agent).searchAbilityResultNoCO(Species.WEREWOLF, gameInfoMgr.getCOInfo())
				.size() + gameInfoMgr.getNumOfWerewolfInCO();
			//System.out.println("getNumOfOpenWerewolf openInCO:"+gameInfoMgr.getNumOfWerewolfInCO());
		}
		else{
			werewolfNum=gameInfoMgr.getNumOfWerewolfInCO();
		}
		return werewolfNum;
	}

}
