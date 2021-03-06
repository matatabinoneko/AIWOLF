package com.gmail.aiwolf.uec.yk.request;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

import java.util.ArrayList;


/**
 * 行動戦術「吊り回避」
 */
public final class AvoidExecute_Werewolf extends AbstractActionStrategy {

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

			// 自分の得票数を取得
			int receiveVoteCountWolf = 0;

			for( int wolf : args.agi.getAliveWolfList() ){
				int receiveVoteCount = voteAnalyzer.receiveVoteCount.getOrDefault(Agent.getAgent(wolf), 0);
				receiveVoteCountWolf = Math.max(receiveVoteCountWolf, receiveVoteCount);
			}

			// 得票数が極端に少ないなら行わない
			if( receiveVoteCountWolf <= 1 ){
				return Requests;
			}

			// 自分の得票数が得票数MAX-1以上なら吊り回避計算が必要（-1はヒステリシスのため）
			if( receiveVoteCountWolf >= receiveVoteCountMax - 1 ){

				// 生存エージェント走査
				for( Agent agent : gameInfo.getAliveAgentList() ){

					// 狼はスキップ
					if( args.agi.getAliveWolfList().contains(agent.getAgentIdx()) ){
						continue;
					}

					// エージェントの得票数を取得
					int receiveVoteCount = voteAnalyzer.receiveVoteCount.getOrDefault(agent, 0);

					// 自分の投票先であれば票数を-1計算する
					if( agent.equals( voteAnalyzer.getVoteTarget(gameInfo.getAgent()) ) ){
						receiveVoteCount--;
					}

					if( receiveVoteCount + 1 > receiveVoteCountWolf ){
						// あと１票で狼より得票数が多くなる
						workReq = new Request(agent);
						workReq.vote = 1.15;
						Requests.add(workReq);
					}else if( receiveVoteCount + 1 >= receiveVoteCountWolf ){
						// あと１票で狼と得票数が同じになる
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
