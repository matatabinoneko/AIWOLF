package com.carlo.aiwolf.lib.info;

public class DeathData {
	private CauseOfDeath cause;
	private int day; 
	public DeathData(int day,CauseOfDeath cause){
		this.day=day;
		this.cause=cause;
	}
	public CauseOfDeath getCause(){
		return cause;
	}
	public int getDay(){
		return day;
	}

}
