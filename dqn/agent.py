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
from divine_model import DivineModel


import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))



class Agent():
    def __init__(self,pred_n_input, pred_n_hidden, pred_n_output,dqn_n_input, dqn_n_hidden, dqn_n_output, agent_num,role_num,t_role_cnt,train_predict_mode=False,train_dqn_mode=False,train_divine_mode=False,explore=False):
        self.agent_num = agent_num
        self.role_num = role_num
        self.train_predict_mode = train_predict_mode
        self.train_dqn_mode = train_dqn_mode
        self.train_divine_mode = train_divine_mode
        self.explore = explore
        self.answer = []
        self.kanning=False
        self.pred_model = PredictRole(pred_n_input,pred_n_hidden,agent_num,role_num)
        self.brain = Brain(n_input=dqn_n_input,n_hidden=dqn_n_hidden,n_output=dqn_n_output)
        self.divine_model = DivineModel(n_input=dqn_n_input,n_hidden=dqn_n_hidden,n_output=agent_num*2)
        self.last_pred_result = None
    
        self.epsilon = 0.1

        if self.kanning==True:
            with open("../AIWolf-ver0.5.6/role.txt","r") as f:
                for agent in f.readlines():
                    agent = agent.strip("\n")
                    self.answer.append(agent)

    
    def update_pred_model(self):
        self.pred_model.train(agent_num=self.agent_num,role_num=self.role_num)

    def eval_pred_model(self,state,label):
        # print(state)
        # state = torch.tensor(state).float()
        # label = torch.tensor(label).float()
        pred = self.pred_model.eval(state=state,label=label,agent_num=self.agent_num,role_num=self.role_num)
        return pred

    def memorize_pred_label(self,state,label):
        state = torch.tensor(state).float()
        label = torch.tensor(label).float()
        self.pred_model.memory.push(state,label)

    def randomSelect(self,votable_mask):
        while(True):
            target = np.random.randint(0,len(votable_mask))
            if votable_mask[target]==True:
                return target

    def createDqnState(self,state):
        # pred_result = self.get_predict_output(state)
        # return torch.FloatTensor(pred_result)
        return torch.FloatTensor(state)
        # return torch.cat((torch.FloatTensor(state),torch.FloatTensor(pred_result)),dim=1).float()

    def selectAgent(self,state,votable_mask,agent_num,role_num,num_to_role):
        #返り値は１始まり
        # return self.brain.selectAgent(state,episode=0).argmax(0).item() + 1

        # if self.kanning == True:
        #     for target,role in enumerate(self.answer):
        #         if role == target_role and votable_mask[target]:
        #             return target + 1
        #     return self.randomSelect(votable_mask),None

        # if self.explore == True and self.train_dqn_mode == True:
        if self.train_dqn_mode == True:
            if np.random.random() < self.epsilon:
                # print("random",pred_result.detach().numpy())
                return self.randomSelect(votable_mask)

        self.brain.target_q_model.eval()
        with torch.no_grad():
            state = torch.tensor(state).float()
            # action_type = torch.tensor(action_type).float()

            # pred_result = self.brain.get_output(state)
            # print(type(pred_result))
            
            
            state = self.createDqnState(state=state)
            vote_list = sorted(enumerate(self.brain.get_output(state).squeeze().detach().numpy()),key=lambda x:x[1],reverse=True)
            for target,_ in vote_list:
                if votable_mask[target] == True:
                    # print("target",pred_result.detach().numpy())
                    return target
            # print("random",pred_result.detach().numpy())
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
    
    def update_divine_function(self):
        self.divine_model.replay()

    # def get_action(self,state,episode):
    #     state  =torch.tensor(state).float()
    #     action = self.brain.decide_action(state,episode)

    def memorize_state(self,state,action,next_state,reward):
        state = torch.FloatTensor(state)
        state = self.createDqnState(state=state)
        action = torch.tensor(action).long()
        if next_state is not None:
            next_state = torch.FloatTensor(next_state)
            next_state = self.createDqnState(state=next_state)
        reward = torch.tensor(reward).float()
        self.brain.memory.push(state,action,next_state,reward)

    def memorize_divine_state(self,state,action,next_state,reward):
        state = torch.FloatTensor(state)
        state = self.createDqnState(state=state)
        action = torch.tensor(action).long()
        if next_state is not None:
            next_state = torch.FloatTensor(next_state)
            next_state = self.createDqnState(state=next_state)
        reward = torch.tensor(reward).float()
        self.divine_model.memory.push(state,action,next_state,reward)


    # def updateWinRatio(self,win):
    #     win = 1 if win else 0
    #     self.brain.memory.pushWinRatio(win)

    def get_predict_output(self,state):
        self.last_pred_result = self.pred_model.get_output(state).detach().numpy()
        return self.last_pred_result


    def randomDivineSelect(self,divinable_mask):
        while(True):
            target = np.random.randint(0,len(divinable_mask))
            if divinable_mask[target]==True:
                role = np.random.randint(0,2)
                return target*2 + role

    def selectDivineAgent(self,state,divinable_mask):
        return self.randomDivineSelect(divinable_mask)

        if state is None:
            return self.randomDivineSelect(divinable_mask)
        self.divine_model.target_q_model.eval()
        with torch.no_grad():

            # if self.explore == True and self.train_divine_mode == True:
            if self.train_divine_mode == True:
                if np.random.random() < self.epsilon:
                    return self.randomDivineSelect(divinable_mask)

            state = torch.tensor(state).float()
            
            # pred_result = self.pred_model.get_output(state)
            # print(type(pred_result))
            
            # if self.train_divine_mode == True:
            #     if np.random.random() < self.epsilon:
            #         # print("random",pred_result.detach().numpy())
            #         return self.randomDivineSelect(divinable_mask)
            
            state = self.createDqnState(state=state)
            divine_list = sorted(enumerate(self.divine_model.get_output(state).squeeze().detach().numpy()),key=lambda x:x[1],reverse=True)
            for target,_ in divine_list:
                if divinable_mask[target//2] == True:
                    # print(target)
                    return target
            return randomDivineSelect(divinable_mask)


    def update_target_q_function(self):
        self.brain.update_target_q_model()
        self.divine_model.update_target_q_model()
