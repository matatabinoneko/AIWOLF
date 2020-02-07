package org.aiwolf.ReGEX.H.WindMill;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class WindMillBasePlayer implements Player{
  Agent me;
  int day;
  boolean canTalk;
  boolean canWhisper;
  GameInfo currentGameInfo;

  List<Agent> aliveOthers = new ArrayList<>();
  //-----------------------------------------
  List<Agent> aliveAlls = new ArrayList<>();
  //-----------------------------------------
  List<Agent> executedAgents = new ArrayList<>();
  List<Agent> killedAgents = new ArrayList<>();
  List<Judge> divinationList = new ArrayList<>();
  List<Judge> identList = new ArrayList<>();

  Deque<Content> talkQueue = new LinkedList<>();
  Deque<Content> whisperQueue = new LinkedList<>();

  Agent voteCandidate;
  Agent declaredVoteCandidate;
  Agent attackVoteCandidate;
  Agent declaredAttackVoteCandidate;
  Map<Agent, Role> comingoutMap = new HashMap<>();

  int talkListHead;

  List<Agent> humans = new ArrayList<>();
  List<Agent> werewolves = new ArrayList<>();

  //追加したリスト----------------------------------------------------------
  List<Agent> grayNoRoles = new ArrayList<>();
  List<Agent> werewolvesSeer = new ArrayList<>();
  List<Agent> werewolvesRole = new ArrayList<>();

//カウンター----------------------------------------------------------------
  Map<Agent, Agent> SuspectNow = new HashMap<>();
  Map<Agent, Agent> wantVote = new HashMap<>();
  List<Agent> DoubtMeNow = new ArrayList<>();

  Map<Agent, Integer> SuspectedCounter = new HashMap<>();
  Map<Agent, Integer> VotedCounter = new HashMap<>();
  Map<Agent, Integer> PleaseDivineCounter = new HashMap<>();
  Map<Agent, Integer> SilentAgentCounter = new HashMap<>();
  Map<Agent, Integer> DoubtMeCounter = new HashMap<>();


  List<Agent> werewolvesSuspected = new ArrayList<>();
  List<Agent> maybeVoted = new ArrayList<>();
  List<Agent> PleaseDivine = new ArrayList<>();

  List<Agent> SilentAgent = new ArrayList<>();
  List<Agent> DoubtMeAgent = new ArrayList<>();
  //----------------------------------------------------------------------



  protected boolean isAlive(Agent agent){
    return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
  }
  protected boolean isKilled(Agent agent){
    return killedAgents.contains(agent);
  }
  protected boolean isCo(Agent agent){
    return comingoutMap.containsKey(agent);
  }
  protected boolean isCo(Role role){
    return comingoutMap.containsValue(role);
  }
  protected boolean isHuman(Agent agent){
    return humans.contains(agent);
  }
  protected boolean isWerewolf(Agent agent){
    return werewolves.contains(agent);
  }
  protected <T> T randomSelect(List<T> list){
    if(list.isEmpty()){
      return null;
    }else{
      return list.get((int) (Math.random() * list.size()));
    }
  }
  public String getName(){
    return "WindMillBasePlayer";
  }
  public void initialize(GameInfo gameInfo, GameSetting gameSetting){
    day = -1;
    me = gameInfo.getAgent();
    aliveOthers = new ArrayList<>(gameInfo.getAliveAgentList());
    aliveOthers.remove(me);
    executedAgents.clear();
    killedAgents.clear();
    divinationList.clear();
    identList.clear();
    comingoutMap.clear();
    humans.clear();
    werewolves.clear();
    //-追加------------------------------
    werewolvesSeer.clear();
    grayNoRoles.clear();
    werewolvesRole.clear();

    //カウンター--------------------------
    SuspectNow.clear();
    DoubtMeNow.clear();
    wantVote.clear();

    SuspectedCounter.clear();
    VotedCounter.clear();
    PleaseDivineCounter.clear();
    SilentAgentCounter.clear();
    DoubtMeCounter.clear();

    werewolvesSuspected.clear();
    maybeVoted.clear();

    werewolvesSuspected.clear();
    maybeVoted.clear();
    PleaseDivine.clear();

    SilentAgent = new ArrayList<>(gameInfo.getAliveAgentList());
    DoubtMeAgent.clear();

    //----------------------------------
    aliveAlls = new ArrayList<>(gameInfo.getAliveAgentList());
    //----------------------------------
  }
  public void update(GameInfo gameInfo){
    currentGameInfo = gameInfo;
    if (currentGameInfo.getDay() == day + 1){
      day = currentGameInfo.getDay();
      return;
    }
    addExecutedAgent(currentGameInfo.getLatestExecutedAgent());
    for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++){
      Talk talk = currentGameInfo.getTalkList().get(i);
      Agent talker = talk.getAgent();
      if(talker == me){
        continue;
      }
      Content content = new Content(talk.getText());
      //喋った人を寡黙リストから消していきます。
      SilentAgent.remove(content.getSubject());
      switch (content.getTopic()){
        case COMINGOUT:
          comingoutMap.put(talker, content.getRole());
          break;
        case DIVINED:
          divinationList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
          break;
        case IDENTIFIED:
          identList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
          break;
        case ESTIMATE:
        	if(content.getRole() == Role.WEREWOLF){
        		SuspectNow.put(content.getSubject(), content.getTarget());
        		if(content.getTarget() == me){
        			DoubtMeNow.add(content.getSubject());
        		}
        	}
        	break;
        case VOTE:
        	wantVote.put(content.getSubject(), content.getTarget());
        	break;
        default:
          break;
      }
    }
    talkListHead = currentGameInfo.getTalkList().size();
  }
  public void dayStart(){
    canTalk = true;
    canWhisper = false;
    if(currentGameInfo.getRole() == Role.WEREWOLF){
      canWhisper = true;
    }
    talkQueue.clear();
    whisperQueue.clear();
    declaredVoteCandidate = null;
    voteCandidate = null;
    declaredAttackVoteCandidate = null;
    attackVoteCandidate = null;
    talkListHead = 0;
    addExecutedAgent(currentGameInfo.getExecutedAgent());
    if (!currentGameInfo.getLastDeadAgentList().isEmpty()){
      addKilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
    }
  }
  private void addExecutedAgent(Agent executedAgent){
    if(executedAgent != null){
      aliveOthers.remove(executedAgent);
      aliveAlls.remove(executedAgent);
      if(!executedAgents.contains(executedAgent)){
        executedAgents.add(executedAgent);
      }
    }
  }
  private void addKilledAgent(Agent killedAgent){
    if(killedAgent != null){
      aliveOthers.remove(killedAgent);
      aliveAlls.remove(killedAgent);
      if(!killedAgents.contains(killedAgent)){
        killedAgents.add(killedAgent);
      }
    }
  }
  protected void chooseVoteCandidate(){
  }
  public String talk(){
    chooseVoteCandidate();
    if(voteCandidate != null && voteCandidate != declaredVoteCandidate){
      talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
      declaredVoteCandidate = voteCandidate;
    }
    return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
  }
  protected void chooseAttackVoteCandidate(){
  }
  public String whisper(){
    chooseAttackVoteCandidate();
    if(attackVoteCandidate != null && attackVoteCandidate != declaredAttackVoteCandidate){
      whisperQueue.offer(new Content(new AttackContentBuilder(attackVoteCandidate)));
      declaredAttackVoteCandidate = attackVoteCandidate;
    }
    return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
  }
  public Agent vote(){
    canTalk = false;
    chooseVoteCandidate();
    return voteCandidate;
  }
  public Agent attack(){
	  canWhisper = false;
	  chooseAttackVoteCandidate();
	  canWhisper = true;
	  return attackVoteCandidate;
  }
  public Agent divine(){
    return null;
  }
  public Agent guard(){
    return null;
  }
  public void finish(){

  }
}
