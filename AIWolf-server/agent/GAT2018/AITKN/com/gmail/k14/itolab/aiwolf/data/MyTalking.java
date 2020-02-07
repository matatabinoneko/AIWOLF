package com.gmail.k14.itolab.aiwolf.data;

import java.util.ArrayDeque;
import java.util.Deque;

import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 自分の発話用のクラス，発話内容を保管していく
 * @author k14096kk
 *
 */
public class MyTalking {

	/**発話を格納するキュー*/
	 Deque<String> talkQue = new ArrayDeque<String>();
	/**発言済みの1日の発言を格納するキュー*/
	 Deque<String> settledQue = new ArrayDeque<String>();
	 
	public MyTalking() {
		talkQue = new ArrayDeque<String>();
		settledQue = new ArrayDeque<String>();
	}
	
	/**
	 * キューを取得する
	 * @return 発話キュー
	 */
	public Deque<String> getTalkQue() {
		return talkQue;
	}
	
	/**
	 * 発言を最後尾に追加する
	 * @param talk :発言
	 */
	public void addTalk(String talk) {
		if(talk==null) return;
		talkQue.addLast(talk);
	}
	
	/**
	 * 発言を先頭に追加する(最優先の発言)
	 * @param talk :発言
	 */
	public void addTopTalk(String talk) {
		if(talk==null) return;
		talkQue.addFirst(talk);
	}
	
	/**
	 * 発言を削除
	 * @param talk :発言
	 */
	public void removeTalk(String talk) {
		if(talk==null) return;
		talkQue.remove(talk);		
	}
	
	/**
	 * 格納した発言を全削除
	 */
	public void clearTalk() {
		talkQue.clear();
		settledQue.clear();
	}
	
	/**
	 * 格納した発言を先頭から取得(取得発言は削除)
	 * 発言が無ければSKIP
	 * @return 発話する発言
	 */
	public String getTalk() {
		if(talkQue.isEmpty()) {
			return TalkFactory.skipRemark();
		}
		// 発言済みキューに格納
		addSettledTalk(talkQue.peek());
		return talkQue.pop();
	}
	
	/**
	 * 格納した発言を先頭から取得(取得発言は削除しない)
	 * 発言が無ければSKIP
	 * @return 発話する発言
	 */
	public String peekTalk() {
		if(talkQue.isEmpty()) {
			return TalkFactory.skipRemark();
		}
		return talkQue.peek();
	}
	
	/**
	 * 発言を指定位置に登録する
	 * @param talk :発言
	 * @param position :登録する位置(1~10)
	 */
	public void addAssignTalk(String talk, int position) {
		//一時保管用キュー
		Deque<String> storeQue = new ArrayDeque<String>();
		//登録位置が超えていたら強制的に1
		if(position>10 || position<1) {
			position = 1;
		}
		//position番目に発言を格納，postionが格納されている大きさより大きければ最後尾に格納
		if(talkQue.size()>=position) {
			for(int i=0; i<position-1; i++) {
				storeQue.push(talkQue.pop());
			}
			talkQue.addFirst(talk);
			for(int i=0; i<storeQue.size(); i++) {
				talkQue.addFirst(storeQue.getLast());
			}
		}else{
			addTalk(talk);
		}
	}
	
	/**
	 * 発言格納キューの大きさ取得
	 * @return 発言キューサイズ
	 */
	public int getSize() {
		return talkQue.size();
	}
	
	/**
	 * 指定発言が発言予定内にあるかどうか
	 * @param talk :指定発言
	 * @return あればtrue,無ければfalse
	 */
	public boolean containTalk(String talk) {
		if(talkQue.contains(talk)) {
			return true;
		} 
		return false;
	}
	
	/**
	 * 指定発言が発言予定にあれば排除
	 * @param talk :発言
	 */
	public void cancelTalk(String talk) {
		if(containTalk(talk)) {
			talkQue.remove(talk);
		}
	}
	
	/**
	 * 発言済みキューを取得する
	 * @return 発言済みキュー
	 */
	public Deque<String> getSettledTalkQue() {
		return settledQue;
	}
	
	/**
	 * 発言済みキューを格納する
	 * @param talk :発言
	 */
	public void addSettledTalk(String talk) {
		if(talk==null) return;
		settledQue.add(talk);
	}
	
	/**
	 * 発言済みキューから会話を削除
	 * @param talk :発言
	 */
	public void removeSettledTalk(String talk) {
		if(talk==null) return;
		settledQue.remove(talk);
	}
	
	/**
	 * 指定発言が発言済内にあるかどうか
	 * @param talk :指定発言
	 * @return あればtrue,無ければfalse
	 */
	public boolean containSettledTalk(String talk) {
		if(settledQue.contains(talk)) {
			return true;
		} 
		return false;
	}
	
	/**
	 * 発言予定が発言済みにあれば排除
	 */
	public void cancelQue() {
		talkQue.containsAll(settledQue);
	}
	
}
