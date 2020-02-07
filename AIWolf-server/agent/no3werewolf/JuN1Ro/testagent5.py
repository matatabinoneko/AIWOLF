#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd

#各役職モジュールインポート
import villager5 as bv
import seer5 as bs
import basemedium as bm
import basebodyguard as bb
import possessed5 as bp
import werewolf5 as bw

import MyData as md
import sys
#

class BaseAgent(object):
    def __init__(self, agent_name):
        #print "__init__"
        #sys.stdout.flush()
        self.agent_name = agent_name
        self.gameInfo   = []
        pass

    def getName(self):
        #print "getName"
        #sys.stdout.flush()
        return self.agent_name

    def initialize(self, game_info, game_setting):
        #print "initialize"
        #sys.stdout.flush()
        #sys.stdout.flush()
        self.agentIdx   = game_info['agent']    #エージェントインデックス
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'#エージェント名
        self.role       = game_info['roleMap'][str(self.agentIdx)]  #自分の役割
        self.roleMap    = game_info['roleMap']#役割一覧（狼は全員の種別，他は自分のみ）
        self.playerNum  = game_setting['playerNum']#プレイヤ人数
        #エージェント名のリスト(Agent[01]など).
        self.agentNames = ['Agent[' + "{0:02d}".format(target) + ']' for target in range(1, self.playerNum+1)]
        self.gameInfo = game_info
        self.talkList = []
        self.grayList = []
        self.agentIdx = 0
        #myDataは毎ゲームリセット
        self.myData     = md.MyData()

        if self.role == 'VILLAGER':
        	self.agent = bv.Villager(self.agent_name)
        elif self.role == 'SEER':
            self.agent = bs.Seer(self.agent_name)
        elif self.role == 'MEDIUM':
        	self.agent = bm.Medium(self.agent_name)
        elif self.role == 'BODYGUARD':
        	self.agent = bb.Bodyguard(self.agent_name)
        elif self.role == 'POSSESSED':
        	self.agent = bp.Possessed(self.agent_name)
        elif self.role == 'WEREWOLF':
        	self.agent = bw.Werewolf(self.agent_name)

        self.myData.gameStart(game_info, self.playerNum, self.agentNames)
        self.agent.setmyData(self.myData)
        self.agent.initialize(game_info, game_setting)
        self.strategy = None
        self.strategyRand = np.random.random()
        if(self.strategyRand < 0.4):
            self.strategy = 0
        else:
            self.strategy = 1


    def update(self, game_info, talk_history, whisper_history, request):
        #print "update"
        #sys.stdout.flush()
        if(len(game_info) > 0):
            self.gameInfo = game_info
        self.myData.update(self.gameInfo, talk_history)
        self.agent.update(self.gameInfo, talk_history, whisper_history, request)
        self.agent.setmyData(self.myData)

    def dayStart(self):
        #print "daystart"
        #sys.stdout.flush()
        self.myData.dayStart(self.gameInfo)
        self.agent.setmyData(self.myData)
        self.agent.dayStart()
        self.grayList = self.myData.getAliveAgentIndexList()
        if(self.agentIdx in self.grayList):
            self.grayList.remove(self.agentIdx)
        self.talkIdx = 0
        self.talkList =[]
        #print "daystart", self.myData.getToday()
        if(self.myData.getToday() == 1):
            #print "day1", self.myData.getToday()

            if self.strategy == 0:
                #print "seer", self.myData.getToday()
                self.talkList.append(ttf.comingout(self.agentIdx, "SEER"))

                result = ""
                if(np.random.random() < 0.5):
                    result = "HUMAN"
                else:
                    result = "WEREWOLF"

                self.talkList.append(ttf.divined(self.grayList[np.random.randint(len(self.grayList))],result))

            else:
                pass
        elif(self.myData.getToday() == 2):
            if(np.random.random() < 0.5 and self.role != "SEER"):
                self.talkList.append(ttf.comingout(self.agentIdx, self.role))
            else:
                result = ""
                if(np.random.random() < 0.5):
                    result = "HUMAN"
                else:
                    result = "WEREWOLF"

                self.talkList.append(ttf.divined(self.grayList[np.random.randint(len(self.grayList))],result))


        self.talkList.append(ttf.over())

    def talk(self):
        #print self.myData.getToday()
        #print self.talkList
        #print "talk"
        #sys.stdout.flush()
        if(np.random.random() < 0.8 and self.talkIdx < len(self.talkList)):
            t = self.talkList[self.talkIdx]
            self.talkIdx += 1
        else:
            t = ttf.skip()
        return t

    def whisper(self):
        #print "whisper"
        #sys.stdout.flush()
        return ttf.over()

    def vote(self):
        #print "vote"
        #sys.stdout.flush()
        return self.grayList[np.random.randint(len(self.grayList))]

    def attack(self):
        #print "attack"
        #sys.stdout.flush()
        attackTargetList = self.myData.getAliveAgentIndexList()
        executed = self.gameInfo["latestExecutedAgent"]
        #print(executed,attackTargetList)
        if(self.agentIdx in attackTargetList):
            attackTargetList.remove(self.agentIdx)
        if(executed in attackTargetList):
            attackTargetList.remove(executed)
        return attackTargetList[np.random.randint(len(attackTargetList))]

    def divine(self):
        #print "divine"
        #sys.stdout.flush()

        divineTargetList = []
        executed = self.gameInfo["latestExecutedAgent"]
        #print(executed,attackTargetList)
        for agent in self.grayList:
            if agent in self.myData.getAliveAgentIndexList():
                divineTargetList.append(agent)

        if(executed in divineTargetList):
            divineTargetList.remove(executed)
        if len(divineTargetList) == 0:
            divineTargetList = self.myData.getAliveAgentIndexList()
            divineTargetList.remove(self.agentIdx)

        return divineTargetList[np.random.randint(len(divineTargetList))]

    def guard(self):
        #print "guard"
        #sys.stdout.flush()
        return self.agent.guard()

    def finish(self):
        #print "finish"
        #sys.stdout.flush()
        return self.agent.finish()
    def setmyData(self,mydata):
        self.myData = mydata
agent = BaseAgent('BaseAgent')

# run
if __name__ == '__main__':
    aiwolfpy.connect(agent)
