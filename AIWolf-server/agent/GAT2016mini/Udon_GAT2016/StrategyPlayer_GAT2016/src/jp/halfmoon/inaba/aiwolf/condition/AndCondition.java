package jp.halfmoon.inaba.aiwolf.condition;

import java.util.ArrayList;
import java.util.List;

import jp.halfmoon.inaba.aiwolf.lib.WolfsidePattern;

/**
 * 複数の条件をAND判定する条件クラス(指定条件が1つも無い場合はTrue)
 */
public final class AndCondition extends AbstractCondition {

	/** 条件のリスト */
	private List<AbstractCondition> conditions = new ArrayList<AbstractCondition>();


	@Override
	public boolean isMatch( WolfsidePattern pattern ) {

		// 条件が１つも無い場合は、条件を満たした扱いにする
		if( conditions.isEmpty() ){
			return true;
		}

		// 条件を１つずつチェックし、１つでも満たさなければ　AND条件を満たさない
		for( AbstractCondition condition : conditions ){
			if( !condition.isMatch(pattern) ){
				return false;
			}
		}

		// 条件を満たさないものが無ければAND条件を満たす
		return true;

	}

	/**
	 * 条件を追加する(chainable)
	 * @param condition 追加する条件
	 * @return 自身のオブジェクト
	 */
	public AndCondition addCondition( AbstractCondition condition ){

		conditions.add(condition);

		return this;

	}


	@Override
	public ArrayList<Integer> getTargetAgentNo() {

		// エージェント番号のリスト
		ArrayList<Integer> ret = new ArrayList<Integer>();

		// 子条件を走査
		for( AbstractCondition condition : conditions ){
			// 子条件からエージェント番号のリストを取得
			ArrayList<Integer> subret = condition.getTargetAgentNo();
			// 取得したエージェント番号を、重複を廃してリストに追加する
			for( Integer agentno : subret ){
				if( !ret.contains(agentno) ){
					ret.add(agentno);
				}
			}
		}

		return ret;
	}


	public String toString(){

		if( conditions.isEmpty() ){
			return "nothing";
		}

		StringBuilder str = new StringBuilder();
		str.append("(");
		for( AbstractCondition condition : conditions ){
			if( str.length() > 1 ){
				str.append( " and " );
			}
			str.append( condition.toString() );
		}
		str.append(")");

		return str.toString();

	}

}
