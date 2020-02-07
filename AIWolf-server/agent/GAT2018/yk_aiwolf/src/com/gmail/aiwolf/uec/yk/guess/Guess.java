package com.gmail.aiwolf.uec.yk.guess;


import java.util.ArrayList;
import java.util.HashMap;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;


/**
 * 推理を表すクラス
 */
public final class Guess {

	/** 条件 */
	public AbstractCondition condition;

	/** 係数(conditionを満たす可能性をcorrelation倍する) */
	public double correlation = 1.0;

	public HashMap<Integer, Integer> info = new HashMap<Integer,Integer>();
}
