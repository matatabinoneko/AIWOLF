#!/usr/bin/env python
from __future__ import print_function, division 

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb
import numpy as np


myname = 'commingout_werewolf_villager'

class SampleAgent(object):
    
    def __init__(self, agent_name):
        # myname
        self.myname = agent_name
        
        
    def getName(self):
        return self.myname
    
    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        # print("---initialize----")
        # print("base_info",base_info,sep='\n')
        # print("diff_data",diff_data,sep='\n')
        # print("game_setting:",game_setting,sep='\n')
        # print()
        
    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        # print("----update----")
        # print("base_info",base_info,sep='\n')
        # print("diff_data",diff_data,sep='\n')
        # print("request=",request)

    def dayStart(self):
        return None

    def randomSelect(self):
        while(True):
            target = np.random.randint(0,self.game_setting["playerNum"])
            if target != self.base_info["agentIdx"]-1:
                return target

    def talk(self):
        return cb.comingout((self.base_info['agentIdx']),"WEREWOLF")
        # return cb.over()
    
    def whisper(self):
        return cb.over()
        
    def vote(self):
        return self.randomSelect()
    
    def attack(self):
        return self.randomSelect()
    
    def divine(self):
        return self.randomSelect()
    
    def guard(self):
        return self.randomSelect()
    
    def finish(self):
        return None
    


agent = SampleAgent(myname)
    


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    