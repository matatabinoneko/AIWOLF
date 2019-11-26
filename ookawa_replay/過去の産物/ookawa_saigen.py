#!/usr/bin/env python
from __future__ import print_function, division 
import re
import numpy as np

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb

# import sys

import chainer
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import chainer.links as L
import chainer.functions as F
from chainer import serializers

import matplotlib.pyplot as plt
from collections import defaultdict

myname = 'matatabi'

class SampleAgent(object):
    
    def __init__(self, agent_name):
        ## myname ##
        self.myname = agent_name

        class MLP(chainer.Chain):
            def __init__(self,n_input,n_hidden,n_output):
                super(MLP, self).__init__()
                self.n_input = n_input
                self.n_hidden = n_hidden
                self.n_output = n_output
            
                with self.init_scope():
                    self.l1 = L.Linear(self.n_input, self.n_hidden)
                    self.l2 = L.Linear(self.n_hidden,self.n_hidden)
                    self.l3 = L.Linear(self.n_hidden,self.n_output)
                    
            def __call__(self, x):
                h1 = F.dropout(F.relu(self.l1(x)),ratio=0.4)
                h2 = F.dropout(F.relu(self.l2(h1)),ratio=0.4)
                h3 = self.l3(h2)
                return h3


        gpu_id = 0

        self.daily_train = False
        self.player_train = False
        self.train_times = 1000

        self.daily_net = MLP(n_input=87,n_hidden=200,n_output=5)
        self.player_net = MLP(n_input=19,n_hidden=50,n_output=4)


        serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_train_daily_5.net", self.daily_net)
        serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_train_player_5.net", self.player_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_daily.net", self.daily_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_player.net", self.player_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_train_only_daily.net", self.daily_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_train_only_player.net", self.player_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_another_daily.net", self.daily_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_another_player.net", self.player_net)

        # print(self.daily_net.l1.W.data)
        # print(self.player_net.l1.W.data)
        

        self.daily_optimizer = chainer.optimizers.Adam()
        self.player_optimizer = chainer.optimizers.Adam()
        self.daily_optimizer.setup(self.daily_net)
        self.player_optimizer.setup(self.player_net)

        self.train_cnt = 1
        
        
        self.daily_x_t = []
        self.player_x_t = []

        self.daily_loss_sum = []
        self.daily_accu_sum = []
        self.daily_loss_mean = []
        self.daily_accu_mean = []
        self.player_loss_sum = []
        self.player_accu_sum = []
        self.player_loss_mean = []
        self.player_accu_mean = []


        self.role_cnt = defaultdict(int)
        self.predict_cnt = defaultdict(int)
        self.correct_predict_cnt = defaultdict(int)

    def getName(self):
        return self.myname
    
    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        ## game_setting ##
        self.game_setting = game_setting
        # print("initialize: base_info=",base_info,sep='\n')
        # print("initialize: diff_data=",diff_data,sep='\n')

        ### edit from here ###
        self.num = self.game_setting["playerNum"]
        self.estimate_list = np.zeros((self.num,self.num,4),dtype=np.int32) #[0:vilagger,1:seer,2:possesed,3:werewolf]
        self.co_list = np.zeros((self.num,4),dtype=np.int32) #[0:vilagger,1:seer,2:possesed,3:werewolf]
        self.role_to_num = {"VILLAGER":0,"SEER":1,"POSSESSED":2,"WEREWOLF":3}
        self.num_to_role = {v:k for k,v in self.role_to_num.items()}
        self.hw_dic = {"HUMAN":0,"VILLAGER":0,"SEER":0,"POSSESSED":1,"WEREWOLF":1}
        self.seer_co_cnt = 1
        self.seer_co_oder = np.zeros(self.num,dtype=np.int32)
        self.seer_have_been_co = False
        self.have_ever_vote = False
        self.divined_list = np.zeros((self.num,self.num,2)) #0:HUMAN 1:WEREWOLF
        self.comingout = ''
        self.myresult = ''
        self.not_reported = False
        self.done_last_commingout = False
        self.vote_declare = False

        self.fakeRole = ''
        if self.base_info['myRole']=='POSSESSED':
            self.fakeRole = 'SEER'

        self.declaration_vote_list = np.zeros((self.num,self.num),dtype=np.int32)
        self.vote_list = np.zeros((self.num,self.num),dtype=np.int32)
        self.vote_another_people = np.zeros(self.num,dtype=np.int32)

        self.alive_list = np.zeros(self.num,dtype=np.int32)#alive:0 attacked:1 execute:-1

        self.ag_esti_list = np.zeros((5,5),dtype=np.int32)
        self.disag_esti_list = np.zeros((5,5),dtype=np.int32)
        # self.talk_day_id = [[]for i in range(5)]

        self.daily_x = []
        self.daily_t = []
        self.player_x = []
        self.player_t = []

        
    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        self.diff_data = diff_data
        self.request = request
        # print("\n\nrequest:",request,sep='\n')
        # print("update: base_info=",base_info,sep='\n')
        # print("update: diff_data=",diff_data,sep='\n')

        ### edit from here ###

        def understand_text(agent,talk_texts):
            '''talkのtext部分を解釈可能にparse'''
            talk_texts = talk_texts.split(' ')
            # print(talk_texts)
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
                if(role=="VILLAGER"):
                    self.co_list[agent][0] = 1
                elif(role=="SEER"):
                    self.co_list[agent][1] = 1
                    self.seer_co_oder[agent] = self.seer_co_cnt
                    self.seer_have_been_co = True
                elif(role=="POSSESED"):
                    self.co_list[agent][2] = 1
                elif(role=="WEREWOLF"):
                    self.co_list[agent][3] = 1
                
                if self.seer_have_been_co == True:
                    self.seer_have_been_co = False
                    self.seer_co_cnt += 1

            elif(talk_texts[0]=="ESTIMATE"):
                None
            elif(talk_texts[0]=="DIVINATION"):
                None
            elif(3<len(talk_texts) and talk_texts[2][1:]=="DIVINED"):
                # print(talk_texts[4][:-1])
                if 0<=target<self.num and 0<=agent<self.num:
                    target = re.search(r"[0-9][0-9]",talk_texts[3]).group()
                    target = int(target) - 1
                    if(talk_texts[4][:-1]=="HUMAN"):
                        role = 0
                    else:
                        role = 1
                    self.divined_list[agent][target][role] = 1
            elif(3<len(talk_texts) and talk_texts[2][1:]=="IDENTIFIED"):
                None
            elif(talk_texts[0]=="DIVINED"):
                target = re.search(r"[0-9][0-9]",talk_texts[1]).group()
                target = int(target) - 1
                if 0<=target<self.num and 0<=agent<self.num:
                    if(talk_texts[2]=="HUMAN"):
                        role = 0
                    else:
                        role = 1
                    self.divined_list[agent][target][role] = 1
            elif(talk_texts[0]=="IDENTIFIED"):
                None
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

        # result
        for i in range(diff_data.shape[0]):
            if diff_data["type"][i] == "dead":
                agent = diff_data["agent"][len(diff_data)-1]-1
                self.alive_list[agent] = -1
            elif diff_data["type"][i] == "execute":
                agent = diff_data["agent"][len(diff_data)-1]-1
                self.alive_list[agent] = 1

        if 0<len(diff_data) and diff_data["type"][0] == "vote":
            for i in range(len(diff_data)-1): 
                # print(diff_data["idx"][i],diff_data["agent"][i])
                self.vote_list[diff_data["idx"][i]-1][diff_data["agent"][i]-1] = 1
            # print(self.vote_list)

        if(0 < diff_data.shape[0]):
            if(diff_data["type"][0]=="talk"):

                #会話の日にちとIDを記憶
                # talk_day_id[self.].append(agent)

                for i in diff_data.index.values:
                    agent = diff_data["agent"][i]-1
                    talk_texts = diff_data["text"][i]
                    if talk_texts.split(' ')[0]=="AND":
                        bracket = 0
                        front = 0
                        for j in range(len(talk_texts)):  
                            if talk_texts[j]=='(':
                                bracket += 1
                                if front == 0:
                                    front = j+1
                            elif talk_texts[j] == ')':
                                bracket -= 1
                                if(bracket==0):
                                    understand_text(agent,talk_texts[front:j])
                                    front = 0
                    elif talk_texts.split(' ')[0]=="BECAUSE" or talk_texts.split(' ')[0]=="REQUEST":
                        None
                    else:
                        understand_text(agent,talk_texts)

            elif diff_data["type"][0]=="finish":
                for i in range(len(diff_data)):
                    agent = diff_data["agent"][i]-1
                    role = diff_data["text"][i].split(' ')[2]
                    self.player_t.append(self.role_to_num[role])
                    # self.num_to_role = {0:}
                    if(role == "WEREWOLF"):
                        self.daily_t.append(agent)
                # print(diff_data)
                # print(self.player_t)
    
            # elif(diff_data["type"][0]=="talk")


        if request == 'DAILY_INITIALIZE':
            self.daily_vector = np.zeros((self.num,17),dtype=np.int32)
            #7:発言と投票先が変わった数をカウント．
            # print(self.declaration_vote_list)
            # print(self.vote_list)
            for i in range(self.num):
                if 1 not in self.declaration_vote_list[i]:
                    continue
                for j in range(self.num):
                    if self.vote_list[i][j]==1 and self.declaration_vote_list[i][j]==0:
                        self.vote_another_people[i] += 1

            self.declaration_vote_list = np.zeros((self.num,self.num),dtype=np.int32)
            self.vote_list = np.zeros((self.num,self.num),dtype=np.int32)


            #昨夜の能力行使の結果を取得
            for i in range(diff_data.shape[0]):
                # IDENTIFY
                if diff_data['type'][i] == 'identify':
                    self.not_reported = True
                    self.myresult = diff_data['text'][i]
                    
                # DIVINE
                if diff_data['type'][i] == 'divine':
                    self.not_reported = True
                    self.myresult = diff_data['text'][i]
                    
                # GUARD
                if diff_data['type'][i] == 'guard':
                    self.myresult = diff_data['text'][i]

                # POSSESEED
                # FAKE DIVINE
                if self.fakeRole == 'SEER':
                    self.not_reported = True
                    idx = self.selectAgent()+1
                    self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'
        elif request == "DAILY_FINISH" and base_info["day"] != 0:
            #0:自分が人間判定された数 1:自分が人狼判定された数 2:占い師の名乗り出た順番 3:報告した人間の数 4:報告した人狼の数 5:発言と投票先が変わった数．6:生死(#alive:0 attacked:1 execute:-1)　7~11:肯定的意見の数　12~16:否定的意見の数
            
            for agent in range(self.num):
                if(not diff_data.empty):    
                    # self.daily_vector[agent][0] = base_info["day"]
                    # self.daily_vector[agent][1] = np.sum(self.co_list,axis=0)[1]
                    self.daily_vector[agent][0] = np.sum(self.divined_list,axis=0)[agent][0]
                    self.daily_vector[agent][1] = np.sum(self.divined_list,axis=0)[agent][1]
                    self.daily_vector[agent][2] = self.seer_co_oder[agent]
                    self.daily_vector[agent][3] = np.sum(self.divined_list,axis=1)[agent][0]
                    self.daily_vector[agent][4] = np.sum(self.divined_list,axis=1)[agent][1]
                    self.daily_vector[agent][5] = self.vote_another_people[agent]
                    self.daily_vector[agent][6] = self.alive_list[agent]
                    self.daily_vector[agent][7] = self.ag_esti_list[agent][0]
                    self.daily_vector[agent][8] = self.ag_esti_list[agent][1]
                    self.daily_vector[agent][9] = self.ag_esti_list[agent][2]
                    self.daily_vector[agent][10] = self.ag_esti_list[agent][3]
                    self.daily_vector[agent][11] = self.ag_esti_list[agent][4]
                    self.daily_vector[agent][12] = self.disag_esti_list[agent][0]
                    self.daily_vector[agent][13] = self.disag_esti_list[agent][1]
                    self.daily_vector[agent][14] = self.disag_esti_list[agent][2]
                    self.daily_vector[agent][15] = self.disag_esti_list[agent][3]
                    self.daily_vector[agent][16] = self.disag_esti_list[agent][4]
            

            # if base_info["day"] == 2:
            for i in range(len(self.daily_vector)):
                self.player_x.append([base_info["day"],np.sum(self.co_list,axis=0)[1]] + self.daily_vector[i,:].tolist())
            self.daily_x.append([base_info["day"],np.sum(self.co_list,axis=0)[1]] + self.daily_vector.reshape(-1).tolist())
            # print(self.daily_vector)
            # print(self.player_x)



    def dayStart(self):
        return None
    
    def talk(self):
        if(self.base_info['myRole']=="VILLAGER"):
            # if(self.done_last_commingout == False):
            #     self.done_last_commingout = True
            #     return cb.comingout((self.base_info['agentIdx']),"VILLAGER")
            if(self.have_ever_vote == False):
                self.have_ever_vote = True
                # return cb.vote(np.argmax(np.sum(self.declaration_vote_list)) + 1)
                return cb.vote(self.selectAgent())
            else:
                return cb.over()
        elif (self.base_info['myRole'] == 'SEER'):
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),"SEER")
            if(self.not_reported == True):
                self.not_reported = False
                return self.myresult
        elif self.base_info['myRole'] == 'POSSESSED':
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),self.fakeRole)
            if(self.not_reported == True):
                self.not_reported = False
                return self.myresult
        elif self.base_info['myRole'] == 'WEREWOLF':
            if(self.have_ever_vote == False):
                self.have_ever_vote = True
                # return cb.vote(np.argmax(np.sum(self.declaration_vote_list)) + 1)
                return cb.vote(self.selectAgent())
            else:
                return cb.over()

        return cb.over()

    def whisper(self):
        # print("whisper")
        return cb.over()

    def randomSelect(self):
        while(True):
            target = np.random.randint(0,self.game_setting["playerNum"])
            if target != self.base_info["agentIdx"]-1 and self.alive_list[target]==0:
                return target

    def selectAgent(self):
        est_werewolf_list = self.daily_net(np.asarray([self.base_info["day"],np.sum(self.co_list,axis=0)[1]] + self.daily_vector.reshape(-1).tolist()).astype(np.float32).reshape(1,-1)).reshape(-1).array
        est_werewolf_list = list(zip(est_werewolf_list,range(1,len(est_werewolf_list)+1)))
        est_role_list = []
        for i in range(self.num):
            role = np.argmax(self.player_net(np.asarray([self.base_info["day"],np.sum(self.co_list,axis=0)[1]] + self.daily_vector[i,:].reshape(-1).tolist()).astype(np.float32).reshape(1,-1)).reshape(-1).array)
            role = [key for key,value in self.role_to_num.items() if value == role][0]
            # self.num_to_role = {0:}
            est_role_list.append(role)

        # print(est_werewolf_list)
        # print(est_role_list)
        if self.hw_dic[self.base_info['myRole']] == 0 or (self.base_info['myRole']=='POSSESSED' and self.request=='vote'): #村人陣営
            tmp = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "WEREWOLF"]
            if len(tmp) != 0 and not(len(tmp)==1) and tmp[0][1]==self.base_info["agentIdx"]:
                est_werewolf_list = tmp
            est_werewolf_list = sorted(est_werewolf_list,reverse=True)
            for _,target in est_werewolf_list:
                if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                    return target
    

        elif self.hw_dic[self.base_info['myRole']] == 1: #人狼陣営
            # print([x for i,x in zip(est_role_list,est_werewolf_list)])
            tmp = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "SEER"]
            if len(tmp) == 0:
                tmp = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "VILLAGER"]
            if len(tmp) == 0:
                tmp = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "POSSESSED"]
            if len(tmp) == 0:
                return self.randomSelect()
            est_werewolf_list = tmp
            est_werewolf_list = sorted(est_werewolf_list,reverse=True)
            for _,target in est_werewolf_list:
                if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                    return target
            return self.randomSelect()

        else:
            return self.randomSelect()
        
    def vote(self):
        # print("vote")
        return self.selectAgent()
    
    def attack(self):
        # print("attack")
        return self.selectAgent()
    
    def divine(self):
        # print("devine")
        return self.selectAgent()
    
    def guard(self):
        # print("guard")
        return self.base_info['agentIdx']
    
    def finish(self):
        for i in range(len(self.daily_x)):
            self.daily_x_t.append(self.daily_x[i] + self.daily_t)

        for x_t in np.hstack((np.asarray(self.player_x), np.tile(np.array(self.player_t).reshape(-1,1),(len(self.player_x)//len(self.player_t),1)))).tolist():
            self.player_x_t.append(x_t)


        # if 100 < len(self.daily_x_t):
        #     daily_x = np.array(self.daily_x,dtype=np.float32)

        #     daily_t = np.array(self.daily_t,dtype=np.int32).repeat(len(daily_x),axis=0)
        #     with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
        #         daily_y = self.daily_net(daily_x)
        #         loss = F.softmax_cross_entropy(daily_y,daily_t)
        #         accuracy = F.accuracy(daily_y,daily_t)

        #         self.daily_sum_loss = np.append(self.daily_sum_loss,loss.array)
        #         self.daily_sum_accuracy = np.append(self.daily_sum_accuracy,accuracy.array)

        # if 100 < len(self.player_x):
        #     player_x = np.array(self.player_x,dtype=np.float32)
        #     player_t = np.tile(np.array(self.player_t,dtype=np.int32),len(self.player_x)//len(self.player_t))
        #     # print(player_x,player_t)
        #     with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
        #         player_y = self.player_net(player_x)
        #         loss = F.softmax_cross_entropy(player_y,player_t)
        #         accuracy = F.accuracy(player_y,player_t)

        #         self.player_sum_loss = np.append(self.player_sum_loss,loss.array)
        #         self.player_sum_accuracy = np.append(self.player_sum_accuracy,accuracy.array)        

        # if(self.train_cnt%100 == 0):
        #     self.daily_loss_train = np.append(self.daily_loss_train,np.mean(self.daily_sum_loss))
        #     self.daily_accuracy_train = np.append(self.daily_accuracy_train,np.mean(self.daily_sum_accuracy))
        #     self.player_loss_train = np.append(self.player_loss_train,np.mean(self.player_sum_loss))
        #     self.player_accuracy_train = np.append(self.player_accuracy_train,np.mean(self.player_sum_accuracy))


        def cal_accu_loss(x,t,net):
            with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
                y = net(x)
                loss = F.softmax_cross_entropy(y,t)
                accuracy = F.accuracy(y,t)

                y = y.array
                for i in range(len(y)):
                    self.role_cnt[self.num_to_role[t[i]]] += 1
                    self.predict_cnt[self.num_to_role[np.argmax(y[i])]] += 1
                    if np.argmax(y[i]) == t[i]:
                        self.correct_predict_cnt[self.num_to_role[t[i]]] += 1

            return loss.array, accuracy.array
                               
                
            # else:
            #     if(net=="daily_net"):
            #         y = self.daily_net(x)
            #         loss = F.softmax_cross_entropy(y,t)
            #         accuracy = F.accuracy(y,t)        
            #         self.daily_net.cleargrads()
            #         loss.backward()
            #         self.daily_optimizer.update()

            #     elif net == "daily_net":
            #         y = self.player_net(x)
            #         loss = F.softmax_cross_entropy(y,t)
            #         accuracy = F.accuracy(y,t)        
            #         self.daily_net.cleargrads()
            #         loss.backward()
            #         self.daily_optimizer.update()
                    


        def plot_accu_loss(daily_loss_mean, daily_accu_mean, player_loss_mean, player_accu_mean):
            plt.subplot(2,2,1)
            plt.plot(self.daily_loss_mean,label="daily_loss")
            plt.legend()
            plt.subplot(2,2,2)
            plt.plot(self.daily_accu_mean,label="daily_accu")
            plt.legend()
            plt.subplot(2,2,3)
            plt.plot(self.player_loss_mean,label="player_loss")
            plt.legend()
            plt.subplot(2,2,4)
            plt.plot(self.player_accu_mean,label="player_accu")
            plt.legend()
            plt.show()




        if 100 < len(self.daily_x_t) and self.daily_train == True:
            daily_x_t_100 = np.asarray(self.daily_x_t)[np.random.choice(len(self.daily_x_t),100),:]
            daily_x_100 = daily_x_t_100[:,:-1].astype(np.float32)
            daily_t_100 = daily_x_t_100[:,-1].astype(np.int32)

            daily_y_100 = self.daily_net(daily_x_100)
            loss = F.softmax_cross_entropy(daily_y_100,daily_t_100)
            accuracy = F.accuracy(daily_y_100,daily_t_100)        
            self.daily_net.cleargrads()
            loss.backward()
            self.daily_optimizer.update()

            self.daily_loss_sum.append(loss.array)
            self.daily_accu_sum.append(accuracy.array)
            self.daily_loss_mean.append(np.mean(self.daily_loss_sum))
            self.daily_accu_mean.append(np.mean(self.daily_accu_sum))


        if 100 < len(self.player_x_t) and self.player_train == True:
            player_x_t_100 = np.asarray(self.player_x_t)[np.random.choice(len(self.player_x_t),100),:]
            player_x_100 = player_x_t_100[:,:-1].astype(np.float32)
            player_t_100 = player_x_t_100[:,-1].astype(np.int32)

            player_y_100 = self.player_net(player_x_100)
            loss = F.softmax_cross_entropy(player_y_100,player_t_100)
            accuracy = F.accuracy(player_y_100,player_t_100)        
            self.player_net.cleargrads()
            loss.backward()
            self.player_optimizer.update()

            self.player_loss_sum.append(loss.array)
            self.player_accu_sum.append(accuracy.array)
            self.player_loss_mean.append(np.mean(self.player_loss_sum))
            self.player_accu_mean.append(np.mean(self.player_accu_sum))

            # player_y_100 = player_y_100.array
            # for i in range(len(player_y_100)):
            #     self.role_cnt[self.num_to_role[player_t_100[i]]] += 1
            #     self.predict_cnt[self.num_to_role[np.argmax(player_y_100[i])]] += 1
            #     if np.argmax(player_y_100[i]) == player_t_100[i]:
            #         self.correct_predict_cnt[self.num_to_role[player_t_100[i]]] += 1


        if(self.train_cnt == self.train_times):
            if self.daily_train == False:
                daily_x_t = np.asarray(self.daily_x_t)
                daily_x = daily_x_t[:,:-1].astype(np.float32)
                daily_t = daily_x_t[:,-1].astype(np.int32)
                batch_size = 10

                with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
                    for i in range(0,len(daily_x),batch_size):
                        daily_y = self.daily_net(daily_x[i:i+batch_size])
                        loss = F.softmax_cross_entropy(daily_y,daily_t[i:i+batch_size])
                        accu = F.accuracy(daily_y,daily_t[i:i+batch_size])
                        self.daily_loss_sum.append(loss.array)
                        self.daily_accu_sum.append(accu.array)
                        self.daily_loss_mean.append(np.mean(self.daily_loss_sum))
                        self.daily_accu_mean.append(np.mean(self.daily_accu_sum))
            else:
                chainer.serializers.save_npz('./ookawa_train_daily.net', self.daily_net)

            
            if self.player_train == False:
                batch_size = 10
                player_x_t = np.asarray(self.player_x_t)
                player_x = player_x_t[:,:-1].astype(np.float32)
                player_t = player_x_t[:,-1].astype(np.int32)

                with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
                    for i in range(0,len(player_x),batch_size):
                        # loss,accu = cal_accu_loss(player_x[i:i+batch_size],player_t[i:i+batch_size],self.player_net)
                        player_y = self.player_net(player_x[i:i+batch_size])
                        loss = F.softmax_cross_entropy(player_y,player_t[i:i+batch_size])
                        accu = F.accuracy(player_y,player_t[i:i+batch_size])

                        # print(player_y.array)
                        # print()
                        # print(accu.array)

                        self.player_loss_sum.append(loss.array)
                        self.player_accu_sum.append(accu.array)
                        self.player_loss_mean.append(np.mean(self.player_loss_sum))
                        self.player_accu_mean.append(np.mean(self.player_accu_sum))

                        player_y = player_y.array
                        
                        for j in range(len(player_y)):
                            # print(player_t)
                            self.role_cnt[self.num_to_role[player_t[i+j]]] += 1
                            self.predict_cnt[self.num_to_role[np.argmax(player_y[j])]] += 1
                            if np.argmax(player_y[j]) == player_t[i+j]:
                                self.correct_predict_cnt[self.num_to_role[player_t[i+j]]] += 1

            else:
                chainer.serializers.save_npz('./ookawa_train_player.net', self.player_net)



            

            print("daily_loss:{0}   daily_accuracy:{1}\n".format(self.daily_loss_mean[-1],self.daily_accu_mean[-1]))
            print("player_loss:{0}   player_accuracy:{1}\n".format(self.player_loss_mean[-1],self.player_accu_mean[-1]))

            if self.player_train == False:
                sum_role_pred = 0
                sum_correct_pred= 0
                for role in self.role_to_num.keys():
                    print(role , self.role_cnt[role])
                    if self.predict_cnt[role] != 0:
                        print("{} recall:{},   precision{}\n".format(role, self.correct_predict_cnt[role]/self.role_cnt[role], self.correct_predict_cnt[role]/self.predict_cnt[role]))
                        sum_correct_pred += self.correct_predict_cnt[role]
                        sum_role_pred += self.role_cnt[role]
                print("accuracy:{}".format(sum_correct_pred/sum_role_pred))

            plot_accu_loss(self.daily_loss_mean,self.daily_accu_mean,self.player_loss_mean,self.player_accu_mean)


        self.train_cnt += 1
        if self.train_cnt%100 == 0:
            print(self.train_cnt)


        return None



agent = SampleAgent(myname)
    


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    