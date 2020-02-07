package com.gmail.k14.itolab.aiwolf.base;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.data.CoHistory;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.data.ForecastMap;
import com.gmail.k14.itolab.aiwolf.data.MyTalking;
import com.gmail.k14.itolab.aiwolf.data.OwnData;
import com.gmail.k14.itolab.aiwolf.data.TalkDataBase;
import com.gmail.k14.itolab.aiwolf.data.Turn;
import com.gmail.k14.itolab.aiwolf.data.VoteCounter;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 役職の行動の基礎となるクラス
 * 各自の役職の行動はこのクラスを継承して実装する
 * @author k14096kk
 *
 */
public class BaseRoleAction {
	
	/**オブジェクト管理データ*/
	protected EntityData entityData;
	/**CO履歴*/
	protected CoHistory coHistory;
	/**予想一覧*/
	protected ForecastMap forecastMap;
	/**ゲームデータ*/
	protected OwnData ownData;
	/**発言キュー*/
	protected MyTalking myTalking;
	/**発言データベース*/
	protected TalkDataBase talkDataBase;
	/**ターン*/
	protected Turn turn;
	/**投票発言のカウンタ*/
	protected VoteCounter voteCounter;
	/**サブ発言キュー*/
	protected MyTalking subTalking;
	
	/*共通変数*/
	/**占い師COで一度でも登録したら保持しておくリスト*/
	protected List<Agent> entrySeerCoList = new ArrayList<>();
	/**霊媒師COで一度でも登録したら保持しておくリスト*/
	protected List<Agent> entryMediumCoList = new ArrayList<>();
	
	
	
	/**
	 * 行動作成
	 * @param entityData :オブジェクト管理データ
	 */
	public BaseRoleAction(EntityData entityData) {
		// オブジェクト取得
		this.getEntityData(entityData);
		// 共通行動許可
		ownData.rejectReaction();
	}
	
	/**
	 * ゲーム情報と自分の情報の更新
	 * @param entityData :オブジェクト管理データ
	 */
	public void setDataUpdate(EntityData entityData) {
		this.entityData = entityData;
		getEntityData(entityData);
	}
	
	/**
	 * 発話終了状態ならばOVER
	 */
	public void finishTalk() {
		//発話終了状態ならばOVER
		if(ownData.isFinish()) {
			myTalking.addTalk(TalkFactory.overRemark());
		}
	}
	
	/**
	 * 一日のはじめの行動
	 */
	public void dayStart() {
		getEntityData(entityData);
	}
	
	/**
	 * 投票時に呼べる処理
	 */
	public void vote() {
		getEntityData(entityData);
	}
	
	/**
	 * 占い時に呼べる処理
	 */
	public void divine() {
		getEntityData(entityData);
	}
	
	/**
	 * 護衛対象選択時に呼べる処理
	 */
	public void guard() {
		getEntityData(entityData);
	}
	
	/**
	 * 襲撃投票時に呼べる処理
	 */
	public void attack() {
		getEntityData(entityData);
	}
	
	/**
	 * 戦略により選択する行動
	 */
	public void selectAction() {
		getEntityData(entityData);
	}
	
	/**
	 * 囁きでの選択行動(人狼専用)
	 */
	public void selectWhisperAction() {
		getEntityData(entityData);
	}
	
	/**
	 * リクエストに対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 * @param reqContent :リクエストコンテンツ
	 */
	public void requestAction(Talk talk, Content content, Content reqContent) {
		getEntityData(entityData);
	}
	
	/**
	 * 他人の発言に対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public void talkAction(Talk talk, Content content) {
		getEntityData(entityData);
	}
	
	/**
	 * 他人の囁きに対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 */
	public void whisperAction(Talk talk, Content content) {
		getEntityData(entityData);
	}
	
	/**
	 * リクエストの囁きに対する行動
	 * @param talk :発言
	 * @param content :コンテンツ
	 * @param reqContent :リクエストコンテンツ
	 */
	public void requestWhisperAction(Talk talk, Content content, Content reqContent) {
		getEntityData(entityData);
	}
	
	
	/**
	 * EntityDataからオブジェクトの取得
	 * @param entityData :オブジェクト管理データ
	 */
	public void getEntityData(EntityData entityData) {
		this.entityData = entityData;
		this.coHistory = entityData.getCoHistory();
		this.forecastMap = entityData.getForecastMap();
		this.ownData = entityData.getOwnData();
		this.myTalking = entityData.getMyTalking();
		this.talkDataBase = entityData.getTalkDataBase();
		this.turn = entityData.getTurn();
		this.voteCounter = entityData.getVoteCounter();
		this.subTalking = entityData.getSubTalking(); 
	}
	
	/**
	 * EntityDataへオブジェクトの更新
	 */
	public void setEntityData() {
		entityData.setCoHistory(coHistory);
		entityData.setForecastMap(forecastMap);
		entityData.setMyTalking(myTalking);
		entityData.setOwnData(ownData);
		entityData.setTalkDataBase(talkDataBase);
		entityData.setTurn(turn);
		entityData.setVoteCounter(voteCounter);
		entityData.setSubTalking(subTalking);
	}

}
