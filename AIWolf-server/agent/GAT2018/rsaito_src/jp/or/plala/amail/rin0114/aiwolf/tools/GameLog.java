package jp.or.plala.amail.rin0114.aiwolf.tools;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.util.ListMap;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Team;

/**
 * 配布された人狼知能プラットフォームから生成されるログファイルをパースするクラス
 */
public class GameLog {
	// 自分のチームの名
    public static final String MY_AGENT_NAME = "cndl";

    public GameLog(String name, int size, List<String> lines) {
        this.name = name;
        agents = new GameLogAgent[size + 1];

        for (String line : lines) {
            String[] data = line.split(",");
            EventLog l = new EventLog();
            l.day = Integer.parseInt(data[0]);
            int i = Integer.parseInt(data[2]);
            switch (data[1]) {
                case "status":
                    if (agents[i] == null) {
                        GameLogAgent ag = new GameLogAgent();
                        ag.role = Role.valueOf(data[3]);
                        ag.ai = data[5].startsWith("Dummy") ? "Dummy" : data[5];
                        agents[i] = ag;
                    }
                    l.type = EventType.DAYSTART;
                    l.agtIdx = i;
                    l.status = Status.valueOf(data[4]);
                    if (MY_AGENT_NAME.equals(agents[i].ai)) {
                        if (died == -1 && l.status == Status.DEAD) {
                            died = l.day - 1;
                        }
                    }
                    break;
                case "whisper":
                    l.type = EventType.WHISPER;
                case "talk":
                    if (l.type == null) l.type = EventType.TALK;
                    l.talkId = i;
                    l.turn = Integer.parseInt(data[3]);
                    l.agtIdx = Integer.parseInt(data[4]);
                    l.talk = new Content(data[5]);
                    break;
                case "attack":
                    l.type = EventType.ATTACK;
                    l.tgtIdx = i;
                    l.atkSuccess = Boolean.parseBoolean(data[3]);
                    break;
                case "execute":
                    l.type = EventType.EXECUTE;
                    l.tgtIdx = i;
                    break;
                case "guard":
                    l.type = EventType.GUARD;
                case "divine":
                    if (l.type == null) l.type = EventType.DIVINE;
                case "vote":
                    if (l.type == null) l.type = EventType.VOTE;
                case "attackVote":
                    if (l.type == null) l.type = EventType.ATTACK_VOTE;
                    l.agtIdx = i;
                    l.tgtIdx = Integer.parseInt(data[3]);
                    break;
                case "result":
                    gameDay = l.day;
                    winner = Team.valueOf(data[4]);
                    remHuman = i;
                    remWolf = Integer.parseInt(data[3]);
                    for (int j = 1; j < agents.length; j++) {
                        if (agents[j].ai.equals(MY_AGENT_NAME)) {
                            won = agents[j].role.getTeam() == winner;
                            break;
                        }
                    }
                    break;

            }
            if (l.type != null) {
                log.add(l.type, l);
                allLog.add(l);
            }
        }
    }

    /**
     * ログの名前
     */
    public String name;
    /**
     * 自分が死んだ日。最後まで生き残った場合は-1
     */
    public int died = -1;

    public GameLog(File logFile, int size) throws Exception {
        this(logFile.getName(), size, Files.readAllLines(logFile.toPath()));
    }

    public Role roleOf(String ai) {
        return Arrays.stream(agents).filter(ag -> ag != null && ag.ai.equals(ai)).findFirst().get().role;
    }

    public GameLogAgent[] agents;
    public ListMap<EventType, EventLog> log = new ListMap<>();
    public List<EventLog> allLog = new ArrayList<>();

    /**
     * 終了時に生き残った村人数
     */
    public int remHuman;
    /**
     * 終了時に生き残った狼数
     */
    public int remWolf;
    /**
     * ゲーム終了時の日数
     */
    public int gameDay;
    /**
     * ゲームに勝利したチーム
     */
    public Team winner;
    /**
     * 自分がゲームに勝利したか
     */
    public boolean won;

    public static class GameLogAgent {

        public String ai;
        public Role role;

        @Override
        public String toString() {
            return ai + "/" + role;
        }

    }

    public static class EventLog {

        public int day;
        /**
         * 備考：GUARDについては誰を守ったかの宣言内容、DAYSTARTは現在のプレイヤー状態を示す
         */
        public EventType type;
        public int agtIdx;
        public int tgtIdx;
        public int talkId;
        public int turn;
        public Status status;
        public Content talk;
        public Role role;
        public boolean atkSuccess;
    }

}
