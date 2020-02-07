package com.carlo.aiwolf.tool;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * 人狼ゲームのログ出力をすっきりと見せるクラス
 * @author info
 *
 */
public class ConsoleLogPrinter {
	
	String consoleLog;
	String[] agentList=new String[16];
	/**
	 * skipとoverを省略してログ出力
	 * @param scanner
	 */
	public ConsoleLogPrinter(Scanner scanner){
		//this.consoleLog=consoleLog;
		readStartStatus(scanner);
		String line="";
		do{
			line=scanner.nextLine();
			if(line.contains("Over") || line.contains("Skip")){
				
			}
			else System.out.println(replaceLine(line));
		}while(line.contains("Winner")==false);
	}
	private void readStartStatus(Scanner scanner){
		for(int i=1;i<=15;i++){
			String line=scanner.nextLine();
			agentList[i]=line.split(",")[5];
		}
	}
	private String replaceLine(String line){
		String[] splitedLine=line.split(",");
		
		switch(splitedLine[1]){
		case "status":
			break;
		case "whisper":
		case "talk":
			int agentIdx=Integer.parseInt(splitedLine[3]);
			splitedLine[3]=agentList[agentIdx]+"["+agentIdx+"]";
			break;
		}
		return String.join(",", splitedLine);
	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("ログをコピペで貼り付けてください");
		Scanner scanner=new Scanner(System.in);
		ConsoleLogPrinter printer=new ConsoleLogPrinter(scanner);

	}

}
