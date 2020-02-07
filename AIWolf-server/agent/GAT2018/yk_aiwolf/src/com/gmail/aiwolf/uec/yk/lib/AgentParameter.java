package com.gmail.aiwolf.uec.yk.lib;

import java.util.HashMap;

/**
 * 個人に関するデータを管理するクラス
 */
public class AgentParameter {

	private HashMap<String, Double> param = new HashMap<String, Double>();


	/**
	 * パラメータの取得
	 * @param key 項目名
	 * @param defaultValue 項目が存在しない場合の戻り値
	 * @return
	 */
	public Double getParam(String key, Double defaultValue){
		if( param.containsKey(key) ){
			return param.get(key);
		}
		return defaultValue;
	}

	/**
	 * パラメータの設定
	 * @param key 項目名
	 * @param value　値
	 * @param multiplication 既に項目がある場合、乗算する
	 */
	public void setParam(String key, Double value, boolean multiplication){

		if( param.containsKey(key) ){
			// 項目が存在する場合、上書きor乗算
			if( multiplication ){
				param.put(key, param.get(key) * value);
			}else{
				param.put(key, value) ;
			}
		}else{
			// 項目が存在しない場合そのまま設定
			param.put(key, value) ;
		}

	}


}
