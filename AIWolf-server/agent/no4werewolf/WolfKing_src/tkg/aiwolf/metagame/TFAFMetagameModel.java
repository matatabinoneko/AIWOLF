package tkg.aiwolf.metagame;

import jp.ne.sakura.vopaldragon.aiwolf.framework.MetagameModel;

/**
 * cedec2017用のメタゲームモデル
 */
public class TFAFMetagameModel extends MetagameModel {

    public ActFrequencyModel actFrequencyModel;
    public TalkFrequencyModel talkFrequencyModel;
    public WinCountModel winCountModel;

    public TFAFMetagameModel() {
        actFrequencyModel = new ActFrequencyModel();
        addMetagameEventListener(actFrequencyModel);
        talkFrequencyModel = new TalkFrequencyModel();
        addMetagameEventListener(talkFrequencyModel);
        winCountModel = new WinCountModel();
        addMetagameEventListener(winCountModel);
    }

}
