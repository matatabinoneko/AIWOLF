package jp.gmail.kogecha05.player;

import jp.gmail.kogecha05.player15.Village15Villager;
import jp.gmail.kogecha05.player5.Village5Villager;

public class MyVillager extends MyBasePlayer {
	{
		player5 = new Village5Villager();
		player15 = new Village15Villager();
	}
}