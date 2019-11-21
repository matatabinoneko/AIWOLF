#!/usr/bin/env python
from __future__ import print_function, division 
import re
import numpy as np

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb


myname = 'matatabinoneko'

class SampleAgent(object):
    
    def __init__(self, agent_name):
        ## myname ##
        self.myname = agent_name
        
        
    def getName(self):
        return self.myname
    
    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        ## game_setting ##
        self.game_setting = game_setting
        # print("initialize: base_info=",base_info,sep='\n')
        # print("initialize: diff_data=",diff_data,sep='\n')

        ### edit from here ###
        self.num = len(base_info["statusMap"])
        self.estimate_list = np.zeros((self.num,self.num,6),dtype=np.int32) #[0:vilagger,1:seer,2:bodyguard,3:medium,4:possesed,5:werewolf]
        self.co_list = np.zeros((self.num,6),dtype=np.int32) #[0:vilagger,1:seer,2:bodyguard,3:medium,4:possesed,5:werewolf]
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


        
        # class MLP(chainer.Chain):
        #     def __init__(self,n_input,n_hidden,n_output):
        #         super(MLP, self).__init__()
        #         self.n_input = n_input
        #         self.n_hidden = n_hidden
        #         self.n_output = n_output
            
        #         with self.init_scope():
        #             self.l1 = L.Linear(self.n_input, self.n_hidden)
        #             self.l2 = L.Linear(self.n_hidden,self.n_output)
                    
        #     def __call__(self, x):
        #         h1 = F.relu(self.l1(x))
        #         h2 = F.relu(self.l2(h1))
        #         return h2

        # n_input = 25
        # n_hidden = 40
        # n_output = 5
        # net = MLP(n_input,n_hidden,n_output)
        # optimizer = optimizers.MomentumSGD(lr=0.001, momentum=0.9)
        # optimizer.setup(net)
        # for param in net.params():
        #     if param.name != 'b':
        #         param.update_rule.add_hook(WeightDecay(0.0001))
        # gpu_id = 0

        # net.to_gpu(gpu_id)


        
    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        # print("\n\nrequest:",request,sep='\n')
        # print("update: base_info=",base_info,sep='\n')
        # print("update: diff_data=",diff_data,sep='\n')

        ### edit from here ###

        # result
        if request == 'DAILY_INITIALIZE':
            self.daily_vector = np.zeros((self.num,8),dtype=np.int32)
            #7:発言と投票先が変わった数をカウント．
            for i in range(self.num):
                if 1 not in self.declaration_vote_list[i]:
                    continue
                for j in range(self.num):
                    if self.vote_list[i][j]==1 and self.declaration_vote_list[i][j]==0:
                        self.daily_vector[i][7] = 1

            # print(self.vote_list)
            # print(self.declaration_vote_list)
            # print(self.daily_vector[:,7])

            self.declaration_vote_list = np.zeros((self.num,self.num),dtype=np.int32)
            self.vote_list = np.zeros((self.num,self.num),dtype=np.int32)

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
        elif request == "DAILY_FINISH":
            #0:日にち　1:占い師の数 2:自分が人間判定された数 3:自分が人狼判定された数 4:占い師の名乗り出た順番 5:報告した人間の数 6:報告した人狼の数 7:発言と投票先が変わった数．
            
            for agent in range(self.num):
                if(not diff_data.empty):    
                    self.daily_vector[agent][0] = diff_data["day"][0]
                    self.daily_vector[agent][1] = np.sum(self.co_list,axis=0)[1]
                    self.daily_vector[agent][2] = np.sum(self.divined_list,axis=0)[agent][0]
                    self.daily_vector[agent][3] = np.sum(self.divined_list,axis=0)[agent][1]
                    self.daily_vector[agent][4] = self.seer_co_oder[agent]
                    self.daily_vector[agent][5] = np.sum(self.divined_list,axis=1)[agent][0]
                    self.daily_vector[agent][6] = np.sum(self.divined_list,axis=1)[agent][1]
                    
            
            print(self.daily_vector)
            # print(self.declaration_vote_list)
            # print(self.vote_list)

        if 0<len(diff_data) and diff_data["type"][0] == "vote":
            for i in range(len(diff_data)-1): 
                # print(diff_data["idx"][i],diff_data["agent"][i])
                self.vote_list[diff_data["idx"][i]-1][diff_data["agent"][i]-1] = 1
            # print(self.vote_list)

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
                elif(role=="BODYGUARD"):
                    self.co_list[agent][2] = 1
                elif(role=="MEDIUM"):
                    self.co_list[agent][3] = 1
                elif(role=="POSSESED"):
                    self.co_list[agent][4] = 1
                elif(role=="WEREWOLF"):
                    self.co_list[agent][5] = 1
                
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
            # elif(diff_data["type"][0]=="talk")


    def dayStart(self):
        return None
    
    def talk(self):
        if(self.base_info['myRole']=="VILLAGER"):
            if(self.done_last_commingout == False):
                self.done_last_commingout = True
                return cb.comingout((self.base_info['agentIdx']),"VILLAGER")
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
        elif self.base_info['myRole'] == 'MEDIUM' and self.not_reported:
            self.not_reported = False
            return self.myresult
        
        return cb.over()

    def whisper(self):
        # print("whisper")
        return cb.over()
        
    def vote(self):
        # print("vote")
        # return self.base_info['agentIdx']
        return np.argmax(np.sum(self.declaration_vote_list)) + 1

    
    def attack(self):
        # print("attack")
        return self.base_info['agentIdx']
    
    def divine(self):
        alived_list = []
        for i in range(1,self.num+1):
            if(i != self.base_info["agentIdx"] and self.base_info["statusMap"][str(i)]=="ALIVE"):
                alived_list.append(i)
        target = np.random.choice(alived_list)

        return target
    
    def guard(self):
        # print("guard")
        return self.base_info['agentIdx']
    
    def finish(self):
        # print(self.declaration_vote_list)
        # print(self.co_list)


        return None



agent = SampleAgent(myname)
    


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
    