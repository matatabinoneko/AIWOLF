package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;
import com.gmail.aiwolf.uec.yk.condition.AndCondition;
import com.gmail.aiwolf.uec.yk.condition.RoleCondition;
import com.gmail.aiwolf.uec.yk.lib.CauseOfDeath;
import com.gmail.aiwolf.uec.yk.lib.VoteAnalyzer;

/**
 * 推理「投票履歴」クラス
 */
public final class VoteRecent extends AbstractGuessStrategy {

	@Override
	public ArrayList<Guess> getGuessList(GuessStrategyArgs args) {

		// 推理リスト
		ArrayList<Guess> guesses = new ArrayList<Guess>();


		// 全ての投票履歴を確認する(初回投票=1日目)
		for( int day = 1; day < args.agi.latestGameInfo.getDay(); day++ ){

			// 霊CO者のリストを取得する
			List<Integer> mediums = args.agi.getEnableCOAgentNo(Role.MEDIUM, day, 0);

			for( Vote vote : args.agi.getVoteList(day) ){

				int voteAgentNo = vote.getAgent().getAgentIdx();
				int voteTargetNo = vote.getTarget().getAgentIdx();

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

				//TODO 判定方法を丁寧にする
				// 視点確定人外の場合、重みを下げる
				Role agentRole = args.agi.getCORole(voteAgentNo, day, 0);
				Role targetRole = args.agi.getCORole(voteTargetNo, day, 0);
				if( agentRole != null && targetRole != null && agentRole == targetRole ){
					weight *= 0.3;
				}

				AbstractCondition agentWolf = RoleCondition.getRoleCondition( voteAgentNo, Role.WEREWOLF );
				AbstractCondition agentPossessed = RoleCondition.getRoleCondition( voteAgentNo, Role.POSSESSED );
				AbstractCondition targetWolf = RoleCondition.getRoleCondition( voteTargetNo, Role.WEREWOLF );
				AbstractCondition targetNotWolf = RoleCondition.getNotRoleCondition( voteTargetNo, Role.WEREWOLF );

				Guess guess;
				// 狼→狼のパターンを薄く見る（ライン切れより）
				if( args.agi.agentState[voteAgentNo].causeofDeath != CauseOfDeath.ATTACKED &&
				    args.agi.agentState[voteTargetNo].causeofDeath != CauseOfDeath.ATTACKED){
					guess = new Guess();
					guess.condition = new AndCondition().addCondition(agentWolf).addCondition(targetWolf);
					guess.correlation = 1.0 - 0.4 * weight;
					guesses.add(guess);
				}

				// 狼→非狼のパターンを濃く見る（スケープゴート）
				if( args.agi.agentState[voteAgentNo].causeofDeath != CauseOfDeath.ATTACKED ){
					guess = new Guess();
					guess.condition = new AndCondition().addCondition(agentWolf).addCondition(targetNotWolf);
					guess.correlation = 1.0 + 0.020 * weight;
					guesses.add(guess);
				}

				// 狂→非狼のパターンを濃く見る（スケープゴート）
				guess = new Guess();
				guess.condition = new AndCondition().addCondition(agentPossessed).addCondition(targetNotWolf);
				guess.correlation = 1.0 + 0.005 * weight;
				guesses.add(guess);

			}
		}

		// ３日目から投票宣言からのライン切れ推理も行う（３日目からなのは処理時間対策）
		if( args.agi.latestGameInfo.getDay() >= 3 ){

			// 霊CO者のリストを取得する
			List<Integer> mediums = args.agi.getEnableCOAgentNo(Role.MEDIUM, args.agi.latestGameInfo.getDay(), 0);

			VoteAnalyzer saidVote = VoteAnalyzer.loadSaidVote(args.agi);

			for( Vote vote : saidVote.voteList ){
				if( vote.getTarget() != null ){

					// 投票の推理要素としての重み(手順吊りだと軽くなる)
					double weight = 1.0;

					// 被投票者が複霊の場合、手順吊りとして重みを下げる
					if( mediums.size() >= 2 && mediums.indexOf(vote.getTarget().getAgentIdx()) != -1 ){
						weight *= 0.5;
					}
					// 被投票者が黒貰いの場合、手順吊りとして重みを下げる
					if( args.agi.isReceiveWolfJudge(vote.getTarget().getAgentIdx(), args.agi.latestGameInfo.getDay(), 0) ){
						weight *= 0.5;
					}

					AbstractCondition agentWolf = RoleCondition.getRoleCondition( vote.getAgent(), Role.WEREWOLF );
					AbstractCondition targetWolf = RoleCondition.getRoleCondition( vote.getTarget(), Role.WEREWOLF );

					Guess guess = new Guess();
					guess.condition = new AndCondition().addCondition(agentWolf).addCondition(targetWolf);
					guess.correlation = 1.0 - 0.4 * weight;
					guesses.add(guess);
				}
			}
		}

		// 推理リストを返す
		return guesses;
	}

}
