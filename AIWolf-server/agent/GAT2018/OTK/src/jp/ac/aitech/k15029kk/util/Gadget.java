package jp.ac.aitech.k15029kk.util;

import java.util.List;

public class Gadget {

	//addAllListを使うための
	//Listに追加したい要素があるか確認してなかったら追加
	public static <T> void addList(List<T> useList, T ob) {
		if(!useList.contains(ob)) {
			useList.add(ob);
		}
	}

	@SafeVarargs
	//Listに追加したい要素があるか確認してなかったら追加
	public static <T> void addList(List<T> useList, T... obs) {
		for(T ob: obs) {
			addList(useList, ob);
		}
	}

	/**
	 * テスト
	 * @param useList :排除先
	 * @param aLists :排除するリスト
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
	 *
	 * @param useList :排除先
	 * @param elements :排除したい要素のListの配列
	 */
	@SafeVarargs
	public static <T> void removeAll(List<T> useList, List<T>... elements) {
		//リストから排除したい要素を排除する
		for(List<T> e: elements) {
			useList.remove(e);
		}
	}

	/**
	 *
	 * @param useList :排除先
	 * @param elements :排除したい要素の配列
	 */
	@SafeVarargs
	public static <T> void removeAll(List<T> useList, T... elements) {
		//リストから排除したい要素を排除する
		for(T e: elements) {
			useList.remove(e);
		}
	}

}