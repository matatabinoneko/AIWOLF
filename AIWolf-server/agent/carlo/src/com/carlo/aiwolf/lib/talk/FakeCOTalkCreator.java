package com.carlo.aiwolf.lib.talk;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;

import com.carlo.aiwolf.base.lib.MyAbstractRole;

/**
 *  偽COをしてくれるCOTalkCreater <br>
 *  
 *  dayStart(List<Judge> myJudgeList) では偽の占いor霊能結果のリストを与える <br>
 *  COは１回のみ。撤回等は出来ない。
 * @author info
 *
 */
public class FakeCOTalkCreator extends COTalkCreator{
	/** 偽CO用の役職 */
	protected Role fakeRole=null;

	public FakeCOTalkCreator(MyAbstractRole myRole) {
		super(myRole);
	}
	/**
	 *  占い師と霊能者騙り用のdayStart<br>
	 *  CO時に発言するJudgeのリストを設定する <br>
	 *  互換性確保のために置いてある
	 * @param myJudgeList 占いもしくは霊能の結果リスト
	 */
	public void dayStart(List<Judge> judgeList){
		super.dayStart();
		this.myJudgeList=new ArrayList<Judge>(judgeList);
	}
	/**
	 * 騙る予定の役職を設定する。comingOut後は変更できない
	 * @param fakeRole
	 * @return 設定(変更)できたらtrue
	 */
	public boolean setFakeRole(Role fakeRole){
		if(isComingOut==false && this.fakeRole!=fakeRole){
			this.fakeRole=fakeRole;
			return true;
		}
		else return false;
	}
	
	@Override
	/**　fakeRoleを返す */
	public Role getComingoutRole(){
		return fakeRole;
	}

}
