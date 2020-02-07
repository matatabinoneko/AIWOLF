package jp.ac.shibaura_it.ma15082;


import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

//NOTE:GameInfo dead_data,gurad_data�Ȃ�

public class WasabiAnalyzer {
	private GameInfo gameinfo;
	private GameSetting gamesetting;
	private ListMap<Agent,JudgeInfo> seer_data;
	private ListMap<Agent,JudgeInfo> medium_data;
	private ListMap<Agent,PlayerInfo> player_data;
	private List<LineInfo> lis;
	
	private TalkInfo talkinfo;
	private int talkindex;
	private int talkday;
	private int whisperindex;
	private int[] talkcount;
	List<Message> estimatelist;
	List<Pair<Agent,Agent>> votelist;
	List<Pair<Agent,Agent>> attacklist;
	private int dead_num;
	private int dead_s;
	private int dead_m;
	
	
	public WasabiAnalyzer(GameInfo gi,GameSetting gs){
		gameinfo=gi;
		gamesetting=gs;
		seer_data=new ListMap<Agent,JudgeInfo>();
		medium_data=new ListMap<Agent,JudgeInfo>();
		player_data=new ListMap<Agent,PlayerInfo>();
		talkinfo=new TalkInfo(gi);
		talkindex=0;
		talkday=-1;
		talkcount=new int[gi.getAgentList().size()+1];
		whisperindex=0;
		estimatelist=new ArrayList<Message>();
		votelist=new ArrayList<Pair<Agent,Agent>>();
		attacklist=new ArrayList<Pair<Agent,Agent>>();
		for(Agent a : gi.getAgentList()){
			player_data.put(a,new PlayerInfo(a));
		}
		dead_num=0;
		dead_s=0;
		dead_m=0;
		lis=new ArrayList<LineInfo>();
		
	}
	
	
	public int getRoleNum(Role role){
		return gamesetting.getRoleNum(role);
	}
	public int getDay(){
		return gameinfo.getDay();
	}
	public int getAliveSize(){
		return gameinfo.getAliveAgentList().size();
	}
	public int getPlayerNum(){
		return gamesetting.getPlayerNum();
	}
	/*
	public List<Agent> getAliveAgent(){
		return gameinfo.getAliveAgentList();
	}
	public List<PlayerInfo> getAlivePlayerInfo(){
		List<PlayerInfo> ret=new ArrayList<PlayerInfo>();
		for(int i=0;i<player_data.size();i++){
			if(player_data.getValue(i).isAlive()){
				ret.add(player_data.getValue(i));
			}
		}
		return ret;
	}*/
	
	
	

    private void countDeadPlayer(Agent agent) {
    	dead_num++;
        if (seer_data.containsKey(agent)) {
            dead_s++;
        } else if (medium_data.containsKey(agent)) {
            dead_m++;
        }
        return;
    }
	public void update(GameInfo gi){
		gameinfo=gi;
		List<Talk> talks=gameinfo.getTalkList();
		List<Talk> whispers=gameinfo.getWhisperList();		
		Agent agent;
		estimatelist.clear();
		votelist.clear();
		attacklist.clear();
		//�����f�[�^�̏�����
		if(getDay()!=talkday){
			talkday=getDay();
			talkindex=0;
			for(int i=0;i<talkcount.length;i++){
				talkcount[i]=0;
			}
			whisperindex=0;
		}
		
		//���S�f�[�^�̍X�V
		//�P����
		//agent=gameinfo.getAttackedAgent();
		for(Agent a : gameinfo.getLastDeadAgentList()){
			if(a!=null){
				player_data.get(a).setBitten();
				countDeadPlayer(a);
			}
		}
		//���Y��
		agent=gameinfo.getExecutedAgent();
		if(agent!=null){
			player_data.get(agent).setVoted();
			countDeadPlayer(agent);
		}
		//��E�җp�̏��Y��
		agent=gameinfo.getLatestExecutedAgent();
		if(agent!=null){
			player_data.get(agent).setVoted();
			countDeadPlayer(agent);
		}
				
		
		//�����̉��
		int count=0;
		//int prev=talkindex;
		for(;whisperindex<whispers.size();whisperindex++){
			Talk talk=whispers.get(whisperindex);
			Content content=new Content(talk.getText());
			
			agent=talk.getAgent();
			switch(content.getTopic()){
			case ATTACK:
				attacklist.add(new Pair<Agent,Agent>(agent,content.getTarget()));
				break;
			default://����ȊO�͋^���Ă��邩�M���Ă��邩�̔��肾������
				Colour c=Colour.analyze(content);
				if(c!=Colour.GREY){
					estimatelist.add(new Message(agent,content.getTarget(),c));
				}
				break;
			}
			
		}
		
		for(;talkindex<talks.size();talkindex++){
			Talk talk=talks.get(talkindex);
			Content content=new Content(talk.getText());
			agent=talk.getAgent();
			talkcount[agent.getAgentIdx()]++;
			switch(content.getTopic()){
			case OPERATOR:
				if(content.getOperator()==Operator.REQUEST){
					//List<Content> list=content.getContentList();
					
				}
				break;
				
			case COMINGOUT://CO ��\�Ɛ肢�������ׂ�B
				//�����̂��Ƃ������Ă��Ȃ������͖�������
				if(!agent.equals(content.getTarget())){
					player_data.get(agent).setRole(Role.WEREWOLF);;
					continue;
				}
				if(content.getRole()==Role.SEER && !seer_data.containsKey(agent)){
					seer_data.put(agent,new JudgeInfo(agent,gi.getDay()));
					boolean flag=player_data.get(agent).setRole(Role.SEER);
					if(flag){
						seer_data.remove(agent);
						medium_data.remove(agent);
					}
				}
				else if(content.getRole()==Role.MEDIUM && !medium_data.containsKey(agent)){
					medium_data.put(agent,new JudgeInfo(agent,gi.getDay()));
					boolean flag=player_data.get(agent).setRole(Role.MEDIUM);
					if(flag){
						seer_data.remove(agent);
						medium_data.remove(agent);
					}
				}
				else{
					boolean flag=player_data.get(agent).setRole(content.getRole());
					if(flag){
						seer_data.remove(agent);
						medium_data.remove(agent);
					}
				}
				break;
			case IDENTIFIED://sence
				//��\��CO���Ă���Ƃ������l������
				if(medium_data.containsKey(agent)){
					medium_data.get(agent).put(gi.getDay(), content.getTarget(), content.getResult(),talkcount[agent.getAgentIdx()]);
					player_data.get(content.getTarget()).setMedium(agent,content.getResult());
				}
				break;
			case DIVINED:
				//�肢��CO���Ă���Ƃ������l������
				if(seer_data.containsKey(agent)){
					seer_data.get(agent).put(gi.getDay(), content.getTarget(), content.getResult(),talkcount[agent.getAgentIdx()]);
					player_data.get(content.getTarget()).setSeer(agent,content.getResult());
				}
				break;
			
			case VOTE:
				votelist.add(new Pair<Agent,Agent>(agent,content.getTarget()));
				//break;
			default://����ȊO�͋^���Ă��邩�M���Ă��邩�̔��肾������
				Colour c=Colour.analyze(content);
				if(c!=Colour.GREY){
					estimatelist.add(new Message(agent,content.getTarget(),c));
				}
				break;
			}
			//�ǂݍ��ޔ�������������Ǝ��ԓ��ɏ����ł��Ȃ��B
			if(++count>20){
				break;
			}
		}
		
		talkinfo.setAttackMessages(attacklist);
		
		talkinfo.setMessages(estimatelist, gameinfo);
		talkinfo.setVoteMessages(votelist);
		talkinfo.calcScore();
		
		
		
		//�肢�E��\���C���̐M���x�̌v�Z
		lis.clear();
		List<PlayerInfo> plist=player_data.valueList();
		
		if(seer_data.size()+medium_data.size() > 8){
			Agent a=gameinfo.getAgent();
			lis.add(new LineInfo(null,null,plist,seer_data.size(),medium_data.size()));
			
			if(seer_data.containsKey(a)){
				lis.add(new LineInfo(seer_data.get(a), null, plist,seer_data.size(),medium_data.size()));
			}
			else if(medium_data.containsKey(a)){
				lis.add(new LineInfo(null, medium_data.get(a), plist,seer_data.size(),medium_data.size()));
			}
			return;
		}
		
		
		for(int s=0;s<seer_data.size();s++){
			JudgeInfo si=seer_data.getValue(s);
			for(int m=0;m<medium_data.size();m++){
				JudgeInfo mi=medium_data.getValue(m);
				lis.add(new LineInfo(si,mi,plist,seer_data.size(),medium_data.size()));
			}
			//int m=medium_data.size();
			JudgeInfo mi=null;
			lis.add(new LineInfo(si,mi,plist,seer_data.size(),medium_data.size()));
		}
		//int s=seer_data.size();
		JudgeInfo si=null;
		for(int m=0;m<medium_data.size();m++){
			JudgeInfo mi=medium_data.getValue(m);
			lis.add(new LineInfo(si,mi,plist,seer_data.size(),medium_data.size()));
		}
		//int m=medium_data.size();
		JudgeInfo mi=null;
		lis.add(new LineInfo(si,mi,plist,seer_data.size(),medium_data.size()));
		

	}
	
	
	
	
	
	public List<PlayerInfo> getPlayerInfos(){
		return player_data.valueList();
	}

	
	public double getScore(Agent from,Agent to,int d){
		
		return talkinfo.getScore(from, to, d);
	}
	
	
	public int seerSize(){
		return seer_data.size()+1;
	}
	public int mediumSize(){
		return medium_data.size()+1;
	}
	public int seerDead(){
		return dead_s;
	}
	public int mediumDead(){
		return dead_m;
	}
	
	public List<Agent> getSeerList(){
		List<Agent> ret=new ArrayList<Agent>(seer_data.keyList());
		ret.add(null);
		return ret;
	}
	
	public List<Agent> getMediumList(){
		List<Agent> ret=new ArrayList<Agent>(medium_data.keyList());
		ret.add(null);
		return ret;
	}
	
	public List<JudgeInfo> getSeerInfos(){
		return seer_data.valueList();
	}
	public List<JudgeInfo> getMediumInfos(){
		return medium_data.valueList();
	}
	
	
	public LineInfo getLineInfo(Agent seer,Agent medium){
		for(LineInfo li : lis){
			if(
				((li.getSeer()==null && seer==null) || (li.getSeer()!=null && li.getSeer().getAgent().equals(seer)))
				&& 
				((li.getMedium()==null && medium==null) || (li.getMedium()!=null && li.getMedium().getAgent().equals(medium)))
			
			){
				return li;
			}
			
		}
		
		return null;
	}


	public List<LineInfo> getLineInfos() {
		return lis;
	}
	
	public TalkInfo getTalkInfo(){
		return talkinfo;
	}
	
	
	public JudgeInfo getSeerInfo(Agent key){
		return seer_data.get(key);
	}
	public JudgeInfo getMediumInfo(Agent key){
		return medium_data.get(key);
	}
		
	public List<Vote> getLatestVoteList(){
		return gameinfo.getLatestVoteList();
	}
	public List<Vote> getLatestAttackVoteList(){
		return gameinfo.getLatestAttackVoteList();
	}
	
	public Agent getLatestAttack(){
		return gameinfo.getAttackedAgent();
	}
	
}
