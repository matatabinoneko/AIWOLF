# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md

import Baseplayers as bp

class Medium(bp.BaseMedium):

    def __init__(self, agent_name):
        super(Medium,self).__init__(agent_name)
        self.willvote   = None
        self.isCall     = True

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Medium,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Medium,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Medium,self).dayStart()
        self.willvote   = None
        self.isCall     = True

    def talk(self):

        self.myfakeSearch()

        #COTrigerがTrueならCO
        if(self.isCall):
            if(self.COTrigger()):
                return ttf.comingout(self.agentIdx,self.role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.willSay) > 0):
            target = self.willSay[0]['target']
            result = self.willSay[0]['result']
            self.willSay.pop(0)
            return ttf.identified(target,result)

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
            return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        return ttf.over()

    def vote(self):

        fake_m,fake_s       = self.o_fakeSearch()
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

        if(self.isCO):  return False
        self.isCall = False

        #2日目黒で必ずCO
        if(self.myData.getToday() == 2):
            self.isCall     = False
            if(len(self.myBlack) > 0):
                self.isCO = True
                return True
            else:
                rand = random.randrange(2)
                if(rand < 1):
                    self.isCO = True
                    return True
                else:
                    return False

        #3日目必ずCO
        if(self.myData.getToday() == 3):
            self.isCO = True
            return True
        return False

    def finish(self):
        super(Medium,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):
        #偽探し
        fake_m,fake_s       = self.o_fakeSearch()
        self.myfakeSearch()
        #VOTE宣言(客観的偽、主観的偽、最黒)
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            target = self.Target(fake)
            return target

        elif(len(self.fakeSeer) > 0 or len(self.fakeMedium)):
            myfake = self.fakeSeer + self.fakeMedium
            target = self.Target(myfake)
            return target

        else:
            target = self.Target(self.candidate)
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
