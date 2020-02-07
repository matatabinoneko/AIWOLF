package jp.halfmoon.inaba.aiwolf.guess;

import jp.halfmoon.inaba.aiwolf.lib.AdvanceGameInfo;
import jp.halfmoon.inaba.aiwolf.lib.AgentParameter;


/**
 * 推理戦術への引数クラス
 */
public final class GuessStrategyArgs {

	/** 整理情報 */
	public AdvanceGameInfo agi;

	/** 個人の持っている情報 */
	public AgentParameter agentParam;

}
