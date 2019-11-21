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

        #ゲーム設定
        self.daily_train = False
        self.player_train = False
        self.train_times = 1000
        self.agent_num = 7
        self.player_vector_length = self.agent_num*2 + 9
        self.role_to_num = {"VILLAGER":0,"SEER":1,"POSSESSED":2,"WEREWOLF":3}
        self.agent_num_to_role = {v:k for k,v in self.role_to_num.items()}
        self.human_list = ["HUMAN","VILLAGER","SEER",]
        self.werewolf_list = ["POSSESSED","WEREWOLF"]
        self.role_num = len(self.role_to_num)

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


        self.daily_net = MLP(n_input=(self.player_vector_length-2)*self.agent_num+2,n_hidden=200,n_output=self.agent_num)
        self.player_net = MLP(n_input=self.player_vector_length,n_hidden=50,n_output=self.role_num)


        serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_train_daily_"+str(self.agent_num)+".net", self.daily_net)
        serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_train_player_" + str(self.agent_num) + ".net", self.player_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_daily.net", self.daily_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_player.net", self.player_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_train_only_daily.net", self.daily_net)
        # serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_train_only_player.net", self.player_net)

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




    def randomSelect(self):
        while(True):
            target = np.random.randint(0,self.game_setting["playerNum"])
            if target != self.base_info["agentIdx"]-1 and self.alive_list[target]==0:
                return target

    def selectAgent(self,target_role):
        # if target_role == 'WEREWOLF':
        est_werewolf_list = self.daily_net(np.asarray([self.base_info["day"],np.sum(self.co_list,axis=0)[1]] + self.daily_vector.reshape(-1).tolist()).astype(np.float32).reshape(1,-1)).reshape(-1).array
        est_werewolf_list = list(zip(est_werewolf_list,range(1,len(est_werewolf_list)+1)))
        est_role_list = []
        for i in range(self.agent_num):
            role = np.argmax(self.player_net(np.asarray([self.base_info["day"],np.sum(self.co_list,axis=0)[1]] + self.daily_vector[i,:].reshape(-1).tolist()).astype(np.float32).reshape(1,-1)).reshape(-1).array)
            role = [key for key,value in self.role_to_num.items() if value == role][0]
            # self.agent_num_to_role = {0:}
            est_role_list.append(role)

        # print(est_werewolf_list)
        # print(est_role_list)
        if target_role == "WEREWOLF":
            tmp = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "WEREWOLF"]
            if len(tmp) != 0 and not(len(tmp)==1) and tmp[0][1]==self.base_info["agentIdx"]:
                est_werewolf_list = tmp
            est_werewolf_list = sorted(est_werewolf_list,reverse=True)
            for _,target in est_werewolf_list:
                if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                    return target
    
        elif target_role == "SEER":
            # print([x for i,x in zip(est_role_list,est_werewolf_list)])
            est_seer_list = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "SEER"]
            est_seer_list = sorted(est_werewolf_list,reverse=True)
            for _,target in est_seer_list:
                if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                    return target
            return -1
        elif target_role == "VILLAGER":
            # print([x for i,x in zip(est_role_list,est_werewolf_list)])
            est_villager_list = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "villager"]
            est_villager_list = sorted(est_werewolf_list,reverse=True)
            for _,target in est_villager_list:
                if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                    return target
            return -1
        elif target_role == "POSSESSED":
            # print([x for i,x in zip(est_role_list,est_werewolf_list)])
            est_possessed_list = [x for i,x in zip(est_role_list,est_werewolf_list) if i == "possessed"]
            est_possessed_list = sorted(est_werewolf_list,reverse=True)
            for _,target in est_possessed_list:
                if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                    return target
            return -1        
        else:
            return -1

    def understand_text(self,agent,talk_texts):
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
                if target<self.agent_num and agent<self.agent_num:
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

    def updateAliveList(self,index):
        if self.diff_data["type"][index] == "dead":
            agent = self.diff_data["agent"][len(self.diff_data)-1]-1
            self.alive_list[agent] = -1
        elif self.diff_data["type"][index] == "execute":
            agent = self.diff_data["agent"][len(self.diff_data)-1]-1
            self.alive_list[agent] = 1

    def updateVoteList(self,index):
        self.vote_list[self.diff_data["idx"][index]-1][self.diff_data["agent"][index]-1] = 1
        # print(self.vote_list)

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

    def updateVote_declare(self):
        self.daily_vector.fill(0)
        #7:発言と投票先が変わった数をカウント．
        # print(self.declaration_vote_list)
        # print(self.vote_list)
        for i in range(self.agent_num):
            if 1 not in self.declaration_vote_list[i]:
                continue
            for j in range(self.agent_num):
                if self.vote_list[i][j]==1 and self.declaration_vote_list[i][j]==0:
                    self.vote_another_people[i] += 1

        self.declaration_vote_list.fill(0)
        self.vote_list.fill(0)

    def getResult(self,index):
        #昨夜の能力行使の結果を取得

        # IDENTIFY
        if self.diff_data['type'][index] == 'identify':
            self.not_reported = True
            self.myresult = self.diff_data['text'][index]
            
        # DIVINE
        if self.diff_data['type'][index] == 'divine':
            self.not_reported = True
            self.myresult = self.diff_data['text'][index]
            
        # GUARD
        if self.diff_data['type'][index] == 'guard':
            self.myresult = self.diff_data['text'][index]

    def getFakeResult(self):
        # FAKE DIVINE
        if self.fake_role == 'SEER':
            self.not_reported = True
            idx = self.selectAgent("WEREWOLF")+1
            self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'

    def updateVector(self):
        seer_num = np.sum(self.co_list,axis=0)[1]
        common_vector = [self.base_info["day"],seer_num]

        divined_human_werewolf_list = np.sum(self.divined_list,axis=0)
        divine_human_werewolf_list = np.sum(self.divined_list,axis=1)

        self.daily_vector = np.hstack((divined_human_werewolf_list,self.seer_co_oder.reshape(-1,1),divine_human_werewolf_list,self.vote_another_people.reshape(-1,1),self.alive_list.reshape(-1,1),self.ag_esti_list,self.disag_esti_list))
        # print(self.daily_vector.shape)
        # if base_info["day"] == 2:
        for i in range(len(self.daily_vector)):
            self.player_x.append(common_vector + self.daily_vector[i,:].tolist())
        self.daily_x.append(common_vector + self.daily_vector.reshape(-1).tolist())
        # for i in range(len(self.daily_x)):
        #     print(len(self.daily_x[i]))
        # print(self.player_x)

    def countEachRole(self,index):
        agent = self.diff_data["agent"][index]-1
        role = self.diff_data["text"][index].split(' ')[2]
        self.player_t.append(self.role_to_num[role])
        # self.agent_num_to_role = {0:}
        if(role == "WEREWOLF"):
            self.daily_t[index] = 1
        # print(self.diff_data)
        # print(self.player_t)









    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        ## game_setting ##
        self.game_setting = game_setting
        # print("initialize: base_info=",base_info,sep='\n')
        # print("initialize: diff_data=",diff_data,sep='\n')

        ### edit from here ###
        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.daily_vector = np.zeros((self.agent_num,self.player_vector_length-2),dtype=np.int32)

        self.estimate_list = np.zeros((self.agent_num,self.agent_num,self.role_num),dtype=np.int32) #[0:vilagger,1:seer,2:possesed,3:werewolf]
        self.co_list = np.zeros((self.agent_num,self.role_num),dtype=np.int32) #[0:vilagger,1:seer,2:possesed,3:werewolf]
        self.seer_co_cnt = 1
        self.seer_co_oder = np.zeros(self.agent_num,dtype=np.int32)
        self.seer_have_been_co = False
        self.have_ever_vote = False
        self.divined_list = np.zeros((self.agent_num,self.agent_num,2)) #0:HUMAN 1:WEREWOLF
        self.comingout = ''
        self.myresult = ''
        self.not_reported = False
        self.done_last_commingout = False
        self.vote_declare = False

        self.fake_role = ''
        if self.base_info['myRole']=='POSSESSED':
            self.fake_role = 'SEER'

        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_another_people = np.zeros(self.agent_num,dtype=np.int32)

        self.alive_list = np.zeros(self.agent_num,dtype=np.int32)#alive:0 attacked:1 execute:-1

        self.ag_esti_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.disag_esti_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        # self.talk_day_id = [[]for i in range(5)]

        self.daily_x = []
        self.daily_t = [0 for i in range(self.agent_num)]
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

        for i in range(len(self.diff_data)):
            if self.diff_data["type"][i] == "talk":
                self.updateTalk(i)
            elif self.diff_data["type"][i] == "vote":
                self.updateVoteList(i)
            elif self.diff_data["type"][i] == 'finish':
                self.countEachRole(i)
            elif self.diff_data["type"][i] == "dead" or self.diff_data["type"][i] == "excute":
                self.updateAliveList(i)
            elif self.diff_data['type'][i] == 'identify' or self.diff_data['type'][i] == 'divine' or self.diff_data['type'][i] == 'guard':
                self.getResult(i)
            # elif self.diff_data["type"][i] == "":

        # self.updateTalk()
        if self.fake_role != '' and self.not_reported == False:
            self.getFakeResult()


        if request == 'DAILY_INITIALIZE' and 2 <= base_info["day"]:
            self.updateVote_declare()

        elif request == "DAILY_FINISH" and 1 <= base_info["day"]:
            #0:自分が人間判定された数 1:自分が人狼判定された数 2:占い師の名乗り出た順番 3:報告した人間の数 4:報告した人狼の数 5:発言と投票先が変わった数．6:生死(#alive:0 attacked:1 execute:-1)　7~11:肯定的意見の数　12~16:否定的意見の数        
            self.updateVector()

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
                return cb.vote(self.selectAgent("WEREWOLF"))
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
                return cb.comingout((self.base_info['agentIdx']),self.fake_role)
            if(self.not_reported == True):
                self.not_reported = False
                return self.myresult
        elif self.base_info['myRole'] == 'WEREWOLF':
            if(self.have_ever_vote == False):
                self.have_ever_vote = True
                # return cb.vote(np.argmax(np.sum(self.declaration_vote_list)) + 1)
                return cb.vote(self.selectAgent("WEREWOLF"))
            else:
                return cb.over()

        return cb.over()

    def whisper(self):
        # print("whisper")
        return cb.over()

    def vote(self):
        # print("vote")
        if self.base_info["myRole"] in self.werewolf_list:
            for target_role in ["SEER","VILLAGER","POSSESSED"]:
                target = self.selectAgent(target_role)
                if target != -1:
                    return target
            return self.randomSelect()
        elif self.base_info["myRole"] in self.human_list:
            for target_role in ["WEREWOLF", "POSSESSED"]:
                target = self.selectAgent(target_role)
                if target != -1:
                    return target
            return self.randomSelect()

    def attack(self):
        # print("attack")
        for target_role in ["SEER","VILLAGER","POSSESSED"]:
            target = self.selectAgent(target_role)
            if target != -1:
                return target
        return self.randomSelect()

    def divine(self):
        # print("devine")
        for target_role in ["WEREWOLF", "POSSESSED"]:
            target = self.selectAgent(target_role)
            if target != -1:
                return target
        return self.randomSelect()

    def guard(self):
        # print("guard")
        for target_role in ["SEER","VILLAGER"]:
            target = self.selectAgent(target_role)
            if target != -1:
                return target
        return self.randomSelect()

    def finish(self):
        for i in range(len(self.daily_x)):
            self.daily_x_t.append(self.daily_x[i] + self.daily_t)
            # print(len(self.daily_t))
        for x_t in np.hstack((np.asarray(self.player_x), np.tile(np.array(self.player_t).reshape(-1,1),(len(self.player_x)//len(self.player_t),1)))).tolist():
            self.player_x_t.append(x_t)



        # def cal_accu_loss(x,t,net):
        #     with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
        #         y = net(x)
        #         loss = F.softmax_cross_entropy(y,t)
        #         accuracy = F.accuracy(y,t)

        #         y = y.array
        #         for i in range(len(y)):
        #             self.role_cnt[self.agent_num_to_role[t[i]]] += 1
        #             self.predict_cnt[self.agent_num_to_role[np.argmax(y[i])]] += 1
        #             if np.argmax(y[i]) == t[i]:
        #                 self.correct_predict_cnt[self.agent_num_to_role[t[i]]] += 1

        #     return loss.array, accuracy.array
                    


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
            random_index = np.random.choice(len(self.daily_x_t),100)
            # print((np.asarray(self.daily_x_t)[:,self.player_vector_length]).reshape)
            # print((np.asarray(self.daily_x_t)[:,-1])[:,1])
            daily_x_100 = np.asarray(self.daily_x_t)[:,:-1*self.agent_num][random_index,:].astype(np.float32)
            daily_t_100 = np.array(self.daily_x_t)[:,-1*self.agent_num:][random_index].astype(np.int32)

            daily_y_100 = self.daily_net(daily_x_100)
            loss = F.sigmoid_cross_entropy(daily_y_100,daily_t_100)

            # accuracy = 0
            # print(np.argsort(-1*daily_y_100.array,axis=1)[:,np.count_nonzero(daily_t_100[0])])
            # print(np.count_nonzero((np.logical_and(daily_y_100 >= ), daily_t_100)))

            # accuracy += np.count_nonzero(np.logical_and(np.argsort(-1*daily_y_100.array,axis=1)[:,np.count_nonzero(daily_t_100)] < daily_y_100, daily_t_100))
            # accuracy = F.accuracy(np.argsort(-1*daily_y_100.array,axis=1)[:,:2].astype(np.float32),np.arange(200).reshape(-1,2))        
            # accuracy = F.accuracy(daily_y_100,daily_t_100)
            self.daily_net.cleargrads()
            loss.backward()
            self.daily_optimizer.update()

            self.daily_loss_sum.append(loss.array)
            # self.daily_accu_sum.append(accuracy.array)
            self.daily_loss_mean.append(np.mean(self.daily_loss_sum))
            # self.daily_accu_mean.append(np.mean(self.daily_accu_sum))


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
            #     self.role_cnt[self.agent_num_to_role[player_t_100[i]]] += 1
            #     self.predict_cnt[self.agent_num_to_role[np.argmax(player_y_100[i])]] += 1
            #     if np.argmax(player_y_100[i]) == player_t_100[i]:
            #         self.correct_predict_cnt[self.agent_num_to_role[player_t_100[i]]] += 1


        if(self.train_cnt == self.train_times):
            if self.daily_train == False:

                daily_x = np.asarray(self.daily_x_t)[:,:-1*self.agent_num].astype(np.float32)
                daily_t = np.array(self.daily_x_t)[:,-1*self.agent_num:].astype(np.int32)
                batch_size = 10

                with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
                    for i in range(0,len(daily_x),batch_size):
                        daily_y = self.daily_net(daily_x[i:i+batch_size])
                        loss = F.sigmoid_cross_entropy(daily_y,daily_t[i:i+batch_size])
                        # accu = F.accuracy(daily_y,daily_t[i:i+batch_size])
                        self.daily_loss_sum.append(loss.array)
                        # self.daily_accu_sum.append(accu.array)
                        self.daily_loss_mean.append(np.mean(self.daily_loss_sum))
                        # self.daily_accu_mean.append(np.mean(self.daily_accu_sum))
            else:
                chainer.serializers.save_npz('./ookawa_train_daily_'+str(self.agent_num)+'.net', self.daily_net)

            
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
                            # print(player_y[j],player_t[i+j])
                            self.role_cnt[self.agent_num_to_role[player_t[i+j]]] += 1
                            self.predict_cnt[self.agent_num_to_role[np.argmax(player_y[j])]] += 1
                            if np.argmax(player_y[j]) == player_t[i+j]:
                                self.correct_predict_cnt[self.agent_num_to_role[player_t[i+j]]] += 1

            else:
                chainer.serializers.save_npz('./ookawa_train_player_'+str(self.agent_num)+'.net', self.player_net)



            
            # if 0<len(self.daily_loss_mean) and 0 < len(self.player_loss_mean):
            #     print("daily_loss:{0}   daily_accuracy:{1}\n".format(self.daily_loss_mean[-1],self.daily_accu_mean[-1]))
            #     print("player_loss:{0}   player_accuracy:{1}\n".format(self.player_loss_mean[-1],self.player_accu_mean[-1]))

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
    