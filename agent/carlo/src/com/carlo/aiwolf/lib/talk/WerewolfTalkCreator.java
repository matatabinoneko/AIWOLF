package com.carlo.aiwolf.lib.talk;

import java.util.*;
import java.util.Map.Entry;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.OverContentBuilder;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.TemplateWhisperFactory;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.*;

import com.carlo.aiwolf.base.lib.MyAbstractRole;
import com.carlo.aiwolf.lib.AiWolfUtil;

/**
 *  FakeCOTalkCreaterにwhisperできる機能をつけたもの
 * @author info
 *
 */
public class WerewolfTalkCreator extends FakeCOTalkCreator {
	/** 今日の襲撃予定先 */
	protected Agent attackTarget;
	protected boolean isNeedToWhisperAttack;
	
	protected boolean isNeedToWhisperCO;
	/** 今日の投票予定先（仲間のみに伝える用） */
	protected Agent whisperVoteTarget;
	protected boolean isNeedToWhisperVote;
	
	protected Map<Agent,Role> whisperEstimateAgentMap;
	protected Map<Agent,Boolean> isNeedToWhisperEstimateMap;
	
	public WerewolfTalkCreator(MyAbstractRole myRole) {
		super(myRole);
		isNeedToWhisperCO=false;
		
		attackTarget=null;
		isNeedToWhisperAttack=false;
		isNeedToWhisperVote=false;
		whisperVoteTarget=null;
		whisperEstimateAgentMap=new HashMap<Agent,Role>();
		isNeedToWhisperEstimateMap=new HashMap<Agent,Boolean>();
	}
	/**
	 *  占い師と霊能者騙り用のdayStart<br>
	 *  CO時に発言するJudgeのリストを設定する <br>
	 *  互換性確保のために置いてある
	 * @param myJudgeList 占いもしくは霊能の結果リスト
	 */
	public void dayStart(List<Judge> judgeList){
		super.dayStart(judgeList);
		attackTarget=null;
		isNeedToWhisperAttack=false;
		isNeedToWhisperVote=false;
		whisperVoteTarget=null;
	}
	public void setAttackTarget(Agent target){
		if(attackTarget!=target){
			attackTarget=target;
			isNeedToWhisperAttack=true;
		}
	}
	public Agent getAttackTarget(){
		return attackTarget;
	}
	public void setWhisperVoteTarget(Agent target){
		if(whisperVoteTarget!=target){
			whisperVoteTarget=target;
			isNeedToWhisperVote=true;
		}
	}
	public Agent getWhisperVoteTarget(){
		return whisperVoteTarget;
	}
	/** agentはroleだと思う。としゃべる */
	public void setWhisperEstimate(Agent agent,Role role){
		if(whisperEstimateAgentMap.containsKey(agent)){
			//roleが変わっていれば反映
			if(whisperEstimateAgentMap.get(agent)!=role){
				whisperEstimateAgentMap.put(agent, role);
				isNeedToWhisperEstimateMap.put(agent, true);
			}
		}
		//keyがなければ追加
		else{
			whisperEstimateAgentMap.put(agent, role);
			isNeedToWhisperEstimateMap.put(agent, true);
		}
	}
	public Map<Agent,Role> getWhisperEstimateMap(){
		return whisperEstimateAgentMap;
	}
	
	@Override
	/** 騙る予定の役職をささやく */
	public boolean setFakeRole(Role fakeRole){
		boolean ok=super.setFakeRole(fakeRole);
		if(fakeRole!=null && ok) isNeedToWhisperCO=true;
		return ok;
	}
	
	/** ささやく予定のものがあれば、その発言を返す<br>
	 * CO>Attack>vote>estimateの優先度 */
	public String whisper(){
		if(isNeedToWhisperCO){
			isNeedToWhisperCO=false;
			//if(this.getComingoutRole()!=null) return TemplateWhisperFactory.comingout(myAgent,this.getComingoutRole());
			if(this.getComingoutRole()!=null) return AiWolfUtil.GetTalkText(new ComingoutContentBuilder(myAgent,this.getComingoutRole()));
		}
		if(isNeedToWhisperAttack){
			isNeedToWhisperAttack=false;
			//return TemplateWhisperFactory.attack(attackTarget);
			return AiWolfUtil.GetTalkText(new AttackContentBuilder(attackTarget));
		}
		if(isNeedToWhisperVote){
			isNeedToWhisperVote=false;
			//return TemplateWhisperFactory.vote(whisperVoteTarget);
			return AiWolfUtil.GetTalkText(new VoteContentBuilder(whisperVoteTarget));
		}
		for(Entry<Agent, Boolean> entry:isNeedToWhisperEstimateMap.entrySet()){
			if(entry.getValue()){
				isNeedToWhisperEstimateMap.put(entry.getKey(),false);
				//return TemplateWhisperFactory.estimate(entry.getKey(), whisperEstimateAgentMap.get(entry.getKey()));
				return AiWolfUtil.GetTalkText(new EstimateContentBuilder(entry.getKey(), whisperEstimateAgentMap.get(entry.getKey())));
			}
		}
		
		return AiWolfUtil.GetTalkText((new OverContentBuilder()));
	}

}
