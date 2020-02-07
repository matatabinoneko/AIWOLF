package local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.*;

public class Database {
	/** カミングアウト状況 */
	Map<Agent, Role> comingoutMap = new HashMap<>();
    List<RoleItem> roleEstimateList = new ArrayList<>();
    
    public Database(int playerMax) {
        initialize(playerMax);
    }
    
	/** エージェントがカミングアウトしたかどうかを返す */
	public boolean isCo(Agent agent) {
	    return comingoutMap.containsKey(agent);
	}
	
	/** 役職がカミングアウトされたかどうかを返す */
	public boolean isCo(Role role) {
	    return comingoutMap.containsValue(role);
	}
	
	/** エージェントがカミングアウトしている役職を返す */
	public Role getRole(Agent agent) {
	    return comingoutMap.get(agent);
	}
	
    public void updateItems(Talk talk) {
        Agent talker = talk.getAgent();
        Content content = new Content(talk.getText());
        switch (content.getTopic()) {
        case AGREE:
            break;
        case COMINGOUT:
            comingoutMap.put(talker, content.getRole());
            break;
        case DISAGREE:
            break;
        case DIVINATION:
            break;
        case DIVINED:
            break;
        case ESTIMATE:
            break;
        case GUARD:
            break;
        case GUARDED:
            break;
        case IDENTIFIED:
            break;
        case VOTE:
            break;
        default:
            break;
        }

    }
    
    private void initialize(int playerMax) {
	    comingoutMap.clear();
        roleEstimateList.clear();
        for (int werewolf1 = 0; werewolf1 < playerMax; werewolf1++) {
            for (int werewolf2 = werewolf1; werewolf2 < playerMax; werewolf2++) {
                for (int werewolf3 = werewolf2; werewolf3 < playerMax; werewolf3++) {
                    for (int possessed = 0; possessed < playerMax; possessed++) {
                        if ((possessed == werewolf1) ||
                            (possessed == werewolf2) ||
                            (possessed == werewolf3)) {
                            continue;
                        }
                        roleEstimateList.add(new RoleItem(werewolf1, werewolf2, werewolf3, possessed));
                    }
                }
            }
        }

    }
    
    private class RoleItem {
        int werewolf1;
        int werewolf2;
        int werewolf3;
        int possessed;
        boolean isPossible;

        public RoleItem(int werewolf1, int werewolf2, int werewolf3, int possessed) {
            this.werewolf1 = werewolf1;
            this.werewolf2 = werewolf2;
            this.werewolf3 = werewolf3;
            this.possessed = possessed;
            isPossible = true;
        }
        
        private boolean contain(int agentIndex) {
            if (containWerewolf(agentIndex) ||
                containPossessed(agentIndex)) {
                return true;
            }
            return false;
        }

        private boolean containWerewolf(int agentIndex) {
            if ((agentIndex == werewolf1) ||
                (agentIndex == werewolf2) ||
                (agentIndex == werewolf3)) {
                return true;
            }
            return false;
        }

        private boolean containPossessed(int agentIndex) {
            if (agentIndex == possessed) {
                return true;
            }
            return false;
        }
    }
}
