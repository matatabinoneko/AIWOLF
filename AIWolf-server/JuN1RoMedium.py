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

import basemedium as bm

class Medium(bm.Medium):

    def __init__(self, agent_name):
        super(Medium,self).__init__(agent_name)
        self.willvote   = None

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Medium,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Medium,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Medium,self).dayStart()
        self.willvote   = None

    def talk(self):

        self.myfakeSearch()

        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,self.Role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.willSay) > 0):
            target = self.willSay[0]['target']
            result = self.willSay[0]['result']
            self.willSay.pop()
            return ttf.identified(target,result)

        #偽探し
        fake_m,fake_s       = self.o_fakeSearch(3)
        self.myfakeSearch()

        #VOTE宣言(客観的偽、主観的偽、最黒)
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return ttf.vote(self.willvote)
        elif(len(self.fakeSeer) > 0 or len(self.fakeMedium)):
            myfake = self.fakeSeer + self.fakeMedium
            if(self.Target(myfake)):    return ttf.vote(self.willvote)
        else:
            if(self.Target(self.candidate)):    return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate()
            if(talk != None):   return talk

        return ttf.over()

    def vote(self):

        fake_m,fake_s       = self.o_fakeSearch(3)
        self.myfakeSearch()

        self.willvote   = None

        #客観的偽、主観的偽、最黒
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return self.willvote
        elif(len(self.fakeSeer) > 0 or len(self.fakeMedium)):
            myfake = self.fakeSeer + self.fakeMedium
            if(self.Target(myfake)):    return self.willvote
        else:
            if(self.Target(self.candidate)):    return self.willvote

        return super(Medium,self).vote()

    #COするか否か
    def COTrigger(self):
        #2日目または1日目対抗でCO
        if(self.myData.getToday() > 1):
            return True
        elif(len(self.fakeMedium) > 0):
            return True
        else:
            return False

    def finish(self):
        super(Medium,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
