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


BATCH_SIZE = 32
CAPACITY = 100000
GAMMA = 0.9


import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

class ReplayMemory():
    def __init__(self):
        self.memory = []
        self.index = 0
        self.win_memory = []

    def push(self,state,action,next_state,reward):
        if len(self.memory) < CAPACITY:
            self.memory.append(None)
        
        self.memory[self.index] = Transition(state,action,next_state,reward)
        self.index = (self.index + 1)%CAPACITY
    
    def sample(self,batch_size):
        return random.sample(self.memory,batch_size)

    def __len__(self):
        return(len(self.memory))

    def pushWinRatio(self,win):
        if len(self.win_memory) == 0:
            self.win_memory.append(win)
        else:
            size = len(self.win_memory)
            win = (self.win_memory[-1]*size + win)/(size+1)
            self.win_memory.append(win)



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

        self.model = Model(n_input=n_input,n_hidden=n_hidden,n_output=n_output)
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
        non_final_mask = torch.ByteTensor(
            tuple(map(lambda s:s is not None, batch.next_state))
        )
        next_state_values = torch.zeros(BATCH_SIZE)
        next_state_values[non_final_mask] = self.model(non_final_next_states).max(1)[0].detach()

        expected_state_action_values = reward_batch + GAMMA * next_state_values

        self.model.train()
        loss = F.smooth_l1_loss(state_action_values,expected_state_action_values.unsqueeze(1))

        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()