#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn,copy,random
import pandas as pd
import MyData as md

import basepossessed as bp

class Possessed(bp.Possessed):

    def __init__(self, agent_name):
        super(Possessed,self).__init__(agent_name)
        self.willvote   = None
        self.realSeer   = None

        #偽確定リスト
        self.fakeSeer   = []
        self.fakeMedium = []

        self.werewolves = []
        self.divineList = []
        self.myDivine   = []
        self.divined    = {}
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Possessed,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Possessed,self).initialize(game_info, game_setting)
        self.playerNum  = game_setting['playerNum']
        self.divineList = self.myData.AgentToIndexList(self.myData.getAliveAgentList())#占い候補リスト
        self.divineList.remove(self.agentIdx)
        self.blacktimes = 0
        self.aliveBlack = []
        self.aliveWhite = []

    def dayStart(self):
        super(Possessed,self).dayStart()
        target , result = self.PossessedDivine()
        self.myDivine.append(target)
        self.willvote   = None

        for i in self.aliveBlack:
            if(not i in self.myData.getAliveAgentIndexList()):   self.aliveBlack.remove(i)
        for i in self.aliveWhite:
            if(not i in self.myData.getAliveAgentIndexList()):   self.aliveWhite.remove(i)
            if(self.myData.getToday > 4 and i in self.candidate):    self.candidate.remove(i)

    def talk(self):

        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger):
            self.isCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.myDivine) > 0):
            target  = self.myDivine[0]
            result  = self.divined[target]
            self.myDivine.pop()
            return ttf.divined(target,result)

        #偽探し
        fake_m,fake_s   = self.o_fakeSearch(3)              #明らかに偽者
        self.checkseer()        #真占調査
        self.myfakeSearch()     #主観的偽者

        #VOTE宣言(黒出し、客観的偽、対抗、最黒)
        if(len(self.aliveBlack) > 0):
            if(self.Target(self.aliveBlack)):      return ttf.vote(self.willvote)
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return ttf.vote(self.willvote)
        elif(self.realSeer != None):
            if(self.willvote != self.realSeer and self.myData.isAliveIndex(self.realSeer)):
                self.willvote = self.realSeer
                return ttf.vote(self.willvote)
        else:
            if(self.Target(self.candidate)):    return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate()
            if(talk != None):   return talk

        return ttf.over()

    def vote(self):

        fake_m,fake_s   = self.o_fakeSearch(3)              #明らかに偽者
        self.checkseer()        #真占調査
        self.myfakeSearch()     #主観的偽者

        self.willvote = None

        #黒出し、客観的偽、対抗、最黒
        if(len(self.aliveBlack) > 0):
            if(self.Target(self.aliveBlack)):      return self.willvote
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return self.willvote
        elif(self.realSeer != None):
            if(self.myData.isAliveIndex(self.realSeer)):
                return self.realSeer
        else:
            if(self.Target(self.candidate)):    return self.willvote

        return super(Possessed,self).vote()

    def PossessedDivine(self):

        #占い対象決定
        if(self.myData.getToday() == 1):
            #1日目対抗COが1人いれば0.1の確率で対抗黒出し
            if(len(self.myData.getSeerCOAgentList()) == 2):
                rand = random.randint(1,10)
                if(rand == 1 and self.realSeer != None):
                    self.divineList.remove(self.realSeer)
                    self.divined.update({self.realSeer:'WEREWOLF'})
                    self.aliveBlack.append(self.realSeer)
                    return self.realSeer,'WEREWOLF'

        rand = random.randint(1,10)

        #7割最白に黒出し(3回まで)、3割最黒に白出し
        if(rand > 3 and self.blacktimes < 3):
            target = self.myData.getMaxLikelyVill(self.divineList)
            self.divineList.remove(target)
            self.divined.update({target:'WEREWOLF'})
            self.aliveBlack.append(target)
            self.blacktimes += 1
            return target,'WEREWOLF'
        else:
            target = self.myData.getMaxLikelyWolf(self.divineList)
            self.divineList.remove(target)
            self.divined.update({target:'HUMAN'})
            self.aliveWhite.append(target)
            return target,'HUMAN'

    #COするか否か
    def COTrigger(self):
        #ひとまず初日1ターン目CO
        #Trigger条件はここで調整可能。
        return True

    #自分以外の占いＣＯがいれば真占とする
    def checkseer(self):
        seerlist = copy.deepcopy(self.myData.getSeerCOAgentList())
        seerlist.remove(self.agentIdx)
        if(len(seerlist) == 1):
            self.realSeer = seerlist[0]

    #偽者調査
    def myfakeSearch(self):

        #偽占い師=狼探し
        seerlist = copy.deepcopy(self.myData.getSeerCOAgentList())

        if(self.agentIdx in seerlist):  seerlist.remove(self.agentIdx)
        if(self.realSeer in seerlist):  seerlist.remove(self.realSeer)

        if(len(seerlist) > 0):
            smap = self.myData.getSeerCODataMap()

            for i in seerlist:

                #真占確定時、自分と真占以外の占い師は狼
                if(self.realSeer != None):
                    if i not in self.fakeSeer:
                        self.fakeSeer.append(i)
                        self.werewolves.append(i)

                #真占い未確定時、自分に黒出しした占い師は狼
                #真占の虚偽発言は考慮しない
                else:
                    mymap   = smap[i]
                    black   = mymap.getBlackList()
                    if(self.agentIdx in black and i not in self.fakeSeer):
                        self.fakeSeer.append(i)
                        self.werewolves.append(i)

        #偽霊媒=狼探し
        mmap = self.myData.getMediumCODataMap()

        if(len(mmap) > 0 and self.realSeer != None):
            allmap  = self.myData.getSeerCODataMap()
            smap    = allmap[self.realSeer]
            black_s = smap.getBlackList()
            white_s = smap.getWhiteList()

            for k,v in mmap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #真占の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in white_s and i not in self.fakeMedium):
                            self.fakeMedium.append(k)
                            self.werewolves.append(k)

                #真占の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in black_s and i not in self.fakeMedium):
                            self.fakeMedium.append(k)
                            self.werewolves.append(k)


    def finish(self):
        super(Possessed,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
