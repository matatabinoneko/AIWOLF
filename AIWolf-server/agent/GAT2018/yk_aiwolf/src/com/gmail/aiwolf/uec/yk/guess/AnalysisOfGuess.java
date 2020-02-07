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
 * �����̕��͌��ʂ�\���N���X
 */
public final class AnalysisOfGuess {


	/** �S�p�^�[���̕��͌��� */
	private HashMap<String, InspectedWolfsidePattern> allPattern;

	/** �G�[�W�F���g���̒P�̘T�v�f���͌��� */
	private List<InspectedWolfsidePattern> singleAgentWolfPattern = new ArrayList<InspectedWolfsidePattern>();

	/** �G�[�W�F���g���̒P�̋��l�v�f���͌��� */
	private List<InspectedWolfsidePattern> singleAgentPossessedPattern = new ArrayList<InspectedWolfsidePattern>();

	/** �G�[�W�F���g���̍ł��Ó��ȘT�p�^�[�� idx=AgentNo */
	private ArrayList<InspectedWolfsidePattern> mostWolfPattern = new ArrayList<InspectedWolfsidePattern>();

	/** �G�[�W�F���g���̍ł��Ó��ȋ��l�p�^�[�� idx=AgentNo */
	private ArrayList<InspectedWolfsidePattern> mostPossessedPattern = new ArrayList<InspectedWolfsidePattern>();

	/** ��̘T�w�c�p�^�[��(Null�΍�) */
	private static final InspectedWolfsidePattern emptyWolfsidePattern = new InspectedWolfsidePattern(new WolfsidePattern(new ArrayList<Integer>(), new ArrayList<Integer>()), 0.0);

	
	//TODO �������ł̓f�[�^�\���݂̂ɂ��āA�i�[�͏�ʃ��W���[���ł��ׂ��H�N���X�̃p�b�P�[�W�ړ�������(ReceivedGuess���z�Q��)
	/**
	 * �R���X�g���N�^
	 * @param patterns �T�w�c�̃p�^�[��
	 * @param guessmanager ��������
	 * @throws IOException 
	 */
	public AnalysisOfGuess(int agentNum, Collection<WolfsidePattern> patterns, GuessManager guessManager, int guesscount, svm_model model, int today, int gamecount) throws IOException {
		
		// �S�p�^�[���̕��͌��� �������e�ʂ��w�肵�ď�����
		allPattern = new HashMap<String, InspectedWolfsidePattern>(patterns.size() * 4 / 3);

		// �X�̃G�[�W�F���g�P�̂ŘT�W���E���l�W�������߂�
		ArrayList<Double> singleWolfScore = new ArrayList<Double>();
		ArrayList<Double> singlePossessedScore = new ArrayList<Double>();
		// 1�I���W���ɂ��邽��0�Ԃ̗v�f�Ƀ_�~�[��ݒ�
		singleWolfScore.add(null);
		singlePossessedScore.add(null);
		/*File file = new File("c:\\Users\\omuricelove\\Documents\\AIWolf-ver0.4.9_2\\Svm_wolfscore\\wolfScoreOfSvm_game_"+gamecount+"_day_"+today+".txt");
		FileWriter filewriter = null;
		try {
			filewriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}*/
		for(int i = 1; i <= agentNum; i++){
			// �W���̏�����
			double wolfScore = 1.0;
			double possessedScore = 1.0;

			// �_�~�[�p�^�[���̍쐬
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
			// �����̑���
			for(ReceivedGuess rguess : singleGuesses ){
				
				// �����̓���p�^�[�����T�p�^�[���ƃ}�b�`���邩
				if( rguess.guess.condition.isMatch(wolfPattern) ){
						for(Integer key : rguess.guess.info.keySet()){
							input[key-1].value = rguess.guess.info.get(key);
							
						}
					
					//wolfScore *= Math.pow( rguess.guess.correlation, rguess.weight);		
				}
				// �����̓���p�^�[�������l�p�^�[���ƃ}�b�`���邩
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

			// �P�̃p�^�[���Ƃ��ċL��
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

		// �G�[�W�F���g���̍ł��Ó��ȃp�^�[���������o�^
		for(int i = 0; i <= agentNum; i++){
			mostWolfPattern.add(emptyWolfsidePattern);
			mostPossessedPattern.add(emptyWolfsidePattern);
		}

		// �T�w�c�p�^�[���̑���
		Iterator<WolfsidePattern> iter = patterns.iterator();
		while( iter.hasNext() ){
			WolfsidePattern pattern = iter.next();

			// ���̃p�^�[���Ɋ֘A���鐄��
			//List<ReceivedGuess> guesses = new ArrayList<ReceivedGuess>();

			double score = 1.0;
			// �e�T�̒P�̌W���̌v�Z
			for( int wolfAgentNo : pattern.wolfAgentNo ){
				score *= singleWolfScore.get(wolfAgentNo);
			}
			// �e���l�̒P�̌W���̌v�Z
			for( int posAgentNo : pattern.possessedAgentNo ){
				score *= singlePossessedScore.get(posAgentNo);
			}
			// �����̑���
			for(ReceivedGuess rguess : guessManager.getGuessForMultiAgent() ){
				// �����̏������T�w�c�p�^�[���ƃ}�b�`���邩
				if( rguess.guess.condition.isMatch(pattern) ){
					// �T�w�c�̃X�R�A��␳����
					score *= Math.pow( rguess.guess.correlation, rguess.weight);

					// ���̃p�^�[���Ɋ֘A���鐄�����L��
					//guesses.add(rguess);
				}
			}
			// �T�w�c�p�^�[���ɑ΂��錟�،��ʂ��i�[
			InspectedWolfsidePattern inspectedPattern = new InspectedWolfsidePattern(pattern, score);
			//inspectedPattern.guesses = guesses;

			addPattern(inspectedPattern);

			// �e�G�[�W�F���g�̘T/���l�Ƃ��čł��Ó��ȃp�^�[���𒀎��v�Z
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
	 * ���،��ʂ̋L��
	 * @param pattern
	 */
	public void addPattern(InspectedWolfsidePattern pattern){

		// ���،��ʂ̋L��
		allPattern.put(pattern.pattern.getWolfSideCode(), pattern);

	}


	/**
	 * ����̘T�w�c�Ɋւ��镪�͌��ʂ��擾����
	 * @return �w�肵���T�w�c�Ɋւ��镪�͌���
	 */
	public InspectedWolfsidePattern getPattern(WolfsidePattern pattern){

		return allPattern.get(pattern.getWolfSideCode());

	}


	//TODO ���\�b�h�ɂ���ă_�~�[��Ԃ��d�l��Null��Ԃ��d�l�����݂��ĂċC���������B���ꂷ��H
	/**
	 * �ł��Ó��ȘT�w�c�Ɋւ��镪�͌��ʂ��擾����
	 * @return �ł��Ó��ȘT�w�c�Ɋւ��镪�͌���(����X�R�A������ꍇ�A���Ԃ͕ۏ؂���Ȃ�)�@��������score0�̃_�~�[��Ԃ�
	 */
	public InspectedWolfsidePattern getMostValidPattern(){

		InspectedWolfsidePattern mostValidWolfsidePattern = emptyWolfsidePattern;
		double mostValidWolfScore = Double.NEGATIVE_INFINITY;

		// �T�w�c�p�^�[���̑���
		for( InspectedWolfsidePattern pattern : mostWolfPattern ){
			// �ő�X�R�A�ł���ΐw�c���L������
			if( pattern != null && pattern.score > mostValidWolfScore ){
				mostValidWolfsidePattern = pattern;
				mostValidWolfScore = pattern.score;
			}
		}

		return mostValidWolfsidePattern;

	}


	/**
	 * ����̃G�[�W�F���g���T�̃p�^�[���̂����A�ł��Ó��ȘT�w�c�Ɋւ��镪�͌��ʂ��擾����
	 * @param agentNo �G�[�W�F���g�ԍ�
	 * @return �ł��Ó��ȘT�w�c�Ɋւ��镪�͌���(����X�R�A������ꍇ�A���Ԃ͕ۏ؂���Ȃ�)�@��������score0�̃_�~�[��Ԃ�
	 */
	public InspectedWolfsidePattern getMostValidWolfPattern(int agentNo){

		return mostWolfPattern.get(agentNo);

	}


	/**
	 * ����̃G�[�W�F���g�����l�̃p�^�[���̂����A�ł��Ó��ȘT�w�c�Ɋւ��镪�͌��ʂ��擾����
	 * @param agentNo �G�[�W�F���g�ԍ�
	 * @return �ł��Ó��ȘT�w�c�Ɋւ��镪�͌���(����X�R�A������ꍇ�A���Ԃ͕ۏ؂���Ȃ�)�@��������score0�̃_�~�[��Ԃ�
	 */
	public InspectedWolfsidePattern getMostValidPossessedPattern(int agentNo){

		return mostPossessedPattern.get(agentNo);

	}


	/**
	 * �G�[�W�F���g�P�̂Ɋւ���l�T�̕��͌��ʂ��擾����
	 * @param agentNo �G�[�W�F���g�ԍ�
	 * @return
	 */
	public InspectedWolfsidePattern getSingleWolfPattern(int agentNo){

		return singleAgentPossessedPattern.get(agentNo - 1);

	}


	/**
	 * �G�[�W�F���g�P�̂Ɋւ��鋶�l�̕��͌��ʂ��擾����
	 * @param agentNo �G�[�W�F���g�ԍ�
	 * @return
	 */
	public InspectedWolfsidePattern getSinglePossessedPattern(int agentNo){

		return singleAgentPossessedPattern.get(agentNo - 1);

	}


	/**
	 * �S�p�^�[���̕��͌��ʂ��擾����
	 * @return
	 */
	public HashMap<String, InspectedWolfsidePattern> getAllPattern(){
		return allPattern;
	}
	

}
