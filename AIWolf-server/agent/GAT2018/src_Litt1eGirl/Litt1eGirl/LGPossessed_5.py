# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import math
import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Possessed(bp.BasePossessed):

    def __init__(self, agent_name):
        super(Possessed,self).__init__(agent_name)
        self.willvote   = None
        self.realSeer   = None
        self.werewolf   = None
        self.maybewolf  = None

        self.divineList = []
        self.whiteList = []
        self.seerList   = []

        self.divined    = {}
        self.result     = {}
        self.isCO       = False
        self.isDivined  = False
        self.isBlack    = False

    def getName(self):
        return self.agent_name

    def initialize(self, game_info, game_setting):
        super(Possessed,self).initialize(game_info, game_setting)
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)
        self.divineList = self.myData.getAliveAgentIndexList()
        self.divineList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Possessed,self).update(game_info, talk_history, whisper_history, request)

        self.seerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.seerList:
            self.seerList.remove(self.agentIdx)
        self.seerCONum = len(self.seerList)

        #COがあったとき
        if(self.myData.isCOCall()):
            self.realSeer = None

            #自分以外の占い師が1人(占い師)
            if self.seerCONum == 1:
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)
                self.realSeer = seer

            #自分以外の占い師が2人以上->seerCOの中に狼と決め打ち
            if self.seerCONum == 2:
                newlist = []
                self.realSeer = None
                for seer in self.seerList:
                    if seer in self.seerList:
                        newlist.append(seer)
                self.grayList = newlist

        #DIVINEDがあったとき
        if(self.myData.isDivineCall()):

            seerMap   = self.myData.getSeerCODataMap()

            if self.seerCONum == 1:
                seer    = self.seerList[0]
                v       = seerMap[seer]

                black   = v.getBlackList()
                white   = v.getWhiteList()
                if(len(black)>0):
                    self.werewolf = black[0]
                if(len(white)>0):
                    for i in white:
                        if not i in self.whiteList:
                            self.whiteList.append(i)

            if self.seerCONum == 2:

                for k,v in seerMap.items():

                    #自分への黒出し=人狼と仮定
                    black   = v.getBlackList()
                    if(self.agentIdx in black): self.werewolf = k
                    if(not black in self.myData.getAliveAgentList()): self.werewolf = k

                    #占い師以外への黒=人狼の偽黒出しと決め打ち

                    #人狼が確定していればもう片方を真占いと確定
                    for i in self.seerList:
                        if(self.werewolf != None and self.werewolf != i):
                            self.realSeer = i

                    #占い師への黒=真占い師の人狼への黒出しと決め打ち

                    #占い師への白=真占い師確定

                    #占い師以外への白=白と借り置く

            if(self.werewolf in self.candidate):
                self.candidate.remove(self.werewolf)

        if(len(self.myData.getWerewolfCOAgentList()) > 0):
            for i in self.myData.getWerewolfCOAgentList():
                if( i not in self.whiteList and i != self.werewolf):
                    self.maybewolf = i

    def dayStart(self):
        super(Possessed,self).dayStart()

        self.willvote   = None

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.whiteList):self.whiteList.remove(execute)
            if(attacked in self.whiteList):    self.whiteList.remove(attacked)

        self.isDivined = False


    def talk(self):

        #COTrigerがTrueならCO
        if(self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #初日，CO後に占い先確定
        # if(self.isCO and not self.isDivined):
            # self.isDivined = True
            # self.MyDivine()

        #COしたらdivined
        rand_x = random.randrange(10)
        threshhold = int(20/math.pi*math.atan(self.myData.getTurn()-1))
        rand_y = random.randrange(
            threshhold if threshhold > 1 else 1 )
        if(self.isCO and rand_x <= rand_y and not self.isDivined):
            self.isDivined = True
            self.MyDivine()
            if(len(self.result) > 0):
                target  = self.result.keys()[0]
                result  = self.result[target]
                del self.result[target]
                return ttf.divined(target,result)

        #VOTE宣言
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

        target = self.MyTarget()
        if(target != None):
            return target

        return self.agentIdx

    def finish(self):
        return None

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):
        #1日目
        if(self.myData.getToday()==1):
            if(self.seerCONum==1):
                #自分の黒出しに投票
                if(self.isBlack):
                    target = self.divined.keys()[0]
                    if(self.myData.isAliveIndex(target)):   return target
                #占い師の白出しに投票
                if(len(self.whiteList)>0):
                    target = self.whiteList[0]
                    if(self.myData.isAliveIndex(target)):   return target
                #真占い師に投票
                if(self.realSeer != None and self.myData.isAliveIndex(self.realSeer)):
                    target = self.realSeer
                    if(self.myData.isAliveIndex(target)):   return target

            if(self.seerCONum==2):
                #人狼占い師の黒出しに投票(未実装)
                #真占い師に投票
                if(self.realSeer != None):
                    target = self.realSeer
                    if(self.myData.isAliveIndex(target)):   return target
                #占い師以外に投票
                else:
                    target = self.myData.getMaxLikelyVill(self.grayList)
                    if(self.myData.isAliveIndex(target)):   return target

        #2日目
        if(self.myData.getToday()==2):
            #人狼が確定していれば除外
            if(self.werewolf in self.candidate):
                self.candidate.remove(self.werewolf)
            #maybe人狼が確定していれば除外
            if(len(self.candidate) > 1 and self.maybewolf in self.candidate):
                self.candidate.remove(self.maybewolf)

            #占い師が1人
            if(self.seerCONum==1):
                #占い師が生きていれば投票
                if (self.myData.isAliveIndex(self.seerList[0])):
                    target = self.seerList[0]
                    return target
                #占い師が死んでいるとき
                else:
                    #確定白に投票
                    if(len(self.whiteList)>0):
                        target = self.whiteList[0]
                        if(self.myData.isAliveIndex(target)):
                            return target

            #占い師が2人
            if(self.seerCONum==2):

                #占い師1名生存(=人狼＆村人)
                if(len(self.myData.getAliveSeerCOAgentList())==1):
                    for i in self.seerList:
                        if i in self.candidate: self.candidate.remove(i)
                        if(len(self.candidate) > 0):  return self.candidate[0]

                #占い師2名生存(=人狼＆占い師)
                else:
                    #人狼確定時
                    if(len(self.candidate) > 0):
                        return self.candidate[0]

                    #人狼未確定
                    target = self.myData.getMaxLikelyVillAll()
                    return target


        target = self.myData.getMaxLikelyVill(self.candidate)
        return target

    def MyDivine(self):
        #占い師が1人
        if(self.seerCONum == 1):
            rand_x = random.randrange(10)
            rand_y = random.randrange(3)
            rand_z = random.randrange(6)

            if(self.myData.getSeerCODataMap().has_key(self.realSeer)):
                afterSayTarget = (self.myData.getSeerCODataMap()[self.realSeer]).getResult(self.myData.getToday())
            else:
                afterSayTarget = None

            # 真占に便乗(50%程度)
            if (afterSayTarget is not None and afterSayTarget.keys()[0] in self.divineList and rand_y >= rand_z):
                self.divineList.remove(afterSayTarget.keys()[0])
                if(afterSayTarget[afterSayTarget.keys()[0]] is False):
                    self.divined.update({afterSayTarget.keys()[0]: "HUMAN"})
                    self.result.update({afterSayTarget.keys()[0]: "HUMAN"})
                else:
                    self.divined.update({afterSayTarget.keys()[0]: "WEREWOLF"})
                    self.result.update({afterSayTarget.keys()[0]: "WEREWOLF"})

            #低確率で占い師に白出し
            elif(rand_x < rand_y and self.seerList[0] in self.divineList):
                self.divineList.remove(self.seerList[0])
                self.divined.update({self.seerList[0]:"HUMAN"})
                self.result.update({self.seerList[0]:"HUMAN"})
            #高確率で占い師以外に黒出し
            else:
                target = np.random.permutation(self.divineList)[0]
                while(target in self.seerList):
                    target = np.random.permutation(self.divineList)[0]
                self.isBlack = True
                self.divineList.remove(target)
                self.divined.update({target:"WEREWOLF"})
                self.result.update({target:"WEREWOLF"})

        #占い師が2人
        elif(self.seerCONum == 2):
            rand_z = random.randrange(2)
            #中確率で占い師に白出し
            if(rand_z == 0):
                target = np.random.permutation(self.seerList)[0]
                if(target in self.divineList):
                    self.divineList.remove(target)
                    self.divined.update({target:"HUMAN"})
                    self.result.update({target:"HUMAN"})
            #中確率で占い師以外に黒出し
            else:
                target = np.random.permutation(self.seerList)[0]
                while(target in self.seerList):
                    target = np.random.permutation(self.divineList)[0]
                self.isBlack = True
                if(target in self.divineList):
                    self.divineList.remove(target)
                    self.divined.update({target:"WEREWOLF"})
                    self.result.update({target:"WEREWOLF"})

        #ランダムに黒出し
        else:
            self.isBlack = True
            target = np.random.permutation(self.divineList)[0]
            self.divineList.remove(target)
            self.divined.update({target:"WEREWOLF"})
            self.result.update({target:"WEREWOLF"})

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
