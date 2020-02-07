package com.gmail.aiwolf.uec.yk.strategyplayer;

import java.util.List;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.TemplateWhisperFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;
import com.gmail.aiwolf.uec.yk.lib.*;


public class StrategyWerewolf extends AbstractBaseStrategyPlayer {


	/**
	 * 繧?��繝ｳ繧?��繝医Λ繧?��繧?��
	 * @param agentStatistics
	 */
	public StrategyWerewolf(AgentStatistics agentStatistics) {
		super(agentStatistics);
	}

	@Override
	public void dayStart() {

		try{
			super.dayStart();

			// 鬨吶?��蛻?��螳壹?��?��霑ｽ蜉�
			if( fakeRole == Role.SEER ){
				if( agi.latestGameInfo.getDay() > 0 ){
					addFakeSeerJudge();
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
	public String talk() {

		try{
			String workString;

			// 遐ｴ邯?��譎ゅ?��?��逋ｺ險?��
			if( agi.selfViewInfo.wolfsidePatterns.isEmpty() ){

				// 縺?��繧翫�?縺医�?髴海O
				if( !isCameOut ){
					isCameOut = true;

					String ret = TemplateTalkFactory.comingout( me, Role.MEDIUM );
					return ret;
				}

				return TemplateTalkFactory.over();

			}


			// 鬨吶?���?�ｹ閨?��縺?��?��?��?��?��?��?��
			if( fakeRole == Role.SEER ){
				if( !isCameOut ){
					isCameOut = true;

					String ret = TemplateTalkFactory.comingout( me, fakeRole );
					return ret;
				}
			}


			// CO貂医?��?��蝣?��蜷?��
			if( isCameOut ){

				// 蜊�CO貂�
				if( fakeRole == Role.SEER ){

					// 譛ｪ蝣?��蜻翫?��?��邨先棡繧貞�?��蜻翫�?繧?��
					if( agi.reportSelfResultCount < agi.selfInspectList.size() ){

						Judge reportJudge = agi.selfInspectList.get( agi.selfInspectList.size() - 1 );

						// 蝣?��蜻頑ｸ医∩縺?��莉ｶ謨?��繧貞｢励?�?縺?��
						agi.reportSelfResultCount++;

						// 逋ｺ隧?��
						String ret = TemplateTalkFactory.divined( Agent.getAgent(reportJudge.targetAgentNo), reportJudge.result );
						return ret;
					}

				}

			}

			// 謚�?�･?��蜈医?��險?��縺?��縺?��縺?��縺?��縺?��蝣?��蜷医?��∵�?縺励?��謚�?�･?��蜈医?��隧?��縺?��
			if( declaredPlanningVoteAgent == null ){
				// 謚�?�･?��蜈医?��螟画峩縺吶?�?
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 譛�邨よ律縺?��迢?��縺?��萓ｿ荵礼?��?��繧帝亟縺舌◆繧∝ｮ?��險?��縺励↑縺?��縲�?��?��險?��縺?��譛�邨よ律莉?��螟悶→縺吶?���?��医→縺?��縺?��蜷咲岼縺?��譚代→蜍輔�?�繧偵�?繧上○繧具?��?��
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// 逋ｺ隧?��
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// 逍代?��蜈医?��迢?��縺?���??��謨?��莉･荳願ｨ?��縺?��縺?��縺?��縺?��縺代?��縺?��隧?��縺?��
			if( agi.talkedSuspicionAgentList.size() < agi.gameSetting.getRoleNumMap().get(Role.WEREWOLF) ){
				// 逍代?��蜈医?��隧?��縺呎枚遶?��繧貞叙蠕励?�?縲∝叙蠕励〒縺阪※縺?��繧後�?��隧?��縺?��
				workString = getSuspicionTalkString();
				if( workString != null ){
					return workString;
				}
			}

			// 謚�?�･?��蜈医?��螟画峩縺吶?��蝣?��蜷医?��∵�?縺励?��謚�?�･?��蜈医?��隧?��縺?��
			if( declaredPlanningVoteAgent != planningVoteAgent ){
				// 謚�?�･?��蜈医?��螟画峩縺吶?�?
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 譛�邨よ律縺?��迢?��縺?��萓ｿ荵礼?��?��繧帝亟縺舌◆繧∝ｮ?��險?��縺励↑縺?��縲�?��?��險?��縺?��譛�邨よ律莉?��螟悶→縺吶?���?��医→縺?��縺?��蜷咲岼縺?��譚代→蜍輔�?�繧偵�?繧上○繧具?��?��
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// 逋ｺ隧?��
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// 逍代?��蜈医?��隧?��縺呎枚遶?��繧貞叙蠕励?�?縲∝叙蠕励〒縺阪※縺?��繧後�?��隧?��縺?��
			workString = getSuspicionTalkString();
			if( workString != null ){
				return workString;
			}

			// 菫?��逕ｨ蜈医?��隧?��縺呎枚遶?��繧貞叙蠕励?�?縲∝叙蠕励〒縺阪※縺?��繧後�?��隧?��縺?��
			workString = getTrustTalkString();
			if( workString != null ){
				return workString;
			}

			// 隧?��縺吩?��九�?�辟｡縺?��蝣?��蜷医?��?��ver繧定ｿ斐�?
			return TemplateTalkFactory.over();

		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)

			// 繧?��繝ｩ繝ｼ譎ゅ?��?��over繧定ｿ斐�?
			return TemplateTalkFactory.over();
		}

	}


	/** 鬨吶?���?�ｹ閨?��繧定ｨ?��螳壹�?繧?�� */
	@Override
	protected void setFakeRole(){

		// 0譌･逶?��0逋ｺ險?��縺?��譛�蛻昴?��?��whisper譎ゅ↓險?��螳?��
		if( fakeRole == null ){

			// 邏�譚鷹?��吶?��縺?��蛻�?�?蛹?��
			setFakeRole(Role.VILLAGER);

			// 遒ｺ�?��縺?��蜊�縺?��鬨吶?���?��域圻螳夲?��?��
			if( Math.random() < 0.2 ){
				setFakeRole(Role.SEER);
				// 莉悶↓蜊?��鬨吶?��縺後＞繧後�?��邏�譚鷹?��吶?��縺?��謌ｻ縺?��
				for( ComingOut co : agi.wisperComingOutList ){
					if( co.isEnable() && co.agentNo != me.getAgentIdx() && co.role == Role.SEER ){
						setFakeRole(Role.VILLAGER);
					}
				}
			}

		}

	}


	@Override
	public String whisper(){

		try{

			// 鬨吶?���?�ｹ閨?��縺?��蝣?��蜻?��
			if( declaredFakeRole != fakeRole ){
				declaredFakeRole = fakeRole;
				return TemplateWhisperFactory.comingout(me, fakeRole);
			}

			// 蝎帙∩蜈医?��?��蝣?��蜻?��
			if( declaredPlanningAttackAgent != actionUI.attackAgent && actionUI.attackAgent != null ){
				declaredPlanningAttackAgent = actionUI.attackAgent;
				return TemplateWhisperFactory.attack( Agent.getAgent(actionUI.attackAgent) );
			}

			return TemplateWhisperFactory.over();
		}catch(Exception ex){
			// 萓句?��悶?��蜀阪せ繝ｭ繝ｼ縺吶?�?
			if(isRethrowException){
				throw ex;
			}

			// 莉･荳九�∽?��句?��也匱逕滓�?�縺?��莉｣譖ｿ�?���??��繧定｡後≧(謌ｻ繧�?��?��縺後≠繧九Γ繧?��繝�繝峨?��?��蝣?��蜷医?��?��謌ｻ縺?��)

			// 繧?��繝ｩ繝ｼ譎ゅ?��?��over繧定ｿ斐�?
			return TemplateTalkFactory.over();
		}
	}


	@Override
	public Agent attack() {

		try{

			if( actionUI.attackAgent == null ){
				return null;
			}

			//TODO null縺?��縺励※繧�?享�?九�?�隘?��謦?��縺輔ｌ繧九▲縺?��縺?��?��溯?��∬?��?��譟ｻ
			//		// 逕溷?��倩?��?��?��?��穂ｺ?��?��?��亥?��?��蛻大?��鯉ｼ比ｺ?��?��?��峨〒蛛?��謨?��隱?��謨?��
			//		if( agi.latestGameInfo.getAliveAgentList().size() == 5 ){
			//			return null;
			//		}

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

			// 螳?��險?��辟｡隕悶〒謚ｼ縺苓ｾ?��繧√�?��蜍昴※繧狗�?�諷九°
			Integer ppVoteAgentNo = getSuspectedPPVoteAgent();
			if( ppVoteAgentNo != null ){
				return Agent.getAgent(ppVoteAgentNo);
			}

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


	/**
	 * 螳?��險?��辟｡隕悶〒蜍昴※繧句?��?��蜷医?��∵兜�?�?��蜈医?��蜿�??��励�?繧?��
	 * @return
	 */
	public Integer getSuspectedPPVoteAgent(){

		List<Integer> aliveWolfList = agi.getAliveWolfList();

		// 縺�?→�?��台?��?��譚代?��蜷翫?��縺?��蜍昴※繧狗�?�諷九°
		if( aliveWolfList.size() >= Common.getRestExecuteCount(agi.latestGameInfo.getAliveAgentList().size()) ){

			GameInfo gameInfo = agi.latestGameInfo;

			// 螂�謨?��騾?��陦?��
			if( agi.latestGameInfo.getAliveAgentList().size() % 2 == 1 ){

				// 繧?��繝ｼ繧?��繧?��繝ｳ繝域?��弱?��?��謚�?�･?���?亥相蜈医?��蜿�??��励�?繧?��
				Integer[] voteTarget = new Integer[agi.gameSetting.getPlayerNum() + 1];
				for( Agent agent : gameInfo.getAliveAgentList() ){
					voteTarget[agent.getAgentIdx()] = agi.getSaidVoteAgent(agent.getAgentIdx());
					if( voteTarget[agent.getAgentIdx()] == null ){
						// 譛ｪ螳?��險?���??��縺後＞繧句?��?��蜷医?��?��荳咲?��?��螳夊ｦ∫?��?��縺後≠繧九�?��縺?��繧?��繧√�?�縺翫?��
						return null;
					}
				}

				// 繧?��繝ｼ繧?��繧?��繝ｳ繝域?��弱?��?��陲?��謚�?�･?��謨?��繧貞叙蠕励�?繧?��
				int[] voteReceiveNum = new int[agi.gameSetting.getPlayerNum() + 1];
				int[] voteReceiveNumWithoutMe = new int[agi.gameSetting.getPlayerNum() + 1];
				for( int i = 1; i < voteTarget.length; i++ ){
					// 謚�?�･?��螳?��險?��繧偵き繧?��繝ｳ繝医�?繧?��
					if( voteTarget[i] != null ){
						voteReceiveNum[voteTarget[i]]++;
					}
					// 閾?��蛻?��莉･螟悶?��?��謚�?�･?��螳?��險?��繧偵き繧?��繝ｳ繝医�?繧?��
					if( i != gameInfo.getAgent().getAgentIdx() && voteTarget[i] != null ){
						voteReceiveNumWithoutMe[voteTarget[i]]++;
					}
				}

				// 譛�螟夂･?��縺?��繧?��繝ｼ繧?��繧?��繝ｳ繝医?��?��逾?��謨?��繧貞叙蠕励�?繧?��
				int maxVoteCount = 0;
				for( int i = 1; i < voteTarget.length; i++ ){
					if( voteReceiveNumWithoutMe[i] > maxVoteCount ){
						maxVoteCount = voteReceiveNum[i];
					}
				}

				// 譛�螟夂･?��繧貞ｾ励※縺?��繧九お繝ｼ繧?��繧?��繝ｳ繝医?��蜿�??��?��
				for( int i = 1; i < voteReceiveNum.length; i++ ){
					if( voteReceiveNumWithoutMe[i] >= maxVoteCount ){
						// 閾?��蛻?��縺?��謚�?�･?��繧帝勁縺上�?�迢?��縺悟衰繧峨?��縺昴≧?��?��?��
						if( aliveWolfList.contains(i) ){
							// 莉悶↓譛�螟夂･?��縺?���??��髢薙�?�縺?��繧九�?�繧画款縺苓ｾ?��繧√�?��蜍晏�?
							for( int j = 1; j < voteReceiveNum.length; j++ ){
								if( voteReceiveNumWithoutMe[j] >= maxVoteCount && !aliveWolfList.contains(j) ){
									return j;
								}
							}
							// LW?��?��?��莉悶↓譛�螟夂･?��縺後＞縺?��縺?��
							if( aliveWolfList.size() <= 1 ){
								for( int j = 1; j < voteReceiveNum.length; j++ ){
									// 1逾?��蟾?��縺?���??��髢薙�?�縺?��繧後�?��謚ｼ縺苓ｾ?��繧薙�?�繝ｩ繝ｳ繝�繝�
									if( voteReceiveNumWithoutMe[j] >= maxVoteCount - 1 && !aliveWolfList.contains(j) ){
										return j;
									}
								}
							}
						}
					}
				}

			}

		}

		return null;

	}


	/**
	 * 蜊�縺?��蛻?��螳壹?��霑?��蜉�縺吶?�?
	 */
	private void addFakeSeerJudge(){

		GameInfo gameInfo = agi.latestGameInfo;

		// 蛻?��螳壼?��医?��?��蛻?��螳夂ｵ先棡縺?��莉ｮ險?��螳?��
		int inspectAgentNo = latestRequest.getMaxInspectRequest().agentNo;
		Species result = Species.HUMAN;

		// 隘ｲ謦?��縺輔ｌ縺溘お繝ｼ繧?��繧?��繝ｳ繝医?��?��蜿�??��?��
		Agent attackedAgent = agi.latestGameInfo.getAttackedAgent();

		// 遒ｺ�?��縺?��莉ｲ髢薙ｒ蜊?��縺?��
		if( Math.random() < 0.2 ){
			for( Agent agent : agi.latestGameInfo.getRoleMap().keySet() ){
				// 莉ｲ髢鍋蕎縺後げ繝ｬ繝ｼ縺?��逕溷?��倥?�?縺?��縺?��繧九°
				if( agi.agentState[agent.getAgentIdx()].causeofDeath == CauseOfDeath.ALIVE &&
				    !agi.selfViewInfo.isFixBlack(agent.getAgentIdx()) &&
				    !agi.selfViewInfo.isFixBlack(agent.getAgentIdx()) ){
					inspectAgentNo = agent.getAgentIdx();
					result = Species.HUMAN;
				}
			}
		}

		// 遒ｺ髴翫→繝ｩ繧?��繝ｳ縺悟牡繧後ｋ縺?��繧牙�?�螳壹?��蜿崎ｻ?��縺吶?�?
		List<Integer> mediums = agi.getEnableCOAgentNo(Role.MEDIUM);
		if( mediums.size() == 1 &&  agi.agentState[mediums.get(0)].causeofDeath == CauseOfDeath.ALIVE ){
			if( agi.latestGameInfo.getDay() > 1 && agi.latestGameInfo.getExecutedAgent().getAgentIdx() == inspectAgentNo ){
				// �?��蛻大?��医?��蜊?��縺?��蝣?��蜷医?��?��髴翫?��?��蛻?��螳壹′蠕後□縺励↓縺?��繧九�?縺?��繧り�?��諷?��
				result = ( agi.isWolf(inspectAgentNo) ) ? Species.WEREWOLF : Species.HUMAN;
			}else{
				if( result == Species.HUMAN ){
					// 逋ｽ繧貞�?��縺励�?蝣?��蜷医?��?��隕也せ繧�?��?��螳壹�?繧?��
					ViewpointInfo future = new ViewpointInfo(agi.selfViewInfo);
					future.removeWolfPattern(inspectAgentNo);
					// 譛ｪ譚･隕也せ縺?��髴願�?��縺檎｢?��螳壻?��?��螟悶↑繧峨Λ繧?��繝ｳ縺悟牡繧後ｋ縺溘ａ蛻?��螳壼渚霆?��
					if( future.isFixWolfSide(mediums.get(0)) ){
						result = Species.WEREWOLF;
					}
				}else{
					// 鮟�?�?��蜃?��縺励�?蝣?��蜷医?��?��隕也せ繧�?��?��螳壹�?繧?��
					ViewpointInfo future = new ViewpointInfo(agi.selfViewInfo);
					future.removePatternFromJudge(agi.latestGameInfo.getAgent().getAgentIdx(), inspectAgentNo, Species.WEREWOLF);
					// 譛ｪ譚･隕也せ縺?��髴願�?��縺檎｢?��螳壻?��?��螟悶↑繧峨Λ繧?��繝ｳ縺悟牡繧後ｋ縺溘ａ蛻?��螳壼渚霆?��
					if( future.isFixWolfSide(mediums.get(0)) ){
						result = Species.HUMAN;
					}
				}
			}
		}

		// �?��雎｡�??��縺瑚�?��蛻?��隕也せ縺?��遒ｺ螳夐ｻ偵?��?��蝣?��蜷医?��?��鮟貞�?��縺励?��陦後≧
		if( agi.selfViewInfo.isFixBlack(inspectAgentNo) ){
			result = Species.WEREWOLF;
		}

		// 蜊�縺翫≧縺?��縺励�?蜈医′蝎帙∪繧後◆
		if( attackedAgent != null && attackedAgent.getAgentIdx() == inspectAgentNo ){
			// 蝎帙∩蜈医↓縺?���??��髢灘�?�螳壹?��蜃?��縺?��
			result = Species.HUMAN;
		}

		List<Integer> aliveWolfList = agi.getAliveWolfList();


		// 縺�?→�?��台?��?��譚代?��蜷翫?��縺?��蜍昴※繧狗�?�諷九°
		if( aliveWolfList.size() >= Common.getRestExecuteCount(agi.latestGameInfo.getAliveAgentList().size()) ){
			for( Agent agent : agi.latestGameInfo.getAliveAgentList() ){
				// 莉ｲ髢薙�?��迢?��縺?��縺?��縺?��縺?��縲?��縺九▽縲?��閾?��蛻?��隕也せ遒ｺ逋ｽ縺?��縺?��縺?��縺?��縲?��縺九▽髴�?��呵?��懊�?�縺?��縺?��縺?��縲?���??��迚ｩ縺?��
				if( agi.latestGameInfo.getRoleMap().get(agent) == null &&
				    !agi.selfViewInfo.isFixWhite(agent.getAgentIdx()) &&
				    agi.agentState[agent.getAgentIdx()].comingOutRole != Role.MEDIUM ){
					// 隧?��蠖楢?��?��縺?��鮟�?�?��蜃?��縺?��
					inspectAgentNo = agent.getAgentIdx();
					result = Species.WEREWOLF;
					break;
				}
			}
		}



		Judge newJudge = new Judge( me.getAgentIdx(), inspectAgentNo, result, null );

		agi.addFakeSeerJudge(newJudge);

	}


}
