package jp.halfmoon.inaba.aiwolf.guess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import jp.halfmoon.inaba.aiwolf.lib.WolfsidePattern;
import jp.halfmoon.inaba.aiwolf.strategyplayer.ReceivedGuess;


/**
 * 推理の分析結果を表すクラス
 */
public final class AnalysisOfGuess {


	/** 全パターンの分析結果 */
	private List<InspectedWolfsidePattern> allPattern = new ArrayList<InspectedWolfsidePattern>();

	/** エージェント毎の単体狼要素分析結果 */
	private List<InspectedWolfsidePattern> singleAgentWolfPattern = new ArrayList<InspectedWolfsidePattern>();

	/** エージェント毎の単体狂人要素分析結果 */
	private List<InspectedWolfsidePattern> singleAgentPossessedPattern = new ArrayList<InspectedWolfsidePattern>();

	/** エージェント毎の最も妥当な狼パターン idx=AgentNo */
	private ArrayList<InspectedWolfsidePattern> mostWolfPattern = new ArrayList<InspectedWolfsidePattern>();

	/** エージェント毎の最も妥当な狂人パターン idx=AgentNo */
	private ArrayList<InspectedWolfsidePattern> mostPossessedPattern = new ArrayList<InspectedWolfsidePattern>();

	/** 空の狼陣営パターン(Null対策) */
	private static final InspectedWolfsidePattern emptyWolfsidePattern = new InspectedWolfsidePattern(new WolfsidePattern(new ArrayList<Integer>(), new ArrayList<Integer>()), 0.0);


	//TODO こっちではデータ構造のみにして、格納は上位モジュールでやるべき？クラスのパッケージ移動も検討(ReceivedGuessが循環参照)
	/**
	 * コンストラクタ
	 * @param patterns 狼陣営のパターン
	 * @param guessmanager 推理結果
	 */
	public AnalysisOfGuess(int agentNum, LinkedHashSet<WolfsidePattern> patterns, GuessManager guessManager) {

		// 個々のエージェント単体で狼係数・狂人係数を求める
		ArrayList<Double> singleWolfScore = new ArrayList<Double>();
		ArrayList<Double> singlePossessedScore = new ArrayList<Double>();
		// 1オリジンにするため0番の要素にダミーを設定
		singleWolfScore.add(null);
		singlePossessedScore.add(null);
		for(int i = 1; i <= agentNum; i++){
			// 係数の初期化
			double wolfScore = 1.0;
			double possessedScore = 1.0;

			// ダミーパターンの作成
			ArrayList<Integer> singleAgent = new ArrayList<Integer>();
			singleAgent.add(i);
			ArrayList<Integer> blank = new ArrayList<Integer>();
			WolfsidePattern wolfPattern = new WolfsidePattern(singleAgent, blank);
			WolfsidePattern posPattern = new WolfsidePattern(blank, singleAgent);

			List<ReceivedGuess> singleGuesses = guessManager.getGuessForSingleAgent(i);
			// 推理の走査
			for(ReceivedGuess rguess : singleGuesses ){
				// 推理の内訳パターンが狼パターンとマッチするか
				if( rguess.guess.condition.isMatch(wolfPattern) ){
					wolfScore *= Math.pow( rguess.guess.correlation, rguess.weight);
				}
				// 推理の内訳パターンが狂人パターンとマッチするか
				if( rguess.guess.condition.isMatch(posPattern) ){
					possessedScore *= Math.pow( rguess.guess.correlation, rguess.weight);
				}
			}

			// 単体パターンとして記憶
			singleWolfScore.add(wolfScore);
			singlePossessedScore.add(possessedScore);
			InspectedWolfsidePattern inspectedWolfPattern = new InspectedWolfsidePattern(wolfPattern, wolfScore);
			InspectedWolfsidePattern inspectedPosPattern = new InspectedWolfsidePattern(posPattern, possessedScore);
			inspectedWolfPattern.guesses = singleGuesses;
			inspectedPosPattern.guesses = singleGuesses;
			singleAgentWolfPattern.add( inspectedWolfPattern );
			singleAgentPossessedPattern.add( inspectedPosPattern );
		}

		// エージェント毎の最も妥当なパターンを初期登録
		for(int i = 0; i <= agentNum; i++){
			mostWolfPattern.add(emptyWolfsidePattern);
			mostPossessedPattern.add(emptyWolfsidePattern);
		}

		// 狼陣営パターンの走査
		Iterator<WolfsidePattern> iter = patterns.iterator();
		while( iter.hasNext() ){
			WolfsidePattern pattern = iter.next();

			// このパターンに関連する推理
			//List<ReceivedGuess> guesses = new ArrayList<ReceivedGuess>();

			double score = 1.0;
			// 各狼の単体係数の計算
			for( int wolfAgentNo : pattern.wolfAgentNo ){
				score *= singleWolfScore.get(wolfAgentNo);
			}
			// 各狂人の単体係数の計算
			for( int posAgentNo : pattern.possessedAgentNo ){
				score *= singlePossessedScore.get(posAgentNo);
			}
			// 推理の走査
			for(ReceivedGuess rguess : guessManager.getGuessForMultiAgent() ){
				// 推理の条件が狼陣営パターンとマッチするか
				if( rguess.guess.condition.isMatch(pattern) ){
					// 狼陣営のスコアを補正する
					score *= Math.pow( rguess.guess.correlation, rguess.weight);

					// このパターンに関連する推理を記憶
					//guesses.add(rguess);
				}
			}
			// 狼陣営パターンに対する検証結果を格納
			InspectedWolfsidePattern inspectedPattern = new InspectedWolfsidePattern(pattern, score);
			//inspectedPattern.guesses = guesses;

			addPattern(inspectedPattern);

			// 各エージェントの狼/狂人として最も妥当なパターンを逐次計算
			for( int wolfAgentNo : pattern.wolfAgentNo ){
				if( score > mostWolfPattern.get(wolfAgentNo).score ){
					mostWolfPattern.set(wolfAgentNo, inspectedPattern);
				}
			}
			for( int posAgentNo : pattern.possessedAgentNo ){
				if( score > mostPossessedPattern.get(posAgentNo).score ){
					mostPossessedPattern.set(posAgentNo, inspectedPattern);
				}
			}

		}


	}


	/**
	 * 検証結果の記憶
	 * @param pattern
	 */
	public void addPattern(InspectedWolfsidePattern pattern){

		// 検証結果の記憶
		allPattern.add(pattern);

	}


	/**
	 * 特定の狼陣営に関する分析結果を取得する
	 * @return 指定した狼陣営に関する分析結果
	 */
	public InspectedWolfsidePattern getPattern(WolfsidePattern pattern){

		// 狼陣営パターンの走査
		for( InspectedWolfsidePattern workpattern : allPattern ){
			// 文字列化して同一かチェック
			if( pattern.toString().equals(workpattern.pattern.toString()) ){
				// 指定された内訳と同じであれば、分析結果を返す
				return workpattern;
			}
		}

		// 見つからなかった場合
		return null;

	}


	//TODO メソッドによってダミーを返す仕様とNullを返す仕様が混在してて気持ち悪い。統一する？
	/**
	 * 最も妥当な狼陣営に関する分析結果を取得する
	 * @return 最も妥当な狼陣営に関する分析結果(同一スコアがある場合、順番は保証されない)　無し時はscore0のダミーを返す
	 */
	public InspectedWolfsidePattern getMostValidPattern(){

		InspectedWolfsidePattern mostValidWolfsidePattern = emptyWolfsidePattern;
		double mostValidWolfScore = Double.NEGATIVE_INFINITY;

		// 狼陣営パターンの走査
		for( InspectedWolfsidePattern pattern : mostWolfPattern ){
			// 最大スコアであれば陣営を記憶する
			if( pattern != null && pattern.score > mostValidWolfScore ){
				mostValidWolfsidePattern = pattern;
				mostValidWolfScore = pattern.score;
			}
		}

		return mostValidWolfsidePattern;

	}


	/**
	 * 特定のエージェントが狼のパターンのうち、最も妥当な狼陣営に関する分析結果を取得する
	 * @param agentNo エージェント番号
	 * @return 最も妥当な狼陣営に関する分析結果(同一スコアがある場合、最も先に登録された内訳)　無し時はscore0のダミーを返す
	 */
	public InspectedWolfsidePattern getMostValidWolfPattern(int agentNo){

		return mostWolfPattern.get(agentNo);

	}


	/**
	 * 特定のエージェントが狂人のパターンのうち、最も妥当な狼陣営に関する分析結果を取得する
	 * @param agentNo エージェント番号
	 * @return 最も妥当な狼陣営に関する分析結果(同一スコアがある場合、最も先に登録された内訳)　無し時はscore0のダミーを返す
	 */
	public InspectedWolfsidePattern getMostValidPossessedPattern(int agentNo){

		return mostPossessedPattern.get(agentNo);

	}


	/**
	 * エージェント単体に関する人狼の分析結果を取得する
	 * @param agentNo エージェント番号
	 * @return
	 */
	public InspectedWolfsidePattern getSingleWolfPattern(int agentNo){

		return singleAgentPossessedPattern.get(agentNo - 1);

	}


	/**
	 * エージェント単体に関する狂人の分析結果を取得する
	 * @param agentNo エージェント番号
	 * @return
	 */
	public InspectedWolfsidePattern getSinglePossessedPattern(int agentNo){

		return singleAgentPossessedPattern.get(agentNo - 1);

	}


}
