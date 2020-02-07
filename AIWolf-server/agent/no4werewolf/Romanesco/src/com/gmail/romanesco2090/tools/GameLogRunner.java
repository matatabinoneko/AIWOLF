package com.gmail.romanesco2090.tools;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.gmail.romanesco2090.cedec2018impl.RomanescoPlayer;
import com.gmail.romanesco2090.framework.AbstractPlayer;
import com.gmail.romanesco2090.framework.EventType;
import com.gmail.romanesco2090.tools.GameLog.EventLog;

/**
 * LogClassifyで解凍したログを読み込ませ、当該試合を疑似体験させるツール。モデルの精度評価等に。なお、狼周りの挙動は未実装。
 */
public class GameLogRunner {

    public static void main(String[] args) throws Exception {
        int vilSize = 15;//村のサイズ
        int iteration = 3;//何ゲームディレクトリ回すか
        GameLogRunner glr = new GameLogRunner();
loop:   for (String revision : new String[]{"501", "516", "543"}) {//対象にしたいRevisionを指定
            Path logDir = Paths.get("予選ログ").resolve("log").resolve(revision);
            for (File gameDir : logDir.toFile().listFiles()) {
                if (gameDir.getName().startsWith("" + vilSize)) {
                    glr.player = new RomanescoPlayer();
                    for (File logFile : gameDir.listFiles()) {
                        try {
                            GameLog log = new GameLog(logFile, vilSize);
                            glr.start(log);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                if (--iteration == 0) break loop;
            }
        }
        System.out.println(glr.totalGame);
    }

    private AbstractPlayer player;
    private Agent[] agents;
    private GameInfo info;
    private int totalGame = 0;

    public void start(GameLog log) throws Exception {
        //ゲーム状態
        Map<Agent, Status> statusMap = new HashMap<>();
        Map<Agent, Role> roleMapInner = new HashMap<>();
        Map<Agent, Role> roleMapOpen = new HashMap<>();
        totalGame++;

        //初期化
        info = new GameInfo();
        GameSetting setting = new GameSetting();
        agents = new Agent[log.agents.length];
        Role selfRole = null;
        Agent self = null;

        for (int i = 1; i < log.agents.length; i++) {
            Constructor<Agent> ag = Agent.class.getDeclaredConstructor(int.class);
            ag.setAccessible(true);
            Agent agent = ag.newInstance(i);
            agents[i] = agent;
            if (log.agents[i].ai.equals(GameLog.MY_AGENT_NAME)) {
                set("agent", agent);
                roleMapOpen.put(agent, log.agents[i].role);
                self = agent;
                selfRole = log.agents[i].role;
            } else {
                roleMapOpen.put(agent, null);
            }
            statusMap.put(agent, Status.ALIVE);
            roleMapInner.put(agent, log.agents[i].role);
        }
        set("roleMap", roleMapOpen);
        //set("roleMap", roleMapInner);//ここでroleMapInnerを指定すると、初期から全員の役職が判る状態になる。デバッグ/評価のために
        set("statusMap", statusMap);
        player.initialize(info, setting);

        //開始
        List<EventLog> buffer = new ArrayList<>();
        EventType lastType = null;
        int turn = 0;
        for (EventLog e : log.allLog) {
            if (lastType != null && lastType != e.type) {
                switch (lastType) {
                    case DAYSTART:
                        //TODO このあたりでモデルの評価コードを入れても良い
                        player.update(info);
                        set("day", e.day);
                        player.dayStart();
                        player.update(info);
                        if (e.day > 0) player.talk();
                        break;
                    case WHISPER:
                        break;
                    case TALK:
                        setTalk(buffer);
                        //最後の会話が終わったら投票
                        player.update(info);
                        player.vote();
                        break;
                    case VOTE:
                        List<Vote> votes = buffer.stream().map(ev -> new Vote(ev.day, agents[ev.agtIdx], agents[ev.tgtIdx])).collect(Collectors.toList());
                        buffer.clear();
                        set("voteList", votes);
                        set("latestVoteList", votes);
                        break;
                }
            }

            switch (e.type) {
                case DAYSTART:
                    statusMap.put(agents[e.agtIdx], e.status);
                    break;
                case DIVINE:
                    if (selfRole == Role.SEER) {
                        Agent targetAgent = agents[e.tgtIdx];
                        Role role = roleMapInner.get(targetAgent);
                        set("divineResult", new Judge(e.day, self, targetAgent, role.getSpecies()));
                    }
                    break;
                case EXECUTE:
                    //追放情報
                    set("executedAgent", agents[e.tgtIdx]);
                    set("latestExecutedAgent", agents[e.tgtIdx]);
                    statusMap.put(agents[e.tgtIdx], Status.DEAD);
                    if (selfRole == Role.MEDIUM) {
                        Role mr = roleMapInner.get(agents[e.tgtIdx]);
                        set("mediumResult", new Judge(e.day, self, agents[e.tgtIdx], mr.getSpecies()));
                    }
                    break;
                case VOTE:
                    if (buffer.size() > info.getAliveAgentList().size()) {
                        //再投票
                        List<Vote> votes = buffer.stream().limit(info.getAliveAgentList().size()).map(ev -> new Vote(ev.day, agents[ev.agtIdx], agents[ev.tgtIdx])).collect(Collectors.toList());
                        set("voteList", votes);
                        set("latestVoteList", votes);
                        player.update(info);
                        player.vote();
                        buffer = new ArrayList(Arrays.asList(buffer.get(0)));
                    }
                    buffer.add(e);
                    break;
                case TALK:
                    if (e.turn != turn) {
                        setTalk(buffer);
                        player.update(info);
                        player.talk();
                    }
                    //ためる
                    buffer.add(e);
                    turn = e.turn;
                    break;
                case ATTACK:
                    if (e.atkSuccess) {
                        //襲撃情報
                        set("attackedAgent", agents[e.tgtIdx]);
                        set("lastDeadAgentList", Arrays.asList(agents[e.tgtIdx]));
                        statusMap.put(agents[e.tgtIdx], Status.DEAD);
                    }
                    break;
                case GUARD:
                    player.guard();
            }
            lastType = e.type;
        }

        //終了処理
        set("roleMap", roleMapInner);
        player.update(info);
        player.finish();

    }

    private void set(String fieldName, Object o) throws Exception {
        Field f = GameInfo.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(info, o);
    }

    private void setTalk(List<EventLog> buffer) throws Exception {
        set("talkList", buffer.stream().map(ev -> new Talk(ev.talkId, ev.day, ev.turn, agents[ev.agtIdx], ev.talk.getText())).collect(Collectors.toList()));
        buffer.clear();
    }

}
