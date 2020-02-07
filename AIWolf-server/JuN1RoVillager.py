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

import basevillager as bv

class Villager(bv.Villager):

    def __init__(self, agent_name):
        super(Villager,self).__init__(agent_name)
        self.willvote   = None
        self.isCall     = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Villager,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Villager,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Villager,self).dayStart()
        self.willvote   = None

    def talk(self):

        #偽探し
        fake_m,fake_s       = self.o_fakeSearch(3)
        myfake   = self.s_fakeSearch(self.agentIdx)

        #VOTE宣言(偽確、最黒)
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return ttf.vote(self.willvote)
        elif(len(myfake) > 0):
            if(self.Target(myfake)):    return ttf.vote(self.willvote)
        else:
            if(self.Target(self.candidate)):    return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate()
            if(talk != None):   return talk

        #BGCO
        if(len(myfake) > 0 and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')

        return ttf.over()

    def vote(self):

        self.willvote = None

        #偽探し
        fake_m,fake_s       = self.o_fakeSearch(3)
        myfake   = self.s_fakeSearch(self.agentIdx)

        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):   return self.willvote
        elif(len(myfake) > 0):
            if(self.Target(myfake)): return self.willvote
        else:
            if(self.Target(self.candidate)): return self.willvote

        return super(Villager,self).vote()

    def finish(self):
        super(Villager,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
