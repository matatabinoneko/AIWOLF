package com.gmail.aiwolf.uec.yk.guess;


import java.util.ArrayList;
import java.util.HashMap;

import com.gmail.aiwolf.uec.yk.condition.AbstractCondition;


/**
 * ������\���N���X
 */
public final class Guess {

	/** ���� */
	public AbstractCondition condition;

	/** �W��(condition�𖞂����\����correlation�{����) */
	public double correlation = 1.0;

	public HashMap<Integer, Integer> info = new HashMap<Integer,Integer>();
}
