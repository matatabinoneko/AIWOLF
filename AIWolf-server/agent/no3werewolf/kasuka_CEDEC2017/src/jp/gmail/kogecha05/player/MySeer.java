package jp.gmail.kogecha05.player;

import jp.gmail.kogecha05.player15.Village15Seer;
import jp.gmail.kogecha05.player5.Village5Seer;

public class MySeer extends MyBasePlayer {
	{
		player5 = new Village5Seer();
		player15 = new Village15Seer();
	}
}