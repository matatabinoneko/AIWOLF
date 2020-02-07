package com.gmail.k14.itolab.aiwolf.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.gmail.k14.itolab.aiwolf.data.MyTalking;
import com.gmail.k14.itolab.aiwolf.util.AbilityResultInfo;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.Debug;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 人狼の行動
 * @author k14096kk
 *
 */
public class WerewolfAction extends BaseRoleAction{

	/**囁き格納キュー*/
	protected MyTalking myWhisper;

	/**味方人狼のリスト*/
	List<Agent> wolfList = new ArrayList<>();
	/**占いCOしたエージェントを格納するリスト*/
	List<Agent> seerCOList = new ArrayList<>(); 
	/**霊媒COしたエージェントを格納するリスト*/
	List<Agent> mediumCOList = new ArrayList<>();
	/**占い師COと霊媒師COしたエージェントを格納するリスト*/
	List<Agent> foList = new ArrayList<>();
	/**白出しされたエージェントリスト*/
	List<Agent> whiteList = new ArrayList<>();
	/**黒出しされたエージェントリスト*/
	List<Agent> blackList = new ArrayList<>();
	/**2名以上から白を出されたリスト*/
	List<Agent> villagerList = new ArrayList<>();
	/**占われたエージェントリスト*/
	List<Agent> divinedAgentList = new ArrayList<>();
	/**占い結果が分かれたエージェントリスト*/
	List<Agent> pandaList = new ArrayList<>();

	/**投票対象*/
	Agent voteAgent = null;
	/**前ターンの投票対象*/
	Agent provVoteAgent = null;
	/**暫定の真占い師*/
	Agent seer = null;
	/**暫定の真霊媒師*/
	Agent medium = null;
	/**暫定の狂人*/
	Agent possessed = null;
	/**暫定の狩人*/
	Agent bodyguard = null;
	/**占って欲しいと発言するエージェント*/
	Agent myDivinationAgent = null;
	/**前回の襲撃先発言ターゲット格納*/
	Agent provAttackTarget = null;
	/**whisperで決めた襲撃対象*/
	Agent reqAttack = null;
	/**襲撃対象*/
	Agent attackTarget = null;


	/**占い師、霊媒師のCO数を格納しておくための変数*/
	int FOCount = 0;
	/**人狼数を格納しておくための変数*/
	int jinroCount = ownData.getSettingRoleNum(Role.WEREWOLF);

	/**1日の特定行動制御*/
	boolean oneAction = false;
	/**5人行動*/
	boolean fiveJinro = false;
	/**15人における行動変更*/
	boolean changeAction = false;
	/**囁きでの行動抑制フラグ*/
	boolean wolfoneaction = false;
	/**襲撃リクエストフラグ*/
	boolean reqattack = false;
	/**5人人狼の占い師騙りフラグ*/
	boolean isFiveFakeSeer = false;

	/***
	 * 人狼行動のコンストラクタ<br>
	 * ここで，味方人狼の判別も同時に行う
	 * @param entityData :オブジェクトデータ
	 */
	public WerewolfAction(EntityData entityData) {
		super(entityData);
		/*ここに処理*/
		ownData.rejectReaction();

		// 味方人狼をリストに保持する
		for(Agent agent:ownData.getAgentList()){
			if(forecastMap.getConfirmRole(agent)==Role.WEREWOLF){
				wolfList.add(agent);
			}
		}
		/**/
		setEntityData();
	}

	@Override
	public void getEntityData(EntityData entityData) {
		super.getEntityData(entityData);
		this.myWhisper = entityData.getMyWhisper();
	}


	@Override
	public void setEntityData() {
		super.setEntityData();
		entityData.setMyWhisper(myWhisper);
	}

	/**
	 * 1日のはじめに呼ぶ処理
	 */
	@Override
	public void dayStart() {
		super.dayStart();

		//初期化
		oneAction = false;
		wolfoneaction = false;
		reqattack = false;
		provVoteAgent = null;

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
		//15人人狼
		if (!fiveJinro) {
			//3日目以降
			if (ownData.getDay() > 3) {
				//投票対象が人狼
				if (wolfList.contains(voteAgent)) {
					//人狼以外の最多
					List<Agent> tmp = ownData.getAliveOtherAgentList();
					tmp.removeAll(wolfList);
					voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), tmp);
				}
			}
			ownData.setVoteTarget(voteAgent);
		}
		else {

		}
		setEntityData();
	}

	/**
	 * 囁き中の行動の選択
	 */
	@Override
	public void selectWhisperAction() {
		super.selectWhisperAction();
		this.whisperAction();
		setEntityData();
	}

	/**
	 * 襲撃時に呼ぶ処理
	 */
	@Override
	public void attack() {
		super.attack();
		Debug.print("attack yobidasi");

		// 5人人狼では，COしていない自分以外のエージェントを襲撃対象にする
		if (fiveJinro) {
			attackTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), forecastMap.comingoutAliveAgentList(), possessed);
		}
		//15人人狼
		else {
			if (ownData.getDay() == 1) {
				Debug.print("1日目");
				//占い師がCO
				if (seerCOList.size() > 1) {
					Debug.print("seer");
					if (Check.isNotNull(possessed)) {
						Debug.print("狂人が判明している");
						seerCOList.remove(possessed);
					}
					attackTarget = RandomSelect.randomAgentSelect(seerCOList,wolfList);
				}
				//霊媒師がCO
				else if (!mediumCOList.isEmpty()) {
					Debug.print("medium");
					attackTarget = RandomSelect.randomAgentSelect(mediumCOList,wolfList);
				}
				//占い、霊媒がCOしていない
				else {
					attackTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),wolfList);
				}
			}
			//2日目
			else {
				// 襲撃先が未決定or死亡していればもう一度決定メソッドを回す
				if(attackTarget==null || ownData.getDeadAgentList().contains(attackTarget)){
					attackMethod();
					// 襲撃先が未決定or死亡していれば生存者からランダム
					if (attackTarget==null || ownData.getDeadAgentList().contains(attackTarget)) {
						attackTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), wolfList);
					}
				}
			}
		}

		ownData.setAttackTarget(attackTarget);

		setEntityData();
	}


	/**
	 * 会話中に行う行動選択
	 */
	@Override
	public void selectAction() {
		super.selectAction();

		//１日目の生存人数が５人以下
		if (ownData.getDay() == 1 && ownData.getAliveOtherAgentList().size() < 6) {
			fiveJinro = true;
		}

		this.normalWerewolfAction();


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
	
		//発話対象から自分を外す
		if (reqContent.getTarget() != ownData.getMe()) {
			//リクエストVoteがあったとき
			if (reqContent.getTopic() == Topic.VOTE) {
				//一番投票発言を受けているエージェントならば投票発言する
				if (reqContent.getTarget() == voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList())) {
					voteAgent = reqContent.getTarget();
					myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
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
						FOCount--;
					}
					//霊媒師からのスライド
					else if (mediumCOList.contains(talk.getAgent())) {
						mediumCOList.remove(talk.getAgent());
						FOCount--;
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
					if(!wolfList.contains(talk.getAgent())) {
						bodyguard = talk.getAgent();
					}
				}
				//狂人CO
				if (content.getRole() == Role.POSSESSED) {
					if(!wolfList.contains(talk.getAgent())) {
						possessed = talk.getAgent();
					}

					// 5人人狼
					if(fiveJinro) {
						if(Check.isNum(ownData.getDay(), 2) && !ownData.isVote()) {
							// 人狼CO，狂人以外のCOエージェントを投票対象
							myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.WEREWOLF));
							voteAgent = RandomSelect.randomAgentSelect(seerCOList, ownData.getDeadAgentList(), possessed);
						}
					}
				}
				//人狼CO
				if (content.getRole() == Role.WEREWOLF) {

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
					// その対象の占い結果のリスト
					List<Judge> tmp = forecastMap.getDivineJudgeList(content.getTarget());

					for (Judge j : tmp) {
						//異なるエージェントが同じ対象に発話したか判断
						if (talk.getAgent() == j.getAgent()) {
							continue; //同じ占い師の場合は処理をスキップする
						}
						//複数占い師が同じ結果を出している
						if (j.getResult() == content.getResult()) {
							//2名以上の占い師が白を出した
							if (content.getResult() == Species.HUMAN) {
								HandyGadget.addList(villagerList, content.getTarget());
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

				//会話しているエージェントが人狼
				if (wolfList.contains(talk.getAgent())) {
					//黒出し
					if (content.getResult() == Species.WEREWOLF) {
						//味方がライン切りをしていないか
						if (content.getTarget() != ownData.getMe()) {
							HandyGadget.addList(blackList, content.getTarget());
							voteAgent = content.getTarget();
						}
						//味方がライン切り
						else {
							myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(),Role.POSSESSED));
						}
					}
					//白出し
					else {
						HandyGadget.addList(whiteList, content.getTarget());
					}
				}
				//会話しているエージェントが人狼以外
				else {
					//占い対象が自分
					if (content.getTarget() == ownData.getMe()) {
						// 自分に黒出し
						if (content.getResult() == Species.WEREWOLF) {
							seer = talk.getAgent();
							myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
							HandyGadget.addList(blackList, talk.getAgent());
						}
						else {
							// 自分に白出ししたら狂人
							possessed = talk.getAgent();
						}
					}
					//占い対象が自分以外
					else {
						//黒出し
						if (content.getResult() == Species.WEREWOLF) {
							//占い対象が味方人狼
							if (wolfList.contains(content.getTarget())) {
								seer = talk.getAgent();
								//3日目まで かつ　味方人狼が2人残っている
								if (ownData.getDay() < 3 && wolfList.size() > 1) {
									voteAgent = content.getTarget();
								}
							}
							//人狼以外に黒出し
							else {
								possessed = talk.getAgent();
								//占い対象をblackListへ
								HandyGadget.addList(blackList, content.getTarget());
								voteAgent = content.getTarget();

								// 5人人狼ならば自分以外への黒出しに投票(潜伏中)
								if(fiveJinro && !isFiveFakeSeer) {
									ownData.setVoteTarget(voteAgent);
									GeneralAction.sayVote(ownData, myTalking);
									myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
								}
							}
						}
						//白出し
						else {
							//占い対象が味方人狼
							if (wolfList.contains(content.getTarget())) {
								possessed = talk.getAgent();
							}
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

				//霊媒結果が黒
				if (content.getResult() == Species.WEREWOLF) {
					//霊媒対象が味方人狼でない
					if (!wolfList.contains(content.getTarget())) {
						//発言者が味方人狼でない
						if (!wolfList.contains(talk.getAgent())) {
							//人狼以外に黒出し
							possessed = talk.getAgent();
						}
					}
					//霊媒対象が味方人狼
					else {
						medium = talk.getAgent();
						//発言者が味方人狼でない
						if (!wolfList.contains(talk.getAgent())) {
							HandyGadget.addList(blackList, talk.getAgent());
						}
					}
				}
				//霊媒結果が白
				else {
					//霊媒対象が味方人狼でない
					if (!wolfList.contains(content.getTarget())) {
						medium = talk.getAgent();
						//発言者が味方人狼でない
						if (!wolfList.contains(talk.getAgent())) {
							HandyGadget.addList(blackList, talk.getAgent());
						}
					}
					//霊媒対象が味方人狼
					else {
						//発言者が味方人狼でない
						if (!wolfList.contains(talk.getAgent())) {
							//味方人狼に白出し
							possessed = talk.getAgent();
						}

					}
				}
			}
		}
		setEntityData();
	}

	//人狼の囁きにおけるリクエスト
	@Override
	public void requestWhisperAction(Talk talk, Content content, Content reqContent) {
		super.requestWhisperAction(talk, content, reqContent);

		//発言者が自分の場合はリターン
		if (talk.getAgent() == ownData.getMe()) {
			return;
		}

		//襲撃リクエストがあった場合、便乗
		if(content.getTopic() == Topic.ATTACK) {
			//前日に襲撃者がいない
			if (ownData.getAttackedAgent() == null) {
				if (ownData.getDay() == 1) {
					attackTarget = content.getTarget();
				}
				else {
					attackMethod();
				}
			}
			//前日に襲撃者がいる
			else {
				attackTarget = content.getTarget();
			}
			if (provAttackTarget != attackTarget) {
				provAttackTarget = attackTarget;
				myWhisper.addTalk(TalkFactory.attackRemark(attackTarget));
				myWhisper.addTalk(TalkFactory.requestAllAttackRemark(attackTarget));
				ownData.setAttackTarget(attackTarget);
			}
		}
		setEntityData();
	}

	@Override
	public void whisperAction(Talk talk, Content content) {
		super.whisperAction(talk, content);

		//発言者が自分の場合はリターン
		if (talk.getAgent() == ownData.getMe()) {
			return;
		}

		//襲撃発言があった場合、そこに便乗する
		if(content.getTopic() == Topic.ATTACK) {
			//前日の襲撃者がいない
			if (ownData.getAttackedAgent() == null) {
				//1日目
				if (ownData.getDay() == 1) {
					attackTarget = content.getTarget();
				}
				//1日目以降
				else {
					attackMethod();
				}
			}
			//前日の襲撃者がいる
			else {
				attackTarget = content.getTarget();
			}
			
			//前ターンの襲撃対象と異なる襲撃対象
			if (provAttackTarget != attackTarget) {
				//発言
				provAttackTarget = attackTarget;
				myWhisper.addTalk(TalkFactory.attackRemark(attackTarget));
				myWhisper.addTalk(TalkFactory.requestAllAttackRemark(attackTarget));
				ownData.setAttackTarget(attackTarget);
			}
		}
		setEntityData();
	}

	/**
	 * 人狼の囁きにおける会話
	 */
	public void whisperAction(){
		if (!wolfoneaction) {
			//0日目の最初だけ呼ばれる
			if (ownData.getDay() == 0) {
				myWhisper.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.VILLAGER));
			}
			//襲撃前に呼ばれる
			else {
				if (turn.getTurn() == 0) {
					Debug.print("turn0 -> attackMethod");
					attackMethod();
				}
			}
			wolfoneaction = true;
		}
	}

	/**
	 * 襲撃関連　昼終了後のwhisperとattack時に呼ばれる
	 */
	public void attackMethod(){
		Debug.print("*****attackMethod*****");
		AbilityResultInfo info = ownData.getAttackResultInfo(ownData.getDay() -1);
		if (Check.isNotNull(info)) {
			Agent target = info.getTarget();
			Debug.print("襲撃しようとしたエージェント"+target);
			//前日の襲撃者がいない
			if (ownData.getAttackedAgent() == null) {
				Debug.print("襲撃者がいない");
				//一度初期化
				attackTarget = null;
				//前日の襲撃対象が占い師
				if (seerCOList.contains(info.getTarget())) {
					Debug.print("前日の襲撃者が占い師");
					//霊媒師がいる
					if (!mediumCOList.isEmpty()) {
						Debug.print("霊能者を襲撃");
						attackTarget = RandomSelect.randomAgentSelect(mediumCOList,ownData.getDeadAgentList(),wolfList);
					}
				}
				//前日の襲撃対象が霊媒師
				else if (mediumCOList.contains(info.getTarget())) {
					Debug.print("前日の襲撃者が霊能者");
					//占い師がいる
					if (!seerCOList.isEmpty()) {
						Debug.print("占い師を襲撃");
						attackTarget = RandomSelect.randomAgentSelect(seerCOList,ownData.getDeadAgentList(),wolfList);
					}
				}
				//前日の襲撃対象が占い師・霊媒師以外
				else {
					Debug.print("前日の襲撃者がその他");
					//生存エージェントから人狼と前日襲撃対象を除外したリスト
					List<Agent> tmp = ownData.getAliveOtherAgentList();
					tmp.removeAll(wolfList);
					tmp.remove(info.getTarget());
					attackTarget = RandomSelect.randomAgentSelect(tmp);
				}

				if (attackTarget == null) {
					attackTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),wolfList);
				}
			}
			//前日の襲撃者がいる
			else {
				Debug.print("前日の襲撃者がいる");
				//占い師を襲撃
				if (!seerCOList.isEmpty()) {
					Debug.print("占い師を襲撃");
					List<Agent> tmp = seerCOList;
					tmp.remove(possessed);
					attackTarget = RandomSelect.randomAgentSelect(tmp,ownData.getDeadAgentList(),wolfList);
				}
				//霊能者を襲撃
				else if (!mediumCOList.isEmpty()) {
					Debug.print("霊能者を襲撃");
					List<Agent> tmp = mediumCOList;
					tmp.remove(possessed);
					attackTarget = RandomSelect.randomAgentSelect(tmp,ownData.getDeadAgentList(),wolfList);
				}
				//その他を襲撃
				else {
					Debug.print("その他を襲撃");
					attackTarget = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(),wolfList);
				}
			}
		}
	}

	/**
	 * 基本行動
	 */
	public void normalWerewolfAction() {

		//リストの重複チェック
		Set<Agent> set = new HashSet<>(blackList);
		blackList = new ArrayList<>(set);

		set = new HashSet<>(whiteList);
		whiteList = new ArrayList<>(set);

		//5人人狼
		if (fiveJinro) {
			Debug.print("voteAgent = " + voteAgent);
			Debug.print("getVoteTarget() = " + ownData.getVoteTarget());

			if (!oneAction) {
				// 1日目
				if (ownData.getDay() == 1) {
					// ランダム値
					int rand = 0;
					rand = RandomSelect.randomInt(99);

					//2割の確率で占い師を騙る
					if (rand < 20) {
						isFiveFakeSeer = true;
						// 占い師COして，ランダムに黒出し
						GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
						voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
						myTalking.addTalk(TalkFactory.divinedResultRemark(voteAgent, Species.WEREWOLF));
						// 黒出しに投票発言
						ownData.setVoteTarget(voteAgent);
						GeneralAction.sayVote(ownData, myTalking);
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
					}
					else {
						// 騙らないならば生存エージェントからランダムに投票発言
						voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
						myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
					}
				}
				// 2日目以降
				else {

					// 狂人が生存している
					if(ownData.getAliveAgentList().contains(possessed)) {
						// 人狼CO，狂人以外のCOエージェントを投票対象
						myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.WEREWOLF));
						voteAgent = RandomSelect.randomAgentSelect(seerCOList, ownData.getDeadAgentList(), possessed);
						ownData.setVoteTarget(voteAgent);
						GeneralAction.sayVote(ownData, myWhisper);
					}
					// 占い師が生存している
					else if(ownData.getAliveAgentList().contains(seer)) {
						// 占い師を人狼予想，投票対象とする
						myTalking.addTalk(TalkFactory.estimateRemark(seer, Role.WEREWOLF));
						voteAgent = seer;
					}

					/*
					// COしている場合
					if (ownData.isCO()) {
						// 占い師COしているエージェントが1人以上ならば，その中の生存エージェントにランダムに投票
						if (seerCOList.size() >= 1) {
							voteAgent = RandomSelect.randomAgentSelect(seerCOList, ownData.getDeadAgentList(), possessed);
						}
					}
					 */

					// 投票対象が決まっていないor投票対象が死亡している場合，ランダムに生存エージェントに投票(狂人除く)
					if (voteAgent == null || ownData.getDeadAgentList().contains(voteAgent)) {
						voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
					}

					//投票と投票リクエスト 
					myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(voteAgent));
					ownData.setVoteTarget(voteAgent);
				}

				oneAction = true;
			}
		}

		//15人人狼
		else {
			//4日目以降
			if (changeAction) {
				decideVote();
			}
			//1日目から3日目まで
			else {
				//投票対象がまだ存在していない
				if (voteAgent == null) {
					// 投票数が最大のエージェントを投票対象にする
					voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList());
				}
				// もし決まっていなければ生存者からランダム
				if (voteAgent == null) {
					voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
				}
				//前回の投票対象と同じでない
				if (provVoteAgent != voteAgent) {
					// 同じ対象への投票発言の繰り返し阻止
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
		//生存者チェック
		AgentAliveCheck();

		/*
		if (!blackList.isEmpty()) {
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), blackList);
		}
		else if (!pandaList.isEmpty()) {
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), pandaList);
		}
		else if (possessed != null) {
			voteAgent = possessed;
		}
		 */

		// 人狼以外の生存エージェントから得票数が最大のエージェントに投票発言
		List<Agent> voteCandidates = ownData.getAliveOtherAgentList();
		voteCandidates.removeAll(wolfList);
		voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), voteCandidates);
		// もし，いないならば人狼以外の生存エージェントからランダム
		if(Check.isNull(voteAgent)) {
			voteAgent = RandomSelect.randomAgentSelect(voteCandidates);
		}

		// 投票繰り返しの阻止
		if (provVoteAgent != voteAgent) {
			provVoteAgent = voteAgent;
			ownData.setVoteTarget(voteAgent);
			GeneralAction.sayVote(ownData, myTalking);
		}
	}


}
