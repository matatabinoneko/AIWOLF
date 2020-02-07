package jp.gr.java_conf.otk.aiwolf.totj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;

/**
 * エージェント の確率モデル
 * 
 * @author otsuki
 *
 */
public class PModel {

	List<Role> roleList;

	// 各役職における，行動-頻度マップ
	Map<Role, Map<Action, Double>> countMap;
	// 各役職における，行動-対数確率マップ
	Map<Role, Map<Action, Double>> model;

	public PModel(GameSetting gameSetting) {
		countMap = new HashMap<Role, Map<Action, Double>>();
		model = new HashMap<Role, Map<Action, Double>>();
		roleList = new ArrayList<Role>();
		for (Role role : gameSetting.getRoleNumMap().keySet()) {
			if (gameSetting.getRoleNum(role) != 0) {
				roleList.add(role);
				countMap.put(role, new HashMap<Action, Double>());
				model.put(role, new HashMap<Action, Double>());
				for (Action action : Action.values()) {
					countMap.get(role).put(action, 1.0); // smoothing
					model.get(role).put(action, -Math.log10((double) Action.values().length));
				}
			}
		}
	}

	public void update(Role role, List<Action> actionList) {
		for (Action action : actionList) {
			incAction(role, action);
		}
		recalc();
	}

	void incAction(Role role, Action action) {
		countMap.get(role).put(action, countMap.get(role).get(action) + 1.0);
	}

	// 対数確率再計算
	void recalc() {
		for (Role role : roleList) {
			double count = 0;
			for (Action action : Action.values()) {
				count += countMap.get(role).get(action);
			}
			double logCount = Math.log10(count);
			for (Action action : Action.values()) {
				model.get(role).put(action, Math.log10(countMap.get(role).get(action)) - logCount);
			}
		}
	}

}
