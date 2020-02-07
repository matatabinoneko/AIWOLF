package army.sh.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import army.sh.gadget.Check;
import army.sh.gadget.Debug;
import army.sh.talk.TalkFactory;

public class BaseRole implements Player {

	private short bitMask15 = 0b0111111111111111;
	private short bitMask5 = 0b000000000011111;

	protected boolean passageFinish;

	protected GameInfo gameInfo;
	protected GameSetting gameSetting;

	protected Deque<String> talkQueue;
	protected Queue<String> whisperTalkQueue;
	protected int readTalkNum;

	protected Agent me;
	protected Agent voteTarget;
	protected Agent divineTarget;
	protected Agent guardTaget;
	protected Agent attackTarget;
	protected Agent identTarget;
	protected Role myRole;
	protected boolean isCO;

	protected short bitMe;
	protected short bitCO;
	protected short bitAlive;

	protected Map<Agent, Role> roleMap;
	protected Map<Agent, Integer> votePointMap;
	protected Map<Agent, Integer> winPointMap;

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		try {
			passageFinish = false;

			this.gameInfo = gameInfo;
			this.gameSetting = gameSetting;

			talkQueue = new ArrayDeque<>();
			whisperTalkQueue = new ArrayDeque<>();
			readTalkNum = 0;

			me = gameInfo.getAgent();
			voteTarget = null;
			divineTarget = null;
			guardTaget = null;
			attackTarget = null;
			identTarget = null;
			myRole = gameInfo.getRole();
			isCO = false;

			bitMe = this.getAgentBit(me);
			bitCO = 0;
			bitAlive = 0;

			roleMap = new HashMap<>();
			votePointMap = new HashMap<>();
			winPointMap = new HashMap<>();
			for (Agent agent : this.gameInfo.getAgentList()) {
				roleMap.put(agent, null);
				votePointMap.put(agent, 0);
				winPointMap.put(agent, 0);
			}

		} catch (Exception e) {
			this.writeException(e);
		}
	}

	@Override
	public void dayStart() {
		try{
			this.talkQueue.clear();
			this.whisperTalkQueue.clear();
			this.readTalkNum = 0;

			this.myRole = gameInfo.getRole();

			List<Agent> aliveList = gameInfo.getAliveAgentList();
			aliveList.forEach(agent -> bitAlive = (short) (bitAlive | this.getAgentBit(agent)));
		}catch(Exception e){
			
		}
	}

	@Override
	public void finish() {
		try{
			this.talkQueue.clear();
			this.whisperTalkQueue.clear();
			this.readTalkNum = 0;

			this.voteTarget = null;
			this.divineTarget = null;
			this.guardTaget = null;
			this.attackTarget = null;
			this.identTarget = null;
			this.myRole = null;
			this.isCO = false;

			bitCO = 0;
			bitAlive = 0;

			if (passageFinish) {
				this.votePointMap.keySet().forEach(agent -> votePointMap.put(agent, 0));
			}
			passageFinish = true;
		}catch(Exception e){
			
		}

	}

	@Override
	public void update(GameInfo gameInfo) {
		try{
			this.gameInfo = gameInfo;
			List<Talk> talkList = gameInfo.getTalkList();
			// talk
			for (int i = readTalkNum; i < talkList.size(); i++) {
				Talk talk = talkList.get(i);
				Agent talker = talk.getAgent();
				if (talker == me) {
					continue;
				}
				short talkerNum = this.getAgentBit(talker);
				Content content = new Content(talk.getText());
				Agent target = content.getTarget();

				switch (content.getTopic()) {
				case VOTE:
					votePointMap.put(target, votePointMap.get(target) + 1);
					break;
				case ESTIMATE:
					switch (content.getRole()) {
					case WEREWOLF:
					case POSSESSED:
						if (target == me) {
							talkQueue.add(TalkFactory.disagree(talk));
						}
						break;

					default:
						break;
					}
					break;
				case COMINGOUT:
					if (!isBitFlag(bitCO, talkerNum)) {
						roleMap.put(talker, content.getRole());
						bitCO = (short) (bitCO | talkerNum);
					} else {
						talkQueue.add(TalkFactory.vote(talker));
						voteTarget = talker;
					}
					break;
				case GUARD:
					break;
				case DIVINATION:
					break;
				case DIVINED:
					if (myRole == Role.SEER) {
						talkQueue.add(TalkFactory.estimate(talker, Role.POSSESSED));
						talkQueue.add(TalkFactory.vote(talker));
						if (Check.isNull(voteTarget)) {
							voteTarget = talker;
						}
					} else {
						if (content.getResult() == Species.WEREWOLF) {
							if (target == me) {
								talkQueue.add(TalkFactory.disagree(talk));
								talkQueue.add(TalkFactory.estimate(talker, Role.POSSESSED));
								voteTarget = talker;
								attackTarget = talker;
							} else {
								talkQueue.add(TalkFactory.estimate(target, Role.WEREWOLF));
							}
						}
					}
					break;
				case IDENTIFIED:
					break;
				case GUARDED:
					break;
				case AGREE:
					break;
				case DISAGREE:
					break;
				case ATTACK:
					break;

				default:
					break;
				}

				readTalkNum += 1;
			}

		}catch(Exception e){
			
		}

	}

	@Override
	public Agent vote() {
		try{
			if (Check.isNull(voteTarget)) {

				List<Agent> list = getBitList((short) (bitAlive & invBit(bitCO) & invBit(bitMe)));

				int max = 0;
				Agent target = null;
				for (Agent agent : votePointMap.keySet()) {
					if (max < votePointMap.get(agent)) {
						max = votePointMap.get(agent);
						target = agent;
					}
				}
				list.add(target);
				return list.get(0);
			}
			return voteTarget;
		}catch(Exception e){
			
		}
		return voteTarget;

	}

	@Override
	public String whisper() {
		if (whisperTalkQueue.isEmpty()) {
			return Talk.OVER;
		}
		return whisperTalkQueue.poll();
	}

	@Override
	public Agent attack() {
		try{
			if (Check.isNull(attackTarget)) {
				for (Agent agent : roleMap.keySet()) {
					if (roleMap.get(agent) == Role.SEER && gameInfo.getAliveAgentList().contains(agent)) {
						return agent;
					}
				}
			}
			return attackTarget;
		}catch(Exception e){
			
		}

		return attackTarget;
	}

	@Override
	public Agent divine() {
		try{
			if (Check.isNull(divineTarget)) {
				List<Agent> list = getBitList((short) (bitAlive & invBit(bitCO) & invBit(bitMe)));
				return list.get(0);
			}
		}catch(Exception e){
			
		}

		return divineTarget;
	}

	@Override
	public Agent guard() {
		if (Check.isNull(guardTaget)) {
			return me;
		}
		return guardTaget;
	}

	@Override
	public String talk() {
		if (talkQueue.isEmpty()) {
			return Talk.SKIP;
		}
		return talkQueue.poll();
	}

	@Override
	public String getName() {
		return "Army";
	}

	public void writeException(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Debug.stackError(sw);
	}

	// bitを引数として呼ぶとフラグが立っているエージェントが入ったリストを返してくれる
	public List<Agent> getBitList(short bit) {
		List<Agent> list = new ArrayList<>();
		List<Agent> agentList = gameInfo.getAgentList();
		for (short i = 0; i < agentList.size(); i++) {
			if ((bit & 1 << i) == 1 << i) {
				list.add(agentList.get(i));
			}
		}
		return list;
	}

	// bit反転した後マスクをかけて返す
	public short invBit(short bit) {
		if (gameSetting.getPlayerNum() < 10) {
			return (short) (~bit & bitMask5);
		} else {
			return (short) (~bit & bitMask15);
		}
	}

	public short getAgentBit(Agent agent) {
		return (short) (1 << agent.getAgentIdx() - 1);
	}

	public boolean isBitFlag(short allBit, short targetBit) {
		if ((allBit & targetBit) != 0) {
			return true;
		}
		return false;
	}

	// TODO 戦略スロットとして作りたいな
	public void tactics() {

	}

}
