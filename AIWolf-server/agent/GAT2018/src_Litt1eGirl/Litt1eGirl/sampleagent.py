# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import random


class SampleAgent(object):

    def __init__(self, agent_name):
        self.agent_name = agent_name
        pass

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        pass

    def initialize(self, game_info, game_setting):
        self.agentIdx = game_info['agent']
        self.playerNum  = game_setting['playerNum']

    def dayStart(self):
        return None

    def talk(self):
        p = self.playerNum

        rand = random.randrange(0,10)
        if(rand == 0):
            target = random.randrange(1,p+1)
            return ttf.vote(target)
        if(rand == 1):
            target = random.randrange(1,p+1)
            return ttf.estimate(target,'WEREWOLF')
        if(rand == 2):
            return ttf.comingout(self.agentIdx,'SEER')
        if(rand == 3):
            target = random.randrange(1,p+1)
            return ttf.divined(target,'WEREWOLF')
        if(rand == 4):
            return ttf.comingout(self.agentIdx,'WEREWOLF')
        if(rand == 5):
            target = random.randrange(1,p+1)
            return ttf.identified(target,'WEREWOLF')
        if(rand == 6):
            target = random.randrange(1,p+1)
            return ttf.guarded(target)
        if(rand == 7):
            target = random.randrange(1,p+1)
            return ttf.guarded(target)
        if(rand == 8):
            target = random.randrange(1,p+1)
            return ttf.estimate(target,'VILLAGER')
        if(rand == 9):  return ttf.over()

    def whisper(self):
        return twf.over()

    def vote(self):
        return self.agentIdx

    def attack(self):
        return self.agentIdx

    def divine(self):
        return self.agentIdx

    def guard(self):
        return self.agentIdx

    def finish(self):
        return None



agent = SampleAgent('AIWolfPy_sample')



# run
if __name__ == '__main__':
    aiwolfpy.connect(agent)
