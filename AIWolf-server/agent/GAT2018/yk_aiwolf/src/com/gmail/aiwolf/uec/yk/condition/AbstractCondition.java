package com.gmail.aiwolf.uec.yk.condition;

import java.util.ArrayList;

import com.gmail.aiwolf.uec.yk.lib.WolfsidePattern;


/**
 * ������\�����ۃN���X
 */
public abstract class AbstractCondition {


	/**
	 * �����𖞂�����
	 * @return
	 */
	abstract public boolean isMatch( WolfsidePattern pattern );


	/**
	 * �ΏۂƂȂ�G�[�W�F���g�̔ԍ��ꗗ���擾����
	 * @return
	 */
	abstract public ArrayList<Integer> getTargetAgentNo();


}
