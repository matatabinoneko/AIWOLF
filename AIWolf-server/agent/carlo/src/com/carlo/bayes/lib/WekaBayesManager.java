package com.carlo.bayes.lib;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JFrame;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.bayes.net.MarginCalculator;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.classifiers.bayes.net.search.local.SimulatedAnnealing;
import weka.core.Instances;
import weka.core.SerializedObject;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.graphvisualizer.BIFFormatException;
import weka.gui.graphvisualizer.GraphVisualizer;
/**
 *  wekaのベイジアンネットワークを簡単に扱うためのクラス
 * @author carlo
 *
 */
public class WekaBayesManager {
	private EditableBayesNet bayes;
	/**value名とインデックスのマップ*/
	private HashMap<String,HashMap<String,Integer>> valueNameMaps=new HashMap<String,HashMap<String,Integer>>(); 
	public WekaBayesManager(String xmlFileName){
		BIFReader reader = new BIFReader();    
		try {
			bayes=new EditableBayesNet( reader.processFile(xmlFileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}
	public WekaBayesManager(InputStream is){
		BIFReader reader = new BIFReader();    
		BufferedReader br
		   = new BufferedReader(
		   new InputStreamReader(is));
		 
		StringBuilder sb = new StringBuilder();
		
		 
		String line;
		 
		try {
			while ((line = br.readLine()) != null) {
			   sb.append(line);
			}
			br.close();
			
			bayes=new EditableBayesNet(reader.processString(sb.toString()));
			
			initialize();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		 
		//System.out.println(sb.toString());
		 
		
	}
	/** ノードにエビデンス（valueの観測情報）をセットする */
	public void setEvidence(String nodeName,String value){
		int nodeIndex = getNodeIndex(nodeName);
		int valueIndex=valueNameMaps.get(nodeName).get(value);
		setEvidence(nodeIndex,valueIndex);
	}
	public void setEvidence(int nodeIndex,int valueIndex){
		bayes.setEvidence(nodeIndex, valueIndex);
	}
	/** ノードにセットされているエビデンスを消去する */
	public void clearEvidence(String nodeName){
		int nodeIndex =getNodeIndex(nodeName);
		setEvidence(nodeIndex,-1);
	}
	/** 全てのノードにセットされているエビデンスを消去する*/
	public void clearAllEvidence(){
		for(int i=0;i<bayes.getNrOfNodes();i++){
			bayes.setEvidence(i, -1);
		}
	}
	/** 全てのノードにセットされているエビデンスを消去し、calcMarginを呼び出す */
	public void clearAllEvidenceWithCalc(){
		clearAllEvidence();
		calcMargin();
	}
	/**
	 *  周辺確率(エビデンスがセットされている場合は周辺事後確率)を返す
	 *  事前にcalcMargin()の呼び出しが必要（再計算が必要ない場合を除く）
	 */
	public double getMarginalProbability(String nodeName,String value){
		int nodeIndex = getNodeIndex(nodeName);
		int valueIndex=valueNameMaps.get(nodeName).get(value);
		return bayes.getMargin(nodeIndex)[valueIndex];
	}
	/**
	 *  周辺確率(エビデンスがセットされている場合は周辺事後確率)を返す
	 *   事前にcalcMargin()の呼び出しが必要（再計算が必要ない場合を除く）
	 */
	public double getMarginalProbability(int nodeIndex,int valueIndex){
		return bayes.getMargin(nodeIndex)[valueIndex];
	}
	/**
	 * ネットワークのグラフをGUIで表示する
	 */
	public void drawGraph(){
		GraphVisualizer gv = new GraphVisualizer();
		try {
			gv.readBIF(bayes.graph());
		} catch (BIFFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		JFrame jf = new JFrame("BayesNet graph");
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.setSize(800, 600);
		jf.getContentPane().setLayout(new BorderLayout());
		jf.getContentPane().add(gv, BorderLayout.CENTER);
		jf.setVisible(true);
		// layout graph
		gv.layoutGraph();
	}
	public int getNodeIndex(String nodeName){
		int nodeIndex = -1;
		try {
			nodeIndex = bayes.getNode(nodeName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodeIndex;
	}
	/** ノードの個数を返す */
	public int getNrOfNodes(){
		return bayes.getNrOfNodes();
	}
	/** nodeNameと名前が一致するノードの、valueの個数を返す */
	public int getNrOfValues(String nodeName){
		return valueNameMaps.get(nodeName).size();
	}
	/** nodeIndexのノードの、valueの個数を返す */
	public int getNrOfValues(int nodeIndex){
		return bayes.getValues(nodeIndex).length;
	}
	/**
	 *  getMarginalProbabilityを呼び出す前に呼び出す必要がある
	 */
	public void calcMargin()  {
		//Marginの計算
		MarginCalculator m_marginCalculator = new MarginCalculator();
		try {
			m_marginCalculator.calcMargins(bayes);
			SerializedObject so = new SerializedObject(m_marginCalculator);
			MarginCalculator m_marginCalculatorWithEvidence = (MarginCalculator) so.getObject();
			for (int iNode = 0; iNode < bayes.getNrOfNodes(); iNode++) {
				if (bayes.getEvidence(iNode) >= 0) {
					m_marginCalculatorWithEvidence.setEvidence(iNode, bayes.getEvidence(iNode));
				}
			}
			for (int iNode = 0; iNode < bayes.getNrOfNodes(); iNode++) {
				bayes.setMargin(iNode, m_marginCalculatorWithEvidence.getMargin(iNode));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * マップの初期化処理
	 */
	private void initialize(){
		calcMargin();
		//マップにデータを追加
		for(int i=0;i<bayes.getNrOfNodes();i++){
			//System.out.println("ノード番号"+i+":"+bayes.getNodeName(i));
			int j=0;
			HashMap<String, Integer> valueMap=new HashMap<String,Integer>();
			for(j=0;j<bayes.getCardinality(i);j++){
				valueMap.put(bayes.getNodeValue(i, j),j);
			}
			valueNameMaps.put(bayes.getNodeName(i),valueMap);
		}
	}
	
	
	public static void main(String[] args) {
		//sample
		
		WekaBayesManager bayes=new WekaBayesManager("xml/werewolf.xml");
		bayes.setEvidence("seer_role", "possessed");
		bayes.setEvidence("day", "1");
		bayes.clearEvidence("day");
		bayes.calcMargin();
		//seer_role=possessedになるという条件の時に、species=humanになる確率が表示される
		System.out.println(bayes.getMarginalProbability("species", "human"));
		//bayes.drawGraph();
	

	}

}
