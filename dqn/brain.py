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


device = "cuda" if torch.cuda.is_available() else "cpu"
print("brain use {}".format(device))
    

import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

class ReplayMemory():
    def __init__(self):
        self.memory = []
        self.index = 0
        self.win_memory = []
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

    def pushWinRatio(self,win):
        writer.add_scalar('data/win_ratio',win,len(self.win_memory))
        
        # if len(self.win_memory) == 0:
        #     self.win_memory.append(win)
        # else:
        #     size = len(self.win_memory)
        #     win = (self.win_memory[-1]*size + win)/(size+1)
        #     self.win_memory.append(win)

        self.win_memory.append(win)
        print("okwin",win,len(self.win_memory))

    def pushMaxQ(self,q):
        # if len(self.max_Q_memory) < 10:
        #     self.max_Q_memory.append(q)
        # else:
        #     ave_q = (self.max_Q_memory[-1]*10 - self.last_q + q)/10
        #     self.max_Q_memory.append(ave_q)
        # self.last_q = q
        self.max_Q_memory.append(q)
        writer.add_scalar('data/vote_max_Q',q,len(self.max_Q_memory))
        print("okQ",q)


class Model(nn.Module):
    def __init__(self,n_input,n_hidden,n_output):
        super(Model,self).__init__()
        self.fc1 = nn.Linear(n_input,n_hidden)
        self.fc2 = nn.Linear(n_hidden,n_hidden)
        self.fc3 = nn.Linear(n_hidden,n_output)


    def forward(self,x):
        x = self.fc1(x)
        x = F.relu(x)
        x = self.fc2(x)
        x = F.relu(x)
        x = self.fc3(x)
        return x

class Brain():
    def __init__(self,n_input,n_hidden,n_output):
        self.n_input = n_input
        self.n_hidden = n_hidden
        self.n_output = n_output

        self.memory = ReplayMemory()
        self.model = Model(n_input=n_input,n_hidden=n_hidden,n_output=n_output).to(device)
        # print("brain:",self.model,sep='\n')

        self.optimizer = optim.Adam(self.model.parameters(),lr=0.0001)



    def replay(self):
        if len(self.memory) < BATCH_SIZE:
            return 

        
        batch = Transition(*zip(*self.memory.sample(BATCH_SIZE)))
        state_batch = torch.cat(batch.state)
        action_batch = torch.cat(batch.action)
        reward_batch = torch.cat(batch.reward)
        non_final_next_states = torch.cat([s for s in batch.next_state if s is not None])
        self.model.eval()

        state_action_values = self.model(state_batch).gather(1,action_batch)
        self.memory.pushMaxQ(torch.mean(state_action_values).detach().item())
        non_final_mask = torch.ByteTensor(
            tuple(map(lambda s:s is not None, batch.next_state))
        )
        next_state_values = torch.zeros(BATCH_SIZE)

        non_final_next_states = non_final_next_states.to(device)
        next_state_values = next_state_values.to(device)

        # print(non_final_next_states.shape)
        # print(next_state_values[non_final_mask].shape)
        next_state_values[non_final_mask] = self.model(non_final_next_states).max(1)[0].detach()

        expected_state_action_values = reward_batch + GAMMA * next_state_values

        self.model.train()
        loss = F.smooth_l1_loss(state_action_values,expected_state_action_values.unsqueeze(1))

        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()



    def get_output(self,state):
        # state = torch.FloatTensor(np.arange(1111).reshape(1,-1))
        return self.model(state).to("cpu")