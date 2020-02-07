package jp.halfmoon.inaba.aiwolf.lib;

import java.util.ArrayList;


/**
 * �T�w�c��\������N���X
 */
public final class WolfsidePattern{

	/** �l�T�̃G�[�W�F���g�ԍ�(�����Őݒ肷�邱��) */
	public final ArrayList<Integer> wolfAgentNo;

	/** ���l�̃G�[�W�F���g�ԍ�(�����Őݒ肷�邱��) */
	public final ArrayList<Integer> possessedAgentNo;

	/** �T�w�c�̖��� */
	public String wolfSideName;


	/**
	 * �R���X�g���N�^
	 * @param wolfAgentNo �l�T�̃G�[�W�F���g�ԍ�(�����Őݒ肷�邱��)
	 * @param possessedAgentNo ���l�̃G�[�W�F���g�ԍ�(�����Őݒ肷�邱��)
	 */
	public WolfsidePattern(ArrayList<Integer> wolfAgentNo, ArrayList<Integer> possessedAgentNo){
		this.wolfAgentNo = wolfAgentNo;
		this.possessedAgentNo = possessedAgentNo;

		// �T�w�c�̖��̂̐ݒ�
		setWolfSideName();
	}


	/**
	 * ����̃G�[�W�F���g���T��
	 * @param agentno
	 * @return
	 */
	public boolean isWolf(int agentno){

		// �����ꂩ�̘T�ԍ��ƈ�v���邩
		return wolfAgentNo.contains(agentno);

	}


	/**
	 * ����̃G�[�W�F���g�����l��
	 * @param agentno
	 * @return
	 */
	public boolean isPossessed(int agentno){

		// �����ꂩ�̋��l�ԍ��ƈ�v���邩
		return possessedAgentNo.contains(agentno);

	}


	/**
	 * ����̃G�[�W�F���g���T�w�c��
	 * @param agentno
	 * @return
	 */
	public boolean isWolfSide(int agentno){

		// �����ꂩ�̘T�ԍ��ƈ�v���邩�A�܂��͂����ꂩ�̋��l�ԍ��ƈ�v���邩
		return ( wolfAgentNo.contains(agentno) || possessedAgentNo.contains(agentno) );

	}

	/**
	 * �T�w�c�̖��̂̐ݒ�
	 */
	private void setWolfSideName(){

		StringBuilder sb = new StringBuilder();

		// �T���ꗗ�\���@��j�T[1,2,3]
		sb.append("�T[");
		for( int wolf : wolfAgentNo ){
			sb.append(wolf).append(",");
		}
		if( !wolfAgentNo.isEmpty() ){
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("]");

		// ���l���ꗗ�\���@��j���l[1,2,3]
		sb.append(" ���l[");
		for( int pos : possessedAgentNo ){
			sb.append(pos).append(",");
		}
		if( !possessedAgentNo.isEmpty() ){
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("]");

		wolfSideName = sb.toString();

	}

	/**
	 * ������
	 * @return �����񉻂����T�w�c
	 */
	public String toString(){
		return wolfSideName;
	}

}
