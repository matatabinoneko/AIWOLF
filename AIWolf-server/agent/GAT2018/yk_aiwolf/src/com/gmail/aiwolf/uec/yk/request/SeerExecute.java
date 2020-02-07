package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.Judge;
import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;
import java.util.List;


/**
 * 行動戦術「占い吊り」
 */
public final class SeerExecute extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// 初日は処刑が発生しないので必要なし
		if( gameInfo.getDay() <= 0 ){
			return Requests;
		}

		// ４発言目まではとりあえず行わない
		if( args.agi.getMyTalkNum() < 4 ){
			return Requests;
		}


		// 宣言済み投票先の分析を取得
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);



		// １票でも入っている
		if( !voteAnalyzer.getMaxReceiveVoteAgent().isEmpty() ){

			// 得票数MAXを取得
			int receiveVoteCountMax = voteAnalyzer.getReceiveVoteCount(voteAnalyzer.getMaxReceiveVoteAgent().get(0));

			List<Integer> seers = args.agi.getEnableCOAgentNo(Role.SEER);

			for( Integer seer : seers ){
				// 人外はスキップ
				if( args.agi.getAliveWolfList().contains(seer) ){
					continue;
				}
				for( Judge judge : args.agi.getSeerJudgeList() ){
					// 間違った判定を出したか
					if( (judge.result == Species.WEREWOLF) != args.agi.isWolf(judge.targetAgentNo) ){
						continue;
					}
				}

				// エージェントの得票数を取得
				int receiveVoteCount = voteAnalyzer.receiveVoteCount.getOrDefault(Agent.getAgent(seer), 0);

				// 占候補の得票数が得票数MAX-1以上なら押し込む
				if( receiveVoteCount >= receiveVoteCountMax - 1 ){
					workReq = new Request(seer);
					workReq.vote = 12.00;
					Requests.add(workReq);
				}
			}

		}

		return Requests;

	}

}
