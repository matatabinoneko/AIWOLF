#!/usr/bin/env python
from __future__ import print_function, division 
import re
import numpy as np
import os

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb

# import sys

import matplotlib.pyplot as plt
from collections import defaultdict

from pytorch import *
# from pytorch_fake_divine_when_talk import *

import pickle

myname = 'matatabi_divine_only'


import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

from rule_base.randomPOSSESSED import SampleAgent as SA

class SampleAgent(SA,object):
    def __init__(self, agent_name):
        ## myname ##
        self.myname = agent_name
        self.w_data = Environment(
                                agent_num=5,
                                train_predict_mode=False,
                                train_dqn_mode=False,
                                train_divine_mode=False,
                                # explore = True,
                                predict_net_load=True,
                                dqn_net_load=False,
                                divine_net_load=True,
                                train_times=10000,
                                )
        # myname
        self.cnt = 0

        super().__init__(agent_name)



    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        self.w_data.initialize(base_info, diff_data, game_setting)
        super().initialize(base_info, diff_data, game_setting)

    def update(self, base_info, diff_data, request):
        self.w_data.update(base_info,diff_data,request)
        super().update(base_info, diff_data, request)


    def dayStart(self):
        return None


    def talk(self):
        return self.w_data.talk()

    def whisper(self):
        # print("whisper")
        return cb.over()

    def vote(self):
        # return self.w_data.vote()
        return super().vote()

    def attack(self):
        return self.w_data.attack()

    def divine(self):
        return self.w_data.divine()


    def guard(self):
        return self.w_data.guard()

    def finish(self):
        self.w_data.finish()
        self.cnt += 1
        if self.cnt%100==0:
            with open(path,'wb') as f:
                pickle.dump(agent,f)
                # print("dumped")
        return None

path = "Agent_divine_only.pickle"
if os.path.exists(path):
    with open(path,'rb') as f:
        agent = pickle.load(f)
        # print("pickle loaded")
else:
    agent = SampleAgent(myname)






# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    
