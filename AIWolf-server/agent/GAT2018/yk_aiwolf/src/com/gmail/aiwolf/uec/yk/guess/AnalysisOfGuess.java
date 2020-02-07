package com.gmail.aiwolf.uec.yk.guess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;

import com.gmail.aiwolf.uec.yk.lib.WolfsidePattern;
import com.gmail.aiwolf.uec.yk.strategyplayer.ReceivedGuess;

import java.util.Random;

import libsvm.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * 推理の分析結果を表すクラス
 */
public final class AnalysisOfGuess {


	/** 全パターンの分析結果 */
	private HashMap<String, InspectedWolfsidePattern> allPattern;

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
	 * @throws IOException 
	 */
	public AnalysisOfGuess(int agentNum, Collection<WolfsidePattern> patterns, GuessManager guessManager, int guesscount, svm_model model, int today, int gamecount) throws IOException {
		
		// 全パターンの分析結果 を初期容量を指定して初期化
		allPattern = new HashMap<String, InspectedWolfsidePattern>(patterns.size() * 4 / 3);

		// 個々のエージェント単体で狼係数・狂人係数を求める
		ArrayList<Double> singleWolfScore = new ArrayList<Double>();
		ArrayList<Double> singlePossessedScore = new ArrayList<Double>();
		// 1オリジンにするため0番の要素にダミーを設定
		singleWolfScore.add(null);
		singlePossessedScore.add(null);
		/*File file = new File("c:\\Users\\omuricelove\\Documents\\AIWolf-ver0.4.9_2\\Svm_wolfscore\\wolfScoreOfSvm_game_"+gamecount+"_day_"+today+".txt");
		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}*/
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
			/*Integer[] rinfo = new Integer[32];
			for(int a = 0; a < 32; a++){
				rinfo[a] = 0;
			}*/
			svm_node[] input = new svm_node[31];
			for(int a = 0;a<31;a++){
				input[a] = new svm_node();
				input[a].index = a+1;
				//filewriter.write(input[a].index+":"+rinfo[a+1]+" ");
			}
			List<ReceivedGuess> singleGuesses = guessManager.getGuessForSingleAgent(i);
			// 推理の走査
			for(ReceivedGuess rguess : singleGuesses ){
				
				// 推理の内訳パターンが狼パターンとマッチするか
				if( rguess.guess.condition.isMatch(wolfPattern) ){
						for(Integer key : rguess.guess.info.keySet()){
							input[key-1].value = rguess.guess.info.get(key);
							
						}
					
					//wolfScore *= Math.pow( rguess.guess.correlation, rguess.weight);		
				}
				// 推理の内訳パターンが狂人パターンとマッチするか
				if( rguess.guess.condition.isMatch(posPattern) ){
					possessedScore *= Math.pow( rguess.guess.correlation, rguess.weight);
				}
			}
			if(input[0].value == 0){
				Random r = new Random();
				wolfScore = r.nextDouble() + 1.0;
				//possessedScore = wolfScore /2;
			}else{
				//filewriter.write("\r\n");
				double v = svm.svm_predict(model, input);
				if(v >= 1.0){
					v = 1.0;
				}else if(v <= -1.0){
					v = -1.0;
				}
				wolfScore = 2.0 - v;
				//possessedScore = wolfScore / 2;
				//filewriter.write("wolfScore:"+wolfScore+"\r\n");
				//filewriter.close();
				
				
				/*try{
					  File file = new File("c:\\Users\\omuricelove\\Documents\\AIWolf-ver0.4.9\\tmp\\info_agentNo_" +i+ "_svm_end"+guesscount+"result.txt");
					  FileWriter filewriter = new FileWriter(file);
					  
					  filewriter.write("agent"+i+"wolfscore:"+wolfScore);
					 /* for(int a= 0; a < 32; a++){
						  filewriter.write("  " +a +":" +rinfo[a]);
					  }
					  filewriter.write("\r\n");
					  filewriter.close();
				}catch(IOException e){
					  System.out.println(e);
				}*/
			}
			Agent a = Agent.getAgent(i);
			//filewriter.write("agent"+i+":"+wolfScore+","+a.toString()+"\r\n");

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
		//filewriter.close();

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
		allPattern.put(pattern.pattern.getWolfSideCode(), pattern);

	}


	/**
	 * 特定の狼陣営に関する分析結果を取得する
	 * @return 指定した狼陣営に関する分析結果
	 */
	public InspectedWolfsidePattern getPattern(WolfsidePattern pattern){

		return allPattern.get(pattern.getWolfSideCode());

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
	 * @return 最も妥当な狼陣営に関する分析結果(同一スコアがある場合、順番は保証されない)　無し時はscore0のダミーを返す
	 */
	public InspectedWolfsidePattern getMostValidWolfPattern(int agentNo){

		return mostWolfPattern.get(agentNo);

	}


	/**
	 * 特定のエージェントが狂人のパターンのうち、最も妥当な狼陣営に関する分析結果を取得する
	 * @param agentNo エージェント番号
	 * @return 最も妥当な狼陣営に関する分析結果(同一スコアがある場合、順番は保証されない)　無し時はscore0のダミーを返す
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


	/**
	 * 全パターンの分析結果を取得する
	 * @return
	 */
	public HashMap<String, InspectedWolfsidePattern> getAllPattern(){
		return allPattern;
	}
	

}
