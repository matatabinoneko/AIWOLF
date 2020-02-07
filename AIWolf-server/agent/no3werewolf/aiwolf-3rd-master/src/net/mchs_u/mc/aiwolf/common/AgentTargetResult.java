package net.mchs_u.mc.aiwolf.common;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

public class AgentTargetResult {
	private Agent agent = null;
	private Agent target = null;
	private Species result = null;
	
	public AgentTargetResult(Agent agent, Agent target, Species result) {
		this.agent = agent;
		this.target = target;
		this.result = result;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentTargetResult other = (AgentTargetResult) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (result != other.result)
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
	
	public Agent getAgent() {
		return agent;
	}
	
	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	public Agent getTarget() {
		return target;
	}
	
	public void setTarget(Agent target) {
		this.target = target;
	}
	
	public Species getResult() {
		return result;
	}
	
	public void setResult(Species result) {
		this.result = result;
	}	
}
