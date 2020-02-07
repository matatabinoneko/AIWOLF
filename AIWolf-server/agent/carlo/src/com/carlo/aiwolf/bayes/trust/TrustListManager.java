package com.carlo.aiwolf.bayes.trust;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;

import com.carlo.aiwolf.base.lib.MyAbstractBodyguard;
import com.carlo.aiwolf.base.lib.MyAbstractRole;
import com.carlo.aiwolf.bayes.trust.noweka.NoWekaTrustList;
import com.carlo.aiwolf.bayes.trust.weka.TrustList;
import com.carlo.aiwolf.lib.info.*;
/**
 * AbstractRoleのdayStart,updateメソッドの中で
 * このクラスのdayStart,updateを呼ぶこと
 * 
 * NOTE:trustListには自分は入っていない
 * @author carlo
 *
 */
public class TrustListManager {
	protected AbstractTrustList trustList;
	protected GameInfoManager gameInfoMgr;
	private MyAbstractRole myRole;
	private Agent truthMedium=null;
	protected int readTalkNum=0;
	
	/**  コンソール出力。この設定がTrustListにも反映される。on,offはここでいじる。 */
	protected boolean isShowConsoleLog=false;
	public TrustListManager(List<Agent> agentList,MyAbstractRole myRole,GameInfoManager agentInfo){
		this.gameInfoMgr=agentInfo;
		this.myRole=myRole;
		//trustList=new TrustList(agentList,myRole.getMe(),agentInfo);
		trustList=new NoWekaTrustList(agentList,myRole.getMe(),agentInfo);
		trustList.setShowConsoleLog(isShowConsoleLog);
	}
	/** 使わない(forLog用) */
	public TrustListManager(List<Agent> agentList,Agent myAgent,GameInfoManager agentInfo){
		this.gameInfoMgr=agentInfo;
		trustList=new TrustList(agentList,myAgent,agentInfo);
		trustList.setShowConsoleLog(false);
	}
	/** AbstractRoleのdayStartの最後に呼ぶ */
	public void dayStart(){
		//昨日の投票によって信頼度を上下
		if(getDay()>1){
			/*
			int yesterday=getDay()-1;
			Agent executedAgent=agentInfo.getDeadAgent(yesterday,CauseOfDeath.EXECUTED);
			for(Agent agent:agentInfo.getAgentList(true)){
				boolean assist= (executedAgent==agentInfo.getVoteTarget(yesterday, agent));
				trustList.changeVoterTrust(agent, null, yesterday, String.valueOf(assist));
			}
			*/
			
		}
		//昨日の襲撃によって信用度を上下
		//long start = System.currentTimeMillis();
		if(getDay()>1){
			int yesterday=getDay()-1;
			Agent attackedAgent=gameInfoMgr.getDeathInfo().getDeadAgent(yesterday, CauseOfDeath.ATTACKED);
			if(attackedAgent!=null){
				trustList.changeDeadAgent(attackedAgent, CauseOfDeath.ATTACKED);
			}
			//System.out.println("襲撃"+attackedAgent+" day:"+yesterday);
			//襲撃された人物を占っていた占いCO者がいた場合、その計算
			for(AbilityResult abilityResult:gameInfoMgr.getAbilityInfo().searchDivinedAbilityResults(attackedAgent)){
				Agent seer=abilityResult.getAgent();
				Agent target=abilityResult.getTarget();
				int day=abilityResult.getDay();
				
				if(isShowConsoleLog) System.out.println("占い先死亡 seer:"+seer+" target:"+target+" day"+day);
				Correct correct=Correct.UNKNOWN;
				Correct preCorrect=trustList.getSeerCorrect(seer,day,target);
				if(preCorrect!=null) correct=preCorrect;
				
				//まず前回の結果を戻す
				if(isShowConsoleLog) System.out.println("back trust ");
				trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), correct,true,target,Correct.UNKNOWN);
				

				
				if(isShowConsoleLog) System.out.println("recalc trust ");
				trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), correct,target,Correct.YES);
			}
		}
		//long stop = System.currentTimeMillis();
		//System.out.println(this.getDay()+"day(dayStart) :"+(stop-start)+"ms");
		readTalkNum=0;
	}
	/** AbstractRoleのupdateの最後に呼ぶ */
	public void update(){
		readTalkList();
	}
	
	/**
	 * 
	 * @param  trustLevel 信用度の高さ。Highest or Lowest
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 条件に合うAgentを返す
	 */
	public Agent getAgent(TrustLevel  trustLevel,boolean isAliveOnly){
		List<Agent> sortedList= trustList.getSortedAgentList(isAliveOnly);
		if(sortedList.size()==0) return null;
		
		if(trustLevel==TrustLevel.LOWEST) return sortedList.get(0);
		else if(trustLevel==TrustLevel.HIGHEST) return sortedList.get(sortedList.size()-1);
		return null;
	}
	/**
	 * 
	 * @param  trustLevel 信用度の高さ。Highest or Lowest
	 * @param coRole COした役職。nullならCOがない人を対象に。
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 条件に合うAgentを返す。いなければnull
	 */
	public Agent getRoleCOAgent(TrustLevel trustLevel,Role coRole,boolean isAliveOnly){
		List<Agent> sortedList= trustList.getSortedRoleCOAgentList(coRole, isAliveOnly);
		if(sortedList.size()==0) return null;
		
		if(trustLevel==TrustLevel.LOWEST) return sortedList.get(0);
		else if(trustLevel==TrustLevel.HIGHEST) return sortedList.get(sortedList.size()-1);
		return null;
	}
	
	/**
	 * @param coRole COした役職。nullならCOがない人を対象に。
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 信用度が低い順にソートされたAgentのList
	 */
	public List<Agent> getSortedRoleCOAgentList(Role coRole,boolean isAliveOnly){
		return trustList.getSortedRoleCOAgentList(coRole,isAliveOnly);
	}
	/**
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 信用度が低い順にソートされたAgentのList
	 */
	public List<Agent> getSortedAgentList(boolean isAliveOnly){
		return trustList.getSortedAgentList(isAliveOnly);
	}
	
	/** 信用度がpoint以下のAgentを探してきてソートして返す */
	public List<Agent> getSortedAgentList(boolean isAliveOnly,double point){
		return trustList.getSortedAgentList(isAliveOnly,point);
	}

	/**
	 *  trustListManagerの isShowConsoleLog がtrueの場合のみログ出力を行う
	 */
	public void printTrustList(){
		if(isShowConsoleLog) trustList.printTrustList();
	}
	/** 強制的に出力 */
	public void printTrustListByForce(){
		trustList.setShowConsoleLog(true);
		trustList.printTrustList();
		trustList.setShowConsoleLog(false);
	}
	public void printTrustList(GameInfo finishedGameInfo){
		if(isShowConsoleLog) trustList.printTrustList(finishedGameInfo);
	}
	public void setShowConsoleLog(boolean isShowConsoleLog){
		this.isShowConsoleLog=isShowConsoleLog;
		trustList.setShowConsoleLog(isShowConsoleLog);
	}
	/** isShowConsoleLogを無視して表示 */
	public void printTrustListForCreatingData(GameInfo finishedGameInfo){
		trustList.printTrustListForCreatingData(finishedGameInfo.getRoleMap());
	}
	protected int getDay(){
		return myRole.getDay();
	}
	public double getTrustPoint(Agent agent){
		return trustList.getTrustPoint(agent);
	}
	
	public void setTruthMedium(Agent truthMedium){
		this.truthMedium=truthMedium;
	}
	
	/**
	 * 発言を読んで、必要があれば信用度の計算を行う
	 */
	protected void readTalkList(){
		long before=0;
		
		//long start = System.currentTimeMillis();
		List<Talk> talkList=myRole.getLatestDayGameInfo().getTalkList();
		for(int i=readTalkNum;i<talkList.size();i++){
			Talk talk=talkList.get(i);
			//Utterance utterance=new Utterance(talk.getContent());
			Content content=new Content(talk.getText());
			//時間計測
			if(before!=0){
			long now = System.currentTimeMillis();
			System.out.println(content.getTopic());
			System.out.println(now-before+"ms");
			before=now;
			}
			
			switch (content.getTopic()){
			case COMINGOUT:
				switch (content.getRole()){
				case MEDIUM:
					break;
				default:
					break;
				}
				break;
			case DIVINED:
				Agent seer=talk.getAgent();
				int day=talk.getDay();
				Species species=content.getResult();
				
				trustList.changeSeerTrust(seer,day, species, Correct.UNKNOWN,content.getTarget(),Correct.UNKNOWN);
				break;
			case IDENTIFIED:
				Agent medium=talk.getAgent();
				//霊能者のネットワーク計算
				trustList.changeMediumTrust(medium, content.getResult(), talk.getDay());
				
				//霊能COが一人だけなら、霊能を真と仮定して各占いとラインが繋がっているかで信用度の計算
				Agent truthMedium=getTruthMedium();
				//gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM)==1
				if(truthMedium!=null && truthMedium==medium){
					Species inquestedResult=content.getResult();
					//投票結果から計算
					for(Entry<Agent,Integer> voteEntry : gameInfoMgr.getVoteInfo().searchVoterMap(content.getTarget()).entrySet()) {
						int voteDay=voteEntry.getValue();
						Agent voteAgent=voteEntry.getKey();
						Agent executedAgent=content.getTarget();
						boolean assist= (executedAgent==gameInfoMgr.getVoteInfo().getVoteTarget(voteDay, voteAgent));
						trustList.changeVoterTrust(voteEntry.getKey(), content.getResult(),voteDay,String.valueOf(assist));
					}
					
					//霊媒先と一致する占い結果を探す
					//for(AbilityResult abilityResult:AIMAssister.searchDivinedAgent(gameInfoMgr, content.getTarget())){
					for(AbilityResult abilityResult: gameInfoMgr.getAbilityInfo().searchDivinedAbilityResults(content.getTarget())){
						//まず前回の結果を戻す
						if(isShowConsoleLog) System.out.println("back trust ");
						trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), Correct.UNKNOWN,true,content.getTarget(),Correct.UNKNOWN);
						
						//判明した結果を入れて計算
						Correct correct=Correct.UNKNOWN;
						if(inquestedResult==abilityResult.getSpecies()) correct=Correct.YES;
						else correct=Correct.NO;
						
						if(isShowConsoleLog) System.out.println("recalc trust ");
						trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), correct,content.getTarget(),Correct.UNKNOWN);	
					}
				}
				break;
			default:
				break;
			}
			readTalkNum++;
		}
		//long stop = System.currentTimeMillis();
		//System.out.println(this.getDay()+"day(readTalkList) :"+(stop-start)+"ms");
	}
	
	private Agent getTruthMedium(){
		if(truthMedium!=null) return truthMedium;
		//自分が霊能なら自分を返す
		if(myRole.getMyRole()==Role.MEDIUM){
			return myRole.getMe();
		}
		//霊能が１COならその霊能を返す
		if(gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM)==1){
			return gameInfoMgr.getCOInfo().getCOAgentList(Role.MEDIUM, false, false).get(0);
		}
		return null;
	}
	
}
