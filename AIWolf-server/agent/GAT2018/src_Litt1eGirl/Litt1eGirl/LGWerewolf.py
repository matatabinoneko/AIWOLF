# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md

import Baseplayers as bp

class Werewolf(bp.BaseWerewolf):

    def __init__(self, agent_name):
        super(Werewolf,self).__init__(agent_name)

        self.seerlist   = []    #狼でも狂人でもない生存占い師リスト
        self.seer       = None  #真占い師
        self.mediumlist = []    #狼でも狂人でもない生存霊媒師リスト
        self.medium     = None  #真霊媒師
        self.bglist     = []    #狼でも狂人でもない生存狩人リスト
        self.possessed  = None  #狂人
        self.isGJ       = False

        self.willattack = None
        self.willvote   = None

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
        self.willvote   = None

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
        fake_m,fake_s   = self.o_fakeSearch()  #明らかに偽者
        self.checkpossessed()       #狂人調査
        self.checkreal()            #真探し

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
            if(self.willvote == 0):
                print("here_wolf")
            return ttf.vote(self.willvote)


        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        #BGCO
        if(self.blacked and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')

        return ttf.over()

    def vote(self):

        target = self.MyTarget()
        if(target != None): return target

        target = self.getMaxLikelyWolfAll()
        return target

    def whisper(self):

        #襲撃対象を決定・宣言
        attacktarget = self.Attacktarget()
        if(self.willattack != attacktarget):
            self.willattack = attacktarget
            return twf.attack(self.willattack)

        return twf.over()

    def attack(self):
        exe = self.gameInfo['latestExecutedAgent']

        if(exe in self.aliveVillagers):
            self.aliveVillagers.remove(exe)

        attacktarget = self.Attacktarget()
        if(attacktarget in self.aliveVillagers and attacktarget != None):
            return attacktarget

        return  self.myData.getAttackTarget(self.aliveVillagers)


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

    def s_fakeSearch(self):

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []

        if(len(seerlist) > 0):
            #自分たちに黒を出していたら偽者
            for k,v in smap.items():
                black   = v.getBlackList()
                if self.agentIdx in black:    fakeSeer.append(k)

        return fakeSeer

    def Attacktarget(self):

        #初日
        if(self.myData.getToday() == 1):

            #占い師2人(狂人,占い師)のとき、占い師確定で占い噛み
            #(初日狂人のみCOの場合があるため)
            if(len(self.myData.getSeerCOAgentList()) > 1 and self.seer != None):
                attacktaret = self.seer
                return attacktaret

            #霊媒師確定、占い師未確定で霊媒噛み
            elif(self.medium != None and self.seer == None):
                attacktaret = self.medium
                return attacktaret

        #GJが出ている場合
        if(self.isGJ):

            #占い師噛みでGJなら霊媒>狩人>村人噛み
            if(self.attacked in self.seerlist):
                #確定霊媒師噛み
                if(self.medium != None and self.myData.isAliveIndex(self.medium)):
                    attacktaret = self.medium
                    return attacktaret

                #霊媒師噛み
                if(len(self.mediumlist) > 0):
                    attacktaret = self.myData.getAttackTarget(self.mediumlist)
                    return attacktaret

                #狩人噛み
                if(len(self.bglist) > 0):
                    attacktaret = self.myData.getAttackTarget(self.bglist)
                    return attacktaret

                #村人噛み
                if(len(self.aliveVillagers) > 0):
                    attacktaret = self.myData.getAttackTarget(self.aliveVillagers)
                    return attacktaret

            #霊媒師噛みでGJなら占い>狩人>村人噛み
            elif(self.attacked in self.mediumlist):
                #確定占い師最優先噛み
                if(self.seer != None and self.myData.isAliveIndex(self.seer)):
                    attacktaret = self.seer
                    return attacktaret

                #占い師噛み
                if(len(self.seerlist) > 0):
                    attacktaret = self.myData.getAttackTarget(self.seerlist)
                    return attacktaret

                #狩人噛み
                if(len(self.bglist) > 0):
                    attacktaret = self.myData.getAttackTarget(self.bglist)
                    return attacktaret

                #村人噛み
                if(len(self.aliveVillagers) > 0):
                    attacktaret = self.myData.getAttackTarget(self.aliveVillagers)
                    return attacktaret

        #GJが出ていない場合
        else:

            #確定占い師最優先噛み
            if(self.myData.getToday() > 1 and self.seer != None and self.myData.isAliveIndex(self.seer)):
                attacktaret = self.seer
                return attacktaret

            #占い師噛み
            if(len(self.seerlist) > 0):
                attacktaret = self.myData.getAttackTarget(self.seerlist)
                return attacktaret

            #確定霊媒師噛み
            if(self.medium != None and self.myData.isAliveIndex(self.medium)):
                attacktaret = self.medium
                return attacktaret

            #霊媒師噛み
            if(len(self.mediumlist) > 0):
                attacktaret = self.myData.getAttackTarget(self.mediumlist)
                return attacktaret

            #狩人噛み
            if(len(self.bglist) > 0):
                attacktaret = self.myData.getAttackTarget(self.bglist)
                return attacktaret

            #村人噛み
            if(len(self.aliveVillagers) > 0):
                attacktaret = self.myData.getAttackTarget(self.aliveVillagers)
                return attacktaret

        attacktarget = self.myData.getAttackTargetAll()
        return attacktarget

    def MyTarget(self):

        fake_m,fake_s   = self.o_fakeSearch()  #明らかに偽者
        self.checkpossessed()       #狂人調査
        self.checkreal()            #真探し

        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            for i in fake:
                if(not self.myData.isAliveIndex(i)):    fake.remove(i)
            if(len(fake) > 0):
                target = self.myData.getMaxLikelyWolf(fake)
                return target

        fake    = self.s_fakeSearch()  #明らかに偽者
        if(len(fake) > 0):
            for i in fake:
                if(not self.myData.isAliveIndex(i)):    fake.remove(i)
            if(len(fake) > 0):
                target = self.myData.getMaxLikelyWolf(fake)
                return target

        target  = self.myData.getMaxLikelyWolfAll()
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

    def finish(self):
        super(Werewolf,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
