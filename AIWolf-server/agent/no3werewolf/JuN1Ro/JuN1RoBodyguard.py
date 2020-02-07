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

import basebodyguard as bb

class Bodyguard(bb.Bodyguard):

    def __init__(self, agent_name):
        super(Bodyguard,self).__init__(agent_name)
        self.willvote   = None
        self.myguard    = None
        self.attacked   = None
        self.isGJ       = False
        self.isRealSeer = True
        self.isSeerguard    = False
        self.yesterdaySeer = []
        #信仰占い師関連
        self.myseer     = None
        self.myWhite    = []
        self.myBlack    = []

        self.isCall     = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Bodyguard,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Bodyguard,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Bodyguard,self).dayStart()
        self.willvote   = None

        #護衛対象(占)が生存かつ襲撃失敗でGJ
        if(self.gameInfo['day'] > 1):
            self.attacked    = self.gameInfo['attackedAgent']
            indexlist   = self.myData.getAliveAgentIndexList()
            if (self.attacked == -1 and self.myguard in indexlist and self.isSeerguard):
                self.isGJ   = True
                self.myseer = self.myguard

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

        return super(Bodyguard,self).vote()

    def finish(self):
        super(Bodyguard,self).finish()

    def guard(self):

        reset = False

        #GJ優先
        if(self.isGJ == True):

            #他の占い師が噛まれていればGJ解除
            if(self.attacked in self.yesterdaySeer):
                self.isGJ   = False
                reset       = True

            #生存していれば引き続き護衛
            if(self.myguard in self.myData.getAliveAgentIndexList()):
                return self.myguard
            else:
                self.isGJ = False

        #占い真置き後、新たな占いが出たら真置き解除
        if(len(self.yesterdaySeer) < len(self.myData.getAliveSeerCOAgentList()) and self.isRealSeer):
            self.isRealSeer = False

        self.yesterdatSeer = self.myData.getAliveSeerCOAgentList()
        self.isSeerguard = False

        #占い師護衛優先
        if(len(self.myData.getAliveSeerCOAgentList()) > 0):
            guardList   = self.myData.getAliveSeerCOAgentList()

            #真置きしていればそいつ
            if(self.isRealSeer and self.myData.isAliveIndex(self.myseer) and not reset):
                self.myguard = self.myseer
                self.isSeerguard = True
                return self.myguard

			#真占が出ていなければ最も村らしいものを選ぶ
            elif(not self.isRealSeer and not reset):
                self.myguard    = self.myData.getMaxLikelyVill(guardList)
                self.isSeerguard = True
                return self.myguard

        #他占い噛みによるGJ解除後(reset == true)は守らない

        #霊媒護衛
        elif(len(self.myData.getAliveMediumCOAgentList()) > 0):
            guardList   = self.myData.getAliveMediumCOAgentList()
            self.myguard    = self.myData.getMaxLikelyVill(guardList)
            return self.myguard

        #その他白出し
        elif(len(self.myWhite) > 0):
            guardList   = self.myWhite
            self.myguard    = self.myData.getMaxLikelyVill(guardList)
            return self.myguard

        #その他村人
        if(len(self.candidate) > 0):
            target  = self.myData.getMaxLikelyVill(self.candidate)
            return target

        return super(Bodyguard,self).guard()

    #偽以外で占いCOが1人なら進行
    def BeliefSeer(self):
        fake_m,fake_s       = self.o_fakeSearch(3)
        myfake   = self.s_fakeSearch(self.agentIdx)

        smap        = self.myData.getseerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()

        if(len(seerlist) > 1):

            seerlist.removeAll(fake_s)
            seerlist.removeAll(myfake)

            if(not self.isRealSeer):
                if(len(seerlist) == 1):
                    self.myseer     = seerlist[0]
                    self.isRealSeer = True

            if(self.isRealSeer):
                if(len(seerlist) > 1):
                    self.isRealSeer = False
                    self.myseer     = None
                    del self.myWhite[:]
                    del self.myBlack[:]

                elif(len(seerlist) == 1):
                    result = smap[seerlist[0]]
                    self.myseer = seerlist[0]
                    if(result != None):
                        self.myWhite    = result.getWhiteList()
                        self.myBlack    = result.getBlackList()
                        self.candidate.remove(self.myseer)
                        for i in self.myWhite:   self.candidate.remove(i)

    def setmyData(self,mydata):
        self.myData = mydata
