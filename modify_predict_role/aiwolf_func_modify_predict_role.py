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


    def __init__(self,agent_num=5,train_mode=False,train_times=1000,net_load=False,test_train_mode=False,each_model=False,epsilon=0.3):
        # super(self).__init__()
        self.Time = time.time()
        self.agent_num = agent_num
        self.train_mode = train_mode
        self.train_times = train_times
        self.each_model = each_model
        self.test_train_mode = test_train_mode
        self.epsilon = epsilon
        self.net_load = net_load
        self.train_cnt = 1

        if self.agent_num <= 6:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2,"POSSESSED":3}   
        elif self.agent_num == 7:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2}
        elif self.agent_num <= 9:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2,"MEDIUM":5}
        else:
            self.role_to_num = {"VILLAGER":0,"SEER":1,"WEREWOLF":2,"POSSESSED":3,"BODYGUARD":4,"MEDIUM":5}      

        if self.agent_num == 5:
            self.utiwake = {"VILLAGER":2,"SEER":1,"POSSESSED":1,"WEREWOLF":1}
        elif self.agent_num == 6:
            self.utiwake = {"VILLAGER":3,"SEER":1,"POSSESSED":1,"WEREWOLF":1}
        elif self.agent_num == 7:
            self.utiwake = {"VILLAGER":4,"SEER":1,"WEREWOLF":2}
        elif self.agent_num == 10:
            self.utiwake = {"VILLAGER":4,"SEER":1,"POSSESSED":1,"WEREWOLF":2,"MEDIUM":1,"BODYGUARD":1}
        elif self.agent_num == 15:
            self.utiwake = {"VILLAGER":8,"SEER":1,"POSSESSED":1,"WEREWOLF":3,"MEDIUM":1,"BODYGUARD":1}
        else:
            self.utiwake = {"VILLAGER":8,"SEER":1,"POSSESSED":1,"WEREWOLF":3,"MEDIUM":1,"BODYGUARD":1}

        self.num_to_role = {v:k for k,v in self.role_to_num.items()}
        self.human_list = ["HUMAN","VILLAGER","SEER","BODYGUARD","MEDIUM"]
        self.werewolf_list = ["POSSESSED","WEREWOLF"]
        self.side_to_num = {"HUMAN":0,"VILLAGER":0,"SEER":0,"BODYGUARD":0,"MEDIUM":0,"POSSESSED":1,"WEREWOLF":1}
        self.role_num = len(self.role_to_num)

        self.max_day = self.agent_num  - (2*self.utiwake.get("WEREWOLF"))
        self.day = np.zeros(self.max_day).astype(np.float32)
        self.day[0] = 1



        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.last_declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.last_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.estimate_list = np.zeros((self.agent_num,self.agent_num,self.role_num),dtype=np.int32) 
        self.co_list = np.zeros((self.agent_num,self.role_num),dtype=np.int32) 
        self.seer_co_oder = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.divined_list = np.zeros((self.agent_num,self.agent_num,2)) #0:HUMAN 1:WEREWOLF
        self.identified_list = np.zeros((self.agent_num,self.agent_num,2))
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
        self.my_role = np.zeros((self.role_num),dtype=np.int32)
        self.talk_cnt = np.zeros((self.agent_num,1),dtype=np.int32)
        self.my_agent_id = np.zeros((self.agent_num),dtype=np.int32)
        self.my_agent_id[-1] = 1
        self.other_role = np.zeros((self.agent_num,2),dtype=np.float32)


        self.createDailyVector()
        self.createSubFeat()
        
        # self.player_vector_length = self.daily_vector.shape[1] + 1
        self.daily_vector_length = self.daily_vector.shape[0]*self.daily_vector.shape[1] + self.sub_feat.shape[0]
        print(self.daily_vector_length)
       

        self.role_cnt = defaultdict(int)
        self.predict_cnt = defaultdict(int)
        self.correct_predict_cnt = defaultdict(int)
        self.using_alive_info_cnt_daily  = 0
        self.alive_werewolf = 0

        self.utiwake_cnt = 0
        self.correct_myrole = 0
        self.trust_my_skill = 0
        self.not_trust_my_skill = 0

        self.t_role_cnt= np.array([self.utiwake[self.num_to_role[i]] for i in range(self.role_num)]).astype(np.float32)
        # self.predict_net = [predict_role(n_input=self.daily_vector_length, n_hidden=200, n_output=self.agent_num*self.role_num) for i in range(self.agent_num)]
        # self.player_net = [predict_role(n_input=self.player_vector_length,n_hidden=50,n_output=self.role_num) for i in range(self.agent_num)]
        self.predict_net = [predict_role(n_input=self.daily_vector_length, n_hidden=500, n_output=self.agent_num*self.role_num, agent_num=self.agent_num,role_num=self.role_num,t_role_cnt = self.t_role_cnt) for i in range(self.max_day+1)]


        if self.net_load == True or self.train_mode == False:
            if self.each_model == True:
                for i in range(len(self.predict_net)):
                    serializers.load_npz('./predict_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/modify_predict_role_train_daily_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.predict_net[i].net)
            # if self.predict_train == False:
            #     for i in range(len(self.player_net)):
            #         serializers.load_npz('./player_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/modify_predict_role_train_player_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.player_net[i].net)
            else:
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

        tmp = [0 for i in range(self.agent_num)]
        for i in range(len(self.seer_co_oder)):
            for j in range(len(self.seer_co_oder[i])):
                if self.seer_co_oder[i][j] == 1:
                    tmp[i] = j+1
        self.daily_vector = np.hstack((
                                    self.co_list,#カミングアウト役職
                                    self.divined_list.reshape(self.agent_num,-1),#占い結果
                                    # self.seer_co_oder,#占いカミングアウト順番
                                    np.array(tmp).reshape(-1,1),#占いカミングアウト順番
                                    self.last_declaration_vote_list,#前回までの投票宣言先
                                    self.last_vote_list,#前回までの投票先
                                    self.declaration_vote_list,#今回の投票宣言先
                                    self.alive_list,#生襲追の情報
                                    self.ag_esti_list,#肯定的意見の数
                                    self.disag_esti_list,#否定的意見の数
                                    ))

        if "MEDIUM" in self.role_to_num.keys():
            self.daily_vector = np.hstack((self.daily_vector,self.identified_list.reshape(self.agent_num,-1),)) #霊媒結果

        #発話割合
        if np.sum(self.talk_cnt) == 0:
            self.daily_vector = np.hstack((self.daily_vector,self.talk_cnt))
        else:
            self.daily_vector = np.hstack((self.daily_vector,self.talk_cnt/np.sum(self.talk_cnt)))
        # print(self.daily_vector.shape)

    def createSubFeat(self):
        common_feats = np.hstack((
            # self.day,#日にち
            np.where(self.day==1)[0][0]+1,#日にち
            self.my_agent_id,#自分の番号
            # self.daily_vector[np.where(self.my_agent_id==1)[0][0],:],#自分のプレイヤベクトル
            self.my_role,#自分の役職
            self.other_role.reshape(-1)#自分の主観情報
            ))

        # alpha_common_feats = self.daily_vector[np.where(self.my_agent_id == 1)[0][0],:]

        self.sub_feat = common_feats


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
        self.seer_co_cnt = 0
        self.seer_co_oder.fill(0)
        self.seer_have_been_co = False
        self.have_ever_vote = False
        self.divined_list.fill(0)
        self.identified_list.fill(0)
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

        self.my_role.fill(0)
        self.my_role[self.role_to_num[self.base_info["myRole"]]] = 1
        self.talk_cnt.fill(0)

        self.my_agent_id.fill(0)
        self.my_agent_id[self.base_info["agentIdx"]-1] = 1

        self.other_role.fill(0)
        for agent,role in self.base_info["roleMap"].items():
            agent = int(agent)-1
            self.other_role[agent][self.side_to_num[role]] = 1

        self.ag_esti_list.fill(0)
        self.disag_esti_list.fill(0)


        self.predict_x = [[] for i in range(len(self.predict_net))]
        self.predict_t = [0 for i in range(self.agent_num*self.role_num)]
        # self.player_x = [[]for i in range(self.agent_num)]
        # self.predict_t = [0 for i in range(self.agent_num)]


    def randomSelect(self):
        while(True):
            target = np.random.randint(0,self.game_setting["playerNum"])
            if target != self.base_info["agentIdx"]-1 and self.alive_list[target][self.alive_to_num["alive"]]==1:
                return target


    def selectAgent(self,target_role):
        #返り値は１始まり

        if self.train_mode == True:
            if np.random.random() < self.epsilon:
                return self.randomSelect()

        if self.each_model == True:
            use_model = self.base_info["day"]
        else:
            use_model = 0

        self.createDailyVector()
        est_role_list = self.predict_net[use_model].net(self.createXPredictData().reshape(1,-1)).reshape(-1).array.reshape(self.agent_num,self.role_num)

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
        #     # print(est_role_list)
        #     est_werewolf_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if self.num_to_role[role] == "WEREWOLF"]
        #     est_werewolf_list = sorted(est_werewolf_list,reverse=True)
        #     # print(est_werewolf_list)
        #     for _,target in est_werewolf_list:
        #         if self.alive_list[target][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']-1:
        #             return target + 1
    
        # elif target_role == "SEER":
        #     est_seer_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if self.num_to_role[role] == "SEER"]
        #     est_seer_list = sorted(est_seer_list,reverse=True)
        #     for _,target in est_seer_list:
        #         if self.alive_list[target][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']-1:
        #             return target + 1

        # elif target_role == "VILLAGER":
        #     # print([x for i,x in zip(est_role_list,est_werewolf_list)])
        #     est_villager_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if self.num_to_role[role] == "VILLAGER"]
        #     est_villager_list = sorted(est_villager_list,reverse=True)
        #     for _,target in est_villager_list:
        #         if self.alive_list[target][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']-1:
        #             return target

        # elif target_role == "POSSESSED":
        #     # print([x for i,x in zip(est_role_list,est_werewolf_list)])
        #     est_possessed_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if self.num_to_role[role] == "POSSESSED"]
        #     est_possessed_list = sorted(est_possessed_list,reverse=True)
        #     for _,target in est_possessed_list:
        #         if self.alive_list[target][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']-1:
        #             return target+1

        est_role_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if self.num_to_role[role] == target_role]
        est_role_list = sorted(est_role_list,reverse=True)

        for _,target in est_role_list:
            if self.alive_list[target][self.alive_to_num["alive"]] == 1 and target != self.base_info['agentIdx']-1:
                return target + 1
        return -1 


    def createXPredictData(self):
        # common_feats = np.hstack((self.base_info["day"],self.my_agent_id,self.my_role,))
        # alpha_common_feats = self.daily_vector[self.base_info["agentIdx"]-1,:]
        self.createSubFeat()
        self.createDailyVector()
        # print(np.hstack((self.daily_vector.reshape(-1),common_feats, alpha_common_feats)).astype(np.float32).shape)
        return np.hstack((self.daily_vector.reshape(-1),self.sub_feat.reshape(-1))).astype(np.float32)

  
    def understand_text(self,agent,talk_texts):
            '''talkのtext部分を解釈可能にparse'''
            talk_texts = talk_texts.split(' ')
            # print(talk_texts)
            if not talk_texts[0] in ["Skip","Over"]:
                self.talk_cnt[agent] += 1

            if(talk_texts[0]=="Skip"):
                None
            elif(talk_texts[0]=="Over"):
                None
            elif(talk_texts[0]=="VOTE"):
                target = re.search(r"[0-9][0-9]",talk_texts[1]).group()
                target = int(target) - 1
                self.declaration_vote_list[agent][target] = 1
            elif(talk_texts[0]=="COMINGOUT"):
                role = talk_texts[2]
                self.co_list[agent][self.role_to_num[role]] = 1

                if role == "SEER":
                    self.seer_have_been_co = True
                    self.seer_co_oder[agent][self.seer_co_cnt] = 1


            elif(talk_texts[0]=="ESTIMATE"):
                target = re.search(r"[0-9][0-9]",talk_texts[1]).group()
                target = int(target) - 1
                role = talk_texts[2]
                self.estimate_list[agent][target][self.role_to_num[role]] = 1
                if role in self.human_list:
                    self.ag_esti_list[agent][target] = 1
                elif role in self.werewolf_list:
                    self.disag_esti_list[agent][target] = 1

            elif(talk_texts[0]=="DIVINATION"):
                None
            elif(3<len(talk_texts) and talk_texts[2][1:]=="DIVINED"):
                # print(talk_texts[4][:-1])
                target = re.search(r"[0-9][0-9]",talk_texts[3]).group()
                target = int(target) - 1
                if(talk_texts[4][:-1]=="HUMAN"):
                    role = 0
                else:
                    role = 1
                self.divined_list[agent][target][role] = 1
            elif(3<len(talk_texts) and talk_texts[2][1:]=="IDENTIFIED"):
                target = re.search(r"[0-9][0-9]",talk_texts[3]).group()
                target = int(target) - 1
                if(talk_texts[4][:-1]=="HUMAN"):
                    role = 0
                else:
                    role = 1
                self.identified_list[agent][target][role] = 1
            elif(talk_texts[0]=="DIVINED"):
                # print(talk_texts)
                target = re.search(r"[0-9][0-9]",talk_texts[1]).group()
                target = int(target) - 1
                if target<self.agent_num and agent<self.agent_num:
                    if(talk_texts[2]=="HUMAN"):
                        role = 0
                    else:
                        role = 1
                    self.divined_list[agent][target][role] = 1
            elif(talk_texts[0]=="IDENTIFIED"):
                target = re.search(r"[0-9][0-9]",talk_texts[1]).group()
                target = int(target) - 1
                # print(self.identified_list[agent][target])
                if target<self.agent_num and agent<self.agent_num:
                    if(talk_texts[2]=="HUMAN"):
                        role = 0
                    else:
                        role = 1
                    self.identified_list[agent][target][role] = 1  
            elif(talk_texts[0]=="GUARD"):
                None
            elif(talk_texts[0]=="GUARDED"):
                None
            elif(talk_texts[0]=="ATTACK"):
                None
            elif(talk_texts[0]=="AGREE"):
                None
            elif(talk_texts[0]=="DISAGREE"):
                None
            elif(talk_texts[0]=="REQUEST"):
                None
            elif(talk_texts[0]=="BECAUSE"):
                None
            else:
                print(talk_texts,"ERROR")

    def updateVector(self):
        # common_feats = [self.base_info["day"]]
        # alpha_common_feats = self.daily_vector[self.base_info["agentIdx"]-1,:]
        # self.createDailyVector()

        # player_x_data = self.createXPlayerData()

        # for i in range(len(self.daily_vector)):
        #     self.player_x[self.base_info["day"]].append(player_x_data[i,:].tolist())
        self.predict_x[np.where(self.day == 1)[0][0]+1].append(self.createXPredictData().tolist())
        # for i in range(len(self.predict_x)):
        #     print(len(self.predict_x[i]))
        # print(self.player_x)

    def countEachRole(self,index):
        agent = self.diff_data["agent"][index]-1
        role = self.diff_data["text"][index].split(' ')[2]
        self.predict_t[self.role_num*agent+self.role_to_num[role]] = 1


    def decode(self,line):
        tmp = []
        # print(line) 
        for l in line:
            # print(tmp)
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
            # print(self.num_to_role[y_role], self.num_to_role[t_role])
        # print(self.correct_predict_cnt)
        # print()
        if collections.Counter([self.num_to_role[role] for role in y]) == self.utiwake:
            self.utiwake_cnt+= 1


        tmp = 0
        for agent,y_role in enumerate(y):
            if self.num_to_role[y_role] == "WEREWOLF" and self.alive_list[agent][self.alive_to_num.get("alive")] == 1:
                tmp += 1
            
            if self.num_to_role.get((np.where(self.my_role==1)[0][0])) in ["SEER","MEDIUM"]:
                if self.num_to_role[y_role] in self.human_list:
                    if self.divined_list[self.base_info["agentIdx"]-1][agent][1]==1 or self.identified_list[self.base_info["agentIdx"]-1][agent][1]==1:
                        self.not_trust_my_skill += 1
                    elif self.divined_list[self.base_info["agentIdx"]-1][agent][0]==1 or self.identified_list[self.base_info["agentIdx"]-1][agent][0]==1:
                        self.trust_my_skill += 1
                elif self.num_to_role[y_role] in self.werewolf_list:
                    if self.divined_list[self.base_info["agentIdx"]-1][agent][0]==1 or self.identified_list[self.base_info["agentIdx"]-1][agent][0]==1:
                        self.not_trust_my_skill += 1
                    elif self.divined_list[self.base_info["agentIdx"]-1][agent][1]==1 or self.identified_list[self.base_info["agentIdx"]-1][agent][1]==1:
                        self.trust_my_skill += 1
        if tmp != 0:
            self.alive_werewolf += 1

        if y[self.base_info["agentIdx"]-1] == t[self.base_info["agentIdx"]-1]:
            self.correct_myrole+=1

    def predict_model_train(self):
        for i in range(len(self.predict_net)):
            if 10 < len(self.predict_net[i].memory):
                loss, accuracy = self.predict_net[i].train()
                # print(loss,accuracy)
                self.predict_net[i].memory.addLossAccuracy(loss,accuracy)

    def predict_model_eval(self):
        # if self.each_model == True:
        #     for i in range(len(self.predict_x)):
        #         loss, accuracy, predict_y = self.predict_net[i].eval(np.array(self.predict_x[i]).reshape(1,-1).astype(np.float32), np.array(self.predict_t).reshape(1,-1).astype(np.int32))
        #         self.update_predict_result(predict_y, np.array(self.predict_t).astype(np.int32))
        #         self.predict_net[i].memory.addLossAccuracy(loss,accuracy)


        # else:
            # for i in range(1,len(self.predict_x)):
            #     index = i
            #     if self.max_day < index:
            #         index = self.max_day
            #     loss, accuracy, predict_y = self.predict_net[0].eval(np.array(self.predict_x[i]).reshape(1,-1).astype(np.float32), np.array(self.predict_t).reshape(1,-1).astype(np.int32))
            #     # print(predict_y,self.predict_t)
            #     self.update_predict_result(predict_y, np.array(self.predict_t).astype(np.int32))
            #     self.predict_net[index].memory.addLossAccuracy(loss,accuracy)
            for i in range(len(self.predict_x)):
                if 0 < len(self.predict_x[i]):
                    # print(np.array(self.predict_x[i]).reshape(1,-1).astype(np.float32))
                    # print(np.array(self.predict_t).reshape(1,-1).astype(np.int32))
                    # print(np.array(self.predict_x[i]).reshape(data_num,-1).astype(np.float32))
                    # print(np.tile(np.array(self.predict_t).reshape(1,-1),(data_num,1)).astype(np.int32))
                    for x in self.predict_x[i]:
                        loss, accuracy, predict_y = self.predict_net[0].eval(np.array(x).reshape(1,-1).astype(np.float32), np.array(self.predict_t).reshape(1,-1).astype(np.int32))
                        self.update_predict_result(predict_y, np.array(self.predict_t).astype(np.int32))
                        if self.train_mode==False:
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
        
        for i in range(1,len(self.predict_x)):
            if len(self.predict_x[i]) != 0:
                self.predict_net[i].addVector(self.predict_x[i],self.predict_t)
        # if self.predict_train == True:
        #     for i in range(len(self.player_x)):
        #         for j in range(len(self.player_x[i])):
        #             self.player_net[i].addVector(self.player_x[i][j],[self.predict_t[j]])

    def addVectorToOneModel(self):
        for i in range(1,len(self.predict_x)):
            if len(self.predict_x[i]) != 0:
                self.predict_net[0].addVector(self.predict_x[i],self.predict_t)
        # if self.predict_train == True:
        #     for i in range(len(self.player_x)):
        #         for j in range(len(self.player_x[i])):
        #             self.player_net[0].addVector(self.player_x[i][j],[self.predict_t[j]])

    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        self.diff_data = diff_data
        self.request = request
        self.day.fill(0)
        if self.base_info["day"] <= self.max_day:
            self.day[self.base_info["day"]-1] = 1
        else:
            self.day[-1] = 1
        # print(self.day,request) if 3 <= self.base_info["day"] else None
        # print("\n\nrequest:",request,sep='\n')
        # print("update: base_info=",base_info,sep='\n')
        # print("update: diff_data=",diff_data,sep='\n')

        ### edit from here ###

        for i in range(len(self.diff_data)):
            # print(self.diff_data["type"][i])
            if self.diff_data["type"][i] == "talk":
                self.updateTalk(i)
            elif self.diff_data["type"][i] == "vote":
                self.updateVoteList(i)
            elif self.diff_data["type"][i] == 'finish':
                self.countEachRole(i)
            elif self.diff_data["type"][i] == "dead" or self.diff_data["type"][i] == "execute":
                self.updateAliveList(i)
            elif self.diff_data['type'][i] == 'identify' or self.diff_data['type'][i] == 'divine' or self.diff_data['type'][i] == 'guard':
                self.getResult(i)
            # elif self.diff_data["type"][i] == "":

        if self.seer_have_been_co == True:
            self.seer_have_been_co = False
            self.seer_co_cnt += 1


        # self.updateTalk()
        if self.fake_role != '' and self.do_fake_report== False:
            self.do_fake_report = True
            self.getFakeResult()

        if request == 'DAILY_INITIALIZE' and 2 <= base_info["day"]:
            self.updateVote_declare()

        elif request == "DAILY_FINISH" and 1 <= base_info["day"]:
            #0:自分が人間判定された数 1:自分が人狼判定された数 2:占い師の名乗り出た順番 3:報告した人間の数 4:報告した人狼の数 5:発言と投票先が変わった数．6:生死(#alive:0 attacked:1 execute:-1)　7~11:肯定的意見の数　12~16:否定的意見の数        
            self.updateVector()
            if self.base_info['myRole'] == 'POSSESSED':
                self.do_fake_report = False


    def display_game_result(self):
        sum_role_pred = 0
        sum_correct_pred= 0
        sum_predict_cnt = np.sum([value for value in self.role_cnt.values()])
        sum_game_cnt = self.role_cnt.get("SEER")

        for role in self.role_to_num.keys():
            print("{:<10}, {:>10}times, {:>10}predicts".format(role , self.role_cnt[role],self.predict_cnt[role]))
            if self.role_cnt[role] != 0 and self.predict_cnt[role] != 0:
                accuracy=(sum_predict_cnt-self.predict_cnt[role]-self.role_cnt[role]+2*self.correct_predict_cnt[role])/sum_predict_cnt
                recall=self.correct_predict_cnt[role]/self.role_cnt[role]
                precision=self.correct_predict_cnt[role]/self.predict_cnt[role]
                f_1 = 2*recall*precision/(recall+precision)   

                print("{:<10}, recall:{:<.2f}, precision:{:<.2f}, f-1:{:<.2f}\n".format(role,recall, precision,f_1))
                # print("TP:{}  TN:{}  FP:{}  FN{}".format(self.correct_predict_cnt[role],self.predict_cnt[role]-self.correct_predict_cnt[role],self.role_cnt[role]-self.correct_predict_cnt[role], (sum_predict_cnt-self.predict_cnt[role]-self.role_cnt[role]+self.correct_predict_cnt[role]),))
                sum_correct_pred += self.correct_predict_cnt[role]
                sum_role_pred += self.role_cnt[role]

        for i in range(len(self.predict_net)):
            if 0 < len(self.predict_net[i].memory.accuracy_memory):
                # print(len(self.player_net[i].memory.accuracy_memory))
                print("day{:<2}  accuracy:{:<.2f}".format(i,np.mean(self.predict_net[i].memory.accuracy_memory)))
        
        # print(sum_game_cnt)
        print("correct utiwake rate is {:.2f}".format(self.utiwake_cnt/sum_game_cnt))
        print("correct alive werewolf rate is {:.2f}".format(self.alive_werewolf/sum_game_cnt))
        print("correct my role rate is {:.2f}".format(self.correct_myrole/sum_game_cnt))
        # print(self.trust_my_skill,self.not_trust_my_skill)
        print("trust my skill rate is {:.2f}".format(self.trust_my_skill/(self.trust_my_skill+self.not_trust_my_skill)))

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
        if self.train_mode == True or (self.train_mode == False and self.test_train_mode == True):
            if self.each_model == True:
                self.addVectorToEachModel()
            else:
                self.addVectorToOneModel()


        if self.train_mode == True or (self.train_mode == False and self.test_train_mode == True):
            self.predict_model_train()
            self.predict_model_eval()
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
            

    def __init__(self,n_input,n_hidden,n_output,agent_num,role_num,t_role_cnt):
        self.net = MLP(n_input=n_input,n_hidden=n_hidden,n_output=n_output)
        self.optimizer = chainer.optimizers.Adam()
        self.optimizer.setup(self.net)
        self.memory = Memory()
        self.agent_num = agent_num
        self.role_num = role_num
        self.t_role_cnt= t_role_cnt
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
            #お手製シグモイドクロスエントロピー
            # loss = F.sum(F.mean(F.mean(-F.log(F.sigmoid(y))*t-F.log(1-F.sigmoid(y))*(1-t),axis=0),axis=0))
            loss = F.sigmoid_cross_entropy(y,t)
            # loss = 0

            #お手製softmaxcrossentropy
            # loss = F.softmax_cross_entropy(F.reshape(y,(-1,self.agent_num,self.role_num),np.where(t.reshape(-1,self.agent_num,self.role_num)==1,axis=2)))
            # loss = F.mean(F.sum(F.sum(-F.log(F.softmax(F.reshape(y,(-1,self.agent_num,self.role_num)),axis=2)).__mul__(t.reshape(-1,self.agent_num,self.role_num)),axis=1),axis=1))
            y_select = np.argsort(np.argsort(-y.array.reshape(-1,self.agent_num,self.role_num),axis=2),axis=2)

            role_select = np.argsort(np.argsort(-y.array.reshape(-1,self.agent_num,self.role_num),axis=1),axis=1)
            y_role_cnt = np.sum(np.array(y_select < 1).astype(np.int32),axis=1)
            # print(y[0])
            # print(y_select[0])
            # print(y_role_cnt[0])
            # print(role_select[0])
            # print(self.t_role_cnt)

            ##多めに推定した役職の値分追加し，少なめに推定した役職の1-xを追加

            for k in range(y_select.shape[0]):
                for j in range(y_select.shape[2]):
                    for i in range(y_select.shape[1]):
                        # if y_role_cnt[k][j] <= role_select[k][i][j] and role_select[k][i][j] < self.t_role_cnt[j]:
                        #     loss += (-F.sigmoid(y)[k][i*self.role_num+j]+1)
                            # print(k,i,j,"少ない",y[k][i*self.role_num+j],(-F.sigmoid(y)[k][i*self.role_num+j]+1))
                        if self.t_role_cnt[j] <= role_select[k][i][j] and role_select[k][i][j] < y_role_cnt[k][j]:
                            loss += 0.5*F.sigmoid(y)[k][i*self.role_num+j]
                            # print(k,i,j,"多い",y[k][i*self.role_num+j],F.sigmoid(y)[k][i*self.role_num+j])

            ##各役職のらしさ総和が内訳のカウントとどれほど違うか
            # print(F.sum((F.mean(F.sum(F.sigmoid(F.reshape(y,(-1,self.agent_num,self.role_num))).__mul__(np.array(np.argsort(np.argsort(-y.array.reshape(-1,self.agent_num,self.role_num),axis=2),axis=2) < 1).astype(np.int32)),axis=1),axis=0) - self.t_role_cnt).__abs__()))
            loss += 0.5*(F.sum((F.mean(F.sum(F.sigmoid(F.reshape(y,(-1,self.agent_num,self.role_num))).__mul__(np.array(np.argsort(np.argsort(-y.array.reshape(-1,self.agent_num,self.role_num),axis=2),axis=2) < 1).astype(np.int32)),axis=1),axis=0) - self.t_role_cnt))**2)

            ##各役職の上位を持ってきて内訳の総和との差をとる
            # for i in range(y_select.shape[0]):
            #     for k in range(y_select.shape[2]):
            #         for j in range(y_select.shape[1]):
            #             if role_select[i][j][k] < self.t_role_cnt[k]:
            #                 loss += abs((F.sigmoid(y)[i][j*self.role_num+k]-1))

            # print(loss)


            y = y.array.reshape((-1,self.agent_num,self.role_num))
            t = t.reshape((-1,self.agent_num,self.role_num))
            

            y = np.array(np.argsort(np.argsort(-y,axis=2),axis=2) < 1)
            accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)
            # print(np.count_nonzero(np.logical_and(y,t)[0]),np.count_nonzero(t[0]),accuracy)
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
            # print(-F.log(F.softmax(y,axis=1)))
            # print(t)
            # print(F.sum(-F.log(F.softmax(y,axis=1))*t))
            y = np.array(np.argsort(np.argsort(-y,axis=1),axis=1) < 1)
            accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)
            # y = self.decode(y)
            # print(y)
            y = y.reshape(-1)
        return loss.array, accuracy, y


    def addVector(self, x, t):
        for i in range(len(x)):
            self.memory.append(x[i],t)

