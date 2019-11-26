package com.carlo.aiwolf.bayes.trust.noweka.tool;

import java.text.DecimalFormat;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import com.carlo.aiwolf.bayes.trust.*;
import com.carlo.aiwolf.bayes.trust.Correct;
import com.carlo.bayes.lib.WekaBayesManager;


/**
 *  wekaBayesManagerでネットワークの計算を行い、各条件での条件付き確率を表示するクラス
 *  このデータを元にBayesResultListを作る。直接エージェントには関係しない。
 * @author info
 *
 */

public class BayesResultPrinter {
	
	public void printAttackedThreshold(){
		WekaBayesManager attackedBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newattacked1.xml"));
		System.out.print("{");
		
		for(Role role:Role.values()){
			if(role==Role.FREEMASON) continue;
			attackedBayes.clearAllEvidence();
			attackedBayes.setEvidence("corole",BayesConverter15.convertRole(role));
			attackedBayes.calcMargin();
			
			DecimalFormat df2 = new DecimalFormat("#.##################");
			double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
			System.out.print(df2.format(margin)+",");
			
		}
		attackedBayes.clearAllEvidence();
		attackedBayes.setEvidence("corole",BayesConverter15.convertRole(null));
		attackedBayes.calcMargin();
		
		DecimalFormat df2 = new DecimalFormat("#.##################");
		double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
		System.out.print(df2.format(margin)+",");

		System.out.print("}");
	}
	public void printAttackedMargin(){
		WekaBayesManager attackedBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newattacked1.xml"));
		System.out.print("{");
		
		for(Role role:Role.values()){
			if(role==Role.FREEMASON) continue;
			attackedBayes.clearAllEvidence();
			attackedBayes.setEvidence("corole",BayesConverter15.convertRole(role));
			attackedBayes.setEvidence("attacked", "true");
			attackedBayes.calcMargin();
			
			DecimalFormat df2 = new DecimalFormat("#.##################");
			double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
			System.out.print(df2.format(margin)+",");
			//System.out.println(","+role.ordinal()+" "+role);
		}
		attackedBayes.clearAllEvidence();
		attackedBayes.setEvidence("corole",BayesConverter15.convertRole(null));
		attackedBayes.setEvidence("attacked", "true");
		attackedBayes.calcMargin();
		
		DecimalFormat df2 = new DecimalFormat("#.##################");
		double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
		System.out.print(df2.format(margin)+",");

		System.out.print("}");
	}
	
	public void printMediumThreshold(){
		WekaBayesManager mediumBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/medium3_correct.xml"));
		System.out.print("{");
		for(int i=2;i<9;i++){
			mediumBayes.clearAllEvidence();
			mediumBayes.setEvidence("day",BayesConverter15.convertDay(i));
			mediumBayes.calcMargin();
			
			DecimalFormat df2 = new DecimalFormat("#.##################");
			double margin=mediumBayes.getMarginalProbability("role", "MEDIUM");
			System.out.print(df2.format(margin)+",");
		}
		System.out.print("}");
	}
	public void printMediumMargin(){
		WekaBayesManager mediumBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/medium3_correct.xml"));
		System.out.print("{");
		for(int i=2;i<9;i++){
			System.out.print("{");
			for(int j=0;j<2;j++){
				Species targetSpecies= (j==0) ? Species.HUMAN : Species.WEREWOLF;
				mediumBayes.clearAllEvidence();
				mediumBayes.setEvidence("day",BayesConverter15.convertDay(i));
				mediumBayes.setEvidence("result",targetSpecies.toString());
				mediumBayes.calcMargin();
				
				DecimalFormat df2 = new DecimalFormat("#.##################");
				double margin=mediumBayes.getMarginalProbability("role", "MEDIUM");
				System.out.print(df2.format(margin)+",");
			}
			System.out.print("}");
		}
		System.out.print("}");
	}
	
	public void printVoterMargin(){
		WekaBayesManager voterBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newvote3_1.xml"));
		System.out.print("{");
		for(int i=1;i<9;i++){
			System.out.println("{");
			for(int j=0;j<3;j++){
				Species targetSpecies= null;
				if(j==1) targetSpecies=Species.HUMAN;
				else if(j==2) targetSpecies=Species.WEREWOLF;
				System.out.print("{");
				for(int k=0;k<3;k++){
					String assist=null;
					if(k==1) assist="true";
					else if(k==2) assist="false";
					
					voterBayes.clearAllEvidence();
					voterBayes.setEvidence("day",BayesConverter15.convertDay(i));
					if(targetSpecies!=null) voterBayes.setEvidence("target",BayesConverter.convert(targetSpecies));
					if(assist!=null) voterBayes.setEvidence("assist", assist);
					voterBayes.calcMargin();
					
					DecimalFormat df2 = new DecimalFormat("#.##################");
					double margin=voterBayes.getMarginalProbability("voter", "human");
					System.out.print(df2.format(margin)+",");
				}
				System.out.print("}");
			}
			System.out.print("}");
		}
		System.out.print("}");
	}
	public void printVoterThreshold(){
		WekaBayesManager voterBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/newvote3_1.xml"));
		System.out.print("{");
		for(int i=1;i<9;i++){
			voterBayes.clearAllEvidence();
			voterBayes.setEvidence("day",BayesConverter15.convertDay(i));
			voterBayes.calcMargin();
			
			DecimalFormat df2 = new DecimalFormat("#.##################");
			double margin=voterBayes.getMarginalProbability("voter", "human");
			System.out.print(df2.format(margin)+",");
		}
		System.out.print("}");
	}
	public void printSeer(){
		WekaBayesManager seerBayes=new WekaBayesManager(this.getClass().getResourceAsStream("xml/new_seer2_6.xml"));
		//new WekaBayesManager(this.getClass().getResourceAsStream(fileName));
		
		
		System.out.println("");
		for(int i=0;i<10;i++){
			System.out.print("{");
			//result
			for(int j=0;j<2;j++){
				System.out.print("{");
				Species result= (j==0) ? Species.HUMAN : Species.WEREWOLF;
				//correct
				//unknoen - yes - no
				for(int k=0;k<3;k++){
					System.out.print("{");
					Correct correct=Correct.UNKNOWN;
					if(k==1) correct=Correct.YES;
					else if(k==2) correct=Correct.NO;
					//is attacked
					for(int l=0;l<3;l++){
						Correct attacked=Correct.UNKNOWN;
						if(l==1) attacked=Correct.YES;
						else if(l==2) attacked=Correct.NO;
						
						
						seerBayes.clearAllEvidence();
						seerBayes.setEvidence("day",BayesConverter15.convertDay(i));
						seerBayes.setEvidence("result",result.toString());
						if(correct!=Correct.UNKNOWN) seerBayes.setEvidence("correct", BayesConverter15.convert(correct));
						if(attacked!=Correct.UNKNOWN) seerBayes.setEvidence("isAttacked", BayesConverter15.convert(attacked));
						
						seerBayes.calcMargin();
						//System.out.println(i+" "+result+" "+correct+" "+attacked);
						DecimalFormat df2 = new DecimalFormat("#.##################");
						//DecimalFormat df2 = new DecimalFormat("##########.##########");
						double margin=seerBayes.getMarginalProbability("role", "SEER");


						System.out.print(df2.format(margin)+",");
					}
					System.out.println("},");
				}
				System.out.println("},");
			}
			System.out.println("},");
			//System.out.println("");
		}

	}

	public static void main(String[] args) {
		BayesResultPrinter printer=new BayesResultPrinter();
		printer.printAttackedThreshold();
	}

}
