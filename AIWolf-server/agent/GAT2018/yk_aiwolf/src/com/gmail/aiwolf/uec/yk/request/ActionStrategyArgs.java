package com.gmail.aiwolf.uec.yk.request;

import com.gmail.aiwolf.uec.yk.guess.AnalysisOfGuess;
import com.gmail.aiwolf.uec.yk.lib.AdvanceGameInfo;
import com.gmail.aiwolf.uec.yk.lib.AgentParameter;
import com.gmail.aiwolf.uec.yk.lib.ViewpointInfo;

/**
 * 行動戦術への引数クラス
 */
public final class ActionStrategyArgs {

	/** 整理情報 */
	public AdvanceGameInfo agi;

	/** 視点情報 */
	public ViewpointInfo view;

	/** 推理情報 */
	public AnalysisOfGuess aguess;

	/** 個人の持っている情報 */
	public AgentParameter parsonalData;

}
