package com.gmail.k14.itolab.aiwolf.util;

import java.util.Comparator;

/**
 * 回数によりエージェントを降順ソートするためのクラス
 * @author k14096kk
 *
 */
public class CountAgentComparator implements Comparator<CountAgent>{
	
	@Override
	public int compare(CountAgent agent1, CountAgent agent2) {
		return agent1.count < agent2.count ? -1 : 1;
	}

}
