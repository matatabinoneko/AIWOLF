package com.gmail.shoot7arrow25.player;

import com.gmail.shoot7arrow25.pattern.PatternProcess;
import com.gmail.shoot7arrow25.pattern.PatternProcessSubjective;
import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.*;

/** ���ׂĂ̖�E�̃x�[�X�ƂȂ�N���X */
public class NeoElizaBasePlayer implements Player {
    /** ���̃G�[�W�F���g */
    Agent me;
    /** ���t */
    int day;
    /** talk()�ł��邩���ԑт� */
    boolean canTalk;
    /** whisper()�ł��邩���ԑт� */
    boolean canWhisper;
    /** �ŐV�̃Q�[����� */
    GameInfo currentGameInfo;
    /** �����ȊO�̐����G�[�W�F���g */
    List<Agent> aliveOthers;
    /** �Ǖ����ꂽ�G�[�W�F���g */
    List<Agent> executedAgents = new ArrayList<>();
    /** �E���ꂽ�G�[�W�F���g */
    List<Agent> killedAgents = new ArrayList<>();
    /** �������ꂽ�肢���ʕ񍐂̃��X�g */
    List<Judge> divinationList = new ArrayList<>();
    /** �������ꂽ��}���ʕ񍐂̃��X�g */
    List<Judge> identList = new ArrayList<>();
    /** �����p�҂��s�� */
    Deque<Content> talkQueue = new LinkedList<>();
    /** �����p�҂��s�� */
    Deque<Content> whisperQueue = new LinkedList<>();
    /** ���[���� */
    Agent voteCandidate;
    /** �錾�ςݓ��[���� */
    Agent declaredVoteCandidate;
    /** �P�����[���� */
    Agent attackVoteCandidate;
    /** �錾�ςݏP�����[���� */
    Agent declaredAttackVoteCandidate;
    /** �J�~���O�A�E�g�� */
    Map<Agent, Role> comingoutMap = new HashMap<>();
    /** GameInfo.talkList�ǂݍ��݂̃w�b�h */
    int talkListHead;
    /** �l�ԃ��X�g */
    List<Agent> humans = new ArrayList<>();
    /** �l�T���X�g */
    List<Agent> werewolves = new ArrayList<>();
    /** �v���C���[�l�� */
    Integer playerNum = 0;

    /** �G�[�W�F���g�������Ă��邩�ǂ�����Ԃ� */
    protected boolean isAlive(Agent agent) {
        return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
    }

    /** �G�[�W�F���g���E���ꂽ���ǂ�����Ԃ� */
    protected boolean isKilled(Agent agent) {
        return killedAgents.contains(agent);
    }

    /** �G�[�W�F���g���J�~���O�A�E�g�������ǂ�����Ԃ� */
    protected boolean isCo(Agent agent) {
        return comingoutMap.containsKey(agent);
    }

    /** ��E���J�~���O�A�E�g���ꂽ���ǂ�����Ԃ� */
    protected boolean isCo(Role role) {
        return comingoutMap.containsValue(role);
    }

    /** �G�[�W�F���g���l�Ԃ��ǂ�����Ԃ� */
    protected boolean isHuman(Agent agent) {
        return humans.contains(agent);
    }

    /** �G�[�W�F���g���l�T���ǂ�����Ԃ� */
    protected boolean isWerewolf(Agent agent) {
        return werewolves.contains(agent);
    }

    /** ���X�g���烉���_���ɑI��ŕԂ� */
    protected <T> T randomSelect(List<T> list) {
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get((int) (Math.random() * list.size()));
        }
    }

    public String getName() {
        return "NeoElizaBasePlayer";
    }

    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        day = -1;
        me = gameInfo.getAgent();
        aliveOthers = new ArrayList<>(gameInfo.getAliveAgentList());
        aliveOthers.remove(me);
        executedAgents.clear();
        killedAgents.clear();
        divinationList.clear();
        identList.clear();
        comingoutMap.clear();
        humans.clear();
        werewolves.clear();
        playerNum = gameInfo.getAgentList().size();
        patternScoreMap.clear();
        patternScoreMapSubjective.clear();
        if (playerNum == 5) {
            go(gameInfo);
        }
    }

    public void update(GameInfo gameInfo) {
        currentGameInfo = gameInfo;
        // 1���̍ŏ��̌Ăяo����dayStart()�̑O�Ȃ̂ŉ������Ȃ�
        if (currentGameInfo.getDay() == day + 1) {
            day = currentGameInfo.getDay();
            return;
        }
        // 2��ڂ̌Ăяo���ȍ~
        // �P���E�J�~���O�A�E�g��񂩂炠�肦�Ȃ��p�^�[�����폜����
        PatternProcess patternProcess = new PatternProcess();
        patternScoreMap = patternProcess.removePatternWithCoVillager(comingoutMap, patternScoreMap);
        patternScoreMapSubjective = patternProcess.removePatternWithCoVillager(comingoutMap, patternScoreMapSubjective);
        patternScoreMap = patternProcess.removePatternWithAttackedWerewolf(killedAgents, patternScoreMap);
        patternScoreMapSubjective = patternProcess.removePatternWithAttackedWerewolf(killedAgents, patternScoreMapSubjective);
//       if (day == 3) { // ������������
//            Integer numAgents = gameInfo.getAgentList().size();
//            patternScoreMap = patternProcess.removePatternWithoutCoSeer(comingoutMap, numAgents, patternScoreMap);
//            patternScoreMapSubjective = patternProcess.removePatternWithoutCoSeer(comingoutMap, numAgents, patternScoreMapSubjective);
//        }

        // �i�����j�Ǖ����ꂽ�G�[�W�F���g��o�^
        addExecutedAgent(currentGameInfo.getLatestExecutedAgent());
        // GameInfo.talkList����J�~���O�A�E�g�E�肢�񍐁E��}�񍐂𒊏o
        for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
            Talk talk = currentGameInfo.getTalkList().get(i);
            Agent talker = talk.getAgent();
            if (talker == me) {
                continue;
            }
            Content content = new Content(talk.getText());
            switch (content.getTopic()) {
            case COMINGOUT:
                comingoutMap.put(talker, content.getRole());
                break;
            case DIVINED:
                divinationList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
                break;
            case IDENTIFIED:
                identList.add(new Judge(day, talker, content.getTarget(), content.getResult()));
                break;
            default:
                break;
            }
        }
        talkListHead = currentGameInfo.getTalkList().size();
    }

    public void dayStart() {
        canTalk = true;
        canWhisper = false;
        if (currentGameInfo.getRole() == Role.WEREWOLF) {
            canWhisper = true;
        }
        talkQueue.clear();
        whisperQueue.clear();
        declaredVoteCandidate = null;
        voteCandidate = null;
        declaredAttackVoteCandidate = null;
        attackVoteCandidate = null;
        talkListHead = 0;
        // �O���ɒǕ����ꂽ�G�[�W�F���g��o�^
        addExecutedAgent(currentGameInfo.getExecutedAgent());
        // ��鎀�S�����i�P�����ꂽ�j�G�[�W�F���g��o�^
        if (!currentGameInfo.getLastDeadAgentList().isEmpty()) {
            addKilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
        }
    }

    private void addExecutedAgent(Agent executedAgent) {
        if (executedAgent != null) {
            aliveOthers.remove(executedAgent);
            if (!executedAgents.contains(executedAgent)) {
                executedAgents.add(executedAgent);
            }
        }
    }

    private void addKilledAgent(Agent killedAgent) {
        if (killedAgent != null) {
            aliveOthers.remove(killedAgent);
            if (!killedAgents.contains(killedAgent)) {
                killedAgents.add(killedAgent);
            }
        }
    }

    /** ���[�����I��voteCandidate�ɃZ�b�g���� */
    protected void chooseVoteCandidate() {
    }

    public String talk() {
        chooseVoteCandidate();
        if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
            talkQueue.offer(new Content(new VoteContentBuilder(voteCandidate)));
            declaredVoteCandidate = voteCandidate;
        }
        return talkQueue.isEmpty() ? Talk.SKIP : talkQueue.poll().getText();
    }

    /** �P�������I��attackVoteCandidate�ɃZ�b�g���� */
    protected void chooseAttackVoteCandidate() {
    }

    public String whisper() {
        chooseAttackVoteCandidate();
        if (attackVoteCandidate != null && attackVoteCandidate != declaredAttackVoteCandidate) {
            whisperQueue.offer(new Content(new AttackContentBuilder(attackVoteCandidate)));
            declaredAttackVoteCandidate = attackVoteCandidate;
        }
        return whisperQueue.isEmpty() ? Talk.SKIP : whisperQueue.poll().getText();
    }

    public Agent vote() {
        canTalk = false;
        chooseVoteCandidate();
        return voteCandidate;
    }

    public Agent attack() {
        canWhisper = false;
        chooseAttackVoteCandidate();
        canWhisper = true;
        return attackVoteCandidate;
    }

    public Agent divine() {
        return null;
    }

    public Agent guard() {
        return null;
    }

    public void finish() {
    }

    private HashMap<HashMap<Integer, Role>, Double> patternScoreMap = new HashMap<>(); // �q�ϖڐ�
    private HashMap<HashMap<Integer, Role>, Double> patternScoreMapSubjective = new HashMap<>(); // ��ϖڐ�
    private Double aDouble = 0.0;
    private List<Role> q5RoleList = Arrays.asList(Role.VILLAGER, Role.VILLAGER, Role.SEER, Role.WEREWOLF, Role.POSSESSED);
    private List<Role> q15RoleList = Arrays.asList(Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.SEER, Role.MEDIUM, Role.BODYGUARD, Role.WEREWOLF, Role.WEREWOLF, Role.WEREWOLF, Role.POSSESSED);

    public void go(GameInfo gameInfo) {
        List<Role> ansRoleList = new ArrayList<>();
        permutation(q5RoleList, ansRoleList);
        // TODO: �֎~����Ă���t�@�C���o�͂͑��{�Ԃ܂łɏ���
        PatternProcessSubjective patternProcessSubjective = new PatternProcessSubjective();
        patternScoreMapSubjective = patternProcessSubjective.keepPatternWithMyCorrectRole(gameInfo, patternScoreMapSubjective);
    }

    private void permutation(List<Role> qRoleList, List<Role> ansRoleList) {
        if(qRoleList.size() <= 1) {
            List<Role> toOutPut = new ArrayList<>();
            toOutPut.addAll(ansRoleList);
            toOutPut.addAll(qRoleList);
            HashMap<Integer, Role> integerRoleHashMap = new HashMap<>();
            for (int k = 0; k < toOutPut.size(); k++) {
                integerRoleHashMap.put(k + 1, toOutPut.get(k));
            }
            this.patternScoreMap.put(integerRoleHashMap, this.aDouble);
            this.patternScoreMapSubjective.put(integerRoleHashMap, this.aDouble);
        }
        else {
            for (int i = 0; i < qRoleList.size(); i++) {
                // qRoleList�̐擪����i�ڂ܂ł̗v�f�S�Ă����Ԃɑ���������
                List<Role> first = new ArrayList<>();
                for (int j1 = 0; j1 < i; j1++) {
                    first.add(qRoleList.get(j1));
                }
                // i+1�ڂ܂ł̗v�f���폜����qRoleList
                List<Role> second = new ArrayList<>();
                for (int j2 = i + 1; j2 < qRoleList.size(); j2++) {
                    second.add(qRoleList.get(j2));
                }
                List<Role> qNext = new ArrayList<>();
                qNext.addAll(first);
                qNext.addAll(second);

                // ansRoleList�̕ʃC���X�^���X
                List<Role> third = new ArrayList<>();
                third.addAll(ansRoleList);
                // qRoleList�̐擪����i�Ԗڂ̗v�f
                List<Role> fourth = new ArrayList<>();
                fourth.add(qRoleList.get(i));
                List<Role> ansNext = new ArrayList<>();
                ansNext.addAll(third);
                ansNext.addAll(fourth);

                permutation(qNext, ansNext);
            }
        }
    }
}
