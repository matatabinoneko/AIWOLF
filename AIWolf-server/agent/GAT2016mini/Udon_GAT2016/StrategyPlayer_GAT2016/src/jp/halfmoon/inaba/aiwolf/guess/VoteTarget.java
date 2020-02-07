package jp.halfmoon.inaba.aiwolf.guess;

import java.util.ArrayList;
import java.util.List;

import jp.halfmoon.inaba.aiwolf.condition.AbstractCondition;
import jp.halfmoon.inaba.aiwolf.condition.AndCondition;
import jp.halfmoon.inaba.aiwolf.condition.OrCondition;
import jp.halfmoon.inaba.aiwolf.condition.RoleCondition;
import jp.halfmoon.inaba.aiwolf.lib.VoteAnalyzer;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

/**
 * 推理「投票先」クラス
 */
public final class VoteTarget extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();


		// 全ての投票履歴を確認する(初回投票=1日目)
		for( int day = 1; day < args.agi.latestGameInfo.getDay(); day++ ){

			VoteAnalyzer analyzer = new VoteAnalyzer(args.agi.getVoteList(day));


			// 霊CO者のリストを取得する
			List<Integer> mediums = args.agi.getEnableCOAgentNo(Role.MEDIUM, day, 0);

			for( Vote vote : args.agi.getVoteList(day) ){

				// 投票の推理要素としての重み(手順吊りだと軽くなる)
				double weight = 1.0;

				// 被投票者が複霊の場合、手順吊りとして重みを下げる
				if( mediums.size() >= 2 && mediums.indexOf(vote.getTarget().getAgentIdx()) != -1 ){
					weight *= 0.5;
				}
				// 被投票者が黒貰いの場合、手順吊りとして重みを下げる
				if( args.agi.isReceiveWolfJudge(vote.getTarget().getAgentIdx(), day, 0) ){
					weight *= 0.5;
				}

				AbstractCondition agentWolf = RoleCondition.getRoleCondition( vote.getAgent().getAgentIdx(), Role.WEREWOLF );
				AbstractCondition agentPossessed = RoleCondition.getRoleCondition( vote.getAgent().getAgentIdx(), Role.POSSESSED );

				Guess guess;


				// 投票先の得票数が多い場合人狼で見る
				guess = new Guess();
				guess.condition = agentWolf;
				guess.correlation = Math.pow(0.98 + analyzer.getReceiveVoteCount(vote.getTarget()) * 0.01, weight);
				guesses.add(guess);


				//TODO 他編成対応
				// 早期の占への投票は人外で見る
				if( day < 3 ){
					Role CORole = args.agi.getCORole( vote.getTarget().getAgentIdx(), day+1, 0 );
					if( CORole == Role.SEER ){
						guess = new Guess();
						guess.condition = new OrCondition().addCondition(agentWolf).addCondition(agentPossessed);
						guess.correlation = 1.05;
						guesses.add(guess);
					}
				}


			}
		}

		// 推理リストを返す
		return guesses;
	}

}
