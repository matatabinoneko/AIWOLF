package com.gmail.aiwolf.uec.yk.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;


/**
 * ゲーム情報
 */
public final class AdvanceGameInfo {


	// ---- 以下、情報系の変数 ----

	/** ゲーム開始時に受け取ったGameSetting */
	public GameSetting gameSetting;

	/** 最新のGameInfo */
	public GameInfo latestGameInfo;

	/** 発言ログ（全日分） */
	private List<List<Talk>> talkLists = new ArrayList<List<Talk>>();

	/** 投票ログ（全日分） */
	private List<List<Vote>> voteLists = new ArrayList<List<Vote>>();

	/** COのリスト(村側役職をCO) */
	public List<ComingOut> comingOutList = new ArrayList<ComingOut>();

	/** COのリスト(人外役職をCO) */
	public List<ComingOut> wolfsideComingOutList = new ArrayList<ComingOut>();

	/** COのリスト(人狼の囁き) */
	public List<ComingOut> wisperComingOutList = new ArrayList<ComingOut>();

	/** エージェントの状態 idx = AgentNo */
	public AgentState[] agentState;

	/** 日ごとの情報のリスト */
	public List<DayInfo> dayInfoList = new ArrayList<DayInfo>();

	/** 過去に解析した発言を格納するマップ(高速化用) key=talkContent value=utterance */
	public HashMap<String, Utterance> analysedUtteranceMap = new HashMap<String, Utterance>(32);

	/** 騙る役職 */
	public Role fakeRole;


	// 視点階層図（元々の予定。タイムオーバー対策でかなり削減済）
	// 全視点（root）
	// 　＋村騙り無い視点
	// 　　＋各PL視点

	/** 全視点の視点情報(システムによる確定情報のみ) */
	public ViewpointInfo allViewSystemInfo;

	/** 全視点の視点情報(決め打ち情報) */
	public ViewpointInfo allViewTrustInfo;

	/** 自分視点の視点情報(人ごとの視点情報への参照) */
	public ViewpointInfo selfViewInfo;

	/** 自分の実際の役職の視点情報(現状狂人のみ対応) */
	public ViewpointInfo selfRealRoleViewInfo;


	/** 占判定一覧 */
	private List<Judge> seerJudgeList = new ArrayList<Judge>();

	/** 霊判定一覧 */
	private List<Judge> mediumJudgeList = new ArrayList<Judge>();

	/** 護衛履歴一覧 */
	private List<GuardRecent> guardRecentList = new ArrayList<GuardRecent>();

	/** 自分の能力による判定のリスト(占用) */
	public List<Judge> selfInspectList = new ArrayList<Judge>();

	/** 自分の能力による判定のリスト(霊用) */
	public List<Judge> selfInquestList = new ArrayList<Judge>();

	/** 自分の護衛履歴(狩用) key=実行した日(初回=1) value=対象エージェント番号 */
	public HashMap<Integer, Integer> selfGuardRecent = new HashMap<Integer, Integer>();

	/** 自分の能力結果を報告した回数(占霊狩 共用) */
	public int reportSelfResultCount;

	// ---- 以下、日ごとにリセットされる情報系の変数 ----

	/** 本日の疑い済エージェントのリスト */
	public List<Integer> talkedSuspicionAgentList = new ArrayList<Integer>();

	/** 本日の信用済エージェントのリスト */
	public List<Integer> talkedTrustAgentList = new ArrayList<Integer>();

	// ---- 以下、動作系の変数 ----

	/** 日付更新で呼ばれたUpdate()のときTrueにする */
	private boolean isDayUpdate;

	/** 会話をどこまで読んだか(次回読み始める発言No) */
	private int readTalkListNum;

	/** 狼会話をどこまで読んだか(次回読み始める発言No) */
	private int readWhisperListNum;

	/**
	 * コンストラクタ
	 */
	public AdvanceGameInfo(GameInfo gameInfo, GameSetting gameSetting){

//		long starttime = System.currentTimeMillis();

		// ゲーム設定の初期化
		this.gameSetting = gameSetting;

		// 受け取ったgameInfoを最新のものとして保管する
		//latestGameInfo = gameInfo;

		// エージェント状態の初期化
		agentState = new AgentState[gameSetting.getPlayerNum() + 1];
		for( int i = 1; i <= gameSetting.getPlayerNum(); i++ ){
			agentState[i] = new AgentState(i);
		}

		// 全視点情報の初期化(システムによる確定情報のみ)
		allViewSystemInfo = new ViewpointInfo(gameSetting);

		// 全視点情報の初期化(村側の虚偽報告が無い前提)
		allViewTrustInfo = new ViewpointInfo(allViewSystemInfo);
		allViewSystemInfo.addInclusionViewpoint(allViewTrustInfo);

		// 自分視点情報の初期化
		selfViewInfo = new ViewpointInfo(allViewTrustInfo);
		selfViewInfo.removeWolfsidePattern(gameInfo.getAgent().getAgentIdx());
		allViewTrustInfo.addInclusionViewpoint(selfViewInfo);

		// 自分狂人視点情報の初期化
		if( gameInfo.getRole() == Role.POSSESSED ){
			selfRealRoleViewInfo = new ViewpointInfo(allViewTrustInfo);
			selfRealRoleViewInfo.removeNotPossessedPattern(gameInfo.getAgent().getAgentIdx());
			allViewTrustInfo.addInclusionViewpoint(selfRealRoleViewInfo);
		}

//		long endtime = System.currentTimeMillis();
//
//		// デバッグメッセージの出力
//		System.out.println("AdvanceGameInfo InitTime:" + (endtime - starttime));

	}


	/**
	 * 情報の更新(AbstractVillager継承クラス等でupdate()を呼び出す時に実行する)
	 * @param gameInfo
	 */
	public void update(GameInfo gameInfo){

		// 日付変更チェック
		boolean updateday = false;
		if( latestGameInfo == null || gameInfo.getDay() > latestGameInfo.getDay() ){
			updateday = true;
		}

		// 受け取ったgameInfoを最新のものとして保管する
		latestGameInfo = gameInfo;

		// 日付変更時の処理か
		if( updateday ){

			isDayUpdate = true;
			dayStart();

		}else{

			isDayUpdate = false;

			// 発言ログの更新
			setTalkList( latestGameInfo.getDay(), latestGameInfo.getTalkList() );

			// CO状況の更新
			setCOList();
			setWhisperCOList();

			// 会話をどこまで読んだか(次回読み始める発言No)の更新
			readTalkListNum = latestGameInfo.getTalkList().size();
			readWhisperListNum = latestGameInfo.getWhisperList().size();

		}


	}


	private void dayStart(){

		// 最後に読んだログ番号のリセット
		readTalkListNum = 0;
		readWhisperListNum = 0;

		// 投票結果の設定
		if( latestGameInfo.getDay() >= 1 ){
			setVoteList( latestGameInfo.getDay() - 1, latestGameInfo.getVoteList() );
		}

		// 吊り発生時の処理
		if( latestGameInfo.getExecutedAgent() != null ){
			// 吊られたエージェントの状態更新
			agentState[latestGameInfo.getExecutedAgent().getAgentIdx()].deathDay = latestGameInfo.getDay();
			agentState[latestGameInfo.getExecutedAgent().getAgentIdx()].causeofDeath = CauseOfDeath.EXECUTED;
		}

		// 噛み発生時の処理
		if( latestGameInfo.getAttackedAgent() != null ){
			// 噛まれたエージェントの状態更新
			agentState[latestGameInfo.getAttackedAgent().getAgentIdx()].deathDay = latestGameInfo.getDay();
			agentState[latestGameInfo.getAttackedAgent().getAgentIdx()].causeofDeath = CauseOfDeath.ATTACKED;

			// 各視点から、噛み先が狼のパターンを消去する(システム情報)
			allViewSystemInfo.removeWolfPattern(latestGameInfo.getAttackedAgent().getAgentIdx());
		}

		//TODO 他編成対応
		// 残り狼数からのパターン絞込み(G16の場合、処刑が３回発生する4日目以降)(システム情報)
		int maxWolfNum = ( latestGameInfo.getAliveAgentList().size() - 1 ) / 2;
		if( latestGameInfo.getDay() >= 4 ){
			allViewSystemInfo.removePatternFromWolfNum(Common.getAgentNo(latestGameInfo.getAliveAgentList()), 1, maxWolfNum);
		}

		// 新しい日の情報を設定する
		DayInfo toDayInfo = new DayInfo( latestGameInfo );
		dayInfoList.add(toDayInfo);


		// 本日の疑い済・信用済エージェントのリストを初期化する
		talkedSuspicionAgentList = new ArrayList<Integer>();
		talkedTrustAgentList = new ArrayList<Integer>();


		// 狩人の処理
		if( latestGameInfo.getRole() == Role.BODYGUARD ){
			// ２日目以降、生きていれば護衛履歴を記憶する
			if( latestGameInfo.getDay() >= 2 &&
				latestGameInfo.getAliveAgentList().contains( latestGameInfo.getAgent() ) ){
				selfGuardRecent.put( latestGameInfo.getDay() - 1, latestGameInfo.getGuardedAgent().getAgentIdx() );
			}
		}

		// 占い師の処理
		if( latestGameInfo.getRole() == Role.SEER ){
			// １日目以降、生きていれば占い結果を記憶する
			if( latestGameInfo.getDay() >= 1 &&
			    latestGameInfo.getAliveAgentList().contains( latestGameInfo.getAgent() ) ){

				Judge newJudge = new Judge( latestGameInfo.getAgent().getAgentIdx(),
				                            latestGameInfo.getDivineResult().getTarget().getAgentIdx(),
				                            latestGameInfo.getDivineResult().getResult(),
				                            null );

				selfInspectList.add(newJudge);
			}
		}

		// 霊能者の処理
		if( latestGameInfo.getRole() == Role.MEDIUM ){
			// ２日目以降、生きていれば霊能結果を記憶する
			if( latestGameInfo.getDay() >= 2 &&
			    latestGameInfo.getAliveAgentList().contains( latestGameInfo.getAgent() ) ){

				Judge newJudge = new Judge( latestGameInfo.getAgent().getAgentIdx(),
				                            latestGameInfo.getMediumResult().getTarget().getAgentIdx(),
				                            latestGameInfo.getMediumResult().getResult(),
				                            null );

				selfInquestList.add(newJudge);
			}
		}

	}


	/**
	 * 発言ログのセット
	 * @param day 日
	 * @param talklist 発言のリスト
	 */
	private void setTalkList(int day, List<Talk> talklist ){

		// 指定日の前日までのログを埋める
		while( talkLists.size() < day ){
			talkLists.add(new ArrayList<Talk>());
		}

		// 当日のログが無ければ追加、あれば上書きする
		if( talkLists.size() > day){
			talkLists.set(day, talklist);
		}else{
			talkLists.add(talklist);
		}

	}


	/**
	 * 発言ログの取得
	 * @param day 日
	 * @return
	 */
	public List<Talk> getTalkList(int day){

		// データが存在する場合
		if( day >= 0 && day < talkLists.size() ){
			return talkLists.get(day);
		}

		// データが存在しない場合
		return null;

	}


	/**
	 *
	 * @param day 日
	 * @param talkid 発言ID
	 * @return
	 */
	public Talk getTalk(int day, int talkid){

		List<Talk> talkList = getTalkList(day);

		// 指定日のログが存在するか
		if( talkList != null ){
			// 発言が存在するか
			if( talkid >= 0 && talkid < talkList.size() ){
				return talkList.get(talkid);
			}
		}

		return null;

	}


	/**
	 * 投票ログのセット
	 * @param day 投票が行われた日(初回投票＝1日目)
	 * @param votelist 投票結果のリスト
	 */
	private void setVoteList( int day, List<Vote> votelist ){

		// 指定日の前日までの投票結果を埋める
		while( voteLists.size() < day - 1){
			voteLists.add(new ArrayList<Vote>());
		}

		// 当日のログが無ければ追加、あれば上書きする
		if( voteLists.lastIndexOf(0) >= day){
			voteLists.set(day, votelist);
		}else{
			voteLists.add(votelist);
		}

	}


	/**
	 * 投票ログの取得
	 * @param day 投票が行われた日(初回投票＝1日目)
	 * @return
	 */
	public List<Vote> getVoteList(int day){

		// データが存在する場合
		if( day >= 0 && day < voteLists.size() ){
			return voteLists.get(day);
		}

		// データが存在しない場合
		return null;
	}


	/**
	 * CO状況の更新
	 */
	private void setCOList(){

		// CO撤回フラグ
		boolean existCancel = false;

		int day = latestGameInfo.getDay();
		List<Talk> talkList = talkLists.get(day);

		// 発言の走査
		for (int i = readTalkListNum; i < talkList.size(); i++) {
			Talk talk = (Talk)talkList.get(i);
			Utterance utterance = getUtterance(talk.getContent());
			switch (utterance.getTopic())
			{
				case COMINGOUT:	// CO
					int COAgentNo = talk.getAgent().getAgentIdx();
					Role CORole = utterance.getRole();
					switch(CORole.getTeam()){
						case VILLAGER:
							// CO対象が自分以外なら処理せずにスキップ
							if( !utterance.getTarget().equals(talk.getAgent()) ){
								break;
							}

							// CO情報の更新
							if( updateCommingOut( COAgentNo, CORole, talk ) ){
								// CO撤回発生フラグを立てる
								existCancel = true;
							}
							break;
						case WEREWOLF:
							// CO情報の更新
							updateWolfSideCommingOut( COAgentNo, CORole, talk );
							break;
						default:
							break;
					}
					break;
				case DIVINED:	// 占い結果
					int seerAgentNo = talk.getAgent().getAgentIdx();
					int inspectedAgentNo = utterance.getTarget().getAgentIdx();
					Species inspectResult = utterance.getResult();
					Judge sjudge = new Judge(seerAgentNo, inspectedAgentNo, inspectResult, talk);

					// 霊能CO者が占い結果を落とした場合、霊能結果として扱う
					if( agentState[seerAgentNo].comingOutRole == Role.MEDIUM ){
						// 霊能判定の登録
						addMediumJudge(sjudge, talk);
					}else{
						// CO情報の更新
						if( updateCommingOut( seerAgentNo, Role.SEER, talk ) ){
							// CO撤回発生フラグを立てる
							existCancel = true;
						}

						// 占い判定の登録
						addSeerJudge(sjudge, talk);
					}

					break;
				case IDENTIFIED:	// 霊能結果
					int mediumAgentNo = talk.getAgent().getAgentIdx();
					int IDENTIFIEDAgentNo = utterance.getTarget().getAgentIdx();
					Species IDENTIFIEDResult = utterance.getResult();
					Judge mjudge = new Judge(mediumAgentNo, IDENTIFIEDAgentNo, IDENTIFIEDResult, talk);


					// 占いCO者が霊能結果を落とした場合、占い結果として扱う
					if( agentState[mediumAgentNo].comingOutRole == Role.SEER ){
						// 占い判定の登録
						addSeerJudge(mjudge, talk);
					}else{
						// CO情報の更新
						if( updateCommingOut( mediumAgentNo, Role.MEDIUM, talk ) ){
							// CO撤回発生フラグを立てる
							existCancel = true;
						}

						// 霊能判定の登録
						addMediumJudge(mjudge, talk);
					}

					break;
				case GUARDED:
					int bodyGuardAgentNo = talk.getAgent().getAgentIdx();
					int guardedAgentNo = utterance.getTarget().getAgentIdx();
					GuardRecent guardRecent = new GuardRecent(bodyGuardAgentNo, guardedAgentNo, talk);

					// CO情報の更新
					if( updateCommingOut( bodyGuardAgentNo, Role.BODYGUARD, talk ) ){
						// CO撤回発生フラグを立てる
						existCancel = true;
					}

					int guardReportCount = 0;
					for( GuardRecent guard : guardRecentList ){
						if( guard.isEnable() && guard.agentNo == bodyGuardAgentNo ){
							guardReportCount++;
						}
					}

					// 護衛実行日を推測して決定
					guardRecent.execDay = guardReportCount + 1;

					// 護衛履歴一覧に登録
					guardRecentList.add(guardRecent);

					break;
				default:
					break;
			}
		}

		// CO撤回があった場合
		if( existCancel ){
			// 視点情報を構築し直す
			remakeViewInfo();
		}

		// 自分視点の内訳が無くなった（人外で破綻、村騙りで内訳破壊等）
		if( selfViewInfo.wolfsidePatterns.isEmpty() ){

			// 自分視点決め打ち情報をシステム視点に合わせる
			selfViewInfo.remakePattern(allViewSystemInfo);

			// 自分を狼陣営から除外する
			selfViewInfo.removeWolfsidePattern(latestGameInfo.getAgent().getAgentIdx());

		}

	}


	/**
	 * CO状況の更新(囁き)
	 */
	private void setWhisperCOList(){

		int day = latestGameInfo.getDay();
		List<Talk> talkList = latestGameInfo.getWhisperList();

		// 発言の走査
		for (int i = readWhisperListNum; i < talkList.size(); i++) {
			Talk talk = (Talk)talkList.get(i);
			Utterance utterance = getUtterance(talk.getContent());
			switch (utterance.getTopic())
			{
				case COMINGOUT:	// CO
					int COAgentNo = talk.getAgent().getAgentIdx();
					Role CORole = utterance.getRole();
					switch(CORole.getTeam()){
						case VILLAGER:
							// CO対象が自分以外なら処理せずにスキップ
							if( !utterance.getTarget().equals(talk.getAgent()) ){
								break;
							}

							// CO情報の更新
							updateWisperCommingOut( COAgentNo, CORole, talk );
							break;
						default:
							break;
					}
					break;
				case DIVINED:	// 占い結果
					int seerAgentNo = talk.getAgent().getAgentIdx();

					// 霊能CO者が占い結果を落とした場合、霊能結果として扱う
					if( agentState[seerAgentNo].comingOutRole == Role.MEDIUM ){
					}else{
						// CO情報の更新
						updateWisperCommingOut( seerAgentNo, Role.SEER, talk );
					}

					break;
				case IDENTIFIED:	// 霊能結果
					int mediumAgentNo = talk.getAgent().getAgentIdx();

					// 占いCO者が霊能結果を落とした場合、占い結果として扱う
					if( agentState[mediumAgentNo].comingOutRole == Role.SEER ){
					}else{
						// CO情報の更新
						updateWisperCommingOut( mediumAgentNo, Role.MEDIUM, talk );
					}

					break;
				case GUARDED:
					int bodyGuardAgentNo = talk.getAgent().getAgentIdx();

					// CO情報の更新
					updateWisperCommingOut( bodyGuardAgentNo, Role.BODYGUARD, talk );

					break;
				default:
					break;
			}
		}

	}

	/**
	 * 報告済みの役職報告を無効にする（CO撤回時の処理）
	 * @param agentNo エージェント番号
	 * @param cancelTalk 撤回を行った発言
	 */
	private void cancelRoleReport(int agentNo, Talk cancelTalk){

		// COの走査
		for( ComingOut co : comingOutList ){
			// 指定したエージェントの有効なCOか
			if( co.agentNo == agentNo && co.isEnable() ){
				// 撤回発言を設定する
				co.cancelTalk = cancelTalk;
			}
		}

		// 全ての占判定履歴を確認する
		for( Judge judge : getSeerJudgeList() ){
			// 指定したエージェントの有効な判定か
			if( judge.agentNo == agentNo && judge.isEnable() ){
				// 撤回発言を設定する
				judge.cancelTalk = cancelTalk;
			}
		}

		// 全ての霊判定履歴を確認する
		for( Judge judge : getMediumJudgeList() ){
			// 指定したエージェントの有効な判定か
			if( judge.agentNo == agentNo && judge.isEnable() ){
				// 撤回発言を設定する
				judge.cancelTalk = cancelTalk;
			}
		}

		// 全ての護衛履歴を確認する
		for( GuardRecent guard : getGuardRecentList() ){
			// 指定したエージェントの有効な護衛履歴か
			if( guard.agentNo == agentNo && guard.isEnable() ){
				// 撤回発言を設定する
				guard.cancelTalk = cancelTalk;
			}
		}

	}


	/**
	 * 視点情報を構築し直す
	 */
	private void remakeViewInfo(){

		// 全視点決め打ち情報をシステム視点に合わせる
		allViewTrustInfo.remakePattern(allViewSystemInfo);

		// 各視点から、自分が狼のパターンを除外する
		selfViewInfo.removeWolfPattern(latestGameInfo.getAgent().getAgentIdx());
		if( selfRealRoleViewInfo != null ){
			selfRealRoleViewInfo.removeWolfPattern(latestGameInfo.getAgent().getAgentIdx());
			selfRealRoleViewInfo.removeNotWolfsidePattern(latestGameInfo.getAgent().getAgentIdx());
		}

		// 占COからのパターン絞り込み(決め打ち情報)
		List<Integer> agents = getEnableCOAgentNo(Role.SEER);
		if( agents.size() > 1 ){
			allViewTrustInfo.removePatternFromUniqueRole(getEnableCOAgentNo(Role.SEER));
		}

		// 霊COからのパターン絞り込み(決め打ち情報)
		agents = getEnableCOAgentNo(Role.MEDIUM);
		if( agents.size() > 1 ){
			allViewTrustInfo.removePatternFromUniqueRole(getEnableCOAgentNo(Role.MEDIUM));
		}

		// 狩COからのパターン絞り込み(決め打ち情報)
		agents = getEnableCOAgentNo(Role.BODYGUARD);
		if( agents.size() > 1 ){
			allViewTrustInfo.removePatternFromUniqueRole(getEnableCOAgentNo(Role.BODYGUARD));
		}

		// 占判定からのパターン絞り込み
		for( Judge seerJudge : seerJudgeList ){
			if( seerJudge.isEnable() ){
				// 占い結果からのパターン絞り込み(決め打ち情報)
				allViewTrustInfo.removePatternFromJudge( seerJudge.agentNo, seerJudge.targetAgentNo, seerJudge.result );
			}
		}

		// 霊判定からのパターン絞り込み
		for( Judge mediumJudge : mediumJudgeList ){
			if( mediumJudge.isEnable() ){
				// 占い結果からのパターン絞り込み(決め打ち情報)
				allViewTrustInfo.removePatternFromJudge( mediumJudge.agentNo, mediumJudge.targetAgentNo, mediumJudge.result );
			}
		}

	}


	/**
	 * CO情報の更新
	 * @param agentNo エージェント番号
	 * @param role COした役職
	 * @param commingOutTalkCOした発言
	 * @return COの撤回が発生したか
	 */
	private boolean updateCommingOut(int agentNo, Role role, Talk commingOutTalk){

		boolean existCancel = false;

		// 既にCOしている状態で、他の役職をCO
		if( agentState[agentNo].comingOutRole != null && role != agentState[agentNo].comingOutRole ){
			// CO撤回発生フラグを立てる
			existCancel = true;

			// 報告済みの役職報告(CO・判定等)を無効にする
			cancelRoleReport( agentNo, commingOutTalk );
		}

		// COの走査
		for( ComingOut co : comingOutList ){
			// COしたエージェントの有効なCOか
			if( co.agentNo == agentNo && co.isEnable() ){
				if( co.role == role ){
					// 既に同じ役職をCOしている状態 → 処理を完了して抜ける
					return existCancel;
				}
			}
		}

		// 新しいCOとして登録する
		comingOutList.add( new ComingOut(agentNo, role, commingOutTalk) );

		// エージェント情報を更新する
		agentState[agentNo].comingOutRole = role;

		// COからのパターン絞り込み(決め打ち情報)
		List<Integer> agents = getEnableCOAgentNo(role);
		if( gameSetting.getRoleNum(role) == 1 && agents.size() > 1 ){
			allViewTrustInfo.removePatternFromUniqueRole(agents);
		}

		return existCancel;

	}


	/**
	 * CO情報の更新（狼役職をCO）
	 * @param agentNo エージェント番号
	 * @param role COした役職
	 * @param commingOutTalkCOした発言
	 */
	private void updateWolfSideCommingOut(int agentNo, Role role, Talk commingOutTalk){

		// COの走査
		for( ComingOut co : wolfsideComingOutList ){
			// COしたエージェントの有効なCOか
			if( co.agentNo == agentNo && co.isEnable() ){
				if( co.role == role ){
					// 既に同じ役職をCOしている状態 → 処理を完了して抜ける
					return;
				}
			}
		}

		// 古いCOはキャンセル扱いにする
		for( ComingOut co : wolfsideComingOutList ){
			if( co.agentNo == agentNo && co.isEnable() ){
				co.cancelTalk = commingOutTalk;
			}
		}

		// 新しいCOとして登録する
		wolfsideComingOutList.add( new ComingOut(agentNo, role, commingOutTalk) );

	}


	/**
	 * CO情報の更新（囁きで宣言した役職）
	 * @param agentNo エージェント番号
	 * @param role COした役職
	 * @param commingOutTalkCOした発言
	 */
	private void updateWisperCommingOut(int agentNo, Role role, Talk commingOutTalk){

		// COの走査
		for( ComingOut co : wisperComingOutList ){
			// COしたエージェントの有効なCOか
			if( co.agentNo == agentNo && co.isEnable() ){
				if( co.role == role ){
					// 既に同じ役職をCOしている状態 → 処理を完了して抜ける
					return;
				}
			}
		}

		// 古いCOはキャンセル扱いにする
		for( ComingOut co : wisperComingOutList ){
			if( co.agentNo == agentNo && co.isEnable() ){
				co.cancelTalk = commingOutTalk;
			}
		}

		// 新しいCOとして登録する
		wisperComingOutList.add( new ComingOut(agentNo, role, commingOutTalk) );

	}


	/**
	 * CO者のリストを取得する（有効なCOのみ）
	 * @param role 取得するCO者の役職
	 * @return CO者のエージェント番号のリスト
	 */
	public List<Integer> getEnableCOAgentNo(Role role) {

		List<Integer> ret = new ArrayList<Integer>();

		// COの走査
		for( ComingOut co : comingOutList ){
			// 指定した役職かつ有効なCOか
			if( co.role == role && co.isEnable() ){
				// 結果リストに追加
				ret.add(co.agentNo);
			}
		}

		return ret;
	}


	/**
	 * 指定時点のCO役職を取得する
	 * @param agentNo 役職を取得するエージェント
	 * @param day 日(以下判定)
	 * @param talkID 発言ID(未満判定)
	 * @return 指定された発言が行われる直前状態でCOしている役職
	 */
	public Role getCORole(int agentNo, int day, int talkID){

		Role ret = null;

		// COの走査
		for( ComingOut co : comingOutList ){
			if( co.agentNo == agentNo ){
				if( Common.compareTalkID(co.commingOutTalk.getDay(), co.commingOutTalk.getIdx(), day, talkID) == -1 ){
					ret = co.role;
				}else{
					break;
				}
			}
		}

		return ret;

	}


	/**
	 * CO者のリストを取得する（指定発言の直前時点で有効なCOのみ）
	 * @param role 取得するCO者の役職
	 * @param day 日
	 * @param talkID 発言ID
	 * @return CO者のエージェント番号のリスト
	 */
	public List<Integer> getEnableCOAgentNo(Role role, int day, int talkID) {

		List<Integer> ret = new ArrayList<Integer>();

		// COの走査
		for( ComingOut co : comingOutList ){
			// 指定した役職かつ有効なCOか
			if( co.role == role && co.isEnable(day, talkID) ){
				// 結果リストに追加
				ret.add(co.agentNo);
			}
		}

		return ret;
	}


	/**
	 * 占い判定の登録
	 * @param judge 判定
	 * @param talk 判定発言
	 */
	private void addSeerJudge(Judge judge, Talk talk){

		// 判定一覧に登録
		seerJudgeList.add(judge);

		if( isValidAgentNo(judge.targetAgentNo) ){
			// 占い結果からのパターン絞り込み(決め打ち情報)
			allViewTrustInfo.removePatternFromJudge( judge.agentNo, judge.targetAgentNo, judge.result );
		}else{
			judge.cancelTalk = talk;
		}

	}


	/**
	 * 霊能判定の登録
	 * @param judge 判定
	 * @param talk 判定発言
	 */
	private void addMediumJudge(Judge judge, Talk talk){

		// 判定一覧に登録
		mediumJudgeList.add(judge);

		if( isValidAgentNo(judge.targetAgentNo) &&
		    agentState[judge.targetAgentNo].causeofDeath == CauseOfDeath.EXECUTED ){
			// 霊能結果からのパターン絞り込み(決め打ち情報)
			allViewTrustInfo.removePatternFromJudge( judge.agentNo, judge.targetAgentNo, judge.result );
		}else{
			judge.cancelTalk = talk;
		}

	}


	/**
	 * 占判定のリストを取得する（無効な判定も含む）
	 * @return
	 */
 	public List<Judge> getSeerJudgeList() {
		return seerJudgeList;
	}


	/**
	 * 霊判定のリストを取得する（無効な判定も含む）
	 * @return
	 */
	public List<Judge> getMediumJudgeList() {
		return mediumJudgeList;
	}


	/**
	 * 護衛履歴のリストを取得する（無効な履歴も含む）
	 * @return
	 */
	public List<GuardRecent> getGuardRecentList() {
		return guardRecentList;
	}


	/**
	 * 日付更新で呼ばれたUpdate()か
	 * @return
	 */
	public boolean isDayUpdate(){
		return isDayUpdate;
	}


	/**
	 * 投票予告先を取得する
	 * @param agentNo
	 * @return
	 */
	public Integer getSaidVoteAgent(int agentNo){

		GameInfo gameInfo = latestGameInfo;

		Integer ret = null;

		// 発言の走査
		for( Talk talk : gameInfo.getTalkList() ){
			if( talk.getAgent().getAgentIdx() == agentNo ){
				Utterance utterance = getUtterance(talk.getContent());
				if( utterance.getTopic() == Topic.VOTE ){
					// 投票宣言
					if( gameInfo.getAgentList().contains(utterance.getTarget()) ){
						ret = utterance.getTarget().getAgentIdx();
					}
				}else if( utterance.getTopic() == Topic.AGREE ){
					// 同意の意図が投票宣言
					Utterance refutterance = getMeanFromAgreeTalk( talk, 0 );
					if( refutterance != null && gameInfo.getAgentList().contains(refutterance.getTarget()) ){
						ret = refutterance.getTarget().getAgentIdx();
					}
				}
			}
		}
		return ret;
	}


	/**
	 * 投票予告先を取得する
	 * @param agentNo
	 * @param day
	 * @return
	 */
	public Integer getSaidVoteAgent(int agentNo, int day){

		GameInfo gameInfo = latestGameInfo;

		Integer ret = null;

		// 発言の走査
		for( Talk talk : talkLists.get(day) ){
			if( talk.getAgent().getAgentIdx() == agentNo ){
				Utterance utterance = getUtterance(talk.getContent());
				if( utterance.getTopic() == Topic.VOTE ){
					// 投票宣言
					if( gameInfo.getAgentList().contains(utterance.getTarget()) ){
						ret = utterance.getTarget().getAgentIdx();
					}
				}else if( utterance.getTopic() == Topic.AGREE ){
					// 同意の意図が投票宣言
					Utterance refutterance = getMeanFromAgreeTalk( talk, 0 );
					if( refutterance != null && gameInfo.getAgentList().contains(refutterance.getTarget()) ){
						ret = refutterance.getTarget().getAgentIdx();
					}
				}
			}
		}
		return ret;
	}


	/**
	 * 生存/死亡状態を取得する
	 * @param AgentNo
	 * @param day
	 * @return
	 */
	public CauseOfDeath getCauseOfDeath( int AgentNo, int day ){

		// 指定された日に死亡済 → 死因を返す
		if( agentState[AgentNo].deathDay != null && agentState[AgentNo].deathDay <= day ){
			return agentState[AgentNo].causeofDeath;
		}

		// 指定された日に生存 → 生存を返す
		return CauseOfDeath.ALIVE;

	}


	/**
	 * agree発言の意味を取得する
	 * @param talk agree発言
	 * @param depth ネストの深さ（外部からの呼び出し時は0を指定）
	 * @return agree発言の意味(解析不能時、agree発言以外を指定時はnull)
	 */
	public Utterance getMeanFromAgreeTalk( Talk talk, int depth ){

		Utterance utterance = getUtterance(talk.getContent());

		// 引数の発言がAGREE以外か
		if( utterance.getTopic() != Topic.AGREE ){
			return null;
		}

		// 発言の種類のチェック(不可視発言への同意)
		if( utterance.getTalkType() == TalkType.WHISPER ){
			// 解析不能
			return null;
		}

		// 時系列のチェック(現在～未来の発言への同意)
		if( Common.compareTalkID( utterance.getTalkDay(), utterance.getTalkID(), talk.getDay(), talk.getIdx() ) >= 0 ){
			// 解析不能
			return null;
		}

		// 参照先の発言の取得
		Talk refTalk = getTalk( utterance.getTalkDay(), utterance.getTalkID() );

		// 参照先が見つからない場合
		if( refTalk == null ){
			// 解析不能
			return null;
		}

		Utterance refutterance = getUtterance(refTalk.getContent());
		switch( refutterance.getTopic() ){
			case ESTIMATE:
				// 参照先と同じ発言をしたと解釈する
				return refutterance;
			case VOTE:
				// 参照先と同じ発言をしたと解釈する
				return refutterance;
			case AGREE:
				// 参照が深すぎれば解決不可とする
				if( depth >= 10 ){
					return null;
				}
				// 更に参照し、参照先と同じ発言をしたと解釈する
				return getMeanFromAgreeTalk(refTalk, depth + 1);
			case DISAGREE:
				// 意図が不明確すぎるので、現状解析不能とする
				break;
			default:
				break;
		}

		// 解析不能
		return null;

	}


	/**
	 * 指定エージェントが指定日時までに占黒判定を受けているか
	 * @param agentNo エージェント番号
	 * @param day 日
	 * @param talkID 発言ID
	 * @return
	 */
	public boolean isReceiveWolfJudge( int agentNo, int day, int talkID ){

		for( Judge judge : getSeerJudgeList() ){
			// 時系列のチェック（判定が指定日時より前か）
			if( Common.compareTalkID( judge.talk.getDay(), judge.talk.getIdx(), day, talkID) == -1 ){
				// 対象者への人狼判定か
				if( judge.targetAgentNo == agentNo && judge.result == Species.WEREWOLF ){
					return true;
				}
			}
		}

		return false;

	}


	/**
	 * 最新日に自分が発したTalkの回数を取得する
	 * @return 最新日に自分が発したTalkの回数
	 */
	public int getMyTalkNum(){

		int count = 0;

		for( Talk talk : latestGameInfo.getTalkList() ){
			if( talk.getAgent().equals(latestGameInfo.getAgent()) ){
				count++;
			}
		}

		return count;

	}


	/**
	 * エージェント番号の妥当性チェック
	 * @param agentno
	 * @return
	 */
	public boolean isValidAgentNo(int agentno){

		if( agentno <= 0 || agentno > gameSetting.getPlayerNum() ){
			return false;
		}

		return true;

	}


	/**
	 * 発言解析内容の取得（キャッシュ利用で高速）
	 * @param talkContent 発言の内容
	 * @return
	 */
	public Utterance getUtterance(String talkContent){

		Utterance ret;

		// 過去の解析済み情報に登録済か
		if( analysedUtteranceMap.containsKey(talkContent) ){

			// 過去の解析済み情報から取得する
			ret = analysedUtteranceMap.get(talkContent);

		}else{

			try{
				ret = new Utterance(talkContent);

				// 不正な発話はskipに変換する
				if( ret == null ){
					ret = new Utterance(TemplateTalkFactory.skip());
				}else{
					switch( ret.getTopic() ){
						case ESTIMATE:
							if( ret.getTarget() == null || ret.getRole() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case COMINGOUT:
							if( ret.getTarget() == null || ret.getRole() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case DIVINED:
							if( ret.getTarget() == null || ret.getResult() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case IDENTIFIED:
							if( ret.getTarget() == null || ret.getResult() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case GUARDED:
							if( ret.getTarget() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case VOTE:
							if( ret.getTarget() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case ATTACK:
							if( ret.getTarget() == null ){
								ret = new Utterance(TemplateTalkFactory.skip());
							}
							break;
						case AGREE:
							break;
						case DISAGREE:
							break;
						case OVER:
							break;
						case SKIP:
							break;
						default:
							ret = new Utterance(TemplateTalkFactory.skip());
					}

				}
			}catch(Exception ex){
				ret = new Utterance(TemplateTalkFactory.skip());
			}

			// 解析済み情報に登録する
			analysedUtteranceMap.put(talkContent, ret);

		}

		return ret;

	}


	/**
	 * 占い判定を追加する（騙り用）
	 * @param judge
	 */
	public void addFakeSeerJudge(Judge judge){

		selfInspectList.add(judge);

	}


	/**
	 * 人狼疑い度を取得する（狂人疑いは無視する）
	 * @param agentNo エージェント番号
	 * @param targetNo 対象エージェント番号
	 * @return 0.0(最白)～1.0(最黒)
     */
	public double getSuspicionWerewolfRate(int agentNo, int targetNo){

		HashSet<Integer> fixWhiteList = new HashSet<Integer>();
		HashSet<Integer> fixBlackList = new HashSet<Integer>();
		HashSet<Integer> suspicionList = new HashSet<Integer>();
		HashSet<Integer> trustList = new HashSet<Integer>();


		// 確定白・黒（全視点情報から）
		for( Agent agent : latestGameInfo.getAliveAgentList() ){
			if( agent.getAgentIdx() == agentNo ){
				continue;
			}
			if( allViewTrustInfo.isFixWhite(agent.getAgentIdx()) ){
				fixWhiteList.add(agent.getAgentIdx());
			}else if( allViewTrustInfo.isFixBlack(agent.getAgentIdx()) ){
				fixBlackList.add(agent.getAgentIdx());
			}
		}

		// 確定白・黒（占い師の判定から）
		for( Judge judge : getSeerJudgeList() ){
			if( judge.isEnable() && judge.agentNo == agentNo ){
				if( agentState[judge.targetAgentNo].causeofDeath == CauseOfDeath.ALIVE ){
					if( judge.result == Species.HUMAN ){
						fixWhiteList.add(judge.targetAgentNo);
					}else{
						fixBlackList.add(judge.targetAgentNo);
					}
				}
			}
		}

		// 発言からの疑い先
		for( Talk talk : latestGameInfo.getTalkList() ){
			if( talk.getAgent().getAgentIdx() == agentNo ){
				Utterance ut = getUtterance(talk.getContent());
				if( ut.getTopic() == Topic.AGREE ){
					ut = getMeanFromAgreeTalk(talk, 0);
					if( ut == null ){
						continue;
					}
				}
				// 予想発言
				if( ut.getTopic() == Topic.ESTIMATE ){
					// 生存者に対する予想のみ処理
					if( latestGameInfo.getAliveAgentList().contains(ut.getTarget()) ){
						if( ut.getRole() == Role.WEREWOLF ){
							// 疑い
							trustList.remove(ut.getTarget().getAgentIdx());
							suspicionList.add(ut.getTarget().getAgentIdx());
						}else if( ut.getRole().getTeam() == Team.VILLAGER ) {
							// 信用
							suspicionList.remove(ut.getTarget().getAgentIdx());
							trustList.add(ut.getTarget().getAgentIdx());
						}
					}
				}
			}
		}

		// 疑い先が0の場合
		if( suspicionList.isEmpty() ){
			// 投票宣言先を疑い先ということにする（暫定処置）
			Integer voteTarget = getSaidVoteAgent( agentNo, latestGameInfo.getDay() );
			if( voteTarget != null && agentState[voteTarget].causeofDeath == CauseOfDeath.ALIVE ){
				if( !fixWhiteList.contains(voteTarget) && !fixBlackList.contains(voteTarget) && !trustList.contains(voteTarget) ){
					suspicionList.add(voteTarget);
				}
			}else if( !latestGameInfo.getVoteList().isEmpty() ){
				// 前日投票先を疑い先ということにする（暫定処置）
				for( Vote vote : latestGameInfo.getVoteList() ){
					if( vote.getAgent().getAgentIdx() == agentNo ){
						voteTarget = vote.getTarget().getAgentIdx();
					}
				}
				if( voteTarget != null && agentState[voteTarget].causeofDeath == CauseOfDeath.ALIVE ){
					if( !fixWhiteList.contains(voteTarget) && !fixBlackList.contains(voteTarget) && !trustList.contains(voteTarget) ){
						suspicionList.add(voteTarget);
					}
				}
			}
		}

		// 確定白・黒は疑い度固定
		if( fixWhiteList.contains(targetNo) ){
			return 0.0;
		}
		if( fixBlackList.contains(targetNo) ){
			return 1.0;
		}

		// 生存している他人の人数
		int aliveOtherCount = latestGameInfo.getAliveAgentList().size() - 1;

		// 生存している自分を除くグレーの人数
		int aliveGrayCount = aliveOtherCount - fixWhiteList.size() - fixBlackList.size();

		// 位置
		int minPosition = 0;
		int maxPosition = aliveOtherCount - 1;

		minPosition += fixBlackList.size();
		maxPosition -= fixWhiteList.size();

		if( suspicionList.contains(targetNo) ){
			// 絶対評価で疑われている
			maxPosition -= aliveGrayCount - suspicionList.size();
		}else if( trustList.contains(targetNo) ){
			// 絶対評価で信用されている
			minPosition += aliveGrayCount - trustList.size();
		}else{
			// 絶対評価でどちらでもない
			minPosition += suspicionList.size();
			maxPosition -= trustList.size();
		}

		return 1.0 - ((minPosition + maxPosition) / 2.0 / (aliveOtherCount-1));

	}


	/**
	 * 指定したエージェントが狼か（自分が狼の時用）
	 * @param agentNo
	 * @return
	 */
	public boolean isWolf(int agentNo){

		GameInfo gameInfo = latestGameInfo;

		Role role = gameInfo.getRoleMap().get(Agent.getAgent(agentNo));

		if( role == Role.WEREWOLF ){
			return true;
		}

		return false;

	}


	/**
	 * 狼の一覧を返す（自分が狼の時用）
	 * @return
	 */
	public List<Integer> getWolfList(){

		GameInfo gameInfo = latestGameInfo;

		List<Integer> ret = new ArrayList<Integer>();

		for( Agent agent : gameInfo.getAgentList() ){
			Role role = gameInfo.getRoleMap().get(agent);
			if( role == Role.WEREWOLF ){
				ret.add(agent.getAgentIdx());
			}
		}

		return ret;

	}


	/**
	 * 生存している狼の一覧を返す（自分が狼の時用）
	 * @return
	 */
	public List<Integer> getAliveWolfList(){

		GameInfo gameInfo = latestGameInfo;

		List<Integer> ret = new ArrayList<Integer>();

		for( Agent agent : gameInfo.getAgentList() ){
			Role role = gameInfo.getRoleMap().get(agent);
			if( role == Role.WEREWOLF && agentState[agent.getAgentIdx()].causeofDeath == CauseOfDeath.ALIVE ){
				ret.add(agent.getAgentIdx());
			}
		}

		return ret;

	}


	/**
	 * PPが可能かを取得する（自分が狼の時用）
	 * @return
	 */
	public boolean isEnablePowerPlay(){

		//TODO 村側のPP返し対策やPP偽装対策が必要
		//TODO 真狂両生存時の判定、狩狂の判定


		// 残り処刑数が生存狼数より多ければPPは発生しない
		if( Common.getRestExecuteCount(latestGameInfo.getAliveAgentList().size()) > getAliveWolfList().size() ){
			return false;
		}

		// 狂人のエージェント番号
		Integer possessed = null;

		// 狂人を探す
		for( Judge judge : getSeerJudgeList() ){
			// 人間の占い師が間違った判定を出したか
			if( !isWolf(judge.agentNo) &&
			    (judge.result == Species.WEREWOLF) != isWolf(judge.targetAgentNo) ){
				possessed = judge.agentNo;
			}
		}
		for( Judge judge : getMediumJudgeList() ){
			// 人間の霊能者が間違った判定を出したか
			if( !isWolf(judge.agentNo) &&
			    (judge.result == Species.WEREWOLF) != isWolf(judge.targetAgentNo) ){
				possessed = judge.agentNo;
			}
		}

		// 狂人特定済 ＆ 狂人生存
		if( possessed != null && agentState[possessed].causeofDeath == CauseOfDeath.ALIVE ){
			// PP成立
			return true;
		}

		return false;

	}


	/**
	 * PPが可能かを取得する（自分が狂人の時用）
	 * @return
	 */
	public boolean isEnablePowerPlay_Possessed(){

		// 3人以下なら確実にPP
		if( latestGameInfo.getAliveAgentList().size() <= 3 ){
			return true;
		}

		// 生存者が8人以下
		if( latestGameInfo.getAliveAgentList().size() <= 8 ){
			// 人外COを行った人物を取得
			for( ComingOut co : wolfsideComingOutList ){
				// 有効なCO かつ 自分狂視点で確白ではない
				if( co.isEnable() && !selfRealRoleViewInfo.isFixWhite(co.agentNo) ){
					// PP成立と判断
					return true;
				}
			}
		}

		return false;

	}

}
