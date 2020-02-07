package jp.gmail.kogecha05.player;

import jp.gmail.kogecha05.player15.Village15Bodyguard;
import jp.gmail.kogecha05.player5.Village5Bodyguard;

public class MyBodyguard extends MyBasePlayer {
	{
		player5 = new Village5Bodyguard();
		player15 = new Village15Bodyguard();
	}
}