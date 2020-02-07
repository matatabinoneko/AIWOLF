package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;


/**
 * s“®ípu’İ‚è‰ñ”ğv
 */
public final class AvoidExecute_Werewolf extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// ‰“ú‚ÍˆŒY‚ª”­¶‚µ‚È‚¢‚Ì‚Å•K—v‚È‚µ
		if( gameInfo.getDay() <= 0 ){
			return Requests;
		}

		// ‚S”­Œ¾–Ú‚Ü‚Å‚Í‚Æ‚è‚ ‚¦‚¸s‚í‚È‚¢
		if( args.agi.getMyTalkNum() < 4 ){
			return Requests;
		}


		// éŒ¾Ï‚İ“Š•[æ‚Ì•ªÍ‚ğæ“¾
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);



		// ‚P•[‚Å‚à“ü‚Á‚Ä‚¢‚é
		if( !voteAnalyzer.getMaxReceiveVoteAgent().isEmpty() ){

			// “¾•[”MAX‚ğæ“¾
			int receiveVoteCountMax = voteAnalyzer.getReceiveVoteCount(voteAnalyzer.getMaxReceiveVoteAgent().get(0));

			// ©•ª‚Ì“¾•[”‚ğæ“¾
			int receiveVoteCountWolf = 0;

			for( int wolf : args.agi.getAliveWolfList() ){
				int receiveVoteCount = voteAnalyzer.receiveVoteCount.getOrDefault(Agent.getAgent(wolf), 0);
				receiveVoteCountWolf = Math.max(receiveVoteCountWolf, receiveVoteCount);
			}

			// “¾•[”‚ª‹É’[‚É­‚È‚¢‚È‚çs‚í‚È‚¢
			if( receiveVoteCountWolf <= 1 ){
				return Requests;
			}

			// ©•ª‚Ì“¾•[”‚ª“¾•[”MAX-1ˆÈã‚È‚ç’İ‚è‰ñ”ğŒvZ‚ª•K—vi-1‚ÍƒqƒXƒeƒŠƒVƒX‚Ì‚½‚ßj
			if( receiveVoteCountWolf >= receiveVoteCountMax - 1 ){

				// ¶‘¶ƒG[ƒWƒFƒ“ƒg‘–¸
				for( Agent agent : gameInfo.getAliveAgentList() ){

					// ˜T‚ÍƒXƒLƒbƒv
					if( args.agi.getAliveWolfList().contains(agent.getAgentIdx()) ){
						continue;
					}

					// ƒG[ƒWƒFƒ“ƒg‚Ì“¾•[”‚ğæ“¾
					int receiveVoteCount = voteAnalyzer.receiveVoteCount.getOrDefault(agent, 0);

					// ©•ª‚Ì“Š•[æ‚Å‚ ‚ê‚Î•[”‚ğ-1ŒvZ‚·‚é
					if( agent.equals( voteAnalyzer.getVoteTarget(gameInfo.getAgent()) ) ){
						receiveVoteCount--;
					}

					if( receiveVoteCount + 1 > receiveVoteCountWolf ){
						// ‚ ‚Æ‚P•[‚Å˜T‚æ‚è“¾•[”‚ª‘½‚­‚È‚é
						workReq = new Request(agent);
						workReq.vote = 1.15;
						Requests.add(workReq);
					}else if( receiveVoteCount + 1 >= receiveVoteCountWolf ){
						// ‚ ‚Æ‚P•[‚Å˜T‚Æ“¾•[”‚ª“¯‚¶‚É‚È‚é
						workReq = new Request(agent);
						workReq.vote = 1.1;
						Requests.add(workReq);
					}

				}

			}

		}

		return Requests;

	}

}
