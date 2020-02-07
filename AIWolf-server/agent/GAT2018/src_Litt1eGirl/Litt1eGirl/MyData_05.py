# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np

import string
import PlayerData
import COData as co

class MyData():

    def __init__(self):
        self.__seerCODataMap_   = {}
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
        self.__isCOCall_        = False
        self.__isDivineCall_    = False
        self.gameInfo           = None
        self.turn               = 0

        self.wolfWinrate    = []
        self.villWinrate    = []

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
                        self.__isCOCall_        = True
                    #村人CO
                    elif (talk['text'].split()[2] == 'VILLAGER'):
                        self.__villCODayMap_.update({agentIdx:self.__today_})
                    #人狼CO
                    elif (talk['text'].split()[2] == 'WEREWOLF'):
                        self.__wolfCODayMap_.update({agentIdx:self.__today_})
                    #狂人CO
                    elif (talk['text'].split()[2] == 'POSSESSED'):
                        self.__possCODayMap_.update({agentIdx:self.__today_})

                    self.__isOneTimeCall_   = True


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

                    self.__isOneTimeCall_   = True
                    self.__isDivineCall_        = True

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
                        if(value != None):
                            value = value + 1
                            self.__voteNum_.update({newTargetIdx:value})
                            self.__voteMap_.update({agentIdx:newTargetIdx})
                        else:
                            print(talk)

                if (talk['text'].split()[0] != 'SKIP' and talk['text'].split()[0] != 'OVER'):
                    num = self.__talkNumMap_.get(agentIdx)
                    num = num + 1
                    self.__talkNumMap_.update({agentIdx:num})

                num = self.__talkAllNumMap_.get(agentIdx)
                num = num +1
                self.__talkAllNumMap_.update({agentIdx:num})

    def dayStart(self,gameInfo):
        self.gameInfo   = gameInfo
        self.__today_   = gameInfo["day"]
        self.__isOneTimeCall_   = False
        self.__isCOCall_  = False
        self.__isDivineCall_   = False
        p_data  = PlayerData

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
        self.agentIdx       = gameInfo['agent']
        self.__playerNum_   = playerNum
        self.__agentList_   = agentNames
        self.__aliveAgentList_  = agentNames
        self.__agentIdxlist_    = self.AgentToIndexList(agentNames)
        self.__aliveAgentIdxlist_ = self.AgentToIndexList(agentNames)

        for i in range(self.__playerNum_):
            pd = PlayerData.PlayerData(self.__agentIdxlist_[i],self.__agentList_[i])
            self.__playerDataMap_.update({self.__agentIdxlist_[i]:pd})
            self.__talkNumMap_.update({self.__agentIdxlist_[i]:0})
            self.__talkAllNumMap_.update({self.__agentIdxlist_[i]:0})
            self.__voteMap_.update({self.__agentIdxlist_[i]:-1})
            self.__voteNum_.update({self.__agentIdxlist_[i]:0})

            #wolfSideTable_.initTable(True);
            #wolfSideTable_.agentMemory_.clear();
            #wolfSideTable_.roleMemory_.clear();
            #wolfSideTable_.infoMemory_.clear();

    def finish(self):
        return None

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
            data = co.SeerCOData(day, turn, True)
            self.__seerCODataMap_.update({agentIdx:data})


    #占い結果を出した占い師のデータ更新
    def addSeerData(self,agentIdx, targetIdx, isBlack, day, turn):
        #COせずに結果を言った人
        if (agentIdx not in self.__seerCODataMap_):
            data = co.SeerCOData(day, turn, False)
            self.__seerCODataMap_.update({agentIdx:data})

        #COして結果を言った人
        else:
            data = self.__seerCODataMap_[agentIdx]
        data.add(day, targetIdx, isBlack)

    def getSeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            reList.append(k)
        return reList

    def getWerewolfCOAgentList(self):
        reList = []
        for k,v in self.__wolfCODayMap_.items():
            reList.append(k)
        return reList

    def getAliveSeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            if (v.isAlive()):
                reList.append(k)
        return reList

    def getTodaySeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
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

    def getRequestDataMap(self):
        return self.RequestMap_

    def getSeerCODataMap(self):
        return self.__seerCODataMap_

    def setSeerCODataMap(self,seerCODataMap):
        self.__seerCODataMap_ = seerCODataMap

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

    def getMaxLikelyWolfAll(self):
        candidate   = self.getMaxLikelyExecuteAgentAll()
        return candidate

    def getMaxLikelyWolf(self,targetList):
        candidate   = self.getMaxLikelyExecuteAgent(targetList)
        return candidate

    def getMaxLikelyVillAll(self):
        candidate = self.getAliveAgentIndexList()
        candidate.remove(self.agentIdx)
        perm = np.random.permutation(candidate)
        return int(perm[0])

    def getMaxLikelyVill(self,targetList):
        if(len(targetList) > 0):
            perm = np.random.permutation(targetList)
            return int(perm[0])
        else:
            return self.getMaxLikelyVillAll()


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

    def getMaxLikelyExecuteAgent(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
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
            if(len(targetIdxList) > 0):
                perm = np.random.permutation(targetIdxList)
                return perm[0]
            else:
                return self.getMaxLikelyExecuteAgentAll()
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

    def getLikelyExecuteMap(self):
        votedmap = []
        for agentIdx in enumerate(self.getAliveAgentList()):
            votedmap.update(agentIdx, self.__voteMap_.get(agentIdx))
        return votedmap

    def isCOCall(self):
        answer = self.__isCOCall_
        self.__isCOCall_ = False
        return answer

    def isDivineCall(self):
        answer = self.__isDivineCall_
        self.__isDivineCall_ = False
        return answer

    def isOneTimCall(self):
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

    def setWinrate(self,wolfWinrate,villWinrate):
        self.wolfWinrate    = wolfWinrate
        self.villWinrate    = villWinrate

    def getWinrate(self):
        return self.wolfWinrate,self.villWinrate

    def CalcWinrank(self):
        self.calc_flag  = True

        wolf,vill   =   {},{}
        self.strongWolf,self.strongVill   = [],[]
        self.strongWolfrate,self.strongVillrate   = [],[]
        self.wolf_ave,self.vill_ave = 0,0

        #各エージェントの勝率を計算
        for k,v in self.wolfWinrate.items():
            if(v[0] != 0):  rate = float(v[1]) / float(v[0])
            else:           rate = 0
            wolf.update({k:rate})
        for k,v in self.villWinrate.items():
            if(v[0] != 0):  rate = float(v[1]) / float(v[0])
            else:           rate = 0
            vill.update({k:rate})

        #各エージェントを勝率順にソート
        for k, v in sorted(wolf.items(), key=lambda x: -x[1]):
            self.strongWolf.append(k)
            self.strongWolfrate.append(v)
            self.wolf_ave   += v
        for k, v in sorted(vill.items(), key=lambda x: -x[1]):
            self.strongVill.append(k)
            self.strongVillrate.append(v)
            self.vill_ave   += v

        #全エージェントの勝率平均
        self.wolf_ave = float(self.wolf_ave)/float(self.__playerNum_ )
        self.vill_ave = float(self.vill_ave)/float(self.__playerNum_ )

        self.strongVillMap,self.strongAgentMap  = {},{}
        self.strongAsVill,self.strongAgent    = [],[]

        for k,v in vill.items():
            #高いほど村人として強い
            vill_rate   = float(v) / float(self.vill_ave)
            #高いほど人狼として弱い
            if(wolf[k] != 0):
                wolf_rate   = float(self.wolf_ave) / float(wolf[k])
            else:
                wolf_rate   = 1
            rate    = vill_rate * wolf_rate

            #人狼としては弱いが，村人として強いエージェント
            self.strongVillMap.update({k:rate})

            #高いほど人狼として強い
            wolf_rate   = float(wolf[k]) / float(self.wolf_ave)
            rate    = vill_rate * wolf_rate
            #人狼としても，村人としても強いエージェント
            self.strongAgentMap.update({k:rate})

        for k, v in sorted(self.strongAgentMap.items(), key=lambda x: -x[1]):
            self.strongAgent.append(k)
        for k, v in sorted(self.strongVillMap.items(), key=lambda x: -x[1]):
            self.strongAsVill.append(k)
