package jp.ac.shibaura_it.ma15082.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.ac.shibaura_it.ma15082.Colour;
import jp.ac.shibaura_it.ma15082.JudgeInfo;
import jp.ac.shibaura_it.ma15082.LineInfo;
import jp.ac.shibaura_it.ma15082.ListMap;
import jp.ac.shibaura_it.ma15082.Pair;
import jp.ac.shibaura_it.ma15082.Personality;
import jp.ac.shibaura_it.ma15082.PersonalityFactory;
import jp.ac.shibaura_it.ma15082.PlayerInfo;
import jp.ac.shibaura_it.ma15082.TalkInfo;
import jp.ac.shibaura_it.ma15082.Tools;
import jp.ac.shibaura_it.ma15082.WasabiAnalyzer;


public class WasabiPlayer implements Player{

	private String name;
	private Agent agent;
	private GameInfo gi;
	private Personality personality;
	private TalkFactory talk_factory;
	
	//î•ñ‚Ì‰ğÍ
	private WasabiAnalyzer wa;
	//©•ª‹“_‚Ìî•ñ
	private List<PlayerInfo> view;
	//åŠÏ“I‚ÈM—Š“x
	private ListMap<Agent,Double> score;
	private ListMap<ListMap<Agent,Double>,Double> scores;
	
	//˜TE‹¶l‚ÌƒŠƒXƒg
	private ListMap<Agent,Pair<Role,Double>> wolfs;
	private List<Agent> lunatics;
	//©•ª‚ªéx‚é–ğE
	private Role myRole;

	private List<String> role_mess;
	private int skip_count;
	private double th;
	
	boolean firstflag;
	private Agent vote_target;
	private Agent attack_target;
	
	//ƒNƒ‰ƒXƒ^•ªŠ„”
	final int v_num=4;
	final int d_num=4;
	final int a_num=4;
	final int g_num=3;
	private ListMap<Role,ListMap<Personality,Team>> datamap=null;
	
	public Agent getAgent(){
		return agent;
	}
	public Role getRole(){
		return gi.getRole();
	}
	public Personality getPersonality(){
		return personality;
	}
	public void setDataMap(ListMap<Role,ListMap<Personality,Team>> map){
		datamap=map;
	}
	public ListMap<Role,ListMap<Personality,Team>> getDataMap(){
		return datamap;
	}
	public int getWinNum(){
		int sum=0;
		for(int i=0;i<datamap.size();i++){
			Role role=datamap.getKey(i);
			List<Team> teams=datamap.getValue(i).valueList();
			for(Team t : teams){
				if(role.getTeam()==t){
					sum++;
				}
			}
		}
		return sum;
	}
	
	public int getGameNum(){
		int sum=0;
		for(int i=0;i<datamap.size();i++){
			sum+=datamap.getValue(i).size();
		}
		return sum;
	}
	public int getGameNum(Role role){
		return datamap.get(role).size();
	}
	public int getWinNum(Role role){
		int sum=0;
		List<Team> teams=datamap.get(role).valueList();
		for(Team t : teams){
			if(role.getTeam()==t){
				sum++;
			}
		}
		return sum;
	}
	
	@Override
	public void initialize(GameInfo arg0, GameSetting arg1) {
		gi=arg0;
		agent=gi.getAgent();
		name="WASABI_"+agent.getAgentIdx();
		view=new ArrayList<PlayerInfo>(16);;
		score=new ListMap<Agent,Double>(16);
		scores=new ListMap<ListMap<Agent,Double>,Double>();
		
		talk_factory=TalkFactory.getInstance();
		
		if(datamap==null){
			datamap=new ListMap<Role,ListMap<Personality,Team>>();
			for(Role r : Role.values()){
				datamap.put(r,new ListMap<Personality,Team>());
			}
		}
		
		
		

		personality=PersonalityFactory.getPersonality(gi.getRole());
		/*
		if(agent.getAgentIdx()==1){
			personality=PersonalityFactory.getPersonality(gi.getRole());
			//personality=PersonalityFactory.getLearnedPersonality(gi.getRole(),datamap);
		}
		else{
			personality=PersonalityFactory.getRandomPersonality();
		}*/
	
		
		
		wolfs=new ListMap<Agent,Pair<Role,Double>>();
		lunatics=new ArrayList<Agent>();

		wa=new WasabiAnalyzer(arg0,arg1);
		LineInfo.wa=wa;
		role_mess=new ArrayList<String>();
		
		//NOTE:éx‚è‚ÌŒvZ‚Í‚Å‚«‚È‚¢
		//˜T‚ªéx‚ç‚È‚¢‚©‚ç‹¶l‚ªéx‚é
		if(gi.getRole()==Role.WEREWOLF){
			for(Agent a : gi.getAgentList()){
				if(gi.getRoleMap().get(a)==Role.WEREWOLF){
					wolfs.put(a,null);
				}
			}
			if(arg1.getPlayerNum()==5){
				myRole=Role.SEER;
			}
		}
		
		else if(gi.getRole()==Role.POSSESSED){
			myRole=Role.SEER;
		}
		th=0;
		vote_target=null;
		attack_target=null;
		firstflag=true;
		return;
	}

	
	

	@Override
	public void update(GameInfo arg0) {
		gi=arg0;
		wa.update(gi);
		view.clear();
		scores.clear();
		for(PlayerInfo p : wa.getPlayerInfos()){
			PlayerInfo pi=new PlayerInfo(p);
			view.add(pi);
			pi.setCertain(0.0);
			if(pi.getAgent().equals(agent)){
				score.put(pi.getAgent(),1.0);//©•ª‚Íâ‘Î‚É”’
			}
			
			else{
				score.put(pi.getAgent(), Tools.random()*personality.getWeightRandom()+(1-personality.getWeightRandom())*wa.getScore(agent,pi.getAgent(),gi.getDay()));//åŠÏ“I‚È‹^‚¢
			}
		}
		
		for(LineInfo li : wa.getLineInfos()){
			ListMap<Agent,Double> lm=li.getScoreList();
			scores.add(lm, li.getScore() * lm.get(agent));
		}
		Tools.unit(scores);
		int count=0;
		th=0;
		for(PlayerInfo pi : view){
			Agent p=pi.getAgent();
			double p_score=score.get(p);
			for(int i=0;i<scores.size();++i){
				double t_score =scores.getKey(i).get(p);
				pi.setCertain(pi.getCertain()+scores.getValue(i)*calcCertain(t_score,p_score,personality.getWeightSubjective()));
			}
			if(pi.isAlive() && !agent.equals(pi.getAgent())){
				th+=pi.getCertain();
				count++;
			}
		}
		if(count>0){
			th/=count;
		}
		
		
		for(PlayerInfo pi : view){
			if(pi.getRole().getTeam()==Team.WEREWOLF){
				pi.setCertain(0);
			}
		}

		//System.err.println(wa.getAliveSize()+" "+wa.getAlivePlayerInfo().size());
		return;
	}
	

	final private static double calcCertain(double a,double b,double r){
		double f=3*r*a*(1-a);
		return (f*b+(1-f)*a);
	}


	//NOTE:mess,role_mess‚ÍdayStart‚Åİ’è
	//role_mess‚Í‚Ç‚¿‚ç‚àCO,Œ‹‰Ê‚Ì2è
	//0“ú–Ú‚Í‚È‚É‚à‚µ‚È‚¢
	//1“ú–Ú‚©‚ç•K‚¸CO‚·‚é
	@Override
	public void dayStart() {
		Judge j;
		if(gi.getDay()<=0){
			//mess=getTalkList();
			return;
		}
		role_mess.clear();
		switch(gi.getRole()){
		case SEER:
			j=gi.getDivineResult();
			if(firstflag){
				role_mess.add(talk_factory.comingout(agent,Role.SEER).getText());
				firstflag=false;
			}
			if(j!=null){
				role_mess.add(talk_factory.divined(j.getTarget(),j.getResult()).getText());
			}
			break;
		case MEDIUM:
			j=gi.getMediumResult();
			if(firstflag){
				role_mess.add(talk_factory.comingout(agent,Role.MEDIUM).getText());
				firstflag=false;
			}
			if(j!=null){
				role_mess.add(talk_factory.inquested(j.getTarget(),j.getResult()).getText());
			}
			break;
		case POSSESSED:
			if(myRole==Role.SEER){
				wolf_divine();
			}
			break;
		case WEREWOLF:
			if(myRole==Role.SEER){
				wolf_divine();
			}
			break;
		default:
			break;
			
		}
		
		skip_count=0;
		vote_target=null;
		attack_target=null;
	}	
	
	@Override
	public void finish() {
		if(datamap!=null && datamap.get(gi.getRole())!=null){
			Map<Agent,Role> map=gi.getRoleMap();
			boolean vwin=true;
			for(Agent a : gi.getAliveAgentList()){
				if(map.get(a).getSpecies()==Species.WEREWOLF){
					vwin=false;
					break;
				}
			}
			datamap.get(gi.getRole()).add( personality, vwin?Team.VILLAGER:Team.WEREWOLF);
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Agent vote() {
		List<Vote> latest=wa.getLatestVoteList();
		//Ä“Š•[
		if(latest!=null && latest.size()>0){
			List<Pair<Agent,Agent>> list=new ArrayList<Pair<Agent,Agent>>();
			
			int[] v_counter=new int[wa.getPlayerNum()+1];
			for(Vote v : latest){
				list.add(new Pair<Agent,Agent>(v.getAgent(),v.getTarget()));
				int index=v.getTarget().getAgentIdx();
				v_counter[index]++;	
			}
			
			int min=Integer.MAX_VALUE;
			int max=-1;
			for(Vote v : latest){
				int index=v.getTarget().getAgentIdx();
				if(v_counter[index]<min){
					min=v_counter[index];
				}
				if(v_counter[index]>max){
					max=v_counter[index];
				}
			}
			wa.getTalkInfo().setVoteMessages(list);
			
			//System.err.println("vote"+latest);
			ListMap<Agent,Double> votelist=getVoteList();
			int counter=v_counter[vote_target.getAgentIdx()];
			//System.err.println(agent+" "+min+" "+max+" "+counter);
			//“Š•[æ‚ªÅ‘å“¾•[”‚Ì‚Æ‚«‚Í•K‚¸•Ï‚¦‚È‚¢
			if((max-min)>1 && min<counter && max<=counter){
				//System.err.println(agent+" "+vote_target);
				return vote_target;
			}
			//“Š•[æ‚ªÅ¬“¾•[”‚Ì‚Æ‚«‚Í•K‚¸•Ï‚¦‚é
			if(counter<=min || counter<max){
				votelist.put(vote_target, 0.0);
			}
			Agent p=Tools.selectKey(votelist);
			if(p!=null){
				//System.err.println(agent+" "+vote_target+" "+p);
				//System.err.flush();
				vote_target=p;
			}
			
			return vote_target;
		}
		
		if(vote_target!=null){
						
			return vote_target;
		}
		ListMap<Agent,Double> votelist=getVoteList();
		vote_target=Tools.selectKey(votelist);
		if(vote_target==null){
			vote_target=getOne();
		}
		
		return vote_target;
	}


	//NOTE:”CˆÓ‚Å€‘Ì‚È‚µ‚ªo‚¹‚é
	//4l‚Ì‚Æ‚«‚Í€‘Ì‚È‚µ‚É‚·‚é
	//5l‚Ì‚Æ‚«‚É“Š•[‚Åˆêl‘Şê‚·‚é‚©‚çc‚èl”5l‚Å€‘Ì‚È‚µ‚ğ‚¾‚·
	@Override
	public Agent attack() {
		List<Vote> latest=wa.getLatestAttackVoteList();
		//Ä“Š•[
		if(latest!=null && latest.size()>0){
			List<Pair<Agent,Agent>> list=new ArrayList<Pair<Agent,Agent>>();
			
			int[] v_counter=new int[wa.getPlayerNum()+1];
			for(Vote v : latest){
				list.add(new Pair<Agent,Agent>(v.getAgent(),v.getTarget()));
				int index=v.getTarget().getAgentIdx();
				v_counter[index]++;	
			}
			
			int min=Integer.MAX_VALUE;
			int max=-1;
			for(Vote v : latest){
				int index=v.getTarget().getAgentIdx();
				if(v_counter[index]<min){
					min=v_counter[index];
				}
				if(v_counter[index]>max){
					max=v_counter[index];
				}
			}
			wa.getTalkInfo().setAttackMessages(list);
			
			ListMap<Agent,Double> attacklist=getAttackList();
			int counter=v_counter[attack_target.getAgentIdx()];
			//“Š•[æ‚ªÅ‘å•[”‚Ì‚Æ‚«‚Í•K‚¸•Ï‚¦‚È‚¢
			if((max-min)>1 && min<counter && max<=counter){
				return attack_target;
			}
			//“Š•[æ‚ªÅ¬•[”‚Ì‚Æ‚«‚Í•K‚¸•Ï‚¦‚é
			if(counter<=min || ((max-min)>1 && counter<max)){
				attacklist.put(attack_target, 0.0);
			}
			Agent p=Tools.selectKey(attacklist);
			if(p!=null){
				attack_target=p;
			}
			
			return attack_target;
		}
		if(attack_target!=null){
			return attack_target;
		}
		/*
		if(gi.getAliveAgentList().size()==5){
			return null;
		}*/
		ListMap<Agent,Double> attacklist=getAttackList();
		attack_target=Tools.selectKey(attacklist);
		if(attack_target==null){
			attack_target=getOneWithoutWolf();
		}
		return attack_target;
	}

	@Override
	public Agent divine() {
		ListMap<Agent,Double> divinelist=getDivineList();
		Agent p=Tools.selectKey(divinelist);
		if(p==null){
			p=getOne();
		}
		return p;
	}

	@Override
	public Agent guard() {
		ListMap<Agent,Double> guardlist=getGuardList();
		Agent p=Tools.selectKey(guardlist);
		if(p==null){
			p=getOne();
		}
		return p;
	}


	
	public boolean coWolf(){
		//‚·‚Å‚É‚b‚n‚µ‚Ä‚¢‚é
		List<Agent> slist=wa.getSeerList();
		if(slist.contains(agent)){
			return true;
		}
		//‚Ü‚¾CO‚µ‚Ä‚¢‚È‚¢
		//‚P“ú–Ú‚Ì‚Æ‚«
		if(wa.getDay()<=1){
			List<JudgeInfo> jlist=wa.getSeerInfos();			
			//‚PˆÈã‚ÌCO”‚Å‚ ‚é‚Æ‚«‚b‚n‚µ‚È‚¢
			if(jlist.size()>1){
				return false;
			}
			//1CO‚Å•‚à‚ç‚¢‚Ì‚Æ‚«‚Í‚b‚b‚n
			for(JudgeInfo j : jlist){
				Colour c=j.getColour(agent);
				if(c==Colour.BLACK){
					return true;
				}
			}
		}
		//‚Q“ú–Ú‚Ì‚Æ‚«
		else{
			//ö•š‚·‚é
		}
		return false;
	}
	//NOTE:ƒƒbƒZ[ƒW‚ÍgetTalkLst‚Å‚Ü‚Æ‚ß‚Äæ“¾‚¹‚¸getTalk‚Å–ˆ‰ñæ“¾‚·‚é
	//talk1‰ñ‚Å1ƒƒbƒZ[ƒW‚µ‚©‘—M‚Å‚«‚È‚¢Aupdate‚Ì‰e‹¿‚ğl—¶‚Å‚«‚é
	@Override
	public String talk() {
		if(role_mess!=null && role_mess.size()>0){
			//l˜T‚¾‚Á‚½‚çğŒŸ‘æ‚Åè‚¢‚b‚n‚·‚é
			if(getRole()!=Role.WEREWOLF || coWolf()){
				return role_mess.remove(0);
			}
		}
		return getTalk();
	}

	@Override
	public String whisper() {
		return getWhisper();
	}

	
	//NOTE:–ğE‚b‚n‚µ‚½’†‚Å–µ‚‚µ‚½‚à‚Ì‚¾‚¯ƒ`ƒFƒbƒN‚·‚é
	public void searchLunatics(){
		lunatics.clear();
		for(JudgeInfo ji : wa.getSeerInfos()){
			for(Judge j : ji.getJudgeList()){
				if(wolfs.containsKey(j.getTarget())){
					if(j.getResult()==Species.HUMAN){
						lunatics.add(ji.getAgent());
						break;
					}
				}
				else{
					if(j.getResult()==Species.WEREWOLF){
						lunatics.add(ji.getAgent());
						break;
					}
				}
			}
		}
		for(JudgeInfo ji : wa.getMediumInfos()){
			for(Judge j : ji.getJudgeList()){
				if(wolfs.containsKey(j.getTarget())){
					if(j.getResult()==Species.HUMAN){
						lunatics.add(ji.getAgent());
						break;
					}
				}
				else{
					if(j.getResult()==Species.WEREWOLF){
						lunatics.add(ji.getAgent());
						break;
					}
				}
			}
		}
		return;
	}

	public void wolf_divine() {
        ListMap<Agent, Double> divinelist = getDivineList();
        Agent p = Tools.selectKey(divinelist);
        if (p == null) {
            p = getOne();
        }

        double a = 0;
        boolean flag = wolfs.containsKey(p);
        Species c;
        for (PlayerInfo pi : view) {
            if (pi.getAgent().equals(p)) {
                a = pi.getCertain();
                break;
            }
        }
        if (flag && 0.3 < a) {
            c = Species.HUMAN;
        } else if ((wa.mediumSize() - 1) == wa.getRoleNum(Role.MEDIUM) && (wa.mediumDead() < wa.getRoleNum(Role.MEDIUM) && Tools.random() < 0.5)) {
            c = Species.HUMAN;
        } else if (a < 0.3) {
            c = Species.WEREWOLF;
        } else {
            c = (Tools.random() < a) ? Species.HUMAN : Species.WEREWOLF;
        }
        if (c == Species.WEREWOLF) {
            JudgeInfo si = wa.getSeerInfo(agent);
            if (si == null) {
            } else if (a > 0.5 && si.blackNum() + 1 >= wa.getRoleNum(Role.WEREWOLF)) {
                c = Species.HUMAN;
            } else if (p.equals(wa.getLatestAttack())) {
                c = Species.HUMAN;
            }
        } else if (c == Species.HUMAN) {
            JudgeInfo si = wa.getSeerInfo(agent);
            if (si == null) {
            } else if (a < 0.5 && (wa.getAliveSize() - 1) <= 2 * (wa.getRoleNum(Role.WEREWOLF) - si.blackNum())) {
                c = Species.WEREWOLF;
            }
        }
        if(firstflag){
			role_mess.add(talk_factory.comingout(agent,Role.SEER).getText());
			firstflag=false;
		}
		role_mess.add(talk_factory.divined(p,c).getText());
        return;

    }

	
	final private Agent getOneWithoutWolf(){
        List<Agent> list = gi.getAliveAgentList();
        list.remove(agent);
        for (Agent a : wolfs.keySet()) {
            list.remove(a);
        }

        return list.get(Tools.rand(list.size()));

    }

    final private Agent getOne() {
        List<Agent> list = gi.getAliveAgentList();
        list.remove(agent);
        return list.get(Tools.rand(list.size()));

    }

	public ListMap<Agent,Double> getAliveList(){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				ret.add(pi.getAgent(), 1.0);
			}
			else{
				ret.add(pi.getAgent(), 0.0);
			}
		}
		return ret;
	}

	//true->white false->black
	public ListMap<Agent,Double> getWhiteAliveList(){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		boolean nonzero=false;
		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				double x=(pi.getCertain());
				if(x>0){
					nonzero=true;
				}
				ret.add(pi.getAgent(),x);
			} else {
				ret.add(pi.getAgent(),0.0);
			}
		}

		if(nonzero){
			return ret;
		}
		return getAliveList();
	}

	public ListMap<Agent,Double> getBlackAliveList(){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		boolean nonzero=false;
		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				double x=(1.0-pi.getCertain());
				if(x>0){
					nonzero=true;
				}
				ret.add(pi.getAgent(),x);
			} else {
				ret.add(pi.getAgent(),0.0);
			}
		}

		if(nonzero){
			return ret;
		}
		return getAliveList();
	}


    public Agent getPowerPlay5() {
        Agent ret = null;
        if (wa.getAliveSize() < 5) {
            List<Agent> targets = gi.getAliveAgentList();
            targets.remove(agent);
            
            switch (getRole()) {
                case WEREWOLF:
                	
                    List<Vote> votelist=wa.getLatestVoteList(); 
                  //Ä“Š•[
            		if(votelist!=null && votelist.size()>0){
	                    //3l‚Ì‚Æ‚«‚Í©•ª‚Ì“Š•[‚ğ“ü‚ê‘Ö‚¦‚éB
	                	if(wa.getAliveSize()==3){
	                		for(Vote v : votelist){
	                			if(v.getAgent().equals(agent)){
	                				targets.remove(v.getTarget());
	                				break;
	                			}
	                		}
	                		if(targets.size()==1){
	                			ret=targets.get(0);
	                		}
	                	}
	                	else{
		                    ListMap<Agent,List<Agent>> vmap=new ListMap<Agent,List<Agent>>(votelist.size());
		                    Agent prev_target=null;
		                    for(Vote v : votelist){
		                    	List<Agent> list=vmap.get(v.getTarget());
		                    	if(list==null){
		                    		list=new ArrayList<Agent>();
		                    		vmap.put(v.getTarget(),list);                    		
		                    	}
		                    	list.add(v.getAgent());
		                    	if(v.getAgent().equals(agent)){
		                    		prev_target=v.getTarget();
		                    	}
		                    }
		                    //2•[‚Í‚¢‚Á‚Ä‚¢‚é‚Æ‚«2-2‚É‚È‚Á‚Ä‚¢‚é‚Í‚¸
		                    if(vmap.get(prev_target).size()>1){
		                    	//©•ª‚É•[‚ª“ü‚Á‚Ä‚¢‚È‚¢‚Æ‚«A•[‚ğ“ü‚ê‘Ö‚¦‚Ä3-1‚ğ‘_‚¤
		                    	if(vmap.get(agent)==null){
		                    		Agent a;
		                    		for(int i=0;i<vmap.size();i++){
		                    			List<Agent> temp=vmap.getValue(i);
		                    			if(temp!=null && !temp.contains(agent)){
				                    		ret=vmap.getKey(i);
		                    			}
		                    		}
		                    	}
		                    	//©•ª‚É•[‚ª“ü‚Á‚Ä‚¢‚é‚Æ‚«A•[‚ğ•Ï‚¦‚È‚¢
		                    	else{
		                    		ret=prev_target;
		                    	}
		                    }
		                    //1•[‚Å‚Ğ‚«‚í‚¯‚È‚ç1-1-1-1‚É‚È‚Á‚Ä‚¢‚é‚Í‚¸
		                    else{
		                    	//•[‚ğ“ü‚ê‘Ö‚¦‚Ä2-1-1‚ğ‘_‚¤
		                    	targets.remove(prev_target);
		                    	ret=targets.get(Tools.rand(targets.size()));
		                    }
		                    
	                	}
            		}
            		else{
            			if(lunatics!=null && lunatics.size()>0){
                        	for(Agent a : lunatics){
                        		targets.remove(a);
                        	}
                        	if(targets.size()==1){
                            	return targets.get(0);
                            }
                        	else if(targets.size()<=0){
                        		return null;
                        	}
                        }
                    	
            		}
            		
                    break;
                case POSSESSED:
                	if(myRole==Role.SEER){
                		//©•ª‚Æ‘ÎR‚Ìè‚¢t‚Ìî•ñ‚ğŒ©‚é
                		JudgeInfo ji1=null;
                		JudgeInfo ji2=null;
                		List<JudgeInfo> jinfos=wa.getSeerInfos();                		
                		//‘ÎR‚ª‚P‚Ü‚Å‚Ì‚Æ‚«
                		if(wa.getSeerList().size()<=3){
                			for(JudgeInfo ji : jinfos){
                				if(ji.getAgent().equals(agent)){
                					ji1=ji;
                				}
                				else{
                					ji2=ji;
                				}
                			}
                			
                			//‘ÎR‚Ìè‚¢Œ‹‰Ê
                    		if(ji2!=null){
                    			List<Judge> jlist=ji2.getJudgeList();
	                			Judge j21=jlist.size()>0?jlist.get(0):null;
	                    		Judge j22=jlist.size()>1?jlist.get(1):null;
	                    		List<Agent> alist=new ArrayList<Agent>(targets);
	                    		List<Agent> wlist=new ArrayList<Agent>();
	                    		//”’”»’è‚ğo‚µ‚½ƒG[ƒWƒFƒ“ƒg‚ª¶‚«‚Ä‚¢‚é‚È‚ç“Š•[‚·‚é
	                    		//•”»’è‚ğo‚µ‚½ƒG[ƒWƒFƒ“ƒg‚ª¶‚«‚Ä‚¢‚é‚È‚ç“Š•[‚µ‚È‚¢
	                    		if(j21!=null &&  targets.contains(j21.getTarget())){
	                    			if(j21.getResult()==Species.WEREWOLF){
	                    				alist.remove(j21.getTarget());	                    				
	                    			}
	                    			else{
	                    				wlist.add(j21.getTarget());
	                    			}
	                    			
	                    		}
	                    		else if(j22!=null &&  targets.contains(j22.getTarget())){
	                    			if(j22.getResult()==Species.WEREWOLF){
	                    				alist.remove(j22.getTarget());
	                    			}
	                    			else{
	                    				wlist.add(j22.getTarget());
	                    			}
	                    		}
	                    		//l˜T‚ğœ‚¢‚½ê‡‚Ì“Š•[æ‚ª‚P‚Â‚Ìê‡
	                    		if(alist.size()==1){
	                    			return alist.get(0);
	                    		}
	                    		//”’”»’è‚Ì‘ÎÛ‚ª‚P‚Â‚Ìê‡
	                    		if(wlist.size()==1){
	                    			return wlist.get(0);
	                    		}
	                    		
                    		}
                    		//©•ª‚Ìè‚¢Œ‹‰Ê
                			if(ji1!=null){
	                			List<Judge> jlist=ji1.getJudgeList();
	                			Judge j11=jlist.size()>0?jlist.get(0):null;
	                    		Judge j12=jlist.size()>1?jlist.get(1):null;
	                    		
	                    		List<Agent> alist=new ArrayList<Agent>(targets);
	                    		
	                    		//•”»’è‚ğo‚µ‚½ƒG[ƒWƒFƒ“ƒg‚ª¶‚«‚Ä‚¢‚é‚È‚ç“Š•[‚·‚é
	                    		//”’”»’è‚ğo‚µ‚½ƒG[ƒWƒFƒ“ƒg‚ª¶‚«‚Ä‚¢‚é‚È‚ç“Š•[‚µ‚È‚¢
	                    		//‰“ú‚ÌŒ‹‰Ê‚ğ—Dæ‚·‚é
	                    		if(j11!=null &&  targets.contains(j11.getTarget())){
	                    			if(j11.getResult()==Species.WEREWOLF){
	                    				return (j11.getTarget());
	                    			}
	                    			else{
	                    				alist.remove(j11.getTarget());
	                    				if(alist.size()==1){
	                    					return alist.get(0);
	                    				}
	                    			}
	                    		}
	                    		else if(j12!=null && j12.getResult()==Species.WEREWOLF && targets.contains(j12.getTarget())){
	                    			if(j12.getResult()==Species.WEREWOLF){
	                    				return (j12.getTarget());
	                    			}
	                    			else{
	                    				alist.remove(j12.getTarget());
	                    				if(alist.size()==1){
	                    					return alist.get(0);
	                    				}
	                    			}
	                    		}
	                    		
	                    		
	                    		
                			}
                    		
                    		
                		}
                		//‘ÎR‚ª‚QˆÈã‚Ì‚Æ‚«
                		else{
                			List<Agent> slist=new ArrayList<Agent>();
                			List<Agent> vlist=new ArrayList<Agent>();
                			List<Agent> alist=gi.getAliveAgentList();
                			//¶‘¶Ò‚Ì’†‚Åè‚¢t‚b‚nÒ‚Æ‚»‚¤‚Å‚È‚¢‚à‚Ì‚É•ª‚¯‚é
                			for(Agent a : alist){
                				if(a==agent){
                					
                				}
                				else if(wa.getSeerInfo(a)!=null){
                					slist.add(a);
                				}
                				else{
                					vlist.add(a);
                				}
                			}
                			//è‚¢t‚ª‚Pl¶‘¶‚µ‚Ä‚¢‚é‚Æ‚«,è‚¢t‚ªl˜T‚Æl‚¦‚é
                			if(slist.size()==1 && vlist.size()==1){
                				return vlist.get(0);
                			}
                			//è‚¢t‚ª2l¶‘¶‚µ‚Ä‚¢‚é‚Æ‚«A³‚µ‚¢è‚¢t‚É“Š•[‚·‚é
                			else if(slist.size()>=2){
                				List<Agent> tlist=new ArrayList<Agent>(targets);
                				List<Agent> whitelist=new ArrayList<Agent>();
                				for(Agent a : gi.getAgentList()){
                					if(!slist.contains(a)){
                						whitelist.add(a);
                					}
                				}
                				
                				
                				for(Agent s : slist){
                					JudgeInfo ji=wa.getSeerInfo(s);
                					boolean flag=false;
                					int b_num=0;
                					int aw_num=0;
                					int db_num=0;
                					
                					//ŠÔˆá‚Á‚½”»’è‚ğ‚µ‚Ä‚¢‚éè‚¢t‚ğ’T‚·
                					for(Judge j : ji.getJudgeList()){
                						if(j.getResult()==Species.WEREWOLF){
                							b_num++;
                							if(!gi.getAliveAgentList().contains(j.getTarget())){
                								db_num++;
                							}
                							if(whitelist.contains(j.getTarget())){
                								flag=true;
                								break;
                							}
                						}
                						else if(j.getResult()==Species.HUMAN){
                							if(gi.getAliveAgentList().contains(j.getTarget())){
                								aw_num++;
                							}
                						}
                					}
                					//•”»’è‚ª2ˆÈã‚Ì‚Æ‚«
                					if(b_num>1){
        								flag=true;
        							}
                					//¶‘¶‚µ‚Ä‚¢‚é”’”»’è‚ª2ˆÈã‚Ì‚Æ‚«
                					if(aw_num>1){
                						flag=true;
                					}
                					//€–S‚µ‚Ä‚¢‚é•”»’è‚ª1ˆÈã‚Ì‚Æ‚«
                					if(db_num>0){
                						flag=true;
                					}
                					
                					//ŠÔˆá‚Á‚Ä‚¢‚éè‚¢t‚ğ“Š•[‘ÎÛ‚©‚çŠO‚·
                					if(flag){
                						tlist.remove(s);
                					}
                					
                				}
                				
                				if(tlist.size()==1){
                					return tlist.get(0);
                				}
                				
                			}
                			
                		}
                	}
                	//“Á‚É“Š•[æ‚ª‚È‚¢‚Æ‚«Aˆê”Ô‹^‚Á‚Ä‚¢‚éÒ‚É‚Í“Š•[‚µ‚È‚¢
                	{
	                	double min=0.0;
	                    for (int k = 0; k < view.size(); k++) {
	                        PlayerInfo pi = view.get(k);
	                        if (targets.contains(pi.getAgent())) {
	                        	if(pi.getCertain()<=min){
	                        		ret = pi.getAgent();
	                        		min=pi.getCertain();
	                        	}
	                        }
	                    }
                	}
                    break;
                
                default:
                    break;
            }
        }

        return ret;
    }
    public Agent getPowerPlay() {
        Agent ret = null;
        if (wa.getAliveSize() < 5) {
            List<Agent> targets = gi.getAliveAgentList();
            targets.remove(agent);
            switch (getRole()) {
                case WEREWOLF:
                    List<Vote> votelist=wa.getLatestVoteList(); 
                  //Ä“Š•[
            		if(votelist!=null && votelist.size()>0){
	                    //3l‚Ì‚Æ‚«‚Í©•ª‚Ì“Š•[‚ğ“ü‚ê‘Ö‚¦‚éB
	                	if(wa.getAliveSize()==3){
	                		for(Vote v : votelist){
	                			if(v.getAgent().equals(agent)){
	                				targets.remove(v.getTarget());
	                				break;
	                			}
	                		}
	                		if(targets.size()==1){
	                			ret=targets.get(0);
	                		}
	                	}
	                	else{
		                    ListMap<Agent,List<Agent>> vmap=new ListMap<Agent,List<Agent>>(votelist.size());
		                    Agent prev_target=null;
		                    for(Vote v : votelist){
		                    	List<Agent> list=vmap.get(v.getTarget());
		                    	if(list==null){
		                    		list=new ArrayList<Agent>();
		                    		vmap.put(v.getTarget(),list);                    		
		                    	}
		                    	list.add(v.getAgent());
		                    	if(v.getAgent().equals(agent)){
		                    		prev_target=v.getTarget();
		                    	}
		                    }
		                    //2•[‚Í‚¢‚Á‚Ä‚¢‚é‚Æ‚«2-2‚É‚È‚Á‚Ä‚¢‚é‚Í‚¸
		                    if(vmap.get(prev_target).size()>1){
		                    	//©•ª‚É•[‚ª“ü‚Á‚Ä‚¢‚È‚¢‚Æ‚«A•[‚ğ“ü‚ê‘Ö‚¦‚Ä3-1‚ğ‘_‚¤
		                    	if(vmap.get(agent)==null){
		                    		Agent a;
		                    		for(int i=0;i<vmap.size();i++){
		                    			List<Agent> temp=vmap.getValue(i);
		                    			if(temp!=null && !temp.contains(agent)){
				                    		ret=vmap.getKey(i);
		                    			}
		                    		}
		                    	}
		                    	//©•ª‚É•[‚ª“ü‚Á‚Ä‚¢‚é‚Æ‚«A•[‚ğ•Ï‚¦‚È‚¢
		                    	else{
		                    		ret=prev_target;
		                    	}
		                    }
		                    //1•[‚Å‚Ğ‚«‚í‚¯‚È‚ç1-1-1-1‚É‚È‚Á‚Ä‚¢‚é‚Í‚¸
		                    else{
		                    	//•[‚ğ“ü‚ê‘Ö‚¦‚Ä2-1-1‚ğ‘_‚¤
		                    	targets.remove(prev_target);
		                    	ret=targets.get(Tools.rand(targets.size()));
		                    }
		                    
	                	}
            		}
                    break;
                case POSSESSED:
                	double min=0.0;
                    for (int k = 0; k < view.size(); k++) {
                        PlayerInfo pi = view.get(k);
                        if (targets.contains(pi.getAgent())) {
                        	if(pi.getCertain()<=min){
                        		ret = pi.getAgent();
                        		min=pi.getCertain();
                        	}
                        }
                    }
                    break;
                
                default:
                    break;
            }
        }

        return ret;
    }
	public ListMap<Agent,Double> getVoteList(){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		boolean flag;
		Agent pp=wa.getPlayerNum()==5?getPowerPlay5():getPowerPlay();
		if(pp!=null){
			
			for(PlayerInfo pi : view){
				Agent a=pi.getAgent();
				if(a.equals(pp)){
					ret.put(a,1.0);
				}
				else{
					ret.put(a, 0.0);
				}
			}
			return ret;
		}
		TalkInfo ti=wa.getTalkInfo();
		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				double v=1.0+ti.getVotePoint(pi.getAgent(),wa.getAliveSize())*personality.getWeightAlong();
				double x=1.0-pi.getCertain();
				//‹^‚í‚µ‚­‚È‚¢l˜T‚É‚Í“Š•[‚µ‚È‚¢
				if(wolfs.keyList().contains(pi.getAgent()) && pi.getCertain()>th){
					ret.add(pi.getAgent(),0.0);
				}
				
				//c‚è‚Ìl”‚ª‘½‚¢‚Æ‚«‚É‚Íè‚¢t‚Í‚Â‚è‚É‚­‚­‚·‚é
				else if(pi.getRole()==Role.SEER){
					
					double th=1.0/gi.getAliveAgentList().size();
					double a=((double)(wa.seerSize()-1.0)/gi.getAliveAgentList().size());
					double r=1.0-pi.getCertain();
					
					if(gi.getRole()==Role.SEER || myRole==Role.SEER){
						x=r*a;
						if(x>1.0){
							x=1.0;
						}
					}
					else{
						if(r<th || (1-th)<r){
							x=r;
						}
						else{
							x=r*a;
							if(x>1.0){
								x=1.0;
							}
						}
					}
					
					
				}
				
				
				double m=x*v;
				if(m<0){
					ret.add(pi.getAgent(), m);
				}
				else{
					ret.add(pi.getAgent(),m);
				}
				
			}
			
			else{
				ret.add(pi.getAgent(),0.0);				
			}
		}
		ret.put(agent,0.0);
		flag=Tools.cutList(ret,v_num,1);
		
		if(flag){
			return ret;
		}
		ret=getBlackAliveList();
		ret.put(agent,0.0);
		return ret;
	}
	




	public ListMap<Agent,Double> getAttackList(){
		searchLunatics();
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		TalkInfo ti=wa.getTalkInfo();
		boolean flag;
		for(PlayerInfo pi : view){
			if(wolfs.containsKey(pi.getAgent())){
				ret.add(pi.getAgent(),0.0);
			}
			else if(pi.isAlive()){
				double v=1.0+(ti.getAttackPoint(pi.getAgent(),wa.getAliveSize())-ti.getVotePoint(pi.getAgent(), wa.getAliveSize()))*personality.getWeightAlong();
				double x;
				
				if(pi.getRole()==Role.SEER){
					x=pi.getCertain()*2;
				}
				else{
					x=pi.getCertain();
				}
				double m=x*v;
				if(m<0){
					ret.add(pi.getAgent(),0.0);
				}
				else{
					ret.add(pi.getAgent(),m);
				}
			}			
			else{
				ret.add(pi.getAgent(),0.0);				
			}
		}
		for(Agent s : lunatics){
			ret.put(s, 0.0);
		}
		flag=Tools.cutList(ret,a_num,1);
		if(flag){
			return ret;
		}
		ret=getWhiteAliveList();
		for(Agent s : lunatics){
			ret.put(s, 0.0);
		}
		for(Agent s : wolfs.keyList()){
			ret.put(s,0.0);
		}
		return ret;
	}

	public ListMap<Agent,Double> getGuardList(){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		boolean flag;
		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				
				double a=0.1;
				if(pi.getRole()==Role.SEER){
					a*=wa.getAliveSize()/2.0;
				}
				else if(pi.getRole()==Role.MEDIUM){
					a*=2;
				}
				TalkInfo ti=wa.getTalkInfo();
				
				double x=pi.getCertain()*a;
				double v=1.0-ti.getVotePoint(pi.getAgent(), wa.getAliveSize())*personality.getWeightAlong();
				double m=x*v;
				if(m<0){
					ret.add(pi.getAgent(),0.0);
				}
				else{
					ret.add(pi.getAgent(),m);
				}
				
			}
			else{
				ret.add(pi.getAgent(),0.0);
			}
		}
		ret.put(agent,0.0);
		flag=Tools.cutList(ret,g_num,1);
		if(flag){
			return ret;
		}
		ret=getWhiteAliveList();
		ret.put(agent,0.0);
		return ret;
	}

	public ListMap<Agent,Double> getDivineList(){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		boolean flag=false;
		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				if(pi.isChecked()){
					ret.add(pi.getAgent(),0.0);
				}
				else if(pi.getRole()==Role.MEDIUM || pi.getRole()==Role.SEER){
					ret.add(pi.getAgent(),0.0);
				}
				else{
					TalkInfo ti=wa.getTalkInfo();
					double x=pi.getCertain()*(1-pi.getCertain());
					double v=1.0-ti.getVotePoint(pi.getAgent(), wa.getAliveSize())*personality.getWeightAlong();
					double m=x*v;
					if(m<0){
						ret.add(pi.getAgent(),0.0);
					}
					else{
						ret.add(pi.getAgent(),m);
					}
				}
				
			}
			else{
				ret.add(pi.getAgent(),0.0);
			}
		}
		ret.put(agent,0.0);
		flag=Tools.cutList(ret, d_num, 1);
		if(flag){
			return ret;
		}


		for(PlayerInfo pi : view){
			if(pi.isAlive()){
				if(pi.isChecked(agent)){
					ret.put(pi.getAgent(),0.0);
				}
				else if(pi.getRole()==Role.MEDIUM || pi.getRole()==Role.SEER){
					ret.put(pi.getAgent(),0.0);
				}
				else{
					double x=pi.getCertain()*(1-pi.getCertain());
					ret.put(pi.getAgent(),x);
				}

			}
			else{
				ret.put(pi.getAgent(),0.0);
			}

		}
		ret.put(agent,0.0);
		flag=Tools.cutList(ret,2,1);
		if(flag){
			return ret;
		}

		ret=getBlackAliveList();
		for(PlayerInfo pi : view){
			if(pi.isChecked(agent)){
				ret.put(pi.getAgent(), 0.0);
			}
		}
		ret.put(agent,0.0);
		flag=Tools.cutList(ret,2,1);
		if(flag){
			return ret;
		}

		ret=getAliveList();
		ret.put(agent,0.0);
		return ret;
	}



	public ListMap<Agent,Double> getWhiteList(double th){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		for(PlayerInfo pi : view){
			if(pi.isDead()){
				ret.add(pi.getAgent(),0.0);
			}
			else if(pi.getAgent().equals(agent)){
				ret.add(pi.getAgent(),0.0);
			}
			else if(th<pi.getCertain()){
				ret.add(pi.getAgent(),pi.getCertain());
			}
			else{
				ret.add(pi.getAgent(), 0.0);
			}
		}
		return ret;
	}


	public ListMap<Agent,Double> getBlackList(double th){
		ListMap<Agent,Double> ret=new ListMap<Agent,Double>();
		for(PlayerInfo pi : view){
			if(pi.isDead()){
				ret.add(pi.getAgent(),0.0);
			}
			else if(pi.getAgent().equals(agent)){
				ret.add(pi.getAgent(),0.0);
			}
			else if(pi.getCertain()<th){
				ret.add(pi.getAgent(),1-pi.getCertain());
			}
			else{
				ret.add(pi.getAgent(), 0.0);
			}
		}

		return ret;
	}

	
	//NOTE:”’‚Í‚»‚Ì–ğEA•‚Í˜T‚Å‚ ‚é‚Æ„‘ª‚·‚é”­Œ¾‚ğ‚·‚é
	public String getTalk(){
		Content ret=skip_count>2?talk_factory.over():talk_factory.skip();
		Agent to;
		Role role;
		if(personality.getWeightVolume()<Tools.random(-0.2,1.0)){
			skip_count++;
			return ret.getText();
		}
		ListMap<Agent,Double> map=getVoteList();
		if(vote_target==null){
			vote_target=Tools.selectKey(map);			
			if(vote_target!=null){
				return talk_factory.vote(vote_target).getText();
			}
		}
		else{
			if(map.get(vote_target)<=0 || Tools.random()<personality.getWeightRandom()){
				Agent temp=Tools.selectKey(map);
				if(temp!=null && vote_target!=temp){
					vote_target=temp;
					return talk_factory.vote(vote_target).getText();
				}
			}
		}
		
		if(Tools.random()<personality.getWeightTalkWhite()){
			to=Tools.selectKey(getWhiteList(th));
			role=Role.VILLAGER;
			for(PlayerInfo pi : view){
				if(pi.getAgent().equals(to)){
					role=pi.getRole();
					break;
				}
			}
		}
		else{
			to=Tools.selectKey(getBlackList(th));
			role=Role.WEREWOLF;
		}
		if(to!=null){
			ret=(talk_factory.estimate(to,role));
		}
		
		return ret.getText();
	}
	
	public String getWhisper(){
		//NOTE:©•ª‚¾‚Á‚½‚ç‚Ç‚ñ‚Ès“®‚ğ‚·‚é‚©A‹¶l‚ÌˆÊ’u‚É‚Â‚¢‚Ä˜b‚·
		Agent p;
		Content ret=talk_factory.over();
		if(personality.getWeightVolume()<Tools.random(-0.2,1.0)){
			return ret.getText();
		}
		
		ListMap<Agent,Double> map=getAttackList();
		if(attack_target==null){
			attack_target=Tools.selectKey(map);
			if(attack_target!=null){
				return talk_factory.attack(attack_target).getText();
			}
		}
		else{
			if(map.get(attack_target)<=0 || Tools.random()<personality.getWeightRandom()){
				p=Tools.selectKey(map);
				if(p!=null && attack_target!=p){
					attack_target=p;
					return talk_factory.attack(attack_target).getText();
				}
			}
		}
		
		
		switch(Tools.rand(5)){
		case 0://‹¶l‚É‚Â‚¢‚Ä
		if(lunatics.size()>0){
			ret=talk_factory.estimate(lunatics.get(Tools.rand(lunatics.size())), Role.POSSESSED);
		}
		break;
		/*
		case 1://“Š•[‚É‚Â‚¢‚Ä
			p=Tools.selectKey(getVoteList());
			if(p!=null){
				ret=factory.vote(p);
			}
			break;
		case 2://è‚¢‚É‚Â‚¢‚Ä
			p=Tools.selectKey(getDivineList());
			if(p!=null){
				Species s=wolfs.containsKey(p)?Species.WEREWOLF:Species.HUMAN;
				ret=factory.divined(p,s);
			}
			break;
		case 3://Œì‰q‚É‚Â‚¢‚Ä
			p=Tools.selectKey(getGuardList());
			if(p!=null){
				ret=factory.guarded(p);
			}
			break;
			
		case 4://PŒ‚æ‚É‚Â‚¢‚Ä
			ListMap<Agent,Double> map1=getAttackList();
			if(attack_target==null){
				attack_target=Tools.selectKey(map1);
				if(attack_target!=null){
					ret=factory.attack(attack_target);
				}
			}
			else{
				if(map1.get(attack_target)<=0 || Tools.random()<personality.getWeightRandom()){
					p=Tools.selectKey(map1);
					if(p!=null && attack_target!=p){
						attack_target=p;
						ret=factory.attack(p);
					}
				}
			}
			break;
			*/
		default:
			break;
		}
		return ret.getText();
	}
	
}

