package com.gmail.k14.itolab.aiwolf.data;

import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * インスタンス化したオブジェクトを管理するクラス
 * @author k14096kk
 *
 */
public class EntityData {
	
	/**CO履歴*/
	CoHistory coHistory;
	/**予想一覧*/
	ForecastMap forecastMap;
	/**発言キュー*/
	MyTalking myTalking;
	/**ゲームデータ*/
	OwnData ownData;
	/**発言データベース*/
	TalkDataBase talkDataBase;
	/**ターン*/
	Turn gameTurn;
	/**投票発言のカウンタ*/
	VoteCounter voteCounter;
	/**囁きキュー*/
	MyTalking myWhisper;
	/**サブ発言キュー*/
	MyTalking subTalking;
	
	
	/**
	 * オブジェクト生成
	 * @param paramGameInfo :ゲーム情報
	 * @param paramGameSetting :ゲーム設定
	 */
	public EntityData(GameInfo paramGameInfo, GameSetting paramGameSetting) {
		this.coHistory = new CoHistory(paramGameInfo.getAgentList());
		this.ownData = new OwnData(paramGameInfo, paramGameSetting);
		this.forecastMap = new ForecastMap(ownData);
		this.myTalking = new MyTalking();
		this.talkDataBase = new TalkDataBase(paramGameInfo.getAgentList());
		this.gameTurn = new Turn();
		this.voteCounter = new VoteCounter();
		this.subTalking = new MyTalking();
		this.myWhisper = new MyTalking();
	}
	
	/**
	 * CoHistoryオブジェクト取得
	 * @return CoHistory
	 */
	public CoHistory getCoHistory() {
		return this.coHistory;
	}
	
	/**
	 * CoHistoryの更新
	 * @param coHistory :CO履歴
	 */
	public void setCoHistory(CoHistory coHistory) {
		this.coHistory = coHistory;
	}
	
	/**
	 * OwnDataオブジェクト取得
	 * @return OwnData
	 */
	public OwnData getOwnData() {
		return this.ownData;
	}
	
	/**
	 * OwnDataの更新
	 * @param ownData :ゲームデータ
	 */
	public void setOwnData(OwnData ownData) {
		this.ownData = ownData;
	}
	
	/**
	 * ForecastMapオブジェクト取得
	 * @return ForecastMap
	 */
	public ForecastMap getForecastMap() {
		return this.forecastMap;
	}
	
	/**
	 * ForecastMapの更新
	 * @param forecastMap :予想一覧
	 */
	public void setForecastMap(ForecastMap forecastMap) {
		this.forecastMap = forecastMap;
	}
	
	/**
	 * TalkDataBaseオブジェクト取得
	 * @return TalkDataBase
	 */
	public TalkDataBase getTalkDataBase() {
		return this.talkDataBase;
	}
	
	/**
	 * TalkDataBaseの更新
	 * @param talkDataBase :会話データベース
	 */
	public void setTalkDataBase(TalkDataBase talkDataBase) {
		this.talkDataBase = talkDataBase;
	}
	
	/**
	 * MyTalkingオブジェクト取得
	 * @return MyTalking
	 */
	public MyTalking getMyTalking() {
		return this.myTalking;
	}
	
	/**
	 * MyTalkingの更新
	 * @param myTalking :発言キュー
	 */
	public void setMyTalking(MyTalking myTalking) {
		this.myTalking = myTalking;
	}
	
	/**
	 * Turnオブジェクト取得
	 * @return Turn
	 */
	public Turn getTurn() {
		return this.gameTurn;
	}
	
	/**
	 * Turn更新
	 * @param turn :ターン
	 */
	public void setTurn(Turn turn) {
		this.gameTurn = turn;
	}
	
	/**
	 * VoteCounterオブジェクト取得
	 * @return VoteCounter
	 */
	public VoteCounter getVoteCounter() {
		return this.voteCounter;
	}
	
	/**
	 * VoteCounterの更新
	 * @param voteCounter :投票回数
	 */
	public void setVoteCounter(VoteCounter voteCounter) {
		this.voteCounter = voteCounter;
	}
	
	/**
	 * MyWhisperオブジェクト取得
	 * @return MyWhisper
	 */
	public MyTalking getMyWhisper() {
		return this.myWhisper;
	}
	
	/**
	 * MyWhisperの更新
	 * @param myWhisper :囁きキュー
	 */
	public void setMyWhisper(MyTalking myWhisper) {
		this.myWhisper = myWhisper;
	}
	
	/**
	 * SubTalkingオブジェクト取得
	 * @return SubTalking
	 */
	public MyTalking getSubTalking() {
		return this.subTalking;
	}
	
	/**
	 * SubTalkingオブジェクトの更新
	 * @param subTalking :サブ発話キュー
	 */
	public void setSubTalking(MyTalking subTalking) {
		this.subTalking = subTalking;
	}
}
