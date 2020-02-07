package tera.aiwolf.metagame;

import static tera.aiwolf.util.Utils.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameEvent;
import tera.aiwolf.framework.GameEventListenr;
import tera.aiwolf.framework.GameTalk;
import tera.aiwolf.framework.MetagameEventListener;

/**
 * 過去のゲームの発言Topicの2-gram頻度分布から役職を予想するモデル
 *
 */
public class TalkFrequencyModel2gram implements GameEventListenr, MetagameEventListener {

    private Game game;
	private String old_topic;
	private String now_topic;
	private String topic2gram = now_topic + "," + old_topic;

	/* agent の role が r である 初期確率 */
    private Map<GameAgent, Map<Role, Double>> roleProbability;
    /* agent の role が r である 確率: 出力用 */
    private Map<GameAgent, Map<Role, Double>> roleProbabilityTemp;
    /* game.Day == d, agent.getindex == idx, agent.role == r
	 * の時に agentの連続したtalk がtopic2gram = topic となる条件付き確率 */
    private Map<GameAgent, Map<Integer, Map<Role, Map<String, Double>>>> topicConditionalProbability = new HashMap<>();
    /* ファイルから事前分布を読む用List */
    private List<String> talk2gram_list;
    /* 各役職の人数 */
    private Map<Role, Double> roleNum;
    private double d;
    private List<String> topic_list;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                calcRoleProbability(e.talks);
                break;
            case ATTACK:
                updateRoleProbability(e.target, Role.WEREWOLF, false);
                break;
            case DIVINE:
            case MEDIUM:
                if (e.species == Species.HUMAN) {
                    updateRoleProbability(e.target, Role.WEREWOLF, false);
                } else {
                    updateRoleProbability(e.target, Role.WEREWOLF, true);
                }
                break;
            case GUARD:
                if (g.getSelf().role == Role.BODYGUARD) {
                    if (e.target != null) {
                        updateRoleProbability(e.target, Role.WEREWOLF, false);
                    }
                } else {
                    game.getAgentStream().filter(x -> !x.isAlive && !x.isSelf).forEach(x -> {
                        updateRoleProbability(x, Role.BODYGUARD, false);
                    });
                }
                break;
            case DAYSTART:
                /* ログを見る */
                g.getAliveOthers().stream().forEachOrdered(ga -> {
                    log(ga.getId() + roleProbability.get(ga));
                });
                break;
            default:
                break;
        }
    }

    private void calcRoleProbability(List<GameTalk> talks) {
        Map<GameAgent, Map<Role, Double>> rpTemp = roleProbabilityTemp;
        if (talks.get(0).getTurn() == 0) {
            rpTemp.entrySet().forEach(e -> {
                rpTemp.get(e.getKey()).entrySet().forEach(e2 -> {
                    e2.setValue(roleProbability.get(e.getKey()).get(e2.getKey()));
                });
            });
        }
        Stream<GameTalk> talkstream = talks.get(0).getTurn() == 0 ? game.getAllTalks() : talks.stream();
        talkstream.filter(x -> x.getTalker() != game.getSelf()).forEach(t -> {
        	if(t.getTurn() == 0 && t.getDay() == 1){
        		old_topic = "GameStart";
        		now_topic = t.getTopic().toString();
        	}else if(t.getTurn() == 0 && t.getDay() > 1){
        		old_topic = now_topic;
        		now_topic = "DayChange";
        	}else if((t.getTopic().toString().equalsIgnoreCase("SKIP")&&now_topic.equalsIgnoreCase("SKIP"))
        			||(t.getTopic().toString().equalsIgnoreCase("OVER")&&now_topic.equalsIgnoreCase("SKIP"))){

        	}else if(t.getTopic().toString().equalsIgnoreCase("OVER")&&!now_topic.equalsIgnoreCase("SKIP")){
        		old_topic = now_topic;
        		now_topic = "SKIP";
        	}else{
        		old_topic = now_topic;
        		now_topic = t.getTopic().toString();
        	}
        	if(old_topic!=null&&now_topic!=null){
        	topic2gram = now_topic + "," + old_topic;

            rpTemp.put(t.getTalker(), calcRoleProbability(t.getTalker(), topic2gram, t.getDay(), rpTemp.get(t.getTalker())));
        	}
        	});
        Map<GameAgent, Double> zs = new HashMap<>();
        rpTemp.entrySet().stream().forEach(e -> {
            zs.put(e.getKey(), e.getValue().values().stream().mapToDouble(x -> x).sum());
            //e.getValue().entrySet().forEach(e2 -> e2.setValue(e2.getValue() / zs.get(e.getKey())));
        });
        reweight(rpTemp);
    }

    /**
     * target が role で ある/ない ことが確定した場合に確率を更新する。
     *
     * @param target
     * @param role
     * @param b
     */
    private void updateRoleProbability(GameAgent target, Role role, boolean b) {
        if (target == game.getSelf()) {
            return;
        }
        if (b) {
            for (Map.Entry<Role, Double> entry : roleProbability.get(target).entrySet()) {
                entry.setValue(0.0);
            }
            roleProbability.get(target).replace(role, 1.0);
        } else {
            roleProbability.get(target).put(role, 0.0);
        }
        reweight(roleProbability);
    }

    /**
     * @param anotherRoleProbability 各エージェントの役職確率を破壊的にリウェイトする。
     */
    private void reweight(Map<GameAgent, Map<Role, Double>> anotherRoleProbability) {
        roleNum.keySet().stream().forEach(role -> {
            /* 各エージェントの役職確率を、役職別に合計し、本来の人数と同じになるようリウェイト */
            double sum = anotherRoleProbability.keySet().stream()
                .mapToDouble(ag -> anotherRoleProbability.get(ag).get(role))
                .sum();
            anotherRoleProbability.keySet().stream().forEach(ag -> {
                anotherRoleProbability.get(ag).put(role, anotherRoleProbability.get(ag).get(role) / sum * roleNum.get(role));
            });
        });
        log(anotherRoleProbability);
        /* 各エージェントの役職確率がそのエージェント内で足して1になるようリウェイト */
        anotherRoleProbability.keySet().stream().forEach(ag -> {
            Map<Role, Double> rp = anotherRoleProbability.get(ag);
            double sum = rp.entrySet().stream().mapToDouble(e -> e.getValue()).sum();
            rp.entrySet().stream().forEach(e -> e.setValue(e.getValue() / sum));
        });
        log(anotherRoleProbability);
    }

    /**
     * 事後確率の分子を計算する。<br>
     * （規格化されていない）
     *
     * @param agent
     * @param topic
     * @param day
     * @param originalP
     * @return
     */

	private Map<Role, Double> calcRoleProbability(GameAgent agent, String topic, int day, Map<Role, Double> originalP) {
        Map<Role, Double> newP = new HashMap<>();


    	String str = day + "," + topic;

    	//den=分子,num=分母
    	double den = 1.0;
    	double num = 1.0;

        for (Entry<Role, Double> entry : originalP.entrySet()) {
            final Role role = entry.getKey();
            double p = entry.getValue();

    	for (String tl: talk2gram_list) {
    		if(tl.contains(str)){
    			int i = 0;
    			String data[] = tl.split(",");
    			switch (role) {
				case BODYGUARD:
					i = 3;
				case MEDIUM:
					i = 5;
				case POSSESSED:
					i = 7;
				case SEER:
					i = 9;
				case VILLAGER:
					i = 11;
				case WEREWOLF:
					i = 13;
				case FOX:
				case FREEMASON:
				}

    			/*スムージング*/
       			den = 1.0 + Double.parseDouble(data[i]);
				if(data[0].equals("1")&&data[2].equalsIgnoreCase("SKIP")){
					num = Double.parseDouble(data[i + 1]) + 11.0;
					break;
				}else if((data[0].equals("1")&&!data[2].equalsIgnoreCase("SKIP"))
						||(!data[0].equals("1")&&data[2].equalsIgnoreCase("SKIP"))
						||(!data[0].equals("1")&&data[2].equalsIgnoreCase("DayChange"))){
					num = Double.parseDouble(data[i + 1]) + 12.0;
					break;
				}else{
					num = Double.parseDouble(data[i + 1]) + 13.0;
					break;
				}
    		}
   	 }
    	//actFrequencyModelで出力される数値に近くなるように再計算.actFrequencyModelもN-gramモデル実装済みの場合不要
		d = 150.0 + (Math.log(den / num) * 10.0);

         newP.put(role, p + d);
        }
        return newP;
    }


    public double getRoleProbability(GameAgent agent, Role role) {
        return roleProbabilityTemp.get(agent).get(role);
    }

    public Map<Role, Double> getRoleProbability(GameAgent agent) {
        return roleProbabilityTemp.get(agent);
    }

    public Map<GameAgent, Map<Role, Double>> getRoleProbability() {
        return roleProbabilityTemp;
    }

    /* 試合開始時の処理 */
    public void startGame(Game game) {

    	if (talk2gram_list == null) {
    		talk2gram_list = new ArrayList<>();
			talk2gram_list.clear();
            String filename = "talk2gram.dat";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(TalkFrequencyModel.class.getClassLoader().getResourceAsStream("jp/or/plala/amail/rin0114/aiwolf/metagame/" + filename)))) {;
                String line = null;

    			while ((line = br.readLine()) != null) {
    				talk2gram_list.add(line);
    			}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    	if (topic_list == null) {
    		topic_list = new ArrayList<>();
    		topic_list.clear();
    		topic_list.add("GameStart");
    		topic_list.add("DayChange");
    		for(Topic t:Topic.values()){
    			 if (t != Topic.ATTACK && t != Topic.OVER) {
    				 topic_list.add(t.toString());
    			 }
    		}
        }

        this.game = game;

        /* 役職人数を初期化 */
        roleNum = new HashMap<>();
        if (game.getVillageSize() == 5) {
            roleNum.put(Role.VILLAGER, 2.0);
            roleNum.put(Role.SEER, 1.0);
            roleNum.put(Role.WEREWOLF, 1.0);
            roleNum.put(Role.POSSESSED, 1.0);
        } else {
            roleNum.put(Role.VILLAGER, 8.0);
            roleNum.put(Role.SEER, 1.0);
            roleNum.put(Role.MEDIUM, 1.0);
            roleNum.put(Role.BODYGUARD, 1.0);
            roleNum.put(Role.WEREWOLF, 3.0);
            roleNum.put(Role.POSSESSED, 1.0);
        }
        /* エージェント別役職確率を初期化 */
        roleProbability = new HashMap<>();
        Role myRole = game.getSelf().role;
        for (GameAgent ga : game.getAgents()) {
            roleProbability.put(ga, new HashMap<>());
            Map<Role, Double> rp = roleProbability.get(ga);
            if (ga == game.getSelf() || (myRole == Role.WEREWOLF && ga.role == Role.WEREWOLF)) {
                roleNum.keySet().stream().forEach(x -> {
                    if (x == ga.role) {
                        rp.put(x, 1.0);
                    } else {
                        rp.put(x, 0.0);
                    }
                });
            } else {
                int vil = game.getVillageSize() - (myRole == Role.WEREWOLF && game.getVillageSize() == 15 ? 3 : 1);
                roleNum.keySet().stream().forEach(x -> {
                    double num = roleNum.get(x);
                    if (x == myRole) {
                        num -= myRole == Role.WEREWOLF && game.getVillageSize() == 15 ? 3.0 : 1.0;
                    }
                    rp.put(x, num / vil);
                });
            }
            /* 条件付き確率の初期化 */
            topicConditionalProbability.put(ga, getTopicProbabilityByIndex(ga.getIndex()));
        }
        roleProbabilityTemp = new HashMap<>();
        roleProbability.entrySet().forEach(e -> {
            roleProbabilityTemp.put(e.getKey(), new HashMap<>());
            e.getValue().entrySet().forEach(e2 -> roleProbabilityTemp.get(e.getKey()).put(e2.getKey(), e2.getValue()));
        });
        game.addGameEventListener(this);//EventListenerに登録
    }

    public void endGame(Game game) {
				try {
					updateTopicProbability(game);
				} catch (Exception e) {}
    }

    /* エージェント別発言条件付き確率 */
    private Map<Integer, Map<Integer, Map<Role, Map<String, Double>>>> topicProbability = new HashMap<>();
    /* エージェント別役職経験回数 */
    private Map<Integer, Map<Role, Integer>> roleCount = new HashMap<>();

    /**
     * 1ゲーム終了時、そのゲーム中発生した発言に対応する確率を、少し上げる
     *
     * @param game
     */
    private void updateTopicProbability(Game game) throws Exception{
        game.getAgentStream().filter(x -> !x.isSelf).forEach(x -> {
            if (!roleCount.containsKey(x.getIndex())) {
                roleCount.put(x.getIndex(), new HashMap<>());
            }
            if (roleCount.get(x.getIndex()).containsKey(x.role)) {
                roleCount.get(x.getIndex()).put(x.role, roleCount.get(x.getIndex()).get(x.role) + 1);
            } else {
                roleCount.get(x.getIndex()).put(x.role, 1);
            }
        });
        game.getAllTalks().forEach(t -> {
            GameAgent ga = t.getTalker();
            if (game.getSelf() != ga) {
                final int day = t.getDay();
                final int agentIndex = ga.getIndex();
                final Role role = ga.role;
                for (String ot : topic_list) {
                	 for (String nt : topic_list) {

                String topic2 = nt + "," + ot;

                try {
                for (Entry<String, Double> entry : topicProbability.get(agentIndex)
						.get(day)
						.get(role)
						.entrySet()) {
                	if(topicProbability.get(agentIndex)
                			.get(day)
                			.get(role)
    						.entrySet() == null){
				double freq = entry.getValue() * (12.0 + roleCount.get(agentIndex).get(role));
				double div = 13.0 + roleCount.get(agentIndex).get(role);
				entry.setValue((entry.getKey() == topic2 ? (freq + 1.0) : freq) / div);
                	}
                	}
                } catch (Exception e) {
				}
            }
                }
            }

        });
    }

    public Map<Integer, Map<Role, Map<String, Double>>> getTopicProbabilityByIndex(int index) {
        Map<Integer, Map<Integer, Map<Role, Map<String, Double>>>> tp = topicProbability;
        if (tp.get(index) == null) {
            tp.put(index, new HashMap<>());
        }
        return tp.get(index);
    }
}
