import aiwolfpy
import aiwolfpy.contentbuilder as cb

import numpy as np
import re
import os
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

from torch.utils.tensorboard import SummaryWriter
writer = SummaryWriter()


from predict_model import PredictRole
from agent import Agent

import sys
from pathlib import Path
sys.path.append(str(Path(__file__).parent.parent))








class Environment():
    def __init__(self,agent_num=5,train_predict_mode=False,train_dqn_mode=False,train_divine_mode=False,train_times=1000,predict_net_load=False,dqn_net_load=False,divine_net_load=False,test_train_mode=False,each_model=False,epsilon=0.3,kanning=False):
        # super(self).__init__()
        self.Time = time.time()
        self.agent_num = agent_num
        self.train_mode = True
        self.train_predict_mode = train_predict_mode
        self.train_dqn_mode = train_dqn_mode
        self.train_divine_mode = train_divine_mode
        self.train_times = train_times
        self.each_model = each_model
        self.test_train_mode = test_train_mode
        self.epsilon = epsilon
        self.predict_net_load = predict_net_load
        self.dqn_net_load = dqn_net_load
        self.divine_net_load = divine_net_load
        self.train_cnt = 1
        self.kanning = kanning

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
        self.human_list = ["HUMAN","VILLAGER","SEER","BODYGUARD","MEDIUM","POSSESSED"]
        self.werewolf_list = ["WEREWOLF"]
        self.side_to_num = {"HUMAN":0,"VILLAGER":0,"SEER":0,"BODYGUARD":0,"MEDIUM":0,"POSSESSED":0,"WEREWOLF":1}
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
        # self.vote_another_people = np.zeros(self.agent_num,dtype=np.int32)
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

        ####################################
        ## 強化学習用特徴量
        #################################

        self.action_to_num = {"vote":0,"divine":1,"guard":2,"attack":3}
        self.action_type = [0 for i in range(len(self.action_to_num))]
        self.state = []
        self.next_state = []
        self.reward = [0]
        self.vote_action = None
        self.pred_result = np.zeros((1,self.agent_num*self.role_num)).astype(np.float32)

        self.divine_action = None

        self.createDailyVector()
        self.createSubFeat()
        
        # self.player_vector_length = self.daily_vector.shape[1] + 1
        self.daily_vector_length = self.daily_vector.shape[0]*self.daily_vector.shape[1] + self.sub_feat.shape[0]
        print(self.daily_vector_length)
       



        #####集計用##########
        self.role_cnt = defaultdict(int)
        self.predict_cnt = defaultdict(int)
        self.correct_predict_cnt = defaultdict(int)
        self.using_alive_info_cnt_daily  = 0
        self.alive_werewolf = 0
        self.utiwake_cnt = 0
        self.correct_myrole = 0
        self.trust_my_skill = 0
        self.not_trust_my_skill = 0
        self.white_divine = 0
        self.black_divine = 0

        self.t_role_cnt= np.array([self.utiwake[self.num_to_role[i]] for i in range(self.role_num)]).astype(np.float32)

        if self.agent_num == 6:
            pred_n_hidden = 500
            dqn_n_hidden = 500
            # divine_n_hidden = 500
        elif self.agent_num == 10:
            pred_n_hidden = 1200
            dqn_n_hidden = 1200
            # divine_n_hidden = 1200           

        self.player = Agent(pred_n_input=self.daily_vector_length, pred_n_hidden=pred_n_hidden, pred_n_output=self.agent_num*self.role_num,dqn_n_input=self.daily_vector_length+self.agent_num*self.role_num,dqn_n_hidden=dqn_n_hidden,dqn_n_output=self.agent_num, agent_num=self.agent_num,role_num=self.role_num,t_role_cnt = self.t_role_cnt,train_predict_mode=self.train_predict_mode,train_dqn_mode=self.train_dqn_mode,train_divine_mode=self.train_divine_mode)


        if self.predict_net_load == True or self.train_predict_mode == False:
            if self.each_model == True:
                for i in range(len(self.predict_net)):
                    serializers.load_npz('./predict_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/modify_predict_role_train_daily_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.predict_net[i].net)
            else:
                self.player.pred_model.model = torch.load('./predict_model/agent'+str(self.agent_num)+'/one_model/modify_predict_role_train_daily_num_'+str(self.agent_num)+'_train_10000.net')
                print("predict model is loaded.")
        if self.dqn_net_load == True or self.train_dqn_mode == False:
            path = "./dqn_model/agent"+str(self.agent_num)+'/'
            file_name = 'dqn_num_'+str(self.agent_num)+'_train_'+str(10000)+'.net'
            print("dqn model is loaded.")
            self.player.brain.main_q_model = torch.load(path+file_name)
        if self.divine_net_load == True or self.train_dqn_mode == False:
            path = "./divine_model/agent"+str(self.agent_num)+'/'
            file_name = 'divine_num_'+str(self.agent_num)+'_train_'+str(10000)+'.net'
            self.player.divine_model.main_q_model = torch.load(path+file_name)
            print("divine model is loaded.")



        self.graph_name = 'dqn_'
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
        # self.vote_another_people.fill(0)

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


        self.predict_x = [[] for i in range(self.max_day+1)]
        self.predict_t = [0 for i in range(self.agent_num*self.role_num)]
        # self.player_x = [[]for i in range(self.agent_num)]
        # self.predict_t = [0 for i in range(self.agent_num)]

        self.state=None
        self.next_state=None
        self.reward = [0]
        self.divine_state = None
        self.divine_next_state = None
        self.divine_action = None
        self.vote_action = None
        self.pred_result = np.zeros((1,self.agent_num*self.role_num)).astype(np.float32)
        self.divine_action = None
        # self.divine_list = [False for i in range(self.agent_num)]


    def createXPredictData(self):
        # common_feats = np.hstack((self.base_info["day"],self.my_agent_id,self.my_role,))
        # alpha_common_feats = self.daily_vector[self.base_info["agentIdx"]-1,:]
        self.createSubFeat()
        self.createDailyVector()
        # print(np.hstack((self.daily_vector.reshape(-1),common_feats, alpha_common_feats)).astype(np.float32).shape)
        return np.hstack((self.daily_vector.reshape(-1),self.sub_feat.reshape(-1))).astype(np.float32).reshape(1,-1)

        

    def randomSelect(self,votable_mask):
        if len(np.where(votable_mask==True)[0]):
            print("infinity roop")
            return self.base_info["agentIdx"]
        while(True):
            target = np.random.randint(0,len(votable_mask))
            if votable_mask[target]==True:
                return target


    def selectAgent(self,fase):

        state = self.createXPredictData()
        votable_mask = [self.alive_list[i][self.alive_to_num["alive"]]==1 and i != self.base_info["agentIdx"]-1 for i in range(self.agent_num)]

        if state is None:
            return self.randomSelect(votable_mask)

        if fase == "vote":
            # return self.randomSelect(votable_mask=votable_mask)
            target = self.player.selectAgent(state=state,votable_mask=votable_mask,agent_num=self.agent_num,role_num=self.role_num,num_to_role=self.num_to_role)
            return target
        else:

            if self.base_info["myRole"] in ["VILLAGER","SEER","MEDIUM","BODYGUARD"]:
                target_list = ["WEREWOLF","POSSESSED"]
            else:
                target_list = ["SEER","BODYGUARD","MEDIUM","VILLAGER"]
            for target_role in target_list:
                est_role_list = self.player.get_predict_output(torch.tensor(state).float()).reshape(self.agent_num,self.role_num)
                est_role_list = [(est_role_list[agent,role],agent) for agent,role in enumerate(np.argmax(est_role_list,axis=1)) if self.num_to_role[role] == target_role]
                est_role_list = sorted(est_role_list,reverse=True)

                for _,target in est_role_list:
                    if votable_mask[target]==True:
                        return target
            
        return self.randomSelect(votable_mask)


    def talk(self):
        if(self.base_info['myRole']=="VILLAGER"):
            # if(self.done_last_commingout == False):
            #     self.done_last_commingout = True
            #     return cb.comingout((self.base_info['agentIdx']),"VILLAGER")
            if(self.have_ever_vote == False):
                self.have_ever_vote = True
                # return cb.vote(np.argmax(np.sum(self.declaration_vote_list)) + 1)
                return cb.vote(self.selectAgent("talk")+1)
            else:
                return cb.over()
        elif (self.base_info['myRole'] == 'SEER'):
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),"SEER")
            if(self.not_reported == True):
                self.not_reported = False
                return self.myresult
            if self.have_ever_vote == False:
                self.have_ever_vote = True
                return cb.vote(self.selectAgent("talk")+1)
            return cb.over()
        elif self.base_info['myRole'] == 'POSSESSED':
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),self.fake_role)
            if(self.not_reported == True):
                self.do_fake_report = True
                self.not_reported = False
                return self.myresult
            return cb.over()
        elif self.base_info['myRole'] == 'WEREWOLF':
            if(self.have_ever_vote == False):
                self.have_ever_vote = True
                # return cb.vote(np.argmax(np.sum(self.declaration_vote_list)) + 1)
                return cb.vote(self.selectAgent("talk")+1)
            else:
                return cb.over()
        elif self.base_info['myRole'] == 'MEDIUM':
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),"MEDIUM")
            if(self.not_reported == True):
                self.not_reported = False
                return self.myresult
            if self.have_ever_vote == False:
                self.have_ever_vote = True
                return cb.vote(self.selectAgent("talk")+1)                

        return cb.over()


    def whisper(self):
        # print("whisper")v
        return cb.over()

    def createDqnState(self,state,pred_result):
        # action_type = np.array([action_type])
        # print(np.hstack((state,pred_result)).astype(np.float32).shape)
        return np.hstack((state,pred_result)).astype(np.float32)

    def vote(self):
        # print("vote")
        # next_action = self.selectAgent("vote")
        # self.next_state = self.createDqnState(state=self.createXPredictData(),action_type=self.action_type,pred_result=self.pred_result)
        # if self.train_dqn_mode==True and self.state is not None:
        #     self.player.memorize_state(state=self.state,action=np.array(self.action).reshape(1,1),next_state=self.next_state,reward=self.reward)

        # self.state = self.createDqnState(state=self.createXPredictData(),pred_result=self.pred_result)

        self.vote_action = self.selectAgent("vote")
        # self.action_type = self.encode(self.action_to_num["vote"],len(self.action_to_num))
        return self.vote_action + 1
        # if self.base_info["myRole"] in self.werewolf_list:
        #     for target_role in ["SEER","VILLAGER","POSSESSED"]:
        #         target = self.selectAgent()
        #         if target != -1:
        #             return target
        #     return self.randomSelect(self.base_info,self.alive_list,self.alive_to_num)
        # elif self.base_info["myRole"] in self.human_list:

            # for target_role in ["WEREWOLF", "POSSESSED"]:
            #     target = self.selectAgent()
            #     if target != -1:
            #         return target
            # return self.randomSelect(self.base_info,self.alive_list,self.alive_to_num)

    def attack(self):
        return self.selectAgent("attack") + 1
        # print("attack")
        # for target_role in ["SEER","VILLAGER","POSSESSED"]:
        #     target = self.selectAgent()
        #     if target != -1:
        #         return target
        # return self.randomSelect(self.base_info,self.alive_list,self.alive_to_num)

    def divine(self):
        return self.selectAgent("divine") + 1
        # print("devine")
        # for target_role in ["WEREWOLF", "POSSESSED"]:
        #     target = self.selectAgent()
        #     if target != -1:
        #         return target
        # return self.randomSelect(self.base_info,self.alive_list,self.alive_to_num)

    def guard(self):
        # print("guard")
        return self.selectAgent("guard") + 1
        # for target_role in ["SEER","VILLAGER"]:
        #     target = self.selectAgent()
        #     if target != -1:
        #         return target
        # return self.randomSelect(self.base_info,self.alive_list,self.alive_to_num)


    def updateTalk(self,index):
        #会話の日にちとIDを記憶
        # talk_day_id[self.].append(agent)
        agent = self.diff_data["agent"][index]-1
        talk_texts = self.diff_data["text"][index]
        if talk_texts.split(' ')[0]=="AND":
            bracket = 0
            front = 0
            for i in range(len(talk_texts)):  
                if talk_texts[i]=='(':
                    bracket += 1
                    if front == 0:
                        front = i+1
                elif talk_texts[i] == ')':
                    bracket -= 1
                    if(bracket==0):
                        self.understand_text(agent,talk_texts[front:i])
                        front = 0
        elif talk_texts.split(' ')[0]=="BECAUSE" or talk_texts.split(' ')[0]=="REQUEST":
            None
        else:
            self.understand_text(agent,talk_texts)
  
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
        self.predict_x[np.where(self.day == 1)[0][0]+1].append(self.createXPredictData().tolist())


    def countEachRole(self,index):
        agent = self.diff_data["agent"][index]-1
        role = self.diff_data["text"][index].split(' ')[2]
        self.predict_t[self.role_num*agent+self.role_to_num[role]] = 1


    def decode(self,line):
        tmp = []
        for l in line:
            tmp.append(np.where(l==True)[0][0])
        return tmp

    def encode(self,data,dim):
        out = [0 for i in range(dim)]
        out[data] = 1
        return out

    def update_predict_result(self,y,t):
        y_list = [self.decode(y) for y in y]
        t = self.decode(np.array(t).reshape(self.agent_num,self.role_num))

        for y in y_list:
            for y_role, t_role in zip(y,t):
                self.predict_cnt[self.num_to_role[y_role]] += 1
                self.role_cnt[self.num_to_role[t_role]] += 1
                if y_role == t_role:
                    self.correct_predict_cnt[self.num_to_role[t_role]]+=1
                # print(self.num_to_role[y_role], self.num_to_role[t_role])
            # print(self.correct_predict_cnt)
            # print()
            if y[self.base_info["agentIdx"]-1] == t[self.base_info["agentIdx"]-1]:
                self.correct_myrole+=1

            if collections.Counter([self.num_to_role[role] for role in y]) == self.utiwake:
                self.utiwake_cnt+= 1


            tmp = 0
            exist_werewolf = False
            for agent,y_role in enumerate(y):
                agent = agent%self.agent_num
                if self.num_to_role[y_role] == "WEREWOLF":
                    exist_werewolf = True
                    if self.alive_list[agent][self.alive_to_num.get("alive")] == 1:
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

            if tmp != 0 or exist_werewolf==False:
                self.alive_werewolf += 1




    def predict_model_train(self):
        self.player.update_pred_model()

    def predict_model_eval(self):
        predict_y = self.player.eval_pred_model(state=self.predict_x, label=self.predict_t)
        self.update_predict_result(predict_y,self.predict_t)
        # for i in range(len(self.predict_x)):
        #     if 0 < len(self.predict_x[i]):
        #         for x in self.predict_x[i]:
        #             predict_y = self.player.eval(state=np.array(x).reshape(1,-1).astype(np.float32), label=np.array(self.predict_t).reshape(1,-1).astype(np.int32),agent_num=self.agent_num,role_num=self.role_num)
        #             self.update_predict_result(predict_y, np.array(self.predict_t).astype(np.int32))
        #             if self.train_mode==False:
        #                 self.predict_net[i].memory.addLossAccuracy(loss,accuracy)

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
        file_name = 'modify_predict_role_train_daily_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        torch.save(self.player.pred_model.model, daily_path+file_name)
        print("pred model is saved")

    def save_dqn_model(self):
        path = "./net_folder/dqn_model/agent"+str(self.agent_num)+'/'
        os.makedirs(path,exist_ok=True)
        file_name = 'dqn_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        torch.save(self.player.brain.main_q_model,path+file_name)
        print("dqn model is saved")

    def save_divine_model(self):
        path = "./net_folder/divine_model/agent"+str(self.agent_num)+'/'
        os.makedirs(path,exist_ok=True)
        file_name = 'divine_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        torch.save(self.player.divine_model.main_q_model,path+file_name)
        print("divine model is saved")

    def addVectorToEachModel(self):
        
        for i in range(1,len(self.predict_x)):
            if len(self.predict_x[i]) != 0:
                for j in range(len(self.predict_x[i])):
                    self.predict_net[i].addVector([self.predict_x[i][j]],self.predict_t)
        # if self.predict_train == True:
        #     for i in range(len(self.player_x)):
        #         for j in range(len(self.player_x[i])):
        #             self.player_net[i].addVector(self.player_x[i][j],[self.predict_t[j]])

    def addVectorToOneModel(self):
        for i in range(1,len(self.predict_x)):
            if len(self.predict_x[i]) != 0:
                for j in range(len(self.predict_x[i])):
                    self.player.memorize_pred_label([self.predict_x[i][j]],[self.predict_t])
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
            # self.next_state = self.createDqnState(state=self.createXPredictData(),pred_result=self.pred_result)
            # if self.train_dqn_mode==True and self.state is not None:
            #     self.player.memorize_state(state=self.state,action=np.array(self.action).reshape(1,1),next_state=self.next_state,reward=self.reward)

            self.updateVote_declare()


        elif request == "DAILY_FINISH" and 1 <= base_info["day"]:
            #0:自分が人間判定された数 1:自分が人狼判定された数 2:占い師の名乗り出た順番 3:報告した人間の数 4:報告した人狼の数 5:発言と投票先が変わった数．6:生死(#alive:0 attacked:1 execute:-1)　7~11:肯定的意見の数　12~16:否定的意見の数        
            self.updateVector()

            if self.base_info['myRole'] == 'POSSESSED':
                if self.train_divine_mode==True and self.state is not None:
                    self.player.memorize_divine_state(state=self.state,action=np.array(self.divine_action).reshape(1,1),next_state=self.next_state,reward=self.reward)
                self.do_fake_report = False

            # self.next_state = self.createDqnState(state=self.createXPredictData(),pred_result=self.pred_result)
            self.next_state = self.createXPredictData()
            if self.train_dqn_mode==True and self.state is not None:
                self.player.memorize_state(state=self.state,action=np.array(self.vote_action).reshape(1,1),next_state=self.next_state,reward=self.reward)

            self.state = self.next_state

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

        for i in range(self.max_day):
            _,accuracy = self.player.pred_model.memory.getLossAccuracy(i)
            if accuracy is not None:
                accuracy = np.mean(accuracy)
                print("day{:<2}  accuracy:{:<.2f}".format(i,accuracy))
        
        # print(sum_game_cnt)
        print("correct utiwake rate is {:.2f}".format(self.utiwake_cnt/sum_game_cnt))
        print("correct alive werewolf rate is {:.2f}".format(self.alive_werewolf/sum_game_cnt))
        print("correct my role rate is {:.2f}".format(self.correct_myrole/sum_game_cnt))
        # print(self.trust_my_skill,self.not_trust_my_skill)
        if 0 < (self.trust_my_skill+self.not_trust_my_skill):
            print("trust my skill rate is {:.2f}".format(self.trust_my_skill/(self.trust_my_skill+self.not_trust_my_skill)))
        print("win_ratio is {:.2f}".format(np.mean(self.player.brain.memory.win_memory)))
        self.displayFakeDivineCount()

    def plot_accu_loss(self):
        fig = plt.figure(figsize=(12.0,8.0))
        graph_name = "agent_"+str(self.agent_num)+"_"
        graph_name += "train_" if self.train_mode == True else "test_"
        graph_name += str(self.train_times)+"_"
        graph_name += "each_model" if self.each_model == True else "one_model"
        fig.suptitle('dqn_ '+graph_name, fontsize=20)
        daily_loss = fig.add_subplot(2,2,1)
        plt.title("daily_loss")
        daily_accu = fig.add_subplot(2,2,2)
        plt.title("daily_accuracy")
        win_ratio = fig.add_subplot(2,2,3)
        plt.title("win_ratio")
        for i in range(self.max_day):
            loss,accuracy = self.player.pred_model.memory.getLossAccuracy(i)
            if loss:
                daily_loss.plot(loss,label="day"+str(i))
                # plt.legend()

            if accuracy:
                daily_accu.plot(accuracy,label="day"+str(i))
                # plt.legend()
        win_ratio.plot(self.player.brain.memory.win_memory,label="win_ratio")
        max_Q_mean = fig.add_subplot(2,2,4)
        plt.title("max_Q_mean")
        max_Q_mean.plot(self.player.brain.memory.max_Q_memory,label="max_Q_mean")
        plt.legend()
        # plt.show()
        os.makedirs("graph_folder", exist_ok=True)
        plt.savefig("graph_folder/"+graph_name+'.png')


    def finish(self):
        if self.train_predict_mode == True:
            if self.each_model == True:
                self.addVectorToEachModel()
            else:
                self.addVectorToOneModel()


        # if self.train_mode == True or (self.train_mode == False and self.test_train_mode == True):
        if self.train_predict_mode == True or self.test_train_mode == True:
            self.predict_model_train()
            
            # self.predict_model_eval()
        else:
            self.predict_model_eval()


        # with open("../AIWolf-ver0.5.6/winner.txt",'r') as f:
        #     winner = f.readlines()
        #     if winner[self.base_info["agentIdx"]-1] == winner[-1]:
        #         self.reward = [1]
        #         win = True
        #     else:
        #         self.reward = [0]
        #         win = False
        # print(self.base_info["statusMap"])
        alive_werewolf_num = 0
        for agent,role in enumerate(np.array(self.predict_t).reshape(self.agent_num,self.role_num)):
            agent += 1
            if np.where(role==1)[0][0] == self.role_to_num["WEREWOLF"] and self.base_info["statusMap"].get(str(agent)) == "ALIVE":
                alive_werewolf_num += 1
        # print(alive_werewolf_num)

        if (0 < alive_werewolf_num and self.base_info["myRole"] in ["WEREWOLF","POSSESSED"]) or (alive_werewolf_num == 0 and self.base_info["myRole"] not in ["WEREWOLF","POSSESSED"]):
            # print("winnwe")
            win = True
            self.reward = [1]
        else:
            win = False
            self.reward = [0]

        # alive_werewolf_num = np.sum(np.array(self.predict_t).reshape(self.agent_num,self.role_num)[:,self.role_to_num["WEREWOLF"]])
        # print(alive_werewolf_num)
        # if alive_num-alive_werewolf_num <= alive_werewolf_num:
        
        
        # print(type(self.state),type(self.action),type(self.next_state),type(self.reward))

        if self.train_dqn_mode == True:
            # print(self.vote_action)
            self.player.memorize_state(state=self.state,action=np.array(self.vote_action).reshape(1,1),next_state=None,reward=self.reward)
            self.player.update_q_function()
        self.player.updateWinRatio(win)
        # writer.add_scalar('data/win_ratio',self.player.brain.memory.win_memory[-1],len(self.player.brain.memory.win_memory))

        if self.base_info["myRole"] == "POSSESSED":
            if self.train_divine_mode == True:
                # print(self.divine_action)
                self.player.memorize_divine_state(state=self.state,action=np.array(self.divine_action).reshape(1,1),next_state=None,reward=self.reward)
                self.player.update_divine_model()

        if (self.train_cnt%3) == 0:
            self.player.update_target_q_function()

        if self.train_cnt%(self.train_times//10) == 0:
            sec = round(time.time()-self.Time)
            print("train:{:<10}time is {:<2}hour {:<2}minutes {:<2}sec".format(self.train_cnt,sec//3600,(sec%3600)//60,(sec%60)))
            # if self.train_mode == True:
            #     if self.each_model == True:
            #         self.save_each_model()
            #     else:
            #         self.save_one_model()
            #         self.save_dqn_model()
            #         self.save_divine_model()
            if self.train_predict_mode == True:
                self.save_one_model()
            if self.train_dqn_mode == True:
                self.save_dqn_model()
            if self.train_divine_mode == True:
                self.save_divine_model()

        if(self.train_cnt == self.train_times):
            if self.train_predict_mode == False:
                self.display_game_result()
            # else:
            #     self.save_one_model()
            #     self.save_each_model()
            self.plot_accu_loss()
            writer.close()

        self.train_cnt += 1

    def updateVoteList(self,index):
        self.vote_list[self.diff_data["idx"][index]-1][self.diff_data["agent"][index]-1] = 1
        # print(self.vote_list)

    def updateAliveList(self,index):
        # print(self.alive_to_num[self.diff_data["type"][index]])
        agent = self.diff_data["agent"][index] - 1
        self.alive_list[agent].fill(0)
        self.alive_list[agent][self.alive_to_num[self.diff_data["type"][index]]] = 1

    def updateVote_declare(self):
        # super(self).updateVote_declare()
        self.daily_vector.fill(0)
        #7:発言と投票先が変わった数をカウント．
        # print(self.declaration_vote_list)
        # print(self.vote_list)
        # for i in range(self.agent_num):
        #     if 1 not in self.declaration_vote_list[i]:
        #         continue
        #     for j in range(self.agent_num):
        #         if self.vote_list[i][j]==1 and self.declaration_vote_list[i][j]==0:
        #             self.vote_another_people[i] += 1
        self.last_declaration_vote_list[...] += self.declaration_vote_list
        self.last_vote_list[...] += self.vote_list
        self.declaration_vote_list.fill(0)
        self.vote_list.fill(0)

    def getResult(self,index):
        #昨夜の能力行使の結果を取得

        # IDENTIFY
        if self.diff_data['type'][index] == 'identify':
            self.not_reported = True
            self.myresult = self.diff_data['text'][index]
            target = re.search(r"[0-9][0-9]",self.myresult).group()
            target = int(target) - 1    
            role = self.myresult.split(' ')[-1]        
            self.other_role[target,self.side_to_num[role]] = 1
            
        # DIVINE
        if self.diff_data['type'][index] == 'divine':
            self.not_reported = True
            self.myresult = self.diff_data['text'][index]
            target = re.search(r"[0-9][0-9]",self.myresult).group()
            target = int(target) - 1    
            role = self.myresult.split(' ')[-1]        
            self.other_role[target,self.side_to_num[role]] = 1

            
        # GUARD
        if self.diff_data['type'][index] == 'guard':
            self.myresult = self.diff_data['text'][index]

    def createMask(self):
        return [self.alive_list[i][self.alive_to_num["alive"]]==1 and i != self.base_info["agentIdx"]-1 for i in range(self.agent_num)]

    def getFakeResult(self):
        # FAKE DIVINE
        # if self.fake_role == 'SEER':
        #     self.not_reported = True
        #     idx = self.selectAgent("divine")+1
        #     if idx == -1:
        #         idx = self.randomSelect(self.base_info,self.alive_list,self.alive_to_num)
        #     self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'

        if self.fake_role == 'SEER':
            self.not_reported = True
            # self.divine_state = self.createXPredictData()
            mask = self.createMask()
            self.divine_action = self.player.selectDivineAgent(self.state,mask)
            # print(self.other_role)
            # print("result is ",self.divine_action)
            target = self.divine_action//2
            role = "WEREWOLF" if self.divine_action%2 != 1 else "HUMAN"
            if role == "WEREWOLF":
                self.black_divine += 1
            else:
                self.white_divine += 1
            # print("result is ",target,role)

            self.other_role[target][self.side_to_num[role]] = 1
            self.myresult = 'DIVINED Agent[' + "{0:02d}".format(target+1) + '] ' + role


    def displayFakeDivineCount(self):
        print("white rate is {:.3f}".format(self.white_divine/(self.white_divine+self.black_divine)))

