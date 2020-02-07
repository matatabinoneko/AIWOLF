package jp.halfmoon.inaba.aiwolf.request;

import java.util.ArrayList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import jp.halfmoon.inaba.aiwolf.lib.VoteAnalyzer;


/**
 * �s����p�u�[�d�ˁv
 * �{���z�肷�铮���́A�ʂ�\��������݂���Ă��邱�ƁB
 */
public final class VoteStack extends AbstractActionStrategy {

	@Override
	public ArrayList<Request> getRequests(ActionStrategyArgs args) {

		GameInfo gameInfo = args.agi.latestGameInfo;

		ArrayList<Request> Requests = new ArrayList<Request>();
		Request workReq;



		// �錾�ςݓ��[��̕��͂��擾
		VoteAnalyzer voteAnalyzer = VoteAnalyzer.loadSaidVote(args.agi);

		// ���Ă���[���ɉ����ĕ[���d�˂�
		for( Agent agent : gameInfo.getAliveAgentList() ){
			workReq = new Request(agent);
			workReq.vote = 1.00 + voteAnalyzer.getReceiveVoteCount(agent) * 0.02 * (1 + voteAnalyzer.getReceiveVoteCount(gameInfo.getAgent()) * 0.05);
			Requests.add(workReq);
		}


		// ���������l���̏���
		if( args.agi.latestGameInfo.getRole() == Role.POSSESSED ){
			for( Agent agent : gameInfo.getAliveAgentList() ){
				// ���������_�Ŋm����
				if( args.agi.selfRealRoleViewInfo.isFixBlack(agent.getAgentIdx()) ){
					Agent target = voteAnalyzer.getVoteTarget(agent);
					// ���[���錾���Ă��邩
					if( target != null ){
						// �T�l�Ɠ����ꏊ�ɓ��[����
						workReq = new Request(target);
						workReq.vote = 1.2;
						Requests.add(workReq);
					}
				}
			}
		}


		return Requests;

	}

}
