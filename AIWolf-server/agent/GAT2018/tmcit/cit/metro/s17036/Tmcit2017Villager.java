package cit.metro.s17036;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.sample.lib.AbstractVillager;

public class Tmcit2017Villager extends AbstractVillager {

	/* すごく適当
	 *
	 * */


	Agent me;
	GameInfo currentGameInfo;
	int talkListHead;
	VillagerLikeSelect brain;

	boolean isCO;

	@Override
	public void dayStart() {
		talkListHead = 0;
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public String getName() {
		return "tmcit2017";
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		me=gameInfo.getAgent();
		brain = new VillagerLikeSelect(me);
		talkListHead = 0;
		isCO = false;
	}

	@Override
	public String talk() {
		if(!isCO){
			isCO = true;
			ContentBuilder builder = new ComingoutContentBuilder(me,Role.VILLAGER);
			return new Content(builder).getText();
		}

		return Talk.OVER;
	}

	@Override
	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
	}

	@Override
	public Agent vote() {
		return brain.predict(currentGameInfo);
	}
}
