package jp.halfmoon.inaba.aiwolf.request;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import jp.halfmoon.inaba.aiwolf.lib.VoteAnalyzer;


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
			workReq.vote = 1.00 + voteAnalyzer.getReceiveVoteCount(agent) * 0.02 * (1 + voteAnalyzer.getReceiveVoteCount(gameInfo.getAgent()) * 0.05);
			Requests.add(workReq);
		}


		// 自分が狂人時の処理
		if( args.agi.latestGameInfo.getRole() == Role.POSSESSED ){
			for( Agent agent : gameInfo.getAliveAgentList() ){
				// 自分狂視点で確黒か
				if( args.agi.selfRealRoleViewInfo.isFixBlack(agent.getAgentIdx()) ){
					Agent target = voteAnalyzer.getVoteTarget(agent);
					// 投票先を宣言しているか
					if( target != null ){
						// 狼様と同じ場所に投票する
						workReq = new Request(target);
						workReq.vote = 1.2;
						Requests.add(workReq);
					}
				}
			}
		}


		return Requests;

	}

}
