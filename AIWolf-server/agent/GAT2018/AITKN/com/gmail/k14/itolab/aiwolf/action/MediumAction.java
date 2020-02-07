package com.gmail.k14.itolab.aiwolf.action;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;


/**
 * 霊媒師の行動
 * @author k14096kk
 * 
 */
public class MediumAction extends BaseRoleAction{

	/**key=seer,value=spe.wolf seer黒出し回数*/
	Map<Agent,Integer> seerBlackCount = new HashMap<Agent,Integer>();
	/**key=medium,value=spe.wolf medium黒出し回数*/
	Map<Agent,Integer> mediumBlackCount = new HashMap<Agent,Integer>();
	/**霊媒結果を格納しておく*/
	Deque<Judge> identQueue = new LinkedList<>();

	/**占われたエージェントのリスト*/
	List<Agent> divinedList = new ArrayList<>();
	/**占い結果黒や偽物を格納する*/
	List<Agent> blackList = new ArrayList<>();
	/**占い結果白を格納する*/
	List<Agent> whiteList = new ArrayList<>();
	/**占い師のCOリスト*/
	List<Agent> seerCOList = new ArrayList<>();
	/**霊媒師のCOリスト*/
	List<Agent> mediumCOList = new ArrayList<>();
	/**2名以上から白を出されたリスト*/
	List<Agent> villagerList = new ArrayList<>();
	/**2名以上から黒を出されたまたは矛盾した発言をしたリスト*/
	List<Agent> werewolfList = new ArrayList<>();
	/**占い結果が分かれたエージェントリスト*/
	List<Agent> pandaList = new ArrayList<>();

	/**投票対象にするエージェント*/
	Agent voteAgent = null;
	/**前ターンまでの投票対象*/
	Agent provVoteAgent = null;
	/**占い対象にするエージェント*/
	Agent divineAgent = null;
	/**占い師だと思うエージェント*/
	Agent seer = null;
	/**狩人COしたエージェント*/
	Agent bodyguard = null;
	/**狂人COしたエージェント*/
	Agent possessed = null;
	/**人狼COしたエージェント*/
	Agent werewolf = null;
	/**占って欲しいと発言するエージェント*/
	Agent myDivinationAgent = null;

	/**霊媒結果を格納する*/
	Judge todayJudge = null;

	/**5人人狼かどうか*/
	boolean fiveJinro = false;
	/**1日の特定行動制御*/
	boolean oneAction = false;
	/**15人における行動変更*/
	boolean changeAction = false;

	/**人狼数を格納しておくための変数*/
	int jinroCount = ownData.getSettingRoleNum(Role.WEREWOLF);
	/**占い師・霊媒師のCOカウント*/
	int foCount = 0;

	/**
	 * 霊媒師行動のコンストラクタ
	 * @param entityData :オブジェクトデータ
	 */
	public MediumAction(EntityData entityData) {
		super(entityData);

		ownData.rejectReaction();

		identQueue.clear();

		setEntityData();
	}


	/**
	 * 1日のはじめ
	 */
	@Override
	public void dayStart() {
		super.dayStart();

		oneAction = false;

		todayJudge = ownData.getLatestMediumResult();

		// 霊媒結果が存在する
		if (Check.isNotNull(todayJudge)) {
			ownData.setIdentResultMap(ownData.getGameInfo().getMediumResult());
		}

		//狩人COがあり、まだ生きている場合
		if(bodyguard != null && ownData.getAliveOtherAgentList().contains(bodyguard)) {
			HandyGadget.addList(blackList, bodyguard);
		}

		if (ownData.getDay() > 3) {
			changeAction = true;
		}

		setEntityData();
	}


	/**
	 * 投票時に呼ぶ
	 */
	@Override
	public void vote() {
		super.vote();

		ownData.setVoteTarget(voteAgent);

		setEntityData();
	}


	/**
	 * 行動選択
	 */
	@Override
	public void selectAction() {
		super.selectAction();

		this.nomalMediumAction();

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
		}
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
		//発話対象から自分を外す
		if (talk.getAgent() != ownData.getMe()) {
			if (content.getTopic() == Topic.COMINGOUT) {
				//村人CO
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
				//占いCO
				if (content.getRole() == Role.SEER) {
					if (!seerCOList.contains(talk.getAgent())) {
						foCount++;
					}

					// 占い師登録
					if(!entrySeerCoList.contains(talk.getAgent())) {
						HandyGadget.addList(seerCOList, talk.getAgent());
					}
					HandyGadget.addList(entrySeerCoList, talk.getAgent());

				}
				//霊媒CO
				if (content.getRole() == Role.MEDIUM) {
					if (!mediumCOList.contains(talk.getAgent())) {
						foCount++;
					}
					//COしていなければ
					if (!ownData.isCO()) {
						//対抗CO
						GeneralAction.sayComingout(ownData, myTalking, Role.MEDIUM);
					}

					// 霊媒師登録
					if(!entryMediumCoList.contains(talk.getAgent())) {
						HandyGadget.addList(mediumCOList, talk.getAgent());
					}
					HandyGadget.addList(entryMediumCoList, talk.getAgent());

					HandyGadget.addList(blackList, talk.getAgent());
				}
				//狩人CO
				if (content.getRole() == Role.BODYGUARD) {
					bodyguard = talk.getAgent();
				}
				//狂人CO
				if (content.getRole() == Role.POSSESSED) {
					possessed = talk.getAgent();
				}
				//人狼CO
				if (content.getRole() == Role.WEREWOLF) {
					werewolf = talk.getAgent();
				}
			}

			//占い結果
			if (content.getTopic() == Topic.DIVINED) {

				//占いCOをしていない場合に対応
				if(!entrySeerCoList.contains(talk.getAgent())) {
					HandyGadget.addList(seerCOList, talk.getAgent());
				}
				HandyGadget.addList(entrySeerCoList, talk.getAgent());

				//既に占われたエージェント
				if (divinedList.contains(content.getTarget())) {
					List<Judge> tmp = forecastMap.getDivineJudgeList(content.getTarget());
					for (Judge j : tmp) {

						//異なるエージェントが同じ対象に発話したか判断
						if (talk.getAgent() == j.getAgent()) {
							continue; //同じ場合は処理をスキップする
						}
						//複数占い師が同じ結果を出している
						if (j.getResult() == content.getResult()) {
							//2名以上の占い師が白を出した
							if (content.getResult() == Species.HUMAN) {
								HandyGadget.addList(villagerList, content.getTarget());
							}
							//2名以上の占い師が黒を出した
							else {
								HandyGadget.addList(werewolfList, content.getTarget());
							}
						}
						//複数の占い師が異なる結果を出している
						else {
							HandyGadget.addList(pandaList, content.getTarget());
						}
					}
				}

				//初めて占われたエージェント
				HandyGadget.addList(divinedList, content.getTarget());

				//占い対象が自分
				if (content.getTarget() == ownData.getMe()) {
					if (content.getResult() == Species.WEREWOLF) {
						//黒出しをしたことがない
						if (!seerBlackCount.containsKey(talk.getAgent())) {
							seerBlackCount.put(talk.getAgent(), 1);
						}
						else {
							int tmp = seerBlackCount.get(talk.getAgent());
							seerBlackCount.put(talk.getAgent(), ++tmp);
						}
						myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(), Role.POSSESSED));
						possessed = talk.getAgent();
						whiteList.remove(talk.getAgent());
						HandyGadget.addList(blackList, talk.getAgent());

						//発言者が真偽判断中のエージェント
						if (talk.getAgent() == seer) {
							//占い師は偽物
							HandyGadget.addList(werewolfList, talk.getAgent());
							whiteList.remove(seer);
							seer = null;
						}
					}
					else {
						HandyGadget.addList(whiteList, talk.getAgent());
					}
				}
				//占い対象が自分以外
				else {
					if (content.getResult() == Species.WEREWOLF) {
						//黒出しをしたことがない
						if (!seerBlackCount.containsKey(talk.getAgent())) {
							seerBlackCount.put(talk.getAgent(), 1);
						}
						else {
							int tmp = seerBlackCount.get(talk.getAgent());
							seerBlackCount.put(talk.getAgent(), ++tmp);
						}
						HandyGadget.addList(blackList, content.getTarget());
					}
					else {
						//占われたエージェントが占いCOしていれば
						if (seerCOList.contains(content.getTarget())) {
							myTalking.addTalk(TalkFactory.estimateRemark(content.getTarget(), Role.POSSESSED));
							possessed = content.getTarget();
							whiteList.remove(content.getTarget());
						}
						else {
							HandyGadget.addList(whiteList, content.getTarget());
						}
					}
				}
			}

			//霊媒結果
			if (content.getTopic()==Topic.IDENTIFIED) {

				//霊媒COをしていない場合に対応
				if(!entryMediumCoList.contains(talk.getAgent())) {
					HandyGadget.addList(mediumCOList, talk.getAgent());
				}
				HandyGadget.addList(entryMediumCoList, talk.getAgent());

				HandyGadget.addList(blackList, talk.getAgent());
			}
		}
		setEntityData();
	}


	/**
	 * 基本行動
	 */
	public void nomalMediumAction () {
		//リストの重複チェック
		Set<Agent> set = new HashSet<>(blackList);
		blackList = new ArrayList<>(set);

		set = new HashSet<>(whiteList);
		whiteList = new ArrayList<>(set);

		if (!oneAction) {
			//霊媒結果が人狼の場合
			if (Check.isNotNull(todayJudge)) {
				if (todayJudge.getResult() == Species.WEREWOLF) {
					if (!ownData.isCO()) {
						GeneralAction.sayComingout(ownData, myTalking, Role.MEDIUM);

					}
				}
				identQueue.offer(todayJudge);
			}
			//COしていれば
			if (ownData.isCO()) {
				//今までの霊媒結果を全部話す
				while (!identQueue.isEmpty()) {
					Judge ident = identQueue.poll();
					myTalking.addTalk(TalkFactory.identRemark(ident.getTarget(), ident.getResult()));
				}
				ownData.setActFlagIdent();
			}

			//4日目以降
			if (changeAction) {
				decideVote();
			}

			//1日目から3日目まで
			else {
				voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList());
				if (voteAgent == null) {
					voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
				}
				//前回の投票対象と同じでない
				if (provVoteAgent != voteAgent) {
					provVoteAgent = voteAgent;
					ownData.setVoteTarget(voteAgent);
					GeneralAction.sayVote(ownData, myTalking);
				}
			}

			//4ターン目以降
			if (turn.afterTurn(4)) {
				//１日に１度、占い対象を報告する
				if (!oneAction) {
					//占い師リストが空でない
					if (!seerCOList.isEmpty()) {
						for (Agent agent : seerCOList) {
							//占い師が生存している場合
							if (ownData.getAliveOtherAgentList().contains(agent)) {
								//生きているエージェントから既に占ったエージェントとCOしているエージェントを除く
								List<Agent> tmpd = ownData.getAliveOtherAgentList();
								tmpd.removeAll(divinedList);
								tmpd.removeAll(mediumCOList);
								tmpd.removeAll(seerCOList);
								myDivinationAgent = RandomSelect.randomAgentSelect(tmpd);
								//nullならCOしているエージェントでも可
								if (myDivinationAgent == null) {
									tmpd.addAll(seerCOList);
									tmpd.addAll(mediumCOList);
									myDivinationAgent = RandomSelect.randomAgentSelect(tmpd);
								}
								//nullなら生きているエージェント
								if (myDivinationAgent == null) {
									myDivinationAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
								}
								myTalking.addTalk(TalkFactory.requestAllDivinationRemark(myDivinationAgent));
								oneAction = true;
							}
						}
					}
				}
			}
			DoubtRole();

			oneAction = true;
		}

		//COしている　かつ　まだ霊能結果を話していない
		if (ownData.isCO() && !ownData.isIdent()) {
			//今までの霊媒結果を全部話す
			while (!identQueue.isEmpty()) {
				Judge ident = identQueue.poll();
				myTalking.addTalk(TalkFactory.identRemark(ident.getTarget(), ident.getResult()));
			}
			ownData.setActFlagIdent();
		}
	}


	/**
	 * 占い師や霊媒師の黒出し数の矛盾を調べる
	 */
	public void DoubtRole(){
		for (Agent agent : seerCOList) {
			if (seerBlackCount.containsKey(agent)) {
				//占い師の黒出し回数が潜伏数を超えた
				if (seerBlackCount.get(agent) > (6-foCount)) {
					//偽物
					whiteList.remove(agent);
					HandyGadget.addList(werewolfList, agent);
					HandyGadget.addList(blackList, agent);
					myTalking.addTalk(TalkFactory.estimateRemark(agent, Role.WEREWOLF));

					//このエージェントが出した判定を全て消す
					List<Judge> tmp = forecastMap.getDivineJudgeList(agent);
					for (Judge j : tmp) {
						if (j.getResult() == Species.HUMAN) {
							whiteList.remove(j.getTarget());
						}
						else {
							blackList.remove(j.getTarget());
						}
					}
				}
			}
		}
	}


	/**
	 * 投票発言(4日目以降)
	 */
	public void decideVote() {
		/**
		 * 投票優先順位
		 * ほぼ人狼 ＞ 多分人狼 ＞ パンダ ＞ 狂人 ＞ 確定？白、多分白を除く ＞ 多分白を除く ＞　ランダム
		 * */
		
		//生存者チェック
		AgentAliveCheck();

		if (!werewolfList.isEmpty()) {
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), werewolfList);
		}
		else if (!blackList.isEmpty()) {
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), blackList);
		}
		else if (!pandaList.isEmpty()) {
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), pandaList);
		}
		else if (possessed != null) {
			voteAgent = possessed;
		}
		if (voteAgent == null) {
			voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),villagerList,whiteList);
		}
		if (voteAgent == null) {
			voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), villagerList);
		}
		if (voteAgent == null) {
			voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
		}
		if (provVoteAgent != voteAgent) {
			provVoteAgent = voteAgent;
			ownData.setVoteTarget(voteAgent);
			GeneralAction.sayVote(ownData, myTalking);
		}
	}

	//変数やリストから死亡者を排除する
	public void AgentAliveCheck() {
		whiteList.removeAll(ownData.getDeadAgentList());
		blackList.removeAll(ownData.getDeadAgentList());
		//		mediumCOList.removeAll(ownData.getDeadAgentList());
		//		seerCOList.removeAll(ownData.getDeadAgentList());
		pandaList.removeAll(ownData.getDeadAgentList());
		villagerList.removeAll(ownData.getDeadAgentList());
		werewolfList.removeAll(ownData.getDeadAgentList());
		if (ownData.getDeadAgentList().contains(werewolf)) {
			werewolf = null;
		}
		if (ownData.getDeadAgentList().contains(bodyguard)) {
			bodyguard = null;
		}
		if (ownData.getDeadAgentList().contains(seer)) {
			seer = null;
		}
	}


	/**
	 * 占い結果と霊媒結果から占い師の真偽を判定する
	 */
	public void seermediumcheck() {
		//今日霊媒対象を占ったエージェントの結果リストを作成
		List<Judge> seerJudge = forecastMap.getDivineJudgeTarget(todayJudge.getTarget());
		for (Judge judge : seerJudge) {
			//占い結果と霊媒結果が同じ
			if (judge.getResult() == todayJudge.getResult()) {
				//霊媒結果が黒
				if (todayJudge.getResult() == Species.WEREWOLF) {
					//99%占い師
					seer = judge.getAgent();
					myTalking.addTalk(TalkFactory.estimateRemark(judge.getAgent(), Role.SEER));
				}
			}
			//占い結果と霊媒結果が異なる
			else {
				//占い結果を出したエージェントをblackListへ
				HandyGadget.addList(blackList, judge.getAgent());
				myTalking.addTalk(TalkFactory.estimateRemark(judge.getAgent(), Role.WEREWOLF));
			}
		}
	}
}
