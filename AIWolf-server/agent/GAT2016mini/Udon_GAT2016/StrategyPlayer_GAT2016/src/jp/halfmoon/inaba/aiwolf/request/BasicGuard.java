package jp.halfmoon.inaba.aiwolf.request;

import java.util.ArrayList;
import java.util.List;

import jp.halfmoon.inaba.aiwolf.guess.InspectedWolfsidePattern;
import jp.halfmoon.inaba.aiwolf.lib.Judge;
import jp.halfmoon.inaba.aiwolf.lib.VoteAnalyzer;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;


/**
 * 行動戦術「基本狩戦術」
 */
public final class BasicGuard extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;



		List<Integer> seers = args.agi.getEnableCOAgentNo(Role.SEER);
		List<Integer> mediums = args.agi.getEnableCOAgentNo(Role.MEDIUM);

		// 偽パターンの最大スコアが最小のものを求める
		double minScore = Double.MAX_VALUE;
		for( int seer : seers ){
			InspectedWolfsidePattern wolfPattern = args.aguess.getMostValidWolfPattern(seer);
			InspectedWolfsidePattern posPattern = args.aguess.getMostValidPossessedPattern(seer);

			double score = ( wolfPattern != null ? wolfPattern.score : 0.0 ) + ( posPattern != null ? posPattern.score : 0.0 );

			minScore = Math.min(score, minScore);
		}

		// 偽スコアの差が大きい占を偽打ち扱い
		int falseCount = 0;
		for( int seer : seers ){
			InspectedWolfsidePattern wolfPattern = args.aguess.getMostValidWolfPattern(seer);
			InspectedWolfsidePattern posPattern = args.aguess.getMostValidPossessedPattern(seer);
			double score = ( wolfPattern != null ? wolfPattern.score : 0.0 ) + ( posPattern != null ? posPattern.score : 0.0 );

			if( score > minScore * 1.6 ){
				falseCount++;
			}
		}

		// １人除いて偽打ちか
		if( falseCount == seers.size() - 1 ){
			// 真打ちした占の護衛を厚くする
			for( int seer : seers ){
				InspectedWolfsidePattern wolfPattern = args.aguess.getMostValidWolfPattern(seer);
				InspectedWolfsidePattern posPattern = args.aguess.getMostValidPossessedPattern(seer);

				double score = ( wolfPattern != null ? wolfPattern.score : 0.0 ) + ( posPattern != null ? posPattern.score : 0.0 );

				if( Double.compare(score, minScore) == 0 ){
					workReq = new Request(seer);
					workReq.guard = 3.0;
					Requests.add(workReq);
				}else{
					workReq = new Request(seer);
					workReq.guard = 0.5;
					Requests.add(workReq);
				}
			}
		}


		//TODO 他編成対応・消去法で特定or不在が分かったパターンの対応(各占視点があれば、生存灰の全員に色がついているかで判断可能)
		// 仕事終了した占は護衛しない
		for( int seer : seers ){
			// 占・霊・それ以外の色が判明した人外数をカウント
			int seerEnemyCnt = seers.size() - 1;
			int mediumEnemyCnt = ( mediums.size() > 1 ) ? (mediums.size() - 1) : 0;
			int hitGrayBlackCnt = 0;
			for( Judge judge : args.agi.getSeerJudgeList() ){
				if( judge.isEnable() &&
				    judge.agentNo == seer &&
				    judge.result == Species.WEREWOLF ){
					// 相手が占霊以外か
					if( args.agi.agentState[judge.targetAgentNo].comingOutRole == null ||
						(args.agi.agentState[judge.targetAgentNo].comingOutRole != Role.SEER && args.agi.agentState[judge.targetAgentNo].comingOutRole != Role.MEDIUM ) ){
						hitGrayBlackCnt++;
					}
				}
			}

			if( seerEnemyCnt + mediumEnemyCnt + hitGrayBlackCnt >= 4 ){
				workReq = new Request(seer);
				workReq.guard = 0.001;
				Requests.add(workReq);
			}
		}


		// 占い師が有用でなければ護衛しない
		for( Judge judge : args.agi.getSeerJudgeList() ){
			// 自分占い
			if( judge.agentNo == judge.targetAgentNo ){
				// 護衛要求を下げる
				workReq = new Request(judge.agentNo);
				workReq.guard = 0.5;
				Requests.add(workReq);
			}
		}


		// 宣言済み投票先の分析を取得
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// 得ている票数に応じて護衛を薄くする
		for( Agent agent : gameInfo.getAliveAgentList() ){
			workReq = new Request(agent);
			workReq.guard = Math.max( 1.00 - voteAnalyzer.getReceiveVoteCount(agent) * 0.03, 0.0 );
			Requests.add(workReq);
		}

		// 最多票を得ているエージェントは護衛先から除外する
		for( Agent agent : voteAnalyzer.getMaxReceiveVoteAgent() ){
			workReq = new Request(agent);
			workReq.guard = 0.01;
			Requests.add(workReq);
		}

		return Requests;
	}

}
