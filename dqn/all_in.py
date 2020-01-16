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

myname = 'matatabi'

class SampleAgent(object):
    def __init__(self, agent_name):
        ## myname ##
        self.myname = agent_name
        self.w_data = Environment(
                                agent_num=6,
                                train_predict_mode=False,
                                train_dqn_mode=False,
                                train_divine_mode=False,
                                # explore = True,
                                predict_net_load=False,
                                dqn_net_load=True,
                                divine_net_load=True,
                                train_times=10000,
                                )




    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        self.w_data.initialize(base_info, diff_data, game_setting)

    def update(self, base_info, diff_data, request):
        self.w_data.update(base_info,diff_data,request)


    def dayStart(self):
        return None


    def talk(self):
        return self.w_data.talk()

    def whisper(self):
        # print("whisper")
        return cb.over()

    def vote(self):
        return self.w_data.vote()

    def attack(self):
        return self.w_data.attack()

    def divine(self):
        return self.w_data.divine()


    def guard(self):
        return self.w_data.guard()

    def finish(self):
        self.w_data.finish()
        return None

agent = SampleAgent(myname)



# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    
