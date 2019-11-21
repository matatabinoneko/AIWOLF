import aiwolfpy
import aiwolfpy.contentbuilder as cb
import numpy as np
import re
import os
import chainer 
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import chainer.links as L
import chainer.functions as F
from chainer import serializers

import matplotlib.pyplot as plt
from collections import defaultdict

class data_info():
    def createDailyVector(self):
        self.daily_vector = np.hstack((self.co_list,self.divined_list.transpose((1,0,2)).reshape(self.agent_num,-1),self.seer_co_oder.reshape(-1,1),self.vote_another_people.reshape(-1,1),self.alive_list.reshape(-1,1),self.ag_esti_list,self.disag_esti_list))


    def __init__(self,agent_num=5,daily_train=False,player_train=False,train_times=1000,each_model=True):
        super().__init__()
        self.agent_num = agent_num
        self.daily_train = daily_train
        self.player_train = player_train
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


        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.estimate_list = np.zeros((self.agent_num,self.agent_num,self.role_num),dtype=np.int32) 
        self.co_list = np.zeros((self.agent_num,self.role_num),dtype=np.int32) 
        self.seer_co_oder = np.zeros(self.agent_num,dtype=np.int32)
        self.divined_list = np.zeros((self.agent_num,self.agent_num,2)) #0:HUMAN 1:WEREWOLF
        self.declaration_vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.vote_another_people = np.zeros(self.agent_num,dtype=np.int32)
        self.alive_list = np.zeros((self.agent_num,),dtype=np.int32)
        # self.alive_list[:,0] = 1    
        # self.alive_to_num = {"alive":0,"dead":1,"execute":2}
        self.ag_esti_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)
        self.disag_esti_list = np.zeros((self.agent_num,self.agent_num),dtype=np.int32)

        self.createDailyVector()
        self.player_vector_length = self.daily_vector.shape[1] + 1
        self.daily_vector_length = (self.player_vector_length - 1)*self.agent_num + 1


        self.role_cnt = defaultdict(int)
        self.predict_cnt = defaultdict(int)
        self.correct_predict_cnt = defaultdict(int)
        self.using_alive_info_cnt_daily  = 0
        self.using_alive_info_cnt_player = 0



        self.daily_net = [predict_werewolf(n_input=self.daily_vector_length, n_hidden=200, n_output=self.agent_num) for i in range(self.agent_num)]
        self.player_net = [predict_role(n_input=self.player_vector_length,n_hidden=50,n_output=self.role_num) for i in range(self.agent_num)]
        if self.each_model == True:
            if self.daily_train == False:
                for i in range(len(self.daily_net)):
                    serializers.load_npz('./daily_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/ookawa_train_daily_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.daily_net[i].net)
            if self.player_train == False:
                for i in range(len(self.player_net)):
                    serializers.load_npz('./player_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/ookawa_train_player_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_10000.net', self.player_net[i].net)
        else:
            if self.daily_train == False:
                serializers.load_npz("./daily_model/agent"+str(self.agent_num)+"/one_model/ookawa_train_daily_num_"+str(self.agent_num)+"_train_10000.net", self.daily_net[0].net)
            if self.player_train == False:
                serializers.load_npz("./player_model/agent"+str(self.agent_num)+"/one_model/ookawa_train_player_num_"+str(self.agent_num)+"_train_10000.net", self.player_net[0].net)

    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        self.diff_data = diff_data
        self.game_setting = game_setting

        self.declaration_vote_list.fill(0)
        self.vote_list.fill(0)
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
        self.done_last_commingout = False
        self.vote_declare = False

        self.fake_role = ''
        if self.base_info['myRole']=='POSSESSED':
            self.fake_role = 'SEER'

        self.declaration_vote_list.fill(0)
        self.vote_list.fill(0)
        self.vote_another_people.fill(0)

        self.alive_list.fill(0)
        # self.alive_list[:,0] = 1


        self.ag_esti_list.fill(0)
        self.disag_esti_list.fill(0)

        self.daily_x = [[]]
        self.daily_t = [0 for i in range(self.agent_num)]
        self.player_x = [[]for i in range(self.agent_num)]
        self.player_t = [0 for i in range(self.agent_num)]


    def randomSelect(self):
        while(True):
            target = np.random.randint(0,self.game_setting["playerNum"])
            if target != self.base_info["agentIdx"]-1 and self.alive_list[target]==0:
                return target

    def selectAgent(self,target_role):
        if self.each_model == True:
            use_model = self.base_info["day"]
        else:
            use_model = 0

        self.createDailyVector()
        est_werewolf_list = self.daily_net[use_model].net(np.asarray([self.base_info["day"]] + self.daily_vector.reshape(-1).tolist()).astype(np.float32).reshape(1,-1)).reshape(-1).array
        est_werewolf_list = list(zip(est_werewolf_list,range(1,len(est_werewolf_list)+1)))
        est_role_list = []
        for i in range(self.agent_num):
            role = np.argmax(self.player_net[use_model].net(np.asarray([self.base_info["day"]] + self.daily_vector[i,:].reshape(-1).tolist()).astype(np.float32).reshape(1,-1)).reshape(-1).array)
            # role = [key for key,value in self.role_to_num.items() if value == role][0]
            role = self.num_to_role[role]
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
        common_vector = [self.base_info["day"]]

        divined_human_werewolf_list = np.sum(self.divined_list,axis=0)
        divine_human_werewolf_list = np.sum(self.divined_list,axis=1)

        self.createDailyVector()

        for i in range(len(self.daily_vector)):
            self.player_x[self.base_info["day"]].append(common_vector + self.daily_vector[i,:].tolist())
        self.daily_x.append(common_vector + self.daily_vector.reshape(-1).tolist())
        # for i in range(len(self.daily_x)):
        #     print(len(self.daily_x[i]))
        # print(self.player_x)

    def countEachRole(self,index):
        agent = self.diff_data["agent"][index]-1
        role = self.diff_data["text"][index].split(' ')[2]
        self.player_t[index] = self.role_to_num[role]
        # self.num_to_role = {0:}
        if(role == "WEREWOLF"):
            self.daily_t[index] = 1
        # print(self.diff_data)
        # print(self.player_t)


    def plot_accu_loss(self):
        fig = plt.figure(figsize=(12.0,8.0))
        graph_name = "agent_"+str(self.agent_num)+"_"
        graph_name += "train_" if self.daily_train == True else "test_"
        graph_name += str(self.train_times)+"_"
        graph_name += "each_model" if self.each_model == True else "one_model"
        fig.suptitle('ookawa_'+graph_name, fontsize=20)
        daily_loss = fig.add_subplot(2,2,1)
        plt.title("daily_loss")
        daily_accu = fig.add_subplot(2,2,2)
        plt.title("daily_accuracy")
        player_loss = fig.add_subplot(2,2,3)
        plt.title("player_loss")
        player_accu = fig.add_subplot(2,2,4)
        plt.title("player_accuracy")
        for i in range(len(self.daily_net)):
            if 0 < len(self.daily_net[i].memory.loss_memory):
                daily_loss.plot(self.daily_net[i].memory.loss_memory,label="day"+str(i))
                plt.legend()

            if 0 < len(self.daily_net[i].memory.accuracy_memory):
                daily_accu.plot(self.daily_net[i].memory.accuracy_memory,label="day"+str(i))
                plt.legend()

        for i in range(len(self.player_net)):
            if 0 < len(self.player_net[i].memory.accuracy_memory):
                player_loss.plot(self.player_net[i].memory.loss_memory,label="day"+str(i))
                plt.legend()

            if 0 < len(self.player_net[i].memory.accuracy_memory):
                player_accu.plot(self.player_net[i].memory.accuracy_memory,label="day"+str(i))
                plt.legend()
        # plt.show()
        os.makedirs("graph_folder", exist_ok=True)
        plt.savefig("graph_folder/"+graph_name+'.png')

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
        print("player accuracy:{:.2f}".format(sum_correct_pred/sum_role_pred))

        for i in range(len(self.daily_net)):
            if 0 < len(self.daily_net[i].memory.accuracy_memory):
                print(len(self.player_net[i].memory.accuracy_memory))
                print("day{:<2}  daily_accuracy:{:<.2f}  player_accuracy:{:<.2f}".format(i,np.mean(self.daily_net[i].memory.accuracy_memory),np.mean(self.player_net[i].memory.accuracy_memory)))
        
        print(self.using_alive_info_cnt_daily, self.using_alive_info_cnt_player)


    def update_predict_result(self,y,t):
        for y_role, t_role in zip(y,t):
            self.predict_cnt[self.num_to_role[y_role]] += 1
            self.role_cnt[self.num_to_role[t_role]] += 1
            if y_role == t_role:
                self.correct_predict_cnt[self.num_to_role[t_role]]+=1

        for agent,y_role in enumerate(y):
            if self.num_to_role[y_role] in self.werewolf_list:
                if self.alive_list[agent][self.alive_to_num["dead"]] != 1:
                    self.using_alive_info_cnt_player += 1

    def daily_model_train(self):
        for i in range(len(self.daily_net)):
            if 10 < len(self.daily_net[i].memory):
                loss, accuracy = self.daily_net[i].train()

                self.daily_net[i].memory.addLossAccuracy(loss,accuracy)


    def daily_model_eval(self):
        if self.each_model == True:
            for i in range(1,len(self.daily_x)):
                loss, accuracy = self.daily_net[i].eval(np.array(self.daily_x[i]).reshape(1,-1).astype(np.float32), np.array(self.daily_t).reshape(1,-1).astype(np.int32))
                self.daily_net[i].memory.addLossAccuracy(loss,accuracy)

        else:
            for i in range(1,len(self.daily_x)):
                loss, accuracy = self.daily_net[0].eval(np.array(self.daily_x[i]).reshape(1,-1).astype(np.float32), np.array(self.daily_t).reshape(1,-1).astype(np.int32))
                self.daily_net[i].memory.addLossAccuracy(loss,accuracy)


    def player_model_train(self):
        for i in range(len(self.player_net)):
            if 10 < len(self.player_net[i].memory):
                loss, accuracy = self.player_net[i].train()
                self.player_net[i].memory.addLossAccuracy(loss,accuracy)

    def player_model_eval(self):

        if self.each_model == True:
            for i in range(len(self.daily_x)):
                if 0 < len(self.daily_x[i]):
                    loss, accuracy, player_y = self.player_net[i].eval(np.array(self.player_x[i]).astype(np.float32), np.array(self.player_t).astype(np.int32))
                    self.update_predict_result(player_y, np.array(self.player_t).astype(np.int32))
                    self.player_net[i].memory.addLossAccuracy(loss,accuracy)

        else:
            for i in range(len(self.player_x)):
                if 0 < len(self.player_x[i]):
                    loss, accuracy, player_y = self.player_net[0].eval(np.array(self.player_x[i]).astype(np.float32), np.array(self.player_t).astype(np.int32))
                    self.update_predict_result(player_y, np.array(self.player_t).astype(np.int32))
                    self.player_net[i].memory.addLossAccuracy(loss,accuracy)


    def save_each_model(self):
        for i in range(len(self.daily_net)):
            daily_path = './net_folder/daily_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/'
            file_name = 'ookawa_train_daily_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_'+str(self.train_cnt)+'.net'
            os.makedirs(daily_path, exist_ok=True)
            chainer.serializers.save_npz(daily_path+file_name, self.daily_net[i].net)

        for i in range(len(self.player_net)):
            player_path = './net_folder/player_model/agent'+str(self.agent_num)+'/each_model/day_'+str(i)+'/'
            file_name = 'ookawa_train_player_num_'+str(self.agent_num)+'_day_'+str(i)+'_train_'+str(self.train_cnt)+'.net'
            os.makedirs(player_path, exist_ok=True)
            chainer.serializers.save_npz(player_path+file_name, self.player_net[i].net)

    def save_one_model(self):
        daily_path = './net_folder/daily_model/agent'+str(self.agent_num)+'/one_model/'
        os.makedirs(daily_path,exist_ok=True)
        file_path = 'ookawa_train_daily_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        chainer.serializers.save_npz(daily_path+file_path, self.daily_net[0].net)
        player_path = './net_folder/player_model/agent'+str(self.agent_num)+'/one_model/'
        os.makedirs(player_path,exist_ok=True)
        file_path = 'ookawa_train_player_num_'+str(self.agent_num)+'_train_'+str(self.train_cnt)+'.net'
        chainer.serializers.save_npz(player_path+file_path, self.player_net[0].net)

    def addVectorToEachModel(self):
        if self.daily_train == True:
            for i in range(1,len(self.daily_x)):
                self.daily_net[i].addVector(self.daily_x[i],self.daily_t)
        if self.player_train == True:
            for i in range(len(self.player_x)):
                for j in range(len(self.player_x[i])):
                    self.player_net[i].addVector(self.player_x[i][j],[self.player_t[j]])

    def addVectorToOneModel(self):
        if self.daily_train == True:
            for i in range(1,len(self.daily_x)):
                self.daily_net[0].addVector(self.daily_x[i],self.daily_t)
        if self.player_train == True:
            for i in range(len(self.player_x)):
                for j in range(len(self.player_x[i])):
                    self.player_net[0].addVector(self.player_x[i][j],[self.player_t[j]])

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
        if self.each_model == True:
            self.addVectorToEachModel()
        else:
            self.addVectorToOneModel()


        if self.daily_train == True:
            self.daily_model_train()
        else:
            self.daily_model_eval()


        if self.player_train == True:
            self.player_model_train()
        else:
            self.player_model_eval()

        if(self.train_cnt == self.train_times):
            if self.player_train == False:
                self.display_game_result()
            else:
                self.save_one_model()
                self.save_each_model()

            self.plot_accu_loss()

        self.train_cnt += 1

        if self.train_cnt%(self.train_times//10) == 0:
            print(self.train_cnt)
        #     if self.each_model == True:
        #         self.save_each_model()
        #     else:
        #         self.save_one_model()


class MLP(chainer.Chain):
        def __init__(self,n_input,n_hidden, n_output):
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

class Memory():
    def __init__(self):
        self.x_memory = []
        self.t_memory = []
        self.loss_memory = []
        self.accuracy_memory = []
        self.memory_max = 1000000000
        self.index = 0

    def append(self,x,t):
        self.x_memory.append(x)
        self.t_memory.append(t)

    def addLossAccuracy(self,loss,accuracy):
        if len(self.loss_memory) == 0:
            self.loss_memory.append(loss)
        else:
            self.loss_memory.append((self.loss_memory[-1]*len(self.loss_memory)+loss)/(len(self.loss_memory)+1))
        if len(self.accuracy_memory) == 0:
            self.accuracy_memory.append(accuracy)
        else:
            self.accuracy_memory.append((self.accuracy_memory[-1]*len(self.accuracy_memory)+accuracy)/(len(self.accuracy_memory)+1))

    def __len__(self):
        return len(self.x_memory)

    def choice(self,n):
        choice = np.random.choice(len(self.x_memory),n)
        x = np.array(self.x_memory)[choice,:].astype(np.float32)
        t = np.array(self.t_memory)[choice,:].astype(np.int32)
        return x,t

class predict_werewolf():

    def __init__(self,n_input,n_hidden,n_output):
        self.net = MLP(n_input=n_input,n_hidden=n_hidden,n_output=n_output)
        self.optimizer = chainer.optimizers.Adam()
        self.optimizer.setup(self.net)
        self.memory = Memory()

    def train(self):
        x,t = self.memory.choice(10)
        with chainer.using_config("train", True), chainer.using_config("enable_backprop", True):
            y = self.net(x)
            loss = F.sigmoid_cross_entropy(y,t)
            y = y.array
            y = np.array(np.argsort(np.argsort(-y)) < np.count_nonzero(t[0]))
            accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)
            self.net.cleargrads()
            loss.backward()
            self.optimizer.update()
    
            return loss.array, accuracy

    def eval(self,x,t):
        with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
            y = self.net(x)
            loss = F.sigmoid_cross_entropy(y,t)
            y = y.array
            y = np.array(np.argsort(np.argsort(-y)) < np.count_nonzero(t[0]))
            accuracy = np.count_nonzero(np.logical_and(y,t))/np.count_nonzero(t)

        return loss.array, accuracy
    
    def addVector(self, x, t):
        self.memory.append(x,t)

class predict_role():

    def __init__(self,n_input,n_hidden,n_output):
        self.net = MLP(n_input=n_input,n_hidden=n_hidden,n_output=n_output)
        self.optimizer = chainer.optimizers.Adam()
        self.optimizer.setup(self.net)
        self.memory = Memory()

    def train(self):
        x,t = self.memory.choice(10)
        t = t.reshape(-1)
        with chainer.using_config("train", True), chainer.using_config("enable_backprop", True):
            y = self.net(x)
            loss = F.softmax_cross_entropy(y,t)
            accuracy = F.accuracy(y,t)
            self.net.cleargrads()
            loss.backward()
            self.optimizer.update()
    
            return loss.array, accuracy.array

    def eval(self,x,t):
        with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
            y = self.net(x)
            loss = F.softmax_cross_entropy(y,t)
            accuracy = F.accuracy(y,t)
            y = y.array
            y = np.argmax(y,axis=1)
        return loss.array, accuracy.array, y
        
    def addVector(self, x, t):
        self.memory.append(x,t)
