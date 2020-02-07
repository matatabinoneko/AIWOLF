package com.gmail.kanpyo2018.Role;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.kanpyo2018.base.BaseRole;
import com.gmail.kanpyo2018.data.GameResult;
import com.gmail.kanpyo2018.talk.TalkFactory;

public class Medium extends BaseRole {
	public Medium(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void action(Talk talk, Content content) {
		if (!isCo) {
			if (!myIdentifiedQueue.isEmpty()) {
				Judge iden = myIdentifiedQueue.pop();
				Agent target = iden.getTarget();
				Species result = iden.getResult();
				powerTalkQueue.offer(TalkFactory.identRemark(target, result));
			}
		}
		if (content.getTopic() == Topic.COMINGOUT) {
			if (content.getRole() == Role.MEDIUM) {
				coMap.put(talk.getAgent(), Role.WEREWOLF);
				if (isCo) {
					isCo = false;
					powerTalkQueue.offer(TalkFactory.comingoutRemark(me, Role.MEDIUM));
				}
				powerTalkQueue.offer(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
				powerTalkQueue.offer(TalkFactory.voteRemark(talk.getAgent()));
				powerTalkQueue.offer(TalkFactory.requestAllVoteRemark(talk.getAgent()));
				wereWolfAgent = talk.getAgent();
			}
		}
	}
}
