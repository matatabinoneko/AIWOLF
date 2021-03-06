package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;


/**
 * 行動戦術「狂ムーブ」
 */
public final class PossessedMove extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;



		// 宣言済み投票先の分析を取得
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);


		for( Agent agent : gameInfo.getAliveAgentList() ){
			// 自分狂視点で確黒か
			if( args.agi.selfRealRoleViewInfo.isFixBlack(agent.getAgentIdx()) ){
				// 狼様に投票しない
				workReq = new Request(agent);
				workReq.vote = 0.9;
				Requests.add(workReq);

				Agent target = voteAnalyzer.getVoteTarget(agent);
				// 投票先を宣言しているか
				if( target != null ){
					// 狼様と同じ場所に投票する
					workReq = new Request(target);
					workReq.vote = 1.2;
					Requests.add(workReq);
				}
			}else if( args.agi.selfRealRoleViewInfo.isFixWhite(agent.getAgentIdx()) ){
				// 非狼に投票する
				workReq = new Request(agent);
				workReq.vote = 1.1;
				Requests.add(workReq);
			}
		}


		return Requests;

	}

}
