package com.gmail.aiwolf.uec.yk.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;

public class VoteAnalyzer {

	/** 投票の一覧 */
	public List<Vote> voteList;

	/** エージェント毎の被投票数 */
	public HashMap<Agent, Integer> receiveVoteCount;


	/**
	 * コンストラクタ
	 */
	public VoteAnalyzer(){

		this.voteList = new ArrayList<Vote>();

		calc();

	}


	/**
	 * コンストラクタ
	 * @param voteList 投票の一覧
	 */
	public VoteAnalyzer(List<Vote> voteList){

		this.voteList = voteList;

		calc();

	}


	/**
	 * コンストラクタ
	 * @param agi
	 * @param day 日
	 */
	public VoteAnalyzer(AdvanceGameInfo agi, int day){

		// 投票一覧の初期化
		voteList = new ArrayList<Vote>();

		// エージェント毎の投票予告先を取得する
		for( Agent agent : agi.latestGameInfo.getAgentList() ){
			Integer voteTarget = agi.getSaidVoteAgent(agent.getAgentIdx(), day);
			// 投票一覧に追加
			if( voteTarget != null ){
				Vote vote = new Vote(agi.latestGameInfo.getDay(), agent, Agent.getAgent(voteTarget));
				voteList.add(vote);
			}else{
				Vote vote = new Vote(agi.latestGameInfo.getDay(), agent, null);
				voteList.add(vote);
			}
		}

		calc();

	}


	/**
	 * 宣言した投票先を読み込む
	 * @param agi
	 */
	public static VoteAnalyzer loadSaidVote(AdvanceGameInfo agi){

		VoteAnalyzer analyzer = new VoteAnalyzer();

		// 投票一覧の初期化
		analyzer.voteList = new ArrayList<Vote>();

		// エージェント毎の投票予告先を取得する
		for( Agent agent : agi.latestGameInfo.getAliveAgentList() ){
			Integer voteTarget = agi.getSaidVoteAgent(agent.getAgentIdx());
			// 投票一覧に追加
			if( voteTarget != null ){
				Vote vote = new Vote(agi.latestGameInfo.getDay(), agent, Agent.getAgent(voteTarget));
				analyzer.voteList.add(vote);
			}else{
				Vote vote = new Vote(agi.latestGameInfo.getDay(), agent, null);
				analyzer.voteList.add(vote);
			}
		}

		analyzer.calc();

		return analyzer;

	}


	/**
	 * 宣言した投票先を読み込む
	 * @param agi
	 */
	public static VoteAnalyzer loadSaidVote(AdvanceGameInfo agi, int day){

		VoteAnalyzer analyzer = new VoteAnalyzer();

		// 投票一覧の初期化
		analyzer.voteList = new ArrayList<Vote>();

		// エージェント毎の投票予告先を取得する
		for( Agent agent : agi.latestGameInfo.getAgentList() ){
			Integer voteTarget = agi.getSaidVoteAgent(agent.getAgentIdx(), day);
			// 投票一覧に追加
			if( voteTarget != null ){
				Vote vote = new Vote(day, agent, Agent.getAgent(voteTarget));
				analyzer.voteList.add(vote);
			}else{
				Vote vote = new Vote(day, agent, null);
				analyzer.voteList.add(vote);
			}
		}

		analyzer.calc();

		return analyzer;

	}


	public Agent getVoteTarget(Agent agent){

		for(Vote vote : voteList){
			if( vote.getAgent().equals(agent) ){
				return vote.getTarget();
			}
		}

		// 見つからない場合
		return null;

	}


	/**
	 * 被投票数を取得
	 * @param agent 被投票数を取得するエージェント
	 * @return
	 */
	public int getReceiveVoteCount(Agent agent){
		if( receiveVoteCount.containsKey(agent) ){
			return receiveVoteCount.get(agent);
		}
		return 0;
	}


	/**
	 * 被投票数を取得
	 * @param agentNo 被投票数を取得するエージェント
	 * @return
	 */
	public int getReceiveVoteCount(int agentNo){
		if( receiveVoteCount.containsKey(Agent.getAgent(agentNo)) ){
			return receiveVoteCount.get(Agent.getAgent(agentNo));
		}
		return 0;
	}


	/**
	 * 被投票数が最大のエージェント一覧を取得
	 * @return
	 */
	public List<Agent> getMaxReceiveVoteAgent(){

		// 最大被投票数の取得
		int max = 0;
		for( Entry<Agent, Integer> set : receiveVoteCount.entrySet() ){
			if( set.getValue() > max ){
				max = set.getValue();
			}
		}

		// 最大被投票数のエージェントの取得
		List<Agent> ret = new ArrayList<Agent>();
		if( max > 0 ){
			for(Vote vote : voteList){
				if( vote.getTarget() != null && getReceiveVoteCount(vote.getTarget()) >= max && !ret.contains(vote.getTarget()) ){
					ret.add(vote.getTarget());
				}
			}
		}

		return ret;

	}



	/**
	 * 各種計算
	 */
	private void calc(){

		// 被投票数の計算
		receiveVoteCount = new HashMap<Agent, Integer>();
		for(Vote vote : voteList){
			Agent target = vote.getTarget();
			if( target != null ){
				addReceiveVoteCount(target);
			}
		}

	}


	/**
	 * 被投票数の加算
	 * @param agent
	 */
	private void addReceiveVoteCount(Agent agent){
		if( receiveVoteCount.containsKey(agent) ){
			receiveVoteCount.put(agent, receiveVoteCount.get(agent) + 1);
		}else{
			receiveVoteCount.put(agent, 1);
		}
	}



}
