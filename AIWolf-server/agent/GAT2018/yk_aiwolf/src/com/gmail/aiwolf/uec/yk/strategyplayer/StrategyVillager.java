package com.gmail.aiwolf.uec.yk.strategyplayer;

import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.*;

import com.gmail.aiwolf.uec.yk.learn.AgentStatistics;


public class StrategyVillager extends AbstractBaseStrategyPlayer {


	/**
	 * ç¹§?½³ç¹ï½³ç¹§?½¹ç¹åŒ»Î›ç¹§?½¯ç¹§?½¿
	 * @param agentStatistics
	 */
	public StrategyVillager(AgentStatistics agentStatistics) {
		super(agentStatistics);
	}

	@Override
	public String talk() {
		try{
			String workString;

			// è¬šæ?•ï½¥?½¨èœˆåŒ»?½’éšª?¿½ç¸º?½£ç¸º?½¦ç¸º?¿½ç¸º?½ªç¸º?¿½è£?½´èœ·åŒ»?¿½âˆµçœ?ç¸ºåŠ±?¼è¬šæ?•ï½¥?½¨èœˆåŒ»?½’éš§?½±ç¸º?¿½
			if( declaredPlanningVoteAgent == null ){
				// è¬šæ?•ï½¥?½¨èœˆåŒ»?½’èŸç”»å³©ç¸ºå¶?½?
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// è­›ï¿½é‚¨ã‚ˆå¾‹ç¸º?½¯è¿¢?½¼ç¸º?½®è“ï½¿èµç¤¼?½¥?½¨ç¹§å¸äºŸç¸ºèˆŒâ—†ç¹§âˆï½®?½£éšª?¿½ç¸ºåŠ±â†‘ç¸º?¿½ç¸²ã‚?½®?½£éšª?¿½ç¸º?½¯è­›ï¿½é‚¨ã‚ˆå¾‹è‰?½¥èŸæ‚¶â†’ç¸ºå¶?½?
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// é€‹ï½ºéš§?½±
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// é€ä»£?¼èœˆåŒ»?½’è¿¢?½¼ç¸º?½®è??½ºè¬¨?½°è‰ï½¥è³é¡˜ï½¨?¿½ç¸º?½£ç¸º?½¦ç¸º?¿½ç¸º?½ªç¸ºä»£?½Œç¸º?½°éš§?½±ç¸º?¿½
			if( agi.talkedSuspicionAgentList.size() < agi.gameSetting.getRoleNumMap().get(Role.WEREWOLF) ){
				// é€ä»£?¼èœˆåŒ»?½’éš§?½±ç¸ºå‘æšé¶?¿½ç¹§è²å™è •åŠ±?¼?ç¸²âˆå™è •åŠ±ã€’ç¸ºé˜ªâ€»ç¸º?¿½ç¹§å¾Œï¿½?½°éš§?½±ç¸º?¿½
				workString = getSuspicionTalkString();
				if( workString != null ){
					return workString;
				}
			}

			// è¬šæ?•ï½¥?½¨èœˆåŒ»?½’èŸç”»å³©ç¸ºå¶?½‹è£?½´èœ·åŒ»?¿½âˆµçœ?ç¸ºåŠ±?¼è¬šæ?•ï½¥?½¨èœˆåŒ»?½’éš§?½±ç¸º?¿½
			if( declaredPlanningVoteAgent != planningVoteAgent ){
				// è¬šæ?•ï½¥?½¨èœˆåŒ»?½’èŸç”»å³©ç¸ºå¶?½?
				actionUI.voteAgent = planningVoteAgent;
				declaredPlanningVoteAgent = planningVoteAgent;

				// è­›ï¿½é‚¨ã‚ˆå¾‹ç¸º?½¯è¿¢?½¼ç¸º?½®è“ï½¿èµç¤¼?½¥?½¨ç¹§å¸äºŸç¸ºèˆŒâ—†ç¹§âˆï½®?½£éšª?¿½ç¸ºåŠ±â†‘ç¸º?¿½ç¸²ã‚?½®?½£éšª?¿½ç¸º?½¯è­›ï¿½é‚¨ã‚ˆå¾‹è‰?½¥èŸæ‚¶â†’ç¸ºå¶?½?
				if( agi.latestGameInfo.getAliveAgentList().size() > 4 ){
					// é€‹ï½ºéš§?½±
					String ret = TemplateTalkFactory.vote( Agent.getAgent(planningVoteAgent) );
					return ret;
				}
			}

			// é€ä»£?¼èœˆåŒ»?½’éš§?½±ç¸ºå‘æšé¶?¿½ç¹§è²å™è •åŠ±?¼?ç¸²âˆå™è •åŠ±ã€’ç¸ºé˜ªâ€»ç¸º?¿½ç¹§å¾Œï¿½?½°éš§?½±ç¸º?¿½
			workString = getSuspicionTalkString();
			if( workString != null ){
				return workString;
			}

			// è«?½¡é€•ï½¨èœˆåŒ»?½’éš§?½±ç¸ºå‘æšé¶?¿½ç¹§è²å™è •åŠ±?¼?ç¸²âˆå™è •åŠ±ã€’ç¸ºé˜ªâ€»ç¸º?¿½ç¹§å¾Œï¿½?½°éš§?½±ç¸º?¿½
			workString = getTrustTalkString();
			if( workString != null ){
				return workString;
			}

			// éš§?½±ç¸ºå©?½ºä¹â?²è¾Ÿï½¡ç¸º?¿½è£?½´èœ·åŒ»?¿½?½›verç¹§å®šï½¿æ–â?
			return TemplateTalkFactory.over();

		}catch(Exception ex){

			// è“å¥?½¤æ‚¶?½’èœ€é˜ªã›ç¹ï½­ç¹ï½¼ç¸ºå¶?½?
			if(isRethrowException){
				throw ex;
			}

			// è‰ï½¥è³ä¹ï¿½âˆ½?½¾å¥?½¤ä¹ŸåŒ±é€•æ»“å?¾ç¸º?½®è‰ï½£è­–ï½¿èœ?½¦é€??¿½ç¹§å®šï½¡å¾Œâ‰§(è¬Œï½»ç¹§é›?¿½?½¤ç¸ºå¾Œâ‰ ç¹§ä¹Î“ç¹§?½½ç¹ï¿½ç¹å³¨?¿½?½®è£?½´èœ·åŒ»?¿½?½¯è¬Œï½»ç¸º?¿½)

			// ç¹§?½¨ç¹ï½©ç¹ï½¼è­ã‚…?¿½?½¯overç¹§å®šï½¿æ–â?
			return TemplateTalkFactory.over();

		}


	}



}
