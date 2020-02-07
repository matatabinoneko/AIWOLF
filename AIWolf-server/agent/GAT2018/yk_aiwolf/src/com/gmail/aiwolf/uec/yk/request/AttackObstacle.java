package com.gmail.aiwolf.uec.yk.request;

import java.util.ArrayList;
import java.util.HashSet;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;


/**
 * çsìÆêÌèpÅuà”å©ãÚÇ¢Åv
 */
public final class AttackObstacle extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;


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
			workReq.attack = attackRate;
			Requests.add(workReq);
		}



		return Requests;

	}

}
