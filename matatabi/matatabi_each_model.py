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

import chainer

from aiwolf_func import *

myname = 'matatabi'

class SampleAgent(object):
    def __init__(self, agent_name):
        ## myname ##
        self.myname = agent_name
        self.w_data = data_info(agent_num=5,daily_train=True,player_train=True,train_times=1000)




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
        self.w_data.addVectorToEachModel()
        self.w_data.finish()

        if self.w_data.train_cnt%1000 == 0:
            print(self.w_data.train_cnt)
            self.w_data.save_each_model()

        return None

agent = SampleAgent(myname)



# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    