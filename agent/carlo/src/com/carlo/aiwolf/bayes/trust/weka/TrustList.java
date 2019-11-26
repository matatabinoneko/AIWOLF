package com.carlo.aiwolf.bayes.trust.weka;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import com.carlo.aiwolf.bayes.trust.AbstractTrustList;
import com.carlo.aiwolf.bayes.trust.BayesConverter;
import com.carlo.aiwolf.bayes.trust.BayesConverter15;
import com.carlo.aiwolf.bayes.trust.Correct;
import com.carlo.aiwolf.lib.info.CauseOfDeath;
import com.carlo.aiwolf.lib.info.GameInfoManager;
import com.carlo.bayes.lib.WekaBayesManager;


/**
 * 各Agentに対する信用度のリスト
 * @author carlo
 *
 */

public class TrustList extends AbstractTrustList{

	private WekaBayesManager seerBayes;
	private WekaBayesManager voterBayes;
	private WekaBayesManager attackedBayes;
	private WekaBayesManager mediumBayes;
	
	/** 占いネットワークの計算結果 <br>
	 * keyは　占いCO者名+発言日+対象   例:Agent[01]2Agent[03] 
	 *  同じ日に同じ対象を占っても知らない */
	private Map<String,Double> seerTpMap;

	
	/** 日数ごとの閾値 */
	private double[] seerBayesThreshold={0.016744050831323284,0.3633872740364856,0.34576317050722255,0.3750486611625374
			,0.315052269410793,0.313899368765621,0.2843986483707879,0.30780677881139146,0.14360365302805994,0.14360365302805994};
	
	public TrustList(List<Agent> agents,Agent myAgent,GameInfoManager agentInfo){
		this.gameInfoMgr=agentInfo;
		for(Agent agent:agents){
			if(agent==myAgent) continue;
			//信用度の初期値は50
			trustMap.put(agent, 50.0);
		}
		seerTpMap=new HashMap<String,Double>();
		seerCorrectMap=new HashMap<String,Correct>();
		
		
		//
		
		//ベイズネットワーククラス生成
		/*
		seerBayes=new WekaBayesManager("xml/new_seer2_6.xml",false);
		voterBayes=new WekaBayesManager("xml/newvote3_1.xml",false);
		attackedBayes=new WekaBayesManager("xml/newattacked1.xml",false);
		mediumBayes=new WekaBayesManager("xml/medium3_correct.xml",false);
		*/
		seerBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/new_seer2_6.xml"));
		//System.out.println("calc:"+seerBayes.getMarginalProbability(0, 1));
		voterBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newvote3_1.xml"));
		attackedBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newattacked1.xml"));
		mediumBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/medium3_correct.xml"));
		
		//new WekaBayesManager(this.getClass().getResourceAsStream(fileName));
		
	}




	public double getTrustPoint(Agent agent){
		return trustMap.get(agent);
	}
	/**
	 * 死亡情報から信用度計算
	 * @param deadAgent 死んだエージェント
	 * @param cause 死因(死んだかどうか)
	 */
	public void changeDeadAgent(Agent deadAgent,CauseOfDeath cause){
		if(!trustMap.containsKey(deadAgent)) return;
		//襲撃されたらattackedネットワークから信用度を計算
		if(cause==CauseOfDeath.ATTACKED) {
			if(isShowConsoleLog)  System.out.println("calc trust based on dead:"+deadAgent+" "+" "+cause+" "+gameInfoMgr.getCOInfo().getCoRole(deadAgent));
			attackedBayes.clearAllEvidence();
			String convertRole=BayesConverter15.convertRole(gameInfoMgr.getCOInfo().getCoRole(deadAgent));
			attackedBayes.setEvidence("corole",convertRole);
			
			attackedBayes.calcMargin();
			//エビデンスがセットされたいない場合の確率を境界とする
			double threshold=attackedBayes.getMarginalProbability("team", "VILLAGER");
			
			attackedBayes.setEvidence("attacked","true");
			//Role deadCoRole=agentInfo.getCoRole(deadAgent);
			//nullのことを考えてconvert
			attackedBayes.calcMargin();
			
			double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
			changeTrust(deadAgent,margin,threshold,false);
			

		}
	}

	/**
	 *  占い発言から信用度計算
	 * @param agent 占い師
	 * @param day 占い発言した日にち
	 * @param species 占い結果
	 * @param correct 占い結果が合ってたかどうか
	 */
	public void changeSeerTrust(Agent agent,int day,Species species,Correct correct,Agent target,Correct attacked){
		changeSeerTrust(agent,day,species,correct,false,target,attacked);
	}
	/**
	 * @param reverse 逆の計算をするかどうか。通常はfalse
	 */
	public void changeSeerTrust(Agent agent,int day,Species species,Correct correct,boolean reverse,Agent target,Correct attacked){
		if(isShowConsoleLog)  System.out.print("calc trust based on seer:");
		if(isShowConsoleLog) System.out.println(agent+"target:"+target+" day:"+day+" species:"+species+" correct:"+correct+" reverse:"+reverse);
		
		
		if(!trustMap.containsKey(agent)) return;
		
		//追加 reverseかつ以前計算したものがあれば
		if(reverse && seerTpMap.containsKey(agent+""+day+target)){
			addTrust(agent,-seerTpMap.get(agent+""+day+target));
			if(isShowConsoleLog) System.out.println("前回の計算結果分戻す"+-seerTpMap.get(agent+""+day+target));
			return;
		}
		
		seerBayes.clearAllEvidence();
		System.out.println(" ");
		//long time=System.currentTimeMillis();
		
		seerBayes.setEvidence("day", BayesConverter15.convertDay(day));
		seerBayes.calcMargin();
		//閾値。エビデンスを与えた場合にこれをどれだけ上回るか下回るかで信用度を上下させる
		double threshold=seerBayes.getMarginalProbability("role", "SEER");
		//double threshold= (day < 9 ) ?  seerBayesThreshold[day] : seerBayesThreshold[9];
		
		seerBayes.setEvidence("result",species.toString());
		if(correct!=Correct.UNKNOWN) seerBayes.setEvidence("correct", BayesConverter15.convert(correct));
		if(attacked!=Correct.UNKNOWN) seerBayes.setEvidence("isAttacked", BayesConverter15.convert(attacked));
		
		//System.out.println("0 "+(System.currentTimeMillis()-time)+"ms");
		seerBayes.calcMargin();
		
		//System.out.println("1 "+(System.currentTimeMillis()-time)+"ms");
		
		double margin=seerBayes.getMarginalProbability("role", "SEER");
		
		//System.out.println("1 "+(System.currentTimeMillis()-time)+"ms");
		//元に戻す時用にデータ保存
		if(!reverse) {
			//long start=System.currentTimeMillis();
			//String name=""+agent+""+day+target;
			seerTpMap.put(""+agent+""+day+target,(margin-threshold)*100);
			//System.out.println("\t0 "+(System.currentTimeMillis()-start)+" "+name);
			seerCorrectMap.put(agent+""+day+target,correct);
			
			//System.out.println("\t1 "+(System.currentTimeMillis()-start)+"ms");
			
		}
		//信用度の変化
		changeTrust(agent,margin,threshold,reverse);
		
		//System.out.println("2 "+(System.currentTimeMillis()-time)+"ms");
	}
	/**
	 *  霊能結果発言から信用度計算
	 * @param agent
	 * @param targetSpecies
	 * @param day
	 */
	public void changeMediumTrust(Agent agent,Species targetSpecies,int day){
		if(!trustMap.containsKey(agent)) return;
		if(isShowConsoleLog) System.out.println("calc trust based on medium agent:"+agent+"result:"+targetSpecies+" day:"+day);
		mediumBayes.clearAllEvidence();
		mediumBayes.setEvidence("day",BayesConverter15.convertDay(day));
		mediumBayes.calcMargin();
		double threshold=mediumBayes.getMarginalProbability("role", "MEDIUM");
		
		mediumBayes.setEvidence("result",targetSpecies.toString());
		mediumBayes.calcMargin();
		double margin=mediumBayes.getMarginalProbability("role", "MEDIUM");
		
		changeTrust(agent,margin,threshold,false);
	}
	/** 
	 *  投票結果から信用度計算
	 * @param agent 投票者
	 * @param targetSpecies 投票先の種族
	 * @param day 投票日
	 * @param assist  booleanをstringにしたもの
	 * TODO:booleanを引数にして、こっちでStringにするべきかも
	 */
	public void changeVoterTrust(Agent agent,Species targetSpecies,int day,String assist){
		if(!trustMap.containsKey(agent)) return;
		if(isShowConsoleLog) System.out.println("calc trust based on vote "+agent+"target:"+targetSpecies);
		voterBayes.clearAllEvidence();
		voterBayes.setEvidence("day",BayesConverter15.convertDay(day));
		voterBayes.calcMargin();
		
		double threshold=voterBayes.getMarginalProbability("voter","human");
		if(targetSpecies!=null) voterBayes.setEvidence("target",BayesConverter.convert(targetSpecies));
		if(assist!=null) voterBayes.setEvidence("assist", assist);
		voterBayes.calcMargin();
		double margin=voterBayes.getMarginalProbability("voter","human");
		changeTrust(agent,margin,threshold,false);
	}
	public void setShowConsoleLog(boolean isShowConsoleLog){
		this.isShowConsoleLog=isShowConsoleLog;
	}
	/**
	 * keyは seerAgent day targetAgent からなる
	 *  占い師の結果が当たっていたかどうかが保存されているかを確認
	 *  保存されていればそれを返す。なければnull。
	 * @param key
	 * @return
	 */
	public Correct getSeerCorrect(Agent seer,int day,Agent target){
		String key=seer+""+day+""+target;
		if(seerCorrectMap.containsKey(key)) return seerCorrectMap.get(key);
		else return null;
	}



}
