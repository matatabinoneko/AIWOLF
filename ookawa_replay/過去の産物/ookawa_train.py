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
import chainer.links as L
import chainer.functions as F
from chainer import serializers

import matplotlib.pyplot as plt

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
                h1 = F.relu(self.l1(x))
                h2 = F.relu(self.l2(h1))
                h3 = self.l3(h2)
                return h3

        self.daily_net = MLP(n_input=87,n_hidden=200,n_output=5)
        self.player_net = MLP(n_input=19,n_hidden=50,n_output=4)

        serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/daily_model/ookawa_daily.net", self.daily_net)
        serializers.load_npz("/Users/matatabinoneko/University/AIWOLF/ookawa_replay/player_model/ookawa_player.net", self.player_net)


        self.optimizer = chainer.optimizers.SGD(lr=0.0001)
        self.optimizer.setup(self.daily_net)
        self.optimizer.setup(self.player_net)

        self.train_cnt = 1
        
        self.daily_sum_loss = np.asarray([])
        self.daily_sum_accuracy = np.asarray([])
        self.daily_loss_train = np.asarray([])
        self.daily_accuracy_train = np.asarray([])
        self.player_sum_loss = np.asarray([])
        self.player_sum_accuracy = np.asarray([])
        self.player_loss_train = np.asarray([])
        self.player_accuracy_train = np.asarray([])

        self.daily_x_t = []

        self.player_x_t = []
        

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
        self.role_dic = {"VILLAGER":0,"SEER":1,"POSSESSED":2,"WEREWOLF":3}
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

        self.declaration_vote_list = np.zeros((self.num,self.num),dtype=np.int32)
        self.vote_list = np.zeros((self.num,self.num),dtype=np.int32)
        self.vote_another_people = np.zeros(self.num,dtype=np.int32)

        self.alive_list = np.zeros(self.num,dtype=np.int32)#alive:0 attacked:1 execute:-1

        self.ag_esti_list = np.zeros((5,5),dtype=np.int32)
        self.disag_esti_list = np.zeros((5,5),dtype=np.int32)

        self.daily_x = []
        self.daily_t = []

        self.player_x = []
        self.player_t = []

        
    def update(self, base_info, diff_data, request):
        self.base_info = base_info
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
                # print(diff_data)
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

            elif diff_data["type"][0]=="finish":# and len(self.x)==len(self.t)+1:
                for i in diff_data.index.values:
                    agent = diff_data["agent"][i]-1
                    role = diff_data["text"][i].split(' ')[2]
                    self.player_t.append(self.role_dic[role])
                    if(role == "WEREWOLF"):
                        self.daily_t.append(agent)
                
                self.daily_x_t.append(list(zip(self.daily_x,np.asarray(self.daily_t).repeat(len(self.daily_x),axis=0).tolist())))
                self.player_x_t.append(list(zip(self.player_x,np.tile(np.asarray(self.player_t),len(self.player_x)//self.player_t))))
    
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
                self.player_x.append(np.insert(self.daily_vector[i,:],[0,0],[base_info["day"],np.sum(self.co_list,axis=0)[1]]))
            self.daily_x.append(np.insert(self.daily_vector.reshape(1,-1),[0,0],[base_info["day"],np.sum(self.co_list,axis=0)[1]]))

            # print(self.daily_vector)



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
                return cb.vote(self.base_info["agentIdx"])
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
                return cb.comingout((self.base_info['agentIdx']),"SEER")
            if(self.not_reported == True):
                self.not_reported = False
                return self.myresult
        elif self.base_info['myRole'] == 'WEREWOLF':
            if(self.have_ever_vote == False):
                self.have_ever_vote = True
                # return cb.vote(np.argmax(np.sum(self.declaration_vote_list)) + 1)
                return cb.vote(self.base_info["agentIdx"])
            else:
                return cb.over()

        return cb.over()

    def whisper(self):
        # print("whisper")
        return cb.over()

    def selectAgent(self):
        target_list = self.net(np.insert(self.daily_vector.reshape(1,-1),[0,0],[self.base_info["day"],np.sum(self.co_list,axis=0)[1]]).astype(np.float32).reshape(1,-1)).array.reshape(-1).tolist()
        target_list = list(zip(target_list,range(1,len(target_list)+1)))
        target_list = sorted(target_list,reverse=True)
        for _,target in target_list:
            if self.alive_list[target-1] == 0 and target != self.base_info['agentIdx']:
                return target
        
    def vote(self):
        return self.selectAgent()


    
    def attack(self):
        # print("attack")
        return self.base_info['agentIdx']
    
    def divine(self):
        return self.selectAgent()
    
    def guard(self):
        # print("guard")
        return self.base_info['agentIdx']
    
    def finish(self):
        # print(len(self.x),len(self.t))
        if 100 <= len(self.daily_x):
            daily_x = np.array(self.daily_x,dtype=np.float32)

            daily_t = np.array(self.daily_t,dtype=np.int32)

            daily_y = self.daily_net(daily_x)
            daily_loss = F.softmax_cross_entropy(daily_y,daily_t)
            daily_accuracy = F.accuracy(daily_y,daily_t)

            self.daily_net.cleargrads()
            daily_loss.backward()
            self.daily_optimizer.update()

            self.daily_sum_loss = np.append(self.daily_sum_loss,daily_loss.array)
            self.daily_sum_accuracy = np.append(self.daily_sum_accuracy,daily_accuracy.array)

            self.daily_loss_train = np.append(self.daily_loss_train,np.mean(self.daily_sum_loss))
            self.daily_accuracy_train = np.append(self.daily_accuracy_train,np.mean(self.daily_sum_accuracy))

        
        if(self.train_cnt == 1000):
            plt.subplot(1,2,1)
            plt.plot(self.daily_loss_train,label="loss")
            plt.legend()
            plt.subplot(1,2,2)
            plt.plot(self.daily_accuracy_train,label="accu")
            plt.legend()
            plt.show()

            print("daily_loss:{0} daily_accuracy:{1}".format(np.mean(self.daily_sum_loss),np.mean(self.daily_sum_accuracy)))
            # chainer.serializers.save_npz('ookawa_'+str(self.train_cnt)+'_lr=001.net', self.net)
            chainer.serializers.save_npz('train_only.net', self.daily_net)


        self.train_cnt += 1
        return None



agent = SampleAgent(myname)
    


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    