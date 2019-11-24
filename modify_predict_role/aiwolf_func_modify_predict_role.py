import aiwolfpy
import aiwolfpy.contentbuilder as cb
import numpy as np
import re
import os
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import chainer
import chainer.links as L
import chainer.functions as F
from chainer import serializers
import time

import matplotlib.pyplot as plt
import collections
from collections import defaultdict


import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))

from modify_vector.aiwolf_func_modify_vector import *

class modify_predict_role_data_info(modify_vector_data_info):
# class data_info():


    def __init__(self,agent_num=5,train_mode=False,train_times=10000,each_model=True):
        # super(self).__init__()
        self.Time = time.time()
        self.agent_num = agent_num
        self.train_mode = train_mode
        self.train_times = train_times
        self.each_model = each_model
        self.train_cnt = 1
        if self.agent_num <= 6:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2,"POSSESSED":3}   
        elif self.agent_num == 7:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2}
        elif self.agent_num <= 9:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2,"MEDIUM":5}
        else:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2,"POSSESSED":3,"BODYGUARD":4,"MEDIUM":5}      
        self.num_to_role = {v:k for k,v in self.role_to_num.items()}
        self.human_list = ["HUMAN","VILLAGER","SEER","BODYGUARD","MEDIUM"]
        self.werewolf_list = ["POSSESSED","WEREWOLF"]
        self.role_num = len(self.role_to_num)


        if self.agent_num == 5:
            self.utiwake = {"VILLAGER":2,"SEER":1,"POSSESSED":1,"WEREWOLF":1}
        elif self.agent_num == 6:
            self.utiwake = {"VILLAGER":3,"SEER":1,"POSSESSED":1,"WEREWOLF":1}



        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.last_declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.last_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.estimate_list = np.zeros((self.agent_num,self.agent_num,self.role_num),dtype=np.int32) 
        self.co_list = np.zeros((self.agent_num,self.role_num),dtype=np.int32) 
        self.seer_co_oder = np.zeros(self.agent_num,dtype=np.int32)
        self.divined_list = np.zeros((self.agent_num,self.agent_num,2)) #0:HUMAN 1:WEREWOLF
        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_another_people = np.zeros(self.agent_num,dtype=np.int32)
        self.alive_list = np.zeros((self.agent_num,3),dtype=np.int32)
        ##次元数を３次元に分離した
        self.alive_list[:,0] = 1
        self.alive_to_num = {"alive":0,"dead":1,"execute":2}

        self.ag_esti_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.disag_esti_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)

        #新たな特徴量
        self.myrole = np.zeros((self.role_num,1),dtype=np.int32)
        self.talk_cnt = np.zeros((self.agent_num,1),dtype=np.int32)


        self.createDailyVector()
        
        # self.player_vector_length = self.daily_vector.shape[1] + 1
        self.daily_vector_length = self.daily_vector.shape[0]*self.daily_vector.shape[1] + 1 + self.daily_vector.shape[1]
        print(self.daily_vector_length)


        self.role_cnt = defaultdict(int)
        self.predict_cnt = defaultdict(int)
        self.correct_predict_cnt = defaultdict(int)
        self.using_alive_info_cnt_daily  = 0
        self.using_alive_info_cnt_player = 0

        self.utiwake_cnt = 0


        # self.predict_net = [predict_role(n_input=self.daily_vector_length, n_hidden=200, n_output=self.agent_num*self.role_num) for i in range(self.agent_num)]
        # self.player_net = [predict_role(n_input=self.player_vector_length,n_hidden=50,n_output=self.role_num) for i in range(self.agent_num)]
        self.predict_net = [predict_role(n_input=self.daily_vector_length, n_hidden=500, n_output=self.agent_num*self.role_num, agent_num=self.agent_num,role_num=self.role_num) for i in range(self.agent_num)]


        if self.each_model == True:
            if self.train_mode == False:
                for i in range(len(self.predict_net)):
                    serializers.load_npz('./predict_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/modify_predict_role_train_daily_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.predict_net[i].net)
            # if self.predict_train == False:
            #     for i in range(len(self.player_net)):
            #         serializers.load_npz('./player_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/modify_predict_role_train_player_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.player_net[i].net)
        else:
            if self.train_mode == False:
                serializers.load_npz("./predict_model/agent"+str(self.agent_num)+"/one_model/modify_predict_role_train_daily_num_"+str(self.agent_num)+"_train_10000.net", self.predict_net[0].net)
            # if self.predict_train == False:
            #     serializers.load_npz("./player_model/agent"+str(self.agent_num)+"/one_model/modify_predict_role_train_player_num_"+str(self.agent_num)+"_train_10000.net", self.player_net[0].net)

        self.graph_name = 'modify_predict_role_'
        self.graph_name += "agent_"+str(self.agent_num)+"_"
        self.graph_name += "train_" if self.train_mode == True else "test_"
        self.graph_name += str(self.train_times)+"_"
        self.graph_name += "each_model" if self.each_model == True else "one_model"

    def createDailyVector(self):
        #カミングアウトのリスト　占い結果　占い師とカミングアウトした順番　前回の投票宣言　前回の投票先　生死情報　肯定的意見　否定的意見　発話の割合
        # print(self.talk_cnt/np.sum(self.talk_cnt))
        if np.sum(self.talk_cnt) == 0:
            self.daily_vector = np.hstack((self.co_list,self.divined_list.reshape(self.agent_num,-1),self.seer_co_oder.reshape(-1,1),self.last_declaration_vote_list,self.last_vote_list,self.alive_list,self.ag_esti_list,self.disag_esti_list,self.talk_cnt))
        else:
            self.daily_vector = np.hstack((self.co_list,self.divined_list.reshape(self.agent_num,-1),self.seer_co_oder.reshape(-1,1),self.last_declaration_vote_list,self.last_vote_list,self.alive_list,self.ag_esti_list,self.disag_esti_list,self.talk_cnt/np.sum(self.talk_cnt)))
        # print(self.daily_vector.shape)

    def initialize(self, base_info, diff_data, game_setting):
        # super(self).initialize()
        self.base_info = base_info
        self.diff_data = diff_data
        self.game_setting = game_setting

        self.declaration_vote_list.fill(0)
        self.vote_list.fill(0)
        self.last_declaration_vote_list.fill(0)
        self.last_vote_list.fill(0)
        self.daily_vector.fill(0)
        self.estimate_list.fill(0)
        self.co_list.fill(0)
        self.seer_co_cnt = 1
        self.seer_co_oder.fill(0)
        self.seer_have_been_co = False
        self.have_ever_vote = False
        self.divined_list.fill(0)
        self.comingout = ''
        self.myresult = ''
        self.not_reported = False
        self.do_fake_report = False
        self.done_last_commingout = False
        self.vote_declare = False

        self.fake_role = ''
        if self.base_info['myRole']=='POSSESSED':
            self.fake_role = 'SEER'

        self.declaration_vote_list.fill(0)
        self.vote_list.fill(0)
        self.vote_another_people.fill(0)

        self.alive_list.fill(0)
        self.alive_list[:,0] = 1

        self.myrole[self.role_to_num[self.base_info["myRole"]]] = 1
        self.talk_cnt.fill(0)

        self.ag_esti_list.fill(0)
        self.disag_esti_list.fill(0)

        self.predict_x = [[]]
        self.predict_t = [0 for i in range(self.agent_num*self.role_num)]
        # self.player_x = [[]for i in range(self.agent_num)]
        # self.predict_t = [0 for i in range(self.agent_num)]


    def randomSelect(self):
        while(True):
            target = np.random.randint(0,self.game_setting["playerNum"])
            if target != self.base_info["agentIdx"]-1 and self.alive_list[target][self.alive_to_num["alive"]]==1:
                return target

    def selectAgent(self,target_role):
        # if self.each_model == True:
        #     use_model = self.base_info["day"]
        # else:
        #     use_model = 0

        # self.createDailyVector()
        # est_werewolf_list = self.predict_net[use_model].net(self.createXDailyData().reshape(1,-1)).reshape(-1).array
        # est_werewolf_list = list(zip(est_werewolf_list,range(1,len(est_werewolf_list)+1)))
        # est_role_list = []

        # player_x_data = self.createXPlayerData()
        # for i in range(self.agent_num):
        #     role = np.argmax(self.player_net[use_model].net(player_x_data[i,:].reshape(1,-1)).reshape(-1).array)
        #     # role = [key for key,value in self.role_to_num.items() if value == role][0]
        #     role = self.num_to_role[role]
        #     est_role_list.append(role)

        # # print(est_werewolf_list)
        # # print(est_role_list)
        # if target_role == "WEREWOLF":
        #     tmp = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "WEREWOLF"]
        #     if len(tmp) != 0 and not((len(tmp)==1) and tmp[0][1]==self.base_info["agentIdx"]):
        #         est_werewolf_list = tmp
        #     # print("after",est_werewolf_list)
        #     est_werewolf_list = sorted(est_werewolf_list,reverse=True)
        #     for _,target in est_werewolf_list:
        #         if self.alive_list[target-1][self.alive_to_num["alive"]] == 0 and target != self.base_info['agentIdx']:
        #             return target
        #     return -1
    
        # elif target_role == "SEER":
        #     # print([x for i,x in zip(est_role_list,est_werewolf_list)])
        #     est_seer_list = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "SEER"]
        #     est_seer_list = sorted(est_werewolf_list,reverse=True)
        #     for _,target in est_seer_list:
        #         if self.alive_list[target-1][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']:
        #             return target
        #     return -1
        # elif target_role == "VILLAGER":
        #     # print([x for i,x in zip(est_role_list,est_werewolf_list)])
        #     est_villager_list = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "villager"]
        #     est_villager_list = sorted(est_werewolf_list,reverse=True)
        #     for _,target in est_villager_list:
        #         if self.alive_list[target-1][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']:
        #             return target
        #     return -1
        # elif target_role == "POSSESSED":
        #     # print([x for i,x in zip(est_role_list,est_werewolf_list)])
        #     est_possessed_list = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "possessed"]
        #     est_possessed_list = sorted(est_werewolf_list,reverse=True)
        #     for _,target in est_possessed_list:
        #         if self.alive_list[target-1][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']:
        #             return target
        #     return -1        
        # else:
        #     return -1
        return -1 

    def createXPredictData(self):
        common_vector = [self.base_info["day"]]
        alpha_common_vector = self.daily_vector[self.base_info["agentIdx"]-1,:]
        self.createDailyVector()
        # print(np.hstack((self.daily_vector.reshape(-1),common_vector, alpha_common_vector)).astype(np.float32).shape)
        return np.hstack((self.daily_vector.reshape(-1),common_vector, alpha_common_vector)).astype(np.float32)


  


    def updateVector(self):
        # common_vector = [self.base_info["day"]]
        # alpha_common_vector = self.daily_vector[self.base_info["agentIdx"]-1,:]
        # self.createDailyVector()

        # player_x_data = self.createXPlayerData()

        # for i in range(len(self.daily_vector)):
        #     self.player_x[self.base_info["day"]].append(player_x_data[i,:].tolist())
        self.predict_x.append(self.createXPredictData().tolist())
        # for i in range(len(self.predict_x)):
        #     print(len(self.predict_x[i]))
        # print(self.player_x)

    def countEachRole(self,index):
        agent = self.diff_data["agent"][index]-1
        role = self.diff_data["text"][index].split(' ')[2]
        self.predict_t[self.role_num*agent+self.role_to_num[role]] = 1


    def decode(self,line):
        tmp = []
        for l in line:
            # print(l)
            # print(np.where(l==True)[0])
            tmp.append(np.where(l==True)[0][0])
        return tmp


    def update_predict_result(self,y,t):
        y = self.decode(y.reshape(self.agent_num,self.role_num))
        t = self.decode(t.reshape(self.agent_num,self.role_num))
        # print(y)
        # print(t)
        for y_role, t_role in zip(y,t):
            self.predict_cnt[self.num_to_role[y_role]] += 1
            self.role_cnt[self.num_to_role[t_role]] += 1
            if y_role == t_role:
                self.correct_predict_cnt[self.num_to_role[t_role]]+=1
        # print(self.correct_predict_cnt)

        if collections.Counter([self.num_to_role[role] for role in y]) == self.utiwake:
            self.utiwake_cnt+= 1

        for agent,y_role in enumerate(y):
            if self.num_to_role[y_role] in self.werewolf_list:
                if self.alive_list[agent][self.alive_to_num["dead"]] != 1:
                    self.using_alive_info_cnt_player += 1

    def predict_model_train(self):
        for i in range(len(self.predict_net)):
            if 10 < len(self.predict_net[i].memory):
                loss, accuracy = self.predict_net[i].train()

                self.predict_net[i].memory.addLossAccuracy(loss,accuracy)

    def predict_model_eval(self):
        if self.each_model == True:
            for i in range(1,len(self.predict_x)):
                loss, accuracy, predict_y = self.predict_net[i].eval(np.array(self.predict_x[i]).reshape(1,-1).astype(np.float32), np.array(self.predict_t).reshape(1,-1).astype(np.int32))
                # print(predict_y,self.predict_t)
                self.update_predict_result(predict_y, np.array(self.predict_t).astype(np.int32))
                self.predict_net[i].memory.addLossAccuracy(loss,accuracy)


        else:
            for i in range(1,len(self.predict_x)):
                loss, accuracy, predict_y = self.predict_net[0].eval(np.array(self.predict_x[i]).reshape(1,-1).astype(np.float32), np.array(self.predict_t).reshape(1,-1).astype(np.int32))
                self.update_predict_result(predict_y, np.array(self.predict_t).astype(np.int32))
                self.predict_net[i].memory.addLossAccuracy(loss,accuracy)


    def save_each_model(self):
        for i in range(len(self.predict_net)):
            daily_path = './net_folder/predict_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/'
            file_name = 'modify_predict_role_train_daily_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_'+str(self.train_cnt)+'.net'
            os.makedirs(daily_path, exist_ok=True)
            chainer.serializers.save_npz(daily_path+file_name, self.predict_net[i].net)

        # for i in range(len(self.player_net)):
        #     player_path = './net_folder/player_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/'
        #     file_name = 'modify_predict_role_train_player_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_'+str(self.train_cnt)+'.net'
        #     os.makedirs(player_path, exist_ok=True)
        #     chainer.serializers.save_npz(player_path+file_name, self.player_net[i].net)

    def save_one_model(self):
        daily_path = './net_folder/predict_model/agent'+str(self.agent_num)+'/one_model/'
        os.makedirs(daily_path,exist_ok=True)
        file_path = 'modify_predict_role_train_daily_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        chainer.serializers.save_npz(daily_path+file_path, self.predict_net[0].net)
        # player_path = './net_folder/player_model/agent'+str(self.agent_num)+'/one_model/'
        # os.makedirs(player_path,exist_ok=True)
        # file_path = 'modify_predict_role_train_player_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        # chainer.serializers.save_npz(player_path+file_path, self.player_net[0].net)

    def addVectorToEachModel(self):
        if self.train_mode == True:
            for i in range(1,len(self.predict_x)):
                self.predict_net[i].addVector(self.predict_x[i],self.predict_t)
        # if self.predict_train == True:
        #     for i in range(len(self.player_x)):
        #         for j in range(len(self.player_x[i])):
        #             self.player_net[i].addVector(self.player_x[i][j],[self.predict_t[j]])

    def addVectorToOneModel(self):
        if self.train_mode == True:
            for i in range(1,len(self.predict_x)):
                self.predict_net[0].addVector(self.predict_x[i],self.predict_t)
        # if self.predict_train == True:
        #     for i in range(len(self.player_x)):
        #         for j in range(len(self.player_x[i])):
        #             self.player_net[0].addVector(self.player_x[i][j],[self.predict_t[j]])

    def display_game_result(self):
        sum_role_pred = 0
        sum_correct_pred= 0
        sum_predict_cnt = np.sum([value for value in self.role_cnt.values()])

        for role in self.role_to_num.keys():
            print("{:<10}, {:>10}times, {:>10}predicts".format(role , self.role_cnt[role],self.predict_cnt[role]))
            if self.role_cnt[role] != 0 and self.predict_cnt[role] != 0:
                print("{:<10} accuracy:{:<.2f},    recall:{:<.2f},   precision:{:<.2f}\n".format(role, (sum_predict_cnt-self.predict_cnt[role]-self.role_cnt[role]+2*self.correct_predict_cnt[role])/sum_predict_cnt,self.correct_predict_cnt[role]/self.role_cnt[role], self.correct_predict_cnt[role]/self.predict_cnt[role]))
                # print("TP:{}  TN:{}  FP:{}  FN{}".format(self.correct_predict_cnt[role],self.predict_cnt[role]-self.correct_predict_cnt[role],self.role_cnt[role]-self.correct_predict_cnt[role], (sum_predict_cnt-self.predict_cnt[role]-self.role_cnt[role]+self.correct_predict_cnt[role]),))
                sum_correct_pred += self.correct_predict_cnt[role]
                sum_role_pred += self.role_cnt[role]

        for i in range(len(self.predict_net)):
            if 0 < len(self.predict_net[i].memory.accuracy_memory):
                # print(len(self.player_net[i].memory.accuracy_memory))
                print("day{:<2}  accuracy:{:<.2f}".format(i,np.mean(self.predict_net[i].memory.accuracy_memory)))
        
        # print(self.using_alive_info_cnt_daily, self.using_alive_info_cnt_player)
        print("correct utiwake is ",self.utiwake_cnt)

    def plot_accu_loss(self):
        fig = plt.figure(figsize=(12.0,8.0))
        graph_name = "agent_"+str(self.agent_num)+"_"
        graph_name += "train_" if self.train_mode == True else "test_"
        graph_name += str(self.train_times)+"_"
        graph_name += "each_model" if self.each_model == True else "one_model"
        fig.suptitle('modify_predict_role_ '+graph_name, fontsize=20)
        daily_loss = fig.add_subplot(2,2,1)
        plt.title("daily_loss")
        daily_accu = fig.add_subplot(2,2,2)
        plt.title("daily_accuracy")
        for i in range(len(self.predict_net)):
            if 0 < len(self.predict_net[i].memory.loss_memory):
                daily_loss.plot(self.predict_net[i].memory.loss_memory,label="day"+str(i))
                plt.legend()

            if 0 < len(self.predict_net[i].memory.accuracy_memory):
                daily_accu.plot(self.predict_net[i].memory.accuracy_memory,label="day"+str(i))
                plt.legend()
        # plt.show()
        os.makedirs("graph_folder", exist_ok=True)
        plt.savefig("graph_folder/"+graph_name+'.png')


    def finish(self):
        if self.each_model == True:
            self.addVectorToEachModel()
        else:
            self.addVectorToOneModel()


        if self.train_mode == True:
            self.predict_model_train()
        else:
            self.predict_model_eval()


        # if self.predict_train == True:
        #     self.player_model_train()
        # else:
        #     self.player_model_eval()

        if(self.train_cnt == self.train_times):
            if self.train_mode == False:
                self.display_game_result()
            # else:
            #     self.save_one_model()
            #     self.save_each_model()

            self.plot_accu_loss()

        self.train_cnt += 1


        if self.train_cnt%(self.train_times//10) == 0:
            sec = round(time.time()-self.Time)
            print("train:{:<10}time is {:<2}hour {:<2}minutes {:<2}sec".format(self.train_cnt,sec//3600,(sec%3600)//60,(sec%60)))
            if self.train_mode == True:
                if self.each_model == True:
                    self.save_each_model()
                else:
                    self.save_one_model()


# class predict_werewolf():

#     def train(self):
#         x,t = self.memory.choice(10)
#         with chainer.using_config("train", True), chainer.using_config("enable_backprop", True):
#             y = self.net(x)
#             loss = F.sigmoid_cross_entropy(y,t)
#             y = y.array
#             y = np.array(np.argsort(np.argsort(-y)) < np.count_nonzero(t[0]))
#             accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)
#             self.net.cleargrads()
#             loss.backward()
#             self.optimizer.update()
    
#             return loss.array, accuracy

#     def eval(self,x,t):
#         with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
#             y = self.net(x)
#             loss = F.sigmoid_cross_entropy(y,t)
#             y = y.array
#             y = np.array(np.argsort(np.argsort(-y)) < np.count_nonzero(t[0]))
#             accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)

#         return loss.array, accuracy
    
#     def addVector(self, x, t):
#         self.memory.append(x,t)

class predict_role(predict_role):
            

    def __init__(self,n_input,n_hidden,n_output,agent_num,role_num):
        self.net = MLP(n_input=n_input,n_hidden=n_hidden,n_output=n_output)
        self.optimizer = chainer.optimizers.Adam()
        self.optimizer.setup(self.net)
        self.memory = Memory()
        self.agent_num = agent_num
        self.role_num = role_num

    def decode(self,line):
        tmp = []
        for l in line:
            # print(l)
            # print(np.where(l==True)[0])
            tmp.append(np.where(l==True)[0][0])
        return tmp

    def train(self):
        x,t = self.memory.choice(10)
        with chainer.using_config("train", True), chainer.using_config("enable_backprop", True):
            y = self.net(x)
            loss = F.sigmoid_cross_entropy(y,t)
            y = y.array.reshape((-1,self.agent_num,self.role_num))
            t = t.reshape((-1,self.agent_num,self.role_num))
            y = np.array(np.argsort(np.argsort(-y,axis=2),axis=2) < 1)
            # print(y[0])
            # print(t[0])
            # print()
            accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)
            # print(np.count_nonzero(t),np.count_nonzero(np.logical_and(y,t)))
            # print(accuracy)
            self.net.cleargrads()
            loss.backward()
            self.optimizer.update()
    
            return loss.array, accuracy

    def eval(self,x,t):
        with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
            y = self.net(x)
            loss = F.sigmoid_cross_entropy(y,t)
            y = y.array.reshape((self.agent_num,self.role_num))
            t = t.reshape((self.agent_num,self.role_num))
            # print(y)
            # print(t)
            y = np.array(np.argsort(np.argsort(-y,axis=1),axis=1) < 1)
            # print(y)
            # print(t)
            accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)
            # y = self.decode(y)
            # print(y)
            y = y.reshape(-1)
        return loss.array, accuracy, y


    def addVector(self, x, t):
        self.memory.append(x,t)
