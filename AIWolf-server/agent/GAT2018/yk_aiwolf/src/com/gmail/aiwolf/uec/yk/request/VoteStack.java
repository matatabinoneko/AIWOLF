package com.gmail.aiwolf.uec.yk.request;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;


/**
 * s“®ípu•[d‚Ëv
 * –{—ˆ‘z’è‚·‚é“®‚«‚ÍA’Ê‚é‰Â”\«‚ª‚ ‚é’İ‚è‚ğ’ñˆÄ‚·‚é‚±‚ÆB
 */
public final class VoteStack extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;



		// éŒ¾Ï‚İ“Š•[æ‚Ì•ªÍ‚ğæ“¾
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// “¾‚Ä‚¢‚é•[”‚É‰‚¶‚Ä•[‚ğd‚Ë‚é
		for( Agent agent : gameInfo.getAliveAgentList() ){
			workReq = new Request(agent);
			workReq.vote = 1.00 + voteAnalyzer.getReceiveVoteCount(agent) * 0.05 * (1 + voteAnalyzer.getReceiveVoteCount(gameInfo.getAgent()) * 0.10);
			Requests.add(workReq);
		}

		// ‚S”­Œ¾ŒãˆÈ~A©•ª‚ğœ‚¢‚Ä‚O•[–á‚¢‚Í“Š•[—v‹‚ğ‰º‚°‚éi4“ú–Ú‚Ü‚Åj
		if( gameInfo.getAliveAgentList().size() >= 7 && args.agi.getMyTalkNum() > 3 ){
			// ©•ª‚Ì“Š•[æ‚ğæ“¾
			Agent voteAgent = voteAnalyzer.getVoteTarget(gameInfo.getAgent());
			for( Agent agent : gameInfo.getAliveAgentList() ){
				int voteCount = voteAnalyzer.getReceiveVoteCount(agent) - ( agent.equals(voteAgent) ? 1 : 0 );
				if( voteCount <= 0 ){
					workReq = new Request(agent);
					workReq.vote = 0.5;
					Requests.add(workReq);
				}
			}

		}

		return Requests;

	}

}
