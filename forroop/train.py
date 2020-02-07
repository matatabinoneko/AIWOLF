#!/usr/bin/env python
from __future__ import print_function, division 
import re
import numpy as np
import os

import argparse
parser = argparse.ArgumentParser(add_help=False)
# parser.add_argument('--input', type=int, default=-1)
parser.add_argument('-p', type=int, action='store', dest='port',default=-1)
parser.add_argument('-h', type=str, action='store', dest='hostname',default="none")
parser.add_argument('-r', type=str, action='store', dest='role', default='none')
parser.add_argument('--pred_h', type=int, default=100)
parser.add_argument('--dqn_h', type=int, default=100)
args = parser.parse_args()

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

import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

from rule_base.randomPOSSESSED import SampleAgent as SA

myname = 'matatabi_train'

class SampleAgent(SA,object):
    def __init__(self, agent_name):
        ## myname ##
        self.myname = agent_name
        self.w_data = Environment(
                                agent_num=15,
                                train_predict_mode=True,
                                train_dqn_mode=True,
                                train_divine_mode=True,
                                # explore = True,
                                predict_net_load=False,
                                dqn_net_load=False,
                                divine_net_load=False,
                                train_times=10000,
                                pred_h=args.pred_h,
                                dqn_h=args.dqn_h,
                                )
        print("pred h is {}  dqn h is {}".format(args.pred_h,args.dqn_h))
        self.cnt = 0
        # super().__init__(agent_name)


    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        self.w_data.initialize(base_info, diff_data, game_setting)
        # super().initialize(base_info, diff_data, game_setting)

    def update(self, base_info, diff_data, request):
        self.w_data.update(base_info,diff_data,request)


    def dayStart(self):
        return None


    def talk(self):
        # return super().talk()
        return self.w_data.talk()

    def whisper(self):
        # print("whisper")
        return cb.over()

    def vote(self):
        # return super().vote()
        return self.w_data.vote()

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


path = "Agent_train_pred_" + str(args.pred_h) + "_dqn_" +str(args.dqn_h) + ".pickle"
if os.path.exists(path):
    with open(path,'rb') as f:
        agent = pickle.load(f)
        # print("pickle loaded")
else:
    agent = SampleAgent(myname)



# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    
