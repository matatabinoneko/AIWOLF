package com.gmail.aiwolf.uec.yk.lib;

import java.util.ArrayList;


/**
 * 狼陣営を表現するクラス
 */
public final class WolfsidePattern{

	/** 狼陣営でないことを表すコード */
	static final char NOTWOLFSIDE_CODE = '0';

	/** 狼であることを表すコード */
	static final char WOLF_CODE = '1';

	/** 狂人であることを表すコード */
	static final char POSSESSED_CODE = '2';


	/** 人狼のエージェント番号(昇順で設定すること) */
	public final ArrayList<Integer> wolfAgentNo;

	/** 狂人のエージェント番号(昇順で設定すること) */
	public final ArrayList<Integer> possessedAgentNo;

	/** 狼陣営の名称 */
	public String wolfSideName;


	/**
	 * コンストラクタ
	 * @param wolfAgentNo 人狼のエージェント番号(昇順で設定すること)
	 * @param possessedAgentNo 狂人のエージェント番号(昇順で設定すること)
	 */
	public WolfsidePattern(ArrayList<Integer> wolfAgentNo, ArrayList<Integer> possessedAgentNo){
		this.wolfAgentNo = wolfAgentNo;
		this.possessedAgentNo = possessedAgentNo;

		// 狼陣営の名称の設定
		setWolfSideName();
	}


	/**
	 * 特定のエージェントが狼か
	 * @param agentno
	 * @return
	 */
	public boolean isWolf(int agentno){

		// いずれかの狼番号と一致するか
		return wolfAgentNo.contains(agentno);

	}


	/**
	 * 特定のエージェントが狂人か
	 * @param agentno
	 * @return
	 */
	public boolean isPossessed(int agentno){

		// いずれかの狂人番号と一致するか
		return possessedAgentNo.contains(agentno);

	}


	/**
	 * 特定のエージェントが狼陣営か
	 * @param agentno
	 * @return
	 */
	public boolean isWolfSide(int agentno){

		// いずれかの狼番号と一致するか、またはいずれかの狂人番号と一致するか
		return ( wolfAgentNo.contains(agentno) || possessedAgentNo.contains(agentno) );

	}


	/**
	 * 狼陣営のコードを返す
	 * エージェント１を２文字目、エージェント２を３文字目として、狼を"1"、狂人を"2"、どちらでもない者を"0"で表す
	 * 狼[1,4,5]狂[3]　→　"01021100000000000"
	 * charAt(agentNo)で該当エージェントの情報を取得できる
	 * @return
	 */
	public String getWolfSideCode(){

		StringBuilder sb = new StringBuilder();

		//TODO 人数をなんとか
		for( int i=0; i<20; i++ ){
			sb.append(NOTWOLFSIDE_CODE);
		}

		for( int wolf : wolfAgentNo ){
			sb.setCharAt(wolf, WOLF_CODE);
		}
		for( int pos : possessedAgentNo ){
			sb.setCharAt(pos, POSSESSED_CODE);
		}

		return sb.toString();

	}


	/**
	 * 狼陣営の名称の設定
	 */
	private void setWolfSideName(){

		StringBuilder sb = new StringBuilder();

		// 狼を一覧表示　例）狼[1,2,3]
		sb.append("狼[");
		for( int wolf : wolfAgentNo ){
			sb.append(wolf).append(",");
		}
		if( !wolfAgentNo.isEmpty() ){
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("]");

		// 狂人を一覧表示　例）狂人[1,2,3]
		sb.append(" 狂人[");
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
	 * 文字列化
	 * @return 文字列化した狼陣営
	 */
	public String toString(){
		return wolfSideName;
	}

}
