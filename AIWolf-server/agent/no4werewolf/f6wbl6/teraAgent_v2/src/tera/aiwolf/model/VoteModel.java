package tera.aiwolf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.GameAgent;
import tera.aiwolf.framework.GameEvent;
import tera.aiwolf.framework.GameEventListenr;
import tera.aiwolf.framework.GameTalk;
import tera.aiwolf.util.HashCounter;

/**
 * 現在の投票状態を表現するモデル
 */
public class VoteModel implements GameEventListenr {

    /**
     * 現在の投票状況
     */
    private VoteStatus currentVote;
    private Game game;

    public VoteModel(Game game) {
        this.game = game;
        game.addGameEventListener(this);
    }

    public VoteStatus currentVote() {
        return currentVote;
    }

    public double[] voteScore() {
        HashCounter<GameAgent> count = currentVote.getVoteCount();
        double[] score = new double[game.getVillageSize()];
        for (GameAgent ag : count.getKeyList()) {
            score[ag.getIndex()] = count.getCount(ag);
        }
        return score;
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                e.talks.forEach(t -> {
                    int talkerInd = t.getTalker().getIndex();
                    GameAgent talker = t.getTalker();
                    switch (t.getTopic()) {
                        case AGREE:
                            GameTalk tgtTalk = t.getTargetTalk();
                            switch (tgtTalk.getTopic()) {
                                //投票へのAgree
                                case VOTE:
                                    currentVote.set(t.getTalker(), tgtTalk.getTarget());
                                    break;
                            }
                            break;
                        case VOTE:
                            //投票宣言
                            currentVote.set(t.getTalker(), t.getTarget());
                            break;
                    }
                });
                break;
            case DAYSTART:
                currentVote = new VoteStatus();
                break;
        }
    }

    public static class VoteStatus {

        public Map<GameAgent, GameAgent> whoVoteWhoMap = new HashMap<>();

        public void set(GameAgent voter, GameAgent target) {
            whoVoteWhoMap.put(voter, target);
        }

        public HashCounter<GameAgent> getVoteCount() {
            HashCounter<GameAgent> counter = new HashCounter<>();
            whoVoteWhoMap.values().forEach(ag -> counter.countPlus(ag));
            return counter;
        }

        public List<GameAgent> voterFor(GameAgent target) {
            List<GameAgent> list = new ArrayList<>();
            whoVoteWhoMap.forEach((from, to) -> {
                if (to == target) list.add(from);
            });
            return list;
        }

        public static class VoteGroup implements Comparable<VoteGroup> {

            public VoteGroup(GameAgent target, double evilScore) {
                this.target = target;
                this.score = evilScore;
            }

            @Override
            public String toString() {
                return "Vg{" + target + "=" + score + '}';
            }

            public GameAgent target;
            public double score;

            @Override
            public int compareTo(VoteGroup o) {
                return Double.compare(o.score, score);
            }

        }

    }

}
