package jp.ac.aitech.k15029kk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSelect {

	public static <T> T get(List<T> targetList) {
		if(targetList.isEmpty()) {
			return null;
		}
		return targetList.get(getNum(targetList.size()));
	}


	/**
	 * removeListの引数を全て排除した状態でrandomSelect
	 * @param targetList
	 * @param removeList
	 * @return
	 */
	@SafeVarargs
	public static <T> T get(List<T> targetList, List<T>... removeList) {
		List<T> storeList = new ArrayList<>(targetList);
		Gadget.removeAll(storeList, removeList);
		return get(storeList);
	}


	public static int getNum(int value) {
		int randomValue = 0;
		randomValue = new Random().nextInt(value);
		return randomValue;
	}
}
