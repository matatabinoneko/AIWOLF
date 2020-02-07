package com.gmail.k14.itolab.aiwolf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.CountAgentComparator;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;

/**
 * エージェントの会話を保持するクラス
 * @author k14096kk
 *
 */
public class TalkDataBase {
	
	/**エージェントごとの会話を保持するマップ*/
	 private Map<Agent, List<Talk>> agentTalkListMap;
	/**ゲーム全体のエージェントごとの会話を保持するマップ*/
	 private Map<Agent, List<List<Talk>> > allTalkListMap;
	/**日付をキーとした会話マップ*/
	 private Map<Integer, List<Talk>> dayTalkListMap;
	 
	public TalkDataBase(List<Agent> agentList) {
		agentTalkListMap = new HashMap<>();
		for (Agent agent : agentList) {
			agentTalkListMap.put(agent, new ArrayList<>());
		}
		allTalkListMap = new HashMap<>();
		dayTalkListMap = new HashMap<>();
	}
	
	
	/**
	 * 会話保存のデータベース作成
	 *@param agentList : 全エージェント
	*/
	public void createTalkDataBase(List<Agent> agentList) {
		agentTalkListMap = new HashMap<>();
		for (Agent agent : agentList) {
			agentTalkListMap.put(agent, new ArrayList<>());
		}
		allTalkListMap = new HashMap<>();
		dayTalkListMap = new HashMap<>();
	}
	
	/**
	 * 会話追加
	 * @param day :日付
	 * @param agent :エージェント
	 * @param talk :会話
	 */
	public void addTalk(int day, Agent agent, Talk talk) {
		/**エージェント版マップに追加*/
		//指定エージェントの会話リスト取り出し
		List<Talk> talkList = agentTalkListMap.get(agent);
		//会話リスト追加
		talkList.add(talk);
		//会話追加
		agentTalkListMap.put(agent, talkList);
		
		/*日付版マップに追加*/
		List<Talk> storeTalk = new ArrayList<>();
		if(dayTalkListMap.containsKey(day)) {
			storeTalk = dayTalkListMap.get(day);
		}
		storeTalk.add(talk);
		dayTalkListMap.put(day, storeTalk);
	}
	
	/**
	 * 一日の会話マップを全体会話マップにリストそのまま登録して初期化
	 */
	public void resetTalkData() {
		//一日の会話マップからエージェントごとの会話リストを取り出す
		for(Agent agent: agentTalkListMap.keySet()){
			List<List<Talk>> storeList = new ArrayList<>();
			if(allTalkListMap.containsKey(agent)){
				//登録してあるエージェントの全体会話リストを取り出す
				storeList = allTalkListMap.get(agent);
			}
			storeList.add(agentTalkListMap.get(agent));
			allTalkListMap.put(agent, storeList);
			agentTalkListMap.put(agent, new ArrayList<>());
		}
		// debug();
	}
	
	/**
	 * 登録データ全削除
	 */
	public void clearAllTalkData() {
		agentTalkListMap.clear();
		allTalkListMap.clear();
	}
	
	/**
	 * 指定エージェントの一日の会話を取得する
	 * @param agent :指定エージェント
	 * @return 一日の会話リスト
	 */
	public List<Talk> getAgentTalkList(Agent agent) {
		return agentTalkListMap.get(agent);
	}
	
	/**
	 * 指定エージェントの全ての会話を取得する
	 * @param agent :指定エージェント
	 * @return 全ての会話リストが入ったリスト
	 */
	public List<List<Talk>> getAgentAllTalkList(Agent agent) {
		//if(allTalkListMap.isEmpty()) return null;
		return allTalkListMap.get(agent);
	}
	
	/**
	 * 指定エージェントの指定日付の会話リストを取得する
	 * @param agent :指定エージェント
	 * @param day :指定日付
	 * @return 会話リスト
	 */
	public List<Talk> getAgentDayTalkList(Agent agent, int day) {
		//if(getAgentAllTalkList(agent)==null) return null;
		if(getAgentAllTalkList(agent).size()<=(day+1)) return null;
		return getAgentAllTalkList(agent).get(day+1);
	}
	
	/** 
	 * 指定トピックの会話をしたエージェントリストを返す
	 * @param topic : 指定topic
	 * @return　指定Topicのエージェントリスト
	 */
	public List<Agent> getTopicAgentList(Topic topic) {
		List<Agent> agentList = new ArrayList<>();
		
		for(Agent agent: agentTalkListMap.keySet()){
			List<Talk> talkList = agentTalkListMap.get(agent);
			//取得した会話リストから指定トピックの会話したエージェント取得
			for(Talk talk: talkList){
				Content content = new Content(talk.getText());
				Topic t = content.getTopic();
				//指定Topicと同じならばエージェントをリストに追加
				if(t==topic){
					agentList.add(agent);
				}
			}
		}
		
		return agentList;
	}
	
	/** 
	 * 指定トピックの会話リストを返す
	 * @param topic :指定topic
	 * @param day :日付期限
	 * @return　指定Topicの会話リスト
	 */
	public List<Talk> getTopicTalkList(Topic topic, int day) {
		List<Talk> topicTalkList = new ArrayList<>();
		for(Agent agent: allTalkListMap.keySet()){ //エージェント数だけまわす
			// 引数の日付まで会話を見る
			for(int i=0; i<=day; i++) { 
				List<Talk> talkList = getAgentDayTalkList(agent, i);
				if(talkList!=null) {
					//取得した会話リストから指定トピックの会話したエージェント取得
					for(Talk talk: talkList){
						Content content = new Content(talk.getText());
						Topic t = content.getTopic();
						//指定Topicと同じならばエージェントをリストに追加
						if(t==topic){
							topicTalkList.add(talk);
						}
					}
				}
			}
		}
		return topicTalkList;
	}
	
	/**
	 * 指定役職がCO会話リスト内にあるかどうか
	 * @param talkList :会話リスト
	 * @param role :役職
	 * @return 指定役職をCOしたエージェントのリスト
	 */
	public List<Agent> getComingoutRoleAgent(List<Talk> talkList, Role role) {
		List<Agent >agentList = new ArrayList<>();
		for(int i=0; i<talkList.size(); i++) {
			Talk talk = talkList.get(i);
			Content content = new Content(talk.getText());
			if(content.getRole()==role) {
				agentList.add(talk.getAgent());
			}
		}
		return agentList;
	}
	
	/** 
	 * 占い発言をしたエージェントごとの結果マップを返す
	 * @return　占い結果マップ
	 */
	public HashMap<Agent, Map<Agent, Species>> getDivineMap() {
		//エージェントごとの占い結果マップ
		HashMap<Agent, Map<Agent, Species>> divineResultMap = new HashMap<>();
		
		for(Agent agent: agentTalkListMap.keySet()){
			//エージェントの会話リスト
			List<Talk> talkList = agentTalkListMap.get(agent);	
			//占い発言したかどうか
			boolean isDivine = false;
			//値格納用
			Map<Agent, Species> storeResultMap = new HashMap<>();
			
			//会話の数だけ繰り返す
			for(Talk talk: talkList){
				Content content = new Content(talk.getText());
				if(content.getTopic()==Topic.DIVINED){
					Agent target = content.getTarget();
					Species species = content.getResult();
					
					storeResultMap.put(target, species);
					isDivine = true;
				}
			}
			
			//占い発言したならマップに追加
			if(isDivine){
				divineResultMap.put(agent, storeResultMap);
			}
		}
		
		return divineResultMap;
	}
	
	
	/**
	 * 指定したAgentとTopicの会話Mapを返す
	 * @param topic : 指定Topic
	 * @return エージェントと会話のマップを返す
	 */
	public Map<Agent, List<Talk>> getTopicTalkMap(Topic topic) {
		Map<Agent, List<Talk>> topicTalkMap = new HashMap<>();
		
		for(Agent agent: agentTalkListMap.keySet()){
			List<Talk> talkList = agentTalkListMap.get(agent);
			//取得した会話リストから指定トピックの会話したエージェント取得
			for(Talk talk: talkList){
				Content content = new Content(talk.getText());
				Topic t = content.getTopic();
				//指定Topicと同じならば会話をそのまま取り出し,エージェントと一緒にマップに追加
				if(t==topic){
					//Mapに格納する会話リスト
					List<Talk> storeTalkList = new ArrayList<>();
					if(topicTalkMap.containsKey(agent)){
						storeTalkList = topicTalkMap.get(agent);
					}
					//会話追加
					storeTalkList.add(talk);
					
					topicTalkMap.put(agent, storeTalkList);
				}
			}
		}
		return topicTalkMap;
	} 
	
	/*-----------------------------発言回数---------------------------------------*/
	
	/**
	 * 指定エージェントの一日の発話回数を返す
	 * @param agent :指定エージェント
	 * @return 発話回数
	 */
	public int countDayTalk(Agent agent) {
		int talkCount = 0;
		for(int i=0; i<agentTalkListMap.get(agent).size(); i++) {
			for(Talk talk :agentTalkListMap.get(agent)) {
				Content content = new Content(talk.getText());
				if(content.getTopic()!=Topic.SKIP && content.getTopic()!=Topic.OVER) {
					talkCount++;
				}
			}
		}
		return talkCount;
	}
	
	/**
	 * 指定エージェントの指定日付の発話数を取得する
	 * @param agent :指定エージェント 
	 * @param day :日付
	 * @return　発話数
	 */
	public int countDayTalk(Agent agent, int day) {
		int talkCount = 0;
		List<Talk> talkList = getAgentDayTalkList(agent, day);
		for(int i=0; i<talkList.size(); i++) {
			Content content = new Content(talkList.get(i).getText());
			if(content.getTopic()!=Topic.SKIP && content.getTopic()!=Topic.OVER) {
				talkCount++;
			}
		}
		return talkCount;
	}
	
	/**
	 * 発言回数が最も多いエージェントを返す
	 * @return 1日の発言回数が最も多いエージェント
	 */
	public Agent countGreatestTalk() {
		int talkCount = 0;
		Agent greatestAgent = null;
		
		for(Agent agent: agentTalkListMap.keySet()) { 
			if(talkCount<=countDayTalk(agent)) {
				greatestAgent = agent;
			}
		}
		return greatestAgent;
	}
	
	/**
	 * 発言回数が最も少ないエージェントを返す
	 * @return 1日の発言回数が最も少ないエージェント
	 */
	public Agent countLatestTalk() {
		int talkCount = 0;
		Agent latestAgent = null;
		
		for(Agent agent: agentTalkListMap.keySet()) { 
			if(talkCount>=countDayTalk(agent)) {
				latestAgent = agent;
			}
		}
		return latestAgent;
	}
	
	/**
	 * 指定エージェントリストの中で指定した日付の発言回数が少ない順のエージェントリスト
	 * @param agentList　:指定エージェントりすおt　
	 * @param day　:指定日付
	 * @return 発言回数少ない順のエージェントリスト
	 */
	public List<Agent> fewerTalkAgentList(List<Agent> agentList, int day) {
		// ソートするためのリスト
		List<CountAgent> countAgentList = new ArrayList<>();
		for(Agent agent: agentList) {
			countAgentList.add(new CountAgent(countDayTalk(agent, day), agent));
		}
		// ソート
		Collections.sort(countAgentList, new CountAgentComparator());
		// 渡すエージェントリスト
		List<Agent> agents = new ArrayList<>();
		for(int i=0; i<countAgentList.size(); i++) {
			agents.add(countAgentList.get(i).agent);
		}
		
		return agents;
	}
	
	/*-----------------------------トピック系---------------------------------------*/
	
	/**
	 * 最も早く指定役職をCOしたエージェント(自分以外)を取得する．同時ならばランダム
	 * @param role :指定役職
	 * @param day :指定日付
	 * @param me :自分
	 * @return 最も指定役職COしたエージェント(自分以外)
	 */
	public Agent getEaliestComingout(Role role, int day, Agent me) {
		//エージェントリスト
		List<Agent> agentList = new ArrayList<>();
		//指定日付までにCO発言したtalkを取得
		List<Talk> talkList = getTopicTalkList(Topic.COMINGOUT, day);
		if(talkList!=null) {
			//日付
			int talkday = 0;
			//ターン
			int turn = 0;
			for(int i=0; i<talkList.size(); i++) {
				//初回は必ず追加
				if(i==0) {
					if(talkList.get(i).getAgent()!=me){
						talkday = talkList.get(i).getDay();
						turn = talkList.get(i).getTurn();
						agentList.add(talkList.get(i).getAgent());
					}
				}else {
					if(talkList.get(i).getAgent()!=me){
						if(talkList.get(i).getDay()<talkday && talkList.get(i).getTurn()<turn) {
							talkday = talkList.get(i).getDay();
							turn = talkList.get(i).getTurn();	
							agentList.clear();
							agentList.add(talkList.get(i).getAgent());
						}else if(talkList.get(i).getDay()==talkday && talkList.get(i).getTurn()==turn){	
							agentList.add(talkList.get(i).getAgent());
						}
					}
				}
			}
		}
		
		Agent agent = null;
		if(agentList.size()==1) {
			agent = agentList.get(0);
		}
		if(agentList.size()>=2) {
			agent = RandomSelect.randomAgentSelect(agentList);
		}
		return agent;
	}
	
	/*----------------------------日付版の処理---------------------------------------*/
	
	/**
	 * 指定日付の会話リストを取得する
	 * @param day :日付
	 * @return 会話リスト
	 */
	public List<Talk> getDayTalkList(int day) {
		List<Talk> talkList = new ArrayList<>();
		if(dayTalkListMap.containsKey(day)) {
			talkList = dayTalkListMap.get(day);
		}
		return talkList;
	}
	
	/**
	 * 指定日付の会話リストから指定した発話IDの発話を取得する
	 * @param day :日付
	 * @param id :発話ID
	 * @return 会話
	 */
	public Talk getToTalk(int day, int id) {
		List<Talk> dayList = getDayTalkList(day);
		for(Talk talk: dayList) {
			if(talk.getIdx()==id) {
				return talk;
			}
		}
		return null;
	}
	
	
}
