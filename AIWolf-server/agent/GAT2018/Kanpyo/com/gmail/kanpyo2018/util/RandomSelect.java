package com.gmail.kanpyo2018.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSelect {

	/**
	 * 0〜(乱数設定値-1)までのint型乱数作成
	 *
	 * @param value:乱数設定値
	 * @return int型乱数
	 */
	public static int getNum(int value) {
		int randomValue = 0;
		randomValue = new Random().nextInt(value);
		return randomValue;
	}

	/**
	 * 対象リストからランダムに要素を取得
	 *
	 * @param targetList:対象リスト
	 * @return
	 */
	public static <T> T get(List<T> targetList) {
		if (targetList.isEmpty()) {
			return null;
		}
		return targetList.get(getNum(targetList.size()));
	}

	public static <T> T get(List<T> targetList, T... removes) {
		List<T> storeList = new ArrayList<>(targetList);
		Gadget.removeAll(storeList, removes);
		return get(storeList);
	}
}
