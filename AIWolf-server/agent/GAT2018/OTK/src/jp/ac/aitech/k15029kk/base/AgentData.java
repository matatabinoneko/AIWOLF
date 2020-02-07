package jp.ac.aitech.k15029kk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

/**
 * 参加しているエージェントのデータ管理クラス
 * @author k15029kk
 *
 */

public class AgentData{

	public GameInfo gameInfo;
	public Agent me;
	/**エージェントID*/
	public int id;

	public int count = 0;
	/**各エージェントの点数*/
	public int score=0;

	/**1ターンにつきVOTEされた回数*/
	public int votedcount;
	/**エージェント*/
	public Agent agent;
	/**VOTEした相手*/
	public List<Agent> voteList;
	public Map<Species, Integer> DivinedResultMap;
	/**白だしした人List*/
	public List<Agent> SeerWhiteList;
	/**黒だしした人List*/
	public List<Agent> SeerBlackList;
	/**白だしした人List*/
	public List<Agent> MediumWhiteList;
	/**黒だしした人List*/
	public List<Agent> MediumBrackList;
	/**つながりがあるエージェントリスト*/
	public List<Agent> connectionList;
	/**1回もVOTEしていないエージェントリスト*/
	public List<Agent> neverVoteAgentList;

	/**狂人or潜伏人狼が擁護している可能性のある人リスト
	 * 各エージェントにおいて名前が出されていない人リスト
	 */
	public List<Agent> protectedAgentList;

	/**その相手にVOTEした回数*/
	public Map<Agent, Integer> voteCountMap;




	/**コンストラクタ*/
	public AgentData(GameInfo arg0, Agent agent){
		this.gameInfo=arg0;
		this.id = agent.getAgentIdx();
		this.agent = agent;
		me = gameInfo.getAgent();

		/**リストの初期化*/
		SeerWhiteList = new ArrayList<>();
		SeerBlackList = new ArrayList<>();
		MediumWhiteList = new ArrayList<>();
		MediumBrackList = new ArrayList<>();
		connectionList = new ArrayList<>();
		voteList = new ArrayList<>();

		protectedAgentList = new ArrayList<>();
		neverVoteAgentList = new ArrayList<>();

		/**マップの初期化*/
		voteCountMap = new HashMap<>();
		DivinedResultMap = new HashMap<>();

		//protectedAgentListの初期化
		for(Agent ag : gameInfo.getAliveAgentList()) {
			if(ag != me) {
				protectedAgentList.add(agent);
				neverVoteAgentList.add(agent);
			}
		}

		//voteCountMapの初期化
		for(Agent ag : protectedAgentList) {
			voteCountMap.put(ag, 0);
		}

		//DivinedResultMapの初期化(Resultは0 or 1)
		DivinedResultMap.put(Species.HUMAN, 0);
		DivinedResultMap.put(Species.WEREWOLF, 0);

	}

	/**
	 * 日が変わるごとに更新する処理
	 */
	public void dayStart() {

		//neverVoteAgentList(生きている)を更新
		for(Agent agent: neverVoteAgentList) {
			if(!gameInfo.getAliveAgentList().contains(agent)) {
				neverVoteAgentList.remove(agent);
			}
		}
		//protectedAgentList(生きている)を更新
		for(Agent agent: protectedAgentList) {
			if(!gameInfo.getAliveAgentList().contains(agent)) {
				protectedAgentList.remove(agent);
			}
		}
	}

	/**加点するメソッド*/
	public void upScore() {
		score++;
	}


	/**減点メソッド*/
	public void downScore() {
		score--;
	}

	/**
	 * DIVされた結果を保持
	 * @param species
	 */
	public void divinedMap(Species species) {
		if(species == Species.HUMAN) {
			DivinedResultMap.put(Species.HUMAN, 1);
		}
		else {
			DivinedResultMap.put(Species.WEREWOLF, 1);
		}
	}

	/**
	 * 占い結果をListに格納
	 * @param talk
	 * @param content
	 */

	public void divineData(Content content) {
		 //占い結果の取り込み

		if(content.getResult()==Species.HUMAN ) {
			SeerWhiteList.add(content.getTarget());
		}else if(content.getResult()==Species.WEREWOLF) {
			SeerBlackList.add(content.getTarget());
		}
	}


	/**
	 * 霊媒師COした人間の相手と結果
	 */
	public void IdentiedData(Talk talk, Content content) {
		//霊媒結果の取り込み
		if(content.getResult()==Species.HUMAN) {
			MediumWhiteList.add(content.getTarget());
		}else if(content.getResult()==Species.WEREWOLF) {
			MediumBrackList.add(content.getTarget());
		}
	}

	/**
	 * 自分が占い師の際，霊媒師によって呼び出されるメソッド
	 * つながりがあるかどうかを調べるメソッド
	 * @return
	 * agent1:霊媒師エージェント(talker)
	 * agent2:相手エージェント
	 * Species:結果
	 */
	public Agent SeerConection(Agent agent1, Agent agent2, Species species) {

		/**白だし*/
		if(species == Species.HUMAN) {
			for(Agent ag: SeerWhiteList) {
				//つながりがあればリストに追加，値を返す
				if(agent2 == ag) {
					connectionList.add(agent1);
					return agent1;
				}
				//つながりがなければリストから消去
				else{
					connectionList.remove(agent1);
				}
			}
		}
		/**黒だし*/
		else {
			for(Agent ag: SeerBlackList) {
				//つながりがあればリストに追加, 値を返す
				if(agent2 == ag) {
					connectionList.add(agent1);
					return agent1;
				}
				//つながりがなければリストから消去
				else{
					connectionList.remove(agent1);
				}
			}

		}
		return null;
	}



	/**
	 * 自分が霊媒師の際，占い師によって呼び出されるメソッド
	 * (つながりがあった際，リストに名前を追加する)
	 * @return
	 * agent:占い師エージェント(talker)
	 */

	public void MediumConection(Agent agent) {

		connectionList.add(agent);
	}

	/**
	 * 自分が霊媒師の際，占い師によって呼び出されるメソッド
	 * (つながりがなかった際，リストから名前を消去)
	 * @return
	 * agent:占い師エージェント(talker)
	 */

	public void MediumNotConection(Agent agent) {
		connectionList.remove(agent);
	}


	/**
	 * 誰にVOTEしたか管理
	 * その人にVOTEした回数もcount
	 */
	public void voteData(Agent agent) {
		voteList.add(agent);
		neverVoteAgentList.remove(agent);
		count = 1;
		if(voteCountMap.containsKey(agent)) {
			count += voteCountMap.get(agent);
		}
		voteCountMap.put(agent, count);
		//3回以上VOTEしたらリストから外す
		if(count>=3) {
			protectedAgentList.remove(agent);
		}
	}

	/**
	 * 1ターンにつきvoteされた回数を管理
	 */
	public void countVotedData() {
		votedcount++;

	}

	public int getVotedCount(Agent agent) {
		return votedcount;

	}

	//毎日votedcountはリセットされる
	public void resetVotedData() {
		votedcount=0;
	}



}
