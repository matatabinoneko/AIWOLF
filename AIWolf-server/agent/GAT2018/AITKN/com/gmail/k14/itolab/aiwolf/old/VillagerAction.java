package com.gmail.k14.itolab.aiwolf.old;


import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.definition.Strategy;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.OperatorElement;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 村人の行動(5人)
 * @author k14096kk
 *
 */
public class VillagerAction extends BaseRoleAction{

	Agent rollerAgent = null; // ローラーの対象エージェント格納用
	Agent seer = null; // 占いCOしたエージェントの格納用(他に方法ある気がする。探せてないだけで。)
	Agent voteWolfAgent = null; // 占い師が他のエージェントに対して黒出ししたときに、そのエージェントに投票する用

	List<Agent> coSeerMB = new ArrayList<>(); // 自分を黒判定した占い師COエージェントリスト
	List<Agent> coSeerMW = new ArrayList<>(); // 自分を白判定した占い師COエージェントリスト
	List<Agent> coSeerOB = new ArrayList<>(); // 他人を黒判定した占い師COエージェントリスト
	List<Agent> coSeerOW = new ArrayList<>(); // 他人を黒判定した占い師COエージェントリスト

	List<Agent> seerRoller = new ArrayList<>(); // ローラー用エージェントリスト
	
	/**ローラー決定フラグ*/
	boolean rollerFlag = false;

	
	public VillagerAction(EntityData entityData) {
		super(entityData);
		setEntityData();
	}

	/**
	 * 一日のはじめの行動(整理行動)
	 */
	@Override
	public void dayStart() {
		super.dayStart();
		if(ownData.currentDay(2)) {
			this.setProveRole();
		}
		// ローラーが上手く出来たかどうか　（生存リストに含まれていない）
		if(!ownData.getAliveAgentList().contains(rollerAgent)){
			// 前回のローラー対象エージェントを次のローラー対象エージェントリストから削除する
			seerRoller.remove(rollerAgent);
			coSeerMB.remove(rollerAgent);
			coSeerOB.remove(rollerAgent);
		}
		
		setEntityData();
	}
	
	@Override
	public void selectAction() { 
		super.selectAction();
		// 1日目は行動１つのみ
		if(ownData.currentDay(1)) {
			this.nomalVillagerAction();
		}
		// 2日目以降戦略で行動変更(仮)
		if(ownData.currentDay(2)) {
			switch (ownData.getStrategy()) {
			case VILLAGER_WEREWOLF_CO:
				this.swindleWerewolf();
				break;
			default:
				this.nomalVillagerAction();
				break;
			}
		}
		
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		// 投票リクエスト
		if(Check.isTopic(OperatorElement.getTopic(content),Topic.VOTE)) {
			// 投票対象がローラーリスト内にいれば同意
			if(seerRoller.contains(OperatorElement.getTarget(content))) {
				myTalking.addTalk(TalkFactory.agreeRemark(talk));
			}
		}
		
		// 予想リクエスト
		if(Check.isTopic(OperatorElement.getTopic(content),Topic.ESTIMATE)) {
			// 予想役職が暫定役職と同じならば同意
			if(Check.isRole(OperatorElement.getRole(content), forecastMap.getProvRole(OperatorElement.getTarget(content)))) {
				myTalking.addTalk(TalkFactory.agreeRemark(talk));
			}
		}
		
		setEntityData();
	}

	/**
	 * 暫定役職決定
	 */
	public void setProveRole() {

		// 被襲撃者の暫定役職が決まっていない
		if(forecastMap.getProvRole(ownData.getAttackedAgent())==null) {
			forecastMap.setProvRole(ownData.getAttackedAgent(), Role.VILLAGER);
		}

		if(ownData.isFinish()) {
			// 残りの暫定役職
			List<Agent> agentList = forecastMap.getProvRoleAgentList(null);
			forecastMap.setRemainRoleRandom(ownData.getRoleNumMap(), agentList);
		}
		
		setEntityData();
	}

	/**
	 * 村人基本の行動
	 */
	public void nomalVillagerAction(){
		// 1日目
		if(ownData.currentDay(1)) {
			/*
			 turn0 skip
			 turn1 myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.SEER)); //　拡大解釈してFOと捉えてください。
			 turn2 skip
			 turn3 myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.VILLAGER)); // 自分の役職を村人だとCOする
			 turn4 myTalking.addTalk(TalkFactory.requestAllEstimateRemark(rollerAgent, Role.POSSESSED)); // 他のエージェントにrollerAgentを狂人だと予想するようにリクエストする
			 turn5 myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget())); // 投票先に決定しているエージェントに投票する


			 requestVoteがあった時　if(rollerList.size()>=1 または requestVoteTarget(rollerList.contain) または requestVoteTarget(寡黙(暫定村人または人狼)))
			 agreeする。そうでない場合は無視またはdisagree
			
			 requestEstimateがあった時　if(requestEstimate(target)が自分の考えと同じ) agree
			 requestEstimateしてきたエージェントが真寄りと見てる　agree
			 requestEstimateしてきたエージェントが狂人寄りと見てる　無視またはdisagree 自分がEstimate(requestしてきたエージェント)
			
			 requestがあったときはそのままaddTalkで追加しておk。ただし、10turnを超えるようなら最後は必ずVote発言をしておきたい
			*/
	
			if(!rollerFlag && !this.ownData.isVote()) {
				// 占いCOをしているエージェントのリストを作成
				List<Agent> seerList = forecastMap.getComingoutRoleAgentList(Role.SEER);

				if (seerList.size() == 3) {
					seerRoller = seerList; // ローラー用へコピー（元データをいじるのが何となく嫌だったから）

					if (coSeerMB.size() >= 1) { // 自分への黒出しエージェントがいる
						rollerAgent = coSeerMB.get(0);
					}else if (coSeerOB.size() >= 1) { // 他のエージェントへの黒出しエージェントがいる
						rollerAgent = coSeerOB.get(0);
					}else { // 黒出しエージェントがいない
						rollerAgent = RandomSelect.randomAgentSelect(seerRoller); // ランダムで選択したエージェントを格納
					}
					ownData.setVoteTarget(rollerAgent); // ランダムで選択したエージェントを投票
					rollerFlag = true;
					
				}else if (seerList.size() >= 1) { // １CO・2CO時
					// 占いCOしているエージェントのリストから一人をピックアップ
					seer = forecastMap.getComingoutRoleAliveAgent(seerList, Role.SEER);
					Judge judge = forecastMap.getDivineJudge(seer, ownData.getDay());
					
					// 黒出しだった場合
					if (Check.isNotNull(judge) && Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
						// ターゲットが自分だった場合
						if (Check.isAgent(judge.getTarget(), ownData.getMe())) {
							// 暫定人狼・確定狂人という形にしておきたい。ここは疑い度を変えるだけで確定にはしないかもしれない。
							forecastMap.setProvRole(seer, Role.WEREWOLF);
							forecastMap.setConfirmRole(seer, Role.POSSESSED);

							// リストへ追加
							coSeerMB.add(seer);

							// 投票は他に考慮することがなければ万が一の人狼騙りの場合に勝てる。ここは他の寡黙への投票リクエストが確認できれば変更
							ownData.setVoteTarget(seer);

						}else {
							// 一応一発で当てた占い師の可能性を考慮したいので、このようにした。
							forecastMap.setProvRole(seer, Role.SEER);
							forecastMap.setConfirmRole(seer, Role.POSSESSED);

							// リストへ追加
							coSeerOB.add(seer);

							// 占われたエージェントを吊って勝てればラッキー ２日目に入ればこのエージェントは狂人濃厚
							ownData.setVoteTarget(judge.getTarget());

							// 人狼COの準備 名前は仮でつけたので後で変更する
							ownData.setStrategy(Strategy.VILLAGER_WEREWOLF_CO);
						}
						rollerFlag = true;
						
					}else if (Check.isNotNull(judge) && Check.isSpecies(judge.getResult(), Species.HUMAN)) { // 白出しだった場合
						// このエージェントは暫定狂人となる
						forecastMap.setProvRole(seer, Role.POSSESSED);
						if (Check.isAgent(judge.getTarget(), (ownData.getMe()))) {
							// リストへ追加
							coSeerMW.add(seer);
						}else {
							// リストへ追加
							coSeerOW.add(seer);
						}

						// ランダムセレクトで寡黙（占いCOしてない）に投票するための準備
						List<Agent> coWhite = new ArrayList<>(); // 白出しエージェントまとめリスト
						for (int i = 0; i < coSeerMW.size(); i++) {
							coWhite.add(coSeerMW.get(i)); // 自分に白出しエージェントを追加
						}
						for (int i = 0; i < coSeerOW.size(); i++) {
							coWhite.add(coSeerOW.get(i)); // 他のエージェントに白出しエージェントを追加
						}

						// 生存者リストから白出しエージェントと自分を抜いて、ランダムに選択 それをvote対象に
						ownData.setVoteTarget(RandomSelect.randomAgentSelect(ownData.getAliveAgentList(),coWhite, ownData.getMe()));
						rollerFlag = true;
					}
					
				}else { // COがないとき
					// 1ターン目 発言
					if(turn.currentTurn(1)) {
						myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.SEER)); //　拡大解釈してFOと捉えてください。
					}
					// 3ターン目 発言
					if(turn.currentTurn(3)) {
						myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.VILLAGER));
					}
					// 4ターン目 発言
					if(turn.currentTurn(4)) {
						myTalking.addTalk(TalkFactory.requestAllEstimateRemark(rollerAgent, Role.POSSESSED)); // 他のエージェントにrollerAgentを狂人だと予想するようにリクエストする
					}
					// 5ターン目発言
					if(turn.currentTurn(5)) {
						myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget())); // 投票先に決定しているエージェントに投票する
					}
				}
			}
			// ローラー対象が決定していれば投票発言
			if(rollerFlag && !ownData.isVote()) {
				myTalking.addTalk(TalkFactory.requestAllEstimateRemark(ownData.getVoteTarget(), Role.POSSESSED)); // 他のエージェントにrollerAgentを狂人だと予想するようにリクエストする
				myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget())); // 投票先に決定しているエージェントに投票する
				ownData.setActFlagVote();
			}
		}
		
		// 2日目
		if(ownData.currentDay(2)){
			
		}
	}
	
	/**
	 * 人狼騙り行動 (現状2日目にしか呼ばれない)
	 */
	public void swindleWerewolf() {
		// ０ターン目発言 人狼CO
		if(turn.startTurn()) {
			myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.WEREWOLF));
		}
		// 1ターン目
		if(turn.currentTurn(1)) {
			Agent pos = forecastMap.getProvAgent(Role.POSSESSED);
			if(Check.isNotNull(pos)) {
				myTalking.addTalk(TalkFactory.requestAllComingoutRemark(pos, Role.POSSESSED));
			}
		}
		// 2ターン目以降 狂人CO 人狼CO待機
		if(turn.afterTurn(2) && ownData.isVote()) {
			// 今日COした狂人
			List<Agent> coPossList = forecastMap.comingoutRoleDayAgentList(Role.POSSESSED, ownData.getDay());
			// 狂COした人狼
			List<Agent> coWolfList = forecastMap.comingoutRoleDayAgentList(Role.WEREWOLF, ownData.getDay());
			// 自分以外
			List<Agent> aliveList = ownData.getAliveAgentList();
			aliveList.remove(ownData.getMe());
			// 狼COがいれば
			if(!coWolfList.isEmpty()) {
				aliveList.remove(coWolfList.get(0));
				// 残りエージェントに投票発言
				myTalking.addTalk(TalkFactory.voteRemark(aliveList.get(0)));
				// 実際は狼に投票
				ownData.setVoteTarget(coWolfList.get(0));
				ownData.setActFlagVote();
			}else if(!coPossList.isEmpty()){ //狂人COはいる
				aliveList.remove(coPossList.get(0));
				// 狂人エージェントに投票発言
				myTalking.addTalk(TalkFactory.voteRemark(coPossList.get(0)));
				// 実際は狼に投票
				ownData.setVoteTarget(aliveList.get(0));
				ownData.setActFlagVote();
			}		
		}
	}
}