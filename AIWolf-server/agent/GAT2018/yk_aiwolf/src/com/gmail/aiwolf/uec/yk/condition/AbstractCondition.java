package com.gmail.aiwolf.uec.yk.condition;

import java.util.ArrayList;

import com.gmail.aiwolf.uec.yk.lib.WolfsidePattern;


/**
 * 条件を表す抽象クラス
 */
public abstract class AbstractCondition {


	/**
	 * 条件を満たすか
	 * @return
	 */
	abstract public boolean isMatch( WolfsidePattern pattern );


	/**
	 * 対象となるエージェントの番号一覧を取得する
	 * @return
	 */
	abstract public ArrayList<Integer> getTargetAgentNo();


}
