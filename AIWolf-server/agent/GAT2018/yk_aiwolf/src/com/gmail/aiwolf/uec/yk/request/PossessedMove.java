package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;


/**
 * s“®ípu‹¶ƒ€[ƒuv
 */
public final class PossessedMove extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;



		// éŒ¾Ï‚İ“Š•[æ‚Ì•ªÍ‚ğæ“¾
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);


		for( Agent agent : gameInfo.getAliveAgentList() ){
			// ©•ª‹¶‹“_‚ÅŠm•‚©
			if( args.agi.selfRealRoleViewInfo.isFixBlack(agent.getAgentIdx()) ){
				// ˜T—l‚É“Š•[‚µ‚È‚¢
				workReq = new Request(agent);
				workReq.vote = 0.9;
				Requests.add(workReq);

				Agent target = voteAnalyzer.getVoteTarget(agent);
				// “Š•[æ‚ğéŒ¾‚µ‚Ä‚¢‚é‚©
				if( target != null ){
					// ˜T—l‚Æ“¯‚¶êŠ‚É“Š•[‚·‚é
					workReq = new Request(target);
					workReq.vote = 1.2;
					Requests.add(workReq);
				}
			}else if( args.agi.selfRealRoleViewInfo.isFixWhite(agent.getAgentIdx()) ){
				// ”ñ˜T‚É“Š•[‚·‚é
				workReq = new Request(agent);
				workReq.vote = 1.1;
				Requests.add(workReq);
			}
		}


		return Requests;

	}

}
