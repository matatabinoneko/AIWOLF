package com.gmail.k14.itolab.aiwolf.util;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

/**
 * リクエストの内容を取得するクラス
 * @author k14096kk
 *
 */
public class OperatorElement {
	
	/**
	 * 内容がリクエストかどうか
	 * @param talk :発言
	 * @return リクエストならばtrue,異なるならばfalse
	 */
	public static boolean isRequest(Talk talk) {
		Content content = new Content(talk.getText());
		if(content.getOperator()==Operator.REQUEST) {
			return true;
		}
		return false;
	}
	
	/**
	 * 内容がリクエストかどうか
	 * @param content :コンテンツ
	 * @return リクエストならばtrue,異なるならばfalse
	 */
	public static boolean isRequest(Content content) {
		if(content.getOperator()==Operator.REQUEST) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容のコンテンツを取得(TALK版)
	 * @param talk :会話
	 * @return リクエストの内容
	 */
	public static Content getRequestContent(Talk talk) {
		return new Content(talk.getText()).getContentList().get(0);
	}
	
	/**
	 * リクエスト内容のコンテンツを取得(Content版)
	 * @param content :コンテンツ
	 * @return リクエストの内容
	 */
	public static Content getRequestContent(Content content) {
		return content.getContentList().get(0);
	}
	
	/**
	 * リクエスト内容のトピックを取得
	 * @param content :コンテンツ
	 * @return リクエストされているトピック
	 */
	public static Topic getTopic(Content content) {
		return getRequestContent(content).getTopic();
	}
	
	/**
	 * リクエスト内容の結果を取得
	 * @param content :コンテンツ
	 * @return リクエストされている結果
	 */
	public static Species getResult(Content content) {
		return getRequestContent(content).getResult();
	}
	
	/**
	 * リクエスト内容の役職を取得
	 * @param content :コンテンツ
	 * @return リクエストされている役職
	 */
	public static Role getRole(Content content) {
		return getRequestContent(content).getRole();
	}
	
	/**
	 * リクエスト内容の日付を取得
	 * @param content :コンテンツ
	 * @return リクエストされている日付
	 */
	public static int getTalkDay(Content content) {
		return getRequestContent(content).getTalkDay();
	}
	
	/**
	 * リクエスト内容の発話タイプを取得
	 * @param content :コンテンツ
	 * @return リクエストされている会話タイプ
	 */
	public static TalkType getTalkType(Content content) {
		return getRequestContent(content).getTalkType(); 
	}
	
	/**
	 * リクエスト内容の発話IDを取得
	 * @param content :コンテンツ
	 * @return リクエストされている発話ID
	 */
	public static int getTalkID(Content content) {
		return getRequestContent(content).getTalkID();
	}
	
	/**
	 * リクエスト内容のリクエスト対象エージェント
	 * @param content :コンテンツ
	 * @return リクエストされている対象エージェント
	 */
	public static Agent getSubject(Content content) {
		return getRequestContent(content).getSubject();
	}
	
	/**
	 * リクエスト内容の対象エージェント
	 * @param content :コンテンツ
	 * @return リクエストされている行動対象エージェント
	 */
	public static Agent getTarget(Content content) {
		return getRequestContent(content).getTarget();
	}
	
	/**
	 * リクエスト内容のテキスト
	 * @param content :コンテンツ
	 * @return リクエストされているテキスト
	 */
	public static String getText(Content content) {
		return getRequestContent(content).getText();
	}
	
	/**
	 * リクエスト内容と指定トピックの比較
	 * @param content :比較するコンテンツ
	 * @param topic :指定トピック
	 * @return 指定トピックと同じならばtrue,異なればfalse;
	 */
	public static boolean compareTopic(Content content, Topic topic) {
		if(getTopic(content)==topic) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定結果の比較
	 * @param content :比較するコンテンツ
	 * @param result :指定結果
	 * @return 指定役職と同じならばtrue,異なればfalse;
	 */
	public static boolean compareResult(Content content, Species result) {
		if(getResult(content)==result) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定日付の比較
	 * @param content :比較するコンテンツ
	 * @param day :指定日付
	 * @return 指定日付と同じならばtrue,異なればfalse;
	 */
	public static boolean compareTalkDay(Content content, int day) {
		if(getTalkDay(content)==day) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定発話タイプの比較
	 * @param content :比較するコンテンツ
	 * @param type :指定発話タイプ
	 * @return 指定発話タイプと同じならばtrue,異なればfalse;
	 */
	public static boolean compareTalkType(Content content, TalkType type) {
		if(getTalkType(content)==type) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定発話IDの比較
	 * @param content :比較するコンテンツ
	 * @param id :指定発話ID
	 * @return 指定発話IDと同じならばtrue,異なればfalse;
	 */
	public static boolean compareTalkID(Content content, int id) {
		if(getTalkID(content)==id) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定リクエスト対象エージェントの比較
	 * @param content :比較するコンテンツ
	 * @param agent :指定エージェント
	 * @return 指定リクエスト対象エージェントと同じならばtrue,異なればfalse;
	 */
	public static boolean compareSubject(Content content, Agent agent) {
		if(getSubject(content)==agent) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定対象エージェントの比較
	 * @param content :比較するコンテンツ
	 * @param agent :指定エージェント
	 * @return 指定対象エージェントと同じならばtrue,異なればfalse;
	 */
	public static boolean compareTarget(Content content, Agent agent) {
		if(getTarget(content)==agent) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト内容と指定テキストの比較
	 * @param content :比較するコンテンツ
	 * @param text :指定テキスト
	 * @return 指定テキストと同じならばtrue,異なればfalse;
	 */
	public static boolean compareText(Content content, String text) {
		if(getText(content)==text) {
			return true;
		}
		return false;
	}
	
	/**
	 * リクエスト発言のリストを取得する
	 * @param talkList :発言リスト
	 * @return リクエスト発言のリスト
	 */
	public static List<Talk> getRequestTalkList(List<Talk> talkList) {
		List<Talk> requestTalk = new ArrayList<>();
		for(int i=0; i<talkList.size(); i++) {
			Talk talk = talkList.get(i);
			Content content = new Content(talk.getText());
			if(isRequest(content)) {
				requestTalk.add(talk);
			}
		}
		return requestTalk;
	}
	
}
