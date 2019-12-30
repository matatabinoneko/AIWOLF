#!/usr/bin/env python
from __future__ import print_function, division 

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb

import numpy as np


myname = 'cash'

class SampleAgent(object):
    
    def __init__(self, agent_name):
        # myname
        self.myname = agent_name

        self.do_fake_report = False
        self.not_report = True
        self.target = -1
        self.done_last_commingout = False
        

        
        
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
        self.agent_num = len(self.base_info["statusMap"])
        self.votable_mask = np.ones(self.agent_num).astype(np.bool)
        self.votable_mask[int(self.base_info["agentIdx"]-1)] = False
        self.divinable_mask = np.ones(self.agent_num).astype(np.bool)
        self.divinable_mask[int(self.base_info["agentIdx"]-1)] = False
        self.werewolf_list = []

        self.do_fake_report = True
        self.not_report = False
        self.target = -1
        self.done_last_commingout = False

        if self.agent_num <= 6:
            self.werewolf_num = 1
        elif self.agent_num <=11:
            self.werewolf_num = 2
        else:
            self.werewolf_num = 3
        self.alive_agent_num = self.agent_num
            
        
    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        # print("----update----")
        # print("base_info",base_info,sep='\n')
        # print("diff_data",diff_data,sep='\n')
        # print("request=",request)
        if request == "DAILY_INITIALIZE" and self.base_info["day"]!=0:
            if self.base_info['myRole'] == 'POSSESSED':
                self.do_fake_report = False
            for agent,status in self.base_info["statusMap"].items():
                self.alive_agent_num = self.agent_num
                agent = int(agent)-1
                if status == "DEAD":
                    self.alive_agent_num -= 1
                    self.votable_mask[agent] = False
                    self.divinable_mask[agent] = False
            # print(self.votable_mask)
            # print(self.divinable_mask)

        if self.do_fake_report== False:
            self.do_fake_report = True
            self.fake_divine()


        # if request == "DAILY_FINISH" and 1 <= base_info["day"]:

    def dayStart(self):
        return None
    
    def talk(self):
        if self.base_info['myRole'] == 'POSSESSED':
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),"SEER")
            if(self.not_report == True):
                # self.do_fake_report = True
                self.not_report = False
                return self.myresult
            return cb.over()
        else:
            print("error")

        return cb.over()
    
    def whisper(self):
        return cb.over()
        
    def vote(self):
        if len(np.where(self.votable_mask == True)[0]) == 0:
            self.votable_mask.fill(True)
            self.votable_mask[self.base_info["agentIdx"]-1] = False

        for target in self.werewolf_list:
            if self.votable_mask[target] == True:
                return target + 1
        while(True):
            target = np.random.randint(0,self.agent_num)
            if self.votable_mask[target] == True:
                # print(target+1)
                return target + 1
        # for target in reversed(self.werewolf_list):
        #     if self.votable_mask[target] == True:
        #         # print(target+1)
        #         return target + 1
        # # return self.base_info['agentIdx']
        # # return self.target + 1
    
    def attack(self):
        return self.base_info['agentIdx']
    
    def fake_divine(self):
        # print(np.where(self.divinable_mask == True))
        if len(np.where(self.divinable_mask == True)[0]) == 0:
            self.divinable_mask.fill(True)
            self.divinable_mask[self.base_info["agentIdx"]-1] = False
        self.not_report = True
        while(True):
            self.target = np.random.randint(0,self.agent_num)
            if self.divinable_mask[self.target] == True:
                self.divinable_mask[self.target] = False
                if np.random.rand() < self.werewolf_num/self.alive_agent_num:
                    role = "WEREWOLF"
                    self.werewolf_num -= 1
                    self.werewolf_list.append(self.target)
                else:
                    role = "HUMAN"
                    self.votable_mask[self.target] = False
                break
        # self.werewolf_list.append(self.target)
        self.myresult = 'DIVINED Agent[' + "{0:02d}".format(self.target+1) + '] ' + role
        # print(self.myresult)


    def divine(self):
        return self.base_info['agentIdx']
    
    def guard(self):
        return self.base_info['agentIdx']
    
    def finish(self):
        return None
    


agent = SampleAgent(myname)
    


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    