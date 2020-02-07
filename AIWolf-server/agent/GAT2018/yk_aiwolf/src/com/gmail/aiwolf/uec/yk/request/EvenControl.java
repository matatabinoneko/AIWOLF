package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.Common;
import com.gmail.aiwolf.uec.yk.lib.Judge;
import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 行動戦術「偶数調整」
 */
public final class EvenControl extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// 生存者５人でなければ実行しない
		if( gameInfo.getAliveAgentList().size() != 5 ){
			return Requests;
		}


		// 宣言済み投票先の分析を取得
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// 最多票を得ているエージェントを襲撃先にする
		for( Agent agent : voteAnalyzer.getMaxReceiveVoteAgent() ){
			workReq = new Request(agent);
			workReq.attack = 30.0;
			Requests.add(workReq);
		}

		return Requests;
	}

}
