package com.gmail.k14.itolab.aiwolf.data;
 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.util.AbilityResultInfo;
import com.gmail.k14.itolab.aiwolf.definition.Strategy;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.HandyGadget;


/**
 * フラグなどのデータをまとめたクラス
 * @author k14096kk
 *
 */
public class OwnData {
	
	/**ゲーム情報*/
	GameInfo gameInfo;
	/**ゲーム設定*/
	GameSetting gameSetting;
	/**自分*/
	Agent me;
	/**自分の役職*/
	Role myRole;
	/**
	 * ビットフラグ<br>
	 * 16:発話終了<br>
	 * 8:霊能結果発言したかどうか<br>
	 * 4:占い結果発言したかどうか<br>
	 * 2:投票発言したかどうか<br>
	 * 1:COしたかどうか<br>
	 */
	int actFlag;
	/**自分のCO役職*/
	Role coRole;
	/**投票対象*/
	Agent voteTarget;
	/**昨日の投票対象*/
	Agent previousVoteTarget;
	/**自分の占い対象*/
	Agent divineTarget;
	/**昨日の自分の占い対象*/
	Agent previousDivineTarget;
	/**占い結果マップ<br>key:占い対象エージェント<br>value:占い結果(Judge)*/
	Map<Agent, Judge> divineResultMap;
	/**自分の霊媒対象*/
	Agent identTarget;
	/**霊媒結果マップ<br>key:霊媒対象エージェント<br>value:霊媒結果(Judge)*/
	Map<Agent, Judge> identResultMap;
	/**自分の護衛対象*/
	Agent guardTarget;
	/**昨日の自分の護衛対象*/
	Agent previousGuardTarget;
	/**護衛結果のマップ<br>key:日付<br>value:護衛結果情報(AbilityResultInfo)*/
	Map<Integer, AbilityResultInfo> guardResultMap;
	/**吊り手*/
	int countHang;
	/**各役職CO*/
	int[] countCO;
	/**戦略*/
	Strategy strategy;
	/**盤面整理*/
	BoardArrange boardArrange;
	/**共通行動許可フラグ*/
	int isReaction;
	/**襲撃対象エージェント*/
	Agent attackTarget;
	/**前日の襲撃対象*/
	Agent previousAttackAgent;
	/**襲撃結果のマップ<br>key:日付<br>value:襲撃結果情報(AbilityResultInfo)*/
	Map<Integer, AbilityResultInfo> attackResultMap;
	
	
	/**
	 * フラグやデータの情報まとめ
	 * @param gameInfo :ゲーム情報
	 * @param gameSetting :ゲーム設定
	 */
	public OwnData(GameInfo gameInfo, GameSetting gameSetting) {
		// ゲーム情報
		this.gameInfo = gameInfo;
		this.gameSetting = gameSetting;
		this.me = gameInfo.getAgent();
		this.myRole = gameInfo.getRoleMap().get(this.me);
		
		
		// データ初期化
		this.actFlag = 0;
		this.coRole = null;
		this.voteTarget = null;
		this.previousVoteTarget = null;
		this.divineTarget = null;
		this.previousDivineTarget = null;
		this.divineResultMap = new LinkedHashMap<>();
		this.identTarget = null;
		this.identResultMap = new LinkedHashMap<>();
		this.guardTarget = null;
		this.previousGuardTarget = null;
		this.guardResultMap = new LinkedHashMap<>();
		this.countHang = 0;
		this.countCO = new int[6];
		this.strategy = Strategy.NONE;
		this.boardArrange = new BoardArrange(gameSetting);
		this.isReaction = 0;
		this.attackTarget = null;
		this.previousAttackAgent = null;
		this.attackResultMap = new LinkedHashMap<>();
	}
	
	/**
	 * ゲーム情報の更新
	 * @param gameInfo :ゲーム情報
	 */
	public void setDataUpdate(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
	}
	
	/**
	 * 1日のはじめに呼ばれる更新<br>
	 * 生存エージェントリスト，生存人数，吊り手
	 */
	public void updateDayStart() {
		this.getCountAliveAgentList();
		this.updateCountHang();
	}
	
	/**
	 * データの初期化
	 */
	public void dataReset() {
		this.voteTarget = null;
		this.divineTarget = null;
		this.identTarget = null;
		this.guardTarget = null;
		//this.reverseActFlagCO();
		this.reverseActFlagVote();
		this.reverseActFlagDivine();
		this.reverseActFlagIdent();
		this.reverseActFlagFinish();
	}
	
	/*-----------------------------ゲーム情報---------------------------------------*/
	
	/**
	 * ゲーム情報の取得
	 * @return ゲーム情報
	 */
	public GameInfo getGameInfo() {
		return this.gameInfo;
	}
	
	/**
	 * ゲームの日付取得
	 * @return 日付
	 */
	public int getDay() {
		return this.getGameInfo().getDay();
	}
	
	/**
	 * 指定する日付かどうか
	 * @param day :指定する日付
	 * @return 指定する日付ならばtrue,異なればfalse
	 */
	public boolean currentDay(int day) {
		if(this.getDay() == day) {
			return true;
		}
		return false;
	}
	
	/**
	 * 指定日付以前かどうか(指定日付を含む)
	 * @param day :日付
	 * @return 以前ならばtrue,なければfalse
	 */
	public boolean beforeDay(int day) {
		if(this.getDay() <= day) {
			return true;
		}
		return false;
	}
	
	/**
	 * 指定日付以降かどうか(指定日付を含む)
	 * @param day :日付
	 * @return 以降ならばtrue,なければfalse
	 */
	public boolean afterDay(int day) {
		if(this.getDay() >= day) {
			return true;
		}
		return false;
	}
	
	/**
	 * 引数間の日数かどうか(引数の日付は含まない)
	 * @param before :何日から
	 * @param after :何日まで
	 * @return 間にあればtrue,なければfalse
	 */
	public boolean whileDay(int before, int after) {
		if(this.getDay() > before && this.getDay() < after) {
			return true;
		}
		return false;
	}
	
	/**
	 * 全エージェントリスト取得
	 * @return 全エージェントリスト
	 */
	public List<Agent> getAgentList() {
		return this.getGameInfo().getAgentList();
	}
	
	/**
	 * 生存エージェントリスト取得
	 * @return 生存エージェントリスト
	 */
	public List<Agent> getAliveAgentList() {
		return this.getGameInfo().getAliveAgentList();
	}
	
	/**
	 * 自分を除いた生存エージェントリスト取得
	 * @return　生存エージェントリスト(自分以外)
	 */
	public List<Agent> getAliveOtherAgentList() {
		List<Agent> aliveList = this.getAliveAgentList();
		aliveList.remove(this.getMe());
		return aliveList;
	}
	
	/**
	 * 死亡しているエージェントのリスト取得
	 * @return 死亡エージェントリスト
	 */
	public List<Agent> getDeadAgentList() {
		List<Agent> deadAgents = this.getGameInfo().getAgentList();
		deadAgents.removeAll(this.getAliveAgentList());
		return deadAgents;
	}
	
	/**
	 * 会話リスト取得
	 * @return 会話リスト
	 */
	public List<Talk> getTalkList() {
		return this.getGameInfo().getTalkList();
	}
	
	/**
	 * 囁きリスト取得
	 * @return 囁きリスト
	 */
	public List<Talk> getWhisperList() {
		return this.getGameInfo().getWhisperList();
	}
	
	/**
	 * 直近の投票一覧を取得する
	 *  @return　投票リスト
	 */
	public List<Vote> getLatestVoteList() {
		return this.getGameInfo().getLatestVoteList();
	}
	
	/**
	 * 前回死亡したエージェントの一覧を取得する
	 *  @return 死亡者リスト
	 */
	public List<Agent> getLastDeadAgentList() {
		return this.getGameInfo().getLastDeadAgentList();
	}
	
	/**
	 * 直近の襲撃投票一覧を取得する
	 * @return 襲撃投票リスト
	 */
	public List<Vote> getLatestAttackVoteList() {
		return this.getGameInfo().getLatestAttackVoteList();
	}
	
	/**
	 * ゲーム情報から最新の占い結果を取得する
	 * @return 最新の占い結果(前日に取得した占い結果)
	 */
	public Judge getLatestDivineResult() {
		if(Check.isNull(this.getGameInfo().getDivineResult())) {
			return null;
		}
		return this.getGameInfo().getDivineResult();
	}
	
	/**
	 * ゲーム情報から最新の霊媒結果を取得する
	 * @return 最新の霊媒結果(最新の被処刑者の霊媒結果)
	 */
	public Judge getLatestMediumResult() {
		if(Check.isNull(this.getGameInfo().getMediumResult())) {
			return null;
		}
		return this.getGameInfo().getMediumResult();
	}
	
	/*-----------------------------ゲーム設定---------------------------------------*/
	
	/**
	 * ゲーム設定の取得
	 * @return ゲーム設定
	 */
	public GameSetting getGameSetting() {
		return this.gameSetting;
	}

	/**
	 * ゲームの役職の人数一覧を取得
	 * @return 人数一覧のマップ
	 */
	public Map<Role, Integer> getRoleNumMap() {
		return gameSetting.getRoleNumMap();
	}
	
	/**
	 * ゲーム上の指定役職の人数を取得
	 * @param role :指定役職
	 * @return 役職人数
	 */
	public int getSettingRoleNum(Role role) {
		return this.getRoleNumMap().get(role);
	}
	
	/*-----------------------------エージェント---------------------------------------*/
	
	/**
	 * 自分を取得
	 * @return 自分エージェント
	 */
	public Agent getMe() {
		return me;
	}
	
	/**
	 * 自分の役職を取得
	 * @return 役職
	 */
	public Role getMyRole() {
		return myRole;
	}
	
	/**
	 * 生存エージェントの人数を取得
	 * @return 人数
	 */
	public int getCountAliveAgentList() {
		return this.getAliveAgentList().size();
	}

	/**
	 * 前日の被襲撃者の取得
	 * @return 被襲撃者がいれば該当エージェント，いなければnullを返す
	 */
	public Agent getAttackedAgent() {
		if(!this.getGameInfo().getLastDeadAgentList().isEmpty()) {
			List<Agent> remainList = this.getGameInfo().getLastDeadAgentList();
			remainList.remove(this.getExecutedAgent());
			if(!remainList.isEmpty()) {
				return remainList.get(0);
			}
//			return this.getGameInfo().getLastDeadAgentList().get(0);
				
		}
		return null;
	}
	
	/**
	 * 前日の被処刑者の取得
	 * @return 被処刑者がいれば該当エージェント，いなければnullを返す
	 */
	public Agent getExecutedAgent() {
		return this.getGameInfo().getExecutedAgent();
	}
	
	/**
	 * 当日の被処刑者の取得
	 * @return 被処刑者がいれば該当エージェント，いなければnullを返す
	 */
	public Agent getLatestExecutedAgent() {
		return this.getGameInfo().getLatestExecutedAgent();
	}
	
	/*-----------------------------行動フラグ---------------------------------------*/
	
	/**
	 * 現在の行動フラグ取得
	 * @return 行動フラグ(ビット)
	 */
	public int getActFlag() {
		return this.actFlag;
	}

	/**
	 * 行動フラグを更新
	 * @param flag :更新したいビット
	 */
	public void setActFlag(int flag) {
		actFlag = actFlag | flag;
	}
	
	/**
	 * 行動フラグの比較
	 * @param flag :指定ビット
	 * @return 指定ビットがあればtrue,なければfalse
	 */
	public boolean compareActFlag(int flag) {
		if(actFlag!=flag) {
			return true;
		}
		return false;
	}
	
	/**
	 * CO済みかどうか
	 * @return CO済みならばtrue,COしていないならばfalse
	 */
	public boolean isCO() {
		int bit = actFlag & 1;
		if(bit==1) {
			return true;
		}
		return false;
	}
	
	/**
	 * CO済みに切り替え(行動フラグ変更)
	 */
	public void setActFlagCO() {
		actFlag = actFlag | 1;
	}
	
	/**
	 * COフラグを0に切り替え(行動フラグ変更)
	 */
	public void reverseActFlagCO() {
		actFlag = actFlag ^ 1;
	}
	
	/**
	 * 投票先を発言したかどうか
	 * @return 投票先を発言済みならばtrue,していないならばfalse
	 */
	public boolean isVote() {
		int bit = actFlag & 2;
		if(bit==2) {
			return true;
		}
		return false;
	}
	
	/**
	 * 投票先を発言済みに切り替え(行動フラグ変更)
	 */
	public void setActFlagVote() {
		actFlag = actFlag | 2;
	}
	
	/**
	 * 投票先発言フラグを0に切り替え
	 */
	public void reverseActFlagVote() {
		int bit = actFlag & 2;
		if(bit==2) {
			actFlag = actFlag ^ 2;
		}
	}
	
	/**
	 * 占い結果を発言したかどうか
	 * @return 占い結果を発言済みならばtrue,していないならばfalse
	 */
	public boolean isDivine() {
		int bit = actFlag & 4;
		if(bit==4) {
			return true;
		}
		return false;
	}
	
	/**
	 * 占い結果を話し済みに切り替え(行動フラグ変更)
	 */
	public void setActFlagDivine() {
		actFlag = actFlag | 4;
	}
	
	/**
	 * 占い結果発言フラグを0に切り替え
	 */
	public void reverseActFlagDivine() {
		int bit = actFlag & 4;
		if(bit==4) {
			actFlag = actFlag ^ 4;
		}
	}
	
	/**
	 * 霊能結果を発言したかどうか
	 * @return 占い結果を発言済みならばtrue,していないならばfalse
	 */
	public boolean isIdent() {
		int bit = actFlag & 8;
		if(bit==8) {
			return true;
		}
		return false;
	}
	
	/**
	 * 霊能結果を話し済みに切り替え(行動フラグ変更)
	 */
	public void setActFlagIdent() {
		actFlag = actFlag | 8;
	}
	
	/**
	 * 霊能結果発言フラグを0に切り替え
	 */
	public void reverseActFlagIdent() {
		int bit = actFlag & 8;
		if(bit==8) {
			actFlag = actFlag ^ 8;
		}
	}
	
	/**
	 * 発話終了状態かどうか
	 * @return 発話終了状態ならばtrue,していないならばfalse
	 */
	public boolean isFinish() {
		int bit = actFlag & 16;
		if(bit==16) {
			return true;
		}
		return false;
	}
	
	/**
	 * 発話終了状態に切り替え(行動フラグ変更)
	 */
	public void setActFlagFinish() {
		actFlag = actFlag | 16;
	}
	
	public void reverseActFlagFinish() {
		int bit = actFlag & 16;
		if(bit==16) {
			actFlag = actFlag ^ 16;
		}
	}
	
	/*-----------------------------CO役職---------------------------------------*/
	
	/**
	 * 自分がCOした役職取得
	 * @return COした役職
	 */
	public Role getComingoutRole() {
		return coRole;
	}
	
	/**
	 * COした役職の更新
	 * @param role :COした役職
	 */
	public void setComingoutRole(Role role) {
		coRole = role;
		this.setActFlagCO();
	}
	
	/**
	 * COした役職と指定役職を比較
	 * @param role :指定役職
	 * @return CO役職と指定役職が同じならばtrue，異なればfalse
	 */
	public boolean compareComingoutRole(Role role) {
		if(coRole==role) {
			return true;
		}
		return false;
	}
	
	
	/*-----------------------------CO数---------------------------------------*/
	
	/**
	 * 指定役職のCO数を返す
	 * @param role :指定役職
	 * @return CO数
	 */
	public int getCountCO(Role role) {
		int num = 0;
		switch (role) {
		case VILLAGER:
			num = countCO[0];
			break;
		case SEER:
			num = countCO[1];
			break;
		case MEDIUM:
			num = countCO[2];
			break;
		case BODYGUARD:
			num = countCO[3];
			break;
		case POSSESSED:
			num = countCO[4];
			break;
		case WEREWOLF:
			num = countCO[5];
			break;
		default:
			break;
		}
		return num;
	}
	
	/**
	 * CO数の合計を取得
	 * @return CO数合計
	 */
	public int sumCountCO() {
		int sum = 0;
		for(int i=0; i<countCO.length; i++) {
			sum += countCO[i];
		}
		return sum;
	}
	
	/**
	 * 指定役職のCO数増加
	 * @param role :指定役職
	 */
	public void addCountCO(Role role) {
		switch (role) {
		case VILLAGER:
			countCO[0] = countCO[0] + 1;
			break;
		case SEER:
			countCO[1] = countCO[1] + 1;
			break;
		case MEDIUM:
			countCO[2] = countCO[2] + 1;
			break;
		case BODYGUARD:
			countCO[3] = countCO[3] + 1;
			break;
		case POSSESSED:
			countCO[4] = countCO[4] + 1;
			break;
		case WEREWOLF:
			countCO[5] = countCO[5] + 1;
			break;
		default:
			break;
		}
	}
	
	/**
	 * 占い師と霊媒師のCO数確認(占い師-霊媒師)
	 * @param s :占い師CO数
	 * @param m :霊媒師CO数
	 * @return どちらも満たせばtrue,ことなればfalse
	 */
	public boolean checkSeerMedium(int s, int m) {
		if(Check.isNum(countCO[1], s) && Check.isNum(countCO[2], m)) {
			return true;
		}
		return false;
	}
	
	/*-----------------------------投票先---------------------------------------*/
	
	/**
	 * 現在の投票先取得
	 * @return 現在の投票対象
	 */
	public Agent getVoteTarget() {
		return this.voteTarget;
	}
	
	/**
	 * 投票先の指定
	 * @param target :投票先エージェント
	 */
	public void setVoteTarget(Agent target) {
		this.voteTarget = target;
	}
	
	/**
	 * 昨日の投票対象を取得
	 * @return 昨日の投票対象
	 */
	public Agent getPreviousVoteTarget() {
		return this.previousVoteTarget;
	}
	
	/**
	 * 昨日の投票対象を設定
	 * @param target :昨日の投票対象
	 */
	public void setPreviousVoteTarget(Agent target) {
		this.previousVoteTarget = target;
	}
	
	/*-----------------------------占い対象---------------------------------------*/
	
	/**
	 * 本日の占う対象を取得する
	 * @return 占い対象エージェント
	 */
	public Agent getDivineTarget() {
		return this.divineTarget;
	}
	
	/**
	 * 本日の占う対象を指定する
	 * @param target :占い対象エージェント
	 */
	public void setDivineTarget(Agent target) {
		this.divineTarget = target;
	}
	
	/**
	 * 前日の占い先(占った先)の対象を取得する
	 * @return 前日の占い対象(実際に占った対象)
	 */
	public Agent getPreviousDivineTarget() {
		return previousDivineTarget;
	}
	
	/**
	 * 前日の占い先(占った先)の対象を指定する
	 * @param target :前日の占い対象
	 */
	public void setPreviousDivineTarget(Agent target) {
		this.previousDivineTarget = target;
	}
	
	/*-----------------------------占い結果--------------------------------------*/

	/**
	 * 占い結果マップを取得する
	 * @return 占い結果マップ
	 */
	public Map<Agent, Judge> getDivineResultMap() {
		return this.divineResultMap;
	}
	
	/**
	 * 占い結果を更新する<br>
	 * @param judge :更新する占い結果
	 */
	public void setDivineResultMap(Judge judge) {
		this.getDivineResultMap().put(judge.getTarget(), judge);
	}
	
	/**
	 * 占い結果を更新する
	 * @param day :日付
	 * @param agent :占い師
	 * @param target :占い対象
	 * @param spe :種族
	 */
	public void setDivineResultMap(int day, Agent agent, Agent target, Species spe) {
		Judge judge = new Judge(day, agent, target, spe);
		this.setDivineResultMap(judge);
	}
	
	/**
	 * 占い結果をリストにして全て取得する
	 * @return 全占い結果のリスト
	 */
	public List<Judge> getAllDivineResultList() {
		List<Judge> storeList = new ArrayList<>();
		for(Agent agent: this.getDivineResultMap().keySet()) {
			storeList.add(this.getDivineResultMap().get(agent));
		}
		return storeList;
	}
	
	/**
	 * 占い結果の対象一覧を取得する
	 * @return 占い対象エージェントリスト(占い済みエージェントのリスト)
	 */
	public List<Agent> getDivineResultTargetList() {
		List<Agent> agentList = new ArrayList<>();
		for(Agent agent: this.divineResultMap.keySet()) {
			agentList.add(agent);
		}
		
		return agentList;
	}
	
	/**
	 * 指定エージェントが占い済みかどうか
	 * @param target :指定エージェント
	 * @return 占い済みならばtrue,占い済みでないならばfalse
	 */
	public boolean containDivineResultTarget(Agent target) {
		return this.getDivineResultMap().containsKey(target);
	}
	
	/**
	 * 指定種族の結果のエージェントがいるかどうか
	 * @param species :指定種族
	 * @return いればtrue,いなければfalse
	 */
	public boolean containDivineResultSpecies(Species species) {
		for(Agent agent :this.divineResultMap.keySet()) {
			if(species==this.getDivineResultSpecies(agent)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定エージェントのJudgeを取得する
	 * @param target :指定エージェント
	 * @return 指定エージェントのJudge
	 */
	public Judge getDivineResultJudge(Agent target) {
		if(!this.containDivineResultTarget(target)) {
			return null;
		}
		return this.getDivineResultMap().get(target);
	}
	
	/**
	 * 指定日付のJudgeを取得する
	 * @param day :指定日付
	 * @return 指定日付のJudge
	 */
	public Judge getDivineResultJudge(int day) {
		
		for(Judge judge: this.getAllDivineResultList()) {
			if(day == judge.getDay()) {
				return judge;
			}
		}
		return null;
	}
	
	/**
	 * 指定エージェントの占い結果を取得する
	 * @param target :指定エージェント
	 * @return 占い結果
	 */
	public Species getDivineResultSpecies(Agent target) {
		if(!this.containDivineResultTarget(target)) {
			return null;
		}
		return this.getDivineResultMap().get(target).getResult();
	}
	
	/**
	 * 指定エージェントの占い日を取得する
	 * @param target :指定エージェント
	 * @return 占い日
	 */
	public int getDivineResultDay(Agent target) {
		if(!this.containDivineResultTarget(target)) {
			return 100;
		}
		return this.getDivineResultMap().get(target).getDay();
	}
	
	/**
	 * 指定したエージェントリストの中で，占っていないエージェントのリストを取得
	 * @param agents :エージェントリスト
	 * @return 占っていないエージェントのリスト
	 */
	public List<Agent> remainDivineAgentList(List<Agent> agents) {
		List<Agent> remainList = new ArrayList<>();
		for(Agent agent: agents) {
			if(!containDivineResultTarget(agent)) {
				remainList.add(agent);
			}
		}
		return remainList;
	}
	
	/**
	 * 占い結果が指定種族である占い結果リストを取得
	 * @param spe :指定種族
	 * @return　結果リスト
	 */
	public List<Judge> getDivineResultList(Species spe) {
		List<Judge> storeList = new ArrayList<>();
		
		for(Judge judge: this.getAllDivineResultList()) {
			if(Check.isSpecies(judge.getResult(), spe)) {
				storeList.add(judge);
			}
		}
		
		return storeList;
	}
	
	
	/*-----------------------------霊媒結果---------------------------------------*/
	
	/**
	 * 霊媒結果マップを取得する
	 * @return 霊媒結果マップ
	 */
	public Map<Agent, Judge> getIdentResultMap() {
		return this.identResultMap;
	}
	
	/**
	 * 霊媒結果を更新する
	 * @param judge :更新する霊媒結果
	 */
	public void setIdentResultMap(Judge judge) {
		this.getIdentResultMap().put(judge.getTarget(), judge);
	}
	
	/**
	 * 霊媒結果を更新する
	 * @param day :日付
	 * @param agent :占い師
	 * @param target :占い対象
	 * @param spe :種族
	 */
	public void setIdentResultMap(int day, Agent agent, Agent target, Species spe) {
		Judge judge = new Judge(day, agent, target, spe);
		this.setIdentResultMap(judge);
	}
	
	/**
	 * 霊媒結果を全てリストにして取得する
	 * @return 全霊媒結果のリスト
	 */
	public List<Judge> getAllIdentResultList() {
		List<Judge> storeList = new ArrayList<>();
		for(Agent agent: this.getIdentResultMap().keySet()) {
			storeList.add(this.getIdentResultMap().get(agent));
		}
		return storeList;
	}
	
	/**
	 * 霊媒結果の対象一覧を取得する
	 * @return 霊媒対象エージェントリスト(霊媒済みのエージェントリスト)
	 */
	public List<Agent> getIdentResultTargetList() {
		List<Agent> agentList = new ArrayList<>();
		for(Agent agent: this.identResultMap.keySet()) {
			agentList.add(agent);
		}
		
		return agentList;
	}
	
	/**
	 * 指定エージェントが霊媒済みかどうか
	 * @param target :指定エージェント(霊媒先)
	 * @return 霊媒済みならばtrue,占い済みでないならばfalse
	 */
	public boolean containIdentResultTarget(Agent target) {
		return this.getIdentResultMap().containsKey(target);
	}
	
	/**
	 * 指定種族の結果のエージェントがいるかどうか
	 * @param species :指定種族
	 * @return いればtrue,いなければfalse
	 */
	public boolean containIdentResultSpecies(Species species) {
		for(Agent agent :this.getIdentResultMap().keySet()) {
			if(species==this.getIdentResultSpecies(agent)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定エージェントのJudgeを取得する
	 * @param target :指定エージェント
	 * @return 指定エージェントのJudge
	 */
	public Judge getIdentResultJudge(Agent target) {
		if(!this.containIdentResultTarget(target)) {
			return null;
		}
		return this.getIdentResultMap().get(target);
	}
	
	/**
	 * 指定日付のJudgeを取得する
	 * @param day :指定日付
	 * @return 指定日付のJudge
	 */
	public Judge getIdentResultJudge(int day) {
		List<Agent> agentList = this.getIdentResultTargetList();
		if(agentList.isEmpty()) {
			return null;
		}
		for(Agent agent: agentList) {
			if(day==this.getIdentResultDay(agent)) {
				return this.getIdentResultJudge(agent);
			}
		}
		return null;
	}
	
	/**
	 * 指定エージェントの霊媒結果を取得する
	 * @param target :指定エージェント(霊媒先)
	 * @return 霊媒結果
	 */
	public Species getIdentResultSpecies(Agent target) {
		if(!this.containIdentResultTarget(target)) {
			return null;
		}
		return this.getIdentResultMap().get(target).getResult();
	}
	
	/**
	 * 指定エージェントの霊媒日を取得する
	 * @param target :指定エージェント(霊媒先)
	 * @return 霊媒日
	 */
	public int getIdentResultDay(Agent target) {
		if(!this.containIdentResultTarget(target)) {
			return 100;
		}
		return this.getIdentResultMap().get(target).getDay();
	}
	
	/**
	 * 霊媒結果が指定種族であった結果リストを取得する
	 * @param spe :指定種族
	 * @return 指定種奥の結果リスト
	 */
	public List<Judge> getIdentResultList(Species spe) {
		List<Judge> storeList = new ArrayList<>();
		for(Judge judge: this.getAllIdentResultList()) {
			if(Check.isSpecies(judge.getResult(), spe)) {
				storeList.add(judge);
			}
		}
		return storeList;
	}
	
	/*-----------------------------護衛先---------------------------------------*/
	
	/**
	 * 護衛対象取得
	 * @return 護衛対象
	 */
	public Agent getGuardTarget() {
		return guardTarget;
	}
	
	/**
	 * 護衛対象を設定
	 * @param agent :護衛先エージェント
	 */
	public void setGuardTarget(Agent agent) {
		this.guardTarget = agent;
	}
	
	/**
	 * 前日の護衛対象(護衛した対象)取得
	 * @return 護衛対象
	 */
	public Agent getPreviousGuardTarget() {
		return previousDivineTarget;
	}
	
	/**
	 * 前日の護衛対象(護衛した対象)を設定
	 * @param agent :前日の護衛対象(護衛した対象)エージェント
	 */
	public void setPreviousGuardTarget(Agent agent) {
		this.previousDivineTarget = agent;
	}
	
	/*-----------------------------護衛結果---------------------------------------*/
	
	/**
	 * 護衛結果の情報を取得
	 * @return 護衛結果の情報
	 */
	public Map<Integer, AbilityResultInfo> getGuardResultMap() {
		return this.guardResultMap;
	}
	
	/**
	 * 護衛結果の情報を登録する
	 * @param day :護衛日
	 * @param target :護衛対象
	 * @param success :護衛の成否
	 */
	public void setGuardResultMap(int day, Agent target, boolean success) {
		AbilityResultInfo info = new AbilityResultInfo(day, target, success);
		this.guardResultMap.put(day, info);
	}
	
	/**
	 * 指定日付の護衛結果を取得する
	 * @param day :護衛日
	 * @return 指定日付の護衛結果
	 */
	public AbilityResultInfo getGuardResultInfo(int day) {
		AbilityResultInfo info = null;
		if(this.guardResultMap.containsKey(day)) {
			info = this.guardResultMap.get(day);
		}
		return info;
	}
	
	/**
	 * 指定エージェントを護衛した回数
	 * @param target :護衛したエージェント
	 * @return 護衛回数
	 */
	public int countGuardResultTarget(Agent target) {
		int count = 0;
		for(int day: this.guardResultMap.keySet()) {
			AbilityResultInfo info = this.getGuardResultInfo(day);
			if(Check.isAgent(info.getTarget(), target)) {
				count+=1;
			}
		}
		return count;
	}
	
	/**
	 * 指定エージェントを護衛した結果リストを取得する
	 * @param target :護衛したエージェント
	 * @return 指定エージェントを護衛した結果リスト
	 */
	public List<AbilityResultInfo> getGuardResultTargetList(Agent target) {
		List<AbilityResultInfo> storeList = new ArrayList<>();
		for(AbilityResultInfo info: this.getAllGuardResultList()) {
			if(Check.isAgent(info.getTarget(), target)) {
				storeList.add(info);
			}
		}
		return storeList;
	}
	
	/**
	 * 護衛結果が成功または失敗(引数で指定)であるエージェントのリストを取得する
	 * @param success :trueならば成功，falseならば失敗
	 * @return エージェントリスト
	 */
	public List<Agent> getGuardResultTargetList(boolean success) {
		List<Agent> storeList = new ArrayList<>();
		for(AbilityResultInfo info: this.getAllGuardResultList()) {
			if(info.getSuccess() == success) {
				HandyGadget.addList(storeList, info.getTarget());
			}
		}
		return storeList;
	}
	
	/**
	 * 護衛結果をリストにして全て取得する
	 * @return 全護衛結果のリスト
	 */
	public List<AbilityResultInfo> getAllGuardResultList() {
		List<AbilityResultInfo> storeList = new ArrayList<>();
		for(int day: this.guardResultMap.keySet()) {
			storeList.add(this.guardResultMap.get(day));
		}
		return storeList;
	}
	
	/*-----------------------------吊り手---------------------------------------*/
	
	/**
	 * 吊り手の取得
	 * @return 吊り手
	 */
	public int getCountHang() {
		return countHang;
	}
	
	/**
	 * 吊り手の更新
	 */
	public void updateCountHang() {
		if(gameInfo.getAliveAgentList().size()%2==1) {
			countHang = gameInfo.getAliveAgentList().size()/2;
		}else {
			countHang = gameInfo.getAliveAgentList().size()/2-1;
		}
	}
	
	/*-----------------------------戦略---------------------------------------*/
	
	/**
	 * 現在の戦略取得
	 * @return 戦略
	 */
	public Strategy getStrategy() {
		return strategy;
	}
	
	/**
	 * 戦略の設定
	 * @param select :設定する戦略
	 */
	public void setStrategy(Strategy select) {
		strategy = select;
	}
	
	/*-----------------------------盤面整理---------------------------------------*/
	
	public BoardArrange getBoardArrange() {
		return boardArrange;
	}
	
	/*-----------------------------共通行動許可--------------------------------------*/
	
	/**
	 * 共通行動が許可か却下か確認
	 * @return 許可ならばtrue,却下ならばfalse
	 */
	public boolean isReaction() {
		if(Check.isNum(isReaction, 0)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 共通行動を許可状態に変更
	 */
	public void permitReaction() {
		isReaction = 1;
	}
	
	/**
	 * 共通行動を却下状態に変更
	 */
	public void rejectReaction() {
		isReaction = 0;
	}
	
	/*-----------------------------襲撃対象---------------------------------------*/
	
	/**
	 * 今日の襲撃対象を取得する
	 * @return 今日の襲撃先エージェント
	 */
	public Agent getAttackTarget() {
		return attackTarget;
	}
	
	/**
	 * 今日の襲撃対象を指定する
	 * @param target :今日の襲撃対象エージェント
	 */
	public void setAttackTarget(Agent target) {
		attackTarget = target;
	}
	
	/**
	 * 自分が決めた前日の襲撃予定対象を取得する(実際に襲撃したかは問わず)
	 * @return 前日の襲撃予定エージェント
	 */
	public Agent getPreviousAttackTarget() {
		return previousAttackAgent;
	}
	
	/**
	 * 自分が決めた前日の襲撃予定対象を指定する(実際に襲撃したかは問わず)
	 * @param target :前日の襲撃予定エージェント
	 */
	public void setPreviousAttackTarget(Agent target) {
		previousAttackAgent = target;
	}
	
	/*-----------------------------襲撃結果---------------------------------------*/
	
	/**
	 * 襲撃結果の情報を取得する
	 * @return 襲撃結果情報
	 */
	public Map<Integer, AbilityResultInfo> getAttackResultMap() {
		return this.attackResultMap;
	}
	
	/**
	 * 襲撃結果の情報を登録する
	 * @param day :襲撃日
	 * @param target :襲撃対象
	 * @param success :襲撃の成否
	 */
	public void setAttackResultMap(int day, Agent target, boolean success) {
		AbilityResultInfo info = new AbilityResultInfo(day, target, success);
		this.attackResultMap.put(day, info);
	}
	
	/**
	 * 指定日付の襲撃結果を取得する
	 * @param day :襲撃日
	 * @return 指定日付の襲撃結果
	 */
	public AbilityResultInfo getAttackResultInfo(int day) {
		AbilityResultInfo info = null;
		if(this.attackResultMap.containsKey(day)) {
			info = this.attackResultMap.get(day);
		}
		return info;
	}
	
	/**
	 * 指定エージェントを襲撃した回数
	 * @param target :襲撃したエージェント
	 * @return 襲撃回数
	 */
	public int countAttackResultTarget(Agent target) {
		int count = 0;
		for(int day: this.attackResultMap.keySet()) {
			AbilityResultInfo info = this.getAttackResultInfo(day);
			if(Check.isAgent(info.getTarget(), target)) {
				count+=1;
			}
		}
		return count;
	}
	
	/**
	 * 指定エージェントの襲撃結果リストを取得する
	 * @param target :襲撃したエージェント
	 * @return 指定エージェントの襲撃結果リスト
	 */
	public List<AbilityResultInfo> getAttackResultTargetList(Agent target) {
		List<AbilityResultInfo> storeList = new ArrayList<>();
		for(AbilityResultInfo info: this.getAllAttackResultList()) {
			if(Check.isAgent(info.getTarget(), target)) {
				storeList.add(info);
			}
		}
		return storeList;
	}
	
	/**
	 * 襲撃結果をリストにして全て取得する
	 * @return 全襲撃結果のリスト
	 */
	public List<AbilityResultInfo> getAllAttackResultList() {
		List<AbilityResultInfo> storeList = new ArrayList<>();
		for(int day: this.attackResultMap.keySet()) {
			storeList.add(this.attackResultMap.get(day));
		}
		return storeList;
	}

}
