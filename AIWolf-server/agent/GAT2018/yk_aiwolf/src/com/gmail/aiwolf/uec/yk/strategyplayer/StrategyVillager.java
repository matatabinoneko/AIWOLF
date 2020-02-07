package com.gmail.aiwolf.uec.yk.strategyplayer;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.*;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;


public class StrategyVillager extends AbstractBaseStrategyPlayer {


	/**
	 * 繧?��繝ｳ繧?��繝医Λ繧?��繧?��
	 * @param agentStatistics
	 */
	public StrategyVillager(AgentStatistics agentStatistics) {
		super(agentStatistics);
	}

	@Override
	public String talk() {
		try{
			String workString;

			// 謚�?�･?��蜈医?��險?��縺?��縺?��縺?��縺?��縺?��蝣?��蜷医?��∵�?縺励?��謚�?�･?��蜈医?��隧?��縺?��
			if( declaredPlanningVoteAgent == null ){
				// 謚�?�･?��蜈医?��螟画峩縺吶?�?
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// 譛�邨よ律縺?��迢?��縺?��萓ｿ荵礼?��?��繧帝亟縺舌◆繧∝ｮ?��險?��縺励↑縺?��縲�?��?��險?��縺?��譛�邨よ律莉?��螟悶→縺吶?�?
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

				// 譛�邨よ律縺?��迢?��縺?��萓ｿ荵礼?��?��繧帝亟縺舌◆繧∝ｮ?��險?��縺励↑縺?��縲�?��?��險?��縺?��譛�邨よ律莉?��螟悶→縺吶?�?
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



}
