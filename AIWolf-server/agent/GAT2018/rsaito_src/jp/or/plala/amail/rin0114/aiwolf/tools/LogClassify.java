package jp.or.plala.amail.rin0114.aiwolf.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jp.or.plala.amail.rin0114.aiwolf.framework.EventType;
import jp.or.plala.amail.rin0114.aiwolf.util.HashCounter;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;

/**
 * 公式ログを解凍、分類、変換、集計するツール
 */
public class LogClassify {

    public static void main(String[] args) throws Exception {
        new LogClassify().process(Paths.get("予選ログ"));
    }

    private static Pattern p = Pattern.compile(",(VILLAGER|SEER|BODYGUARD|POSSESSED|WEREWOLF|MEDIUM),");

    public void process(Path base) throws Exception {
        Path zipDir = base.resolve("zip");
        Path logDir = base.resolve("log");
        Path readDir = base.resolve("readable");
        if (false) {
            recursiveDeleteFile(logDir.toFile());
            recursiveDeleteFile(readDir.toFile());
        }

        if (!Files.exists(logDir)) Files.createDirectory(logDir);
        if (!Files.exists(readDir)) Files.createDirectory(readDir);

        for (File rev : zipDir.toFile().listFiles()) {
            System.out.println(rev);
            if (rev.getName().endsWith(".DS_Store")) {
                continue;
            }
            String revision = rev.getName();
            Path logRevDir = logDir.resolve(revision);
            if (!Files.exists(logRevDir)) Files.createDirectory(logRevDir);
            Path readRevDir = readDir.resolve(revision);
            if (!Files.exists(readRevDir)) Files.createDirectory(readRevDir);
            int sequence = 1;
            for (File zip : rev.listFiles()) {
                ZipFile zf = new ZipFile(zip);
                if (zf.size() != 100 && zf.size() != 50 && zf.size() != 20) {
                    System.out.println("Less than 100 " + zf.size() + "\t" + zip);
                    continue;
                }
                //人数の判定
                int num = 5;
                Optional<? extends ZipEntry> ze = zf.stream().filter(z -> !z.isDirectory()).findFirst();
                if (ze.isPresent()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(ze.get())))) {
                        List<String> lines = reader.lines().collect(Collectors.toList());
                        if (lines.stream().limit(15).allMatch(s -> s.contains("status"))) {
                            num = 15;
                        }
                    }
                } else {
                    continue;
                }

                String gameName = num + "-" + String.format("%03d", sequence++);
                System.out.println(revision + "\t" + gameName);
                Path logRevGameDir = logRevDir.resolve(gameName);
                if (!Files.exists(logRevGameDir)) Files.createDirectory(logRevGameDir);

                int numFinal = num;

                zf.stream().forEach(z -> {
                    if (!z.isDirectory()) {
                        String logName = z.getName().replace("game/", "").replace(".log", "");
                        int gameNum = Integer.parseInt(logName);
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(z)))) {
                            List<String> lines = reader.lines().collect(Collectors.toList());
                            //役職の確定
                            String role = null;
                            for (String line : lines) {
                                if (line.contains(GameLog.MY_AGENT_NAME)) {
                                    Matcher m = p.matcher(line);
                                    if (m.find()) role = m.group(1);
                                    break;
                                }
                            }
                            logName = logName + "-" + role;
                            //ログの書き込み（解凍）
                            Path logFile = logRevGameDir.resolve(logName + ".log");
                            if (!Files.exists(logFile)) {
                                Files.write(logFile, lines);
                            }
                            //解析
                            analyze(readRevDir, revision, gameName, logName, gameNum, numFinal, lines);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        write(base);
    }
    HashCounter<String> talkC15 = new HashCounter<>();
    HashCounter<String> voteC15 = new HashCounter<>();
    HashCounter<String> gameC15 = new HashCounter<>();
    HashCounter<String> talkC5 = new HashCounter<>();
    HashCounter<String> voteC5 = new HashCounter<>();
    HashCounter<String> gameC5 = new HashCounter<>();

    void analyze(Path readDir, String rev, String gameName, String logName, int gameNum, int num, List<String> logs) throws IOException {
        GameLog log = new GameLog(logName, num, logs);
        Role role = log.roleOf(GameLog.MY_AGENT_NAME);
        HashCounter<String> talkC = num == 5 ? talkC5 : talkC15;
        HashCounter<String> voteC = num == 5 ? voteC5 : voteC15;
        HashCounter<String> gameC = num == 5 ? gameC5 : gameC15;
        StringBuilder game = new StringBuilder();
        StringBuilder atk = new StringBuilder();
        StringBuilder divine = new StringBuilder();
        StringBuilder bg = new StringBuilder();
        String diedOf = "";
        List<String> readableLog = new ArrayList<>();
        for (GameLog.EventLog l : log.allLog) {
            GameLog.GameLogAgent ag = log.agents[l.agtIdx];
            GameLog.GameLogAgent tgt = log.agents[l.tgtIdx];
            if (l.type == EventType.DAYSTART) {
            } else if (l.type == EventType.EXECUTE) {
                if (tgt != null && GameLog.MY_AGENT_NAME.equals(tgt.ai)) diedOf = "exe";
                game.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                if (tgt != null) readableLog.add(String.format("%s\t%s\t%s\t%s", l.day, "execute", tgt.ai, tgt.role));
            } else if (l.type == EventType.ATTACK) {
                atk.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                if (!l.atkSuccess) {
                    atk.append("!");
                } else {
                    if (tgt != null && GameLog.MY_AGENT_NAME.equals(tgt.ai)) diedOf = "atk";
                }
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s", l.day, "attack", tgt.ai, tgt.role, l.atkSuccess ? "" : "guarded"));
            } else if (l.type == EventType.DIVINE) {
                if (tgt != null) {
                    divine.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                    readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s", l.day, "divine", ag.ai, ag.role, tgt.ai, tgt.role));
                }

            } else if (l.type == EventType.GUARD) {
                bg.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s", l.day, "guard", ag.ai, ag.role, tgt.ai, tgt.role));
            } else if (l.type == EventType.TALK) {
                if (l.talk.getTopic() == Topic.VOTE) {
                    voteC.countPlus(String.join("\t",
                        rev,
                        "" + (log.winner == role.getTeam()),
                        "" + log.gameDay,
                        "" + l.day,
                        role.name(),
                        "declare",
                        ag.ai,
                        ag.role.name(),
                        log.agents[l.talk.getTarget().getAgentIdx()].role.name()
                    ));
                }
                talkC.countPlus(String.join("\t",
                    rev,
                    "" + (log.winner == role.getTeam()),
                    "" + l.day,
                    "" + l.turn,
                    ag.ai,
                    ag.role.name(),
                    l.talk.getTopic() == Topic.OPERATOR ? l.talk.getOperator().name() : l.talk.getTopic().name(),
                    Objects.toString(l.talk.getRole(), "-")
                ));

                GameLog.GameLogAgent talkTgt = null;
                if (l.talk.getTarget() != null) talkTgt = log.agents[l.talk.getTarget().getAgentIdx()];
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", l.day, "talk", l.turn, l.tgtIdx, ag.ai, ag.role,
                    l.talk.getTopic() == Topic.OPERATOR ? l.talk.getOperator().name() : l.talk.getTopic().name(),
                    Objects.toString(l.talk.getRole(), Objects.toString(l.talk.getResult(), "-")),
                    talkTgt != null ? talkTgt.ai : "",
                    talkTgt != null ? talkTgt.role : ""
                ));
            } else if (l.type == EventType.VOTE) {
                voteC.countPlus(String.join("\t",
                    rev,
                    "" + (log.winner == role.getTeam()),
                    "" + log.gameDay,
                    "" + l.day,
                    role.name(),
                    "vote",
                    ag.ai,
                    ag.role.name(),
                    tgt.role.name()
                ));
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", l.day, "vote", l.agtIdx, ag.ai, ag.role, l.tgtIdx, tgt.ai, tgt.role));
            }
        }
        Path readablePath = readDir.resolve(gameName + "-" + logName + ".txt");
        if (!Files.exists(readablePath)) Files.write(readablePath, readableLog);

        String div = Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.SEER).findFirst().get().ai;
        String pos = Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.POSSESSED).findFirst().get().ai;
        String med = num == 5 ? "" : Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.MEDIUM).findFirst().get().ai;
        String bdg = num == 5 ? "" : Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.BODYGUARD).findFirst().get().ai;
        String wlvs = Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.WEREWOLF).map(ww -> ww.ai).collect(Collectors.joining(","));

        String day2Rem = log.log.getList(EventType.DAYSTART).stream().filter(e -> e.day == 2 && e.status == Status.ALIVE).map(e -> log.agents[e.agtIdx].role.toString().substring(0, 1)).collect(Collectors.joining());

        gameC.countPlus(rev + "\t" + gameName + "\t" + gameNum + "\t" + logName + "\t" + Objects.toString(log.winner, "err") + "\t"
            + (log.winner == role.getTeam()) + "\t"
            + role + "\t"
            + log.gameDay + "\t"
            + log.remWolf + "\t"
            + log.died + "\t"
            + diedOf + "\t"
            + game + "\t"
            + game.substring(0, Math.min(game.length(), 1)) + "\t"
            + atk + "\t"
            + atk.substring(0, Math.min(atk.length(), 1)) + "\t"
            + divine + "\t"
            + divine.substring(0, Math.min(divine.length(), 1)) + "\t"
            + bg + "\t"
            + bg.substring(0, Math.min(bg.length(), 1)) + "\t"
            + div + "\t"
            + pos + "\t"
            + med + "\t"
            + bdg + "\t"
            + wlvs
        );

    }

    private void write(Path base) throws Exception {
        write(talkC5, base.resolve("talk5.txt"));
        write(voteC5, base.resolve("vote5.txt"));
        write(gameC5, base.resolve("game5.txt"));
        write(talkC15, base.resolve("talk15.txt"));
        write(voteC15, base.resolve("vote15.txt"));
        write(gameC15, base.resolve("game15.txt"));
    }

    private void write(HashCounter<String> counter, Path file) throws IOException {
        try (BufferedWriter result = Files.newBufferedWriter(file)) {
            for (String key : counter.getKeyList()) {
                result.append(key + "\t" + counter.getCount(key));
                result.newLine();
            }
        }
    }

    private static void recursiveDeleteFile(final File file) throws Exception {
        // 存在しない場合は処理終了
        if (!file.exists()) {
            return;
        }
        // 対象がディレクトリの場合は再帰処理
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                recursiveDeleteFile(child);
            }
        }
        // 対象がファイルもしくは配下が空のディレクトリの場合は削除する
        file.delete();
    }

}
