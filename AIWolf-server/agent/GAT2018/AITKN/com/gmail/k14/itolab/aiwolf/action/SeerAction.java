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
 * 占い師の行動
 * @author k14096kk
 *
 */
public class SeerAction extends BaseRoleAction{

	/**key=agent,val=占って欲しい発言数*/
	Map<Agent,Integer> divinationMap = new HashMap<Agent,Integer>();

	/**自分が占ったエージェントのリスト*/
	List<Agent> divinedList = new ArrayList<>();
	/**占い師が占ったエージェントのリスト*/
	List<Agent> allDivineList = new ArrayList<>();
	/**占い結果黒や偽物を格納する*/
	List<Agent> blackList = new ArrayList<>();
	/**占い結果白を格納する*/
	List<Agent> whiteList = new ArrayList<>();
	/**占い師のCOリスト*/
	List<Agent> seerCOList = new ArrayList<>();
	/**霊媒師のCOリスト*/
	List<Agent> mediumCOList = new ArrayList<>();

	/**投票対象にするエージェント*/
	Agent voteAgent = null;
	/**占い対象にするエージェント*/
	Agent divineAgent = null;
	/**霊媒師だと思うエージェント*/
	Agent medium = null;
	/**狩人COしたエージェント*/
	Agent bodyguard = null;
	/**狂人COしたエージェント*/
	Agent possessed = null;
	/**人狼COしたエージェント*/
	Agent werewolf = null;
	/**5人人狼で白を引いた時用*/
	Agent fiveDivine = null;

	/**5人人狼かどうか*/
	boolean fiveJinro = false;
	/**1日の特定行動制御*/
	boolean oneAction = false;

	/**人狼数を格納しておくための変数*/
	int jinroCount = ownData.getSettingRoleNum(Role.WEREWOLF);
	/**占い師・霊媒師のCOカウント*/
	int foCount = 0;

	
	/**
	 * 占い師行動のコンストラクタ
	 * @param entityData :オブジェクトデータ
	 */
	public SeerAction(EntityData entityData) {
		super(entityData);

		ownData.rejectReaction();

		setEntityData();
	}

	
	/**
	 * 1日のはじめ
	 */
	@Override
	public void dayStart() {
		super.dayStart();

		oneAction = false;

		//１日目の生存人数が５人以下
		if (ownData.getDay() == 1 && ownData.getAliveOtherAgentList().size() < 6) {
			fiveJinro = true;
		}

		// 占い結果が存在すれば保持する
		if (ownData.getLatestDivineResult() != null) {
			ownData.setDivineResultMap(ownData.getGameInfo().getDivineResult());
			HandyGadget.addList(divinedList, ownData.getDivineTarget());
			//占い結果が白
			if (ownData.getDivineResultJudge(ownData.getDay()).getResult() == Species.HUMAN) {
				//占い対象が占いCOしている場合、狂人
				if (seerCOList.contains(ownData.getDivineResultJudge(ownData.getDay()).getTarget())) {
					possessed = ownData.getDivineResultJudge(ownData.getDay()).getTarget();
				}
				//占い対象がCOしていない場合、白
				else {
					HandyGadget.addList(whiteList, ownData.getDivineResultJudge(ownData.getDay()).getTarget());
				}
			}
			//占い結果が黒
			else {
				HandyGadget.addList(blackList, ownData.getDivineResultJudge(ownData.getDay()).getTarget());
				voteAgent = ownData.getDivineResultJudge(ownData.getDay()).getTarget();
			}
		}

		//狩人COがあり、まだ生きている場合
		if(bodyguard != null && ownData.getAliveOtherAgentList().contains(bodyguard)) {
			HandyGadget.addList(blackList, bodyguard);
		}

		//生存チェック
		AgentAliveCheck();

		setEntityData();
	}
	
	
	/**
	 * 投票時に呼ぶ処理
	 */
	@Override
	public void vote() {
		super.vote();

		//死亡者リストに投票対象が含まれている
		if (ownData.getDeadAgentList().contains(voteAgent)) {
			voteAgent = null;
		}

		//できれば狂人に投票したくない
		if (voteAgent == possessed) {
			voteAgent = null;
		}

		if (voteAgent == null) {
			//blackListの最多
			if (!blackList.isEmpty()) {
				voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), blackList);
			}
			//whiteListを除く生存エージェントの最多
			else {
				List<Agent> tmp = ownData.getAliveOtherAgentList();
				tmp.removeAll(whiteList);
				voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), tmp);
			}
			//生存者からランダム
			if (voteAgent == null) {
				voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList());
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

		this.nomalSeerAciton();

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

					HandyGadget.addList(blackList, talk.getAgent());
					
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
						ownData.setVoteTarget(voteAgent);
						GeneralAction.sayVote(ownData, myTalking);
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
					blackList.add(talk.getAgent());
				}
				else if (content.getRole() == Role.WEREWOLF) {
					blackList.add(talk.getAgent());
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
			}

			if (content.getTopic() == Topic.IDENTIFIED) {
				
				//霊媒COをしていない場合に対応
				if(!entryMediumCoList.contains(talk.getAgent())) {
					HandyGadget.addList(mediumCOList, talk.getAgent());
				}
				HandyGadget.addList(entryMediumCoList, talk.getAgent());
				
				//自分の占ったリストの中に対象がいる
				if (divinedList.contains(content.getTarget())) {
					//自分の占い結果と異なる霊媒結果
					if (ownData.getDivineResultJudge(content.getTarget()).getResult() != content.getResult()) {
						HandyGadget.addList(blackList, talk.getAgent());
						whiteList.remove(talk.getAgent());
						myTalking.addTalk(TalkFactory.estimateRemark(talk.getAgent(), Role.WEREWOLF));
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
	 * 占い時に呼ぶ処理
	 */
	@Override
	public void divine() {
		super.divine();

		divinationAction();

		//占い対象が空
		if(divineAgent == null || ownData.getDeadAgentList().contains(divineAgent)) {
			//生きているエージェントから占っていないエージェント
			divineAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), divinedList);
		}

		ownData.setDivineTarget(divineAgent);

		setEntityData();
	}

	
	/**
	 * 基本行動
	 */
	public void nomalSeerAciton() {
		//リストの重複チェック
		Set<Agent> set = new HashSet<>(blackList);
		blackList = new ArrayList<>(set);

		set = new HashSet<>(whiteList);
		whiteList = new ArrayList<>(set);

		if (!oneAction) {
			//5人人狼
			if (fiveJinro) {
				//2日目
				if (ownData.getDay() == 2) {
					//COしていない
					if (!ownData.isCO()) {
						GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
					}
					//１日目の結果報告
					myTalking.addTalk(TalkFactory.divinedResultRemark(fiveDivine, Species.HUMAN));
					//２日目の結果報告
					GeneralAction.sayDivine(ownData, myTalking, ownData.getPreviousDivineTarget(), ownData.getDivineResultSpecies(ownData.getPreviousDivineTarget()));
					//結果が黒
					if (ownData.getDivineResultSpecies(ownData.getPreviousDivineTarget()) == Species.WEREWOLF) {
						//そのまま投票対象に
						voteAgent = ownData.getPreviousDivineTarget();
					}
					//結果が白
					else {
						//占い対象と自分をのぞいたリストからランダム（実質１人）
						List<Agent> tmp = ownData.getAliveOtherAgentList();
						tmp.remove(ownData.getPreviousDivineTarget());
						voteAgent = RandomSelect.randomAgentSelect(tmp);
					}
					ownData.setVoteTarget(voteAgent);
					GeneralAction.sayVote(ownData, myTalking);
				}
				else if (ownData.getDivineResultJudge(ownData.getDay()).getResult()==Species.HUMAN){
					voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), whiteList);
					voteAction();
					fiveDivine = ownData.getPreviousDivineTarget();
					myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
				}
				else {
					GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
					//占い結果報告
					GeneralAction.sayDivine(ownData, myTalking, ownData.getPreviousDivineTarget(), ownData.getDivineResultSpecies(ownData.getPreviousDivineTarget()));
					voteAgent = ownData.getPreviousDivineTarget();
					ownData.setVoteTarget(voteAgent);
					GeneralAction.sayVote(ownData, myTalking);
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(voteAgent));
				}
			}
			//15人人狼
			else {
				if (!ownData.isCO()) {
					GeneralAction.sayComingout(ownData, myTalking, Role.SEER);
				}
				//占い結果報告
				GeneralAction.sayDivine(ownData, myTalking, ownData.getPreviousDivineTarget(), ownData.getDivineResultSpecies(ownData.getPreviousDivineTarget()));

				//占い結果が白
				if (ownData.getDivineResultJudge(ownData.getDay()).getResult()==Species.HUMAN){
					//占い対象が占い師COしている場合
					if (seerCOList.contains(ownData.getPreviousDivineTarget())) {
						//狂人予想
						myTalking.addTalk(TalkFactory.estimateRemark(ownData.getPreviousDivineTarget(), Role.POSSESSED));
					}
					voteAgent = RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList(), whiteList);
					voteAction();
					myTalking.addTalk(TalkFactory.voteRemark(voteAgent));
				}

				//占い結果が黒
				else {
					voteAgent = ownData.getPreviousDivineTarget();
					ownData.setVoteTarget(voteAgent);
					GeneralAction.sayVote(ownData, myTalking);
					myTalking.addTalk(TalkFactory.requestAllVoteRemark(voteAgent));
				}

				if (ownData.isCO()) {
					//占い先発言
					divinationAction();
					myTalking.addTalk(TalkFactory.divinationRemark(divineAgent));
				}
			}
			oneAction = true;
		}
		if (!fiveJinro) {
			if (ownData.getDay() < 3) {
				if (ownData.getDivineResultJudge(ownData.getDay()).getResult()==Species.HUMAN){
					voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), ownData.getAliveOtherAgentList());
				}
			}
		}
		else {
			//3COになっている
			if (foCount > 1) {
				//占い結果が白の場合
				if (ownData.getDivineResultSpecies(ownData.getPreviousDivineTarget()) == Species.HUMAN) {
					//占った対象がCOしている
					if (seerCOList.contains(ownData.getPreviousDivineTarget())) {
						//狂人なので、投票しない
						List<Agent> tmp = seerCOList;
						tmp.remove(ownData.getPreviousDivineTarget());
						tmp.remove(ownData.getMe());
						voteAgent = RandomSelect.randomAgentSelect(tmp);
					}
					else {
						//COしているエージェントからランダムに
						voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), seerCOList);
					}
				}
			}
		}
	}

	
	/**
	 * 投票先決定
	 */
	public void voteAction() {
		//blackListが空でない
		if (!blackList.isEmpty()) {
			//blackListの最多
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), blackList);
		}
		if (voteAgent == null) {
			List<Agent> tmp = ownData.getAliveOtherAgentList();
			tmp.removeAll(whiteList);
			//whiteListを除く生存エージェントから最多
			voteAgent = voteCounter.maxCountAgent(voteCounter.getAllMap(), tmp);
		}
	}


	/**
	 * 占い先エージェント発言
	 */
	public void divinationAction() {
		if (ownData.isCO()) {
			int tmp = 0;
			Agent age = null;
			List<Agent> foList = new ArrayList<>();

			//黒発見数が１ かつ ４人以上CO
			if (ownData.getDivineResultList(Species.WEREWOLF).size() == 1 && foCount > 3) {
				foList.addAll(seerCOList);
				foList.addAll(mediumCOList);
				divineAgent = RandomSelect.randomAgentSelect(blackList,ownData.getDeadAgentList(),divinedList);
			}
			//黒発見数が２ または CO数が４より多い
			else if (ownData.getDivineResultList(Species.WEREWOLF).size() == 2 || foCount > 4) {
				foList.addAll(seerCOList);
				foList.addAll(mediumCOList);
				foList.removeAll(ownData.getDeadAgentList());
				if (Check.isNotNull(foList)) {
					divineAgent = RandomSelect.randomAgentSelect(foList,divinedList);
				}
				else if (Check.isNotNull(blackList)) {
					divineAgent = RandomSelect.randomAgentSelect(blackList,ownData.getDeadAgentList(),divinedList);
				}
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
				//占い対象が自分
				if (divineAgent == ownData.getMe()) {
					divineAgent = null;
				}
				//占い対象が既に占い済み
				if (divinedList.contains(divineAgent)) {
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

