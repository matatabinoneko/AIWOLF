package com.gmail.k14.itolab.aiwolf.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.definition.CauseOfDeath;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.CoInfo;
import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.CountAgentComparator;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;

/**
 * 予想エージェント一覧
 * @author k14096kk
 *
 */
public class ForecastMap {

	OwnData ownData;
	
	/**予想エージェント一覧<br>
	 * key:エージェント <br>
	 * value:0-疑惑度,1-CO役職,2-暫定(予想)役職,3-確定役職,4-占い結果Judge,5-霊能結果Judge,6-生存or死亡,7-死因
	 * 
	 */
	HashMap<Agent, List<Object>> agentMap = new HashMap<>();
	
	/**
	 * 予想エージェント一覧作成
	 * @param ownData :データ
	 */
	public ForecastMap(OwnData ownData) {
		this.ownData = ownData;
		//一覧作成
		for(Agent agent: ownData.getGameInfo().getAgentList()) {
			List<Object> storeList = new ArrayList<>(); // Valueリスト
			storeList.add(0.0); //疑い度
			storeList.add(null);  //CO役職
			storeList.add(ownData.getGameInfo().getRoleMap().get(agent));  //暫定(予想)役職
			storeList.add(ownData.getGameInfo().getRoleMap().get(agent));  //確定役職
			storeList.add(new ArrayList<Judge>()); //占い結果
			storeList.add(new ArrayList<Judge>()); //霊媒結果
			storeList.add(CauseOfDeath.ALIVE); //状態(ALIVE or ATTACKED or EXECUTED)
			
			// 一覧作成
			agentMap.put(agent, storeList);
		}
	}
	
	/**
	 * 自分取得(OwnDataから経由している)
	 * @return 自分エージェント
	 */
	public Agent getMe() {
		return ownData.getMe();
	}
	
	/**
	 * 自分役職取得(OwnDataから経由している)
	 * @return 自分の役職
	 */
	public Role getMyRole() {
		return ownData.getMyRole();
	}
	
	/*----------------------------------------------------------------------*/
	
	/**
	 * 予想一覧のマップ取得
	 * @return 予想一覧のマップ
	 */
	public HashMap<Agent, List<Object>> getMap() {
		return agentMap;
	}
	
	/**
	 * 指定したエージェントのValueリストを取得する
	 * @param agent :指定エージェント
	 * @return valueリスト
	 */
	public List<Object> getAgentValue(Agent agent) {
		return agentMap.get(agent);
	}
	
	/**
	 * 最新のデータへ更新
	 * @param ownData :データ
	 */
	public void setDataUpdate(OwnData ownData) {
		this.ownData = ownData;
	}
	
	/*-----------------------------疑い度-----------------------------------------*/
	
	/**
	 * 指定したエージェントの疑い度を取得
	 * @param agent :指定エージェント
	 * @return 疑い度
	 */
	public double getDoubt(Agent agent) {
		return (double) getAgentValue(agent).get(0);
	}
	
	/**
	 * 指定したエージェントの疑い度を更新
	 * @param agent :指定エージェント
	 * @param doubt :更新する疑い度
	 */
	public void setDoubt(Agent agent, double doubt) {
		List<Object> storeList = this.getAgentValue(agent);
		storeList.set(0, doubt);
		agentMap.put(agent, storeList);
	}
	
	/**
	 * 全エージェントから疑い度が昇順となるエージェントリスト
	 * @return 疑い度が低い順のエージェントリスト
	 */
	public List<Agent> fewerDoubtAgentList() {
		List<Agent> agents = new ArrayList<>();
		List<CountAgent> countAgents = new ArrayList<>();
		
		for(Agent agent: this.getMap().keySet()) {
			countAgents.add(new CountAgent(this.getDoubt(agent), agent));
		}
		// 昇順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		Collections.reverse(countAgents);
		
		for(CountAgent countAgent: countAgents) {
			agents.add(countAgent.agent);
		}
		
		return agents;
	}
	
	/**
	 * 指定したエージェントリストの中から疑い度が昇順となるエージェントリスト(自分以外)
	 * @param serachAgents :エージェントリスト
	 * @return 疑い度が低い順のエージェントリスト
	 */
	public List<Agent> fewerDoubtAgentList(List<Agent> serachAgents) {
		List<Agent> agents = new ArrayList<>();
		List<CountAgent> countAgents = new ArrayList<>();
		
		List<Agent> storeList = serachAgents;
		storeList.remove(this.getMe());
		
		for(Agent agent: storeList) {
			countAgents.add(new CountAgent(this.getDoubt(agent), agent));
		}
		// 昇順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		Collections.reverse(countAgents);
		
		for(CountAgent countAgent: countAgents) {
			agents.add(countAgent.agent);
		}
		
		return agents;
	}
	
	/**
	 * 全エージェントから疑い度が降順となるエージェントリスト
	 * @return 疑い度が高い順のエージェントリスト
	 */
	public List<Agent> moreDoubtAgentList() {
		List<Agent> agents = new ArrayList<>();
		List<CountAgent> countAgents = new ArrayList<>();
		
		for(Agent agent: this.getMap().keySet()) {
			countAgents.add(new CountAgent(this.getDoubt(agent), agent));
		}
		// 降順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		
		for(CountAgent countAgent: countAgents) {
			agents.add(countAgent.agent);
		}
		
		return agents;
	}
	
	/**
	 * 指定したエージェントリストの中から疑い度が降順となるエージェントリスト(自分以外)
	 * @param serachAgents :エージェントリスト
	 * @return 疑い度が高い順のエージェントリスト
	 */
	public List<Agent> moreDoubtAgentList(List<Agent> serachAgents) {
		List<Agent> agents = new ArrayList<>();
		List<CountAgent> countAgents = new ArrayList<>();
		
		List<Agent> storeList = serachAgents;
		storeList.remove(this.getMe());
		
		for(Agent agent: storeList) {
			countAgents.add(new CountAgent(this.getDoubt(agent), agent));
		}
		// 降順ソート
		Collections.sort(countAgents, new CountAgentComparator());
		
		for(CountAgent countAgent: countAgents) {
			agents.add(countAgent.agent);
		}
		
		return agents;
	}
	
	/**
	 * 疑い度加算
	 * @param agent :更新するエージェント
	 * @param value :加算値
	 */
	public void plusDoubt(Agent agent, double value) {
		double doubt = this.getDoubt(agent);
		doubt = calDecimal(doubt + value);
		this.setDoubt(agent, doubt);
	}
	
	/**
	 * 疑い度減算
	 * @param agent :更新するエージェント
	 * @param value :減算値
	 */
	public void minusDoubt(Agent agent, double value) {
		double doubt = this.getDoubt(agent);
		doubt = calDecimal(doubt - value);
		this.setDoubt(agent, doubt);
	}
	
	/*-----------------------------CO役職-----------------------------------------*/
	
	/**
	 * 指定エージェントのCO情報を取得
	 * @param agent :エージェント
	 * @return CO情報
	 */
	public CoInfo getComingoutInfo(Agent agent) {
		if(agent==null) return null;
		return (CoInfo) getAgentValue(agent).get(1);
	}
	
	/**
	 * 指定したエージェントのCO役職更新
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public void setComingoutRole(Talk talk, Content content) {
		Agent coAgent = content.getSubject();
		if(coAgent==null) {
			coAgent = talk.getAgent();
		}
		List<Object> storeList = this.getAgentValue(coAgent);
		storeList.set(1, new CoInfo(talk, content));
		agentMap.put(coAgent, storeList);
	}

	/**
	 * 指定エージェントのCO役職を取得
	 * @param agent :指定エージェント
	 * @return 役職
	 */
	public Role getComingoutRole(Agent agent) {
		CoInfo coInfo = getComingoutInfo(agent);
		if(coInfo==null) return null;
		return coInfo.getRole();
	}
	
	/**
	 * 指定エージェントのCO日付を取得
	 * @param agent :指定エージェント
	 * @return CO日付(COしていなければ100を返す)
	 */
	public int getComingoutDay(Agent agent) {
		CoInfo coInfo = getComingoutInfo(agent);
		if(coInfo==null) return 100;
		return coInfo.getDay();
	}
	
	/**
	 * 指定エージェントのCOした会話IDを取得
	 * @param agent :指定エージェント
	 * @return COタした会話ーン
	 */
	public int getComingoutId(Agent agent) {
		CoInfo coInfo = getComingoutInfo(agent);
		if(coInfo==null) return 100;
		return coInfo.getId();
	}
	
	/**
	 * 指定エージェントのCOターンを取得
	 * @param agent :指定エージェント
	 * @return COターン
	 */
	public int getComingoutTurn(Agent agent) {
		CoInfo coInfo = getComingoutInfo(agent);
		if(coInfo==null) return 100;
		return coInfo.getTurn();
	}
	
	/**
	 * 指定役職がCO役職に含まれているか
	 * @param role :指定役職
	 * @return 含まれていればtrue,いなければfalse
	 */
	public boolean containComingoutRole(Role role) {
		for(Agent agent: agentMap.keySet()) {
			if(this.getComingoutRole(agent)==role) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定したCO役職のエージェントのリストを取得
	 * @param role :指定役職
	 * @return 指定役職のCOをしたエージェントリスト
	 */
	public List<Agent> getComingoutRoleAgentList(Role role) {
		List<Agent> agentList = new ArrayList<>();
		for(Agent agent: agentMap.keySet()) {
			if(Check.isRole(getComingoutRole(agent), role)) {
				agentList.add(agent);
			}
		}
		return agentList;
	}
	
	/**
	 * 指定エージェントの中から指定役職のエージェントリストを取得
	 * @param agentList :エージェントリスト
	 * @param role :役職
	 * @return 役職エージェントリスト
	 */
	public List<Agent> comingoutRoleAgentList(List<Agent> agentList, Role role) {
		List<Agent> agents = new ArrayList<>();
		for(int i=0; i<agentList.size(); i++) {
			if(Check.isRole(this.getComingoutRole(agentList.get(i)), role)) {
				agents.add(agentList.get(i));
			}
		}
		return agents;
	}
	
	/**
	 * 指定役職をCOしているエージェント数を取得
	 * @param role :指定役職
	 * @return 役職COしているエージェントの人数を取得，いなければ0
	 */
	public int countComingoutRole(Role role) {
		List<Agent> agents = getComingoutRoleAgentList(role);
		if(agents.isEmpty()) {
			return 0;
		}
		return agents.size();
	}
	
	/**
	 * 自分と同じ役職をCOしたエージェントのリストを取得する(自分を除く)
	 * @return 自分と同じ役職をCOしたエージェントリスト
	 */
	public List<Agent> getComingoutRoleOtherAgentList() {
		// 自分と同じ役職をCOしたエージェントリスト作成
		List<Agent> agentList = this.getComingoutRoleAgentList(this.getComingoutRole(ownData.getMe()));
		agentList.remove(ownData.getMe());
		return agentList;
	}
	
	/**
	 * 指定日付に指定役職をCOしたエージェントのリスト
	 * @param role :役職
	 * @param day :日付
	 * @return エージェントリスト
	 */
	public List<Agent> comingoutRoleDayAgentList(Role role, int day) {
		List<Agent> coAgentList = new ArrayList<>();
		for(Agent coAgent: this.getMap().keySet()) {
			// 指定日付中に指定役職COしたエージェント
			if(Check.isRole(this.getComingoutRole(coAgent), role) && Check.isNum(this.getComingoutDay(coAgent), day)) {
				coAgentList.add(coAgent);
			}
		} 
		return coAgentList;
	}
	
	/**
	 * COしている生存エージェントのリスト
	 * @return COした生存エージェントリスト
	 */
	public List<Agent> comingoutAliveAgentList() {
		List<Agent> coAgentList = new ArrayList<>();
		for(Agent coAgent: this.getMap().keySet()) {
			if(this.getComingoutInfo(coAgent)!=null && this.getStatus(coAgent)==Status.ALIVE) {
				coAgentList.add(coAgent);
			}
		}
		return coAgentList;
	}
	
	/**
	 * COしているエージェントのリスト取得
	 * @return COしているエージェントのリスト
	 */
	public List<Agent> getComingoutAgentList() {
		List<Agent> coAgents = new ArrayList<>();
		for(Agent agent: this.getMap().keySet()) {
			if(Check.isNotNull(this.getComingoutRole(agent))) {
				coAgents.add(agent); 
			}
		}
		return coAgents;
	}
	
	/*-----------------------------暫定役職-----------------------------------------*/
	
	/**
	 * 指定したエージェントの暫定役職を取得
	 * @param agent :指定エージェント
	 * @return 暫定役職
	 */
	public Role getProvRole(Agent agent) {
		if(agent==null) return null;
		return (Role) getAgentValue(agent).get(2);
	}
	
	/**
	 * 指定したエージェントの暫定役職更新
	 * @param agent :指定エージェント
	 * @param role :暫定役職
	 */
	public void setProvRole(Agent agent, Role role) {
		if(agent==null) {
			return;
		}
		List<Object> storeList = this.getAgentValue(agent);
		storeList.set(2, role);
		agentMap.put(agent, storeList);
	}
	
	/**
	 * 指定した暫定役職のエージェントのリストを取得
	 * @param role :指定役職
	 * @return 指定役職のCOをしたエージェントリスト
	 */
	public List<Agent> getProvRoleAgentList(Role role) {
		List<Agent> agentList = new ArrayList<>();
		for(Agent agent: agentMap.keySet()) {
			if(getProvRole(agent) == role) {
				agentList.add(agent);
			}
		}
		return agentList;
	}
	
	/**
	 * 渡されたエージェントリストの中から指定役職のエージェントを返す<br>
	 * (現在，番号が一番若いエージェントが返される)
	 * @param selectList :エージェントリスト
	 * @param role :暫定役職
	 * @return エージェント
	 */
	public Agent getProvAgent(List<Agent> selectList, Role role) {
		List<Agent> agentList = this.getProvAgentList(selectList, role);
		
		//該当者いないならばnull
		if(agentList.isEmpty()) {
			return null;
		}
		return agentList.get(0);
	}
	
	/**
	 * 渡されたエージェントリストの中から指定役職のエージェントリストを返す
	 * @param selectList :エージェントリスト
	 * @param role :暫定役職
	 * @return エージェントリスト
	 */
	public List<Agent> getProvAgentList(List<Agent> selectList, Role role) {
		List<Agent> agentList = new ArrayList<>();
		for(int i=0; i<selectList.size(); i++) {
			if(this.getProvRole(selectList.get(i))==role) {
				agentList.add(selectList.get(i));
			}
		}
		return agentList;
	}
	
	/**
	 * 指定役職の暫定役職をもつエージェントを取得する
	 * @param role :指定役職
	 * @return 暫定役職エージェント(ランダム)
	 */
	public Agent getProvAgent(Role role) {
		List<Agent> agentList = getProvRoleAgentList(role);
		if(agentList.isEmpty()) {
			return null;
		}
		// ランダムにリストから選択して返す
		return RandomSelect.randomAgentSelect(agentList);
	}

	/**
	 * 指定役職が暫定役職に含まれているか
	 * @param role :指定役職
	 * @return 含まれていればtrue,いなければfalse
	 */
	public boolean containProvRole(Role role) {
		for(Agent agent: agentMap.keySet()) {
			if(this.getProvRole(agent)==role) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 役職人数と暫定役職人数から割り当てられていない役職の人数マップを取得
	 * @param roleNumMap :役職人数
	 * @return 暫定役職にない役職人数マップ
	 */
	public Map<Role, Integer> getRemainProvRoleNum(Map<Role, Integer> roleNumMap) {
		Map<Role, Integer> remainRoleMap = new HashMap<>();
		for(Role role: roleNumMap.keySet()) {
			if(roleNumMap.get(role)>0) {
				for(Agent agent: agentMap.keySet()) {
					if(this.getProvRole(agent)==role) {
						roleNumMap.put(role, roleNumMap.get(role)-1);	
					}
				}
			}
		}
		remainRoleMap = roleNumMap;

		return remainRoleMap;
	}
	
	/**
	 * 残り暫定役職をランダムに与えられたエージェントに設定
	 * @param roleNumMap :残り役職マップ
	 * @param agentList :設定するエージェントリスト
	 */
	public void setRemainRoleRandom(Map<Role, Integer> roleNumMap, List<Agent> agentList) {
		//残り役職人数
		Map<Role, Integer> remainRoleMap = getRemainProvRoleNum(roleNumMap);
		for(Role role: remainRoleMap.keySet()) {
			while(remainRoleMap.get(role)>0 && !agentList.isEmpty()) {
				Agent agent = RandomSelect.randomAgentSelect(agentList);
				this.setProvRole(agent, role);
				remainRoleMap.put(role, remainRoleMap.get(role)-1);
				agentList.remove(agent);
			}
		}
	}
	
	/**
	 * 村人と狂人以外の残り暫定役職を疑い度をもとに設定
	 * @param roleNumMap :残り役職マップ
	 * @param agentList :設定するエージェントリスト
	 */
	public void setRemainDoubtRole(Map<Role, Integer> roleNumMap, List<Agent> agentList) {
		//残り役職人数
		Map<Role, Integer> remainRoleMap = getRemainProvRoleNum(roleNumMap);
		for(Role role: remainRoleMap.keySet()) {
			// 村人と狂人以外
			if(!Check.isRole(role, Role.VILLAGER) || !Check.isRole(role, Role.POSSESSED)) {
				// 狼は疑い度が高い方から
				if( Check.isRole(role, Role.WEREWOLF)) {
//					while(!agentList.isEmpty() && remainRoleMap.get(role)>0) {
//						Agent agent = this.moreDoubtAgentList(agentList).get(0);
//						this.setProvRole(agent, role);
//						remainRoleMap.put(role, remainRoleMap.get(role)-1);
//						agentList.remove(agent);
//					}
				}else { //その他役職は低い方から
					while(!agentList.isEmpty() && remainRoleMap.get(role)>0) {
						Agent agent = this.fewerDoubtAgentList(agentList).get(0);
						this.setProvRole(agent, role);
						remainRoleMap.put(role, remainRoleMap.get(role)-1);
						agentList.remove(agent);
					}
				}
			}
		}
	}
	
	/**
	 * 残り役職一覧から指定役職をエージェントリストの先頭から割当て
	 * @param roleNumMap :役職一覧
	 * @param agentList :エージェントリスト
	 * @param role :割当て役職
	 * @return 残りエージェントリスト
	 */
	public List<Agent> setRemainRoleOrder(Map<Role, Integer> roleNumMap, List<Agent> agentList, Role role) {
		//残り役職人数
		Map<Role, Integer> remainRoleMap = getRemainProvRoleNum(roleNumMap);
		//割り当てるエージェントリスト
		List<Agent> agents = agentList;
		
		int remain = remainRoleMap.get(role);
		while(remain>0 && !agents.isEmpty()) {
			this.setProvRole(agents.get(0), role);
			agents.remove(agents.get(0));
			remain--;
		}
		return agents;
	}
	
	/*-----------------------------確定役職-----------------------------------------*/
	
	/**
	 * 指定したエージェントの確定役職を取得
	 * @param agent :指定エージェント
	 * @return 確定役職
	 */
	public Role getConfirmRole(Agent agent) {
		if(agent==null) return null;
		return (Role) getAgentValue(agent).get(3);
	}
	
	/**
	 * 指定したエージェントの確定役職更新
	 * @param agent :指定エージェント
	 * @param role :確定役職
	 */
	public void setConfirmRole(Agent agent, Role role) {
		if(agent==null) {
			return;
		}
		List<Object> storeList = this.getAgentValue(agent);
		storeList.set(3, role);
		agentMap.put(agent, storeList);
	}
	
	/**
	 * 指定した確定役職のエージェントのリストを取得
	 * @param role :指定役職
	 * @return 指定役職のCOをしたエージェントリスト
	 */
	public List<Agent> getConfirmRoleAgentList(Role role) {
		List<Agent> agentList = new ArrayList<>();
		for(Agent agent: agentMap.keySet()) {
			if(Check.isRole(getConfirmRole(agent), role)) {
				agentList.add(agent);
			}
		}
		return agentList;
	}
	
	/**
	 * 指定役職が確定役職に含まれているか
	 * @param role :指定役職
	 * @return 含まれていればtrue,いなければfalse
	 */
	public boolean containConfirmRole(Role role) {
		for(Agent agent: agentMap.keySet()) {
			if(Check.isRole(this.getConfirmRole(agent), role)) {
				return true;
			}
		}
		return false;
	}
	
	/*-----------------------------占い結果-----------------------------------------*/
	
	/**
	 * 対象エージェントの占い結果リストを取得
	 * @param agent :占い師
	 * @return 占い発言のリスト
	 */
	@SuppressWarnings("unchecked")
	public List<Judge> getDivineJudgeList(Agent agent) {
		return (List<Judge>) getAgentValue(agent).get(4);
	}
	
	/**
	 * 対象エージェントの占い結果を更新
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public void setDivineJudgeList(Talk talk, Content content) {
		Judge judge = new Judge(talk.getDay(), talk.getAgent(), content.getTarget(), content.getResult());
		this.getDivineJudgeList(talk.getAgent()).add(judge);
	}
	
	/**
	 * 指定した占い師が指定日に占ったJudgeを取得する
	 * @param agent :占い師
	 * @param day :日付
	 * @return 存在すればJudge，なければnull
	 */
	public Judge getDivineJudge(Agent agent, int day) {
		for(Judge judge: this.getDivineJudgeList(agent)) {
			if(judge.getDay()==day) {
				return judge;
			}
		}
		return null;
	}
	
	/**
	 * 指定した日付の占いJudgeをリストにして返す
	 * @param day :日付
	 * @return 指定日付に占い結果発言をしたJudgeリスト
	 */
	public List<Judge> getDivineJudgeDay(int day) {
		List<Judge> judgeList =new ArrayList<>();
		// 全エージェント確認
		for(Agent agent: this.getMap().keySet()) {
			// 占い結果を取得
			List<Judge> storeList = this.getDivineJudgeList(agent);
			// 占い師結果から日付があうものをリストに格納
			for(Judge judge: storeList) {
				if(judge.getDay()==day) {
					judgeList.add(judge);
				}
			}
		}
		return judgeList;
	}
	
	/**
	 * 指定したエージェントを占ったJudgeをリストにして返す
	 * @param target :占い対象
	 * @return 指定エージェントを占い対象したJudgeリスト
	 */
	public List<Judge> getDivineJudgeTarget(Agent target) {
		List<Judge> judgeList = new ArrayList<>();
		// 全エージェント確認
		for(Agent agent: this.getMap().keySet()) {
			// 占い結果を取得
			List<Judge> storeList = this.getDivineJudgeList(agent);
			// 占い結果から指定エージェントを占ったものをリストに格納
			for(Judge judge: storeList) {
				if(Check.isAgent(judge.getTarget(), target)) {
					judgeList.add(judge);
				}
			}
		}
		return judgeList;
	}
	
	/**
	 *占い結果が指定種族であるJudgeのリストを取得
	 * @param species: 指定種族
	 * @return Judgeのリスト
	 */
	public List<Judge> getDivineJudgeSpecies(Species species) {
		List<Judge> judgeList = new ArrayList<>();
		// 全エージェント確認
		for(Agent agent: this.getMap().keySet()) {
			// 占い結果を取得
			List<Judge> storeList = this.getDivineJudgeList(agent);
			// 占い結果から指定種族であるものをリストに格納
			for(Judge judge: storeList) {
				if(Check.isSpecies(judge.getResult(), species)) {
					judgeList.add(judge);
				}
			}
		}
		return judgeList;
	}
	
	/**
	 * 指定日付の占いで，結果が指定種族である占いリストを取得
	 * @param day :日付
	 * @param spe :種族
	 * @return 占いリスト
	 */
	public List<Judge> getDivineJudgeList(int day, Species spe) {
		List<Judge> judgeList = new ArrayList<>();
		List<Judge> dayList = this.getDivineJudgeDay(day);
		List<Judge> speList = this.getDivineJudgeSpecies(spe);
		
		for(Judge judge: dayList) {
			if(speList.contains(judge)) {
				HandyGadget.addList(judgeList, judge);
			}
		}
		
		return judgeList;
	}
	
	/*-----------------------------霊媒結果-----------------------------------------*/
	
	/**
	 * 対象エージェントの霊媒結果リストを取得
	 * @param agent :対象エージェント
	 * @return 霊媒結果リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Judge> getIdentJudgeList(Agent agent) {
		return (List<Judge>) getAgentValue(agent).get(5);
	}
	
	/**
	 * 対象エージェントの霊媒結果を更新
	 * @param talk :会話
	 * @param content :コンテンツ
	 */
	public void setIdentJudgeList(Talk talk, Content content) {
		Judge judge = new Judge(talk.getDay(), talk.getAgent(), content.getTarget(), content.getResult());
		this.getIdentJudgeList(talk.getAgent()).add(judge);
	}
	
	/**
	 * 指定した占い師が指定日に占ったJudgeを取得する
	 * @param agent :霊媒師
	 * @param day :日付
	 * @return 存在すればJudge，なければnull
	 */
	public Judge getIdentJudge(Agent agent, int day) {
		for(Judge judge: this.getIdentJudgeList(agent)) {
			if(judge.getDay()==day) {
				return judge;
			}
		}
		return null;
	}
	
	/**
	 * 指定した日付の占いJudgeをリストにして返す
	 * @param day :日付
	 * @return 指定日付に占い結果発言をしたJudgeリスト
	 */
	public List<Judge> getIdentJudgeDay(int day) {
		List<Judge> judgeList =new ArrayList<>();
		for(Agent agent: this.getMap().keySet()) {
			List<Judge> storeList = this.getIdentJudgeList(agent);
			for(Judge judge: storeList) {
				if(judge.getDay()==day) {
					judgeList.add(judge);
				}
			}
		}
		return judgeList;
	}
	
	/**
	 * 指定したエージェントを占ったJudgeをリストにして返す
	 * @param target :霊媒師対象
	 * @return 指定エージェントを占い対象したJudgeリスト
	 */
	public List<Judge> getIdentJudgeTarget(Agent target) {
		List<Judge> judgeList = new ArrayList<>();
		for(Agent agent: this.getMap().keySet()) {
			List<Judge> storeList = this.getIdentJudgeList(agent);
			for(Judge judge: storeList) {
				if(Check.isAgent(judge.getTarget(), target)) {
					judgeList.add(judge);
				}
			}
		}
		return judgeList;
	}
	
	/*-----------------------------状態(死因)-----------------------------------------*/
	
	/**
	 * 指定したエージェントの状態(死因)を取得
	 * @param agent :指定エージェント
	 * @return　エージェントの状態(死因)
	 */
	public CauseOfDeath getCauseOfDeath(Agent agent) {
		if(Check.isNull(agent)) {
			return null;
		}
		return (CauseOfDeath) getAgentValue(agent).get(6);
	}

	/**
	 * 指定したエージェントの状態を取得
	 * @param agent :指定エージェント
	 * @return エージェントの状態(ALIVE or DEAD)
	 */
	public Status getStatus(Agent agent) {
		CauseOfDeath state = this.getCauseOfDeath(agent);
		if(state == CauseOfDeath.ALIVE) {
			return Status.ALIVE;
		}
		return Status.DEAD;
	}
	
	/**
	 * 指定エージェントの状態を更新
	 * @param agent :指定エージェント
	 * @param state :状態
	 */
	public void setStatus(Agent agent, CauseOfDeath state) {
		List<Object> storeList = this.getAgentValue(agent);
		storeList.set(6, state);
		agentMap.put(agent, storeList);
	}
	
	/**
	 * 指定エージェントを被襲撃状態へ
	 * @param agent :指定エージェント
	 */
	public void setAttackedStatus(Agent agent) {
		if(agent!=null){
			this.setStatus(agent, CauseOfDeath.ATTACKED);
		}
	}
	
	/**
	 * 指定エージェントを被処刑状態へ
	 * @param agent :指定エージェント
	 */
	public void setExecutedStatus(Agent agent) {
		if(agent!=null){
			this.setStatus(agent, CauseOfDeath.EXECUTED);
		}
	}
	
	/*-----------------------------その他-----------------------------------------*/
	
	/**
	 * 残り役職人数の合計数を取得
	 * @param roleNumMap :役職人数一覧
	 * @return 合計数
	 */
	public int getRoleNumSum(Map<Role, Integer> roleNumMap) {
		int sum = 0;
		for(Role role: roleNumMap.keySet()) {
			sum = roleNumMap.get(role);
		}
		return sum;
	}
	
	/**
	 * 生存者における暫定役職の人数をマップで取得する
	 * @return 暫定役職と人数マップ
	 */
	public Map<Role, Integer> getProvRoleAliveNumMap() {
		Map<Role, Integer> proveMap = new HashMap<>();
		for(Agent agent: agentMap.keySet()) {
			Role role = this.getProvRole(agent);
			if(this.getStatus(agent)==Status.ALIVE) {
				if(role!=null) {
					if(!agentMap.containsKey(role)) {
						proveMap.put(role, 1);
					}else {
						proveMap.put(role, proveMap.get(role)+1);
					}
				}
			}
		}
		return proveMap;
	}
	
	/**
	 * 生存者リストの中から指定CO役職のエージェントを返す(自分以外)
	 * @param aliveAgentList :生存者リスト
	 * @param role :CO役職
	 * @return CO役職のエージェント
	 */
	public Agent getComingoutRoleAliveAgent(List<Agent> aliveAgentList, Role role) {
		List<Agent> agentList = new ArrayList<>();
		for(int i=0; i<aliveAgentList.size(); i++) {
			if(this.getComingoutRole(aliveAgentList.get(i))==role && aliveAgentList.get(i)!=ownData.getMe()) {
				agentList.add(aliveAgentList.get(i));
			}
		}
		
		//該当者いないならばnull
		if(agentList.isEmpty()) {
			return null;
		}
		return agentList.get(0);
	}
	
	/*-------------------------------計算Util-------------------------------------*/
	
	/**
	 * 少数整形
	 * @param value :値
	 * @return 整形済み値
	 */
	private double calDecimal(double value) {
		BigDecimal bd = new BigDecimal(value);
		BigDecimal bd2 = bd.setScale(3, BigDecimal.ROUND_HALF_UP); 
		return bd2.doubleValue();
	}
}
