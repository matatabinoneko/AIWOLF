package net.mchs_u.mc.aiwolf.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;

public class Probabilities implements Cloneable {
	private static final double EPS = 0.000000000000000000000001d;

	private boolean updated = false;
	private Map<RoleCombination, Double> probs = null;

	public Probabilities(List<Agent> agents, GameSetting gameSetting) {
		updated = true;
		probs = new HashMap<>();

		int w = gameSetting.getRoleNum(Role.WEREWOLF);
		int p = gameSetting.getRoleNum(Role.POSSESSED);
		for(List<Integer> ids: makeLoops(w + p, agents.size())){
			Set<Agent> wolves = new HashSet<>();
			for(int i = 0; i < w; i++)
				wolves.add(agents.get(ids.get(i)));

			Set<Agent> possesseds = new HashSet<>();
			for(int i = w; i < w + p; i++)
				possesseds.add(agents.get(ids.get(i)));

			RoleCombination rc = new RoleCombination(wolves, possesseds);
			if(rc.isValid(agents.size(), gameSetting))
				probs.put(rc, 1d);
		}
	}

	@Override
	public Probabilities clone() {
		Probabilities obj = null;
		try {
			obj = (Probabilities)super.clone();
			obj.updated = this.updated;
			obj.probs = new HashMap<>(this.probs.size());
			for(RoleCombination rc: this.probs.keySet())
				obj.probs.put(rc, this.probs.get(rc).doubleValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	private List<List<Integer>> makeLoops(int nFold, int num) {
		return makeLoops(new ArrayList<>(), nFold, num);
	}

	public static List<List<Integer>> makeLoops(List<Integer> list, int nFold, int num) {
		if(nFold <= 0) {
			List<List<Integer>> ans = new ArrayList<>();
			ans.add(list);
			return ans;
		}

		List<List<Integer>> loops = new ArrayList<>();
		for(int i = 0; i < num; i++) {
			List<Integer> lsub = new ArrayList<>(list);
			lsub.add(i);
			loops.addAll(makeLoops(lsub, nFold - 1, num));
		}
		return loops;
	}	

	public Set<RoleCombination> getRoleCombinations() {
		return probs.keySet();
	}

	public void init(RoleCombination rc, double rate) {
		probs.put(rc, rate);
		updated = true;
	}
	
	public void update(RoleCombination rc, double rate) {
		probs.put(rc, probs.get(rc) * rate);
		updated = true;
	}

	public void restore(RoleCombination rc, double rate) {
		probs.put(rc, probs.get(rc) / rate);
		updated = true;
	}

	public void remove(RoleCombination rc) {
		probs.remove(rc);
		updated = true;
	}

	public void removeZeros() {
		Set<RoleCombination> willRemove = new HashSet<>();
		for(RoleCombination rc: probs.keySet())
			if(probs.get(rc) < EPS)
				willRemove.add(rc);

		for(RoleCombination rc: willRemove)
			probs.remove(rc);
	}

	public boolean isUpdated() {
		return updated;
	}

	public void update() {
		updated = true;
	}

	public void resetUpdated() {
		updated = false;
	}

	public double getProbability(RoleCombination roleCombination){
		return probs.get(roleCombination);
	}

}
