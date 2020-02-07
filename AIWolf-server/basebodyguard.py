#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd
import copy

import MyData as md
import basevillager as bv

class Bodyguard(bv.Villager):

    def __init__(self, agent_name):
        super(Bodyguard,self).__init__(agent_name)
        self.Role = 'BODYGUARD'
        self.willvote   = None

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Bodyguard,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Bodyguard,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Bodyguard,self).dayStart()
        self.willvote   = None

    def talk(self):
        super(Bodyguard,self).talk()
        return ttf.over()

    def vote(self):
        target      = self.myData.getMaxLikelyWolfAll()
        return target

    def guard(self):

        if(len(self.candidate) > 0):
            target      = self.myData.getMaxLikelyVill(self.candidate)
            return target

        return self.agentIdx

    def finish(self):
        super(Bodyguard,self).finish()
