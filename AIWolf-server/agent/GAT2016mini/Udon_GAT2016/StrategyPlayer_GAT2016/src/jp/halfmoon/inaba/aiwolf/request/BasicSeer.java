package jp.halfmoon.inaba.aiwolf.request;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import jp.halfmoon.inaba.aiwolf.lib.VoteAnalyzer;


/**
 * 行動戦術「基本占戦術」
 */
public final class BasicSeer extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// 宣言済み投票先の分析を取得
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// 最多票を得ているエージェントは占い先から除外する
		for( Agent agent : voteAnalyzer.getMaxReceiveVoteAgent() ){
			workReq = new Request(agent);
			workReq.inspect = 0.05;
			Requests.add(workReq);
		}

		return Requests;
	}

}
