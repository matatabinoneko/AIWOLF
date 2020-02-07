package com.gmail.aiwolf.uec.yk.strategyplayer;


import com.gmail.aiwolf.uec.yk.guess.AbstractGuessStrategy;



/**
 * �ۗL���鐄���헪���Ǘ�����N���X
 */
public final class HasGuessStrategy {

	/**
	 * �����헪�N���X
	 */
	public AbstractGuessStrategy strategy;

	/**
	 * �헪�̔�d(�������ʂ̑��֌W����Weight�悷��)
	 */
	public double weight;


	/**
	 * �R���X�g���N�^
	 * @param strategy �헪�N���X
	 * @param weight �헪�̔�d(�������ʂ̑��֌W����Weight�悷��)
	 */
	public HasGuessStrategy(AbstractGuessStrategy strategy, double weight){
		this.strategy = strategy;
		this.weight = weight;
	}


}
