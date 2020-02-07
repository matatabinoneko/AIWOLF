package jp.ac.aitech.k15029kk.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.ac.aitech.k15029kk.GameResult;
import jp.ac.aitech.k15029kk.TalkFactory;
import jp.ac.aitech.k15029kk.util.Debug;
import jp.ac.aitech.k15029kk.util.RandomSelect;

public class DemoRole implements Player {

	/**----グローバル変数-----*/
	/**-------ここに実装する変数はprotectedをつけてください．つけるとこのクラスを継承したクラスでも使えるようになります--------*/

	protected GameInfo gameInfo;
	/*各エージェントとそのデータ管理*/
	protected Map<Agent, AgentData> AgentDataMap;
	/*占った相手と結果*/
	protected Map<Agent, Species> DivineMap;
	/*霊媒した相手と結果*/
	protected Map<Agent, Species> IdentMap;
	/*SeerCOしたエージェントとタイミングのマップ*/
	protected Map<Agent, Integer> SeerCoTimingMap;
	/*1turnにつき1talkのためのフラグマップ*/
	protected Map<Integer, Boolean> TurnFlagMap;

	protected Agent me;
	protected Role myRole;
	protected boolean isPassFinish;
	protected GameResult gameResult;
	protected Deque<String> talkQueue;
	protected int readTalkNum = 0;
	protected int turn;
	/*turnが変わるごとにreset*/
	protected int myVoteCountByTurn = 0;
	/*日付保持(daystartが複数回らないため)*/
	protected int date = 0;
	/*myVoteCountByTurnのVOTEしてきたエージェント(turnが変わるごとにreset)*/
	protected Agent agentVotingMe = null;
	protected Agent Possessed;
	protected Agent doutWerewolf;
	protected Agent doutMedium;
	protected Agent Seer;
	protected Agent Medium;
	/*潜伏人狼(myRole = SEER)*/
	protected Agent hidingWerewolf;
	/*DIVされた潜伏人狼(myRole = WEREWOLF)*/
	protected Agent divinedHidingWolf;
	/*追放された人*/
	protected Agent outcast;
	/*狩られた人*/
	protected Agent AttackedAgent;
	/*esWOLFしたエージェント(死んだらその都度変える)*/
	protected Agent esWolfAgent;
	/*後々PPするための(狂人)エージェント(5人)*/
	protected Agent PPAgent;
	/*後々PPできない(占い師)エージェント(5人)*/
	protected Agent notPPAgent;

	protected Agent voteAgent;
	protected Agent highestVotedAgent;
	/*Bodygardが守る仮の占い師と定めたエージェント*/
	protected Agent ProvisionalSeer;
	/*人狼確定エージェントのリスト*/
	protected List<Agent> WerewolfList;
	/*潜伏人狼リスト(myRole = WEREWOLF)*/
	protected List<Agent> HidingWolfList;
	/*人狼なしgrayList(myRole = WEREWOLF)*/
	protected List<Agent> grayListWithoutWolf;
	/*狂人か人狼だと思う人のList*/
	protected List<Agent> blackList;
	/*自分以外の生きてるエージェントリスト*/
	protected List<Agent> AliveAgentList;
	/*attackするエージェントを決めるためのList*/
	protected List<Agent> AttackList;
	/*自分とつながりのあるエージェントリスト(占い師or霊媒師)*/
	protected List<Agent> myConnectionList;
	/*本物の占い師が見つかった時，偽占い師のConectionList*/
	protected List<Agent> wolfConectionList;
	/*偽占い師にDIVされた人(myRole == SEER or MEDIUM)*/
	protected List<Agent> provisionalWhiteList;
	/*潜伏人狼の可能性があるエージェントリスト*/
	protected List<Agent> maybeHidingWolfList;
	/*AgentDataをそれぞれのエージェントに用意するためのカウント*/
	int agentcount;

	/**COList*/
	protected List<Agent> MediumList;
	protected List<Agent> SeerList;
	protected List<Agent> grayList;


	/**フラグ管理
	 * ESフラグはDemoRoleで管理
	 * COフラグは各役職クラスで管理
	*/
	protected boolean isCO;
	protected boolean turn0Flag;
	protected boolean esPossessed;
	protected boolean esSeer;
	protected boolean esWolf;
	protected boolean esVillager;
	protected boolean esMedium;
	protected boolean VillagerCO;
	protected boolean SeerCO;
	protected boolean MediumCO;
	protected boolean BodygardCO;
	protected boolean WolfCO;
	protected boolean PossessedCO;
	protected boolean divineRemark;
	protected boolean identRemark;
	protected boolean hidingWerewolfDied;
	/*SeerListとMediumListの整理がついた時のフラグ*/
	protected boolean SeerMediumArranged;
	/*誰かがOVERしたらフラグを立てる*/
	protected boolean OverFlag;
	/*狂人時の動作決定フラグ(myRole = POSSESSED)*/
	protected boolean seerMovement = false;
	protected boolean mediumMovement = false;


	protected Integer wolfCount;
	protected Integer voteTalkCount;




	//コンストラクタ
	public DemoRole(GameResult gameResult) {
		this.gameResult = gameResult;
	}



	public String getName() {
		return null;
	}

	//エージェントが生きているかどうかを返す
	public boolean isAlive(Agent agent) {
		return gameInfo.getAliveAgentList().contains(agent);
	}

	// リストからランダムに選んで返す
	protected <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	/**
	 * 指定のリストに指定のエージェントがいるかどうかを返すメソッド
	 * @param agent
	 * @param list
	 * @return
	 */


	//日付を取得
	protected Integer getDate() {
		return gameInfo.getDay();
	}



	/*--------ここからは絶対実装するメソッド．人狼知能エージェントが実装しないといけないメソッド---------*/
	public void initialize(GameInfo arg0, GameSetting arg1) {
		try {
			this.gameInfo = arg0;
			this.date = 0;
			this.isPassFinish = false;
			this.gameResult.init(arg0);;
			this.readTalkNum = 0;
			this.turn = 0;
			this.myVoteCountByTurn = 0;
			this.talkQueue = new LinkedList<>();
			agentcount = 0;
			this.voteTalkCount = 0;
			me = gameInfo.getAgent();
			myRole = gameInfo.getRole();

			/*フラグの初期化**/
			this.isCO = false;
			this.turn0Flag = false;
			this.esPossessed = false;
			this.esSeer = false;
			this.esWolf = false;
			this.esVillager = false;
			this.esMedium = false;
			this.VillagerCO = false;
			this.SeerCO = false;
			this.MediumCO = false;
			this.BodygardCO = false;
			this.WolfCO = false;
			this.PossessedCO = false;
			this.divineRemark =false;
			this.identRemark = false;
			this.hidingWerewolfDied = false;
			this.SeerMediumArranged = false;
			this.seerMovement = false;
			this.mediumMovement = false;

			/*Agent型の初期化**/
			this.agentVotingMe = null;
			this.Possessed = null;
			this.doutWerewolf = null;
			this.doutMedium = null;
			this.voteAgent = null;
			this.highestVotedAgent = null;
			this.ProvisionalSeer = null;
			this.Seer = null;
			this.Medium = null;
			this.outcast = null;
			this.AttackedAgent = null;
			this.hidingWerewolf = null;
			this.divinedHidingWolf = null;
			this.esWolfAgent = null;
			this.notPPAgent = null;
			this.PPAgent = null;

			/*Mapの初期化*/
			this.AgentDataMap = new HashMap<>();
			this.DivineMap = new HashMap<>();
			this.IdentMap = new HashMap<>();
			this.SeerCoTimingMap = new HashMap<>();
			this.TurnFlagMap = new HashMap<>();

			/*Listの初期化**/
			SeerList = new ArrayList<>();
			MediumList = new ArrayList<>();
			grayList = new ArrayList<>();
			blackList = new ArrayList<>();
			WerewolfList = new ArrayList<>();
			HidingWolfList = new ArrayList<>();
			grayListWithoutWolf = new ArrayList<>();
			AliveAgentList = new ArrayList<>();
			AttackList = new ArrayList<>();
			myConnectionList = new ArrayList<>();
			wolfConectionList = new ArrayList<>();
			provisionalWhiteList = new ArrayList<>();
			maybeHidingWolfList = new ArrayList<>();

			/*Integerの初期化*/
			wolfCount=3;

			/**TurnFlagの初期化*/
			for(int i=0; i<50; i++) {
				TurnFlagMap.put(i,false);
			}


			//各エージェント(自分以外の)のAgentDataを作成
			for(Agent agent: gameInfo.getAliveAgentList()) {
				if(agent != me) {
					AgentData agentData = new AgentData(gameInfo, agent);
					AgentDataMap.put(agent, agentData);
				}
			}

			//各AgentDataを日付ごとに更新
			for(Agent agent: AgentDataMap.keySet()) {
				AgentDataMap.get(agent).dayStart();
			}

			//味方人狼をリストへ(myRole = WEREWOLF)
			for(Agent agent: gameInfo.getRoleMap().keySet()) {
				if(gameInfo.getRoleMap().get(agent) == Role.WEREWOLF) {
					WerewolfList.add(agent);
					HidingWolfList.add(agent);
				}
			}

			//狂人時の動作決定
			double tmp = Math.random();
			int tmp2 = (int)(tmp);
			if(tmp2%2==1) {
				seerMovement = true;
			}
			else {
				mediumMovement = true;
			}

			/**毎日生きているエージェントにgrayListを更新*/
			for(Agent agent: gameInfo.getAliveAgentList()) {
				if(agent != me) {
					grayList.add(agent);
				}
			}


		} catch (Exception e) {
			this.writeEx(e);
		}

	}

	public void dayStart() {
		try {
			//dateよりgetDate()のほいが数が多い場合は日付が変わった時
			if(date == getDate()) {
				return;
			}
			//日付更新
			this.date = getDate();
			this.turn = 0;
			this.turn0Flag = false;
			this.voteTalkCount = 0;
			this.talkQueue.clear();
			//もっともVOTE数が多いエージェントは毎日リセット
			this.highestVotedAgent = null;
			//1日の中でdiviedRemarkしてあるかどうか
			this.divineRemark = false;
			//1日の中でidentRemarkしてあるかどうか
			this.identRemark = false;
			voteAgent = null;
			this.readTalkNum=0;

			/*ESフラグを初期化*/
			this.esPossessed = false;
			this.esSeer = false;
			this.esWolf = false;
			this.esVillager = false;

			/**TurnFlagの初期化*/
			for(int i=0; i<50; i++) {
				TurnFlagMap.put(i,false);
			}

			/**
			 * 5人と15人での役職ごとのdayStart動作
			 */
			if(gameInfo.getAgentList().size()==5) {
				dayStartAction();
			}
			else {
				dayStartAction15();
			}

			/**前日に釣られたエージェント
			 *・outcast : 前日追放された人
			*/
			List<Vote> list = gameInfo.getVoteList();
			List<Agent> list2 = new ArrayList<>();
			Map<Agent, Integer> map = new HashMap<>();
			int count=1;
			int tmp=0;
			for(int i=0; i<list.size(); i++) {
				if(!list2.contains(list.get(i).getTarget())) {
					list2.add(list.get(i).getTarget());
					map.put(list.get(i).getTarget(), count);
				}
				else {
					count = map.get(list.get(i).getTarget())+1;
					map.put(list.get(i).getTarget(), count);
				}
			}
			for(Agent agent: map.keySet()) {
				if(tmp<map.get(agent)) {
					tmp=map.get(agent);
					//前日追放された人
					outcast = agent;
				}
			}

			//esWolfAgentが追放されたらesWolfし直し
			if(outcast == esWolfAgent) {
				esWolf=false;
			}


			/**前日狩られたエージェント
			 * ・AliveAgentList : 前日の生きていたエージェントリスト
			*/
			for(Agent agent: AliveAgentList) {
				if(!isAlive(agent) && agent!=outcast) {
					AttackedAgent = agent;
				}
			}

			/**AliveAgentListは毎日更新*/
			AliveAgentList = new ArrayList<>();
			AliveAgentList = getAliveOthersList();


			/**毎日生きているエージェントにgrayListを更新*/
			grayList.removeAll(getDeadAgentList());

			/**SeerListは死んでも更新しない*/

			/**MediumListは死んでも更新しない*/

			/**毎日brackList(生きてる)を更新*/
			blackList.removeAll(getDeadAgentList());

			/**毎日WerewolfList(生きてる)を更新*/
			WerewolfList.removeAll(getDeadAgentList());

			/**毎日provitionalWhiteList(生きている)を更新*/
			provisionalWhiteList.removeAll(getDeadAgentList());

			/**潜伏人狼の可能性がある人リスト作成(故意にVOTEをしていないエージェントが3人以下)*/
			for(Agent agent: getAliveOthersList()) {
				if(AgentDataMap.get(agent).neverVoteAgentList.size()<=3) {
					maybeHidingWolfList.add(agent);
				}
			}

			/**毎日votecountはリセット*/
			for(Agent agent: getAliveOthersList()) {
				AgentDataMap.get(agent).resetVotedData();
			}

		} catch (Exception e) {
			this.writeEx(e);
		}

	}


	@Override
	public void update(GameInfo arg0) {
		try {
			if(gameInfo.getAgentList().size()==5) {
				update5(arg0);
			}
			else {
				update15(arg0);
			}

		} catch (Exception e) {
			this.writeEx(e);
		}

	}

	public String talk() {
		try {
			// ここでtalkActionを呼び出す
			talkAction();
			turn++;
			myVoteCountByTurn=0;
			agentVotingMe = null;
			if(talkQueue.isEmpty()) {
				return null;
			}
			return talkQueue.poll();

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}


	@Override
	public Agent vote() {
		try {
			return voteAgent;

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}



	public Agent attack() {
		try {
			return randomSelect(getAliveOthersList());

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}




//占うAgentをかえす
	public Agent divine() {
		try {
			if(!grayList.isEmpty()) {
				//占う相手をランダム選択，占った相手はリストから消去
				Agent agent = RandomSelect.get(grayList);
				grayList.remove(agent);

				return agent;
			}

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}


	public void finish() {
		try {
			// finish通過確認
			// なぜかfinishメソッドが2回呼ばれるため，2度処理実行するのを回避
			if(!isPassFinish) {
				isPassFinish = true;
				return;
			}

			// ゲーム結果更新
			gameResult.updateData(gameInfo);
			gameResult.show();

		} catch (Exception e) {
			this.writeEx(e);
		}

		//finishだから最後に溜まったエラー文をまとめて表示
		Debug.showError();

	}




	public Agent guard() {
		try {
			//仮の占い師を定めて守る
			ProvisionalSeer = randomSelect(SeerList);
			return ProvisionalSeer;

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}





	@Override
	public String whisper() {
		try {
			if(mediumMovement) {
				talkQueue.offer(TalkFactory.comingoutRemark(me,Role.MEDIUM));
			}

		} catch (Exception e) {
			this.writeEx(e);
		}
		return null;
	}

	/*---------ここは自分で実装するメソッドではあるがとくに役職ごとにいじる必要はない--------*/

	/**
	 * エラー文記述
	 *
	 *  e:エラー
	 */
	public void writeEx(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		Debug.stackError(stringWriter);;
	}

	/*---------5人と15人それぞれのupdateの動作--------------------------*/

	/**
	 * 5人と15人それぞれの全体のupdateの動作
	 * @param talk
	 * @param content
	 */
	public void update5(GameInfo arg0) {
		try {
			this.gameInfo = arg0;
			voteAgent=null;
			int readcount = 0 ;

			if(turn==0 && !turn0Flag && getDate()!=0) {
				//1日目だけは指定
				if(getDate()==1) {
					date=1;
				}
				turn0Flag=true;
				turn0Action();

			}

			List<Talk> talkList = this.gameInfo.getTalkList();

			for(int i=readTalkNum; i < talkList.size(); i++ ) {
				Talk talk = talkList.get(i);
				Content content = new Content(talk.getText());


				/*CO発言時の動作*/
				if(content.getTopic() == Topic.COMINGOUT) {
					//占い師COの人をリストに登録(自分も含む)
					if(content.getRole() == Role.SEER) {
						SeerList.add(talk.getAgent());
					}

					if(talk.getAgent() == me) {
						isCO = true;
						if(content.getRole() == Role.SEER) {
							SeerCO = true;
						}
						else if(content.getRole() == Role.VILLAGER) {
							VillagerCO = true;
						}
						else if(content.getRole() == Role.POSSESSED) {
							PossessedCO = true;
						}
						else if(content.getRole() == Role.WEREWOLF) {
							WolfCO = true;
						}
					}
					//自分以外がCOした場合
					else{
						if(content.getRole()==Role.SEER) {
							//1日目のSeerCOのタイミング
							if(getDate() == 1) {
								SeerCoTimingMap.put(talk.getAgent(), turn);
							}
						}
					}
					grayList.remove(talk.getAgent());
				}

				/*DIV発言時の動作*/
				if(content.getTopic() == Topic.DIVINED) {

					if(talk.getAgent() == me) {
						//DIVは1日1回
						divineRemark = true;
					}
					else {
						//COせずにDIVした人も
						if(!SeerList.contains(talk.getAgent())) {
							SeerList.add(talk.getAgent());
						}
						//DIVした人のAgentDataへ(誰に何を出したか)
						AgentDataMap.get(talk.getAgent()).divineData(content);
					}
					grayList.remove(content.getTarget());
					if (content.getTarget() != me) {
						AgentDataMap.get(content.getTarget()).divinedMap(content.getResult());
					}
				}

				/*ES発言時の動作*/
				if(content.getTopic() == Topic.ESTIMATE) {

					if(talk.getAgent() == me) {
						//すでにESした役職にフラグをつけていく
						if(content.getRole()==Role.POSSESSED) {
							esPossessed=true;
						}else if(content.getRole()==Role.SEER) {
							esSeer=true;
						}
						else if(content.getRole()==Role.WEREWOLF) {
							esWolf=true;
							esWolfAgent=content.getTarget();
						}
						else if(content.getRole()==Role.WEREWOLF) {
							esVillager=true;
						}
					}
				}

				/*VOTE発言時の動作*/
				if(content.getTopic() == Topic.VOTE) {
					if(content.getTarget() != me || talk.getAgent() != me) {
						continue;
					}
					//VOTEしたひとのAgentDataへ(VOTE相手との関係)
					AgentDataMap.get(talk.getAgent()).voteData(content.getTarget());
					//VOTEされた人のAgentDataへ(isVotedの回数)
					AgentDataMap.get(content.getTarget()).countVotedData();
				}

				readcount = i;


				/*自分の発言は無視*/
				if(talk.getAgent() == me) {
					continue;
				}

				//updateActionを呼び出す
				this.updateAction(talk, content);

				/*現在のturnですでに発言がされている場合*/
				if(TurnFlagMap.get(turn)) {
					continue;
				}

				//actionを呼び出す
				this.action(talk, content);

				//actionでtalkQueueに格納された場合フラグ
				if(!talkQueue.isEmpty()) {
					TurnFlagMap.put(turn, true);
				}

			}

			if(readcount!=0) {
				readTalkNum = readcount+1;
			}

		} catch (Exception e) {
			this.writeEx(e);
			System.err.println(e);
		}

	}


	/**15人人狼時update動作*/
	public void update15(GameInfo arg0) {
		try {
			this.gameInfo = arg0;
			voteAgent=null;
			int readcount = 0;

			/*0日目の処理*/
			if(turn==0 && !turn0Flag && getDate()!=0) {
				//1日目だけは指定
				if(getDate()==1) {
					date=1;
				}
				turn0Flag=true;
				turn0Action15();
			}

			/*人狼時whisper*/
			if(gameInfo.getRole()==Role.WEREWOLF && getDate()==0) {
				List<Talk> whisperList = this.gameInfo.getWhisperList();
				for(int i=readTalkNum; i < whisperList.size(); i++ ) {
					Talk talk = whisperList.get(i);
					Content content = new Content(talk.getText());

					/*CO発言時の動作*/
					if(content.getTopic() == Topic.COMINGOUT) {
						if(content.getRole() == Role.SEER) {
							mediumMovement = true;
						}
					}

				}
			}

			List<Talk> talkList = this.gameInfo.getTalkList();

			for(int i=readTalkNum; i < talkList.size(); i++ ) {
				Talk talk = talkList.get(i);
				Content content = new Content(talk.getText());


				/*CO発言時の動作*/
				if(content.getTopic() == Topic.COMINGOUT) {
					//占い師COの人をリストに登録(自分も含む)
					if(content.getRole() == Role.SEER) {
						SeerList.add(talk.getAgent());
					}
					//霊媒師COの人をリストに登録(自分も含む)
					else if(content.getRole() == Role.MEDIUM) {
						MediumList.add(talk.getAgent());
					}

					if(talk.getAgent() == me) {
						isCO = true;
						if(content.getRole() == Role.SEER) {
							SeerCO = true;
						}
						else if(content.getRole() == Role.VILLAGER) {
							VillagerCO = true;
						}
						else if(content.getRole() == Role.POSSESSED) {
							PossessedCO = true;
						}
						else if(content.getRole() == Role.WEREWOLF) {
							WolfCO = true;
						}
						else if(content.getRole() == Role.MEDIUM) {
							MediumCO = true;
						}
						else if(content.getRole() == Role.BODYGUARD) {
							BodygardCO = true;
						}
					}
					//自分以外がCOした場合
					else{
						if(content.getRole()==Role.SEER) {
							//1日目のSeerCOのタイミング
							if(getDate() == 1) {
								SeerCoTimingMap.put(talk.getAgent(), turn);
							}
						}
					}
					grayList.remove(talk.getAgent());

				}

				/*DIV発言時の動作*/
				if(content.getTopic() == Topic.DIVINED) {

					if(talk.getAgent() == me) {
						//DIVは1日1回
						divineRemark = true;
					}
					else {
						//COせずにDIVした人も
						if(!SeerList.contains(talk.getAgent())) {
							SeerList.add(talk.getAgent());
						}
						//DIVした人のAgentDataへ(誰に何を出したか)
						AgentDataMap.get(talk.getAgent()).divineData(content);
					}
					grayList.remove(content.getTarget());
					if (content.getTarget() != me) {
						AgentDataMap.get(content.getTarget()).divinedMap(content.getResult());
					}
				}

				/*IDENTIFIED発言時の動作*/
				if(content.getTopic() == Topic.IDENTIFIED) {

					if(talk.getAgent() != me) {
						//IDした人のAgentDataへ(誰に何を出したか)
						AgentDataMap.get(talk.getAgent()).IdentiedData(talk,content);

						//ConectionListへの処理(me以外)
						for(Agent agent: AgentDataMap.keySet()) {
							if(agent != talk.getAgent()) {
								//占い師側のAgentDataのConectionList
								Agent a = AgentDataMap.get(agent).SeerConection(talk.getAgent(),content.getTarget(),content.getResult());
								//霊媒師側のAgentDataのConectionList
								if(a != null) {
									AgentDataMap.get(talk.getAgent()).MediumConection(agent);
								}
								else {
									AgentDataMap.get(talk.getAgent()).MediumNotConection(agent);
								}
							}
						}

					}
					grayList.remove(content.getTarget());

				}

				/*ES発言時の動作*/
				if(content.getTopic() == Topic.ESTIMATE) {

					if(talk.getAgent() == me) {
						//すでにESした役職にフラグをつけていく
						if(content.getRole()==Role.POSSESSED) {
							esPossessed=true;
						}else if(content.getRole()==Role.SEER) {
							esSeer=true;
						}
						else if(content.getRole()==Role.WEREWOLF) {
							esWolf=true;
							esWolfAgent=content.getTarget();
						}
					}
				}

				/*VOTE発言時の動作*/
				if(content.getTopic() == Topic.VOTE) {
					if(talk.getAgent() != me) {
						//VOTEしたひとのAgentDataへ(VOTE相手との関係)
						AgentDataMap.get(talk.getAgent()).voteData(content.getTarget());
					}
					if(content.getTarget() != me) {
						//VOTEされた人のAgentDataへ(isVotedの回数)
						AgentDataMap.get(content.getTarget()).countVotedData();
					}
				}

				readcount = i;

				/*自分の発言は無視*/
				if(talk.getAgent() == me) {
					continue;
				}


				//ここでupdateAction15とaction15を呼び出す
				this.updateAction15(talk, content);

				/*現在のturnですでに発言がされている場合*/
				if(TurnFlagMap.get(turn)) {
					continue;
				}

				this.action15(talk, content);

				/*actionでtalkQueueに格納された場合フラグ*/
				if(!talkQueue.isEmpty()) {
					TurnFlagMap.put(turn, true);
				}

			}

			if(readcount!=0) {
				readTalkNum=readcount+1;
			}

		} catch (Exception e) {
			this.writeEx(e);
		}
	}

	/*---------ここからは自分で実装するメソッド．これ以降のメソッドを各役職でOverrideすることで役職ごとの行動を適用する--------*/


	/**各役職頃にオーバーライドされるから空
	 * 例：疑い度の変動
	 * 発言キューに登録
	*/
	public void action(Talk talk, Content content) {

	}

	public void action15(Talk talk, Content content) {

	}

	/**
	 * dayStartメソッドで呼んでいる処理 dayStart()の中に書かれているのを確認されたし
	 * 各役職ごとにオーバーライドされるから空
	 */
	public void dayStartAction() {
	}

	public void dayStartAction15() {
	}

	/**
	 * updateメソッドで呼んでいる処理 update()の中に書かれているのを確認されたし
	 * 各役職ごとにオーバーライドされるから空
	 */

	public void updateAction(Talk talk, Content content) {
	}

	public void updateAction15(Talk talk, Content content) {
	}

	/**
	 * updateメソッドで呼んでいる処理 update()の中に書かれているのを確認されたし
	 * 各役職ごとにオーバーライドされるから空
	 */
	public void turn0Action() {
	}

	public void turn0Action15() {
	}

	/**
	 * talkメソッドで呼んでいる処理 talk()の中に書かれているのを確認されたし
	 * 各役職ごとにオーバーライドされるから空
	 */
	public void talkAction() {
	}

	/**
	 * ラインを作る処理
	 */
	public void MyConection() {
	}



	public List<Agent> getDeadAgentList() {
		List<Agent> storeList = gameInfo.getAgentList();
		storeList.removeAll(gameInfo.getAliveAgentList());
		return storeList;
	}

	public List<Agent> getAliveOthersList() {
		List<Agent> storeList = new ArrayList<>(gameInfo.getAliveAgentList());
		storeList.remove(gameInfo.getAgent());
		return storeList;
	}

	public Agent getHighestVoteAgent() {
		int highestVoteCount = 0;
		for(Agent agent: getAliveOthersList()) {
			if(highestVoteCount <= AgentDataMap.get(agent).getVotedCount(agent)) {
				highestVoteCount = AgentDataMap.get(agent).getVotedCount(agent);
				highestVotedAgent = agent;
			}
		}
		return highestVotedAgent;
	}

	public Agent getHighestVoteAgent(Agent agent) {
		int highestVoteCount = 0;
		for(Agent ag: getAliveOthersList()) {
			if(ag != agent) {
				if(highestVoteCount<= AgentDataMap.get(agent).getVotedCount(agent)) {
					highestVoteCount = AgentDataMap.get(agent).getVotedCount(agent);
					highestVotedAgent = agent;
				}
			}
		}
		return highestVotedAgent;
	}

	public Agent getHighestScoreAgent() {
		int highestScoreCount = 0;
		Agent highestScoreAgent = null;
		for(Agent ag: getAliveOthersList()) {
			if(highestScoreCount<=AgentDataMap.get(ag).score) {
				highestScoreCount = AgentDataMap.get(ag).score;
				highestScoreAgent = ag;
			}
		}
		return highestScoreAgent;
	}

	public Agent getLowestScoreAgent() {
		int lowestScoreCount = 0;
		Agent lowestScoreAgent = null;
		for(Agent ag: getAliveOthersList()) {
			if(lowestScoreCount>=AgentDataMap.get(ag).score) {
				lowestScoreCount = AgentDataMap.get(ag).score;
				lowestScoreAgent = ag;
			}
		}
		return lowestScoreAgent;
	}





}
