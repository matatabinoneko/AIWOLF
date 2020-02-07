#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn,copy
import pandas as pd
import copy,random
import MyData as md

class Villager(object):

    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.Role       = 'VILLAGER'
        self.myData     = md.MyData()
        self.gameInfo   = None
        self.candidate  = []    #自分以外の生存エージェント

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        self.gameInfo = game_info

    def initialize(self, game_info, game_setting):
        self.agentIdx   = game_info['agent']
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'
        self.gameInfo   = game_info

    def dayStart(self):
        #投票候補リストの更新 (生存者-自分)
        self.candidate = self.myData.AgentToIndexList(self.myData.getAliveAgentList())
        self.candidate.remove(self.agentIdx)
        return None

    def talk(self):
        return ttf.over()

    def vote(self):
        target      = self.myData.getMaxLikelyWolfAll()
        return target

    def divine(self):
        return self.agentIdx

    def guard(self):
        return self.agentIdx

    def finish(self):
        self.myData.finish()


    #客観的偽者調査
    def o_fakeSearch(self,werewolves):

        #偽霊媒師探し
        mmap        = self.myData.getMediumCODataMap()
        mediumlist  = self.myData.getMediumCOAgentList()
        fakeMedium  = []

        if (len(mediumlist) > 0):
            #人狼数以上の黒出し
            for k,v in mmap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #黒を人狼数以上出していたら偽者
                if(len(black) > werewolves):    fakeMedium.append(k)

                #生存者に結果を出していたら偽者
                result = v.getKnownList()
                for i in result.keys():
                    if(i in self.myData.getAliveAgentList()):
                        fakeMedium.append(k)

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []

        if(len(seerlist) > 0):
            #人狼数以上の黒出し
            for k,v in smap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #黒を人狼数以上出していたら偽者
                if(len(black) > werewolves):    fakeMedium.append(k)

            #2日目以降、霊媒師1人のときに、霊媒と異なる結果であれば偽者
            if(len(mediumlist) == 1):
                for k,v in smap.items():
                    med_black   = v.getBlackList()
                    med_white   = v.getWhiteList()

                for k,v in smap.items():
                    black   = v.getBlackList()
                    white   = v.getWhiteList()

                    #占い師の白出しに霊媒師が黒を出していたら偽者
                    if(len(white) > 0):
                        for i in white:
                            if(i in med_black): fakeSeer.append(k)

                    #占い師の黒出しに霊媒師が白を出していたら偽者
                    if(len(black) > 0):
                        for i in white:
                            if(i in med_white): fakeSeer.append(k)

        return fakeMedium, fakeSeer

    #主観的偽者調査
    def s_fakeSearch(self,me):

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []

        if(len(seerlist) > 0):
            #自分に黒を出していたら偽者
            for k,v in smap.items():
                black   = v.getBlackList()
                if(me in black):    fakeSeer.append(k)

        return fakeSeer

    #一定確率で予想を言う(20%)
    def Estimate(self):
        rand = random.randint(1,10)
        if(rand < 3):
            rand = random.randint(1,3)
            if(rand > 1 and len(self.candidate) > 0):
                target = self.myData.getMaxLikelyWolf(self.candidate)
                if(target != None):
                    return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None):
                    return ttf.estimate(target,'VILLAGER')
        return None

    #talkやvoteのtaergetを決める
    def Target(self,mylist):

        for i in mylist:
            if(not i in self.myData.getAliveAgentIndexList()):
                mylist.remove(i)

        if(len(mylist) > 0):
            if(self.willvote != self.myData.getMaxLikelyWolf(mylist) ):
                self.willvote = self.myData.getMaxLikelyWolf(mylist)
                return True

        return False

    def setmyData(self,mydata):
        self.myData = mydata
