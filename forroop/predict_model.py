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
LOSSACC = namedtuple("LOSSACC",("loss","accuracy"))

device = "cuda" if torch.cuda.is_available() else "cpu"
# print("predict model use {}".format(device))

import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

# from torch.utils.tensorboard import SummaryWriter


BATCH_SIZE = 16
CAPACITY = 100000

class Memory():
    def __init__(self):
        # # self.writer = SummaryWriter()
        self.memory = []
        self.index = 0
        self.loss_accuracy_memory = defaultdict(list)

    def push(self,x,t):
        if len(self.memory) < CAPACITY:
            self.memory.append(None)
        
        self.memory[self.index] = X_T(x.to(device),t.to(device))
        self.index = (self.index + 1)%CAPACITY
    
    def sample(self,batch_size):
        return random.sample(self.memory,batch_size)

    def __len__(self):
        return(len(self.memory))

    def getLossAccuracy(self,day):
        if 0 < len(self.loss_accuracy_memory[day]):
            l_a = LOSSACC(*(zip(*(self.loss_accuracy_memory[day]))))
            return l_a.loss,l_a.accuracy
        return None,None

    def pushLossAccuracy(self,batch,day):
        loss,accuracy = batch.loss,batch.accuracy
        # if 0 < len(self.loss_accuracy_memory[day]):
        #     l_a = self.loss_accuracy_memory[day][-1]
        #     size = len(self.loss_accuracy_memory[day])
        #     loss = (l_a.loss*size+loss)/(size+1)
        #     accuracy = (l_a.accuracy*size+accuracy)/(size+1)
        #     self.loss_accuracy_memory[day].append(LOSSACC(loss,accuracy))
        # else:
        #     self.loss_accuracy_memory[day].apend(batch)

        self.loss_accuracy_memory[day].append(batch)
        # self.writer.add_scalars('data/total_loss',{"day_"+str(day):loss},len(self.loss_accuracy_memory[day]))
        # self.writer.add_scalars('data/total_accuracy',{"day_"+str(day):accuracy},len(self.loss_accuracy_memory[day]))




class Model(nn.Module):
    def __init__(self,n_input,n_hidden,agent_num,role_num):
        self.agent_num = agent_num
        self.role_num  = role_num
        super(Model,self).__init__()
        self.fc1 = nn.Linear(n_input,n_hidden)
        self.fc2 = nn.Linear(n_hidden,n_hidden)
        self.fc3 = nn.Linear(n_hidden,agent_num*role_num)
        self.sigmoid = nn.Sigmoid()
        self.d1 = nn.Dropout(0.5)
        self.d2 = nn.Dropout(0.5)


    def forward(self,x):
        x = F.relu(self.fc1(x))
        x = self.d1(x)
        x = F.relu(self.fc2(x))
        x = self.d2(x)
        x = self.fc3(x)
        x = self.sigmoid(x)
        # print(x.shape)
        # x = x.reshape(-1,self.agent_num,self.role_num)
        # print(x.shape)
        return x

class PredictRole():
    def __init__(self,n_input,n_hidden,agent_num,role_num):
        self.memory = Memory()

        self.model = Model(n_input=n_input,n_hidden=n_hidden,agent_num=agent_num,role_num=role_num).to(device)
        # print("pred model:",self.model,sep='\n')
        self.criterion = nn.BCELoss()
        self.optimizer = optim.Adam(self.model.parameters())



    def eval(self,state,label,agent_num,role_num):
        out = []
        self.model.eval()
        with torch.no_grad():
            for i,x in enumerate(state):
                if 0 < len(x):
                    x = torch.tensor(x).view(1,len(x),-1).float().to(device)
                    pred = self.model(x)
                    label_batch = torch.tensor(label).repeat(pred.shape[1],1).view(1,pred.shape[1],-1).float().to(device)
                    loss = self.criterion(pred,label_batch)

                    pred = pred.to("cpu").detach().reshape(-1,agent_num,role_num).numpy()
                    tmp_pred = np.array(np.argsort(np.argsort(-pred,axis=2),axis=2)<1)
                    label_batch = label_batch.to("cpu").detach().reshape(-1,agent_num,role_num)
                    accuracy = np.count_nonzero(np.logical_and(tmp_pred,label_batch))/np.count_nonzero(label_batch)

                    self.memory.pushLossAccuracy(batch=LOSSACC(loss.to("cpu").detach(),accuracy),day=i)
                    for sth in pred:
                        out.append(sth)
        # print(type(pred))
        return pred

        


    def train(self,agent_num,role_num):
        self.model.train()
        if len(self.memory) < BATCH_SIZE:
            return None

        
        batch = X_T(*zip(*self.memory.sample(BATCH_SIZE)))
        state_batch = torch.cat(batch.state).view(BATCH_SIZE,1,-1).to(device)
        label_batch = torch.cat(batch.label).view(BATCH_SIZE,1,-1).to(device)
        self.optimizer.zero_grad()
        pred = self.model(state_batch)
        loss = self.criterion(pred,label_batch)
        loss.backward()
        self.optimizer.step()

        loss = loss.to("cpu")
        pred = pred.to("cpu")
        pred = pred.detach().reshape(-1,agent_num,role_num)
        pred = np.array(np.argsort(np.argsort(-pred,axis=2),axis=2)<1)
        label_batch = label_batch.to("cpu").detach().reshape(-1,agent_num,role_num)
        accuracy = np.count_nonzero(np.logical_and(pred,label_batch))/np.count_nonzero(label_batch)

        self.memory.pushLossAccuracy(batch=LOSSACC(loss.detach(),accuracy),day=0)
                
    def get_output(self,state):
        self.model.eval()
        with torch.no_grad():
            state = state.to(device)
            # print(self.model(state).to("cpu"))
            return self.model(state).to("cpu")