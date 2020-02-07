package jp.or.plala.amail.rin0114.aiwolf.metagame;

import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.MetagameModel;

/**
 * GAT2018人狼知能プレ大会用のメタゲームモデル
 */
public class TFAFMetagameModel extends MetagameModel {

	private Game game;
	public ActFrequencyModel actFrequencyModel;
	public TalkFrequencyModel talkFrequencyModel;
	public TalkFrequencyModel2gram talkFrequencyModel2gram;
	public WinCountModel winCountModel;

	public TFAFMetagameModel() {
		actFrequencyModel = new ActFrequencyModel();
		addMetagameEventListener(actFrequencyModel);
		// if(game.getVillageSize() == 5){
		// talkFrequencyModel = new TalkFrequencyModel();
		// addMetagameEventListener(talkFrequencyModel);
		// }else{
		talkFrequencyModel2gram = new TalkFrequencyModel2gram();
		addMetagameEventListener(talkFrequencyModel2gram);
		// }
		winCountModel = new WinCountModel();
		addMetagameEventListener(winCountModel);
	}

}
