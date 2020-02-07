package com.gmail.aiwolf.uec.yk.strategyplayer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

import javax.print.DocFlavor.URL;

import libsvm.*;
import libsvm.svm;


import java.io.IOException;


import org.aiwolf.client.*;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

import com.gmail.aiwolf.uec.yk.guess.*;
import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.learn.MyAgentStatistics;
import com.gmail.aiwolf.uec.yk.lib.AdvanceGameInfo;
import com.gmail.aiwolf.uec.yk.lib.AgentParameter;
import com.gmail.aiwolf.uec.yk.lib.AgentParameterItem;
import com.gmail.aiwolf.uec.yk.lib.WolfsidePattern;
import com.gmail.aiwolf.uec.yk.request.*;
import com.gmail.aiwolf.uec.yk.request.Request;

/**
 * 蜈ｨ縺?��縺?��蠖ｹ閨?��縺?��繝吶?��?��繧?��縺?��縺?��繧九け繝ｩ繧?��
 */
public abstract class AbstractBaseStrategyPlayer implements Player{

	/** 諡?��蠑ｵ繧?��繝ｼ繝��??��蝣?�� */
	protected AdvanceGameInfo agi;

	/** 陦悟虚繧定ｨ?��螳壹�?繧九◆繧√�?��謫?��莨?��UI */
	protected ActionUI actionUI = new ActionUI();

	/** 莉�?�律謚�?�･?��縺励?��縺?��縺?��諤昴▲縺?��縺?��繧九�励Ξ繧?��繝､繝ｼ */
	protected Integer planningVoteAgent;

	/** 閾?��蛻?��縺梧怙�?�後�?�螳?��險?��縺励�?縲梧兜�?�?��縺励?��縺?��縺?��諤昴▲縺?��縺?��繧九�励Ξ繧?��繝､繝ｼ縲?�� */
	protected Integer declaredPlanningVoteAgent;

	/** 閾?��蛻?��縺梧怙�?�後�?�陦後▲縺滓�?��??�� */
	protected AnalysisOfGuess latestGuess;

	/** 閾?��蛻?��縺梧怙�?�後�?�陦後▲縺溯?��悟虚隕∵?��?�� */
	protected AnalysisOfRequest latestRequest;

	/** 鬨吶?���?�ｹ閨?�� */
	protected Role fakeRole;

	/** 螳?��險?��貂医∩縺?��鬨吶?���?�ｹ閨?�� */
	protected Role declaredFakeRole;

	/** 閾?��蛻?��縺梧怙�?�後�?�螳?��險?��縺励�?縲瑚･?��謦?��縺励?��縺?��縺?��諤昴▲縺?��縺?��繧九�励Ξ繧?��繝､繝ｼ縲?�� */
	protected Integer declaredPlanningAttackAgent;

	/** CO貂医° */
	protected boolean isCameOut;


	/** 蛟�?�?��?��縺?��謖�?�▲縺?��縺?��繧区ュ蝣?�� */
	protected AgentParameter agentParam = new AgentParameter();

	/** 菫�?怏縺吶?��謗ｨ�??��謌ｦ逡?�� */
	protected ArrayList<HasGuessStrategy> guessStrategys = new ArrayList<HasGuessStrategy>();

	/** 菫�?怏縺吶?��陦悟虚謌ｦ逡?�� */
	protected ArrayList<HasActionStrategy> actionStrategys = new ArrayList<HasActionStrategy>();


	/** 繧?��繝ｼ繧?��繧?��繝ｳ繝域?��弱?��?��邨?��險域ュ蝣?�� */
	protected AgentStatistics agentStatistics = new AgentStatistics();

	/** finish()繝｡繧?��繝�繝峨?��譛牙柑縺?��縺吶?��縺具?��?��finish縺鯉ｼ貞屓譚･繧倶?��榊�?��蜷医∈縺?���?��蠢懶?��?�� */
	protected boolean isEnableFinish;


	// 隱?��謨?��讖溯?��?��

	/** 萓句?��也匱逕滓�?�縺?�� 萓句?��悶?��?���?阪せ繝ｭ繝ｼ繧定｡後≧縺?�� */
	protected final boolean isRethrowException = false;

	/** 騾比ｸ?��邨碁℃繧貞�?��蜉帙�?繧九° */
	protected final boolean isPrintPassageTalk = false;

	/** 邨�??��?��邨先棡繧貞�?��蜉帙�?繧九° */
	protected final boolean isPrintFinishTalk = false;

	/** �?��鄙�?�畑縺?��繝�繝ｼ繧?��繧貞�?��蜉帙�?繧九° */
	protected final boolean isPutLearnData = false;

	Deque<Content> talkQueue = new LinkedList<>();

	// 繝�繝�?�ャ繧?��逕ｨ

	/** Update()縺?��縺九°縺?��縺滓�?�髢薙�?��縺?��縺?��譛�髟ｷ縺?��繧�??��?��?��?��医ョ繝�?�ャ繧?��逕ｨ?��?��?�� */
	protected long MaxUpdateTime = Long.MIN_VALUE;

	/** Update()縺?��縺九°縺?��縺滓�?�髢薙�?�譛�髟ｷ縺?��繧?��繧?��繝溘Φ繧?��?��?��医ョ繝�?�ャ繧?��逕ｨ?��?��?�� */
	protected String MaxUpdateTiming;

	Agent me;
	
	int day;
	int guesscount;
	svm_model daymodel;
	int gamecount = -1;
	/**
	 * 繧?��繝ｳ繧?��繝医Λ繧?��繧?��
	 * @param agentStatistics
	 */
	public AbstractBaseStrategyPlayer(AgentStatistics agentStatistics){
		this.agentStatistics = agentStatistics;
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {

		try{
			//super.initialize(gameInfo, gameSetting);
			
			me = gameInfo.getAgent();
			// 繧?��繝ｼ繝�髢句?��区凾縺?��finish()繧呈�?�蜉?��縺?��縺吶?�?
			gamecount += 1;
			isEnableFinish = true;
			guesscount = 0;
			// 繝｡繝ｳ繝仙��?�?蛹?��
			planningVoteAgent = null;
			declaredPlanningVoteAgent = null;
			declaredFakeRole = null;
			declaredPlanningAttackAgent = null;
			fakeRole = null;
			isCameOut = false;
			guessStrategys = new ArrayList<HasGuessStrategy>();
			actionStrategys = new ArrayList<HasActionStrategy>();
			agentParam = new AgentParameter();


			// 諡?��蠑ｵ繧?��繝ｼ繝��??��蝣?��縺?��蛻�?�?蛹?��
			agi = new AdvanceGameInfo(gameInfo, gameSetting);


			// 蛟�?�?��?��縺?��謖�?�▲縺?��縺?��繧区ュ蝣?��
			agentParam.setParam(AgentParameterItem.FAVOR_RATE, 0.20, true);
			agentParam.setParam(AgentParameterItem.VOTE_RATE_WTOW, 0.8, true);

			agentParam.setParam(AgentParameterItem.DEVINE_RATE_WTOW_WHITE, 1.05, true);
			agentParam.setParam(AgentParameterItem.DEVINE_RATE_WTOW_BLACK, 0.8, true);


			// 謗ｨ�??��謌ｦ逡?��繧定ｨ?��螳?��
			HasGuessStrategy guessStrategy;

			guessStrategy = new HasGuessStrategy(new FirstImpression(), 1.0);
			guessStrategys.add(guessStrategy);
			guessStrategy = new HasGuessStrategy(new FromGuardRecent(), 1.0);
			guessStrategys.add(guessStrategy);
			guessStrategy = new HasGuessStrategy(new Formation_Basic(), 1.0);
			guessStrategys.add(guessStrategy);
			guessStrategy = new HasGuessStrategy(new COTiming(), 1.0);
			guessStrategys.add(guessStrategy);
			//guessStrategy = new HasGuessStrategy(new AttackObstacle_Guess(), 1.0);
			//guessStrategys.add(guessStrategy);
			guessStrategy = new HasGuessStrategy(new JudgeRecent(), 1.0);
			guessStrategys.add(guessStrategy);
			guessStrategy = new HasGuessStrategy(new AgentSta(), 1.0);
			guessStrategys.add(guessStrategy);
			// 譚鷹劵蝟ｶ縺?��縺?��縺?��逕ｨ縺?��繧区耳�??��
			if( gameInfo.getRole().getTeam() == Team.VILLAGER ){
				guessStrategy = new HasGuessStrategy(new Noise(), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new VoteRecent(), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new VoteTarget(), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new FromAgentStatistics_B(agentStatistics), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new Learn_1dVoteSaid(agentStatistics), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new Learn_0dEstimateSaid(agentStatistics), 1.0);
				guessStrategys.add(guessStrategy);
//				guessStrategy = new HasGuessStrategy(new Learn_0dProtectCompany(agentStatistics), 1.0);
//				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new Learn_COAndDay(agentStatistics), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new Learn_VoteStack(agentStatistics), 1.0);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new Learn_1dCompetitionDevine(agentStatistics), 1.0);
				guessStrategys.add(guessStrategy);
			}

			// 迢?��髯?��蝟ｶ縺?��縺?��縺?��逕ｨ縺?��繧区耳�??��
			if( gameInfo.getRole().getTeam() == Team.WEREWOLF ){
				guessStrategy = new HasGuessStrategy(new Noise(), 0.5);
				guessStrategys.add(guessStrategy);
				guessStrategy = new HasGuessStrategy(new VoteRecent(), 0.5);
				guessStrategys.add(guessStrategy);
			}

			// 迢?��縺?��縺?��縺?��逕ｨ縺?��繧区耳�??��
			if( gameInfo.getRole() == Role.WEREWOLF ){
				guessStrategy = new HasGuessStrategy(new Favor(), 1.0);
				guessStrategys.add(guessStrategy);
			}
			/*try{
				  File file = new File("guess_guessStrategys_after.txt");
				  FileWriter filewriter = new FileWriter(file);

				  filewriter.write("こんにちは" +guessStrategys.size());

				  filewriter.close();
				}catch(IOException e){
				  System.out.println(e);
				}*/
			
			// 陦悟虚謌ｦ逡?��繧定ｨ?��螳?��
			HasActionStrategy actStrategy;
			actStrategy = new HasActionStrategy(new FixInfo(), 1.0);
			actionStrategys.add(actStrategy);
			actStrategy = new HasActionStrategy(new FromGuess(), 1.0);
			actionStrategys.add(actStrategy);

			// 譚鷹劵蝟ｶ縺?��縺?��縺?��逕ｨ縺?��繧玖｡悟虚
			if( gameInfo.getRole().getTeam() == Team.VILLAGER ){
				actStrategy = new HasActionStrategy(new RoleWeight(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new VoteStack(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new ReticentExecute(), 1.0);
				actionStrategys.add(actStrategy);
//				actStrategy = new HasActionStrategy(new BalanceExecute(), 1.0);
//				actionStrategys.add(actStrategy);
//				actStrategy = new HasActionStrategy(new CheckmateExecute(), 1.0);
//				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new Retaliation(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new AvoidExecute(), 1.0);
				actionStrategys.add(actStrategy);
			}

			// 蜊�縺?��縺?��縺?��逕ｨ縺?��繧玖｡悟虚
			if( gameInfo.getRole() == Role.SEER ){
				actStrategy = new HasActionStrategy(new BasicSeer(), 1.0);
				actionStrategys.add(actStrategy);
			}

			// 迢?��縺?��縺?��縺?��逕ｨ縺?��繧玖｡悟虚
			if( gameInfo.getRole() == Role.BODYGUARD ){
				actStrategy = new HasActionStrategy(new BasicGuard(), 1.0);
				actionStrategys.add(actStrategy);
			}


			// 迢?��髯?��蝟ｶ縺?��縺?��縺?��逕ｨ縺?��繧玖｡悟虚
			if( gameInfo.getRole().getTeam() == Team.WEREWOLF ){
				actStrategy = new HasActionStrategy(new RoleWeight_Wolfside(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new BasicAttack(), 1.0);
				actionStrategys.add(actStrategy);
			}

			// 迢?��縺?��縺?��縺?��逕ｨ縺?��繧玖｡悟虚
			if( gameInfo.getRole() == Role.WEREWOLF ){
				actStrategy = new HasActionStrategy(new VoteStack(), 3.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new AttackObstacle(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new EvenControl(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new AvoidExecute_Werewolf(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new SeerExecute(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new PowerPlay_Werewolf(), 1.0);
				actionStrategys.add(actStrategy);
			}

			// 迢�??��?��縺?��縺?��縺?��逕ｨ縺?��繧玖｡悟虚
			if( gameInfo.getRole() == Role.POSSESSED ){
				actStrategy = new HasActionStrategy(new PowerPlay_Possessed(), 1.0);
				actionStrategys.add(actStrategy);
				actStrategy = new HasActionStrategy(new PossessedMove(), 1.0);
				actionStrategys.add(actStrategy);
			}

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			// Do nothing
		}


	}


	@Override
	public void dayStart() {

		try{
			// 陦悟虚險?��螳壹?��繝ｪ繧?��繝�繝医�?繧?��
			actionUI.reset();
			planningVoteAgent = null;
			declaredPlanningVoteAgent = null;

			// 繝�繝�?�ャ繧?��逕ｨ
			if( false && agi.latestGameInfo.getDay() >= 1 ){
				for( int i=1; i<=15; i++ ){
					double singleScore = latestGuess.getSingleWolfPattern(i).score;
					double teamScore = latestGuess.getMostValidWolfPattern(i).score;
					double voteScore = latestRequest.TotalRequest.get(i-1).vote;
					double inspectScore = latestRequest.TotalRequest.get(i-1).inspect;
					double guardScore = latestRequest.TotalRequest.get(i-1).guard;
					double attackScore = latestRequest.TotalRequest.get(i-1).attack;
					System.out.println( i + "," + singleScore + "," + teamScore  + "," + voteScore  + "," + inspectScore  + "," + guardScore  + "," + attackScore );
				}
			}
			daymodel = null;
			if(day == 1){
				 try {
					//FileReader fr = new FileReader("modelfile/alldata_day1.model");
					java.net.URL u = this.getClass().getResource(".//lib//modelfile//alldata_day1.model");
					daymodel = svm.svm_load_model(u.toString());
				} catch (IOException e) {
					// TODO 自動生成された catch ブロ�?ク
					e.printStackTrace();
				}
			}else if(day == 2){
				 try {
					java.net.URL u = this.getClass().getResource(".//lib//modelfile//alldata_day2.model");
					daymodel = svm.svm_load_model(u.toString());
				} catch (IOException e) {
					// TODO 自動生成された catch ブロ�?ク
					e.printStackTrace();
				}
			}else if(day == 3){
				 try {
						java.net.URL u = this.getClass().getResource(".//lib//modelfile//alldata_day3.model");
					daymodel = svm.svm_load_model(u.toString());
				} catch (IOException e) {
					// TODO 自動生成された catch ブロ�?ク
					e.printStackTrace();
				}
			}else if(day > 3){
				 try {
					java.net.URL u = this.getClass().getResource(".//lib//modelfile//alldata_day4.model");
					daymodel = svm.svm_load_model(u.toString());
				} catch (IOException e) {
					// TODO 自動生成された catch ブロ�?ク
					e.printStackTrace();
				}
			}

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			// Do nothing
		}

	}


	@Override
	public void update(GameInfo gameInfo) {

		try{

			// 譎る俣險域ｸ?��髢句?��?��
			long starttime = System.currentTimeMillis();
			
			day = gameInfo.getDay();
			//super.update(gameInfo);

			// 諡?��蠑ｵ繧?��繝ｼ繝��??��蝣?��縺?��譖ｴ�?��
			agi.update(gameInfo);
			int i = 0;
			if(day <= 1){
				i = 1;
			}else if(day >= 4){
				i = 4;
			}else{
				i = day;
			}
			
			// 譌･莉俶峩�?���?���??��譎ゆ?��?��螟悶↓謗ｨ�??��邉ｻ�?���??��繧定｡後≧
			if( !agi.isDayUpdate() ){

				// 鬨吶?���?�ｹ閨?��縺?��險?��螳?��
				setFakeRole();

				// 謗ｨ�??��繧定｡後≧
				execGuess();

				// 陦悟虚�?育?��?��繧貞�?��繧後�?
				execActionReserve();

			}

			// 譎る俣險域ｸ?��邨�??��?��
			long endtime = System.currentTimeMillis();
			long updatetime = endtime - starttime;

			// update()縺?���?���??��譎る俣縺梧怙髟ｷ縺?��繧芽?��俶?��?��
			if( updatetime > MaxUpdateTime ){
				MaxUpdateTime = updatetime;
				MaxUpdateTiming = new StringBuilder().append(gameInfo.getDay()).append("日目 ").append(gameInfo.getTalkList().size()).append("発�?").toString();
			}

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
					try {
						throw ex;
					} catch (Exception e) {
						// TODO 自動生成された catch ブロ�?ク
						e.printStackTrace();
					}
				
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			// Do nothing
		}

	}


	@Override
	public Agent attack() {

		try{
			// 蝟九ｉ縺帙�?
			if( isPrintPassageTalk ){
				putDebugMessage(actionUI.attackAgent.toString() + "を襲�?する");
			}

			if( actionUI.attackAgent == null ){
				return null;
			}

			return Agent.getAgent(actionUI.attackAgent);

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			return agi.latestGameInfo.getAliveAgentList().get(0);

		}

	}


	@Override
	public Agent vote() {

		try{

			if( actionUI.voteAgent == null ){
				// 謚�?�･?��蜈医?��螳?��險?���?��譚･縺?��縺?��縺?��縺?��蝣?��蜷医?��∵兜�?�?��縺励?��縺?��縺?��諤昴▲縺?��縺?��縺溯?��?��縺?��謚�?�･?��
				if( planningVoteAgent == null ){
					return null;
				}
				return Agent.getAgent(planningVoteAgent);
			}
			return Agent.getAgent(actionUI.voteAgent);

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			return agi.latestGameInfo.getAliveAgentList().get(0);

		}

	}


	@Override
	public Agent guard() {

		try{

			if( actionUI.guardAgent == null ){
				return null;
			}
			return Agent.getAgent(actionUI.guardAgent);

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			return agi.latestGameInfo.getAliveAgentList().get(0);

		}

	}


	@Override
	public Agent divine() {

		try{

			if( actionUI.inspectAgent == null ){
				return null;
			}
			return Agent.getAgent(actionUI.inspectAgent);

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			return agi.latestGameInfo.getAliveAgentList().get(0);

		}

	}


	@Override
	public String whisper(){
		return null;
	}


	@Override
	public void finish() {

		try{

			// finish()縺檎┌蜉ｹ?��?��亥?��?���??��貂医∩?��?��峨↑繧牙叉謚懊?�?繧?��
			if( !isEnableFinish ){
				return;
			}

			// finish()繧抵?��大屓蜃?���??��縺励�?繧臥┌蜉?��縺?��縺吶?�?
			isEnableFinish = false;

			// 邨�??��?��譎ゅ?��?���?���??��繧貞幕繧峨○繧九°
			if( isPrintFinishTalk ){
				// 蝟九ｉ縺帙ｋ�?��医�?縺?��縺托ｼ?��

				GameInfo gameInfo = agi.latestGameInfo;
				ArrayList<Integer> wolves = new ArrayList<Integer>();
				ArrayList<Integer> possess = new ArrayList<Integer>();
				for( int i = 1; i<= agi.gameSetting.getPlayerNum(); i++ ){
					Role role = gameInfo.getRoleMap().get( Agent.getAgent(i) );
					if( role == Role.WEREWOLF ){
						wolves.add(i);
					}else if( role == Role.POSSESSED ){
						possess.add(i);
					}
				}
				WolfsidePattern dummyWolfside = new WolfsidePattern( wolves ,possess );
				InspectedWolfsidePattern dummyInspect = latestGuess.getPattern(dummyWolfside);
				if( dummyInspect != null ){
					double dummyWolfsideScore = latestGuess.getPattern(dummyWolfside).score;
					putDebugMessage("螳�?�?縺?���??��險?��縺?�� " + dummyWolfside.toString() + String.format(" (Score:%.5f) ", dummyWolfsideScore));
				}


				WolfsidePattern mostValidWolfside = latestGuess.getMostValidPattern().pattern;
				double mostValidWolfsideScore = latestGuess.getMostValidPattern().score;
				putDebugMessage("譛�邨よ律謗ｨ�??��縺?�� " + mostValidWolfside.toString() + String.format(" (Score:%.5f) ", mostValidWolfsideScore));

				// 繝�繝�?�ャ繧?��繝｡繝�繧?��繝ｼ繧?��縺?���?��蜉�
				putDebugMessage("update() 譛�髟ｷ譎る俣縺?��" + MaxUpdateTime + "ms (" + MaxUpdateTiming + ")");
			}

			// 閾?��繧?��繝ｼ繧?��繧?��繝ｳ繝医?��?��邨?��險医?��?��譖ｴ�?��
			updateMyStatistics();

			// 邨?��險医?��?��譖ｴ�?��
			updateStatistics();

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)
			// Do nothing
		}



	}


	/** 鬨吶?���?�ｹ閨?��繧定ｨ?��螳壹�?繧?�� */
	protected void setFakeRole(){
		setFakeRole(null);
	}


	/** 鬨吶?���?�ｹ閨?��繧定ｨ?��螳壹�?繧?�� */
	protected void setFakeRole(Role role){
		fakeRole = role;
		agi.fakeRole = role;
	}


	/**
	 * 謗ｨ�??��繧定｡後≧
	 * @throws IOException 
	 */
	protected void execGuess() throws IOException{

		GameInfo gameInfo = agi.latestGameInfo;

		GuessManager guessManager = new GuessManager(agi.gameSetting.getPlayerNum());
		ArrayList<Guess> guesses;

		// 謗ｨ�??��謌ｦ逡?��縺?��縺?��蠑墓�?�縺?��險?��螳?��
		GuessStrategyArgs args = new GuessStrategyArgs();
		args.agi = agi;
		args.agentParam = agentParam;
		//int i = 0;
		/*try{
			  File file = new File("guess_start_before.txt");
			  FileWriter filewriter = new FileWriter(file);

			  filewriter.write("こんにちは"+i);

			  filewriter.close();
			}catch(IOException e){
			  System.out.println(e);
			}*/
		/*if( !agi.selfViewInfo.wolfsidePatterns.isEmpty() ){
			
			i += 1;
			
		}*/

		// 蜷?��謗ｨ�??��謌ｦ逡?��繧?��繝ｩ繧?��縺九ｉ謗ｨ�??��繧貞叙蠕�
		
		/*for( HasGuessStrategy hasStrategy : guessStrategys ){
			
			guesses = hasStrategy.strategy.getGuessList(args);
			guessManager.addGuess(ReceivedGuess.newGuesses(guesses, hasStrategy.strategy));
			i += 1;
			try{
				  File file = new File("guess_start_now.txt");
				  FileWriter filewriter = new FileWriter(file);

				  filewriter.write("こんにちは" +i);

				  filewriter.close();
				}catch(IOException e){
				  System.out.println(e);
				}
		}*/
		for(int a = 0; a < guessStrategys.size(); a++){
			/*try{
				  File file = new File("guess_start_now.txt");
				  FileWriter filewriter = new FileWriter(file);

				  filewriter.write("こんにちは" +a);

				  filewriter.close();
				}catch(IOException e){
				  System.out.println(e);
				}*/
			guesses = guessStrategys.get(a).strategy.getGuessList(args);
			guessManager.addGuess(ReceivedGuess.newGuesses(guesses, guessStrategys.get(a).strategy));
			//i = a;
		}
		
		// 隍�謨?��縺?��謗ｨ�??��縺九ｉ蛻?��譫�?��先棡繧貞叙蠕励�?繧?��
		AnalysisOfGuess aguess;
		/*svm_model daymodel = null;
		if(day == 1){
			 try {
				daymodel = svm.svm_load_model("C:/Users/omuricelove/workspace/StrategyPlayer4/src/modelfile/alldata_day1.model");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロ�?ク
				e.printStackTrace();
			}
		}else if(day == 2){
			 try {
				daymodel = svm.svm_load_model("C:/Users/omuricelove/workspace/StrategyPlayer4/src/modelfile/alldata_day2.model");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロ�?ク
				e.printStackTrace();
			}
		}else if(day == 3){
			 try {
				daymodel = svm.svm_load_model("C:/Users/omuricelove/workspace/StrategyPlayer4/src/modelfile/alldata_day3.model");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロ�?ク
				e.printStackTrace();
			}
		}else if(day > 3){
			 try {
				daymodel = svm.svm_load_model("C:/Users/omuricelove/workspace/StrategyPlayer4/src/modelfile/alldata_day4.model");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロ�?ク
				e.printStackTrace();
			}
		}*/
		guesscount++;
		if( !agi.selfViewInfo.wolfsidePatterns.isEmpty() ){
			
			aguess = new AnalysisOfGuess(agi.gameSetting.getPlayerNum(), agi.selfViewInfo.wolfsidePatterns.values(), guessManager,guesscount,daymodel,day,gamecount);
		}else{
			//i = -1;
			// 菴輔ｉ縺九�?���??��逕ｱ縺?���??��險?��遐ｴ邯?��縺励�?蝣?��蜷医?��∝�?��隕也せ縺?��繧?��繧?��繝�繝��??��蝣?��縺?��縺?��繧貞盾�??��縺?��縺吶?�?
			
			aguess = new AnalysisOfGuess(agi.gameSetting.getPlayerNum(), agi.allViewSystemInfo.wolfsidePatterns.values(), guessManager,guesscount,daymodel,day,gamecount);
		}

		// 譛��?��縺?��謗ｨ�??��縺?��縺励※譬?��邏阪�?繧?��
		latestGuess = aguess;
		// 蝟九ｉ縺帙�?
		if( isPrintPassageTalk ){
			WolfsidePattern mostValidWolfside = aguess.getMostValidPattern().pattern;
			putDebugMessage(mostValidWolfside.toString() + "が�?�しい", gameInfo.getDay(), gameInfo.getTalkList().size());
		}

	}


	/**
	 * 陦悟虚�?育?��?��繧定｡後≧
	 */
	protected void execActionReserve(){

		RequestManager ReqManager = new RequestManager();

		// 陦悟虚謌ｦ逡?��縺?��縺?��蠑墓�?�縺?��險?��螳?��
		ActionStrategyArgs args = new ActionStrategyArgs();
		args.agi = agi;
		args.view = agi.selfViewInfo;
		args.aguess = latestGuess;
		args.parsonalData = agentParam;

		// 蜷?��陦悟虚謌ｦ逡?��繧?��繝ｩ繧?��縺九ｉ陦悟虚隕∵?���??��蜿�??��?��
		for( HasActionStrategy hasStrategy : actionStrategys ){
			ArrayList<Request> Requests = hasStrategy.strategy.getRequests(args);
			ReqManager.addRequest(ReceivedRequest.newRequests(Requests, hasStrategy.strategy));
		}

		// 陦悟虚隕∵?���??��髮?��險医?�?縲∝叙蠕励�?繧?��
		AnalysisOfRequest calcRequest = new AnalysisOfRequest(agi.gameSetting.getPlayerNum(), ReqManager.allRequest);


		// 蜷?��陦悟虚縺?���?��雎｡縺?��縺励※譛�繧�?��?��蠖薙↑莠?��迚ｩ繧貞叙蠕�
		int voteAgentNo = calcRequest.getMaxVoteRequest().agentNo;
		int guardAgentNo = calcRequest.getMaxGuardRequest().agentNo;
		int inspectAgentNo = calcRequest.getMaxInspectRequest().agentNo;
		int attackAgentNo = calcRequest.getMaxAttackRequest().agentNo;

		// 謚�?�･?���?亥?��壹→縺励※險俶?��?��?��?��亥?���?�?縺?��謚�?�･?��蜈医そ繝�繝医?��?��謚�?�･?��蜈医?��螳?��險?��縺励�?譎ゅ↓陦後≧?��?��?��
		planningVoteAgent = voteAgentNo;

		// 謚�?�･?��莉･螟悶?��?��蜷?��陦悟虚繧偵そ繝�繝�
		actionUI.guardAgent = guardAgentNo;
		actionUI.inspectAgent = inspectAgentNo;
		actionUI.attackAgent = attackAgentNo;

		// 譛��?��縺?��陦悟虚隕∵?���?→縺励※譬?��邏阪�?繧?��
		latestRequest = calcRequest;

	}


	/**
	 * 蝗樣∩CO縺悟ｿ?��隕�?�°縺?��蛻?���?��繧定｡後≧
	 * @return
	 */
	protected boolean isAvoidance(){

		GameInfo gameInfo = agi.latestGameInfo;

		// 謚�?�･?��螳?��險?��貂医∩繧?��繝ｼ繧?��繧?��繝ｳ繝医?��?��謨?��
		int voteAgentCount = 0;

		// 繧?��繝ｼ繧?��繧?��繝ｳ繝域?��弱?��?��謚�?�･?���?亥相蜈医?��蜿�??��励�?繧?��
		Integer[] voteTarget = new Integer[agi.gameSetting.getPlayerNum() + 1];
		for( Agent agent : gameInfo.getAliveAgentList() ){
			voteTarget[agent.getAgentIdx()] = agi.getSaidVoteAgent(agent.getAgentIdx());

			// 謚�?�･?��螳?��險?��貂医∩繧?��繝ｼ繧?��繧?��繝ｳ繝医?��?��繧?��繧?��繝ｳ繝�
			if( voteTarget[agent.getAgentIdx()] != null ){
				voteAgentCount++;
			}
		}

		// 繧?��繝ｼ繧?��繧?��繝ｳ繝域?��弱?��?��陲?��謚�?�･?��謨?��繧貞叙蠕励�?繧?��
		int[] voteReceiveNum = new int[agi.gameSetting.getPlayerNum() + 1];
		for( int i = 1; i < voteTarget.length; i++ ){
			if( voteTarget[i] != null ){
				voteReceiveNum[voteTarget[i]]++;
			}
		}

		// 譛�螟夂･?��縺?��繧?��繝ｼ繧?��繧?��繝ｳ繝医?��?��逾?��謨?��繧貞叙蠕励�?繧?��
		int maxVoteCount = 0;
		for( int i = 1; i < voteTarget.length; i++ ){
			if( voteReceiveNum[i] > maxVoteCount ){
				maxVoteCount = voteReceiveNum[i];
			}
		}


		// 蜷翫?��縺檎匱逕溘�?縺?��縺?��蛻�?律縺?��蝗樣∩CO縺?��蠢?��隕�?��?�縺?��
		if( gameInfo.getDay() < 1 ){
			return false;
		}

		// 3逋ｺ險?��逶?��縺?��縺?��縺?��蝗樣∩CO縺励↑縺?��(縺昴?��縺?��縺?��閾?��蛻?��縺薫ver繧定ｿ斐�?縺?��縺?��繧医≧縺?��縺吶?��縺薙�??)
		if( agi.getMyTalkNum() < 2 ){
			return false;
		}

		// 謚�?�･?��螳?��險?���??��縺悟ｰ代↑縺代?��縺?��蝗樣∩CO縺?��蠢?��隕�?��?�縺?��
		if( voteAgentCount < gameInfo.getAliveAgentList().size() * 0.65 ){
			return false;
		}

		// 譛�螟夂･?��繧貞ｾ励※縺?��繧後�?��蝗樣∩CO縺悟ｿ?��隕�
		if( voteReceiveNum[gameInfo.getAgent().getAgentIdx()] >= maxVoteCount ){
			return true;
		}

		return false;

	}


	/**
	 * 逍代?��蜈医?��隧?��縺呎枚遶?��繧貞叙蠕励�?繧?��(逋ｺ隧?��螻?���?��縺?��菫晏ｭ倥?��陦後≧)
	 * @return
	 */
	protected String getSuspicionTalkString(){

		// 逍代≧縺?��縺堺?��?��迚ｩ繧貞叙蠕励�?繧?��
		Integer suspicionAgentNo = getSuspicionTalkAgentNo();

		// 逍代≧縺?��縺堺?��?��迚ｩ縺後＞繧後�?��隧?��縺?��
		if( suspicionAgentNo != null ){
			//TODO 險俶?��?��縺帙�?縺?��閾?��蛻?��縺?��逋ｺ險?��繧定ｿ?��縺?��縺?��蜿�??��励?�?縺帙◆�?��縺瑚ｨ?��險域��?Φ縺?��縺励※縺?��濶?��縺?��
			// 逍代?��貂医→縺励※險俶?��?��縺吶?�?
			agi.talkedSuspicionAgentList.add(suspicionAgentNo);

			// 逋ｺ險?���??��螳?��繧定ｿ斐�?
			String ret = TemplateTalkFactory.estimate( Agent.getAgent(suspicionAgentNo), Role.WEREWOLF );
			return ret;
		}

		// 縺薙�?��逋ｺ險?��繧定｡後ｏ縺?��縺?��蝣?��蜷医?��?��ull繧定ｿ斐�?
		return null;

	}


	/**
	 * 菫?��逕ｨ蜈医?��隧?��縺呎枚遶?��繧貞叙蠕励�?繧?��(逋ｺ隧?��螻?���?��縺?��菫晏ｭ倥?��陦後≧)
	 * @return
	 */
	protected String getTrustTalkString(){

		// 譛��?��縺?��蛻?��譫�?��先棡繧貞叙蠕励�?繧?��
		AnalysisOfGuess aguess = latestGuess;

		// 譛�繧�?��?��蠖薙↑迢?��髯?��蝟ｶ縺?��繧?��繧?��繧?��繧貞叙蠕励�?繧?��
		double mostValidWolfsideScore = aguess.getMostValidPattern().score;

		// 逕溷?���??��?��縺?��蜈ｨ繧?��繝ｼ繧?��繧?��繝ｳ繝医?��襍ｰ譟ｻ
		for( Agent agent : agi.latestGameInfo.getAliveAgentList() ){

			// 閾?��蛻?��縺?��繧?��繧?��繝�繝�
			if( agent.equals(me) ){
				continue;
			}

			int agentNo = agent.getAgentIdx();

			InspectedWolfsidePattern wolfPattern = aguess.getMostValidWolfPattern(agentNo);
			InspectedWolfsidePattern posPattern = aguess.getMostValidPossessedPattern(agentNo);

			double wolfScore = 0.0;
			double posScore = 0.0;
			if( wolfPattern != null ){
				wolfScore = wolfPattern.score;
			}
			if( posPattern != null ){
				posScore = posPattern.score;
			}

			// 迢?��繝ｻ迢�??��?��縺昴?��縺槭?��縺?��譛�螟ｧ繧?��繧?��繧?��縺悟ｰ上�?縺?��
			if( wolfScore < mostValidWolfsideScore * 0.4 &&
					posScore < mostValidWolfsideScore * 0.7 ){

				if( !agi.talkedTrustAgentList.contains(agentNo) ){

					// 菫?��逕ｨ貂医→縺励※險俶?��?��縺吶?�?
					agi.talkedTrustAgentList.add(agentNo);

					Role role;

					// 菴輔°CO縺励※縺?��繧九°
					if( agi.agentState[agentNo].comingOutRole != null ){
						// CO縺励�?蠖ｹ閨?��縺?��謗ｨ�??��縺吶?�?
						role = agi.agentState[agentNo].comingOutRole;
					}else{
						// 譚台?��?��縺?��謗ｨ�??��縺吶?�?
						role = Role.VILLAGER;
					}

					// 逋ｺ險?���??��螳?��繧定ｿ斐�?
					String ret = TemplateTalkFactory.estimate(agent, role);
					return ret;
				}
			}

		}

		// 縺薙�?��逋ｺ險?��繧定｡後ｏ縺?��縺?��蝣?��蜷医?��?��ull繧定ｿ斐�?
		return null;

	}


	/**
	 * 逍代?��蜈医→縺励※隧?��縺吶∋縺阪お繝ｼ繧?��繧?��繝ｳ繝育�?蜿?��繧貞叙蠕励�?繧?��
	 * @return 繧?��繝ｼ繧?��繧?��繝ｳ繝育�?蜿?��(Null縺?��蝣?��蜷医?��譛�)
	 */
	protected Integer getSuspicionTalkAgentNo(){

		// 譛��?��縺?��蛻?��譫�?��先棡繧貞叙蠕励�?繧?��
		AnalysisOfGuess aguess = latestGuess;

		// 譛�繧�?��?��蠖薙↑迢?��髯?��蝟ｶ繧貞叙蠕励�?繧?��
		WolfsidePattern mostValidWolfside = aguess.getMostValidPattern().pattern;

		// 逍代?��蜈医→縺励※逋ｺ險?��縺励※縺?��縺?��縺?��迢?��繧貞叙蠕励�?繧?��
		ArrayList<Integer> wolves = new ArrayList<Integer>();
		for( Integer wolf : mostValidWolfside.wolfAgentNo ){
			if( !agi.talkedSuspicionAgentList.contains(wolf) ){
				wolves.add(wolf);
			}
		}

		// �?��雎｡荳榊惠譎ゅ?��?��Null繧定ｿ斐�?
		if( wolves.isEmpty() ){
			return null;
		}

		return wolves.get(0);

	}


	/**
	 * 閾?��繧?��繝ｼ繧?��繧?��繝ｳ繝医?��?��邨?��險医?��?��譖ｴ�?��
	 */
	protected void updateMyStatistics(){

		GameInfo gameInfo = agi.latestGameInfo;
		WolfsidePattern lastGuessWolfPattern = latestGuess.getMostValidPattern().pattern;

		// 繧?��繝ｼ繝�謨?��+1
		MyAgentStatistics.gameCount += 1;

		// 譛ｫ霍ｯ繧?��繧?��繝ｳ繝�
		switch( agi.agentState[gameInfo.getAgent().getAgentIdx()].causeofDeath ){
			case ALIVE:
				MyAgentStatistics.aliveCount++;
				break;
			case ATTACKED:
				MyAgentStatistics.attackCount++;
				break;
			case EXECUTED:
				MyAgentStatistics.executeCount++;
				break;
			default:
				break;
		}

		// 迢?���?��隗｣謨?��縺?��繧?��繧?��繝ｳ繝�
		int wolfCorrectCount = 0;
		for( int wolf : lastGuessWolfPattern.wolfAgentNo ){
			if( gameInfo.getRoleMap().get(Agent.getAgent(wolf)) == Role.WEREWOLF ){
				wolfCorrectCount++;
			}
		}
		MyAgentStatistics.wolfCorrectCount[wolfCorrectCount]++;

		// update()縺?���?���??��譎る俣縺梧怙髟ｷ縺?��繧芽?��俶?��?��
		if( MaxUpdateTime > MyAgentStatistics.maxUpdateTime ){
			MyAgentStatistics.maxUpdateTime = MaxUpdateTime;
		}

		// 繝�繝�?�ャ繧?��逕ｨ�?���?怜�励?��?��險俶?��?��
		MyAgentStatistics.debugStringBuilder.append(wolfCorrectCount).append(",");

	}


	protected void updateStatistics(){

		agentStatistics.addStatictics(agi);


//		for( int agentNo = 1; agentNo <= agi.gameSetting.getPlayerNum(); agentNo++ ){
//			HashMap<String, Double> weightOfGuess = agentStatistics.statistics.get(agentNo).weightOfGuess;
//
//			boolean isWolf = agi.latestGameInfo.getRoleMap().get(Agent.getAgent(agentNo)) == Role.WEREWOLF;
//			boolean isPossessed = agi.latestGameInfo.getRoleMap().get(Agent.getAgent(agentNo)) == Role.POSSESSED;
//
//			for( ReceivedGuess guess : latestGuess.getSingleWolfPattern(agentNo).guesses ){
//
//			}
//
//		}

	}


	/**
	 * 繝�繝�?�ャ繧?��繝｡繝�繧?��繝ｼ繧?��繧貞幕繧峨○縺?��縺?��
	 * @param str
	 */
	protected void putDebugMessage(String str){

		GameInfo gameInfo = agi.latestGameInfo;

		System.out.println("(Agent" + gameInfo.getAgent().getAgentIdx() + ") "
				+ "?��?��?�� "
				+ str
				+ "");

	}


	/**
	 * 繝�繝�?�ャ繧?��繝｡繝�繧?��繝ｼ繧?��繧貞幕繧峨○縺?��縺?��
	 * @param str
	 * @param day
	 * @param talkid
	 */
	protected void putDebugMessage(String str, int day, int talkid){

		GameInfo gameInfo = agi.latestGameInfo;

		System.out.println("(Agent" + gameInfo.getAgent().getAgentIdx() + ") "
				+ "?��?��?�� "
				+ "(" + day + "譌･ " + talkid + "逋ｺ險?��) "
				+ str
				+ "");

	}
	
	public String getName(){
		return "Udon";
	}

	/*public String talk(){
		return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
	}*/
	
}
