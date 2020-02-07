package jp.ac.aitech.k15029kk.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Debug {

	//エラー文格納リスト
	private static List<String> passError = new ArrayList<>();

	private static boolean isDebug = true;

	//エラー文を赤く表示する
	public static <T> void print(T s) {
		if(isDebug){
			System.err.println(s);
		}
	}

	/*
	public static void print(String s) {
		if(isDebug) {
			System.out.println(s);
		}
	}
	*/

	//エラー文をリストに貯める
	public static void stackError(StringWriter sw) {
		passError.add(sw.toString());
	}

	//確認できたエラーを表示
	public static void showError() {
		for(int i=0; i<passError.size(); i++) {
			System.out.println(passError.get(i));
		}
	}

}
