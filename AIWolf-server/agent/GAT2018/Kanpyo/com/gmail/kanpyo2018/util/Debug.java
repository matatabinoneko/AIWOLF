package com.gmail.kanpyo2018.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Debug {

	private static List<String> passError = new ArrayList<>();
	private static boolean isDebug = true;

	/**
	 * デバック中なら標準出力
	 * @param s
	 */
	public static <T> void print(T s) {
		if (isDebug) {
			System.out.println(s);
		}
	}

	/**
	 * デバック中なら表示出力
	 * @param s
	 */
	public static <T> void error(T s) {
		if (isDebug) {
			System.err.println(s);
		}
	}

	/**
	 * エラー文をリストに貯める
	 *
	 * @param sw:表示文
	 */
	public static void stackError(StringWriter sw) {
		passError.add(sw.toString());
	}

	/**
	 * エラー表示
	 */
	public static void showError() {
		for (int i = 0; i < passError.size(); i++) {
			System.out.println(passError.get(i));
		}
	}
}
