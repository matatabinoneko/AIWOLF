package jp.gmail.kogecha05.estimator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gmail.kogecha05.utils.Const;
import jp.gmail.kogecha05.utils.Functions;

public class RoleEstimator {
	final String inputFileVillage5 = "jp/gmail/kogecha05/estimator/data5";
	final String inputFileVillage15 = "jp/gmail/kogecha05/estimator/data15";
	int learningGame = 50;
	final int dimFeature = 5;

	GameSetting currentGameSetting;
	GameInfo currentGameInfo;
	Integer gameCount = 0;

	List<Role> roleList;
	List<Agent> agentList;

	Map<Agent, Map<Topic, Integer> > statisticsAgentTalk = new HashMap<>();
	Map<Agent, Map<Role, Double> > roleProbability = new HashMap<>();

	Map<Agent, Map<String, Double>> dataEvidence = new HashMap<>();
	static public Map<Agent, Map<String, Double>> agentProbability = new HashMap<>();

	static private Map<String, Map<Role, List<List<Integer>>>> preAgentModel;
	static private Map<Agent, Map<Role, List<List<Integer>>>> currentAgentModel
		= new HashMap<>(new HashMap<>());

	@SuppressWarnings("resource")
	private void readLog() {
		preAgentModel = new HashMap<>();

		String filename;
		switch (currentGameSetting.getPlayerNum()) {
		case 5: filename = inputFileVillage5; break;
		default: filename = inputFileVillage15; break;
		}

		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
		Scanner sc = new Scanner(is);

		while (sc.hasNext()) {
			String name = sc.next();
			String role = sc.next();
			int N = sc.nextInt();

			List<List<Integer>> agentRoleFeature = new ArrayList<>();
			for (int i = 0; i < N; i++) {
				String s = sc.next();
				String[] array = s.split(",");
				List<Integer> feature = new ArrayList<>();
				for (String a : array) {
					feature.add(Integer.parseInt(a));
				}
				agentRoleFeature.add(feature);
			}

			if (preAgentModel.get(name) == null)
				preAgentModel.put(name, new HashMap<>());
			Map<Role, List<List<Integer>>> roleFeatureLog = preAgentModel.get(name);
			roleFeatureLog.put(Role.valueOf(role), agentRoleFeature);
			preAgentModel.put(name, roleFeatureLog);
		}
	}

	private Map<Role, Double> initPMap() {
		Map<Role, Double> pMap = new HashMap<>();
		for (Role role: roleList) {
			double p = 0.0;
			if (currentGameSetting.getPlayerNum() == 5) {
				switch (role) {
				case VILLAGER:	p = 2 / 5.0; break;
				case SEER:       	p = 1 / 5.0; break;
				case POSSESSED: 	p = 1 / 5.0; break;
				case WEREWOLF:  	p = 1 / 5.0; break;
				default: break;
				}
			} else {
				switch (role) {
				case VILLAGER:	p = 8 / 15.0; break;
				case BODYGUARD:	p = 1 / 15.0; break;
				case SEER:       	p = 1 / 15.0; break;
				case MEDIUM:     	p = 1 / 15.0; break;
				case POSSESSED: 	p = 1 / 15.0; break;
				case WEREWOLF:  	p = 3 / 15.0; break;
				default: break;
				}
			}
			pMap.put(role, p);
		}
		return pMap;
	}

	public void initialize(GameSetting gameSetting, GameInfo gameInfo) {
		currentGameSetting = gameSetting;
		currentGameInfo = gameInfo;
		roleList = gameInfo.getExistingRoles();
		gameCount++;

		if (preAgentModel == null) {
			agentList = currentGameInfo.getAgentList();

			readLog();

			double initP = 1.0 / (double)preAgentModel.keySet().size();
			for (Agent agent: agentList) {
				agentProbability.put(agent, new HashMap<>());
				for (String agentName: preAgentModel.keySet()) {
					agentProbability.get(agent).put(agentName, initP);
				}
			}
		}

		statisticsAgentTalk.clear();
		for (Agent agent: agentList) {
			statisticsAgentTalk.put(agent, new HashMap<Topic, Integer>());
		}

		roleProbability.clear();
		for (Agent agent: agentList) {
			Map<Role, Double> pMap = initPMap();
			roleProbability.put(agent, pMap);
		}

		dataEvidence.clear();
		for (Agent agent: agentList) {
			dataEvidence.put(agent, new HashMap<>());
			for (String nameAgent: preAgentModel.keySet()) {
				dataEvidence.get(agent).put(nameAgent, 0.0);
			}
			dataEvidence.get(agent).put(agent.toString(), 0.0);
		}
	}

	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;

		if (gameInfo.getDay() >= 1)
			updateRoleProbability();
	}

	public void putTalkLog(Talk talk) {
		// 発言回数を記録
		Agent talker = talk.getAgent();
		Content content = new Content(talk.getText());
		Topic topic = content.getTopic();

		Map<Topic, Integer> countTopic = statisticsAgentTalk.get(talker);
		if (countTopic.get(topic) == null) {
			countTopic.put(topic, 1);
		} else {
			Integer count = countTopic.get(topic);
			countTopic.replace(topic, count + 1);
		}
		statisticsAgentTalk.put(talker, countTopic);
	}

	List<Integer> extractAgentFeature(Agent agent, Map<Topic, Integer> topicCount) {
		List<Integer> vectorTalk = new ArrayList<>();

		for (Topic topic : Arrays.asList(
				Topic.ESTIMATE,
				Topic.VOTE,
				Topic.AGREE,
				Topic.DISAGREE,
				Topic.OPERATOR)) {
			Integer count = topicCount.get(topic);
			if (count != null) {
				vectorTalk.add(count);
			} else {
				vectorTalk.add(0);
			}
		}

		return vectorTalk;
	}

	public void updateActionLog() {
		// エージェントの特徴を抽出して保存
		Map<Agent, Role> roleMap = currentGameInfo.getRoleMap();
		for (Agent agent : agentList) {
			Role role = roleMap.get(agent);

			// この役職での特徴を保存
			Map<Topic, Integer> topicCount = statisticsAgentTalk.get(agent);
			Map<Role, List< List<Integer> > > agentRoleTalk = currentAgentModel.get(agent);
			if (agentRoleTalk == null) agentRoleTalk = new HashMap<>();
			List<List<Integer>> memoryTopicCount = agentRoleTalk.get(role);
			if (memoryTopicCount == null) memoryTopicCount = new ArrayList<>();
			List<Integer> vectorTalk = extractAgentFeature(agent, topicCount);
			memoryTopicCount.add(vectorTalk);

			// 十分にデータが溜まったら古いデータは破棄する
			if (memoryTopicCount.size() > 5000) {
				memoryTopicCount.remove(0);
			}

			agentRoleTalk.put(role, memoryTopicCount);
			currentAgentModel.put(agent, agentRoleTalk);

			// エージェント推定
			if (gameCount < learningGame) {
				updateAgentProbability(agent);
			}

			Functions.debugPrintln("-------------- Agent Estimator ---------------");
			Functions.debugPrintln(agent);
			Functions.debugPrintln(agentProbability.get(agent));
			Functions.debugPrintln("----------------------------------------------");
		}
	}

	private void updateAgentProbability(Agent agent) {
		Map<String, Double> ap = agentProbability.get(agent);

		// logsumexp
		double maxval = -Const.INF;
		for (String nameAgent: preAgentModel.keySet()) {
			double logval = dataEvidence.get(agent).get(nameAgent) + Math.log(ap.get(nameAgent));
			if (maxval < logval) {
				maxval = logval;
			}
		}
		double logsum = 0.0;
		for (String nameAgent: preAgentModel.keySet()) {
			double logval = dataEvidence.get(agent).get(nameAgent) + Math.log(ap.get(nameAgent));
			logsum += Math.exp(logval - maxval);
		}
		logsum = maxval + Math.log(logsum);

		Map<String, Double> new_ap = new HashMap<>();
		for (String nameAgent: preAgentModel.keySet()) {
			double logval = dataEvidence.get(agent).get(nameAgent) + Math.log(ap.get(nameAgent));
			new_ap.put(nameAgent, Math.exp(logval - logsum));
		}
		agentProbability.put(agent, new_ap);
	}

	private void updateRoleProbability() {
		for (Agent agent: agentList) {
			Map<Topic, Integer> currentTopicCount = statisticsAgentTalk.get(agent);
			List<Integer> vectorTalk = extractAgentFeature(agent, currentTopicCount);
			Map<String, Double> ap = agentProbability.get(agent);

			Map<Role, Double> pMap = new HashMap<>();
			for (Role role: roleList) {
				pMap.put(role, 0.0);
			}

			if (gameCount < learningGame) {
				// 事前モデル
				for (String agentName: preAgentModel.keySet()) {
					Map<Role, List< List<Integer>>> agentRoleTalk = preAgentModel.get(agentName);
					if (agentRoleTalk == null) continue;
					Map<Role, Double> newPMap = calculateProbability(agent, agentName, vectorTalk, agentRoleTalk);
					for (Role role: roleList) {
						double p = ap.get(agentName) * newPMap.get(role) + pMap.get(role);
						pMap.put(role, p);
					}
				}
			} else {
				// 学習モデル
				Map<Role, List< List<Integer>>> agentRoleTalk = currentAgentModel.get(agent);
				if (agentRoleTalk != null) {
					Map<Role, Double> newPMap = calculateProbability(agent, agent.toString(), vectorTalk, agentRoleTalk);
					for (Role role: roleList) {
						double p = newPMap.get(role) + pMap.get(role);
						pMap.put(role, p);
					}
				}
			}

			roleProbability.put(agent, pMap);
		}
	}

	private Map<Role, Double> calculateProbability(
			Agent agent,
			String nameAgent,
			List<Integer> vectorTalk,
			Map<Role, List<List<Integer>>> agentRoleTalk) {
		int K = 11; // Parameter: 平滑度的ななにか

		Map<Role, List<Double>> roleDistMap = new HashMap<>();
		PriorityQueue<Double> pq = new PriorityQueue<>();
		int dataCount = 0;
		Map<Role, Integer> dataRoleCount = new HashMap<>();
		for (Role role : roleList) {
			dataRoleCount.put(role, 0);

			List<List<Integer>> memoryTopicCount = agentRoleTalk.get(role);
			if (memoryTopicCount == null) continue;

			List<Double> distList = new ArrayList<>();
			for (List<Integer> memoryVector : memoryTopicCount) {
				dataCount++;
				Integer count = dataRoleCount.get(role);
				dataRoleCount.put(role, count + 1);

				// 距離計算
				double dist = Functions.distance(vectorTalk, memoryVector);
				distList.add(dist);
				pq.add(dist);
			}
			roleDistMap.put(role, distList);
		}

		// 推定に使うデータの範囲
		double R = 0;
		for (int i = 0; i < K; i++) {
			if (pq.isEmpty()) break;
			R = pq.poll();
		}

		// 範囲内にあるデータの数を数える
		int inrangeCount = 0;
		Map<Role, Double> inrangeRoleCount = new HashMap<>();
		for (Role role : roleList) {
			inrangeRoleCount.put(role, 10e-10);

			List<Double> distList = roleDistMap.get(role);
			if (distList == null) continue;

			for (double dist : distList) {
				if (dist <= R) {
					inrangeCount++;
					Double count = inrangeRoleCount.get(role);
					inrangeRoleCount.put(role, count + 1);
				}
			}
		}

		// 事後確率を計算
		Map<Role, Double> pMap = new HashMap<>();
		for (Role role: roleList) {
			if (inrangeRoleCount.get(role) == null || inrangeCount == 0) {
				pMap.put(role, 0.0);
				continue;
			}
			double inrangeRoleRate = inrangeRoleCount.get(role) / (double)inrangeCount;
			double new_p = inrangeRoleRate;
			pMap.put(role, new_p);
		}

		// 正規化
		double sum_p = 0.0;
		for (double p : pMap.values()) {
			sum_p += p;
		}
		for (Role role: roleList) {
			if (sum_p == 0) break;
			Double p = pMap.get(role);
			if (p == null) continue;
			pMap.put(role, p / sum_p);
		}

		// エージェント推定用のモデルエビデンス
		double logEvidence = Math.log(inrangeCount + Const.EPS)
				- Math.log(dataCount + Const.EPS) - dimFeature * Math.log(R + Const.EPS);
		dataEvidence.get(agent).put(nameAgent, logEvidence);

		return pMap;
	}

	public double getRoleProbability(Agent agent, Role role) {
		Map<Role, Double> pMap = roleProbability.get(agent);
		if (pMap == null) return 0.0;
		Double p = pMap.get(role);
		if (p == null) return 0.0;
		return p;
	}

	public void setRoleProbability(Agent agent, Role role, Double p) {
		try {
			roleProbability.get(agent).put(role, p);
		} catch (Exception e) {

		}
	}

	public Map<Role, Double> getAgentResult(Agent agent) {
		return roleProbability.get(agent);
	}

}
