#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd
import MyData as md

import basevillager as bv

class Werewolf(bv.Villager):

    def __init__(self, agent_name):
        super(Werewolf,self).__init__(agent_name)
        self.Role = 'WEREWOLF'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Werewolf,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Werewolf,self).initialize(game_info, game_setting)
        self.playerNum  = game_setting['playerNum']
        self.werewolves = []      #仲間の人狼
        self.villagers  = []      #村人たち
        self.aliveWerewolves = []      #仲間の人狼
        self.aliveVillagers  = []      #村人たち

        #自分以外の人狼リストの作成
        for k,v in game_info['roleMap'].items():
            if(self.agentIdx != int(k)):
                self.werewolves.append(int(k))

        #村人リストの作成
        self.villagers = [target for target in range(1, self.playerNum+1)]
        for i in self.werewolves:
            if (i in self.villagers):   self.villagers.remove(i)
        self.villagers.remove(self.agentIdx)

    def dayStart(self):
        super(Werewolf,self).dayStart()

        #生存リストの更新
        self.aliveVillagers, self.aliveWerewolves = [], []
        for i in self.myData.getAliveAgentIndexList():
            if(i in self.villagers):
                self.aliveVillagers.append(i)
            if(i in self.werewolves):
                self.aliveWerewolves.append(i)

    def talk(self):
        return ttf.over()

    def whisper(self):
        return twf.over()

    def vote(self):

        #村人から最も狼らしい者(身内切りはしない)
        if(len(self.aliveVillagers) > 0):
            return self.myData.getMaxLikelyWolf(self.aliveVillagers)

        #例外対策
        return self.agentIdx

    def attack(self):

        #村人から最も村人らしい者(役職狙い撃ちはしない)
        if(len(self.aliveVillagers) > 0):
            return self.myData.getMaxLikelyVill(self.aliveVillagers)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(Werewolf,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
