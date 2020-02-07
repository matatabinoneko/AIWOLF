package com.carlo.aiwolf.lib.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 *  ゲームについての様々な情報クラスを保持しているクラス<br>
 *  何か情報が欲しければ各種情報クラスをgetし、そのオブジェクトでメソッド呼び出しを行う <br>
 *  AbilityInfo 能力結果発言 <br>
 *  TalkInfo 投票先発言  <br>
 *  DeathInfo 死亡情報 <br>
 *  VoteInfo 投票情報　<br>
 *  COInfo CO情報
 *  
 *  使う場合は、各役職実装クラスで update,dayStartメソッドを呼び出すことが必須
 * @author carlo
 *
 */



public class GameInfoManager {
	private GameInfo gameInfo;
	
	protected AbilityInfo abilityInfo;
	protected VoteInfo voteInfo;
	protected TalkInfo talkInfo;
	protected DeathInfo deathInfo;
	protected COInfo coInfo;
	
	protected WhisperInfo whisperInfo;
	protected AttackVoteInfo attackVoteInfo;
	
	protected int readTalkNum;
	protected int readWhisperNum;
	
	private Agent myAgent;
	
	public GameInfoManager(GameInfo gameInfo,Agent myAgent){
		this.gameInfo=gameInfo;
		this.myAgent=myAgent;

		this.talkInfo=new TalkInfo(this);
		this.abilityInfo=new AbilityInfo(this);
		this.deathInfo=new DeathInfo(this);
		this.voteInfo=new VoteInfo();
		this.coInfo=new COInfo(this);
		this.whisperInfo=new WhisperInfo(this);
		this.attackVoteInfo=new AttackVoteInfo(this);
		
		System.out.println("In new GameInfoManager() "+this.getMyRole());
	}
	/** 使わないでください */
	public GameInfoManager(ArrayList<Agent> agentList){

		this.talkInfo=new TalkInfo(this);
		this.abilityInfo=new AbilityInfo(this);
		/** 不具合発生するだろう */
		this.coInfo=new COInfo(this);
		this.whisperInfo=new WhisperInfo(this);
		this.attackVoteInfo=new AttackVoteInfo(this);
	}
	/** 呼び出し必須 */
	public void update(GameInfo gameInfo){
		this.gameInfo=gameInfo;
		readTalkList();
		readWhisperList();
	}
	/** 呼び出し必須 */
	public void dayStart(){
		readTalkNum=0;
		readWhisperNum=0;
		talkInfo.dayStart();
		whisperInfo.dayStart();
		//死亡者リストの追加
		int yesterday=gameInfo.getDay()-1;
		if(yesterday>=0){ 
			deathInfo.addDeathData(yesterday, gameInfo.getExecutedAgent(), gameInfo.getAttackedAgent());
		}
		if(gameInfo.getDay()!=0) voteInfo.addList(gameInfo.getVoteList());
		//if(gameInfo.getDay()!=0) attackVoteInfo.addList(gameInfo.getAttackVoteList());
		
	}
	
	/**
	 * 
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 自身を除いた、条件に合うエージェントのリストを返す
	 */
	public List<Agent> getAgentListExceptMe(boolean isAliveOnly){
		ArrayList<Agent> agentList=new ArrayList<Agent>();
		if(isAliveOnly) agentList.addAll(gameInfo.getAliveAgentList());
		else agentList.addAll(gameInfo.getAgentList());
		agentList.remove(myAgent);
		return agentList;
	}
	
	/** agentが生存しているかどうか */
	public boolean isAlive(Agent agent){
		if(gameInfo.getStatusMap().get(agent)==Status.ALIVE) return true;
		else return false;
	}
	
	/** 露出人狼数を返す<br>
	 * 露出人狼=COの中にいると分かっている人狼の数。狂人は必ずCOしているとして数える */
	public int getNumOfWerewolfInCO(){
		//絶対人狼露出数(COした全役職-真役職数-狂人数)
		int seerCONum=getCOInfo().getNumOfCOAgent(Role.SEER);
		int mediumCONum=getCOInfo().getNumOfCOAgent(Role.MEDIUM);
		int bodyGuardCOnum=getCOInfo().getNumOfCOAgent(Role.BODYGUARD);
		
		int werewolfCONum= seerCONum>0 ? seerCONum-getRoleNum(Role.SEER) : 0;
		werewolfCONum+= mediumCONum>0 ? mediumCONum-getRoleNum(Role.MEDIUM) : 0;
		werewolfCONum+= bodyGuardCOnum>0 ? bodyGuardCOnum-getRoleNum(Role.BODYGUARD) : 0;
		werewolfCONum= (werewolfCONum-getRoleNum(Role.POSSESSED)) >=0 ?(werewolfCONum-getRoleNum(Role.POSSESSED)) : 0;
		
		//System.out.println(werewolfCONum);
		//coInfo.printCoRoleMap();
		return werewolfCONum;
	}
	
	/** 投票情報を管理するVoteInfoを返す。。 */
	public VoteInfo getVoteInfo(){
		return voteInfo;
	}
	/**発言情報を管理するTalkInfoを返す。 */
	public TalkInfo getTalkInfo(){
		return talkInfo;
	}
	/** 死亡情報を管理するDeathInfoを返す */
	public DeathInfo getDeathInfo(){
		return deathInfo;
	}
	/** 能力結果発言を管理するAbilityInfoを返す */
	public AbilityInfo getAbilityInfo(){
		return abilityInfo;
	}
	/** CO発言を管理するCOInfoを返す */
	public COInfo getCOInfo(){
		return coInfo;
	}
	/** 人狼のみ。ささやき発言を管理するWhisperInfoを返す */
	public WhisperInfo getWhisperInfo(){
		return whisperInfo;
	}
	/** 人狼のみ。襲撃投票情報を管理するAttackVoteInfoを返す */
	public AttackVoteInfo getAttackVoteInfo(){
		return attackVoteInfo;
	}
	
	//おまけ
	public Agent getMyAgent(){
		return myAgent;
	}
	public int getDay(){
		return gameInfo.getDay();
	}
	public Role getMyRole(){
		return gameInfo.getRole();
	}
	/** playerNumの人数でゲームを始めた時、roleが何人いるかを返す（配役） */
	public int getRoleNum(Role role){
		return GameSetting.getDefaultGame(gameInfo.getAgentList().size()).getRoleNum(role);
	}
	/** gameInfoのメソッドをそのまま */
	public List<Agent> getAgentList(){
		return gameInfo.getAgentList();
	}
	public List<Agent> getAliveAgentList(){
		return gameInfo.getAliveAgentList();
	}
	public GameInfo getGameInfo(){
		return gameInfo;
	}

	
	
	/**
	 * 発言を読んで、情報を格納
	 */
	private void readTalkList(){
		List<Talk> talkList=gameInfo.getTalkList();
		for(int i=readTalkNum;i<talkList.size();i++){
			Talk talk=talkList.get(i);
			talkInfo.addTalk(talk);
			//Utterance utterance=new Utterance(talk.getContent());
			Content content=new Content(talk.getText());
			//System.out.println(talk.getIdx()+" "+utterance.getTopic());
			Agent speaker=talk.getAgent();
			switch (content.getTopic()){
			case VOTE:
				break;
			case COMINGOUT:
				coInfo.putCORole(speaker, content.getRole(),talk.getDay(),talk.getIdx(),true);
				break;
			case DIVINED:
				coInfo.putCORole(speaker, Role.SEER,talk.getDay(),talk.getIdx(),false);
				abilityInfo.addAbilityResult(speaker, content, talk.getDay());
				break;
			case IDENTIFIED:
				coInfo.putCORole(speaker, Role.MEDIUM,talk.getDay(),talk.getIdx(),false);
				abilityInfo.addAbilityResult(speaker, content, talk.getDay());
				break;
			case GUARDED:
				coInfo.putCORole(speaker, Role.BODYGUARD,talk.getDay(),talk.getIdx(),false);
				abilityInfo.addAbilityResult(speaker, content, talk.getDay());
				break;
			default:
				break;

			}
			readTalkNum++;
		}
		abilityInfo.checkNonVillagerTeam();
	}
	
	private void readWhisperList(){
		List<Talk> whisperList=gameInfo.getWhisperList();
		for(int i=readWhisperNum;i<whisperList.size();i++){
			Talk talk=whisperList.get(i);
			whisperInfo.addWhisperTalk(talk);
			readWhisperNum++;
		}
	}
	

}
