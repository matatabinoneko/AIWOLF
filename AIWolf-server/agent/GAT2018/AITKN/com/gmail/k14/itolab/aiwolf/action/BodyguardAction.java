package com.gmail.k14.itolab.aiwolf.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import com.gmail.k14.itolab.aiwolf.definition.CauseOfDeath;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 狩人の行動(5人)
 * @author k14096kk
 *
 */
public class BodyguardAction extends BaseRoleAction{

	/**key=seer,value=medium 占いと霊媒のライン*/
	Map<Agent,Agent> lineSeerMedium = new HashMap<Agent,Agent>();
	/**key=medium,value=seer 霊媒と占いのライン*/
	Map<Agent,Agent> lineMediumSeer = new HashMap<Agent,Agent>();
	/**key=seer,value=spe.wolf seer黒出し回数*/
	Map<Agent,Integer> seerBlackCount = new HashMap<Agent,Integer>();
	/**key=medium,value=spe.wolf medium黒出し回数*/
	Map<Agent,Integer> mediumBlackCount = new HashMap<Agent,Integer>();

	/**占いCOしたエージェントを格納するリスト*/
	List<Agent> seerCOList = new ArrayList<>(); //
	/**霊媒COしたエージェントを格納するリスト*/
	List<Agent> mediumCOList = new ArrayList<>(); //
	/**占い師COと霊媒師COしたエージェントを格納するリスト*/
	List<Agent> foList = new ArrayList<>(); //
	/**白出しされたエージェントリスト*/
	List<Agent> whiteList = new ArrayList<>(); //
	/**黒出しされたエージェントリスト*/
	List<Agent> blackList = new ArrayList<>(); //
	/**2名以上から白を出されたリスト*/
	List<Agent> villagerList = new ArrayList<>(); //
	/**2名以上から黒を出されたまたは矛盾した発言をしたリスト*/
	List<Agent> werewolfList = new ArrayList<>(); //
	/**占われたエージェントリスト*/
	List<Agent> divinedAgentList = new ArrayList<>(); //
	/**占い結果が分かれたエージェントリスト*/
	List<Agent> pandaList = new ArrayList<>(); //
	/**投票したいエージェントリスト*/
	List<Agent> voteAgentList = new ArrayList<>(); //

	/**投票するエージェント*/
	Agent voteAgent = null;
	/**前ターンに投票発言したエージェントを格納する*/
	Agent provVoteAgent = null; 
	/**本物占い師*/
	Agent seer = null;
	/**本物霊媒師*/
	Agent medium = null;
	/**占い師と霊媒師の真偽*/
	Agent whatSeer = null;
	/**霊媒師と占い師の真偽*/
	Agent whatMedium = null;
	/**狂人だと思われるエージェント*/
	Agent possessed = null;
	/**狩人COしたエージェント*/
	Agent bodyguard = null;
	/**人狼だと思われるエージェント*/
	Agent werewolf = null;
	/**占って欲しいと発言するエージェント*/
	Agent myDivinationAgent = null;
	/**護衛対象を格納する*/
	Agent guardTarget = null;

	/**占い師、霊媒師のCO数を格納しておくための変数*/
	int FOCount = 0;
	/**人狼数を格納しておくための変数*/
	int jinroCount = ownData.getSettingRoleNum(Role.WEREWOLF);

	/**1日の特定行動制御*/
	boolean oneAction = false;
	/**15人における行動変更*/
	boolean changeAction = false;
	/**占い師が襲撃された*/
	boolean seerAttack = false;
	/**霊媒師が襲撃された*/
	boolean mediumAttack = false;

	
	/**
	 * 狩人行動のコンストラクタ
	 * @param entityData :オブジェクトデータ
	 */
	public BodyguardAction(EntityData entityData) {
		super(entityData);
		/*ここに処理*/
		ownData.rejectReaction();
		/**/
		setEntityData();
	}

	
	/**
	 * 1日のはじめに呼ぶ処理
	 */
	@Override
	public void dayStart() {
		super.dayStart();

		oneAction = false; //1日の特定行動初期化

		//護衛成功
		if (guardTarget != null && ownData.getAttackedAgent() == null) {
			//whiteListに追加・blackList,werewolfListから削除
			HandyGadget.addList(whiteList, guardTarget);
			blackList.remove(guardTarget);
			werewolfList.remove(guardTarget);
			//護衛対象が霊媒師（おそらく真）
			if (mediumCOList.contains(guardTarget)) {
				HandyGadget.addList(villagerList, guardTarget);
			}
			//護衛対象が村人の狩人騙り
			if (guardTarget == bodyguard) {
				HandyGadget.addList(whiteList, bodyguard);
			}
		}

		//護衛失敗
		if (ownData.getAttackedAgent() != null) {
			guardTarget = null;
			//占い師のリストから襲撃者が出た
			if (seerCOList.contains(ownData.getAttackedAgent())) {
				seerAttack = true;
			}
			//霊媒師のリストから襲撃者が出た
			else if (mediumCOList.contains(ownData.getAttackedAgent())) {
				mediumAttack = true;
			}
		}

		//昨日の処刑者がいる
		if (ownData.getExecutedAgent() != null) {
			//昨日の処刑者がwerewolfListまたはblackListに含まれている
			if (werewolfList.contains(ownData.getExecutedAgent()) || blackList.contains(ownData.getExecutedAgent())) {
				jinroCount--;
			}
		}

		//占い師真偽チェック
		SeerTrueFalse();

		//霊媒師真偽チェック
		MediumTrueFalse();

		//生存チェック
		AgentAliveCheck();

		setEntityData();
	}
	
	/***
	 * 投票時に呼ぶ処理
	 */
	@Override
	public void vote() {
		super.vote();
		/**
		 * ↓これ結構良さげに思える
		 * */
		//		voteAgentList.clear();
		//		voteAgentList.addAll(ownData.getAliveOtherAgentList());
		//		voteAgentList.removeAll(villagerList);
		//		voteAgentList.removeAll(whiteList);
		//		voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), voteAgentList);

		//投票対象が護衛成功者の場合
		if (ownData.getGuardResultTargetList(true).contains(voteAgent)) {
			List<Agent> tmp = ownData.getAliveOtherAgentList();
			tmp.removeAll(ownData.getGuardResultTargetList(true));
			//生存者の中から護衛成功者以外の最多に投票
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), tmp);
		}
		ownData.setVoteTarget(voteAgent);
		setEntityData();
	}
	
	
	/**
	 * 護衛時に呼ぶ処理
	 */
	@Override
	public void guard() {
		super.guard();

		AgentAliveCheck();

		//護衛対象が投票で死亡している
		if (ownData.getDeadAgentList().contains(guardTarget)) {
			guardTarget = null;
		}

		//前日に被襲撃者がいる場合（護衛失敗の場合）
		if (ownData.getAttackedAgent() != null) {
			if (seer != null) {
				//占い師が生きている
				if (ownData.getAliveOtherAgentList().contains(seer)) {
					guardTarget = seer;
				}
			}
			if (medium != null) {
				//霊媒師が生きている
				if (ownData.getAliveOtherAgentList().contains(medium)) {
					guardTarget = medium;
				}
			}
			else {
				//占い師が襲撃されていない
				if (!seerAttack) {
					//占い師が1人
					if (forecastMap.getComingoutRoleAgentList(Role.SEER).size() == 1) {
						guardTarget = forecastMap.getComingoutRoleAliveAgent(ownData.getAliveAgentList(), Role.SEER);
					}
					//占い師の中からランダムに
					else if (!seerCOList.isEmpty()) {
						List<Agent> rs = removeDead(seerCOList);
						guardTarget = RandomSelect.randomAgentSelect(rs);
					}
				}
				//霊媒師が襲撃されていない
				else if (!mediumAttack) {
					//霊媒師が1人
					if (forecastMap.getComingoutRoleAgentList(Role.MEDIUM).size() == 1) {
						guardTarget = forecastMap.getComingoutRoleAliveAgent(ownData.getAliveAgentList(), Role.MEDIUM);				
					}

					//霊媒師の中からランダムに
					else if (!mediumCOList.isEmpty()) {
						List<Agent> rm = removeDead(mediumCOList);
						guardTarget = RandomSelect.randomAgentSelect(rm);
					}
				}
				else if (!villagerList.isEmpty()) {
					guardTarget = RandomSelect.randomAgentSelect(villagerList);
				}
				//白判定を受けたエージェントからランダムに
				else if (!whiteList.isEmpty()) {
					guardTarget = RandomSelect.randomAgentSelect(whiteList);
				}
				//生きているエージェントからランダムに
				else {
					guardTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
				}
			}
		}
		//護衛成功の場合
		else {
			//護衛対象が死亡している場合
			if (ownData.getDeadAgentList().contains(guardTarget)) {
				guardTarget = null;
			}
		}

		if (guardTarget == null) {
			if (!seerAttack) {
				//占い師の中からランダムに
				if (!seerCOList.isEmpty()) {
					guardTarget = RandomSelect.randomAgentSelect(removeDead(seerCOList));				
				}
			}
			else if (!mediumAttack) {
				//霊媒師の中からランダムに
				if (!mediumCOList.isEmpty()) {
					guardTarget = RandomSelect.randomAgentSelect(removeDead(mediumCOList));				
				}
			}
			else {
				//白判定を受けたエージェントからランダムに
				if (!villagerList.isEmpty()) {
					guardTarget = RandomSelect.randomAgentSelect(villagerList);
				}
				else if (!whiteList.isEmpty()) {
					guardTarget = RandomSelect.randomAgentSelect(whiteList);
				}
				//生きているエージェントからランダムに
				else {
					guardTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
				}
			}
		}

		ownData.setGuardTarget(guardTarget);
		setEntityData();
	}
	

	/**
	 * 行動選択
	 */
	@Override
	public void selectAction() { 
		super.selectAction();

		this.nomalBodyguardAction();

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
		if (reqContent.getTarget() != ownData.getMe()) {
			//リクエストVoteがあったとき
			if (reqContent.getTopic() == Topic.VOTE) {
				//一番投票発言を受けているエージェントならば投票発言する
				if (reqContent.getTarget() == voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList())) {
					voteAgent = reqContent.getTarget();
					myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
				}
			}
			//リクエストguardがあった時
			if (reqContent.getTopic() == Topic.GUARD) {
				//COしているエージェントであれば護衛する
				if (foList.contains(reqContent.getTarget())) {
					guardTarget = reqContent.getTarget();
				}
			}
		}
		/**/
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
						FOCount--;
					}
					//霊媒師からのスライド
					else if (mediumCOList.contains(talk.getAgent())) {
						mediumCOList.remove(talk.getAgent());
						FOCount--;
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
						FOCount++;
					}
					
					// 占い師登録
					if(!entrySeerCoList.contains(talk.getAgent())) {
						HandyGadget.addList(seerCOList, talk.getAgent());
					}
					HandyGadget.addList(entrySeerCoList, talk.getAgent());
					
					HandyGadget.addList(foList, talk.getAgent());
				}
				//霊媒CO
				if (content.getRole() == Role.MEDIUM) {
					if (!mediumCOList.contains(talk.getAgent())) {
						FOCount++;
					}
					
					// 霊媒師登録
					if(!entryMediumCoList.contains(talk.getAgent())) {
						HandyGadget.addList(mediumCOList, talk.getAgent());
					}
					HandyGadget.addList(entryMediumCoList, talk.getAgent());
					
					HandyGadget.addList(foList, talk.getAgent());
				}
				//狩人CO
				if (content.getRole() == Role.BODYGUARD) {
					bodyguard = talk.getAgent();
					guardTarget = bodyguard;
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
				if (divinedAgentList.contains(content.getTarget())) {
					List<Judge> tmp = forecastMap.getDivineJudgeList(content.getTarget());
					for ( Judge j : tmp ) {

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
				HandyGadget.addList(divinedAgentList, content.getTarget());

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
						if (talk.getAgent() == whatSeer) {
							//占い師は偽物
							HandyGadget.addList(werewolfList, talk.getAgent());
							whiteList.remove(whatSeer);
							whatSeer = null;
							//霊媒師は本物
							HandyGadget.addList(whiteList, whatMedium);
							whatMedium = null;
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
				
				if (content.getResult() == Species.WEREWOLF) {
					//黒出しをしたことがない
					if (!mediumBlackCount.containsKey(talk.getAgent())) {
						mediumBlackCount.put(talk.getAgent(), 0);
					}
					else {
						int tmp = mediumBlackCount.get(talk.getAgent());
						mediumBlackCount.put(talk.getAgent(), ++tmp);
					}
				}
				//占い結果のリスト
				List<Judge> tmp = forecastMap.getDivineJudgeList(content.getTarget());
				for (Judge j : tmp) {
					//占い対象と霊媒対象が同じ
					if (j.getTarget() == content.getTarget()) {
						//占い結果と霊媒結果が同じ
						if (j.getResult() == content.getResult()) {
							//ラインのマップ作成
							lineSeerMedium.put(j.getAgent(), talk.getAgent());
							lineMediumSeer.put(talk.getAgent(), j.getAgent());
						}
						//占い結果と霊媒結果が異なる
						else {
							//霊媒師が偽物
							if (blackList.contains(talk.getAgent()) || werewolfList.contains(talk.getAgent())) {
								HandyGadget.addList(whiteList, j.getAgent()); //占い師をwhiteListへ
							}
							//占い師が偽物
							else if (blackList.contains(j.getAgent()) || werewolfList.contains(j.getAgent())) {
								HandyGadget.addList(whiteList, talk.getAgent()); //霊媒師をwhiteListへ
							}
							//真偽不明
							else {
								whatSeer = j.getAgent(); //ひとまず代入
								whatMedium = talk.getAgent(); //ひとまず代入
							}
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
	public void nomalBodyguardAction(){

		//リストの重複チェック
		Set<Agent> set = new HashSet<>(blackList);
		blackList = new ArrayList<>(set);

		set = new HashSet<>(whiteList);
		whiteList = new ArrayList<>(set);
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

		// 4ターン目以降
		if (turn.afterTurn(4)) {
			// 1日に1度、占い対象を報告する
			if (!oneAction) {
				//占い師リストが空でない
				if (!seerCOList.isEmpty()) {
					for (Agent agent : seerCOList) {
						//占い師が生存している場合
						if (ownData.getAliveOtherAgentList().contains(agent)) {
							//生存エージェントから占い済みエージェントとCOしているエージェントを除いて，占いたいエージェントにする
							List<Agent> tmpd = ownData.getAliveOtherAgentList();
							tmpd.removeAll(divinedAgentList);
							tmpd.removeAll(foList);
							myDivinationAgent = RandomSelect.randomAgentSelect(tmpd);

							//nullならCOしているエージェントでも可
							if (myDivinationAgent == null) {
								tmpd.addAll(foList);
								myDivinationAgent = RandomSelect.randomAgentSelect(tmpd);
							}

							//nullなら生きているエージェント
							if (myDivinationAgent == null) {
								myDivinationAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
							}

							// 占いリクエスト 
							myTalking.addTalk(TalkFactory.requestAllDivinationRemark(myDivinationAgent));
							oneAction = true;
							break;
						}
					}
				}
			}
		}
		DoubtRole();

	}

	
	/**
	 * 死亡者をリストから除く（元のリストは変更しない）
	 * @param tmp :リスト
	 * @return 死亡者を省いたリスト
	 */
	public List<Agent> removeDead(List<Agent> tmp) {
		List<Agent> tmpList = new ArrayList<>();
		tmpList = tmp;
		tmpList.removeAll(ownData.getDeadAgentList());
		return tmpList;
	}

	/**
	 * 占い師や霊媒師の黒出し数の矛盾を調べる
	 */
	public void DoubtRole(){
		for (Agent agent : seerCOList) {
			if (seerBlackCount.containsKey(agent)) {
				//占い師の黒出し回数が潜伏数を超えた
				if (seerBlackCount.get(agent) > (6-FOCount)) {
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

		for (Agent agent : mediumCOList) {
			if (mediumBlackCount.containsKey(agent)) {
				//2回以上黒を出した＝役割終了
				if(mediumBlackCount.get(agent) > 1) {
					HandyGadget.addAllList(blackList, mediumCOList);
					HandyGadget.addAllList(werewolfList, mediumCOList);
				}
			}
		}
	}

	
	
	/**
	 * 占い師の真偽を確かめる
	 */
	public void SeerTrueFalse() {
		
		boolean attack = false;
		List<Agent> sList = new ArrayList<>(seerCOList);
		for (Agent agent : sList) {
			//占い師が襲撃されている
			if (forecastMap.getCauseOfDeath(agent) == CauseOfDeath.ATTACKED) {
				attack = true;
				//襲撃された占い師の占い結果を取得
				List<Judge> tmp = forecastMap.getDivineJudgeList(agent);
				for (Judge j : tmp) {
					//占い結果白をwhiteListへ追加、blackListから削除
					if (j.getResult() == Species.HUMAN) {
						HandyGadget.addList(whiteList, j.getTarget());
						blackList.remove(j.getTarget());
					}
					//占い結果黒をblackListへ追加、whiteListから削除
					else {
						HandyGadget.addList(blackList, j.getTarget());
						whiteList.remove(j.getTarget());
					}
				}
				//襲撃されたエージェントをリストから削除
				seerCOList.remove(agent);
			}
		}
		if (attack) {
			//占い師が生存している場合、その人数分ループ
			for (Agent agent : seerCOList) {
				HandyGadget.addList(blackList, agent);
				//ラインで繋がっているエージェントがいる場合
				if (lineSeerMedium.containsKey(agent)) {
					//偽霊媒師をblackListとwerewolfListへ
					HandyGadget.addList(blackList, lineSeerMedium.get(agent));
					HandyGadget.addList(werewolfList, lineSeerMedium.get(agent));
				}
			}
			//whiteListに含まれる生存占い師を削除
			whiteList.removeAll(seerCOList);
		}
	}


	/**
	 * 霊媒師の真偽を確かめる
	 */
	public void MediumTrueFalse() {
		/* 真と疑い度をあげたくない人狼が白を出す方が多い印象がある */
		for (Agent agent : mediumCOList) {
			//2人以上の占い師から白を出された霊媒師
			if (villagerList.contains(agent)) {
				//霊媒師を確定させ、他霊媒師をblackListに追加、リストを空に
				medium = agent;
				HandyGadget.addAllList(blackList, mediumCOList);
				mediumCOList.clear();
				break;
			}
		}

		boolean attack = false;
		List<Agent> mList = new ArrayList<>(mediumCOList);
		for (Agent agent : mList) {
			//襲撃された霊媒師
			if (forecastMap.getCauseOfDeath(agent) == CauseOfDeath.ATTACKED) {
				attack = true;
				List<Judge> tmp = forecastMap.getIdentJudgeList(agent); //霊媒師のJudgeリスト
				for (Judge j : tmp) {
					for (Agent sa : seerCOList) {
						List<Judge> seertmp = forecastMap.getDivineJudgeList(sa); //占い師のJudgeリスト
						for (Judge sj : seertmp) {
							//占い師と霊媒師が同じ対象のエージェントを判定
							if (j.getTarget() == sj.getTarget()) {
								//占い師と霊媒師の結果が異なるエージェント
								if (j.getResult() != sj.getResult()) {
									//占い師は偽物
									HandyGadget.addList(blackList, sj.getAgent());
									whiteList.remove(sj.getAgent());
								}
							}
						}
					}
				}
				//襲撃されたエージェントをリストから削除
				mediumCOList.remove(agent);
			}
		}
		//襲撃があれば
		if (attack) {
			//霊媒師が生存している場合、その人数分ループ
			for (Agent agent : mediumCOList) {
				HandyGadget.addList(blackList, agent);
				HandyGadget.addList(werewolfList, agent);

				//ラインで繋がっているエージェントがいる場合
				if (lineMediumSeer.containsKey(agent)) {
					//偽占い師をblackListとwerewolfListへ
					HandyGadget.addList(blackList, lineMediumSeer.get(agent));
					HandyGadget.addList(werewolfList, lineMediumSeer.get(agent));
				}
			}
			whiteList.removeAll(mediumCOList);
		}
	}


	
	/**
	 * 変数やリストから死亡者を排除する
	 */
	public void AgentAliveCheck() {
		foList.removeAll(ownData.getDeadAgentList());
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
	 * 投票発言(4日目以降)
	 */
	public void decideVote() {
		/**
		 * 投票優先順位
		 * ほぼ人狼 ＞ 多分人狼 ＞ パンダ ＞ 狂人 ＞ 確定？白、多分白を除く ＞ 多分白を除く ＞　ランダム
		 * */

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

}
