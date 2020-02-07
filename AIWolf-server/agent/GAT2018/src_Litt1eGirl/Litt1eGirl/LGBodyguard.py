# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md

import Baseplayers as bp

class Bodyguard(bp.BaseBodyguard):

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

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
            if(self.willvote == 0):
                print("here_vill")
            return ttf.vote(self.willvote)


        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        '''
        #BGCO
        if(len(myfake) > 0 and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')
        '''

        return ttf.over()

    def vote(self):

        self.willvote = None
        #偽探し
        fake_m,fake_s       = self.o_fakeSearch()
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
        exe= self.gameInfo["latestExecutedAgent"]
        if exe in self.candidate:   self.candidate.remove(exe)
        
        #GJ優先
        if(self.isGJ == True):

            #他の占い師が噛まれていればGJ解除
            if(self.attacked in self.yesterdaySeer):
                self.isGJ   = False
                reset       = True

            #生存していれば引き続き護衛
            if(self.myguard in self.myData.getAliveAgentIndexList() and self.myguard != exe):
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
                if(self.myguard != exe):
                    return self.myguard

			#真占が出ていなければ最も村らしいものを選ぶ
            if(not self.isRealSeer and not reset):
                self.myguard    = self.myData.getMaxLikelyVill(guardList)
                self.isSeerguard = True
                if(self.myguard != exe):
                    return self.myguard

        #他占い噛みによるGJ解除後(reset == true)は守らない

        #霊媒護衛
        if(len(self.myData.getAliveMediumCOAgentList()) > 0):
            guardList   = self.myData.getAliveMediumCOAgentList()
            self.myguard    = self.myData.getMaxLikelyVill(guardList)
            if(self.myguard != exe):
                return self.myguard

        #その他白出し
        if(len(self.myWhite) > 0):
            guardList   = self.myWhite
            self.myguard    = self.myData.getMaxLikelyVill(guardList)
            if(self.myguard != exe):
                return self.myguard

        #その他村人
        if(len(self.candidate) > 0):
            target  = self.myData.getMaxLikelyVill(self.candidate)
            return target

        return super(Bodyguard,self).guard()

    #偽以外で占いCOが1人なら進行
    def BeliefSeer(self):
        fake_m,fake_s       = self.o_fakeSearch()
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

    def MyTarget(self):

        fake_m,fake_s       = self.o_fakeSearch()
        myfake   = self.s_fakeSearch(self.agentIdx)

        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            for i in fake:
                if(not self.myData.isAliveIndex(i)):    fake.remove(i)
            if(len(fake) > 0):
                target = self.myData.getMaxLikelyWolf(fake)
                return target
        if(len(myfake) > 0):
            for i in myfake:
                if(not self.myData.isAliveIndex(i)):    myfake.remove(i)
            if(len(myfake) > 0):
                target = self.myData.getMaxLikelyWolf(myfake)
                return target
        if(len(self.candidate) > 0):
            target = self.myData.getMaxLikelyWolf(self.candidate)
            return target

        target  = self.myData.getMaxLikelyWolf(self.myData.getAliveAgentIndexList())
        return target

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,3)
            if(rand > 1):
                return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None
