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

Transition = namedtuple("Transition",("state","action","next_state","reward"))

from torch.utils.tensorboard import SummaryWriter
writer = SummaryWriter()

BATCH_SIZE = 32
CAPACITY = 1000
GAMMA = 0.99

TD_ERROR_EPSILON = 0.0001



device = "cuda" if torch.cuda.is_available() else "cpu"
print("divine model use {}".format(device))
    

import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

class ReplayMemory():
    def __init__(self):
        self.memory = []
        self.index = 0
        self.max_Q_memory = []
        self.last_q = 0

    def push(self,state,action,next_state,reward):
        if len(self.memory) < CAPACITY:
            self.memory.append(None)
        if next_state is not None:
            next_state = next_state.to(device)
        self.memory[self.index] = Transition(state.to(device),action.to(device),next_state,reward.to(device))
        self.index = (self.index + 1)%CAPACITY
    
    def sample(self,batch_size):
        return random.sample(self.memory,batch_size)

    def __len__(self):
        return(len(self.memory))

    def pushMaxQ(self,q):
        # if len(self.max_Q_memory) < 10:
        #     self.max_Q_memory.append(q)
        # else:
        #     ave_q = (self.max_Q_memory[-1]*10 - self.last_q + q)/10
        #     self.max_Q_memory.append(ave_q)
        # self.last_q = q
        self.max_Q_memory.append(q)
        writer.add_scalar('data/divine_max_Q',q,len(self.max_Q_memory))



class TDerrorMemory():
    def __init__(self):
        self.memory = []
        self.index = 0

    def push(self,td_error):
        if len(self.memory) < CAPACITY:
            self.memory.append(None)
        self.memory[self.index] = td_error
        self.index = (self.index+1)%CAPACITY

    def __len__(self):
        return len(self.memory)

    def get_prioritized_indexes(self, batch_size):
        '''TD誤差に応じた確率でindexを取得'''

        # TD誤差の和を計算
        sum_absolute_td_error = np.sum(np.absolute(self.memory))
        sum_absolute_td_error += TD_ERROR_EPSILON * len(self.memory)  # 微小値を足す

        # batch_size分の乱数を生成して、昇順に並べる
        rand_list = np.random.uniform(0, sum_absolute_td_error, batch_size)
        rand_list = np.sort(rand_list)

        # 作成した乱数で串刺しにして、インデックスを求める
        indexes = []
        idx = 0
        tmp_sum_absolute_td_error = 0
        for rand_num in rand_list:
            while tmp_sum_absolute_td_error < rand_num:
                tmp_sum_absolute_td_error += (
                    abs(self.memory[idx]) + TD_ERROR_EPSILON)
                idx += 1

            # 微小値を計算に使用した関係でindexがメモリの長さを超えた場合の補正
            if idx >= len(self.memory):
                idx = len(self.memory) - 1
            indexes.append(idx)

        return indexes

    def update_td_error(self, updated_td_errors):
        '''TD誤差の更新'''
        self.memory = updated_td_errors


class Model(nn.Module):
    def __init__(self,n_input,n_hidden,n_output):
        super(Model,self).__init__()
        self.fc1 = nn.Linear(n_input,n_hidden)
        self.fc2 = nn.Linear(n_hidden,n_hidden)
        # self.fc = nn.Linear(n_hidden,n_hidden)
        self.fc3 = nn.Linear(n_hidden,n_output)


    def forward(self,x):
        x = self.fc1(x)
        x = F.relu(x)
        x = self.fc2(x)
        x = F.relu(x)
        # x =F.relu(self.fc(x))
        x = self.fc3(x)
        return x

class DivineModel():
    def __init__(self,n_input,n_hidden,n_output):
        self.n_input = n_input
        self.n_hidden = n_hidden
        self.n_output = n_output

        self.memory = ReplayMemory()
        self.td_error_memory = TDerrorMemory()

        self.main_q_model = Model(n_input=n_input,n_hidden=n_hidden,n_output=n_output).to(device)
        self.target_q_model = Model(n_input=n_input,n_hidden=n_hidden,n_output=n_output).to(device)
        print("divine model:",self.main_q_model,sep='\n')

        self.optimizer = optim.Adam(self.main_q_model.parameters())



    def replay(self):
        if len(self.memory) < BATCH_SIZE:
            return 

        self.batch,self.state_batch,self.action_batch,self.reward_batch,self.non_final_next_states = self.make_minibatch()
        self.expected_state_action_values = self.get_expected_state_action_values().to(device)
        self.update_main_q_model()



    def get_output(self,state):
        state = state.to(device)
        return self.target_q_model(state).to("cpu")

    def make_minibatch(self):
        while(True):
            # 2.1 メモリからミニバッチ分のデータを取り出す
            if len(self.td_error_memory) < 100:
                transitions = self.memory.sample(BATCH_SIZE)
            else:
                # TD誤差に応じてミニバッチを取り出すに変更
                indexes = self.td_error_memory.get_prioritized_indexes(BATCH_SIZE)
                transitions = [self.memory.memory[n] for n in indexes]

            batch = Transition(*zip(*self.memory.sample(BATCH_SIZE)))
            s = [s for s in batch.next_state if s is not None]
            # print(s)
            if len(s) != 0:
                non_final_next_states = torch.cat(s).float()
                break


        state_batch = torch.cat(batch.state)
        action_batch = torch.cat(batch.action)
        reward_batch = torch.cat(batch.reward)

        return batch,state_batch,action_batch,reward_batch,non_final_next_states

    def get_expected_state_action_values(self):
        self.main_q_model.eval()
        self.target_q_model.eval()
        self.state_action_values = self.main_q_model(self.state_batch).gather(1,self.action_batch)
        self.memory.pushMaxQ(torch.mean(self.state_action_values).detach().item())
        non_final_mask = torch.BoolTensor(tuple(map(lambda s:s is not None, self.batch.next_state))).to(device)
        next_state_values = torch.zeros(BATCH_SIZE).to(device)
        a_m = torch.zeros(BATCH_SIZE).type(torch.LongTensor).to(device)##なんでこんな形なん？
        # print(a_m.dtype, non_final_mask.dtype, self.non_final_next_states.dtype)
        a_m[non_final_mask] = self.main_q_model(self.non_final_next_states).detach().max(1)[1]
        a_m_non_final_next_states = a_m[non_final_mask].view(-1,1)
        next_state_values[non_final_mask] = self.target_q_model(self.non_final_next_states).gather(1,a_m_non_final_next_states).detach().squeeze().to(device)
        expected_state_action_values = self.reward_batch + GAMMA * next_state_values
        return expected_state_action_values



    def update_main_q_model(self):
        self.main_q_model.train()
        loss = F.smooth_l1_loss(self.state_action_values,self.expected_state_action_values.unsqueeze(1))
        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()


    def update_target_q_model(self):
        self.target_q_model.load_state_dict(self.main_q_model.state_dict())
        
