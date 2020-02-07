package jp.gmail.kogecha05.utils;

import java.util.List;

public class Functions {

	Functions () {}

	public static double distance(List<Integer> x, List<Integer> y) {
		assert(x.size() != y.size());
		double dist = 0;
		for (int i = 0; i < x.size(); i++) {
			dist += Math.pow((double)x.get(i) - y.get(i), 2.0);
		}
		return Math.sqrt(dist);
	}

	public static <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	public static <T> void debugPrintln(T s) {
		if (Const.DEBUG) System.out.println(s);
	}
}