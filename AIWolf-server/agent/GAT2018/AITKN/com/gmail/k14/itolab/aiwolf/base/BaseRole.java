package com.gmail.k14.itolab.aiwolf.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.action.CommonAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.data.GameResult;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.Debug;
import com.gmail.k14.itolab.aiwolf.util.OperatorElement;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 役職のベースとなるクラス
 * 各役職はこのクラスを継承して実装する
 * @author k14096kk
 *
 */
public class BaseRole implements Player {

	/**オブジェクト管理データ*/
	protected EntityData entityData;
	/**共通行動*/
	protected CommonAction commonAction;
	/**役職行動*/
	protected BaseRoleAction action;
	/**ゲーム結果*/
	protected GameResult gameResult;

	/** どこまで会話を読んだか */
	protected int readTalkNum;
	/** 最初に通過するfinishフラグ */
	protected boolean passageFinish;
	/** 囁き中であるかどうか */
	protected boolean isWhisper;
	/**エラーが起きたかどうか*/
	protected boolean isError;
	/**エラーの表示の有無<br>isDebugがfalseのときのみ使用*/
	protected boolean isCheckError = false;
	
	/**
	 * コンストラクタでGameResultを設定
	 * @param gameResult :ゲーム結果
	 */
	public BaseRole(GameResult gameResult) {
		this.gameResult = gameResult;
	}

	@Override
	public String getName() {
		return "AITKN";
	}

	@Override
	public void initialize(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		try {
			// インスタンス生成
			entityData = new EntityData(paramGameInfo, paramGameSetting);
			commonAction = new CommonAction(entityData);
			action = new BaseRoleAction(entityData);
			
			// 戦略一覧作成
			gameResult.createStrategyMap(entityData.getOwnData().getMyRole());
			// 戦略ランダム選択
			entityData.getOwnData().setStrategy(gameResult.selectStrategy(entityData.getOwnData().getMyRole()));

			// 会話読み取り回数初期化
			readTalkNum = 0;
			// finish通過フラグ
			passageFinish = false;
			// 囁き中フラグ
			isWhisper = false;
			
		} catch (Exception e) {
			this.writeEx(e);
		}
		
	}

	@Override
	public void update(GameInfo paramGameInfo) {
		try {
			// データ更新
			entityData.getOwnData().setDataUpdate(paramGameInfo);
			entityData.getForecastMap().setDataUpdate(entityData.getOwnData());
			this.action.setDataUpdate(entityData);
			
			// 処刑情報と襲撃情報を更新
			this.commonAction.dayStart();

			// 会話リスト取得
			List<Talk> talkList = entityData.getOwnData().getTalkList();
			// 囁きリスト取得
			List<Talk> whisperList = entityData.getOwnData().getGameInfo().getWhisperList();

			// 1日のはじめの発言(0ターン目の前)
			if (entityData.getTurn().startTurn()) {
				// 0日目ならば囁き中へ変更
				if (Check.isNum(entityData.getOwnData().getDay(), 0)) {
					isWhisper = true;
				}
				if (!isWhisper) { // 普通の発言
					// 行動選択
					this.action.selectAction();
					// ターン更新
					entityData.getTurn().updateTurn();
					
				}else { // 囁き
					entityData.getMyTalking().clearTalk();
					// 読み回数初期化
					readTalkNum = 0;
					// 囁き行動選択
					this.action.selectWhisperAction();
					// ターン更新
					entityData.getTurn().updateTurn();
				}
			}

			// 話し合い中
			if (!isWhisper) {
				
				// 一日の間にどこまで読みこんだのかを取得し，for文をまわす．
				for (int i = readTalkNum; i < talkList.size(); i++) {
					// 発言取得
					Talk talk = talkList.get(i);
					// コンテンツ取得
					Content content = new Content(talk.getText());
					// 共通行動呼び出し
					commonAction.talkControl(talk, content);

					// 会話データベースに会話格納
					entityData.getTalkDataBase().addTalk(entityData.getOwnData().getDay(), talk.getAgent(), talk);
					// 読み回数増加
					readTalkNum++;

					// 発言に対する行動(急を要する発言みたいな)
					this.action.talkAction(talk, content);

					// リクエストに対する行動
					if (OperatorElement.isRequest(content)) {
						this.action.requestAction(talk, content, OperatorElement.getRequestContent(content));
					}

					// 最後の会話まで読んでから戦略に乗っ取った発言と行動
					if (readTalkNum == (talkList.size() - 1)) {
						// エラーじゃなければ行動を起こす
						if(!isError) {
							this.action.selectAction();
						}
						// ターン更新
						entityData.getTurn().updateTurn();

						// 全員OVERならば囁き中に変更 ターンを0に戻す
						if (isEndTalk(talkList)) {
							isWhisper = true;
							
							entityData.getTurn().resetTurn();
						}
					}
				}
			} else { // 囁き中
				
//				// 囁き0ターン前
//				if (entityData.getTurn().startTurn()) {
//					entityData.getMyTalking().clearTalk();
//					// 読み回数初期化
//					readTalkNum = 0;
//					this.action.selectWhisperAction();
//					entityData.getTurn().updateTurn();
//				}

				// 一日の間にどこまで読みこんだのかを取得し，for文をまわす．
				for (int i = readTalkNum; i < whisperList.size(); i++) {
					// 発言取得
					Talk talk = whisperList.get(i);
					// 発言をコンテキスト型として取得
					Content content = new Content(talk.getText());
					// 共通行動呼び出し
					/* commonAction.talkAct(talk, content); */

					// 会話データベースに会話格納
					/*
					 * TalkDataBase.addTalk(ownData.getDay(), talk.getAgent(),
					 * talk);
					 */
					// 読み回数増加
					readTalkNum++;

					// 発言に対する行動(急を要する発言みたいな)
					this.action.whisperAction(talk, content);

					// リクエストに対する行動
					if (OperatorElement.isRequest(content)) {
						this.action.requestWhisperAction(talk, content, OperatorElement.getRequestContent(content));
					}

					// 最後の会話まで読んでから戦略に乗っ取った発言と行動
					if (readTalkNum == (whisperList.size() - 1)) {
						// エラーじゃなければ行動を起こす
						if(!isError) {
							this.action.selectWhisperAction();
						}
						// ターン更新
						entityData.getTurn().updateTurn();
						
						// 全員OVERならば囁き中に変更 ターンを0に戻す
						if (isEndWhisper(whisperList)) {
							isWhisper = false;
							entityData.getTurn().resetTurn();
						}
					}
				}
			}

		} catch (Exception e) {
			this.writeEx(e);
			// エラーが起きれば，エラーフラグ立ててOVER
			isError = true;
			entityData.getMyTalking().addTalk(TalkFactory.overRemark());
		}
	}

	@Override
	public void dayStart() {
		try {
			this.dataReset();
			this.setRoleData();
			this.action.dayStart();
			// ターンリセット
			entityData.getTurn().resetTurn();
			// エラーフラグ解除
			isError = false;
		} catch (Exception e) {
			this.writeEx(e);
		}
		
	}

	@Override
	public String talk() {
		String remark = entityData.getMyTalking().getTalk();
		// リアクション許可状態かつSKIPかつサブトークに発言があれば発言
		if(entityData.getOwnData().isReaction() && remark.equals(TalkFactory.skipRemark())) {
			remark = entityData.getSubTalking().getTalk();
		}
		Debug.print("発言 = " + remark);
		return remark;
	}

	@Override
	public String whisper() {
		String remark = entityData.getMyWhisper().getTalk();
		Debug.print("囁き = " + remark);
		return remark;
	}

	@Override
	public Agent vote() {
		try {
			action.vote();
			// 投票処理までいったら強制的にfinish状態へ変更
			entityData.getOwnData().setActFlagFinish();
		} catch (Exception e) {
			this.writeEx(e);
		}
		// 投票対象
		Agent voteTarget = entityData.getOwnData().getVoteTarget();
		
		// 投票対象が決まっていなければ生存者の中からランダム
		if(Check.isNull(voteTarget)) {
			voteTarget = RandomSelect.randomAgentSelect(entityData.getOwnData().getAliveOtherAgentList());
		}
		// 前日の投票対象として登録
		entityData.getOwnData().setPreviousVoteTarget(voteTarget);
		
		Debug.print(entityData.getOwnData().getDay() + "Day MyVote = " + voteTarget);
		
		return voteTarget;
	}

	@Override
	public Agent attack() {
		try {
			action.attack();
		} catch (Exception e) {
			this.writeEx(e);
		}
		// 襲撃対象
		Agent attackTarget = entityData.getOwnData().getAttackTarget();
		
		// 襲撃対象が決まっていなければ人狼以外の生存者の中からランダム
		if(Check.isNull(attackTarget)) {
			List<Agent> attackCandidates = entityData.getOwnData().getAliveOtherAgentList();
			attackCandidates.removeAll(entityData.getForecastMap().getConfirmRoleAgentList(Role.WEREWOLF));
			attackTarget = RandomSelect.randomAgentSelect(attackCandidates);
		}
		// 前日の自分の襲撃予定対象として登録
		entityData.getOwnData().setPreviousAttackTarget(entityData.getOwnData().getGameInfo().getAttackedAgent());
		
		Debug.print(entityData.getOwnData().getDay() + "Day MyAttack = " + attackTarget);
		
		return attackTarget;
	}

	@Override
	public Agent divine() {
		try {
			action.divine();
		} catch (Exception e) {
			this.writeEx(e);
		}
		// 占い対象
		Agent divineTarget = entityData.getOwnData().getDivineTarget();
		
		// 占い対象が決まっていなければ占っていない生存者の中からランダム
		if(Check.isNull(divineTarget)) {
			List<Agent> remainList = entityData.getOwnData().remainDivineAgentList(entityData.getOwnData().getAliveOtherAgentList());
			divineTarget = RandomSelect.randomAgentSelect(remainList);
		}
		// 前日の占い対象として登録
		entityData.getOwnData().setPreviousDivineTarget(divineTarget);
		
		Debug.print(entityData.getOwnData().getDay() + "Day MyDivine = " + divineTarget);
		
		return divineTarget;
	}

	@Override
	public Agent guard() {
		try {
			action.guard();
		} catch (Exception e) {
			this.writeEx(e);
		}
		// 護衛対象
		Agent guardTarget = entityData.getOwnData().getGuardTarget();
		
		// 護衛対象が決まっていなければ生存者の中からランダム
		if(Check.isNull(guardTarget)) {
			guardTarget = RandomSelect.randomAgentSelect(entityData.getOwnData().getAliveOtherAgentList());
		}
		// 前日の護衛対象として登録
		entityData.getOwnData().setPreviousGuardTarget(guardTarget);
		
		Debug.print("護衛対象 = " + guardTarget);
		
		return guardTarget;
	}

	@Override
	public void finish() {
		try {
			entityData.getMyTalking().clearTalk();
			entityData.getTalkDataBase().clearAllTalkData();
			if (passageFinish) {
				gameResult.setGameResult(entityData);
				// デバッグ表示
				debugFinish();
				// デバッグ表示なしの際にエラーのみを表示する
				if(isCheckError) {
					Debug.showError();
				}
			}
		} catch (Exception e) {
			this.writeEx(e);
		}
		entityData.getOwnData().dataReset();
		passageFinish = true;
	}

	/**
	 * talkの終了判断
	 * @param talks :発言リスト
	 * @return talk終了(全員がOVER)していればtrue,一人でもしていなければfalse
	 */
	public boolean isEndTalk(List<Talk> talks) {

		for (int i = 0; i < entityData.getOwnData().getAliveAgentList().size(); i++) {
			int num = readTalkNum - i;
			Talk talk = talks.get(num);
			Content content = new Content(talk.getText());
			if (!Check.isTopic(content.getTopic(), Topic.OVER)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * whisperの終了判断
	 * @param talks :囁きリスト
	 * @return talk終了(全員がOVER)していればtrue,一人でもしていなければfalse
	 */
	public boolean isEndWhisper(List<Talk> talks) {
		
		List<Agent> wolfList = entityData.getForecastMap().getConfirmRoleAgentList(Role.WEREWOLF);
		List<Agent> deadList = entityData.getOwnData().getDeadAgentList();
		
		// 人狼陣営から死亡者を排除
		wolfList.removeAll(deadList);
		
		for (int i = 0; i < wolfList.size(); i++) {
			int num = readTalkNum - i;
			Talk talk = talks.get(num);
			Content content = new Content(talk.getText());
			if (!Check.isTopic(content.getTopic(), Topic.OVER)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 盤面整理
	 */
	public void decideBoat() {
		entityData.getOwnData().getBoardArrange().update(entityData.getForecastMap(), entityData.getOwnData().getAliveAgentList(), entityData.getOwnData().getDay());
	}

	/**
	 * データ初期化
	 */
	public void dataReset() {
		this.readTalkNum = 0;
		this.isWhisper = false;
		this.commonAction.dayStart();
		entityData.getOwnData().updateDayStart();
		entityData.getTalkDataBase().resetTalkData();
		entityData.getMyTalking().clearTalk();
		entityData.getMyWhisper().clearTalk();
		entityData.getSubTalking().clearTalk();
		entityData.getVoteCounter().clearVoteCount();
		Debug.show(entityData, gameResult);
//		if (entityData.getOwnData().isFinish()) {
//			 発言終了状態ならば発話リストとデータを初期化
		entityData.getOwnData().dataReset();
//			decideBoat();	
//		}
	}
	
	/**
	 * 役職ごとにそれぞれの能力結果のデータを格納する
	 */
	public void setRoleData() {
		
		// 狩人は自身の護衛した結果を格納
		if(Check.isRole(entityData.getOwnData().getMyRole(), Role.BODYGUARD)) {
			// 前日の護衛先を取得
			Agent target = entityData.getOwnData().getGameInfo().getGuardedAgent();
			// 護衛していれば，護衛結果を登録
			if (Check.isNotNull(target)) {
				boolean success = false;
				// 前日の襲撃対象が存在しなければ護衛成功
				if (Check.isNull(entityData.getOwnData().getAttackedAgent())) {
					success = true;
				}
				int day = entityData.getOwnData().getDay() - 1;
				entityData.getOwnData().setGuardResultMap(day, target, success);

			}
		}
		
		// 人狼は襲撃した結果を格納
		if(Check.isRole(entityData.getOwnData().getMyRole(), Role.WEREWOLF)) {
			// 前日の襲撃先を取得
			Agent target = entityData.getOwnData().getGameInfo().getAttackedAgent();
			// 襲撃成功フラグ
			boolean success = false;

			// 前日に襲撃しており，襲撃対象が死亡していれば襲撃成功とする
			if (Check.isNotNull(target)) {
				if (entityData.getOwnData().getGameInfo().getStatusMap().get(target) == Status.DEAD) {
					success = true;
				}

				// 前日の襲撃情報を登録する
				int day = entityData.getOwnData().getDay() - 1;
				entityData.getOwnData().setAttackResultMap(day, target, success);
			}
		}
		
	}

	/**
	 * finishで呼ぶデバッグ処理
	 */
	public void debugFinish() {
		Debug.print("FINISH");
		Debug.show(entityData, gameResult);
		// 10回ごとに勝利数表示
		if (Check.isNum(gameResult.getGameCount()%10, 0)) {
			Debug.showWin(gameResult);
		}
		Debug.checkPass();
		Debug.showError();
	}
	
	/**
	 * エラー文記述
	 * @param e :エラー
	 */
	public void writeEx(Exception e) {
		StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        Debug.stackError(stringWriter);
	}

}
