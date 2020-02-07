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

import com.gmail.k14.itolab.aiwolf.action.GeneralAction;
import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 人狼の行動(5人)
 * @author k14096kk
 *
 */
public class WerewolfAction extends BaseRoleAction{
	
//	public MyTalking myWhisper;
	
	boolean isSilence;
	
	public WerewolfAction(EntityData entityData) {
		super(entityData);
//		myWhisper = entityData.getMyWhisper();
		isSilence = false;
		//ownData.rejectReaction();
		setEntityData();
	}
	
	/**
	 * 一日のはじめの行動(整理行動)
	 */
	@Override
	public void dayStart() {
		super.dayStart();
		this.setProveRole();
		setEntityData();
	}
	
	@Override
	public void attack() {
		super.attack();
		this.decideAttackTarget();
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		this.normalAction();
		setEntityData();
	}
	
	@Override
	public void talkAction(Talk talk, Content content) {
		super.talkAction(talk, content);
		// 占い師CO
		if(Check.isTopic(content.getTopic(), Topic.COMINGOUT)) {
			if(Check.isRole(content.getRole(), Role.SEER)) {
				// 自分なら暫定更新なし
				if(Check.isAgent(talk.getAgent(), ownData.getMe())) {
					return;
				}
				// 先なら狂人，後なら占い師
				if(!forecastMap.containProvRole(Role.POSSESSED)) {
					entityData.getForecastMap().setProvRole(talk.getAgent(), Role.POSSESSED);
				}else if(!forecastMap.containProvRole(Role.SEER)) {
					entityData.getForecastMap().setProvRole(talk.getAgent(), Role.SEER);
				}else {
					entityData.getForecastMap().setProvRole(talk.getAgent(), Role.VILLAGER);
				}
			}
		}
		
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
	/**
	 * 暫定役職を設定する
	 */
	public void setProveRole() {
		
		// 被襲撃者がCOしていなくて，暫定役職が決まっていないならば暫定村人
		if(Check.isNull(forecastMap.getComingoutRole(ownData.getAttackedAgent())) && Check.isNull(forecastMap.getProvRole(ownData.getAttackedAgent()))) {
			forecastMap.setProvRole(ownData.getAttackedAgent(), Role.VILLAGER);
		}
		
		if(ownData.isFinish()) {
			// 残りの暫定役職
			List<Agent> agentList = forecastMap.getProvRoleAgentList(null);
			forecastMap.setRemainRoleRandom(ownData.getRoleNumMap(), agentList);
		}
	}
	
	/**
	 * 人狼基本行動
	 */
	public void normalAction() {
		// 1日目
		if(ownData.currentDay(1)) {
			//0ターン目前
			if(turn.startTurn()) {
				myTalking.addTalk(TalkFactory.skipRemark());
			}
			// 3ターン目に行動を決める
			if(turn.currentTurn(3)) {
				// 1CO
				if(ownData.getCountCO(Role.SEER)<=1) {
					/* 寡黙 */
					isSilence = true;
				}else if(ownData.getCountCO(Role.SEER)>=2) { // 2CO以上
					// 占い師CO
					GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
					// 占い師COエージェントリスト
					List<Agent> coSeerMB = new ArrayList<>(); // 自分を黒判定した占い師COエージェントリスト
					List<Agent> coSeerMW = new ArrayList<>(); // 自分を白判定した占い師COエージェントリスト
					List<Agent> coSeerOB = new ArrayList<>(); // 他人を黒判定した占い師COエージェントリスト
					List<Agent> coSeerOW = new ArrayList<>(); // 他人を黒判定した占い師COエージェントリスト
					List<Judge> judgeList = forecastMap.getDivineJudgeDay(ownData.getDay());

					for(Judge judge: judgeList) {
						// 自分が占い対象
						if(judge.getTarget()==ownData.getMe()) {
							// 黒判定
							if(judge.getResult()==Species.WEREWOLF) {
								coSeerMB.add(judge.getAgent());
							}else { //白判定
								coSeerMW.add(judge.getAgent());
							}
						}else { // 自分以外が対象
							// 黒判定
							if(judge.getResult()==Species.WEREWOLF) {
								coSeerOB.add(judge.getAgent());
							}else { //白判定
								coSeerOW.add(judge.getAgent());
							}
						}
					}
					// 2人とも自分を黒判定
					if(coSeerMB.size()==2) {
						
					}else if(coSeerMB.size()==1 && coSeerOB.size()==1) { // 片方が自分に黒，もう片方が他人に黒
						// 自分に黒出し = 暫定&確定占い師
						forecastMap.setProvRole(coSeerMB.get(0), Role.SEER);
						forecastMap.setConfirmRole(coSeerMB.get(0), Role.SEER);
						// 他人に黒出し = 暫定&確定狂人
						forecastMap.setProvRole(coSeerOB.get(0), Role.POSSESSED);
						forecastMap.setConfirmRole(coSeerOB.get(0), Role.POSSESSED);
						
						// CO以外エージェントを占い結果は白
						List<Agent> divineList = ownData.getAliveOtherAgentList();
						divineList.removeAll(coSeerMB);
						divineList.remove(coSeerOB);
						if(!divineList.isEmpty()) {
							myTalking.addTalk(TalkFactory.divinedResultRemark(divineList.get(0), Species.HUMAN));
						}
						
						// 暫定占い師を狂人予想，暫定狂人を投票発言
						myTalking.addTalk(TalkFactory.estimateRemark(coSeerMB.get(0), Role.POSSESSED));
						myTalking.addTalk(TalkFactory.voteRemark(coSeerOB.get(0)));
						ownData.setVoteTarget(coSeerOB.get(0));
					}else if(!coSeerOW.isEmpty()) { // 他人に白出し 暫定占い師
						forecastMap.setProvRole(coSeerOW.get(0), Role.SEER);
						// CO以外エージェントを占い結果は白
						List<Agent> divineList = ownData.getAliveOtherAgentList();
						divineList.removeAll(coSeerMB);
						divineList.remove(coSeerOB);
						if(!divineList.isEmpty()) {
							myTalking.addTalk(TalkFactory.divinedResultRemark(divineList.get(0), Species.HUMAN));
						}
					}else if(!coSeerOB.isEmpty()) { // 他人に黒出し 暫定&確定狂人
						for(Agent seer: coSeerOB) {
							forecastMap.setProvRole(seer, Role.POSSESSED);
							forecastMap.setConfirmRole(seer, Role.POSSESSED);
						}
						// CO以外エージェントを占い結果は白
						List<Agent> divineList = ownData.getAliveOtherAgentList();
						divineList.removeAll(coSeerMB);
						divineList.remove(coSeerOB);
						if(!divineList.isEmpty()) {
							myTalking.addTalk(TalkFactory.divinedResultRemark(divineList.get(0), Species.HUMAN));
						}
					}else { // それ以外の条件
						// CO以外エージェントを占い結果は白
						List<Agent> divineList = ownData.getAliveOtherAgentList();
						divineList.removeAll(coSeerMB);
						divineList.remove(coSeerOB);
						if(!divineList.isEmpty()) {
							myTalking.addTalk(TalkFactory.divinedResultRemark(divineList.get(0), Species.HUMAN));
						}
					}
					// 適当に投票
					GeneralAction.sayVote(ownData, myTalking);
				}
			}
		
			// 4ターン目以降
			if(turn.afterTurn(4)) {
				// 寡黙状態(占い師CO1人以下)
				if(isSilence && !ownData.isVote()) {
					// 占い結果リスト
					List<Judge> judgeList = forecastMap.getDivineJudgeDay(ownData.getDay());
					if(!judgeList.isEmpty()) {
						// 自分以外が対象
						if(!Check.isAgent(judgeList.get(0).getTarget(), ownData.getMe())) {
							// 狼判定だったならば，その対象を投票対象
							if(Check.isSpecies(judgeList.get(0).getResult(), Species.WEREWOLF)) {
								ownData.setVoteTarget(judgeList.get(0).getTarget());
							}else {
								// 暫定狂人と自分以外を投票候補に
								List<Agent> voteList = ownData.getAliveOtherAgentList();
								voteList.removeAll(forecastMap.getProvRoleAgentList(Role.POSSESSED));
								if(!voteList.isEmpty()) {
									ownData.setVoteTarget(voteList.get(0));
								}
							}
						}else { // 自分が対象
							// 白判定だった
							if(Check.isSpecies(judgeList.get(0).getResult(), Species.HUMAN)) {
								// 確定狂人
								forecastMap.setConfirmRole(judgeList.get(0).getAgent(), Role.POSSESSED);
								// 確定狂人と自分以外を投票候補に
								List<Agent> voteList = ownData.getAliveOtherAgentList();
								voteList.removeAll(forecastMap.getConfirmRoleAgentList(Role.POSSESSED));
								if(!voteList.isEmpty()) {
									ownData.setVoteTarget(voteList.get(0));
								}
							}
						}
					}
					// 投票発言と投票リクエスト
					if(Check.isNotNull(ownData.getVoteTarget())){
						GeneralAction.sayVote(ownData, myTalking);
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
					}
				}
			}
		}
		
		/*2日目*/
		if(ownData.currentDay(2)) {
			// 寡黙中
			if(isSilence && !ownData.isVote()) {
				// 狂人予想
				myTalking.addTalk(TalkFactory.estimateRemark(forecastMap.getProvAgent(Role.POSSESSED), Role.POSSESSED));
				// 暫定占い師と狂人以外を対象
				List<Agent> vote = ownData.getAliveOtherAgentList();
				vote.removeAll(forecastMap.getProvAgentList(ownData.getAliveOtherAgentList(), Role.SEER));
				vote.removeAll(forecastMap.getProvAgentList(ownData.getAliveOtherAgentList(), Role.POSSESSED));
				if(!vote.isEmpty()) {
					Agent SilentVote = vote.get(0);
					ownData.setVoteTarget(SilentVote);
				}
				// 人狼予想と投票，投票リクエスト
				myTalking.addTalk(TalkFactory.estimateRemark(ownData.getVoteTarget(), Role.WEREWOLF));
				GeneralAction.sayVote(ownData, myTalking);
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
				
			}else {
				// 今日占い師COしたエージェントのリスト
				List<Agent> coSeerList = forecastMap.comingoutRoleDayAgentList(Role.SEER, ownData.getDay());
				if(!coSeerList.isEmpty()) {
					if(!ownData.isCO()) { //COしていないならば占い師CO
						myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
					}
					// 占い先リストはCOしているエージェント以外
					List<Agent> divineList = ownData.getAliveOtherAgentList();
					divineList.removeAll(coSeerList);
					if(!divineList.isEmpty()) {
						// COしていないエージェントを白判定
						myTalking.addTalk(TalkFactory.divinedResultRemark(divineList.get(0), Species.HUMAN));
						ownData.setActFlagDivine();
						myTalking.addTalk(TalkFactory.voteRemark(forecastMap.getProvAgent(Role.POSSESSED)));
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(coSeerList.get(0)));
						ownData.setActFlagVote();
						ownData.setVoteTarget(coSeerList.get(0));
					}else if(turn.currentTurn(0)){ //0ターン目
						// 1COならば
						if(ownData.getCountCO(Role.SEER)==1){
							myTalking.addTalk(TalkFactory.estimateRemark(forecastMap.getProvAgent(Role.POSSESSED), Role.POSSESSED));
							Agent voteTarget = forecastMap.getProvAgent(Role.VILLAGER);
							myTalking.addTalk(TalkFactory.voteRemark(voteTarget));
							myTalking.addTalk(TalkFactory.requestAllVoteRemark(voteTarget));
						}else if(ownData.getCountCO(Role.SEER)>=2){ // 2CO以上ならば
							myTalking.addTalk(TalkFactory.estimateRemark(forecastMap.getProvAgent(Role.SEER), Role.WEREWOLF));
							myTalking.addTalk(TalkFactory.voteRemark(forecastMap.getProvAgent(Role.SEER)));
							myTalking.addTalk(TalkFactory.requestAllVoteRemark(forecastMap.getProvAgent(Role.SEER)));
						}
					}
				}
			}
			
			// 寡黙中
			if(isSilence && !ownData.isVote()) {
				// 狂人予想
				myTalking.addTalk(TalkFactory.estimateRemark(forecastMap.getProvAgent(Role.POSSESSED), Role.POSSESSED));
				// 暫定占い師と狂人以外を対象
				List<Agent> vote = ownData.getAliveOtherAgentList();
				vote.removeAll(forecastMap.getProvAgentList(ownData.getAliveOtherAgentList(), Role.SEER));
				vote.removeAll(forecastMap.getProvAgentList(ownData.getAliveOtherAgentList(), Role.POSSESSED));
				if(!vote.isEmpty()) {
					Agent SilentVote = vote.get(0);
					ownData.setVoteTarget(SilentVote);
				}
				// 人狼予想と投票，投票リクエスト
				myTalking.addTalk(TalkFactory.estimateRemark(ownData.getVoteTarget(), Role.WEREWOLF));
				GeneralAction.sayVote(ownData, myTalking);
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));

			}else if(ownData.getBoardArrange().meet(1, 0, 0, 0, 0, 1)) { // (村，村，狼)
				// 2ターン目
				if(turn.currentTurn(2)) {
					Agent target = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
					myTalking.addTalk(TalkFactory.estimateRemark(target, Role.WEREWOLF));
					ownData.setVoteTarget(target);
					GeneralAction.sayVote(ownData, myTalking);
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(target));
				}
			}else if(ownData.getBoardArrange().meet(1, 1, 0, 0, 0, 1)) { // (村，占，狼)
				// リアクション許可
				ownData.permitReaction();
				// 1日目が1CO
				if(forecastMap.comingoutRoleDayAgentList(Role.SEER, 1).size() <= 1) {
					// リクエスト狂人CO
					if(turn.startTurn()) {
						myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.POSSESSED));
					}
					// SKIP
					if(turn.currentTurn(1)) {
						ownData.rejectReaction();
						myTalking.addTalk(TalkFactory.skipRemark());
					}
					if(turn.afterTurn(2) && !ownData.isVote()) {
						// 2日目に占い師COしたエージェントが存在する
						List<Agent> coSeerList = forecastMap.comingoutRoleDayAgentList(Role.SEER, ownData.getDay());
						coSeerList.remove(ownData.getMe());
						if(!coSeerList.isEmpty()) {
							if(!ownData.isCO()) { //COしていないならば占い師CO
								GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
							}
							// 占い師COしたエージェント以外を対象
							List<Agent> divineList = ownData.getAliveOtherAgentList();
							divineList.removeAll(coSeerList);

							if(!divineList.isEmpty()) {
								// COしていないエージェントを白判定
								myTalking.addTalk(TalkFactory.divinedResultRemark(divineList.get(0), Species.HUMAN));
								ownData.setActFlagDivine();
								// 占い師COしたエージェントに投票と投票リクエスト
								ownData.setVoteTarget(coSeerList.get(0));
								GeneralAction.sayVote(ownData, myTalking);
								myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
							}
						}
					}
				}else if(forecastMap.comingoutRoleDayAgentList(Role.SEER, 1).size() >= 2) { // CO2以上
					// 0ターン目
					if(turn.startTurn()) { 
						//COしていないならば占い師CO（念のため）
						if(!ownData.isCO()) {
							GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
						}
						// 暫定占い師の占い結果は黒
						Agent seer = forecastMap.getProvAgent(Role.SEER);
						myTalking.addTalk(TalkFactory.divinedResultRemark(seer, Species.WEREWOLF));
						ownData.setActFlagDivine();
						// 暫定占い師に投票と投票リクエスト
						ownData.setVoteTarget(seer);
						GeneralAction.sayVote(ownData, myTalking);
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
					}
				}else {
					// 狂人予想
					myTalking.addTalk(TalkFactory.estimateRemark(forecastMap.getProvAgent(Role.POSSESSED), Role.POSSESSED));
					// 暫定占い師と狂人以外を対象
					List<Agent> vote = ownData.getAliveOtherAgentList();
					vote.removeAll(forecastMap.getProvAgentList(ownData.getAliveOtherAgentList(), Role.SEER));
					vote.removeAll(forecastMap.getProvAgentList(ownData.getAliveOtherAgentList(), Role.POSSESSED));
					Agent SilentVote = vote.get(0);
					if(!vote.isEmpty()) {
						ownData.setVoteTarget(SilentVote);
					}
					// 人狼予想と投票，投票リクエスト
					myTalking.addTalk(TalkFactory.estimateRemark(ownData.getVoteTarget(), Role.WEREWOLF));
					GeneralAction.sayVote(ownData, myTalking);
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
				}
				
			}else if(ownData.getBoardArrange().meet(1, 0, 0, 0, 1, 1)) {  // (村，狂，狼)
				// 0ターン目
				if(turn.startTurn()) {
					// リクエスト狂人CO
					if(!ownData.isCO()) {
						myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.POSSESSED));
					}
				}
				// 2ターン目以降
				if(turn.afterTurn(2) && !ownData.isVote()) {
					/*
					// 占い師COリストから1人抽出(おそらく暫定狂人のはず)
					Agent seer = forecastMap.getComingoutRoleAliveAgent(ownData.getAliveOtherAgentList(), Role.SEER);
					if(Check.isNotNull(seer)) {
						// 暫定狂人に結果は黒
						myTalking.addTalk(TalkFactory.divinedResultRemark(seer, Species.WEREWOLF));
						// 暫定狂人に投票発言
						myTalking.addTalk(TalkFactory.voteRemark(seer));
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(seer));
						 // 実際はCOしていない方(村人)を投票
						List<Agent> voteList = ownData.getAliveOtherAgentList();
						voteList.remove(seer);
						ownData.setVoteTarget(voteList.get(0));
					}
					*/
					// 狂人COがあった
					if(!forecastMap.comingoutRoleDayAgentList(Role.POSSESSED, ownData.getDay()).isEmpty() && !ownData.isVote()) {
						// 人狼CO
						GeneralAction.sayComingout(ownData, myTalking, Role.WEREWOLF);
						// 狂人COしていないエージェントの方を抽出
						List<Agent> voteList = ownData.getAliveOtherAgentList();
						voteList.remove(forecastMap.comingoutRoleDayAgentList(Role.POSSESSED, ownData.getDay()).get(0));
						if(!voteList.isEmpty()) {
							// 投票と投票リクエスト
							ownData.setVoteTarget(voteList.get(0));
							GeneralAction.sayVote(ownData, myTalking);
							myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
						}
					}
				}
			}else if(ownData.getBoardArrange().meet(0, 1, 0, 0, 1, 1)) { // (占，狂，狼)
				// 0ターン目
				if(turn.startTurn()) {
					// リクエスト狂人CO
					if(!ownData.isCO()) {
						myTalking.addTalk(TalkFactory.requestAllComingoutRemark(ownData.getMe(), Role.POSSESSED));
					}
				}
				// 2ターン目以降
				if(turn.afterTurn(2) && !ownData.isVote()) {
					/*
					// 占い師COリストから1人抽出(おそらく暫定狂人のはず)
					Agent seer = forecastMap.getComingoutRoleAliveAgent(ownData.getAliveOtherAgentList(), Role.SEER);
					if(Check.isNotNull(seer)) {
						// 暫定狂人に結果は黒
						myTalking.addTalk(TalkFactory.divinedResultRemark(seer, Species.WEREWOLF));
						// 暫定狂人に投票発言
						myTalking.addTalk(TalkFactory.voteRemark(seer));
						myTalking.addTalk(TalkFactory.requestAllVoteRemark(seer));
						 // 実際はCOしていない方(村人)を投票
						List<Agent> voteList = ownData.getAliveOtherAgentList();
						voteList.remove(seer);
						ownData.setVoteTarget(voteList.get(0));
					}
					*/
					// 狂人COがあった
					if(!forecastMap.comingoutRoleDayAgentList(Role.POSSESSED, ownData.getDay()).isEmpty() && !ownData.isVote()) {
						// 人狼CO
						GeneralAction.sayComingout(ownData, myTalking, Role.WEREWOLF);
						// 狂人COしていないエージェントの方を抽出
						List<Agent> voteList = ownData.getAliveOtherAgentList();
						voteList.remove(forecastMap.comingoutRoleDayAgentList(Role.POSSESSED, ownData.getDay()).get(0));
						if(!voteList.isEmpty()) {
							// 投票と投票リクエスト
							ownData.setVoteTarget(voteList.get(0));
							GeneralAction.sayVote(ownData, myTalking);
							myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
						}
					}
				}
				/*//CO1
				if(forecastMap.comingoutRoleDayAgentList(Role.SEER, 1).size() <= 1) {
					if(turn.startTurn()) {
						if(!ownData.isCO()) {
							myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
							ownData.setActFlagCO();
						}
					}
					if(turn.afterTurn(1) && !ownData.isVote()) {
						// 占い師COしたエージェントを1人抽出
						Agent seer = forecastMap.getComingoutRoleAliveAgent(ownData.getAliveOtherAgentList(), Role.SEER);
						if(seer!=null) {
							// 結果は
							myTalking.addTalk(TalkFactory.divinedResultRemark(seer, Species.WEREWOLF));
							myTalking.addTalk(TalkFactory.voteRemark(seer));
							List<Agent> voteList = ownData.getAliveOtherAgentList();
							voteList.remove(seer);
							ownData.setVoteTarget(voteList.get(0)); // COしていない方(村人)を投票
						}
					}
				}else if(forecastMap.comingoutRoleDayAgentList(Role.SEER, 1).size() >= 1) {
					if(turn.startTurn()) {
						if(!ownData.isCO()) {
							myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
							ownData.setActFlagCO();
						}
						
						Agent poss = forecastMap.getProvAgent(Role.POSSESSED);
						if(poss != null) {
							myTalking.addTalk(TalkFactory.divinedResultRemark(poss, Species.WEREWOLF));
							myTalking.addTalk(TalkFactory.voteRemark(poss));
							List<Agent> voteList = ownData.getAliveOtherAgentList();
							voteList.remove(poss);
							ownData.setVoteTarget(voteList.get(0)); // 別のとこ(村人)を投票
						}
					}
				}else {
					
				}
				*/
			}
		}

		// 7ターン目
		if(turn.currentTurn(7)) {
			// まだ投票発言していないならば発言
			if(!ownData.isVote()) {
				GeneralAction.sayVote(ownData, myTalking);
				myTalking.addTalk(TalkFactory.requestAllVoteRemark(ownData.getVoteTarget()));
			}
			ownData.setActFlagFinish();
		}

		if (ownData.isFinish()) {
			myTalking.addTalk(TalkFactory.overRemark());
		}
		
	}
	
	@Override
	public void setEntityData() {
		super.setEntityData();
//		entityData.setMyWhisper(myWhisper);
	}
	
	/**
	 * 襲撃先を決める処理
	 */
	public void decideAttackTarget() {
		
		// 暫定占い師がいればそいつを噛む
		List<Agent> seerList = forecastMap.getProvRoleAgentList(Role.SEER);
		seerList.remove(ownData.getExecutedAgent());
		if(!seerList.isEmpty()) {
			ownData.setAttackTarget(seerList.get(0));
			return;
		}
		
		// COエージェント以外を噛む
		List<Agent> targetList = ownData.getAliveOtherAgentList();
		targetList.removeAll(forecastMap.getComingoutAgentList());
		seerList.remove(ownData.getExecutedAgent());
		if(!targetList.isEmpty()) {
			ownData.setAttackTarget(targetList.get(0));
			return;
		}
		
		// 確定狂人がいれば，それ以外を噛む
		List<Agent> possList = forecastMap.getConfirmRoleAgentList(Role.POSSESSED);
		seerList.remove(ownData.getExecutedAgent());
		if(!possList.isEmpty()) {
			List<Agent> list = ownData.getAliveOtherAgentList();
			list.removeAll(possList);
			if(!list.isEmpty()) {
				ownData.setAttackTarget(list.get(0));
				return;
			}
		}
		
		// 上記以外ならばランダム
		Agent target = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
		ownData.setAttackTarget(target);
 	}
	
}
