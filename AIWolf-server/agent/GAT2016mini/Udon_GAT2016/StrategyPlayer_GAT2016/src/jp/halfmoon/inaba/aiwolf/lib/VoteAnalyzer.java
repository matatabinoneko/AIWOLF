package jp.halfmoon.inaba.aiwolf.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Vote;

public class VoteAnalyzer {

	/** ���[�̈ꗗ */
	public List<Vote> voteList;

	/** �G�[�W�F���g���̔퓊�[�� */
	public HashMap<Agent, Integer> receiveVoteCount;


	/**
	 * �R���X�g���N�^
	 */
	public VoteAnalyzer(){

		this.voteList = new ArrayList<Vote>();

		calc();

	}


	/**
	 * �R���X�g���N�^
	 * @param voteList ���[�̈ꗗ
	 */
	public VoteAnalyzer(List<Vote> voteList){

		this.voteList = voteList;

		calc();

	}


	/**
	 * �錾�������[���ǂݍ���
	 * @param agi
	 */
	public static VoteAnalyzer loadSaidVote(AdvanceGameInfo agi){

		VoteAnalyzer analyzer = new VoteAnalyzer();

		// ���[�ꗗ�̏�����
		analyzer.voteList = new ArrayList<Vote>();

		// �G�[�W�F���g���̓��[�\������擾����
		for( Agent agent : agi.latestGameInfo.getAliveAgentList() ){
			Integer voteTarget = agi.getSaidVoteAgent(agent.getAgentIdx());
			// ���[�ꗗ�ɒǉ�
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


	public Agent getVoteTarget(Agent agent){

		for(Vote vote : voteList){
			if( vote.getAgent().equals(agent) ){
				return vote.getTarget();
			}
		}

		// ������Ȃ��ꍇ
		return null;

	}


	/**
	 * �퓊�[�����擾
	 * @param agent �퓊�[�����擾����G�[�W�F���g
	 * @return
	 */
	public int getReceiveVoteCount(Agent agent){
		if( receiveVoteCount.containsKey(agent) ){
			return receiveVoteCount.get(agent);
		}
		return 0;
	}


	/**
	 * �퓊�[�����擾
	 * @param agentNo �퓊�[�����擾����G�[�W�F���g
	 * @return
	 */
	public int getReceiveVoteCount(int agentNo){
		if( receiveVoteCount.containsKey(Agent.getAgent(agentNo)) ){
			return receiveVoteCount.get(Agent.getAgent(agentNo));
		}
		return 0;
	}


	/**
	 * �퓊�[�����ő�̃G�[�W�F���g�ꗗ���擾
	 * @return
	 */
	public List<Agent> getMaxReceiveVoteAgent(){

		// �ő�퓊�[���̎擾
		int max = 0;
		for( Entry<Agent, Integer> set : receiveVoteCount.entrySet() ){
			if( set.getValue() > max ){
				max = set.getValue();
			}
		}

		// �ő�퓊�[���̃G�[�W�F���g�̎擾
		List<Agent> ret = new ArrayList<Agent>();
		for(Vote vote : voteList){
			if( getReceiveVoteCount(vote.getAgent()) >= max ){
				ret.add(vote.getAgent());
			}
		}

		return ret;

	}



	/**
	 * �e��v�Z
	 */
	private void calc(){

		// �퓊�[���̌v�Z
		receiveVoteCount = new HashMap<Agent, Integer>();
		for(Vote vote : voteList){
			Agent target = vote.getTarget();
			if( target != null ){
				addReceiveVoteCount(target);
			}
		}

	}


	/**
	 * �퓊�[���̉��Z
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
