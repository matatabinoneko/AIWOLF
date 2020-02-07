package jp.gmail.kogecha05.player;

import jp.gmail.kogecha05.player15.Village15Werewolf;
import jp.gmail.kogecha05.player5.Village5Werewolf;

public class MyWerewolf extends MyBasePlayer {
	{
		player5 = new Village5Werewolf();
		player15 = new Village15Werewolf();
	}
}