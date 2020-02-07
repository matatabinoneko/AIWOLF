package com.gmail.k14.itolab.aiwolf.old;


import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Talk;

import com.gmail.k14.itolab.aiwolf.base.BaseRoleAction;
import com.gmail.k14.itolab.aiwolf.data.EntityData;

/**
 * 狩人の行動(5人)
 * @author k14096kk
 *
 */
public class BodyguardAction extends BaseRoleAction{

	public BodyguardAction(EntityData entityData) {
		super(entityData);
		setEntityData();
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
	public void requestAction(Talk talk, Content content, Content reqContent) {
		super.requestAction(talk, content, reqContent);
		setEntityData();
	}
	
}
