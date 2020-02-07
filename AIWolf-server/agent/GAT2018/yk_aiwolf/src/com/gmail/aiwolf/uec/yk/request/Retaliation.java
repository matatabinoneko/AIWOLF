package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.guess.InspectedWolfsidePattern;
import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;


/**
 * 行動戦略「しっぺ返し�?
 */
public final class Retaliation extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		GameInfo gameInfo = args.agi.latestGameInfo;


		if( gameInfo.getDay() < 4 ){
			// 宣�?した投票先を取�?
			VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

			for( Agent agent : gameInfo.getAliveAgentList() ){
				if( args.agi.isWolf(agent.getAgentIdx())  ){
					continue;
				}
				double attackRate = 1.0;
				for( Agent target : gameInfo.getAliveAgentList() ){
					if( !agent.equals(target) && args.agi.isWolf(target.getAgentIdx()) ){
						double rate = args.agi.getSuspicionWerewolfRate(agent.getAgentIdx(), target.getAgentIdx());

						attackRate *= Math.pow(Math.max(rate, 0.1) + 0.5, 0.2);
					}
				}

				workReq = new Request(agent.getAgentIdx());
				workReq.vote = attackRate;
				workReq.inspect = attackRate;
				Requests.add(workReq);
			}
		}


		return Requests;

	}

}
