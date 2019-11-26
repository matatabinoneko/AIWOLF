package com.carlo.aiwolf.lib.info;

import java.util.List;

import org.aiwolf.common.data.Agent;



/**
 * あるAgentが占われてどの判定を出されたのかをまとめたもの
 * @author carlo
 *
 */
public class DivinedResult {
	/** 占われたAgent */
	private Agent divineTarget;
	/** divineTargetを占って、人狼判定を出した占い師のリスト */
	private List<Agent> seerJudgedWerewolf;
	/** divineTargetを占って、村人判定を出した占い師のリスト */
	private List<Agent> seerJudgedHuman;
	public DivinedResult(Agent divineTarget,List<Agent> seerJudgedWerewolf,List<Agent> seerJudgedHuman){
		this.divineTarget=divineTarget;
		this.seerJudgedWerewolf=seerJudgedWerewolf;
		this.seerJudgedHuman=seerJudgedHuman;
	}
	public List<Agent> getSeerListJudgedWerewolf(){
		return seerJudgedWerewolf;
	}
	public List<Agent> getSeerListJudgedHuman(){
		return seerJudgedHuman;
	}
	public Agent getDivineTarget(){
		return divineTarget;
	}
	public  DivinedType getDivinedType(){
		if(seerJudgedWerewolf.size()>0 && seerJudgedHuman.size()>0) return DivinedType.PANDA;
		else if(seerJudgedWerewolf.size()>0) return DivinedType.BLACK;
		else if(seerJudgedHuman.size()>0) return DivinedType.WHITE;
		return DivinedType.NONE;
	}
	
	public String toString(){
		return getDivinedType().toString();
	}

}
