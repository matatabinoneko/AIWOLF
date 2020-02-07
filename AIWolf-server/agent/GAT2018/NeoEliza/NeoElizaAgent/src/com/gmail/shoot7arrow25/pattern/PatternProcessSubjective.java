package com.gmail.shoot7arrow25.pattern;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatternProcessSubjective {
    // åŠÏî•ñ(©•ª‚ª³‚µ‚¢Role‚Æ‚È‚Á‚Ä‚¢‚éƒpƒ^[ƒ“‚¾‚¯•Û)
    public HashMap<HashMap<Integer, Role>, Double> keepPatternWithMyCorrectRole(GameInfo gameInfo, HashMap<HashMap<Integer, Role>, Double> patternScoreList) {
        List<HashMap<Integer, Role>> patternToDelete = new ArrayList<>();
        for (HashMap.Entry<HashMap<Integer, Role>, Double> entry : patternScoreList.entrySet()) {
            HashMap<Integer, Role> pattern = entry.getKey();
            Agent me = gameInfo.getAgent();
            Integer myIdx = me.getAgentIdx();
            Role myRole = gameInfo.getRole();
            if (pattern.get(myIdx) != myRole) {
                patternToDelete.add(pattern);
            }
        }
        PatternProcess patternProcess = new PatternProcess();
        for (HashMap<Integer, Role> pattern : patternToDelete) {
            patternScoreList = patternProcess.removePatternFromPossiblePatterns(pattern, patternScoreList);
        }
        return patternScoreList;
    }
}
