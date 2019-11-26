package com.carlo.aiwolf.lib.talk;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.*;

import com.carlo.aiwolf.base.lib.MyAbstractRole;
import com.carlo.aiwolf.lib.AiWolfUtil;

/**
 * CO・能力結果発言をしてくれる機能を追加したTalkCreater
 * @author info
 *
 */
public class COTalkCreator extends TalkCreator {
	protected boolean isComingOut;
	protected boolean isNeedToComingOut;
	protected ArrayList<Judge> myToldJudgeList;
	protected ArrayList<Judge> myJudgeList;
	
	protected ArrayList<Agent> myGuardList;
	protected int myToldGuardIdx;
	
	protected Role myRole;
	protected Agent myAgent;
	public COTalkCreator(MyAbstractRole myRole){
		isComingOut=false;
		isNeedToComingOut=false;
		myToldJudgeList=new ArrayList<Judge>();
		myJudgeList=new ArrayList<Judge>();
		
		myGuardList=new ArrayList<Agent>();
		myToldGuardIdx=0;
		
		this.myRole=myRole.getMyRole();
		this.myAgent=myRole.getMe();
	}
	/** 非推奨。能力結果報告ができない。<br>
	 *dayStart(Agent guardTarget),
	 *dayStart(Judge yesterdayJudge)をご利用ください。  */
	@Deprecated
	public void dayStart(){
		super.dayStart();
	}
	/**
	 *  狩人用のdayStart
	 * @param guardTarget 昨夜護衛したターゲット <br>
	 * 真狩人ならgetLatestDayGameInfo().getGuardedAgent() を渡す
	 */
	public void dayStart(Agent guardTarget){
		super.dayStart();
		if(guardTarget!=null){
			myGuardList.add(guardTarget);
		}
	}
	/**
	 * 占い師と霊能者用のdayStart
	 * 
	 * @param yesterdayJudge 昨日の占いもしくは霊能結果<br>
	 * 占い師なら getLatestDayGameInfo().getDivineResult() <br>
	 * 霊能者なら getLatestDayGameInfo().getMediumResult() <br>
	 * を渡す
	 */
	public void dayStart(Judge yesterdayJudge){
		super.dayStart();
		if(yesterdayJudge!=null){
			this.myJudgeList.add(yesterdayJudge);
		}
	}
	/** このメソッドを呼ぶと、自分の発話が回ってきた時に自動的にカミングアウトを行う。<br>
	 * カミングアウト以降は結果報告を行うようになる。 */
	public void doComingOut(){
		isNeedToComingOut=true;
	}
	public boolean isComingOut(){
		return isComingOut;
	}
	/** しゃべる必要があることをしゃべる。
	 *  <br>CO>占い・霊能結果>スーパークラスのtalk で優先  */
	public String talk(){
		
		if(isNeedToComingOut && isComingOut==false){
			isComingOut=true;
			ContentBuilder builder = new ComingoutContentBuilder(myAgent,getComingoutRole());
			Content content=new Content(builder);
			return content.getText();
			//return TemplateTalkFactory.comingout(myRole.getMe(), getComingoutRole());
		}
		else if(isComingOut){
			if(getComingoutRole()==Role.SEER || getComingoutRole()==Role.MEDIUM){
				for(Judge judge:myJudgeList){
					//しゃべってないCO結果があるならしゃべる
					if(!myToldJudgeList.contains(judge)){
						myToldJudgeList.add(judge);
						if(getComingoutRole() ==Role.SEER) {
							return AiWolfUtil.GetTalkText(new DivinedResultContentBuilder(judge.getTarget(),judge.getResult()));
						}
						else if(getComingoutRole()==Role.MEDIUM) {
							return AiWolfUtil.GetTalkText(new IdentContentBuilder(judge.getTarget(),judge.getResult()));
						}
					}
				}
			}
			else if(getComingoutRole()==Role.BODYGUARD){
				for(int i=myToldGuardIdx;i<myGuardList.size();){
					myToldGuardIdx++;
					return AiWolfUtil.GetTalkText(new GuardedAgentContentBuilder(myGuardList.get(i)));
				}
			}
		}
		return super.talk();
	}
	public Role getComingoutRole(){
		return myRole;
	}
	
	public void setMyGuardList(List<Agent> guardTarges){
		myGuardList=new ArrayList<Agent>(guardTarges);
	}
	public void addMyGuardTarget(Agent target){
		if(target!=null) myGuardList.add(target);
	}

}
