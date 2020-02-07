package com.gmail.kanpyo2018.util;

import java.util.List;

public class Gadget {

	/**
	 * リストに追加したい要素がリストに入っていないかを確認して追加する
	 *
	 * @param useList:使用するリスト
	 * @param ob:追加要素
	 * @param <T>:型
	 */
	public static <T> void addList(List<T> useList, T ob) {
		if (!useList.contains(ob)) {
			useList.add(ob);
		}
	}

	/**
	 * リストに追加したい要素がリストに入っていないかを確認して追加する
	 *
	 * @param useList:使用するリスト
	 * @param ob:追加要素(複数)
	 * @param <T>:型
	 */
	public static <T> void addList(List<T> useList, T... obs) {
		for (T ob : obs) {
			addList(useList, ob);
		}
	}

	/**
	 * リストに追加リストの中から重複していない要素のみ追加する
	 *
	 * @param useList:リスト
	 * @param aLists:追加するリスト(複数可能)
	 * @param <T>:型
	 */
	public static <T> void addAllList(List<T> useList, List<T>... aLists) {
		for (List<T> aList : aLists) {
			for (T e : aList) {
				addList(useList, e);
			}
		}
	}

	/**
	 * 指定したリストから複数のリスト内容を排除する
	 *
	 * @param useList:指定リスト
	 * @param removeList:排除するリスト(複数可能)
	 */
	public static <T> void removeAll(List<T> useList, List<T>... removeList) {
		// リストから排除したいリストを排除する
		for (List<T> rl : removeList) {
			useList.removeAll(rl);
		}
	}

	/**
	 * 指定したリストから複数の要素を削除する
	 *
	 * @param useList:指定リスト
	 * @param elements:排除する要素(複数可能)
	 * @param <T>:型
	 */
	public static <T> void removeAll(List<T> useList, T... elements) {
		for (T e : elements) {
			useList.remove(e);
		}
	}

}
