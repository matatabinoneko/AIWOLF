package com.gmail.aiwolf.uec.yk.strategyplayer;


import com.gmail.aiwolf.uec.yk.guess.AbstractGuessStrategy;



/**
 * 保有する推理戦略を管理するクラス
 */
public final class HasGuessStrategy {

	/**
	 * 推理戦略クラス
	 */
	public AbstractGuessStrategy strategy;

	/**
	 * 戦略の比重(推理結果の相関係数をWeight乗する)
	 */
	public double weight;


	/**
	 * コンストラクタ
	 * @param strategy 戦略クラス
	 * @param weight 戦略の比重(推理結果の相関係数をWeight乗する)
	 */
	public HasGuessStrategy(AbstractGuessStrategy strategy, double weight){
		this.strategy = strategy;
		this.weight = weight;
	}


}
