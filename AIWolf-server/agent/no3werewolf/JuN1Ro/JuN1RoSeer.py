#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn,copy
import pandas as pd
import MyData as md

import baseseer as bs

class Seer(bs.Seer):

    def __init__(self, agent_name):
        super(Seer,self).__init__(agent_name)
        self.willvote   = None

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Seer,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Seer,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Seer,self).dayStart()
        self.willvote   = None

    def talk(self):


        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger):
            self.isCO = True
            return ttf.comingout(self.agentIdx,self.Role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.myDivine) > 0):
            target = self.myDivine[0]['target']
            result = self.myDivine[0]['result']
            self.myDivine.pop()
            return ttf.divined(target,result)

        #偽探し
        fake_m,fake_s       = self.o_fakeSearch(3)
        self.myfakeSearch()

        #VOTE宣言(黒出し、客観的偽、主観的偽、最黒)
        if(len(self.aliveBlack) > 0):
            target = self.myData.getMaxLikelyWolf(self.aliveBlack)
            if(self.willvote != target):
                self.willvote = target
                return ttf.vote(self.willvote)
        elif(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.willvote != self.myData.getMaxLikelyWolf(fake) ):
                self.willvote = self.myData.getMaxLikelyWolf(fake)
                return ttf.vote(self.willvote)
        elif(len(self.fakeSeer) > 0 or len(self.fakeMedium)):
            myfake = self.fakeSeer + self.fakeMedium
            if(self.willvote != self.myData.getMaxLikelyWolf(myfake) ):
                self.willvote = self.myData.getMaxLikelyWolf(myfake)
                return ttf.vote(self.willvote)
        else:
            target = self.myData.getMaxLikelyWolf(self.candidate)
            if(self.willvote != target):
                self.willvote = target
                return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate()
            if(talk != None):   return talk

        return ttf.over()

    def vote(self):

        fake_m,fake_s       = self.o_fakeSearch(3)
        self.myfakeSearch()

        #黒出し、客観的偽、主観的偽、最黒
        if(len(self.aliveBlack) > 0):
            target = self.myData.getMaxLikelyWolf(self.aliveBlack)
            return target
        elif(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            target = self.myData.getMaxLikelyWolf(fake)
            return target
        elif(len(self.fakeSeer) > 0 or len(self.fakeMedium)):
            myfake = self.fakeSeer + self.fakeMedium
            target = self.myData.getMaxLikelyWolf(myfake)
            return target
        else:
            target = self.myData.getMaxLikelyWolf(self.candidate)
            if(target != None):
                return target

        return super(Seer,self).vote()

    def divine(self):
        exe = self.gameInfo['latestExecutedAgent']

        if(exe in self.divineList):
            self.divineList.remove(exe)

        #占い候補から最も狼らしい者
        if(len(self.divineList) > 0):
            target = self.myData.getMaxLikelyWolf(self.divineList)
            if(target != None): return target

        return super(Seer,self).divine()

    def finish(self):
        super(Seer,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
