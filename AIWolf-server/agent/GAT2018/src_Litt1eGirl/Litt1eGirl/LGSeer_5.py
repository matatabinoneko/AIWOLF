# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import math
import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Seer(bp.BaseSeer):

    def __init__(self, agent_name):
        super(Seer,self).__init__(agent_name)
        self.willvote   = None
        self.possessed  = None
        self.werewolf   = None

        self.divineList = []
        self.whiteList  = []
        self.grayList   = []

        self.divined    = []
        self.result     = {}
        self.isCO       = False
        self.isDivined  = False
        self.isBlack      = False


    def initialize(self, game_info, game_setting):
        super(Seer,self).initialize(game_info, game_setting)
        self.divineList = self.myData.getAliveAgentIndexList()
        self.divineList.remove(self.agentIdx)
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Seer,self).update(game_info, talk_history, whisper_history, request)

        self.fakeseerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.fakeseerList:
            self.fakeseerList.remove(self.agentIdx)
        self.seerCONum = len(self.fakeseerList)

        #COがあったとき
        if(self.myData.isCOCall()):

            #自分以外の占い師が1人(狂人)
            if self.seerCONum == 1:
                fake = self.fakeseerList[0]
                if fake in self.grayList:
                    self.grayList.remove(fake)
                self.possessed = fake

            #自分以外の占い師が2人(狂人と人狼)
            if self.seerCONum == 2:

                gray = None

                for i in self.fakeSeer:
                    #片方から黒が出た場合
                    if (self.werewolf != None and self.werewolf != i):
                        self.possessed = i

                    #片方から白が出た場合
                    if (i in self.whiteList):   self.possessed = i
                    else:                       gray = i

                #狂人確定で人狼確定
                if(self.possessed != None and self.werewolf == None):
                    if(gray != None):
                        self.werewolf = gray

                #両方未確定->グレー置き
                else:   self.grayList = self.fakeSeer

    def dayStart(self):
        super(Seer,self).dayStart()
        self.willvote   = None
        self.isDivined = False
        divined  = self.gameInfo['divineResult']

        #占い結果処理
        if(divined != None):
            self.divined.append(divined)
            target  = divined['target']
            result  = divined['result']
            self.result.update({target:result})

            if(result == 'HUMAN'):
                self.whiteList.append(target)
                if(target in self.grayList):  self.grayList.remove(target)
            else:
                self.werewolf   = target
                self.isBlack      = True

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.divineList):self.divineList.remove(execute)
            if(attacked in self.divineList):    self.divineList.remove(attacked)
            if(execute in self.grayList):self.grayList.remove(execute)
            if(attacked in self.grayList):    self.grayList.remove(attacked)


    def talk(self):
        #COTrigerがTrueならCO
        if(self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #COしたらdivined
        rand_x = random.randrange(10)
        threshhold = int(20 / math.pi * math.atan(self.myData.getTurn() - 1))
        rand_y = random.randrange(
            threshhold if threshhold > 1 else 1)
        if (self.isCO and rand_x <= rand_y and not self.isDivined and len(self.result) > 0):
            self.isDivined = True
            target = self.result.keys()[0]
            result = self.result[target]
            del self.result[target]
            return ttf.divined(target, result)

        #VOTE宣言
        if(self.willvote != self.MyTarget()):
            self.willvote = self.MyTarget()
            return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        return ttf.over()


    def vote(self):
        if(self.myData.getToday() == 1):
            target = self.MyTarget()
            if(target != None):
                return target

        return super(Seer,self).vote()

    def divine(self):

        #初日はランダム
        if(self.myData.getToday()==0):
            perm = np.random.permutation(self.divineList)
            target = perm[0]
            self.divineList.remove(target)
            return target

        #2日目はif-then
        if(self.myData.getToday()==1):
            #黒出ししていたらランダム
            if(self.isBlack):
                perm = np.random.permutation(self.divineList)
                target = perm[0]
                self.divineList.remove(target)
                return target
            #黒出ししていないが人狼が特定できていればそいつ
            elif(self.werewolf != None):
                target = self.werewolf
                self.divineList.remove(target)
                return target
            #グレーから
            elif(len(self.grayList) > 0):
                target = self.myData.getMaxLikelyWolf(self.grayList)
                self.divineList.remove(target)
                return target

        #例外対策
        return self.agentIdx

    def finish(self):
        return super(Seer,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):

        #自分の黒出しに投票
        if(self.isBlack):
            target = self.werewolf
            if(self.myData.isAliveIndex(target)):   return target
        #グレーリストから投票
        if(len(self.grayList)>0):
            target = self.myData.getMaxLikelyWolf(self.grayList)
            if(self.myData.isAliveIndex(target)):   return target

        target = self.myData.getMaxLikelyWolf(self.candidate)
        return target

    def COTrigger(self):
        rand_x = random.randrange(10)
        threshhold = int(20 / math.pi * math.atan(self.myData.getTurn()))
        rand_y = random.randrange(
            threshhold if threshhold > 1 else 1)
        if (self.isCO or rand_x <= rand_y):
            return False
        else:
            self.isCO = True
            return True

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y or not self.isDivined):
            rand = random.randint(1,3)
            if(rand > 1):
                target = self.MyTarget()
                return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None
