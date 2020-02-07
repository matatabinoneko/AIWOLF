#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd

import MyData as md
import basevillager as bv

class Medium(bv.Villager):

    def __init__(self, agent_name):
        super(Medium,self).__init__(agent_name)
        self.Role = 'MEDIUM'
        self.candidate  = []    #投票候補リスト

        #白黒リスト
        self.myBlack    = []    #霊媒結果黒(人狼)だったプレイヤのリスト
        self.myWhite    = []    #霊媒結果白(人間)だったプレイヤのリスト
        self.willSay    = []    #まだいっていない霊媒結果のリスト

        #偽確定リスト
        self.fakeSeer   = []    #自分の霊媒結果と異なる結果を出した占い師
        self.fakeMedium = []    #自分以外に霊媒師COしたプレイヤ

        self.isCall     = False
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Medium,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Medium,self).initialize(game_info, game_setting)
        self.isCO         = False

    def dayStart(self):
        super(Medium,self).dayStart()

        if(self.gameInfo['day'] > 1):
            identified   = self.gameInfo['mediumResult']
            result      = identified['result']
            target      = identified['target']

            self.willSay.append(identified)

            if(result == 'HUMAN'):
                self.myWhite.append(target)
            else:
                self.myBlack.append(target)

    def talk(self):

        self.myfakeSearch()

        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger):
            self.isCO = True
            return ttf.comingout(self.agentIdx,self.Role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.willSay) > 0):
            target = self.willSay[0]['target']
            result = self.willSay[0]['result']
            self.willSay.pop()
            return ttf.identified(target,result)

        return ttf.over()

    def vote(self):

        #投票候補から最も狼らしい者
        if(len(self.candidate) > 0):
            return self.myData.getMaxLikelyWolf(self.candidate)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(Medium,self).finish()

    #COするか否か
    def COTrigger(self):
        #ひとまず2日目1ターン目CO
        #Trigger条件はここで調整可能。
        if(self.myData.getToday() > 1):
            return True
        else:
            return False

    #偽者調査
    def myfakeSearch(self):
        #偽霊媒師探し
        mediumlist = self.myData.getMediumCOAgentList()

        if self.agentIdx in mediumlist:   mediumlist.remove(self.agentIdx)
        if len(mediumlist) > 0:
            for i in mediumlist:
                if i not in self.fakeMedium:  self.fakeMedium.append(i)

        #偽占い師探し
        seer = self.myData.getSeerCODataMap()
        if(len(seer) > 0):
            for k,v in seer.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()
                #自分の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in self.myWhite and k not in self.fakeSeer):
                            self.fakeSeer.append(k)
                #自分の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in self.myBlack and k not in self.fakeSeer):
                            self.fakeSeer.append(k)
        return None

    def setmyData(self,mydata):
        self.myData = mydata
