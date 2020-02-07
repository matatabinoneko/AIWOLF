/**
 * AbstractWerewolf.java
 * 
 * Copyright (c) 2016 人狼知能プロジェクト
 */
package com.carlo.aiwolf.base.lib;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;

/**
 * <div lang="ja">人狼用抽象クラス。 呼ばれるはずのないメソッドが呼ばれると例外を投げる</div>
 *
 * <div lang="en">Abstract class for werewolf. When the invalid method is called, it throws an exception.</div>
 */
public abstract class MyAbstractWerewolf extends MyAbstractRole {

	@Override
	public final Agent divine() {
		throw new UnsuspectedMethodCallException();
	}

	@Override
	public final Agent guard() {
		throw new UnsuspectedMethodCallException();
	}

}
