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

import basewerewolf as bw

class Werewolf(bw.Werewolf):

    def __init__(self, agent_name):
        super(Werewolf,self).__init__(agent_name)
        self.willvote   = None
        self.seerlist   = []    #狼でも狂人でもない生存占い師リスト
        self.seer       = None  #真占い師
        self.mediumlist = []    #狼でも狂人でもない生存霊媒師リスト
        self.medium     = None  #真霊媒師
        self.bglist     = []    #狼でも狂人でもない生存狩人リスト
        self.possessed  = None  #狂人
        self.isGJ       = False
        self.willattack = None

        self.isCall     = False
        self.blacked    = False #黒出しされたか否か

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Werewolf,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Werewolf,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Werewolf,self).dayStart()
        self.willattack = None

        #GJcheck
        if(self.myData.getToday() > 1):
            result          = self.gameInfo['latestExecutedAgent']
            self.attacked   = self.gameInfo['latestAttackVoteList']
            self.isGJ = False

            #襲撃対象が生存でGJ
            if(result == -1 and self.myData.isAliveIndex(self.attacked)):
                self.isGJ = True

    def talk(self):

        #偽探し
        fake_m,fake_s   = self.o_fakeSearch(3)  #明らかに偽者
        self.checkpossessed()       #狂人調査
        self.checkreal()            #真探し

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return ttf.vote(self.willvote)

        else:
            if(self.Target(self.candidate)):    return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate()
            if(talk != None):   return talk

        #BGCO
        if(self.blacked and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')

        return ttf.over()

    def vote(self):

        fake_m,fake_s   = self.o_fakeSearch(3)              #明らかに偽者
        self.checkpossessed()       #狂人調査
        self.checkreal()            #真探し

        self.willvote = None

        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):   return self.willvote
        else:
            if(self.Target(self.candidate)): return self.willvote

        return super(Werewolf,self).vote()

    def whisper(self):

        #襲撃対象を決定・宣言
        if(self.willattack == None):
            return twf.attack(self.target())

        return twf.over()

    def attack(self):
        exe = self.gameInfo['latestExecutedAgent']

        if(exe in self.aliveVillagers):
            self.aliveVillagers.remove(exe)

        #襲撃宣言対象が生存していればそのプレイヤ
        if(self.willattack != None and self.willattack in self.aliveVillagers):
            return self.willattack

        #襲撃宣言対象が死亡している、あるいはwhisperしていない(lw)場合、targetを決めなおす
        else:
            return self.target()

        return  self.myData.getMaxLikelyVill(aliveVillagers)


    #偽者調査
    def checkpossessed(self):

        #偽占い師=狂人探し
        smap        = self.myData.getSeerCODataMap()

        if len(smap) > 0:
            for k,v in smap.items():
                if(not k in self.werewolves):
                    black   = v.getBlackList()
                    white   = v.getWhiteList()
                    isPossessed = False

                    #村に黒を出していたら偽者
                    if(len(black) > 0):
                        for i in black:
                            if(i in self.villagers):
                                self.possessed = k
                                isPossessed = True

                            if(i == self.agentIdx):
                                self.blacked = True

                    #狼に白を出していたら偽者
                    if(len(white) > 0):
                        for i in white:
                            if(i in self.werewolves):
                                self.possessed = k
                                isPossessed = True

                    if(not isPossessed and self.myData.isAliveIndex(k)):    self.seerlist.append(k)

        #偽霊媒探し　= 狂人
        mmap = self.myData.getMediumCODataMap()

        if len(mmap) > 0:
            for k,v in mmap.items():
                if(not k in self.werewolves):
                    black   = v.getBlackList()
                    white   = v.getWhiteList()
                    isPossessed = False

                    #村に黒を出していたら偽者
                    if(len(black) > 0):
                        for i in black:
                            if(i in self.werewolves):
                                self.possessed = k
                                isPossessed = True

                    #狼に白を出していたら偽者
                    if(len(white) > 0):
                        for i in white:
                            if(i in self.werewolves):
                                self.possessed = k
                                isPossessed = True

                    if(not isPossessed and self.myData.isAliveIndex(k)):    self.seerlist.append(k)

    #狼と狂人以外の占いＣＯがいれば真占とする
    def checkreal(self):

        if(len(self.seerlist) == 1):
            self.seer   = self.seerlist[0]
        if(len(self.mediumlist) == 1):
            self.medium   = self.mediumlist[0]

        bg = copy.deepcopy(self.myData.getBGCOAgentList())
        for i in self.werewolves:
            if i in bg: bg.remove(i)
        if self.possessed in bg:
            bg.remove(self.possessed)

        self.bglist = bg

    def target(self):

        #襲撃対象を決定・宣言

        #初日
        if(self.myData.getToday() == 1):

            #占い師2人(狂人,占い師)のとき、占い師確定で占い噛み
            #(初日狂人のみCOの場合があるため)
            if(len(self.myData.getSeerCOAgentList()) > 1 and self.seer != None):
                self.willattack = self.seer
                return self.willattack

            #霊媒師確定、占い師未確定で霊媒噛み
            elif(self.medium != None and self.seer == None):
                self.willattack = self.medium
                return self.willattack

        #GJが出ている場合
        if(self.isGJ):

            #占い師噛みでGJなら霊媒>狩人>村人噛み
            if(self.attacked in self.seerlist):
                #確定霊媒師噛み
                if(self.medium != None and self.myData.isAliveIndex(self.medium)):
                    self.willattack = self.medium
                    return self.willattack

                #霊媒師噛み
                if(len(self.mediumlist) > 0):
                    self.willattack = self.myData.getMaxLikelyVill(self.mediumlist)
                    return self.willattack

                #狩人噛み
                if(len(self.bglist) > 0):
                    self.willattack = self.myData.getMaxLikelyVill(self.bglist)
                    return self.willattack

                #村人噛み
                if(len(self.aliveVillagers) > 0):
                    self.willattack = self.myData.getMaxLikelyVill(self.aliveVillagers)
                    return self.willattack

            #霊媒師噛みでGJなら占い>狩人>村人噛み
            elif(self.attacked in self.mediumlist):
                #確定占い師最優先噛み
                if(self.seer != None and self.myData.isAliveIndex(self.seer)):
                    self.willattack = self.seer
                    return self.willattack

                #占い師噛み
                if(len(self.seerlist) > 0):
                    self.willattack = self.myData.getMaxLikelyVill(self.seerlist)
                    return self.willattack

                #狩人噛み
                if(len(self.bglist) > 0):
                    self.willattack = self.myData.getMaxLikelyVill(self.bglist)
                    return self.willattack

                #村人噛み
                if(len(self.aliveVillagers) > 0):
                    self.willattack = self.myData.getMaxLikelyVill(self.aliveVillagers)
                    return self.willattack

        #GJが出ていない場合
        else:

            #確定占い師最優先噛み
            if(self.myData.getToday() > 1 and self.seer != None and self.myData.isAliveIndex(self.seer)):
                self.willattack = self.seer
                return self.willattack

            #占い師噛み
            if(len(self.seerlist) > 0):
                self.willattack = self.myData.getMaxLikelyVill(self.seerlist)
                return self.willattack

            #確定霊媒師噛み
            if(self.medium != None and self.myData.isAliveIndex(self.medium)):
                self.willattack = self.medium
                return self.willattack

            #霊媒師噛み
            if(len(self.mediumlist) > 0):
                self.willattack = self.myData.getMaxLikelyVill(self.mediumlist)
                return self.willattack

            #狩人噛み
            if(len(self.bglist) > 0):
                self.willattack = self.myData.getMaxLikelyVill(self.bglist)
                return self.willattack

            #村人噛み
            if(len(self.aliveVillagers) > 0):
                self.willattack = self.myData.getMaxLikelyVill(self.aliveVillagers)
                return self.willattack


    def finish(self):
        super(Werewolf,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
