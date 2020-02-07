package com.carlo.aiwolf.bayes.trust.noweka;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
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
import com.carlo.aiwolf.bayes.trust.Correct;
import com.carlo.aiwolf.lib.info.CauseOfDeath;
import com.carlo.aiwolf.lib.info.GameInfoManager;
import com.carlo.bayes.lib.WekaBayesManager;


/**
 * BayesResultListを用いたバージョンのTrustList
 * 計算コストが重すぎるので、WekaBayesManagerをあまり使わないように計算数値をハードコーディングで保持している
 * @author carlo
 *
 */

public class NoWekaTrustList extends AbstractTrustList{

	private BayesResultList bayesResultList=new BayesResultList();
	
	public NoWekaTrustList(List<Agent> agents,Agent myAgent,GameInfoManager agentInfo){
		this.gameInfoMgr=agentInfo;
		for(Agent agent:agents){
			if(agent==myAgent) continue;
			//信用度の初期値は50
			trustMap.put(agent, 50.0);
		}
		//seerTpMap=new HashMap<String,Double>();
		seerCorrectMap=new HashMap<String,Correct>();
		
		//System.out.println("calc:"+seerBayes.getMarginalProbability(0, 1));
		//voterBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newvote3_1.xml"));
		//attackedBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newattacked1.xml"));
		//mediumBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/medium3_correct.xml"));

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
			
			//double threshold=attackedBayes.getMarginalProbability("team", "VILLAGER");
			
			Role deadCoRole=gameInfoMgr.getCOInfo().getCoRole(deadAgent);
			double threshold=bayesResultList.getAttackedBayesThreshold(deadCoRole);
			
			//attackedBayes.setEvidence("attacked","true");
			//Role deadCoRole=agentInfo.getCoRole(deadAgent);
			//nullのことを考えてconvert
			//attackedBayes.calcMargin();
			
			//double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
			double margin=bayesResultList.getAttackedBayesMargin(deadCoRole);
			
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
		if(reverse){
			//double point =bayesResultList.getSeerBayesMargin(day, species, correct, attacked)-bayesResultList.getSeerBayesThreshold(day);
			//addTrust(agent,-point);
			double point=changeTrust(agent,bayesResultList.getSeerBayesMargin(day, species, correct, attacked),bayesResultList.getSeerBayesThreshold(day),true);
			if(isShowConsoleLog) System.out.println("前回の計算結果分戻す"+-point);
			return;
		}
		
		//System.out.println(" ");
		//long time=System.currentTimeMillis();
		
		//seerBayes.setEvidence("day", BayesConverter15.convertDay(day));
		//seerBayes.calcMargin();
		//閾値。エビデンスを与えた場合にこれをどれだけ上回るか下回るかで信用度を上下させる
		//double threshold=seerBayes.getMarginalProbability("role", "SEER");
		double threshold=bayesResultList.getSeerBayesThreshold(day);
		
		
		//System.out.println("1 "+(System.currentTimeMillis()-time)+"ms");
		double margin=bayesResultList.getSeerBayesMargin(day, species, correct, attacked);
		
		//System.out.println("\t1 "+(System.currentTimeMillis()-time)+"ms");
		//元に戻す時用にデータ保存
		//なぜか時間かかる
		if(!reverse) {
			seerCorrectMap.put(agent+""+day+target,correct);
		}
		//信用度の変化
		changeTrust(agent,margin,threshold,reverse);
		
		//System.out.println("\t2 "+(System.currentTimeMillis()-time)+"ms");
	}
	/**
	 *  霊能結果発言から信用度計算
	 * @param agent
	 * @param targetSpecies
	 * @param day
	 */
	public void changeMediumTrust(Agent agent,Species targetSpecies,int day){
		if(!trustMap.containsKey(agent)) return;
		if(isShowConsoleLog) System.out.println("calc trust based on medium agent:"+agent+"result:"+targetSpecies);
		//mediumBayes.clearAllEvidence();
		//mediumBayes.setEvidence("day",BayesConverter15.convertDay(day));
		//voterBayes.calcMargin();
		//double threshold=mediumBayes.getMarginalProbability("role", "MEDIUM");
		double threshold=bayesResultList.getMediumBayesThreshold(day);
		
		//mediumBayes.setEvidence("result",targetSpecies.toString());
		//mediumBayes.calcMargin();
		//double margin=mediumBayes.getMarginalProbability("role", "MEDIUM");
		double margin=bayesResultList.getMediumBayesMargin(day, targetSpecies);
		
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

		
		//double threshold=voterBayes.getMarginalProbability("voter","human");
		double threshold=bayesResultList.getVoterBayesThreshold(day);
		//if(targetSpecies!=null) voterBayes.setEvidence("target",BayesConverter.convert(targetSpecies));
		//if(assist!=null) voterBayes.setEvidence("assist", assist);
		//voterBayes.calcMargin();
		//double margin=voterBayes.getMarginalProbability("voter","human");
		double margin=bayesResultList.getVoterBayesMargin(day, targetSpecies, assist);
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
