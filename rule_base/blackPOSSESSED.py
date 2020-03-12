#!/usr/bin/env python
from __future__ import print_function, division 

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb

import numpy as np


myname = 'black'

class SampleAgent(object):
    
    def __init__(self, agent_name):
        # myname
        self.myname = agent_name

        self.do_fake_report = False
        self.not_report = True
        self.target = -1
        self.done_last_commingout = False
        self.game_cnt = 0
        self.win_ratio_list = []
        

        
        
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

        self.agent_role = [''for i in range(self.agent_num)]
        
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
                agent = int(agent)-1
                if status == "DEAD":
                    self.votable_mask[agent] = False
                    self.divinable_mask[agent] = False
            # print(self.votable_mask)
            # print(self.divinable_mask)


        if self.do_fake_report== False:
            self.do_fake_report = True
            self.fake_divine()

        for i in range(len(diff_data)):
            if diff_data["type"][i] == 'finish':
                # self.countEachRole(i)
                agent = diff_data["agent"][i]-1
                role = diff_data["text"][i].split(' ')[2]
                self.agent_role[i] = role



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
        for target in reversed(self.werewolf_list):
            if self.votable_mask[target] == True:
                # print(target+1)
                return target + 1
        # return self.base_info['agentIdx']
        # return self.target + 1
    
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
                break
        self.werewolf_list.append(self.target)
        self.myresult = 'DIVINED Agent[' + "{0:02d}".format(self.target+1) + '] ' + "WEREWOLF"
        # print(self.myresult)


    def divine(self):
        return self.base_info['agentIdx']
    
    def guard(self):
        return self.base_info['agentIdx']
    
    def finish(self):
        self.win_ratio_list.append(0)
        for i in range(self.agent_num):
            # print(self.agent_role[i],self.base_info["statusMap"].get(str(i+1)))
            if self.agent_role[i]=="WEREWOLF" and self.base_info["statusMap"].get(str(i+1)) == "ALIVE":
                self.win_ratio_list[-1] = 1
                # print("win")
                break


        self.game_cnt += 1
        if self.game_cnt%100 == 0:
            print("game cnt is {}  win ratio is {:.4f}".format(self.game_cnt,np.mean(self.win_ratio_list)))
        return None
    


agent = SampleAgent(myname)
    


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    