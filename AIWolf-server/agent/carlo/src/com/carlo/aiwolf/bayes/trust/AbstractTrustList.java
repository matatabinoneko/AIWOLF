package com.carlo.aiwolf.bayes.trust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import com.carlo.aiwolf.lib.info.CauseOfDeath;
import com.carlo.aiwolf.lib.info.GameInfoManager;
/**
 *  TrustList がWeka使うのと使わない（ベタ打ち）の２つになったので、抽象クラスを作成
 *  TrustListは必要ないのもオーバーライドしているのでそのうち直したい
 * @author info
 *
 */
public abstract class  AbstractTrustList {
	public Map<Agent,Double> trustMap=new HashMap<Agent,Double>();
	/**  コンソール出力 */
	public boolean isShowConsoleLog=false;
	
	protected Map<String,Correct> seerCorrectMap;
	


	protected GameInfoManager gameInfoMgr;
	
	public List<Agent> getSortedRoleCOAgentList(Role coRole,boolean isAliveOnly){
		List<Agent> sortedAgentList=new ArrayList<Agent>();

		for(Agent agent:gameInfoMgr.getCOInfo().getCOAgentList(coRole,isAliveOnly,true)){
			if(trustMap.containsKey(agent)==false) continue;
			if(sortedAgentList.size()==0) sortedAgentList.add(agent);
			else{
				int i=0;
				for(Agent sortedAgent :sortedAgentList){
					if(trustMap.containsKey(sortedAgent)==false) continue;
					if(trustMap.get(agent)<trustMap.get(sortedAgent)){
						sortedAgentList.add(i, agent);
						break;
					}
					i++;
				}
				if(!sortedAgentList.contains(agent)) sortedAgentList.add(agent);
			}
		}
		return sortedAgentList;
	}
	/**
	 * 
	 * @param isAliveOnly
	 * @return
	 */
	public List<Agent> getSortedAgentList(boolean isAliveOnly){
		List<Agent> sortedAgentList=new ArrayList<Agent>();

		for(Agent agent:gameInfoMgr.getAgentListExceptMe(isAliveOnly)){
			if(trustMap.containsKey(agent)==false) continue;
			if(sortedAgentList.size()==0) sortedAgentList.add(agent);
			else{
				int i=0;
				for(Agent sortedAgent :sortedAgentList){
					if(trustMap.containsKey(sortedAgent)==false) continue;
					if(trustMap.get(agent)<trustMap.get(sortedAgent)){
						sortedAgentList.add(i, agent);
						break;
					}
					i++;
				}
				if(!sortedAgentList.contains(agent)) sortedAgentList.add(agent);
			}
		}
		return sortedAgentList;
	}

	public List<Agent> getSortedAgentList(boolean isAliveOnly,double point){
		List<Agent> sortedAgentList=new ArrayList<Agent>();

		for(Agent agent:gameInfoMgr.getAgentListExceptMe(isAliveOnly)){
			if(trustMap.containsKey(agent)==false) continue;
			if(trustMap.get(agent)>point) continue;
			if(sortedAgentList.size()==0) sortedAgentList.add(agent);
			else{
				int i=0;
				for(Agent sortedAgent :sortedAgentList){
					if(trustMap.containsKey(sortedAgent)==false) continue;
					if(trustMap.get(agent)<trustMap.get(sortedAgent)){
						sortedAgentList.add(i, agent);
						break;
					}
					i++;
				}
				if(!sortedAgentList.contains(agent)) sortedAgentList.add(agent);
			}
		}
		return sortedAgentList;
	}


	public double getTrustPoint(Agent agent){
		return trustMap.get(agent);
	}
	/**
	 * 死亡情報から信用度計算
	 * @param deadAgent 死んだエージェント
	 * @param cause 死因(死んだかどうか)
	 */
	public abstract void changeDeadAgent(Agent deadAgent,CauseOfDeath cause);
	public void printTrustList(){
		if(isShowConsoleLog){
			System.out.println("\nAgent[番号]\t信用度\t生存\tCO");
			for(Entry<Agent, Double> entry : trustMap.entrySet()) {
				System.out.print(entry.getKey()+"\t");
				System.out.printf("%.3f",entry.getValue());
				System.out.print("\t"+gameInfoMgr.isAlive(entry.getKey()));
				System.out.println("\t"+gameInfoMgr.getCOInfo().getCoRole(entry.getKey()));
			}
			System.out.println();
		}
	}
	public void printTrustList(GameInfo finishedGameInfo){
		if(isShowConsoleLog){
			System.out.println("\nAgent[番号]\t信用度\t生存\tCO\t役職");
			for(Entry<Agent, Double> entry : trustMap.entrySet()) {
				System.out.print(entry.getKey()+"\t");
				System.out.printf("%.3f",entry.getValue());
				System.out.print("\t"+gameInfoMgr.isAlive(entry.getKey()));
				System.out.print("\t"+gameInfoMgr.getCOInfo().getCoRole(entry.getKey()));
				System.out.println("\t"+finishedGameInfo.getRoleMap().get(entry.getKey()));
			}
			System.out.println();
		}
	}
	/** 結果ネットワークのarffファイル作成用 */
	public void printTrustListForCreatingData(Map<Agent,Role> roleMap){
		//System.out.println("\n信用度,CO,役職,占いCO数,霊能CO数");
		for(Entry<Agent, Double> entry : trustMap.entrySet()) {
			//System.out.print(entry.getKey()+",");
			//System.out.print(agentInfo.getDayAgentDied(agentInfo.getMyAgent()));
			double trustP=entry.getValue();
			String label="";
			if(trustP<25)  label="low";
			else if(trustP<45) label="little_low";
			else if(trustP<56) label="middle";
			else if(trustP<76) label="little_high";
			else label="high";
			System.out.print(""+label);
			//System.out.printf(",%.3f",entry.getValue());
			System.out.print(","+gameInfoMgr.getCOInfo().getCoRole(entry.getKey()));
			System.out.print(","+roleMap.get(entry.getKey()));
			System.out.print(","+gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.SEER));
			System.out.println(","+gameInfoMgr.getCOInfo().getNumOfCOAgent(Role.MEDIUM));
			//System.out.println(","+agentInfo.getDayAgentDied(entry.getKey()));
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
	public abstract void changeSeerTrust(Agent agent,int day,Species species,Correct correct,boolean reverse,Agent target,Correct attacked);
	/**
	 *  霊能結果発言から信用度計算
	 * @param agent
	 * @param targetSpecies
	 * @param day
	 */
	public abstract void changeMediumTrust(Agent agent,Species targetSpecies,int day);
	/** 
	 *  投票結果から信用度計算
	 * @param agent 投票者
	 * @param targetSpecies 投票先の種族
	 * @param day 投票日
	 * @param assist  booleanをstringにしたもの
	 * TODO:booleanを引数にして、こっちでStringにするべきかも
	 */
	public abstract void changeVoterTrust(Agent agent,Species targetSpecies,int day,String assist);
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

	/**
	 * threshold,marginが村人陣営の確率なら、reverseはfalse<br>
	 *  Agentの信用度 +=(margin-threshold)*100 <br>
	 *  if(reverse)  (threshold-margin)   <br>
	 *  
	 */
	protected double changeTrust(Agent agent,double margin,double threshold,boolean reverse){
		//System.out.print("*");
		double point;
		if(reverse) point=(threshold-margin)*100;
		else point=(margin-threshold)*100;
		double pre=trustMap.get(agent);
		double result;
		//事後確率は1%を切るようなら、それは破綻とみなし一気にポイントを下げる
		if(margin<0.01) point=-100;
		
		//if(reverse) result=addTrust(agent,-point);
		//else result=addTrust(agent,point);
		
		result=addTrust(agent,point);
		if(isShowConsoleLog){
			System.out.print(""+agent);
			if(reverse) System.out.printf("%.2f-%.2f(%.2f-%.2f)=%.2f\n",pre,point,margin,threshold,result);
			else System.out.printf("%.2f+%.2f(%.2f-%.2f)=%.2f\n",pre,point,margin,threshold,result);
		}
		return point;
	}
	/**
	 *  0<=trustPoint<=100 になるように計算
	 * @param agent
	 * @param point
	 * @return
	 */
	protected double addTrust(Agent agent,double point){
		double beforePoint=trustMap.get(agent);
		double afterPoint=beforePoint+point;
		if(afterPoint>100) afterPoint=100;
		else if(afterPoint<0) afterPoint=0;
		trustMap.put(agent, afterPoint);
		return afterPoint;
	}



}
