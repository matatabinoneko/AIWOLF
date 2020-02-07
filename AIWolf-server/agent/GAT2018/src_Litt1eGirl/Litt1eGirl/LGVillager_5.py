# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Villager(bp.BaseVillager):

    def __init__(self, agent_name):
        super(Villager,self).__init__(agent_name)
        self.fakeList   = []
        self.grayList   = []
        self.whiteList  = []
        self.targetList = []

        self.realSeer   = None
        self.possessed  = None
        self.werewolf   = None

        self.maybe_black    = []
        self.maybe_white    = []
        self.isCO       = False
        self.isWWCO     = False

        self.divineMap = {}

    def getName(self):
        return self.agent_name

    def initialize(self, game_info, game_setting):
        super(Villager,self).initialize(game_info, game_setting)
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)
        self.targetList = self.myData.getAliveAgentIndexList()
        self.targetList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Villager,self).update(game_info, talk_history, whisper_history, request)

        self.seerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.seerList:
            self.seerList.remove(self.agentIdx)
        self.seerCONum = len(self.seerList)

        #COがあったとき
        if(self.myData.isCOCall()):
            self.realSeer = None

            #占い師が1人(占い師)
            if self.seerCONum == 1:
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)
                self.realSeer = seer

            #占い師が2人(占い師と狂人)
            if self.seerCONum == 2:
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)

            #占い師が3人(占い師と狂人と人狼)
            if self.seerCONum == 3:
                newlist = []
                self.realSeer = None
                for seer in self.seerList:
                    if seer in self.seerList:
                        newlist.append(seer)
                self.grayList = newlist

        #DIVINEDがあったとき
        if(self.myData.isDivineCall()):

            seerMap   = self.myData.getSeerCODataMap()
            self.whiteList  = []
            self.maybe_black=[]
            self.maybe_white=[]

            if self.seerCONum == 2:

                self.fakeList = self.fakeSearch()
                if(self.seerCONum-len(self.fakeList)==1):
                    if(self.seerList[0]==self.fakeList):
                        self.realSeer = self.seerList[1]
                        self.possessed= self.fakeList[0]
                    else:
                        self.realSeer = self.seerList[0]
                        self.possessed= self.fakeList[0]

            if self.seerCONum == 3:

                self.fakeList = self.fakeSearch()
                if(self.seerCONum-len(self.fakeList)==1):
                    for i in self.seerList:
                        if(not i in self.fakeList):
                            self.realSeer = i

            #真占い確定時
            if(self.realSeer != None):
                seer    = self.seerList[0]
                v       = seerMap[seer]
                black   = v.getBlackList()
                white   = v.getWhiteList()
                if(len(black)>0):
                    self.werewolf = black[0]
                if(len(white)>0):
                    for i in white:
                        self.whiteList.append(i)
            else:
                seerMap   = self.myData.getSeerCODataMap()
                for k,v in seerMap.items():
                    if not k in self.fakeList:
                        black   = v.getBlackList()
                        white   = v.getWhiteList()
                        if(len(black) > 0):
                            self.maybe_black.append(black[0])
                        if(len(white) > 0):
                            for i in white:
                                self.maybe_white.append(i)

    def dayStart(self):
        super(Villager,self).dayStart()
        self.targetList = self.myData.getAliveAgentIndexList()
        self.targetList.remove(self.agentIdx)
        self.willvote   = None

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.whiteList):  self.whiteList.remove(execute)
            if(attacked in self.whiteList): self.whiteList.remove(attacked)
            if(execute in self.grayList):  self.grayList.remove(execute)
            if(attacked in self.grayList): self.grayList.remove(attacked)


    def talk(self):

        if(self.COTrigger()):
            if(self.isCO):
                return ttf.comingout(self.agentIdx,'SEER')
            else:
                return ttf.comingout(self.agentIdx,'VILLAGER')

        #2日目：狂人占い師確定でWWCO
        if(self.myData.getToday() > 1 and self.WWCOTrigger()):
            return ttf.comingout(self.agentIdx,'WEREWOLF')

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
        target = self.MyTarget()
        if(target != None):
            return target

        return self.agentIdx

    def finish(self):
        return None

    def setmyData(self,mydata):
        self.myData = mydata

    #COTrigger
    def COTrigger(self):

        #SEERCOしていたら次ターンで撤回
        if(self.isCO):
            self.isCO = False
            return True

        #SEERCOするかどうかの判定
        if(self.myData.getToday() == 0 and self.myData.getTurn() < 2):
            rand_x = random.randrange(10)
            rand_y = random.randrange(2)
            if(rand_x < rand_y):
                self.isCO = True
                return True

        return False

    #偽者調査
    def fakeSearch(self):

        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []
        werewolves  = 1

        if(len(seerlist) > 0):
            #人狼数以上の黒出し
            for k,v in smap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #自分に黒を出していたら偽者
                if(self.agentIdx in black):            fakeSeer.append(k)

                if(len(black) > 0 and k not in fakeSeer):
                    #黒を人狼数以上出していたら偽者
                    if(len(black) > werewolves):
                        fakeSeer.append(k)

                    #黒出しが死んでいたら偽物
                    if(not self.myData.isAliveIndex(black[0])):
                        fakeSeer.append(k)

        return fakeSeer

    def MyTarget(self):

        #1日目
        if(self.myData.getToday() == 1):

            #真占い師確定時
            if(self.realSeer != None):

                #真占い師の黒出しに投票
                if(self.werewolf != None):
                    target = self.werewolf
                    if(self.myData.isAliveIndex(target)):   return target

                #真占い師の白出しを除外して占い師以外からランダム投票
                if(len(self.whiteList)>0):
                    target = self.whiteList[0]
                    if(self.myData.isAliveIndex(target)):   return target

            elif(self.seerCONum==2):
                #占い師以外への黒出しに投票
                if(len(self.maybe_black)>0):
                    for i in self.maybe_black:
                        if not i in self.seerList:
                            target = self.realSeer
                            if(self.myData.isAliveIndex(target)):   return target

                #占い師以外に投票
                for i in self.seerList:
                    if i in self.grayList:  self.grayList.remove(i)

                #白出しも抜いてみる
                if(len(self.maybe_white)>0):
                    copy_list   = copy.deepcopy(self.grayList)
                    for i in self.maybe_white:
                        if i in copy_list:  copy_list.remove(i)
                    if(len(copy_list)>0):
                        target = self.myData.getMaxLikelyWolf(copy_list)
                        if(self.myData.isAliveIndex(target)):   return target

                if(len(self.grayList)>0):
                    target = self.myData.getMaxLikelyWolf(self.grayList)
                    if(self.myData.isAliveIndex(target)):   return target

            elif(self.seerCONum==3):
                #偽確定がいればその中から選ぶ
                if(len(self.fakeList)>0):
                    target  = self.myData.getMaxLikelyWolf(self.fakeList)
                    return target

                #占い師以外に黒を出している占い師は怪しい
                seerMap   = self.myData.getSeerCODataMap()
                maybe_dark= []
                for k,v in seerMap.items():
                    black   = v.getBlackList()
                    if(len(black) > 0 and not black[0] in self.seerList):
                        maybe_dark.append(k)
                if(len(maybe_dark)>0):
                    target  = self.myData.getMaxLikelyWolf(maybe_dark)
                    return target

                #黒判定されている占い師がいれば怪しい
                for k,v in seerMap.items():
                    black   = v.getBlackList()
                    if(len(black) > 0 and black[0] in self.seerList):
                        maybe_dark.append(black[0])
                    if(len(maybe_dark)>0):
                        target  = self.myData.getMaxLikelyWolf(maybe_dark)
                        return target

                #占い師からランダム
                target  = self.myData.getMaxLikelyWolf(self.seerList)
                return target

        #2日目
        if(self.myData.getToday() == 2):

            #真占い師確定時
            if(self.realSeer != None):

                #占い師が一人
                if(self.seerCONum == 1):
                    #占い師が生存
                    if(self.myData.isAliveIndex(self.realSeer)):
                        if(self.realSeer in self.candidate):
                            self.candidate.remove(self.realSeer)
                        if(len(self.candidate)>0):
                            target  = self.myData.getMaxLikelyWolf(self.candidate)
                            return target
                    #占い師が死亡
                    else:
                        if(self.werewolf != None):
                            if(self.myData.isAliveIndex(self.werewolf)):
                                target = self.werewolf
                                return target
                        if(len(self.whiteList) > 0):
                            for i in self.whiteList:
                                if(i in self.candidate):
                                    self.candidate.remove(i)
                        if(len(self.candidate) > 0):
                            target = self.myData.getMaxLikelyWolf(self.candidate)
                            return target

                #占い師が二人
                if(self.seerCONum == 2):
                    #占い師が生存
                    if(self.myData.isAliveIndex(self.realSeer)):
                        if(self.realSeer in self.targetList):
                            self.targetList.remove(self.realSeer)
                        target  = self.myData.getMaxLikelyWolf(self.targetList)
                    #占い師が死亡
                    else:
                        #狂人が生存
                        if(self.myData.isAliveIndex(self.possessed)):
                            target = self.possessed
                        #狂人が死亡
                        else:
                            if(len(self.grayList) > 0):
                                target  = self.myData.getMaxLikelyWolf(self.grayList)
                    return target

                #占い師が三人
                    #占い師が死亡
                        #詰んだ
                    #占い師が生存

            #真占い師未特定
                #占い師が二人
                    #片占い師が死亡

                #占い師が三人
                #占い師二人生存
                #占い師一人生存

        target = self.myData.getMaxLikelyWolf(self.candidate)
        return target

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,3)
            if(rand > 1):
                target = self.MyTarget()
                return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None

    def WWCOTrigger(self):

        if(self.isWWCO):
            return False

        if(self.possessed != None and self.myData.isAliveIndex(self.possessed) and len(self.myData.getAliveSeerCOAgentList()) == 1):
            rand_x  = random.randrange(5)
            self.isWWCO = True
            if(rand_x < 4): return True
            else:           return False
        return False
