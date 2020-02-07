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

class Possessed(bv.Villager):

    def __init__(self, agent_name):
        super(Possessed,self).__init__(agent_name)
        self.Role = 'POSSESSED'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Possessed,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Possessed,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Possessed,self).dayStart()

    def talk(self):
        super(Possessed,self)
        return ttf.over()

    def vote(self):
        #投票候補から最も狼らしい者
        if(len(self.candidate) > 0):
            return self.myData.getMaxLikelyWolf(self.candidate)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(Possessed,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
