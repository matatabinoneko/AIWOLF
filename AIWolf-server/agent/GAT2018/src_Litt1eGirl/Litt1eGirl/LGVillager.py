# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md
import Baseplayers as bp

class Villager(bp.BaseVillager):

    def __init__(self, agent_name):
        super(Villager,self).__init__(agent_name)
        self.isCall     = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Villager,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Villager,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Villager,self).dayStart()


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

        #BGCO
        myfake          = self.s_fakeSearch(self.agentIdx)
        if(len(myfake) > 0 and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')

        return ttf.over()

    def vote(self):

        target = self.MyTarget()
        if(target != None): return target

        target = self.getMaxLikelyWolfAll()
        if(target != None): return target

        return super(Villager,self).vote()

    def finish(self):
        super(Villager,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):

        fake_m,fake_s   = self.o_fakeSearch()
        myfake          = self.s_fakeSearch(self.agentIdx)

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
