from __future__ import print_function, division
from collections import defaultdict
import re
import numpy as np
from .topic_rate import TopicRate

class PlayerPredictor(object):

    def __init__(self):

        self.rate = TopicRate()

        self.players = [ 'AITKN', 'aplain', 'carlo', 'cash', 'cndl', 'colun', 'daisyo', 'hibagon', 'johnny', 'JuN1Ro', 'kasuka', 'm_cre', 'takkyubu', 'Udon', 'wasabi', 'army', 'noisey', 'eSteen', 'cm11_ike', 'glycine', 'flex', 'RyuZyu', 'megumish', 'Tanion', 'cbc', 'haru-e60', 'ichimi', 'sonoda', 'Litt1eGirl', 'Sanzu', 'TOT', 'ninini', 'kuboon', 'OLab', 'jedipuppy', 'remcr', 'tdr31', 'Waffle', 'Noppo', 'tsukammo', 'YkUecWolf', 'tori' ]

        self.power = np.ones(len(self.players))
        self.power[self.name_to_idx('daisyo')] = 0
        for idx in range(15, len(self.players)):
            self.power[idx] = 0.5

        # ranking kara
        self.power[self.name_to_idx('cndl')] = 2.5
        self.power[self.name_to_idx('Udon')] = 2.3
        self.power[self.name_to_idx('wasabi')] = 2.0
        self.power[self.name_to_idx('kasuka')] = 2.0
        self.power[self.name_to_idx('m_cre')] = 1.9
        self.power[self.name_to_idx('JuN1Ro')] = 1.5
        self.power[self.name_to_idx('cash')] = 1.3
        self.power[self.name_to_idx('calro')] = 1.2

        self.dict_5 = { 'COMINGOUT WEREWOLF' : 0, 'COMINGOUT POSSESSED' : 1, 'COMINGOUT SEER' : 2, 'COMINGOUT VILLAGER' : 3, 'ESTIMATE WEREWOLF' : 4, 'ESTIMATE POSSESSED' : 5, 'ESTIMATE SEER' : 6, 'ESTIMATE VILLAGER' : 7, 'DIVINATION' : 8, 'DIVINED HUMAN' : 9, 'DIVINED WEREWOLF' : 10, 'VOTE' : 11, 'AGREE' : 12, 'DISAGREE' : 13, 'Skip' : 14, 'Over' : 15, 'REQUEST VOTE' : 16, 'REQUEST A VOTE' : 17, 'REQUEST DIVINATION' : 18, 'REQUEST A DIVINATION' : 19, 'REQUEST A Over' : 20, 'REQUEST Over' : 21 }

        self.dict_5_list = [ 'COMINGOUT WEREWOLF', 'COMINGOUT POSSESSED', 'COMINGOUT SEER', 'COMINGOUT VILLAGER', 'ESTIMATE WEREWOLF' , 'ESTIMATE POSSESSED', 'ESTIMATE SEER', 'ESTIMATE VILLAGER', 'DIVINATION', 'DIVINED HUMAN', 'DIVINED WEREWOLF', 'VOTE', 'AGREE', 'DISAGREE', 'Skip', 'Over', 'REQUEST VOTE', 'REQUEST A VOTE', 'REQUEST DIVINATION', 'REQUEST A DIVINATION', 'REQUEST A Over', 'REQUEST Over' ]


        self.role_5 = [ 'WEREWOLF', 'POSSESSED', 'SEER', 'VILLAGER' ]

        self.dict_15 = { 'COMINGOUT WEREWOLF' : 0, 'COMINGOUT POSSESSED' : 1, 'COMINGOUT SEER' : 2, 'COMINGOUT MEDIUM' : 3, 'COMINGOUT BODYGUARD' : 4, 'COMINGOUT VILLAGER' : 5, 'ESTIMATE WEREWOLF' : 6, 'ESTIMATE POSSESSED' : 7, 'ESTIMATE SEER' : 8, 'ESTIMATE MEDIUM' : 9, 'ESTIMATE BODYGUARD' : 10, 'ESTIMATE VILLAGER' : 11, 'DIVINATION' : 12, 'DIVINED HUMAN' : 13, 'DIVINED WEREWOLF' : 14, 'IDENTIFIED HUMAN' : 15, 'IDENTIFIED WEREWOLF' : 16, 'GUARD' : 17, 'GUARDED' : 18, 'VOTE' : 19, 'AGREE' : 20, 'DISAGREE' : 21, 'Skip' : 22, 'Over' : 23, 'REQUEST VOTE' : 24, 'REQUEST A VOTE' : 25, 'REQUEST DIVINATION' : 26, 'REQUEST A DIVINATION' : 27, 'REQUEST A Over' : 28, 'REQUEST Over' : 29, 'ATTACK' : 30, 'REQUEST ATTACK' : 31, 'REQUEST A ATTACK' : 32 }

        self.dict_15_list = [ 'COMINGOUT WEREWOLF', 'COMINGOUT POSSESSED', 'COMINGOUT SEER', 'COMINGOUT MEDIUM', 'COMINGOUT BODYGUARD', 'COMINGOUT VILLAGER', 'ESTIMATE WEREWOLF', 'ESTIMATE POSSESSED', 'ESTIMATE SEER', 'ESTIMATE MEDIUM', 'ESTIMATE BODYGUARD', 'ESTIMATE VILLAGER', 'DIVINATION', 'DIVINED HUMAN', 'DIVINED WEREWOLF', 'IDENTIFIED HUMAN', 'IDENTIFIED WEREWOLF', 'GUARD', 'GUARDED', 'VOTE', 'AGREE', 'DISAGREE', 'Skip', 'Over', 'REQUEST VOTE', 'REQUEST A VOTE', 'REQUEST DIVINATION', 'REQUEST A DIVINATION', 'REQUEST A Over', 'REQUEST Over', 'ATTACK', 'REQUEST ATTACK', 'REQUEST A ATTACK' ]

        self.role_15 = [ 'WEREWOLF', 'POSSESSED', 'SEER', 'VILLAGER', 'MEDIUM', 'BODYGUARD' ]

        self.is_start = False

    def initialize(self, base_info, game_setting):

        self.role_map = game_setting['roleNumMap']

        if (game_setting['playerNum'] == 5):
            self.roles = np.zeros((6, len(self.role_5)))
        else:
            self.roles = np.zeros((16, len(self.role_15)))



        # if (game_setting['playerNum'] == 15 and base_info['myRole'] == 'WEREWOLF'):
        #     for idx in range(1, game_setting['playerNum'] + 1):
        #         if (str(i) in self.base_info['roleMap'].keys()):


        if (self.is_start == False):
            self.player_num = game_setting['playerNum']
            self.name_pred = np.ones((self.player_num+1, len(self.players)))
            self.is_start = True

    def finish(self, base_info, talks, roles, game_count, whisper=[]):

        if (self.player_num == 5):

            # first
            for idx in range(1, 6):

                if (base_info['agentIdx'] == idx):
                    continue

                #talk = re.split(r'\s|"|,|\.|\n|\(|\)', talks[game_count][idx][0])
                #print (talks[game_count][idx])
                # first
                for name in self.players:
                    if (self.rate.first_rate_5[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][0]))] <= 0.0):
                        self.name_pred[idx][self.name_to_idx(name)] -= 10000
                    else:
                        self.name_pred[idx][self.name_to_idx(name)] += self.rate.first_rate_5[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][0]))]

                # second
                for name in self.players:
                    if (self.rate.second_5[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][1]))] <= 0.0):
                        self.name_pred[idx][self.name_to_idx(name)] -= 10000
                    else:
                        self.name_pred[idx][self.name_to_idx(name)] += self.rate.second_5[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][1]))]

            for idx in range(1, 6):
                for name in self.players:
                    for talk in talks[game_count][idx]:
                        if (self.rate.topic_rate_5[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talk))] <= 0.0):
                            self.name_pred[idx][self.name_to_idx(name)] -= 10000
                        else:
                            self.name_pred[idx][self.name_to_idx(name)] += self.rate.topic_rate_5[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talk))]


            # DEBUG
            # for idx in range(1, 6):
            #     if (base_info['agentIdx'] == idx):
            #         continue
            #     print (self.players[self.name_pred[idx].argmax()],', ', end='')
            #     # for n in self.name_pred[idx]:
            #     #     print (n, ' / ', end='')
            #     #print ('')
            #     #print ('')
            # print ('')

        else:

            # whisper
            if (base_info['myRole'] == 'WEREWOLF' and len(whisper) > 0):

                for idx in range(1, 16):
                    if (base_info['agentIdx'] == idx):
                        continue;

                    for name in self.players:
                        if (len(whisper[idx]) <= 0):
                            continue
                        if (self.rate.first_whisper[self.name_to_idx(name)][self.role_to_idx_5('WEREWOLF')][self.topic_to_idx(self.to_topic(whisper[idx][0]))] <= 0.0):
                            self.name_pred[idx][self.name_to_idx(name)] -= 10000
                        else:
                            self.name_pred[idx][self.name_to_idx(name)] += self.rate.first_whisper[self.name_to_idx(name)][self.role_to_idx_5('WEREWOLF')][self.topic_to_idx(self.to_topic(whisper[idx][0]))]

                    # print (whisper[idx][0])

                for idx in range(1, 16):
                    for name in self.players:
                        for w in whisper[idx]:
                            if (self.rate.whisper_rate[self.name_to_idx(name)][self.role_to_idx_5('WEREWOLF')][self.topic_to_idx(self.to_topic(w))] == 0.0):
                                self.name_pred[idx][self.name_to_idx(name)] -= 10000
                            else:
                                self.name_pred[idx][self.name_to_idx(name)] += self.rate.whisper_rate[self.name_to_idx(name)][self.role_to_idx_5('WEREWOLF')][self.topic_to_idx(self.to_topic(w))]



            # first
            for idx in range(1, 16):

                if (base_info['agentIdx'] == idx):
                    continue

                for name in self.players:
                    if (self.rate.first_rate_15[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][0]))] <= 0.0):
                        self.name_pred[idx][self.name_to_idx(name)] -= 10000
                    else:
                        self.name_pred[idx][self.name_to_idx(name)] += self.rate.first_rate_15[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][0]))]

            # second
            for idx in range(1, 16):

                if (base_info['agentIdx'] == idx):
                    continue

                for name in self.players:
                    if (self.rate.second_15[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][1]))] <= 0.0):
                        self.name_pred[idx][self.name_to_idx(name)] -= 10000
                    else:
                        self.name_pred[idx][self.name_to_idx(name)] += self.rate.second_15[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talks[game_count][idx][1]))]


            for idx in range(1, 16):
                for name in self.players:
                    for talk in talks[game_count][idx]:
                        if (self.rate.topic_rate_15[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talk))] == 0.0):
                            self.name_pred[idx][self.name_to_idx(name)] -= 10000
                        else:
                            self.name_pred[idx][self.name_to_idx(name)] += self.rate.topic_rate_15[self.name_to_idx(name)][self.role_to_idx_5(roles[idx])][self.topic_to_idx(self.to_topic(talk))]


            # DEBUG
            # for idx in range(1, 16):
            #     if (base_info['agentIdx'] == idx):
            #         continue
            #     print (self.players[self.name_pred[idx].argmax()], ', ', end='')
            #     # for n in self.name_pred[idx]:
            #     #     print (n, ' / ', end='')
            #     #print ('')
            #     #print('')
            # print ('')

    def name_to_idx(self, name):
        try:
            return self.players.index(name)
        except:
            return self.players.index('daisyo')

    def role_to_idx_5(self, role):
        if (self.player_num == 5):
            try:
                return self.role_5.index(role)
            except:
                return self.role_5['VILLAGER']
        else:
            try:
                return self.role_15.index(role)
            except:
                return self.role_15['VILLAGER']

    def to_topic(self, t_str):

        talk = re.split(r'\s|"|,|\.|\n|\(|\)', t_str)

        if (self.player_num == 5):
            if (talk[0] == 'REQUEST'):
                pre = re.split(r'\s|"|,|\.|\n|\(|\)|\[|\]', talk[1])
                t2 = ' '
                if (pre[0] == 'Agent'):
                    t2 = ' A ' + talk[2]
                else:
                    t2 = ' ' + talk[1]
                return talk[0] + t2
            if (talk[0] == 'ESTIMATE' or talk[0] == 'COMINGOUT' or talk[0] == 'DIVINED'):
                return talk[0] + ' ' + talk[2]
            else:
                return talk[0]
        else:

            if (talk[0] == 'REQUEST'):
                try:
                    pre = re.split(r'\s|"|,|\.|\n|\(|\)|\[|\]', talk[1])
                except:
                    return 'Skip'
                t2 = ' '
                if (pre[0] == 'Agent'):
                    t2 = ' A ' + talk[2]
                else:
                    t2 = ' ' + talk[1]
                return talk[0] + t2
            if (talk[0] == 'ESTIMATE' or talk[0] == 'COMINGOUT' or talk[0] == 'DIVINED' or talk[0] == 'IDENTIFIED'):
                return talk[0] + ' ' + talk[2]
            else:
                return talk[0]


        return 'Skip'

    def topic_to_idx(self, topic):
        if (self.player_num == 5):
            try:
                return self.dict_5[topic]
            except:
                return self.dict_5['Skip']
        else:
            try:
                return self.dict_15[topic]
            except:
                return self.dict_15['Skip']

    def update(self, base_info, talk, talk_turn):

        if (self.player_num == 5 and len(talk) == 0):
            return self.roles
        elif (len(talk) == 0):
            return self.roles

        if (self.player_num == 5):

            for idx in range(1, 6):
                if (base_info['agentIdx'] == idx):
                    continue

                name_idx = self.get_estimate_name_idx(idx)

                for role in self.role_5:
                    for t in talk[idx]:

                        if (base_info['day'] != 1 and talk_turn != 0):
                            if (self.rate.topic_rate_5[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))] <= 0.0):
                                self.roles[idx][self.role_to_idx_5(role)] = -10000
                            else:
                                self.roles[idx][self.role_to_idx_5(role)] += self.rate.topic_rate_5[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))]
                        elif (base_info['day'] == 1 and talk_turn == 0):
                            if (self.rate.first_rate_5[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))] <= 0.0):
                                self.roles[idx][self.role_to_idx_5(role)] = -10000
                            else:
                                self.roles[idx][self.role_to_idx_5(role)] += self.rate.first_rate_5[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))]
                        elif (talk_turn == 1):
                            if (self.rate.second_5[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))] <= 0.0):
                                self.roles[idx][self.role_to_idx_5(role)] = -10000
                            else:
                                self.roles[idx][self.role_to_idx_5(role)] += self.rate.second_5[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))]
        else:
            #roles = np.zeros((self.player_num+1, len(self.role_15)))

            for idx in range(1, 16):

                if (base_info['agentIdx'] == idx):
                    continue

                name_idx = self.get_estimate_name_idx(idx)

                for role in self.role_15:
                    for t in talk[idx]:

                        if (base_info['day'] != 1 and talk_turn != 0):

                            if (self.rate.topic_rate_15[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))] <= 0.0):
                                self.roles[idx][self.role_to_idx_5(role)] -= 10000
                            else:
                                self.roles[idx][self.role_to_idx_5(role)] += self.rate.topic_rate_15[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))]

                        elif (base_info['day'] == 1 and talk_turn == 0):

                            if (self.rate.first_rate_15[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))] <= 0.0):
                                self.roles[idx][self.role_to_idx_5(role)] -= 10000
                            else:
                                self.roles[idx][self.role_to_idx_5(role)] += self.rate.first_rate_15[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))]
                        elif (talk_turn == 1):
                            if (self.rate.second_15[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))] <= 0.0):
                                self.roles[idx][self.role_to_idx_5(role)] -= 10000
                            else:
                                self.roles[idx][self.role_to_idx_5(role)] += self.rate.second_15[name_idx][self.role_to_idx_5(role)][self.topic_to_idx(self.to_topic(t))]


        return self.roles

    def get_pred_assign_roles(self, base_info):

        role_num = self.role_map

        #
        pred_role_map = {}
        for idx in range(self.player_num + 1):
            if str(idx) in base_info['roleMap'].keys():
                pred_role_map[idx] = base_info['roleMap'][str(idx)]
                role_num[base_info['roleMap'][str(idx)]] -= 1
            else:
                pred_role_map[idx] = ''

        if self.player_num == 5:
            for role in self.role_5:
                if role_num[role] > 0:
                    num = role_num[role]

                    for n in range(num):

                        p = -1000000
                        i = 1

                        for idx in range(1, 6):

                            if pred_role_map[idx] != '':
                                continue

                            if p < self.roles[idx][self.role_to_idx_5(role)]:
                                p = self.roles[idx][self.role_to_idx_5(role)]
                                i = idx

                        pred_role_map[i] = role
        else:
            for role in self.role_15:
                if role_num[role] > 0:
                    num = role_num[role]

                    for n in range(num):

                        p = -1000000
                        i = 1

                        for idx in range(1, 16):

                            if pred_role_map[idx] != '':
                                continue

                            if p < self.roles[idx][self.role_to_idx_5(role)]:
                                p = self.roles[idx][self.role_to_idx_5(role)]
                                i = idx

                        pred_role_map[i] = role

        return pred_role_map

    def get_pred_assign_names(self, base_info):

        name_map = {}
        for idx in range(self.player_num + 1):
            if base_info['agentIdx'] == idx:
                name_map[idx] = 'daisyo'
            else:
                name_map[idx] = ''

        user_flag = np.zeros(len(self.players))

        # self.players[self.name_pred[idx].argmax()], ', ', end='')

        user_flag[self.name_to_idx('daisyo')] = 1

        for idx in range(1, self.player_num + 1):
            p = -1000000
            nm = 'daisyo'
            if base_info['agentIdx'] == idx:
                continue
            for name in self.players:
                if p < self.name_pred[idx][self.name_to_idx(name)] and user_flag[self.name_to_idx(name)] == 0:
                    p = self.name_pred[idx][self.name_to_idx(name)]
                    nm = name
            user_flag[self.name_to_idx(nm)] = 1
            name_map[idx] = nm

        return name_map

    def get_estimate_name_idx(self, idx):
        return self.name_pred[idx].argmax()
