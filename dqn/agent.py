import aiwolfpy
import aiwolfpy.contentbuilder as cb

import numpy as np
import re
import os
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
import time
import random

import matplotlib.pyplot as plt
import collections
from collections import *
X_T = namedtuple("X_T",("state","label"))

from predict_model import PredictRole
from brain import Brain


import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))



class Agent():
    def __init__(self,n_input, n_hidden, n_output, agent_num,role_num,t_role_cnt,train_mode=False):
        self.agent_num = agent_num
        self.role_num = role_num
        self.train_mode = train_mode
        self.answer = []
        self.kanning=False
        self.pred_model = PredictRole(n_input,n_hidden,n_output)
        self.brain = Brain(n_input,200,n_output=self.agent_num)
        
        if self.train_mode == True:
            self.epsilon = 0
        else:
            self.epsilon = 0.2

        if self.kanning==True:
            with open("../AIWolf-ver0.5.6/role.txt","r") as f:
                for agent in f.readlines():
                    agent = agent.strip("\n")
                    self.answer.append(agent)

    
    def update_pred_model(self):
        self.pred_model.train(agent_num=self.agent_num,role_num=self.role_num)

    def eval_pred_model(self,state,label):
        pred = self.pred_model.eval(state=state,label=label,agent_num=self.agent_num,role_num=self.role_num)
        return pred

    def memorize_pred_label(self,state,label):
        self.pred_model.memory.push(state,label)

    def randomSelect(self,votable_mask):
        while(True):
            target = np.random.randint(0,len(votable_mask))
            if votable_mask[target]==True:
                return target


    def selectAgent(self,state,votable_mask,agent_num,role_num,num_to_role):
        #返り値は１始まり
        # return self.brain.selectAgent(state,episode=0).argmax(0).item() + 1

        if self.kanning == True:
            for target,role in enumerate(self.answer):
                if role == target_role and votable_mask[target]:
                    return target + 1
            return self.randomSelect(votable_mask)


        self.brain.model.eval()

        if self.train_mode == True:
            if np.random.random() < self.epsilon:
                return self.randomSelect(votable_mask)

        state = torch.tensor(state).long()
        vote_list = sorted(enumerate(self.brain.get_output(state).squeeze().detach().numpy()),key=lambda x:x[1],reverse=True)
        for target,_ in vote_list:
            if votable_mask[target] == True:
                return target
        return randomSelect(votable_mask)


        # est_role_list = self.pred_model.model(state).reshape(-1).detach().numpy().reshape(agent_num,role_num)

        # est_role_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if num_to_role[role] == target_role]
        # est_role_list = sorted(est_role_list,reverse=True)

        # for _,target in est_role_list:
        #     if votable_mask[target]==True:
        #         return target + 1

        # return self.randomSelect(votable_mask)

    def update_q_function(self):
        self.brain.replay()

    def get_action(self,state,episode):
        action = self.brain.decide_action(state,episode)

    def memorize_state(self,state,action,next_state,reward):
        self.brain.memory.push(state,action,next_state,reward)

    def updateWinRatio(self,win):
        1 if win else 0
        self.brain.memory.pushWinRatio(win)