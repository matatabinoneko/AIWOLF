package jp.gmail.kogecha05.player;

import jp.gmail.kogecha05.player15.Village15Possessed;
import jp.gmail.kogecha05.player5.Village5Possessed;

public class MyPossessed extends MyBasePlayer {
	{
		player5 = new Village5Possessed();
		player15 = new Village15Possessed();
	}
}