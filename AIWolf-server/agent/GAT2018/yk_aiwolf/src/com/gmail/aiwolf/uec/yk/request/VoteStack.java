package com.gmail.aiwolf.uec.yk.request;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;


/**
 * 行動戦術「票重ね」
 * 本来想定する動きは、通る可能性がある吊りを提案すること。
 */
public final class VoteStack extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;



		// 宣言済み投票先の分析を取得
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// 得ている票数に応じて票を重ねる
		for( Agent agent : gameInfo.getAliveAgentList() ){
			workReq = new Request(agent);
			workReq.vote = 1.00 + voteAnalyzer.getReceiveVoteCount(agent) * 0.05 * (1 + voteAnalyzer.getReceiveVoteCount(gameInfo.getAgent()) * 0.10);
			Requests.add(workReq);
		}

		// ４発言後以降、自分を除いて０票貰いは投票要求を下げる（4日目まで）
		if( gameInfo.getAliveAgentList().size() >= 7 && args.agi.getMyTalkNum() > 3 ){
			// 自分の投票先を取得
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
