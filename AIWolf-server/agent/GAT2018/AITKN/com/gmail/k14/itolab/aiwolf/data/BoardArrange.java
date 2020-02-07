package com.gmail.k14.itolab.aiwolf.data;

import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;

import com.gmail.k14.itolab.aiwolf.definition.Board;
import com.gmail.k14.itolab.aiwolf.definition.RoleId;

/**
 * 盤面整理クラス<br>
 * 盤面状態，生存役職，人数，日付で管理する
 * @author k14096kk
 *
 */
public class BoardArrange {

	/**盤面*/
	 Board board;
	
	/**
	 * ビットの生存役職一覧<br>
	 * 1:村人，2:占い師，4:霊媒師，8:狩人，16:狂人，32:人狼
	 */
	 int place;
	
	/**生存人数*/
	 int people;
	
	/**日付*/
	 int day;
	 
	 
	/**
	 * ゲーム開始時の盤面設定
	 * @param gameSetting :ゲーム設定
	 */
	public BoardArrange(GameSetting gameSetting) {
		// 盤面はSTART
		board = Board.START;
		// 役職が存在していれば1を立てる
		place = 0;
		for(Role role: gameSetting.getRoleNumMap().keySet()) {
			if(gameSetting.getRoleNumMap().get(role)>0) {
				switch (role) {
				case VILLAGER:
					place = place | RoleId.VILLAGER_ID.getId();
					break;
				case SEER:
					place = place | RoleId.SEER_ID.getId();
					break;
				case MEDIUM:
					place = place | RoleId.MEDIUM_ID.getId();
					break;
				case BODYGUARD:
					place = place | RoleId.BODYGUARD_ID.getId();
					break;
				case POSSESSED:
					place = place | RoleId.POSSESSED_ID.getId();
					break;
				case WEREWOLF:
					place = place | RoleId.WEREWOLF_ID.getId();
					break;
				default:
					break;
				}
			}
		}
		// 生存人数は参加人数
		people = gameSetting.getPlayerNum();
		// 日付は0日目
		day = 0;
	} 
	
	/**
	 * 設定中の盤面取得
	 * @return 盤面
	 */
	public  Board getBoard() {
		return board;
	}
	
	/**
	 * 設定中の役職ビット取得
	 * @return 役職ビット
	 */
	public  int getPlace() {
		return place;
	}
	
	/**
	 * 設定中の人数取得
	 * @return 人数
	 */
	public  int getPeople() {
		return people;
	}
	
	/**
	 * 設定中の日付取得
	 * @return 日付 
	 */
	public  int getDay() {
		return day;
	}
	
	/**
	 * 盤面情報を更新
	 * @param forecastMap :予想一覧
	 * @param aliveList :生存エージェントリスト
	 * @param today :日付
	 */
	public  void update(ForecastMap forecastMap, List<Agent> aliveList, int today) {
		// 役職ビット初期化
		place = 0;
		// 現在の暫定役職から役職ビット再構成
		for(Role role: forecastMap.getProvRoleAliveNumMap().keySet()) {
			if(forecastMap.getProvRoleAliveNumMap().get(role)>0) {
				switch (role) {
				case VILLAGER:
					place = place | RoleId.VILLAGER_ID.getId();
					break;
				case SEER:
					place = place | RoleId.SEER_ID.getId();
					break;
				case MEDIUM:
					place = place | RoleId.MEDIUM_ID.getId();
					break;
				case BODYGUARD:
					place = place | RoleId.BODYGUARD_ID.getId();
					break;
				case POSSESSED:
					place = place | RoleId.POSSESSED_ID.getId();
					break;
				case WEREWOLF:
					place = place | RoleId.WEREWOLF_ID.getId();
					break;
				default:
					break;
				}
			}
		}
		// 人数更新
		people = aliveList.size();
		// 日付更新
		day = today; 
	}
	
	/**
	 * 村人が生存しているかどうか
	 * @return 生存ならばtrue，死亡ならばfalse
	 */
	public  boolean isVillager() {
		int bit = place & RoleId.VILLAGER_ID.getId();
		if(bit==1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 占い師が生存しているかどうか
	 * @return 生存ならばtrue，死亡ならばfalse
	 */
	public  boolean isSeer() {
		int bit = place & RoleId.SEER_ID.getId();
		if(bit==2) {
			return true;
		}
		return false;
	}
	
	/**
	 * 霊媒師が生存しているかどうか
	 * @return 生存ならばtrue，死亡ならばfalse
	 */
	public  boolean isMedium() {
		int bit = place & RoleId.MEDIUM_ID.getId();
		if(bit==4) {
			return true;
		}
		return false;
	}
	
	/**
	 * 狩人が生存しているかどうか
	 * @return 生存ならばtrue，死亡ならばfalse
	 */
	public  boolean isBodyguaed() {
		int bit = place & RoleId.BODYGUARD_ID.getId();
		if(bit==8) {
			return true;
		}
		return false;
	}
	
	/**
	 * 狂人が生存しているかどうか
	 * @return 生存ならばtrue，死亡ならばfalse
	 */
	public  boolean isPossessed() {
		int bit = place & RoleId.POSSESSED_ID.getId();
		if(bit==16) {
			return true;
		}
		return false;
	}
	
	/**
	 * 人狼が生存しているかどうか
	 * @return 生存ならばtrue，死亡ならばfalse
	 */
	public  boolean isWerewolf() {
		int bit = place & RoleId.WEREWOLF_ID.getId();
		if(bit==32) {
			return true;
		}
		return false;
	}
	
	/**
	 * 盤面整理(BoardOrganizeから移植)<br>
	 * 村，占，霊，狩，狂，狼それぞれが生存しているかどうか<br>
	 * 生存していれば1,生存していなければfalse
	 * @param num :1以上(生存)or0(死亡)
	 * @return 満たしていればtrue,不足ならばfalse
	 */
	public boolean meet(int... num) {
		// 配列の長さが6以上ならば村人も指定していると判断
		if(num.length>=6) {
			// 村人がいるかどうか
			if(num[0] >= 1) {
				if(!isVillager()) {
					return false;
				}
			}
		}
		// 占い師がいるかどうか
		if(num[num.length-5] >= 1) {
			if(!isSeer()) {
				return false;
			}
		}
		// 霊媒師がいるかどうか
		if(num[num.length-4] >= 1) {
			if(!isMedium()) {
				return false;
			}
		}
		// 狩人がいるかどうか
		if(num[num.length-3] >= 1) {
			if(!isBodyguaed()) {
				return false;
			}
		}
		// 狂人がいるかどうか
		if(num[num.length-2] >= 1) {
			if(!isPossessed()) {
				return false;
			}
		}
		// 人狼がいるかどうか
		if(num[num.length-1] >= 1) {
			if(!isWerewolf()) {
				return false;
			}
		}
		// 全部満たしていれば真
		return true;
	}

}
