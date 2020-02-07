package com.gmail.aiwolf.uec.yk.strategyplayer;


import com.gmail.aiwolf.uec.yk.request.AbstractActionStrategy;



/**
 * �ۗL����s���헪���Ǘ�����N���X
 */
public final class HasActionStrategy {

	/**
	 * �s���헪�N���X
	 */
	public AbstractActionStrategy strategy;

	/**
	 * �헪�̔�d(�e�s���̗v���W����Weight�悷��)
	 */
	public double weight;


	/**
	 * �R���X�g���N�^
	 * @param strategy �헪�N���X
	 * @param weight �헪�̔�d(�������ʂ̑��֌W����Weight�悷��)
	 */
	public HasActionStrategy(AbstractActionStrategy strategy, double weight){
		this.strategy = strategy;
		this.weight = weight;
	}


}
