package com.gmail.k14.itolab.aiwolf.old;

import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;
import com.gmail.k14.itolab.aiwolf.data.MyTalking;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;
import com.gmail.k14.itolab.aiwolf.util.TalkSelect;

/**
 * 人狼の行動(15人)
 * @author k14096kk
 *
 */
public class WerewolfMaxAction extends BaseRoleAction {
	
	public MyTalking myWhisper;

	public WerewolfMaxAction(EntityData entityData) {
		super(entityData);
		myWhisper = entityData.getMyWhisper();
		setEntityData();
	}

	@Override
	public void setEntityData() {
		super.setEntityData();
		entityData.setMyWhisper(myWhisper);
	}
	
	@Override
	public void dayStart() {
		super.dayStart();
		setEntityData();
	}
	
	@Override
	public void selectAction() {
		super.selectAction();
		setEntityData();
	}
	
	@Override
	public void selectWhisperAction() {
		super.selectWhisperAction();
		//myWhisper.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
		this.normalNightAction();
		setEntityData();
	}
	
	@Override
	public void talkAction(Talk talk, Content content) {
		super.talkAction(talk, content);
		setEntityData();
	}
	
	@Override
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
	/**
	 * 通常行動
	 */
	public void normalAction() {
		
	}
	
	/**
	 * 夜の通常行動(囁き中の行動)
	 */
	public void normalNightAction() {
		/*0日目*/
		if(ownData.currentDay(0)) {
			// 0ターン目
			if(turn.startTurn()) {
				// 占い師CO
				myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), Role.SEER));
			}
			// 1ターン目
			if(turn.currentTurn(1)) {
				
			}
			// 2ターン目
			if(turn.currentTurn(2)) {
				List<Talk> disagreeList = TalkSelect.topicList(ownData.getWhisperList(), Topic.DISAGREE);
				if(!disagreeList.isEmpty()) {
					
				}
			}
		}
	}

}
