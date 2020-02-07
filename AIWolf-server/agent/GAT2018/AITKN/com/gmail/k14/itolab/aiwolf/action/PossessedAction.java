package com.gmail.k14.itolab.aiwolf.action;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 狂人の行動
 * @author k14096kk
 *
 */
public class PossessedAction extends BaseRoleAction{

	/**key=agent,val=占って欲しい発言数*/
	Map<Agent,Integer> divinationMap = new HashMap<Agent,Integer>();
	/**占い結果のマップ*/
	Map<Agent,Species> fakeDivineMap = new HashMap<Agent,Species>();

	/**自分が占ったエージェントのリスト*/
	List<Agent> divinedList = new ArrayList<>();
	/**占い師が占ったエージェントのリスト*/
	List<Agent> allDivineList = new ArrayList<>();
	/**自分の黒出しリスト*/
	List<Agent> blackList = new ArrayList<>();
	/**自分の白出しリスト*/
	List<Agent> whiteList = new ArrayList<>();
	/**占い師のCOリスト*/
	List<Agent> seerCOList = new ArrayList<>();
	/**霊媒師のCOリスト*/
	List<Agent> mediumCOList = new ArrayList<>();
	/**人狼だと思われるリスト*/
	List<Agent> werewolfList = new ArrayList<>();

	/**投票対象にするエージェント*/
	Agent voteAgent = null;
	/**占い対象にするエージェント*/
	Agent divineAgent = null;
	/**占い師だと思うエージェント*/
	Agent seer = null;
	/**霊媒師だと思うエージェント*/
	Agent medium = null;
	/**狩人COしたエージェント*/
	Agent bodyguard = null;
	/**狂人COしたエージェント*/
	Agent possessed = null;
	/**人狼COしたエージェント*/
	Agent werewolf = null;
	/**５人人狼で白を引いた時用*/
	Agent fiveDivine = null;

	/**5人人狼かどうか*/
	boolean fiveJinro = false;
	/**1日の特定行動制御*/
	boolean oneAction = false;
	/**1日に1回占い発言をする*/
	boolean divinedFlug = true;

	/**人狼数を格納しておくための変数*/
	int jinroCount = ownData.getSettingRoleNum(Role.WEREWOLF);
	/**占い師・霊媒師のCOカウント*/
	int foCount = 0;
	/**自分が黒を出した回数*/
	int myDivineBlack = 0;

	/**占い騙り用*/
	Species fakeRole = null;

	/**
	 * 狂人行動のコンストラクタ
	 * @param entityData :オブジェクトデータ
	 */
	public PossessedAction(EntityData entityData) {
		super(entityData);
		/*ここに処理*/
		ownData.rejectReaction();
		/**/
		setEntityData();
	}

	
	/**
	 * 1日のはじめ
	 */
	@Override
	public void dayStart() {
		super.dayStart();

		oneAction = false;
		divinedFlug = true;
		voteAgent = null;

		//狩人COがあり、まだ生きている場合
		if(bodyguard != null && ownData.getAliveOtherAgentList().contains(bodyguard)) {
			HandyGadget.addList(blackList, bodyguard);
		}

		AgentAliveCheck();

		setEntityData();
	}

	
	/**
	 * 投票時に呼ぶ
	 */
	@Override
	public void vote() {
		super.vote();

		if (!fiveJinro && ownData.getDay() < 3) {
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList());
		}
		else {
			//死亡者リストに投票対象が含まれている
			if (ownData.getDeadAgentList().contains(voteAgent)) {
				voteAgent = null;
			}

			if (werewolfList.contains(voteAgent)) {
				voteAgent = null;
			}

			if (voteAgent == null) {
				voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),werewolfList);
			}
		}

		ownData.setVoteTarget(voteAgent);

		setEntityData();
	}
	
	
	/**
	 * 行動選択
	 */
	@Override
	public void selectAction() {
		super.selectAction();

		//１日目の生存人数が５人以下
		if (ownData.getDay() == 1 && ownData.getAliveOtherAgentList().size() < 6) {
			fiveJinro = true;
		}

		this.nomalPossessedAciton();

		setEntityData();
	}

	
	/**
	 * 他人の発言に対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	@Override
	public void talkAction(Talk talk, Content content) {
		super.talkAction(talk, content);
		if (talk.getAgent() != ownData.getMe()) {
			if (content.getTopic() == Topic.COMINGOUT) {
				if (content.getRole() == Role.VILLAGER) {
					//占い師からのスライド
					if (seerCOList.contains(talk.getAgent())) {
						seerCOList.remove(talk.getAgent());
						foCount--;
					}
					//霊媒師からのスライド
					else if (mediumCOList.contains(talk.getAgent())) {
						mediumCOList.remove(talk.getAgent());
						foCount--;
					}
					//人狼からのスライド
					else if (werewolf == talk.getAgent()) {
						werewolf = null;
					}
					//狂人からのスライド
					else if (possessed == talk.getAgent()) {
						possessed = null;
					}
					//whiteListに含まれていなければ追加する
					HandyGadget.addList(whiteList, talk.getAgent());
					blackList.remove(talk.getAgent());
				}

				else if (content.getRole() == Role.SEER) {
					
					// 占い師登録
					if(!entrySeerCoList.contains(talk.getAgent())) {
						HandyGadget.addList(seerCOList, talk.getAgent());
					}
					HandyGadget.addList(entrySeerCoList, talk.getAgent());
					
					foCount++;

					//COしていなければCO
					if (!ownData.isCO()) {
						GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
					}

					//５人人狼でCO数が1を超えている
					if (seerCOList.size() > 1 && fiveJinro) {
						voteAgent = RandomSelect.randomAgentSelect(seerCOList);
						myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
					}
				}
				else if (content.getRole() == Role.MEDIUM) {
					// 霊媒師登録
					if(!entryMediumCoList.contains(talk.getAgent())) {
						HandyGadget.addList(mediumCOList, talk.getAgent());
					}
					HandyGadget.addList(entryMediumCoList, talk.getAgent());
					
					foCount++;
				}
				else if (content.getRole() == Role.BODYGUARD) {
					bodyguard = talk.getAgent();
				}
				else if (content.getRole() == Role.POSSESSED) {
					possessed = talk.getAgent();
				}
				else if (content.getRole() == Role.WEREWOLF) {
					werewolfList.add(talk.getAgent());
				}
			}

			if (content.getTopic() == Topic.DIVINED) {
				
				//占いCOをしていない場合に対応
				if(!entrySeerCoList.contains(talk.getAgent())) {
					HandyGadget.addList(seerCOList, talk.getAgent());
				}
				HandyGadget.addList(entrySeerCoList, talk.getAgent());
				
				//全占い師の占い済みリストに追加
				HandyGadget.addList(allDivineList, content.getTarget());
				//占い対象が自分
				if (content.getTarget() == ownData.getMe()) {
					if (content.getResult() == Species.WEREWOLF) {
						HandyGadget.addList(werewolfList, talk.getAgent());
					}
					else {
						seer = talk.getAgent();
						//狂人だと思う発言をしてもいいかもしれない
					}
				}
				else {
					if (content.getResult() == Species.WEREWOLF) {
						HandyGadget.addList(werewolfList, content.getTarget());
						seer = talk.getAgent();
					}
				}

			}

			if (content.getTopic() == Topic.IDENTIFIED) {
				
				//霊媒COをしていない場合に対応
				if(!entryMediumCoList.contains(talk.getAgent())) {
					HandyGadget.addList(mediumCOList, talk.getAgent());
				}
				HandyGadget.addList(entryMediumCoList, talk.getAgent());
				
				//自分の占ったリストの中に対象がいる
				if (divinedList.contains(content.getTarget())) {
					if (ownData.getDivineResultJudge(content.getTarget()) != null) {
						//自分の占い結果と異なる霊媒結果
						if (ownData.getDivineResultJudge(content.getTarget()).getResult() != content.getResult()) {
							HandyGadget.addList(blackList, talk.getAgent());
							whiteList.remove(talk.getAgent());
							myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
						}
					}
				}
			}
		}
		setEntityData();
	}

	
	/**
	 * リクエストに対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 * @param reqContent :リクエストコンテンツ
	 */
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		if (talk.getAgent() != ownData.getMe()) {
			if (reqContent.getTopic() == Topic.VOTE) {
				//投票対象が自分でない
				if (reqContent.getTarget() != ownData.getMe()) {
					//投票対象がもっとも得票を得ている
					if (reqContent.getTarget() == voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList())) {
						//投票対象がblackListに含まれている
						if (blackList.contains(reqContent.getTarget())) {
							//投票セット・投票発言
							ownData.setVoteTarget(reqContent.getTarget());
							GeneralAction.sayVote(ownData, myTalking);
						}
						//blackListが空でない　かつ　voteAgentがblackListに含まれている
						else if (!blackList.isEmpty() && blackList.contains(voteAgent)) {
							//リクエストvote
							myTalking.addTalk(TalkFactory.requestAllVoteRemark(voteAgent));
						}
					}
				}
			}

			if (reqContent.getTopic() == Topic.DIVINATION) {
				//COしているか
				if (ownData.isCO()) {
					//占い対象が自分でない
					if (reqContent.getTarget() != ownData.getMe()) {
						//Mapに登録済みか
						if (!divinationMap.containsKey(reqContent.getTarget())) {
							divinationMap.put(reqContent.getTarget(), 1);
						}
						else {
							//占って欲しいカウントを足す
							int tmp = divinationMap.get(reqContent.getTarget());
							divinationMap.put(reqContent.getTarget(), ++tmp);
						}
					}
				}
			}
		}
		setEntityData();
	}

	
	/**
	 * 基本行動
	 */
	public void nomalPossessedAciton() {
		if (!ownData.isCO()) {
			GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
			ownData.setComingoutRole(Role.SEER);
		}

		if (!oneAction) {
			divineAction();

			//5人人狼
			if (fiveJinro) {
				//２日目
				if (ownData.getDay() == 2) {
					//狂人CO
					GeneralAction.sayComingout(ownData, myTalking, Role.POSSESSED);
					myTalking.addTalk(TalkFactory.skipRemark());
					//本物だけCOしている
					if (seerCOList.size() == 1) {
						voteAgent = RandomSelect.randomAgentSelect(seerCOList);
					}
					//3CO
					else if (seerCOList.size() > 1) {
						voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),seerCOList);
					}
					myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
				}
			}
			//15人人狼
			else {
				//偽占い結果が黒
				if (fakeRole == Species.WEREWOLF) {
					voteAgent = divineAgent;
				}
				//偽占い結果が白
				else {
					voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList());
				}
				myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(voteAgent));
			}
			oneAction = true;
		}

		//5人人狼
		if (fiveJinro) {
			if (seerCOList.size() == 1) {
				voteAgent = RandomSelect.randomAgentSelect(seerCOList);
			}
			else if (seerCOList.size() > 1) {
				voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),werewolfList);
			}
		}
		//15人人狼
		else {
			if (ownData.getDay() < 3) {
				voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList());
			}
		}
	}

	
	/**
	 * 占い先決定
	 */
	public void divineAction() {
		//占い対象がnull　または　占い対象が死んでいる　または　占い対象を既に占っている
		if (divineAgent == null || ownData.getDeadAgentList().contains(divineAgent) || divinedList.contains(divineAgent)) {
			divineAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),divinedList);
			HandyGadget.addList(divinedList, divineAgent);
		}


		//5人人狼
		if (fiveJinro) {
			//0~99の乱数　8割の確率で黒
			int r = RandomSelect.randomInt(100);
			if (r < 80) {
				if (myDivineBlack < ownData.getSettingRoleNum(Role.WEREWOLF)) {
					fakeRole = Species.WEREWOLF;
					myDivineBlack++;
				}
			}
			//2割の確率で白
			else {
				fakeRole = Species.HUMAN;
			}
		}

		//15人人狼
		else {
			//黒を3回出していない
			if (myDivineBlack < ownData.getSettingRoleNum(Role.WEREWOLF)){
				int r = RandomSelect.randomInt(100);
				//生存人数が9人未満（通常進行で５日目）
				if (ownData.getAliveAgentList().size() < 9) {
					r -= 20;
				}
				//日付*10の確率で黒
				else if (r < ownData.getDay() * 10) {
					fakeRole = Species.WEREWOLF;
					myDivineBlack++;
				}
				else {
					fakeRole = Species.HUMAN;
				}
			}
			//黒を3回出した
			else {
				fakeRole = Species.HUMAN;
			}
		}


		if (!ownData.isDivine() && ownData.getComingoutRole() == Role.SEER) {
			myTalking.addTalk(TalkFactory.divinedResultRemark(divineAgent, fakeRole));
			ownData.setActFlagDivine();
			HandyGadget.addList(divinedList, divineAgent);

			//占い結果が黒
			if (fakeRole == Species.WEREWOLF) {
				HandyGadget.addList(blackList, divineAgent);
				voteAgent = divineAgent;
				myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
			}
			//占い結果が白
			else {
				HandyGadget.addList(whiteList, divineAgent);
				voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),divinedList,whiteList);
				GeneralAction.sayVote(ownData, myTalking);
			}
		}
	}


	/**
	 * 占い先エージェント発言
	 */
	public void divinationAction() {
		if (ownData.isCO()) {
			int tmp = 0;
			Agent age = null;

			//黒発見数が１で４人以上CO
			if (ownData.getDivineResultList(Species.WEREWOLF).size() == 1 && foCount > 3) {
				blackList.addAll(seerCOList);
				blackList.addAll(mediumCOList);
				divineAgent = RandomSelect.randomAgentSelect(blackList,ownData.getDeadAgentList(),divinedList);
			}
			//黒発見数が２
			else if (ownData.getDivineResultList(Species.WEREWOLF).size() == 2) {
				blackList.addAll(seerCOList);
				blackList.addAll(mediumCOList);
				divineAgent = RandomSelect.randomAgentSelect(blackList,ownData.getDeadAgentList(),divinedList);
			}
			//5人以上CO
			else if (foCount > 4) {
				blackList.addAll(seerCOList);
				blackList.addAll(mediumCOList);
				divineAgent = RandomSelect.randomAgentSelect(blackList,ownData.getDeadAgentList(),divinedList);
			}
			else {
				//占いリクエストの最多を見る
				for (Agent agent : divinationMap.keySet()) {
					if (tmp == 0) {
						tmp = divinationMap.get(agent);
						age = agent;
					}
					if (tmp < divinationMap.get(agent)) {
						tmp = divinationMap.get(agent);
						age = agent;
					}
				}
				divineAgent = age;
				if (divineAgent == ownData.getMe()) {
					divineAgent = null;
				}
			}

			//占い対象が死亡またはnull
			if (ownData.getDeadAgentList().contains(divineAgent) || divineAgent == null) {
				//占ったことがないエージェントからランダム
				divineAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), allDivineList,divinedList);
				if (divineAgent == null) {
					divineAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), divinedList);
				}
			}
			ownData.setDivineTarget(divineAgent);
			myTalking.addTalk(TalkFactory.divinationRemark(divineAgent));
		}
	}

	
	/**
	 * 変数やリストから死亡者を排除する
	 */
	public void AgentAliveCheck() {

		whiteList.removeAll(ownData.getDeadAgentList());
		blackList.removeAll(ownData.getDeadAgentList());

		if (ownData.getDeadAgentList().contains(werewolf)) {
			werewolf = null;
		}
		if (ownData.getDeadAgentList().contains(bodyguard)) {
			bodyguard = null;
		}
	}
}