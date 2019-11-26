/**
 * AbstractBodyguard.java
 * 
 * Copyright (c) 2016 人狼知能プロジェクト
 */
package com.carlo.aiwolf.base.lib;

import org.aiwolf.common.data.Agent;

/**
 * <div lang="ja">狩人用抽象クラス。呼ばれるはずのないメソッドが呼ばれると例外を投げる</div>
 * 
 * <div lang="en">Abstract class for bodyguard. When the invalid method is called, it throws an exception.</div>
 */
public abstract class MyAbstractBodyguard extends MyAbstractRole {

	@Override
	public final String whisper() {
		throw new UnsuspectedMethodCallException();
	}

	@Override
	public final Agent attack() {
		throw new UnsuspectedMethodCallException();
	}

	@Override
	public final Agent divine() {
		throw new UnsuspectedMethodCallException();
	}

}
