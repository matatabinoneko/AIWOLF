package tera.aiwolf.framework;

import static tera.aiwolf.util.Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import tera.aiwolf.util.ListMap;

/**
 * ゲーム全体の情報を保持する構造体
 */
public class Game {

    private List<GameAgent> gameAgents = new ArrayList<>();
    private Map<Agent, GameAgent> agentPlayerMap = new HashMap<>();
    private GameAgent self;
    private String gameId;

    public String getGameId() {
        return gameId;
    }

    /**
     * GameAgent全員（のコピー）を返す
     *
     */
    public List<GameAgent> getAgents() {
        return new ArrayList<>(gameAgents);
    }

    /**
     * 指定されたIndex（0始まり、GameAgent.getIndexに対応）のGameAgentを返す
     */
    public GameAgent getAgentAt(int index) {
        return gameAgents.get(index);
    }

    /**
     * GameAgent全員（のコピー）のStreamを返す
     *
     */
    public Stream<GameAgent> getAgentStream() {
        return new ArrayList<>(gameAgents).stream();
    }

    /**
     * 村の初期サイズを返す（5or15）
     *
     * @return
     */
    public int getVillageSize() {
        return gameAgents.size();
    }

    /**
     * 生き残っている村人の数を返す
     *
     * @return
     */
    public int getAliveSize() {
        return this.getAlives().size();
    }

    public GameAgent getSelf() {
        return self;
    }

    public Team getTeam() {
        return team;
    }

    private Team team;
    private MetagameModel meta;

    Game(MetagameModel meta, GameInfo gameInfo, GameSetting gameSetting) {
        this.meta = meta;
        this.gameId = "g" + meta.numberOfGames;

        //GameAgentの作成
        gameInfo.getAgentList().forEach(agent -> {
            GameAgent gp = new GameAgent(this, agent);
            if (agent == gameInfo.getAgent()) {
                gp.isSelf = true;
                gp.role = gameInfo.getRole();
                this.team = gp.role.getTeam();
                this.self = gp;
            }
            if (gameInfo.getRoleMap().containsKey(agent)) {
                // 人狼なら最初に役職わかる他人がいる。
                gp.role = gameInfo.getRoleMap().get(agent);
            }
            agentPlayerMap.put(agent, gp);
            gameAgents.add(gp);
        });

        meta.startGame(this);

        log("I am " + this.self.role);
        for (GameAgent ga : gameAgents) {
            log("Agent", ga, ga.role);
        }
    }

    private AbstractRole currentRole = null;

    void setCurrentRole(AbstractRole currentRole) {
        this.currentRole = currentRole;
    }

    public <T extends GameModel> T getGameModel() {
        return (T) currentRole.getModel();
    }

    public <T extends MetagameModel> T getMeta() {
        return (T) meta;
    }

    GameAgent toGameAgent(Agent agent) {
        return agentPlayerMap.get(agent);
    }

    public List<GameAgent> getAlives() {
        return gameAgents.stream().filter(gp -> gp.isAlive).collect(Collectors.toList());
    }

    public List<GameAgent> getAliveOthers() {
        return gameAgents.stream().filter(gp -> gp.isAlive && gp != self).collect(Collectors.toList());
    }

    /**
     * 今が何日目かを返す
     *
     * @return
     */
    public int getDay() {
        return day;
    }

    void dayStart() {
        log("### DAY START" + getDay() + "######");
        this.talk_turn = 0;
        this.whisper_turn = 0;
    }

    /**
     * 指定されたイベント種別のリストを返す
     *
     * @param type イベント種別
     * @return ゲーム開始時からの当該種別のイベント（時系列順）
     */
    public List<GameEvent> getEventsOf(EventType type) {
        return events.getList(type);
    }

    public GameEvent getLastEventOf(EventType type) {
        return events.getLast(type);
    }

    public List<GameEvent> getEventAtDay(EventType type, int day) {
        return events.getList(type).stream().filter(ev -> ev.day == day).collect(Collectors.toList());
    }

    private ListMap<EventType, GameEvent> events = new ListMap<>();
    private int day = 0;

    private List<GameEventListenr> eventListenrs = new ArrayList<>();

    public void addGameEventListener(GameEventListenr gel) {
        eventListenrs.add(gel);
    }

    private Map<String, GameTalk> idTalkMap = new LinkedHashMap<>();
    private ListMap<String, GameTalk> contentTalkMap = new ListMap<>();

    public GameTalk getTalkById(int day, int id) {
        return idTalkMap.get(day + "_" + id);
    }

    private Map<String, GameTalk> idWhisperMap = new LinkedHashMap<>();

    public GameTalk getWhisperById(int day, int id) {
        return idWhisperMap.get(day + "_" + id);
    }

    /**
     * 指定された発話と同内容の発話の一覧を返す
     *
     * @param talk
     * @return
     */
    public List<GameTalk> getSameTalk(GameTalk talk) {
        return contentTalkMap.getList(talk.talkContent());
    }

    /**
     * 全ての発話のStreamを返す
     *
     * @return
     */
    public Stream<GameTalk> getAllTalks() {
        return idTalkMap.values().stream();
    }

    /**
     * 同発言者、同内容の発話をDistinctしたStreamを返す
     *
     * @return
     */
    public Stream<GameTalk> getUniqueTalks() {
        return getAllTalks().collect(Collectors.toMap(t -> (t.getTalker().getId() + "_" + t.talkContent()), t -> t, (p, q) -> p)).values().stream();
    }

    private Set<Integer> talkHashCodes = new HashSet<>();

    private void addEvent(GameEvent e) {
        events.add(e.type, e);
        //会話の記録
        if (e.type == EventType.TALK) e.talks.forEach(t -> {
                idTalkMap.put(t.talkUniqueId(), t);
                contentTalkMap.add(t.talkContent(), t);
                int talkHash = (t.getTalker() + "_" + t.talkContent()).hashCode();
                if (talkHashCodes.contains(talkHash)) t.isRepeat = true;
                else talkHashCodes.add(talkHash);

                if (t.getTopic() == Topic.COMINGOUT) {
                    t.getTalker().coRole = t.getRole();
                }
            });
        //ささやきの記録
        if (e.type == EventType.WHISPER) e.talks.forEach(t -> {
                idWhisperMap.put(t.talkUniqueId(), t);
            });
        log("event", e);
        eventListenrs.forEach(gel -> {
            try {
                gel.handleEvent(this, e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    void updateWorld(GameInfo gameInfo) {
        if (game_end) return;
        boolean isNewDay = day != gameInfo.getDay();
        day = gameInfo.getDay();

        //生存状況の更新
        gameAgents.forEach(gp -> gp.isAlive = gameInfo.getAliveAgentList().contains(gp.agent));

        if (isNewDay) addEvent(new GameEvent(EventType.DAYSTART, day));

        //ゲーム終了時の役割情報開示
        gameAgents.forEach(gp -> {
            if (gameInfo.getRoleMap().get(gp.agent) != null) gp.role = gameInfo.getRoleMap().get(gp.agent);
        });

        //占いイベントの記録
        Judge divJudge = gameInfo.getDivineResult();
        if (divJudge != null) {
            GameEvent event = events.getLast(EventType.DIVINE);
            if (event == null || event.day != divJudge.getDay() - 1) {
                addEvent(new GameEvent(EventType.DIVINE, this, divJudge));
            }
        }

        //投票の処理
        if (!gameInfo.getVoteList().isEmpty()) {
            if (gameInfo.getVoteList().get(0).getDay() == voteDay) {
                List<GameVote> votes = new ArrayList<>();
                for (Vote v : gameInfo.getVoteList()) {
                    votes.add(new GameVote(v.getDay(), there_was_revote_flag, toGameAgent(v.getAgent()), toGameAgent(v.getTarget())));
                }
                addEvent(new GameEvent(EventType.VOTE, votes.get(0).day, votes));
                there_was_revote_flag = false;
                voteDay++;
            }
        }
        if (!gameInfo.getLatestVoteList().isEmpty()) {
            if (gameInfo.getLatestVoteList().get(0).getDay() == voteDay) {
                List<GameVote> votes = new ArrayList<>();
                for (Vote v : gameInfo.getLatestVoteList()) {
                    votes.add(new GameVote(v.getDay(), there_was_revote_flag, toGameAgent(v.getAgent()), toGameAgent(v.getTarget())));
                }
                addEvent(new GameEvent(EventType.VOTE, votes.get(0).day, votes));
                there_was_revote_flag = false;
                voteDay++;
            }
        }

        //追放イベントの記録
        executeCheck(gameInfo.getExecutedAgent());
        executeCheck(gameInfo.getLatestExecutedAgent());

        //霊能力者イベントの記録
        Judge medJudge = gameInfo.getMediumResult();
        if (medJudge != null) {
            GameEvent event = events.getLast(EventType.MEDIUM);
            if (event == null || event.day != medJudge.getDay() - 1) {
                addEvent(new GameEvent(EventType.MEDIUM, this, medJudge));
            }
        }

        //襲撃投票結果の処理
        if (!gameInfo.getAttackVoteList().isEmpty()) {
            if (gameInfo.getAttackVoteList().get(0).getDay() == attackVoteDay) {
                List<GameVote> votes = new ArrayList<>();
                for (Vote v : gameInfo.getAttackVoteList()) {
                    votes.add(new GameVote(v.getDay(), there_was_attack_revote_flag, toGameAgent(v.getAgent()), toGameAgent(v.getTarget())));
                }
                addEvent(new GameEvent(EventType.ATTACK_VOTE, votes.get(0).day, votes));
                there_was_attack_revote_flag = false;
                attackVoteDay++;
            }
        }
        if (!gameInfo.getLatestAttackVoteList().isEmpty()) {
            if (gameInfo.getLatestAttackVoteList().get(0).getDay() == attackVoteDay) {
                List<GameVote> votes = new ArrayList<>();
                for (Vote v : gameInfo.getLatestAttackVoteList()) {
                    votes.add(new GameVote(v.getDay(), there_was_attack_revote_flag, toGameAgent(v.getAgent()), toGameAgent(v.getTarget())));
                }
                addEvent(new GameEvent(EventType.ATTACK_VOTE, votes.get(0).day, votes));
                there_was_attack_revote_flag = false;
                attackVoteDay++;
            }
        }

        //襲撃イベントの記録
        boolean attackedAdded = false;
        if (gameInfo.getLastDeadAgentList().size() >= 1) {
            Agent lastAttacked = gameInfo.getLastDeadAgentList().get(0);
            GameEvent event = events.getLast(EventType.ATTACK);
            if (event == null || !event.target.agent.equals(lastAttacked)) {
                addEvent(new GameEvent(EventType.ATTACK, day - 1, toGameAgent(lastAttacked)));
                toGameAgent(lastAttacked).isAttacked = true;
                attackedAdded = true;
            }
        }
        if (day > 1 && !attackedAdded && isNewDay) {
            Agent guarded = gameInfo.getGuardedAgent();
            addEvent(new GameEvent(EventType.GUARD, day - 1, toGameAgent(guarded)));
        }
        Agent lastAttacked = gameInfo.getAttackedAgent();
        if (lastAttacked != null) {
            GameEvent event = events.getLast(EventType.VICTIM_DECIDED);
            if (event == null || event.day != day - 1) {
                addEvent(new GameEvent(EventType.VICTIM_DECIDED, day - 1, toGameAgent(lastAttacked)));
            }
        }

        //会話記録のキャプチャ
        if (!gameInfo.getTalkList().isEmpty()) {
            List<GameTalk> talks = new ArrayList<>();
            for (Talk talk : gameInfo.getTalkList()) {
                if (talk.getTurn() == talk_turn) {
                    talks.add(new GameTalk(talk, this, false));
                }
            }
            if (!talks.isEmpty()) {
                this.addEvent(new GameEvent(EventType.TALK, talks));
                talk_turn++;
            }
        }

        //ささやき記録のキャプチャ
        if (!gameInfo.getWhisperList().isEmpty()) {
            List<GameTalk> whispers = new ArrayList<>();
            for (Talk talk : gameInfo.getWhisperList()) {
                if (talk.getTurn() == whisper_turn) {
                    if (!talk.isOver()) {
                        whispers.add(new GameTalk(talk, this, true));
                    }
                }
            }
            if (!whispers.isEmpty()) {
                this.addEvent(new GameEvent(EventType.WHISPER, whispers));
                whisper_turn++;
            }
        }
    }

    private void executeCheck(Agent agent) {
        if (agent != null) {
            boolean neverExecuted = true;
            for (GameEvent exeEvent : getEventsOf(EventType.EXECUTE)) {
                if (exeEvent.target.agent.equals(agent)) {
                    neverExecuted = false;
                    break;
                }
            }
            if (neverExecuted) {
                addEvent(new GameEvent(EventType.EXECUTE, executionDay++, toGameAgent(agent)));
                toGameAgent(agent).isExecuted = true;
            }
        }
    }

    private boolean there_was_revote_flag = false;
    private boolean there_was_attack_revote_flag = false;
    private boolean game_end = false;
    private int talk_turn = 0;
    private int whisper_turn = 0;
    private int executionDay = 1;
    private int voteDay = 1;
    private int attackVoteDay = 1;

    void notifyAtkRevote() {
        there_was_attack_revote_flag = true;
        attackVoteDay--;
    }

    void notifyRevote() {
        there_was_revote_flag = true;
        voteDay--;
    }

    private Team wonTeam;

    public Team getWonTeam() {
        return wonTeam;
    }

    void finish() {
        if (!game_end) {
            log("<<<<<<<<<<<Game End>>>>>>>>>>>");
            game_end = true;
            boolean allWolfDead = true;
            for (GameAgent gp : gameAgents) {
                if (gp.role == Role.WEREWOLF && gp.isAlive) allWolfDead = false;
            }

            wonTeam = allWolfDead ? Team.VILLAGER : Team.WEREWOLF;

            try {
                meta.finishGame(this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            log(wonTeam == getSelf().role.getTeam() ? "I won" : "I lost");
            this.meta.numberOfGames++;
        }
    }

}
