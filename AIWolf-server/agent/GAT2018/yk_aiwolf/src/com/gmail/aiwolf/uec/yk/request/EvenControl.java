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
 * �s����p�u���������v
 */
public final class EvenControl extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;

		// �����҂T�l�łȂ���Ύ��s���Ȃ�
		if( gameInfo.getAliveAgentList().size() != 5 ){
			return Requests;
		}


		// �錾�ςݓ��[��̕��͂��擾
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// �ő��[�𓾂Ă���G�[�W�F���g���P����ɂ���
		for( Agent agent : voteAnalyzer.getMaxReceiveVoteAgent() ){
			workReq = new Request(agent);
			workReq.attack = 30.0;
			Requests.add(workReq);
		}

		return Requests;
	}

}
