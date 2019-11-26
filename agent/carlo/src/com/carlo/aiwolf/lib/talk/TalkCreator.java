package com.carlo.aiwolf.lib.talk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.OverContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.carlo.aiwolf.lib.AiWolfUtil;
import com.carlo.aiwolf.lib.ExRandom;
/**
 *  投票先についての発言をしてくれるクラス
 * @author info
 *
 */

public class TalkCreator {
	protected Agent voteTarget;
	protected boolean isNeedToTalkVote;
	
	/** */
	protected Map<Agent,Role> estimateAgentMap;
	protected Map<Agent,Boolean> isNeedToTalkEstimateMap;
	
	public TalkCreator(){
		estimateAgentMap=new HashMap<Agent,Role>();
		isNeedToTalkEstimateMap=new HashMap<Agent,Boolean>();
	}
	public void dayStart(){
		voteTarget=null;
		isNeedToTalkVote=false;
	}
	/** 投票する予定のAgentを設定する。<br>もし前回の対象から変更されていれば自動的に投票対象をtalk()メソッド中でしゃべる。一緒ならしゃべらない。<br><br>
	 * 注意:ある範囲内からランダムでAgentを選択する場合は下記のいずれかを行う<br>
	 * 　・setVoteTargetFromListを使う<br>
	 *  ・ExRandom.select(List<Agent> agentList,Agent nowSelectedAgent)を使って同じ場合は固定化させてこのメソッドを呼ぶ <br>
	 *  そうしなければupdate()ごとに毎回発言することになる。
	 *  list.get(0)のようにsetする対象を固定化させてこのメソッドを呼んでも可。
	 *   */
	public void setVoteTarget(Agent target){
		if(voteTarget!=target){
			voteTarget=target;
			isNeedToTalkVote=true;
		}
	}
	/** 指定された範囲からランダムに投票対象を決定し、voteTargetを設定する。voteTargetがtargetListに含まれている場合は、変更なし。 */
	public void setVoteTargetFromList(List<Agent> targetList){
		if(targetList.contains(voteTarget)==false){
			voteTarget=ExRandom.select(targetList);
			isNeedToTalkVote=true;
		}
	}
	
	/** agentはroleだと思う。としゃべる */
	public void setEstimate(Agent agent,Role role){
		if(estimateAgentMap.containsKey(agent)){
			//roleが変わっていれば反映
			if(estimateAgentMap.get(agent)!=role){
				estimateAgentMap.put(agent, role);
				isNeedToTalkEstimateMap.put(agent, true);
			}
		}
		//keyがなければ追加
		else{
			estimateAgentMap.put(agent, role);
			isNeedToTalkEstimateMap.put(agent, true);
		}
	}
	
	/** しゃべることがあればその発言を返す。なければOVER。<br><br>
	 * ESTIMATE>投票発言で優先 */
	public String talk(){
		for(Entry<Agent, Boolean> entry:isNeedToTalkEstimateMap.entrySet()){
			if(entry.getValue()){
				isNeedToTalkEstimateMap.put(entry.getKey(),false);
				//return TemplateTalkFactory.estimate(entry.getKey(), estimateAgentMap.get(entry.getKey()));
				return AiWolfUtil.GetTalkText(new EstimateContentBuilder(entry.getKey(), estimateAgentMap.get(entry.getKey())));
			}
		}
		
		if(isNeedToTalkVote){
			isNeedToTalkVote=false;
			//return TemplateTalkFactory.vote(voteTarget);
			return AiWolfUtil.GetTalkText(new VoteContentBuilder(voteTarget));
		}
		//else return TemplateTalkFactory.over();
		return AiWolfUtil.GetTalkText(new SkipContentBuilder());
	}
	/** 投票予定のエージェントを返す */
	public Agent getVoteTarget(){
		return voteTarget;
	}

}
