#!/usr/bin/env python
# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd

#各役職モジュールインポート
import JuN1RoVillager as lv
import JuN1RoSeer as ls
import JuN1RoMedium as lm
import JuN1RoBodyguard as lb
import JuN1RoPossessed as lp
import JuN1RoWerewolf as lw
import villager5 as bv
import seer5 as bs
import possessed5 as bp
import werewolf5 as bw

import MyData as md
import MyData_base as mdb

class JuN1Ro(object):
    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.gameInfo   = []
        pass

    def getName(self):
        return self.agent_name

    def initialize(self, game_info, game_setting):
        self.agentIdx   = game_info['agent']    #エージェントインデックス
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'#エージェント名
        self.role       = game_info['roleMap'][str(self.agentIdx)]  #自分の役割
        self.roleMap    = game_info['roleMap']#役割一覧（狼は全員の種別，他は自分のみ）
        self.playerNum  = game_setting['playerNum']#プレイヤ人数
        #エージェント名のリスト(Agent[01]など).
        self.agentNames = ['Agent[' + "{0:02d}".format(target) + ']' for target in range(1, self.playerNum+1)]
        self.gameInfo = game_info

        #myDataは毎ゲームリセット
        if(len(self.agentNames) < 15):
            self.myData     = mdb.MyData()
        else:
            self.myData     = md.MyData()

        if self.role == 'VILLAGER':
            if(len(self.agentNames) < 15):
                self.agent = bv.Villager(self.agent_name)
            else:
                self.agent = lv.Villager(self.agent_name)
        elif self.role == 'SEER':
            if(len(self.agentNames) < 15):
                self.agent = bs.Seer(self.agent_name)
            else:
                self.agent = ls.Seer(self.agent_name)
        elif self.role == 'MEDIUM':
        	self.agent = lm.Medium(self.agent_name)
        elif self.role == 'BODYGUARD':
        	self.agent = lb.Bodyguard(self.agent_name)
        elif self.role == 'POSSESSED':
            if(len(self.agentNames) < 15):
                self.agent = bp.Possessed(self.agent_name)
            else:
                self.agent = lp.Possessed(self.agent_name)
        elif self.role == 'WEREWOLF':
            if(len(self.agentNames) < 15):
                self.agent = bw.Werewolf(self.agent_name)
            else:
                self.agent = lw.Werewolf(self.agent_name)

        self.myData.gameStart(game_info, self.playerNum, self.agentNames)
        self.agent.setmyData(self.myData)
        self.agent.initialize(game_info, game_setting)

    def update(self, game_info, talk_history, whisper_history, request):
        if(len(game_info) > 0):
            self.gameInfo = game_info
        self.myData.update(self.gameInfo, talk_history)
        self.agent.update(self.gameInfo, talk_history, whisper_history, request)
        self.agent.setmyData(self.myData)

    def dayStart(self):
        self.myData.dayStart(self.gameInfo)
        self.agent.setmyData(self.myData)
        return self.agent.dayStart()

    def talk(self):
        return self.agent.talk()

    def whisper(self):
        #print ("whisper")
        return self.agent.whisper()

    def vote(self):
        #print ("vote")
        return self.agent.vote()

    def attack(self):
        #print ("attack")
        return self.agent.attack()

    def divine(self):
        #print ("divine")
        return self.agent.divine()

    def guard(self):
        #print ("guard")
        return self.agent.guard()

    def finish(self):
        #print ("finish")
        return self.agent.finish()

agent = JuN1Ro('JuN1Ro')

# run
if __name__ == '__main__':
    aiwolfpy.connect(agent)
