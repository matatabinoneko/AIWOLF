package com.gmail.k14.itolab.aiwolf.util;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

/**
 * 渡された会話リストを操作するクラス
 * @author k14096kk
 *
 */
public class TalkSelect {

	/**
	 * 渡された会話リストから指定トピックの会話リストを取得
	 * @param talks :会話リスト
	 * @param topic :トピック
	 * @return 指定トピックの会話リスト
	 */
	public static List<Talk> topicList(List<Talk> talks, Topic topic) {
		List<Talk> talkList = new ArrayList<>();
		for(Talk talk: talks) {
			Content content = new Content(talk.getText());
			if(Check.isTopic(content.getTopic(), topic)) {
				talkList.add(talk);
			}
		}
		return talkList;
	}
	
	/**
	 * 渡された会話リストから指定エージェントが話し手の会話リストを取得
	 * @param talks :会話リスト
	 * @param agent :話し手
	 * @return 指定した話し手の会話リスト
	 */
	public static List<Talk> speakerList(List<Talk> talks, Agent agent) {
		List<Talk> talkList = new ArrayList<>();
		for(Talk talk: talks) {
			if(Check.isAgent(talk.getAgent(), agent)) {
				talkList.add(talk);
			}
		}
		return talkList;
	}
	
	/**
	 * 渡された会話リストから指定エージェントが対象の会話リストを取得
	 * @param talks :会話リスト
	 * @param agent :対象
	 * @return 指定した対象の会話リスト
	 */
	public static List<Talk> targetList(List<Talk> talks, Agent agent) {
		List<Talk> talkList = new ArrayList<>();
		for(Talk talk: talks) {
			Content content = new Content(talk.getText());
			if(Check.isAgent(content.getTarget(), agent)) {
				talkList.add(talk);
			}
		}
		return talkList;
	}
	
	/**
	 * 渡された会話リストから指定役職の会話リストを取得
	 * @param talks :会話リスト
	 * @param role :役職
	 * @return 指定した役職が含まれる会話リスト
	 */
	public static List<Talk> roleList(List<Talk> talks, Role role) {
		List<Talk> talkList = new ArrayList<>();
		for(Talk talk: talks) {
			Content content = new Content(talk.getText());
			if(Check.isRole(content.getRole(), role)) {
				talkList.add(talk);
			}
		}
		return talkList;
	}
	
	/**
	 * 渡された会話リストから指定種族の会話リストを取得
	 * @param talks :会話リスト
	 * @param species :種族
	 * @return 指定した種族が結果となる会話リストを取得
	 */
	public static List<Talk> speciesList(List<Talk> talks, Species species) {
		List<Talk> talkList = new ArrayList<>();
		for(Talk talk: talks) {
			Content content = new Content(talk.getText());
			if(Check.isSpecies(content.getResult(), species)) {
				talkList.add(talk);
			}
		}
		return talkList;
	}
	
	/**
	 * 指定した会話リストから指定トピックを最も早く発言したエージェント取得(自分以外)
	 * @param talkList :指定会話リスト
	 * @param topic :指定トピック
	 * @param me :自分
	 * @return 発言したエージェント
	 */
	public static Agent getEaliestTopic(List<Talk> talkList, Topic topic, Agent me) {
		//エージェントリスト
		List<Agent> agentList = new ArrayList<>();
		//指定トピックの会話リスト
		List<Talk> storeList = topicList(talkList, topic);
		int talkday = 0;
		int turn = 0;
		
		for(int i=0; i<storeList.size(); i++) {
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

		Agent agent = null;
		if(agentList.size()==1) {
			agent = agentList.get(0);
		}
		if(agentList.size()>=2) {
			agent = RandomSelect.randomAgentSelect(agentList);
		}
		return agent;
	}
	
	/**
	 * 指定した会話リストから最も早く指定役職をCOしたエージェント(自分を除く)
	 * @param talkList :指定会話リスト
	 * @param role :指定役職
	 * @param me :自分
	 * @return COしたエージェント
	 */
	public static Agent getEaliestComingout(List<Talk> talkList, Role role, Agent me) {
		//指定トピックの会話リスト
		List<Talk> storeList = topicList(talkList, Topic.COMINGOUT);
		
		for(int i=0; i<storeList.size(); i++) {
			Talk talk = storeList.get(i);
			Content content = new Content(talk.getText());
			if(talk.getAgent()!=me && content.getRole()==Role.WEREWOLF) {
				return talk.getAgent();
			}
		}
		return null;
	}
	
	public static Species getDivinedResult(List<Talk> talkList, Agent agent) {
		//指定トピックの会話リスト
		List<Talk> storeList = topicList(talkList, Topic.DIVINED);
		
		for(int i=0; i<storeList.size(); i++) {
			Talk talk = storeList.get(i);
			Content content = new Content(talk.getText());
			if(talk.getAgent()==agent) {
				return content.getResult();
			}
		}
		return null;
	}
	
	/**
	 * リクエストリストから自分を除いて一番早くリクエストしたエージェントを取得する
	 * @param talkList :リクエストリスト
	 * @param me :自分
	 * @return リクエストしたエージェント
	 */
	public static Agent getEaliestRequest(List<Talk> talkList, Agent me) {
		for(int i=0; i<talkList.size(); i++) {
			Talk talk = talkList.get(i);
			if(talk.getAgent()!=me) {
				return talk.getAgent();
			}
		}
		return null;
	}
	
	/**
	 * 会話リストからリクエストの会話リストを取得
	 * @param talks :会話リスト
	 * @return リクエストの会話リスト
	 */
	public static List<Talk> requestList(List<Talk> talks) {
		List<Talk> talkList = new ArrayList<>();
		for(Talk talk: talks) {
			if(OperatorElement.isRequest(new Content(talk.getText()))) {
				talkList.add(talk);
			}
		}
		return talkList;
	}
	
}
