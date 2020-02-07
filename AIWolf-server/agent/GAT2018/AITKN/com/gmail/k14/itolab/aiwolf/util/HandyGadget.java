package com.gmail.k14.itolab.aiwolf.util;

import java.util.List;
import java.util.Map;

/**
 * 便利系のツールを使えるクラス
 * @author k14096kk
 *
 */
public class HandyGadget {
	
	/**
	 * リストに追加要素の存在があるかを確認して，存在しなければリストに追加する
	 * @param useList :リスト
	 * @param ob :追加要素
	 * @param <T> :型
	 */
	public static <T> void addList(List<T> useList, T ob) {
		if(!useList.contains(ob)) {
			useList.add(ob);
		}
	}
	
	/**
	 * リストに追加リストの中から重複していない要素のみを追加する
	 * @param useList :リスト
	 * @param aLists :追加するリスト(複数可能)
	 * @param <T> :型
	 */
	@SafeVarargs
	public static <T> void addAllList(List<T> useList, List<T>... aLists) {
		for(List<T> aList: aLists) {
			for(T e: aList) {
				addList(useList, e);
			}
		}
	}
	
	/**
	 * リストの中から指定位置に存在する要素を取得する<br>
	 * リストが空またはサイズが足りないならばnullを返す
	 * @param useList :リスト
	 * @param num :取り出す位置
	 * @return リストの指定位置の要素
	 * @param <T> :型
	 */
	public static <T> T getListValue(List<T> useList, int num) {
		// リストが空ならばnull
		if(useList.isEmpty()) {
			return null;
		}
		// リストのサイズが指定の長さなければnull
		if(useList.size() < (num+1)) {
			return null;
		}
		
		return useList.get(num);
	}
	
	/**
	 * 指定したリストから複数のリスト内容を排除する
	 * @param useList :指定リスト
	 * @param removeLists :排除するリスト(複数可能)
	 * @param <T> :型
	 */
	@SafeVarargs
	public static <T> void removeAll(List<T> useList, List<T>... removeLists) {
		// リストから排除したいリストを排除する
		for(List<T> rl: removeLists) {
			useList.removeAll(rl);
		}
	}
	
	/**
	 * 指定したリストから複数の要素を排除する
	 * @param useList :指定リスト
	 * @param elements :排除する要素(複数可能)
	 * @param <T> :型
	 */
	@SafeVarargs
	public static <T> void removeAll(List<T> useList, T... elements) {
		// リストから排除したい要素を排除する
		for(T e: elements) {
			useList.remove(e);
		}
	}
	
	/**
	 * 要素(第一引数)を指定した全てのリストから排除する
	 * @param element :排除する要素
	 * @param removedLists :指定リスト(複数可能)
	 * @param <T> :型
	 */
	@SafeVarargs
	public static <T> void remove(T element, List<T>... removedLists) {
		for(List<T> rl: removedLists) {
			rl.remove(element);
		}
	}
	
	/**
	 * 複数のリストのサイズの合計値を計算する
	 * @param elements :サイズをとるリスト
	 * @return サイズの合計値
	 * @param <T> :型
	 */
	@SafeVarargs
	public static <T> int sumListSize(List<T>... elements) {
		int s = 0;
		for(List<T> element: elements) {
			s += element.size();
		}
		return s;
	} 
	
	/**
	 * マップに指定キーが存在するか確認して，存在すれば要素を取得する<br>
	 * マップが空またはキーが存在しなければnullを返す
	 * @param useMap :マップ
	 * @param key :指定キー
	 * @return マップの要素
	 * @param <T> :型
	 */
	public static <T> T getMapValue(Map<T, T> useMap, T key) {
		// マップが空ならばnullを返す
		if(useMap.isEmpty()) {
			return null;
		}
		
		// マップに指定したキーが存在していなければnullを返す
		if(!useMap.containsKey(key)) {
			return null;
		}
		
		return useMap.get(key);
	}

}
