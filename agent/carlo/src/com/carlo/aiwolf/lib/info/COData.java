package com.carlo.aiwolf.lib.info;

import org.aiwolf.common.data.*;

public class COData {
	private Agent agent;
	private Role coRole;
	private int talkIdx;
	private int day;
	/** カミングアウトでの情報追加かどうか。いきなり能力結果発言の場合はfalse */
	private boolean isComingOut;
	
	public COData(Agent agent,Role coRole,int day,int talkIdx,boolean isComingOut){
		this.agent=agent;
		this.coRole=coRole;
		this.day=day;
		this.talkIdx=talkIdx;
		this.isComingOut=isComingOut;
	}
	public Role getCORole(){
		return coRole;
	}
	public Agent getAgent(){
		return agent;
	}
	public int getDay(){
		return day;
	}
	public int getTalkIdx(){
		return talkIdx;
	}
	public boolean isComingOut(){
		return isComingOut;
	}
	
	public String toString(){
		return "coRole:"+coRole+" day:"+day+" idx:"+talkIdx+" isComingOut:"+isComingOut;
	}

}
