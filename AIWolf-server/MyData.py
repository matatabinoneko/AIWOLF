# -*- coding: utf-8 -*-
#!/usr/bin/env python
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import scipy.sparse as sp
import sklearn,copy
import pandas as pd
import string
import PlayerData
import COData as co
import SeerCOData as sco
import MediumCOData as mco
import BGCOData as bco
import savelog as sv
import LSTM

class MyData():

    def __init__(self):
        self.__seerCODataMap_   = {}
        self.__mediumCODataMap_ = {}
        self.__bgCODataMap_     = {}
        self.__villCODayMap_    = {}
        self.__wolfCODayMap_    = {}
        self.__possCODayMap_    = {}
        self.__RequestMap_      = {}
        self.__playerDataMap_   = {}
        self.__talkNumMap_      = {}
        self.__talkAllNumMap_   = {}
        self.__voteMap_         = {}
        self.__resultMap_       = {}
        self.__voteNum_         = {}
        #AliveAgent
        self.__aliveAgentList_  = []
        self.__agentList_  = []
        self.__aliveAgentIndexList_  = []
        self.__agentIndexList_  = []
        self.__attackedAgentList_ =[]
        self.__executedAgentList_ =[]
        self.__voteTable_   = {}
        self.__today_       = 0
        self.__playerNum_   = 0
        self.__isCall_          = False
        self.__isOneTimeCall_   = False
        self.gameInfo           = None
        self.turn               = 0
        self.logTurn            = -1

        self.werewolves = []
        self.log        = sv.savelog()
        self.pred       = LSTM.Predict()
        self.result     = []
        self.rank       = []

    def update(self,gameInfo,talk_history):
        self.gameInfo   =   gameInfo
        if (talk_history != None):

            for i in range(len(talk_history)):
                talk    = talk_history[i]
                agentIdx    = talk['agent']
                turn    = talk['turn']
                self.setTurn(turn)
                isBlack = False

                #CO
                if (talk['text'].split()[0]== 'COMINGOUT'):

                    #占い師CO
                    if (talk['text'].split()[2] == 'SEER'):
                        self.newSeerData(agentIdx, self.__today_, turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('SEER', self.__today_ , turn)
                        resultlist = []
                        self.__resultMap_.update({agentIdx:resultlist})

                    #霊媒師CO
                    elif(talk['text'].split()[2] == 'MEDIUM'):
                        self.newMediumData(agentIdx, self.__today_, turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('MEDIUM', self.__today_, turn)

                    #狩人CO
                    elif (talk['text'].split()[2] == 'BODYGUARD'):
                        self.newBGData(agentIdx, self.__today_, turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('BODYGUARD', self.__today_, turn)

                    #村人CO
                    elif (talk['text'].split()[2] == 'VILLAGER'):
                        self.__villCODayMap_.update({agentIdx:self.__today_})
                    #人狼CO
                    elif (talk['text'].split()[2] == 'WEREWOLF'):
                        self.__wolfCODayMap_.update({agentIdx:self.__today_})
                    #狂人CO
                    elif (talk['text'].split()[2] == 'POSSESSED'):
                        self.__possCODayMap_.update({agentIdx:self.__today_})

                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #DIVINED
                elif (talk['text'].split()[0]== 'DIVINED'):
                    resultList = []
                    if (agentIdx not in self.__resultMap_):
                        self.__resultMap_.update({agentIdx:resultList})
                    else:
                        resultList = self.__resultMap_.get(agentIdx)
                    if (talk['text'].split()[2]== 'WEREWOLF'):
                        isBlack = True
                    if (isBlack):   resultList.append(1)
                    else:   resultList.append(0)
                    target = talk['text'].split()[1]
                    targetIdx = self.AgentToIndex(target)
                    #発言者、対象、白黒、日付を保存
                    self.addSeerData(agentIdx, targetIdx, isBlack, self.__today_, turn)
                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #IDENTIFIED
                elif (talk['text'].split()[0]== 'IDENTIFIED'):
                    if (talk['text'].split()[2] == 'WEREWOLF'): isBlack = True
                    target = talk['text'].split()[1]
                    targetIdx = self.AgentToIndex(target)
                    self.addMediumData(agentIdx, targetIdx, isBlack, self.__today_, turn)
                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #GUARDED
                elif (talk['text'].split()[0]== 'GUARDED'):
                    target = talk['text'].split()[1]
                    targetIdx = self.AgentToIndex(target)
                    self.addBGData(agentIdx, targetIdx, self.__today_, turn)
                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                p_data = self.__playerDataMap_.get(agentIdx)

                if (talk['text'].split()[0] == "VOTE"):
                    newTargetIdx = self.AgentToIndex(talk['text'].split()[1])
                    oldTargetIdx = self.__voteMap_.get(agentIdx)
                    if (oldTargetIdx != newTargetIdx):
                        if (oldTargetIdx != -1):
                            value = self.__voteNum_.get(oldTargetIdx)
                            value = value - 1
                            self.__voteNum_.update({oldTargetIdx:value})
                        value = self.__voteNum_.get(newTargetIdx)
                        value = value + 1
                        self.__voteNum_.update({newTargetIdx:value})
                        self.__voteMap_.update({agentIdx:newTargetIdx})

                '''
                if (uttr.getTopic() == Topic.OPERATOR):
                    targetIdx = 0;
                    if(uttr.getTarget()!= None):
                        targetIdx = uttr.getTarget().getAgentIdx()
                    t = uttr.getContentList()
				#	if(targetIdx==None || targetIdx==gameInfo.getAgent().getAgentIdx())
                    ReqData(agentIdx,t,targetIdx)
                '''

                if (talk['text'].split()[0] != 'SKIP' and talk['text'].split()[0] != 'OVER'):
                    num = self.__talkNumMap_.get(agentIdx)
                    num = num + 1
                    self.__talkNumMap_.update({agentIdx:num})

                num = self.__talkAllNumMap_.get(agentIdx)
                num = num +1
                self.__talkAllNumMap_.update({agentIdx:num})

            #LOGの成形
            if(self.logTurn != self.getTurn()):
                self.log.saveTalk(gameInfo,talk_history)
                turndata = self.log.getTurntalk()
                if(len(turndata) > 0):
                    self.logTurn = self.getTurn()
                    self.Predictor(turndata)

    def dayStart(self,gameInfo):
        self.gameInfo   = gameInfo
        self.__today_   = gameInfo["day"]
        self.__isCall_  = False
        self.__isOneTimeCall_   = False
        self.logTurn    = -1
        p_data  = PlayerData

        #LOGの成形
        if(self.__today_ > 1):
            self.log.saveVote(gameInfo,self.__today_ - 1)
            dayvote    = self.log.getDayvote()
            self.Predictor(dayvote)

        if "statusMap" in gameInfo.keys():
            statusMap = gameInfo["statusMap"]

        del self.__aliveAgentList_[:]
        for ids in statusMap.keys():
            if statusMap[ids] == "ALIVE":
                name = 'Agent[' + "{0:02d}".format(int(ids)) + ']'
                self.__aliveAgentList_.append(name)

        executedAgentIdx = -1
        if gameInfo["executedAgent"] != None:
            executedAgentIdx = gameInfo["executedAgent"]
            self.addExecutedAgentList(executedAgentIdx)

        attackedAgentIdx = -1
        if gameInfo["attackedAgent"] != None:
            attackedAgentIdx = gameInfo["attackedAgent"]
            self.addAttackedAgentList(attackedAgentIdx)

        self.setAlive(executedAgentIdx, attackedAgentIdx)

        dayVoteList = [-1] * self.__playerNum_

        self.__voteTable_.update({self.__today_-1 : dayVoteList})
        for vote in gameInfo['voteList']:
            voteList = self.__voteTable_.get(self.__today_-1)
            self.__voteTable_[self.__today_-1][vote['agent']-1] = vote['target']

        for vote in gameInfo['voteList']:
            agentIdx    = vote['agent']
            voteIdx     = vote['target']
            talkedIdx = self.__voteMap_[agentIdx]

            if (voteIdx == talkedIdx):  votepattern = 0
            else:
                if (talkedIdx != -1):   votepattern = 1
                else:                   votepattern = 2

            p_data  = self.__playerDataMap_.get(agentIdx)
            seer    = self.getAliveSeerCOAgentList()
            if (self.__today_ > 1 and agentIdx in seer):
                p_data.addDayData_black(votepattern, self.__resultMap_.get(agentIdx))
            elif (self.__today_ > 0):
                p_data.addDayData(votepattern)

        for agent in self.__aliveAgentList_:
            idx = self.AgentToIndex(agent)
            self.__talkNumMap_[idx]     =   0
            self.__talkAllNumMap_[idx]  =   0
            self.__voteMap_[idx]        =   -1
            self.__voteNum_[idx]        =   0

    def gameStart(self,gameInfo, playerNum, agentNames):
        self.gameInfo       =   gameInfo
        self.__playerNum_   = playerNum
        self.__agentList_   = agentNames
        self.__aliveAgentList_  = agentNames
        self.__agentIdxlist_    = self.AgentToIndexList(agentNames)
        self.__aliveAgentIdxlist_ = self.AgentToIndexList(agentNames)
        self.agentIdx   = gameInfo['agent']
        self.role       = gameInfo['roleMap'][str(self.agentIdx)]  #自分の役割

        if(self.role == 'WEREWOLF'):
            for k,v in gameInfo['roleMap'].items():
                if(self.agentIdx != int(k)):
                    self.werewolves.append(int(k))

        for i in range(self.__playerNum_):
            pd = PlayerData.PlayerData(self.__agentIdxlist_[i],self.__agentList_[i])
            self.__playerDataMap_.update({self.__agentIdxlist_[i]:pd})
            self.__talkNumMap_.update({self.__agentIdxlist_[i]:0})
            self.__talkAllNumMap_.update({self.__agentIdxlist_[i]:0})
            self.__voteMap_.update({self.__agentIdxlist_[i]:-1})
            self.__voteNum_.update({self.__agentIdxlist_[i]:0})

    def finish(self):
        self.pred.finish()

    #データを入れて予測結果を得る
    def Predictor(self,data):
        self.result = self.pred.predict(data)[0]
        self.result[self.agentIdx-1] = float("-inf")
        self.rank = np.argsort(self.result)[::-1]

        #人狼の場合、人狼度は0.6倍にする
        if(self.role == 'WEREWOLF'):
            for i in range (len(self.result)):
                if( (i+1) in self.werewolves):
                    self.result[i]  = 0.6 * self.result[i]
            self.rank = np.argsort(self.result)[::-1]

        print(self.rank,self.role)

    #エージェントをインデックスにする
    def AgentToIndex(self,agent):
        idx = str(agent)
        idx = idx.replace("Agent[0","")
        idx = idx.replace("Agent[", "")
        idx = idx.replace("]","")
        re  = int(idx)
        return re

    #エージェントリストをインデックスリストにする
    def AgentToIndexList(self,agentlist):
        reList = []
        for i in enumerate(agentlist):
            idx = str(i[1])
            idx = idx.replace("Agent[0","")
            idx = idx.replace("Agent[", "")
            idx = idx.replace("]","")
            reList.append(int(idx))
        return reList

    #インデックスリストをエージェントリストにする
    def IndexToAgentList(self,indexlist):
        reList = []
        for i in enumerate(indexlist):
            if(i < 10):
                agent = "Agent[0" + str(talk['agent']) + "]"
            else:
                agent = "Agent[" + str(talk['agent']) + "]"
            reList.append(agentIdx)
        return reList

    #役職持ちの生死更新
    def setAlive(self,executedIdx,attackedIdx):
        for i in self.__seerCODataMap_.keys():
            if (i == executedIdx or i == attackedIdx):
                self.__seerCODataMap_[i].setAlive(False)

        for i in self.__mediumCODataMap_.keys():
            if (i == executedIdx or i == attackedIdx):
                self.__mediumCODataMap_[i].setAlive(False)

        for i in self.__bgCODataMap_.keys():
            if (i == executedIdx or i == attackedIdx):
                self.__bgCODataMap_[i].setAlive(False)

    def ReqData(self,agentIdx,p,targetIdx):
        data = RequestData()
        if (not RequestMap_.has_key(agentIdx)):
            data = RequestData(p,targetIdx)
            RequestMap_.update({agentIdx:data})
        else:
            data = RequestMap_.get(agentIdx)
            data.add(p,targetIdx)

    #新しく占いCOした人のデータ作成
    def newSeerData(self,agentIdx, day, turn):
        if (agentIdx not in self.__seerCODataMap_):
            data = sco.SeerCOData(day, turn, True)
            self.__seerCODataMap_.update({agentIdx:data})
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に狩人COしていたらスライドチェック
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

    #新しく霊媒COした人のデータ作成
    def newMediumData(self,agentIdx, day, turn):
        if (agentIdx not in self.__mediumCODataMap_):
            data = mco.MediumCOData(day, turn, True)
            self.__mediumCODataMap_.update({agentIdx:data})
            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

    #新しく狩人COした人のデータ作成
    def newBGData(self,agentIdx, day, turn):
        if (agentIdx not in self.__bgCODataMap_):
            data = bco.BGCOData(day, turn, True)
            self.__bgCODataMap_.update({agentIdx:data})
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)

    #占い結果を出した占い師のデータ更新
    def addSeerData(self,agentIdx, targetIdx, isBlack, day, turn):
        #COせずに結果を言った人
        if (agentIdx not in self.__seerCODataMap_):
            data = sco.SeerCOData(day, turn, False)
            self.__seerCODataMap_.update({agentIdx:data})
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)
        #COして結果を言った人
        else:
            data = self.__seerCODataMap_[agentIdx]
        data.add(day, targetIdx, isBlack)

    #霊媒結果を出した霊媒師のデータ更新
    def addMediumData(self, agentIdx, targetIdx, isBlack, day, turn):
        #COせずに結果を言った人
        if (agentIdx not in self.__mediumCODataMap_):
            data = mco.MediumCOData(day, turn, False)
            self.__mediumCODataMap_.update({agentIdx:data})
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)
        #COして結果を言った人
        else:
            data = self.__mediumCODataMap_[agentIdx]
        data.add(day, targetIdx, isBlack)

    #護衛結果を出した狩人のデータ更新
    def addBGData(self, agentIdx, targetIdx, day, turn):
        if (agentIdx not in self.__bgCODataMap_):
            data = bco.BGCOData(day, turn, False)
            self.__bgCODataMap_.update({agentIdx:data})
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)
        else:
            data = self.__bgCODataMap_[agentIdx]
        data.add(targetIdx)

    def getSeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            reList.append(k)
        return reList

    def getMediumCOAgentList(self):
        reList = []
        for k,v in self.__mediumCODataMap_.items():
            reList.append(k)
        return reList

    def getBGCOAgentList(self):
        reList = []
        for k,v in self.__bgCODataMap_.items():
            reList.append(k)
        return reList

    def getAliveSeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            if (v.isAlive()):
                reList.append(k)
        return reList

    def getAliveMediumCOAgentList(self):
        reList = []
        for k,v in self.__mediumCODataMap_.items():
            if (v.isAlive()):
                reList.append(k)
        return reList

    def getAliveBGCOAgentList(self):
        reList = []
        for k,v in self.__bgCODataMap_.items():
            if (v.isAlive()):
                reList.append(k)
        return reList

    def getTodaySeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.__today_):
                reList.append(k)
        return reList

    def getTodayMediumCOAgentList(self):
        reList = []
        for k,v in self.__mediumCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.__today_):
                reList.append(k)
        return reList

    def getTodayBGCOAgentList(self):
        reList = []
        for k,v in self.__bgCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.__today_):
                reList.append(k)
        return reList

    def getVoteTable(self,day):
        return self.__voteTable_.get(day)

    def getSeerResultsList(self,index):
        data  = self.getseerCODataMap()
        if (index in data):
            return data.get(index).getKnownList()
        else:
            return None

    def getMediumResultsList(self,index):
        data  = self.getmediumCODataMap()
        if (index in data):
            return data.get(index).getKnownList()
        else:
            return None

    def getRequestDataMap(self):
        return self.RequestMap_

    def getSeerCODataMap(self):
        return self.__seerCODataMap_

    def setseerCODataMap(self,seerCODataMap):
        self.__seerCODataMap_ = seerCODataMap

    def getMediumCODataMap(self):
        return self.__mediumCODataMap_

    def setMediumCODataMap(self,mediumCODataMap):
        self.__mediumCODataMap_ = mediumCODataMap

    #今まで処刑されたプレイヤのリスト
    def getExecutedAgentList(self):
        return self.__executedAgentList_

    #前日処刑されたプレイヤ(daystartでget)
    def getExecutedAgent(self):
        return self.gameInfo['executedAgent']

    #当日処刑されたプレイヤ(Actionでget)
    def getLatestExecutedAgent(self):
        return self.gameInfo['latestExecutedAgent']

    #今まで襲撃されたプレイヤのリスト
    def getAttackedAgentList(self):
        return self.__attackedAgentList_

    #前日襲撃されたプレイヤ(daystartでget)
    def getAttackedAgent(self):
        if(len(self.gameInfo['lastDeadAgentList']) > 0):
            return self.gameInfo['lastDeadAgentList'][0]
        return 0

    #生存エージェントリスト
    def getAliveAgentList(self):
        return self.__aliveAgentList_

    #生存エージェントインデックスリスト
    def getAliveAgentIndexList(self):
        reList = []
        reList = self.AgentToIndexList(self.__aliveAgentList_)
        return reList

    def addExecutedAgentList(self,votedAgent):
        self.__executedAgentList_.append(votedAgent)

    def addAttackedAgentList(self,attackedAgent):
        self.__attackedAgentList_.append(attackedAgent)

    def getBgCODataMap(self):
        return self.__bgCODataMap_

    def setBgCODataMap(self,bgCODataMap):
        self.__bgCODataMap_ = bgCODataMap

    '''
	def tableUpdate(upType):
		#wolfSideTable_.initTable(False);
		if (upType == 0):
			yesterday = self.__today_ - 1
			attackedAgentIdx = attackedAgentList_.get(yesterday)
			if (attackedAgentIdx != -1):
				wolfSideTable_.calcAttackInfo(attackedAgentIdx)

            votes = getVoteTable(yesterday)
			for (int i = 0; i < votes.length ; i++):
				if (votes[i] > 0) {
					// wolfSideTable_.calcVoteInfo(i + 1, votes[i]);
					wolfSideTable_.calcVoteInfo((i+1), votes[i], yesterday);
				}
			}

		} else if (upType == 1) {
			wolfSideTable_.calcSeerCOInfo(getSeerCOAgentList());
			wolfSideTable_.calcMediumCOInfo(getMediumCOAgentList());
			wolfSideTable_.calcBGCOInfo(getBGCOAgentList());
			ArrayList<Integer> notCOAgentList = new ArrayList<Integer>();
			for (int idx : self.__aliveAgentList_) {
				if (!getAliveSeerCOAgentList().contains(idx)
						&& !getAliveMediumCOAgentList().contains(idx)
						&& !getAliveBGCOAgentList().contains(idx)) {
					notCOAgentList.append(idx);
				}
			}
			wolfSideTable_.calcNotCOInfo(notCOAgentList);
			for (java.util.Map.Entry<Integer, COData> entry : self.__seerCODataMap_.entrySet()) {
				int agentIdx = entry.getKey();
				HashMap<Integer, Boolean> result = entry.getValue().getResult(self.__today_);
				if (result != None) {
					for (int targetIdx : result.keySet()) {
						boolean isBlack = result.get(targetIdx);
						if (isBlack) {
							wolfSideTable_.calcDivineInfo(agentIdx, targetIdx,
									True);
						} else {
							wolfSideTable_.calcDivineInfo(agentIdx, targetIdx,
									False);
						}
					}
				}
			}
			for (java.util.Map.Entry<Integer, COData> entry : self.__mediumCODataMap_
					.entrySet()) {
				int agentIdx = entry.getKey();
				HashMap<Integer, Boolean> result = entry.getValue().getResult(
						self.__today_);
				if (result != None) {
					for (int targetIdx : result.keySet()) {
						boolean isBlack = result.get(targetIdx);
						if (isBlack) {
							wolfSideTable_.calcInquestInfo(agentIdx, targetIdx,
									True);
						} else {
							wolfSideTable_.calcInquestInfo(agentIdx, targetIdx,
									False);
						}
					}
				}
			}
			for (int seerIdx : self.__seerCODataMap_.keySet()) {
				SeerCOData seerCO = (SeerCOData) self.__seerCODataMap_.get(Integer
						.valueOf(seerIdx));
				for (int mediumIdx : self.__mediumCODataMap_.keySet()) {
					MediumCOData mediumCO = (MediumCOData) self.__mediumCODataMap_
							.get(Integer.valueOf(mediumIdx));
					wolfSideTable_.calcPatternInfo(seerIdx, seerCO, mediumIdx,
							mediumCO);
				}
			}

		} else {
			System.out.println("`upType' is wrong number! (upType is 0 or 1)");
		}

		wolfSideTable_.update();

	public void learningTableRate(GameInfo gameInfo) {
		wolfSideTable_.learningChangeRate(gameInfo);
		// for ( int i = 0; i < wolfSideTable_.getRates().length; i++ ){
		// System.out.print(String.format("%.5f", wolfSideTable_.getRate(i)) +
		// " , ");
		// }
		// System.out.println();
	}
    '''

    def getMaxLikelyWolfAll(self):

        #初日,2日目は便乗
        if(self.__today_ < 3):
            candidate   = self.getMaxLikelyExecuteAgentAll()
            return candidate
        #3日目以降LSTM
        else:
            if(len(self.rank) > 0):
                for i in self.rank:
                    if( (i+1) in self.getAliveAgentIndexList()):
                        return (i+1)
            candidate   = self.getMaxLikelyExecuteAgentAll()
            return candidate


    def getMaxLikelyWolf(self,targetList):

        #初日、2日目は便乗
        if(self.__today_ < 3):
            candidate   = self.getMaxLikelyExecuteAgent(targetList)
            return candidate
        #3日目以降LSTM
        else:
            if(len(self.rank) > 0):
                for i in self.rank:
                    if( (i+1) in self.getAliveAgentIndexList() and (i+1) in targetList):
                        return (i+1)
            candidate   = self.getMaxLikelyExecuteAgent(targetList)
            return candidate

    def getLikelyWolfRate(self):

        return self.result

    def getLikelyWolfRateMap(self):

        wolfmap = {}
        for i in range(len(self.result)):
            wolfmap.update({(i+1):self.result[i]})

        return wolfmap

    def getMaxLikelyVillAll(self):

        #初日は便乗
        if(self.__today_ == 1):
            perm = np.random.permutation(self.getAliveAgentIndexList())
            return perm[0]
        #2日目以降LSTM
        else:
            if(len(self.rank) > 0):
                for i in self.rank:
                    if( (i+1) in self.getAliveAgentIndexList()):
                        return (i+1)
            perm = np.random.permutation(self.getAliveAgentIndexList())
            return perm[0]

    def getMaxLikelyVill(self,targetList):

        if(not len(targetList) > 0):    return None

        #初日はランダム
        if(self.__today_ == 1):
            perm = np.random.permutation(targetList)
            return perm[0]
        #2日目以降LSTM
        else:
            if(len(self.rank) > 0):
                reverse = self.rank[::-1]
                for i in reverse:
                    if( (i+1) in self.getAliveAgentIndexList() and (i+1) in targetList):
                        return (i+1)
            perm = np.random.permutation(targetList)
            return perm[0]


    def getMinLikelyVillAll(self):
        candidateAgent  = self.getAliveAgentList()
        candidateIdx    = self.AgentToIndexList(candidateAgent)
        perm = np.random.permutation(candidateIdx)
        return candidateIdx[0]

        '''#人外テーブルを使うとき、最も狼らしくないプレイヤインデックスを返す
		double minRate = Double.POSITIVE_INFINITY;
		ArrayList<Integer> minIdxList = new ArrayList<Integer>();
		for (int idx : self.__aliveAgentList_) {
			double nWolfRate = wolfSideTable_.getNotWolfRate(idx);
			if (minRate > nWolfRate) {
				minRate = nWolfRate;
				minIdxList.clear();
				minIdxList.append(idx);
			} else if (minRate == nWolfRate) {
				minIdxList.append(idx);
			}
		}
		if (minIdxList.isEmpty()) {
			return -1;
		} else if (minIdxList.size() == 1) {
			return minIdxList.get(0);
		} else {
			return minIdxList.get(r_.nextInt(minIdxList.size()));
		}
        '''

    def getMinLikelyVill(self,targetList):
        perm = np.random.permutation(targetList)
        return perm[0]

        '''
		double minRate = 5460;
		ArrayList<Integer> minIdxList = new ArrayList<Integer>();
		for (int idx : targetIdxList) {
			if (isAlive(idx)) {
				double wolfRate = wolfSideTable_.getNotWolfRate(idx);
				if (minRate > wolfRate) {
					minRate = wolfRate;
					minIdxList.clear();
					minIdxList.append(idx);
				} else if (minRate == wolfRate) {
					minIdxList.append(idx);
				}
			}
		}
		if (minIdxList.isEmpty()) {
			return -1;
		} else if (minIdxList.size() == 1) {
			return minIdxList.get(0);
		} else {
			return minIdxList.get(r_.nextInt(minIdxList.size()));
		}
        '''

    def getLatestVotedNumber(self):
        voteMap = {}

        for i in range(len(self.__voteTable_.get(self.__today_))):
            targetIdx = self.__voteTable_.get(self.__today_)[i]
            if (voteMap.has_key(targetIdx)):
                voteMap.update(targetIdx,voteMap.get(targetIdx) + 1)
            else:
                voteMap.update(targetIdx, 1)
        return voteMap

    def getMaxLikelyExecuteAgentAll(self):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ != None ):
            for idx,votenum in self.__voteNum_.items():
                if (maxVote < votenum):
                    maxVote = votenum
                    del maxIdxList[:]
                    maxIdxList.append(idx)
                elif (maxVote == votenum):
                    maxIdxList.append(idx)

        if (len(maxIdxList) == 0):
            perm = np.random.permutation(targetIdxList)
            return perm[0]
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = np.random.permutation(len(maxIdxList))
            return maxIdxList[perm[0]]

    def getMaxLikelyExecuteAgentNum(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ !=None ):
            for idx in range(self.__voteNum_.keySet()):
                votenum = self.__voteNum_.get(idx)
                if (maxVote < votenum):
                    maxVote = votenun
        return maxVote

    def getMaxLikelyExecuteAgent(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
        if(len(targetIdxList) == 0):
            return None
        if(self.__voteNum_ != None ):
            for idx in targetIdxList:
                votenum = self.__voteNum_[idx]
                if (maxVote < votenum):
                    maxVote = votenum
                    del maxIdxList[:]
                    maxIdxList.append(idx)
                elif(maxVote == votenum):
                    maxIdxList.append(idx)

        if (len(maxIdxList) == 0):
            perm = np.random.permutation(targetIdxList)
            return perm[0]
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = np.random.permutation(len(maxIdxList))
            return maxIdxList[perm[0]]

    def getLikelyExecuteMap(self):
        votedmap = []
        for agentIdx in enumerate(self.getAliveAgentList()):
            votedmap.update(agentIdx, self.__voteMap_.get(agentIdx))
        return votedmap

    def isCall(self):
        return self.__isCall_

    def isOneTimeCall(self):
        answer = self.__isOneTimeCall_
        self.__isOneTimeCall_ = False
        return answer

    def isAlive(self,agent):
        if (agent in self.__aliveAgentList_):
            return True
        else:
            return False

    def isAliveIndex(self,agentIdx):
        if (agentIdx in self.getAliveAgentIndexList()):
            return True
        else:
            return False
    def getAgentIdxList(self):
        return self.__agentIdxlist_

    def getToday(self):
        return self.__today_

    def getPlayerDataMap(self):
        return self.__playerDataMap_

    def getResultMap(self):
        return self.__resultMap_

    def getTalkAllNumMap(self):
        return self.__talkAllNumMap_

    def getVillCODayMap(self):
        return self.__villCODayMap_

    def getWolfCODayMap(self):
        return self.__wolfCODayMap_

    def getPossCODayMap(self):
        return self.__possCODayMap_

    def getVoteMap(self):
        return self.__voteMap_

    def setTurn(self,turn):
        self.turn = turn

    def getTurn(self):
        return self.turn
