#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd
import copy,random

import MyData as md
import basevillager as bv

class Seer(bv.Villager):

    def __init__(self, agent_name):
        super(Seer,self).__init__(agent_name)
        self.Role = 'SEER'
        self.willvote   = None

        #占い結果リスト
        self.myDivine   = []
        self.myBlack    = []    #自分の黒
        self.aliveBlack = []    #生存している黒
        self.myWhite    = []    #自分の白
        self.aliveWhite = []    #生存している白

        #偽確定リスト
        self.fakeSeer   = []
        self.fakeMedium = []
        self.isCall     = False
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Seer,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Seer,self).initialize(game_info, game_setting)
        self.playerNum  = game_setting['playerNum']
        self.divineList = self.myData.AgentToIndexList(self.myData.getAliveAgentList())#占い候補リスト
        self.divineList.remove(self.agentIdx)

    def dayStart(self):
        super(Seer,self).dayStart()
        self.willvote   = None
        self.isCall = False
        divined  = self.gameInfo['divineResult']

        #占い結果処理
        if(divined != None):
            self.myDivine.append(divined)
            target  = divined['target']
            result  = divined['result']

            if(result == 'HUMAN'):
                self.myWhite.append(target)
                self.aliveWhite.append(target)
            else:
                self.myBlack.append(target)
                self.aliveBlack.append(target)
            if(target in self.divineList):  self.divineList.remove(target)

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.divineList):     self.divineList.remove(execute)
            if(execute in self.aliveBlack):     self.aliveBlack.remove(execute)
            if(execute in self.aliveWhite):     self.aliveWhite.remove(execute)
            if(attacked in self.divineList):    self.divineList.remove(attacked)
            if(attacked in self.aliveBlack):    self.aliveBlack.remove(attacked)
            if(attacked in self.aliveWhite):    self.aliveWhite.remove(attacked)

        #投票候補リスト (生存白リストを抜く)
        for i in self.aliveWhite:   self.candidate.remove(i)

    def talk(self):

        self.myfakeSearch()
        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,self.Role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.myDivine) > 0):
            target = self.myDivine[0]['target']
            result = self.myDivine[0]['result']
            self.myDivine.pop()
            return ttf.divined(target,result)

        return ttf.over()

    def vote(self):

        #投票から最も狼らしい者
        if(len(self.candidate) > 0):
            return self.myData.getMaxLikelyWolf(self.candidate)

        #例外対策
        return self.agentIdx

    def divine(self):

        #生存者から最も狼らしい者
        if(len(self.candidate) > 0):
            target = self.myData.getMaxLikelyWolf(self.candidate)
            print(self.myData.getAliveAgentList())
            return int(target)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(Seer,self).finish()

    #COするか否か
    def COTrigger(self):
        #初日1ターン目(確率0.9)
        if(self.myData.getToday() == 1 and self.myData.getTurn() == 0):
            rand = random.randint(1,10)
            if(rand < 10):  return True

        #黒を見つけたとき
        if(len(self.myBlack) > 0):  return True

        #他占い師COがあったとき
        seerlist    = self.myData.getSeerCOAgentList()
        if(len(seerlist) > 0):      return True

        #吊られそうなとき
        exe     = self.myData.getMaxLikelyExecuteAgentAll()
        if(exe == self.agentIdx):     return True

        #2日目は必ずCO
        if(self.myData.getToday == 2):  return True

        return False

    #偽者調査
    def myfakeSearch(self):

        #偽占い師探し
        seerlist = self.myData.getSeerCOAgentList()

        if self.agentIdx in seerlist:   seerlist.remove(self.agentIdx)
        if len(seerlist) > 0:
            for i in seerlist:
                if i not in self.fakeSeer:  self.fakeSeer.append(i)

        #偽霊媒探し
        medium = self.myData.getMediumCODataMap()
        if(len(medium) > 0):
            for k,v in medium.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()
                #自分の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in self.myWhite and i not in self.fakeMedium):
                            self.fakeMedium.append(k)
                #自分の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in self.myBlack and i not in self.fakeMedium):
                            self.fakeMedium.append(k)

    def setmyData(self,mydata):
        self.myData = mydata
