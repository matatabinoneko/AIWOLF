package com.gmail.kanpyo2018.Role;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.kanpyo2018.base.BaseRole;
import com.gmail.kanpyo2018.data.GameResult;

public class Bodyguard extends BaseRole {

	public Bodyguard(GameResult gameResult) {
		super(gameResult);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void action(Talk talk, Content content) {
		if (!isAlive(guardTaeget)) {
			guardTaeget = null;
		}
		List<Agent> seerList = new ArrayList<>();
		for (Agent agent : coMap.keySet()) {
			if (coMap.get(agent) == Role.SEER) {
				seerList.add(agent);
			}
		}
//		if (seerGuard) {
//			if (gameInfo.getAttackedAgent() != null) {
//				seerGuard = false;
//				seerList.remove(guardTaeget);
//				if (!seerList.isEmpty()) {
//					guardTaeget = seerList.get(0);
//				}
//			}
//		}
		if (seerCount == 1 && turn > 2) {
			if (!seerList.isEmpty()) {
				guardTaeget = seerList.get(0);
				seerGuard = true;
			}
		}
		if (content.getTopic() == Topic.DIVINED) {
			if (gameInfo.getDay() == 1) {
				if (coMap.get(talk.getAgent()) == Role.SEER) {
					if (content.getResult() == Species.HUMAN) {
						if (guardTaeget == null) {
							guardTaeget = talk.getAgent();
						}
					}
				}
			}
		}

	}
}
