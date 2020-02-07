#!/usr/bin/env python
from __future__ import print_function, division

from collections import Counter
import re

import numpy as np

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb

import aiwolfpy.daisyo

import python_sample

myname = 'daisyo'

class SampleAgent(object):

    def __init__(self, agent_name):
        # myname
        self.myname = agent_name

        #
        self.predictor_p = aiwolfpy.daisyo.PlayerPredictor()

        # 100 games log
        self.talks = {}
        for game_count in range(1002):
            self.talks[game_count] = {}
            for i in range(17):
                self.talks[game_count][i] = []

        self.game_count = 0

        # debug
        self.win_num = 0

        self.ps = python_sample.PythonPlayer(self.myname)
        self.ps_num = 30
        #self.ps.init(self.myname)



    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):

        if self.game_count <= self.ps_num:
            self.ps.initialize(base_info, diff_data, game_setting)
            # return

        #
        self.base_info = base_info
        self.game_setting = game_setting

        # initialize
        self.predictor_p.initialize(base_info, game_setting)

        #
        self.game_count += 1

        self.divined_list = []
        self.comingout = ''
        self.myresult = ''
        self.not_reported = False
        self.vote_declare = 0

        self.attack_declare = 0
        self.is_finish = False

        self.is_true_comingout = False

        self.is_power_play = False
        self.is_power_play_reported = False
        self.is_werewolf_co = False
        self.pp_comingout = ''

        self.whispers = {}
        self.day_talks = {}
        self.co_map = {}
        self.vote_map = {}
        for i in range(self.game_setting['playerNum'] + 1):
            self.whispers[i] = []
            self.day_talks[i] = []
            self.co_map[i] = ''
            self.vote_map[i] = ''

        #
        self.pred_roles = self.predictor_p.update(base_info, [], 0)

        # debug
        self.is_win = 0

    def update(self, base_info, diff_data, request):

        if self.game_count <= self.ps_num:
            self.ps.update(base_info, diff_data, request)
            #return

        self.base_info = base_info

        # Before day_start()
        if request == 'DAILY_INITIALIZE':
            for i in range(diff_data.shape[0]):

                d_type = diff_data['type'][i]

                # IDENTIFY OR DIVINE OR GUARD
                if d_type == 'identify' or d_type == 'divine' or d_type == 'guard':
                    self.not_reported = True
                    self.myresult = diff_data['text'][i]

            # POSSESSED
            if self.base_info['myRole'] == 'POSSESSED':
                self.not_reported = True


            # CHECK PP
            alive_num = 0
            for i in range(1, self.game_setting['playerNum'] + 1):
                if self.base_info['statusMap'][str(i)] == 'ALIVE':
                    alive_num += 1

            if alive_num == 3:
                is_power_play = True

        # Before talk()
        elif request == 'TALK':

            for i in range(diff_data.shape[0]):

                # SKIP My TALK
                if self.base_info['agentIdx'] == diff_data['agent'][i]:
                    continue

                # UPDATE TALKS LOG
                self.day_talks[diff_data['agent'][i]].append(diff_data['text'][i])
                self.talks[self.game_count][diff_data['agent'][i]].append(diff_data['text'][i])

                # UPDATE MAP
                text = re.split(r'\s|"|,|\.|\n|\(|\)', diff_data['text'][i])
                if text[0] == 'VOTE':
                    self.vote_map[diff_data['agent'][i]] = text[1]
                elif text[0] == 'COMINGOUT':
                    self.co_map[diff_data['agent'][i]] = text[2]

                # VOTE COUNTER
                agent_counter = Counter()
                for k, v in self.vote_map.items():
                    if k == 0:
                        continue
                    v = re.split(r'\s|"|,|\.|\n|\(|\)', v)
                    cnt = Counter(v)
                    agent_counter += cnt

                alive_num = 0
                for i in range(1, self.game_setting['playerNum']+1):
                    if self.base_info['statusMap'][str(i)] == 'ALIVE':
                        alive_num += 1

                for word, cnt in agent_counter.most_common():
                    if cnt > (alive_num-1)/2 and word == 'Agent[' + "{0:02d}".format(self.base_info['agentIdx']) + ']':
                        self.is_true_comingout = True

            # UPDATE ROLE PREDICTOR
            #print (len(self.day_talks))
            self.pred_roles = self.predictor_p.update(base_info, self.day_talks, self.talk_turn)

        # Before whisper()
        elif request == 'WHISPER':

            for i in range(diff_data.shape[0]):

                if self.base_info['agentIdx'] == diff_data['agent'][i]:
                    continue

                # UPDATE WHISPER LOG
                self.whispers[diff_data['agent'][i]].append(diff_data['text'][i])

        # Before vote()
        elif request == 'VOTE':

            # DEBUG
            # for idx in range(1, self.game_setting['playerNum'] + 1):
            #     print ('idx=', idx, ': ', self.predictor_p.role_15[self.pred_roles[idx].argmax()], ' / ', end='')
            # print ('')

            # VOTE COUNTER
            agent_counter = Counter()
            for k, v in self.vote_map.items():
                if k == 0:
                    continue;
                v = re.split(r'\s|"|,|\.|\n|\(|\)', v)
                cnt = Counter(v)
                agent_counter += cnt


            alive_num = 0
            for i in range(1, self.game_setting['playerNum']+1):
                if self.base_info['statusMap'][str(i)] == 'ALIVE':
                    alive_num += 1

            try:
                agent = str('Agent[' + "{0:02d}".format(self.vote()) + ']')
                agent = re.split(r'\s|"|,|\.|\n|\(|\)', agent)
                #agent_counter += Counter(agent)


                for word, cnt in agent_counter.most_common():
                    if word != '' and cnt > (alive_num-1) / 2 and agent != word:
                        self.is_negative_vote = 1
                        self.negative_vote_declare = word
                        break

            except:
                print ('error')

        # Before finish()
        elif request == 'FINISH':

            # TRUE ROLES
            self.roles = []
            for i in range(17):
                self.roles.append('')

            aiwolf_list = []
            for i in range(diff_data.shape[0]):
                text = re.split(r'\s|"|,|\.|\n|\(|\)', diff_data['text'][i])
                self.roles[diff_data['agent'][i]] = text[2]
                if text[2] == 'WEREWOLF':
                    aiwolf_list.append(diff_data['agent'][i])

            ww = 0
            for i in range(1, self.game_setting['playerNum'] + 1):
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and aiwolf_list.count(i) > 0:
                    ww = 1

            if ((self.base_info['myRole'] == 'WEREWOLF' or self.base_info['myRole'] == 'POSSESSED') and ww == 1) or ((self.base_info['myRole'] != 'WEREWOLF' and self.base_info['myRole'] != 'POSSESSED') and ww == 0):
                self.is_win = 1



    def dayStart(self):

        if self.game_count <= self.ps_num:
            self.ps.dayStart()

        # RESET
        self.vote_declare = 0
        self.attack_declare = 0
        self.talk_turn = 0
        self.is_negative_vote = 0
        self.negative_vote_declare = 0

        self.estimate_list = []

        for i in range(self.game_setting['playerNum'] + 1):
            self.vote_map[i] = ''

        return None

    def talk(self):

        self.talk_turn += 1

        if self.game_count <= self.ps_num:
            return self.ps.talk()


        myrole = self.base_info['myRole']
        agentidx = self.base_info['agentIdx']

        if self.is_true_comingout == True and self.comingout == '':
            if self.base_info['myRole'] != 'WEREWOLF' and self.base_info['myRole'] != 'POSSESSED':
                self.comingout = self.base_info['myRole']
                return cb.comingout(self.base_info['agentIdx'], self.comingout)
            else:
                self.comingout = 'VILLAGER'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)

        if self.is_power_play == True:

            # POSSESSED
            if self.pp_comingout == '' and self.base_info['myRole'] == 'POSSESSED':
                self.pp_comingout = 'POSSESSED'
                return cb.comingout(self.base_info['agentIdx'], self.pp_comingout)



            if self.talk_turn >= 5 and self.base_info['myRole'] == 'WEREWOLF':

                possessed_num = 0
                possessed_idx = -1
                for k, v in self.co_map.items():

                    if k == self.base_info['agentIdx']:
                        continue

                    if v == 'POSSESSED':
                        possessed_num += 1
                        possessed_idx = k

                if possessed_num == 1 and self.pp_comingout == '':
                    self.pp_comingout = 'WEREWOLF'
                    return cb.comingout(self.base_info['agentIdx'], self.pp_comingout)

            if self.base_info['myRole'] != 'WEREWOLF' and self.base_info['myRole'] != 'POSSESSED':

                werewolf_num = 0
                possessed_num = 0
                for k, v in self.co_map.items():

                    if k == self.base_info['agentIdx']:
                        continue

                    if v == 'POSSESSED':
                        possessed_num += 1
                    elif v == 'WEREWOLF':
                        werewolf_num += 1

                    if possessed_num == 1:
                        self.pp_comingout = 'WEREWOLF'
                        return cb.comingout(self.base_info['agentIdx'], self.pp_comingout)


        if self.game_setting['playerNum'] == 15:

            # 1.comingout anyway
            if myrole == 'SEER' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(agentidx, self.comingout)

            elif myrole == 'MEDIUM' and self.comingout == '':
                self.comingout = 'MEDIUM'
                return cb.comingout(agentidx, self.comingout)

            elif myrole == 'POSSESSED' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(agentidx, self.comingout)

            # 2.report
            if myrole == 'SEER' and self.not_reported:
                self.not_reported = False
                return self.myresult

            elif myrole == 'MEDIUM' and self.not_reported:
                self.not_reported = False
                return self.myresult

            elif myrole == 'POSSESSED' and self.comingout == 'SEER' and self.not_reported:
                self.not_reported = False

                # rnd = np.random.rand(1)

                name_map = self.predictor_p.get_pred_assign_names(self.base_info)
                role_map = self.predictor_p.get_pred_assign_roles(self.base_info)

                p = 0
                i = 1
                for idx in range(1, 16):
                    if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] and i not in self.divined_list and self.pred_roles[idx][0] > 0 and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                        p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])]
                        i = idx

                self.divined_list.append(i)

                if role_map[i] == 'WEREWOLF':
                    self.myresult = 'DIVINED Agent[' + "{0:02d}".format(i) + '] ' + 'HUMAN'
                else:
                    self.myresult = 'DIVINED Agent[' + "{0:02d}".format(i) + '] ' + 'WEREWOLF'

                return self.myresult

                # if rnd[0] <= 0.5:
                #
                #     p = -100000
                #     idx = 1
                #     for i in range(1, 16):
                #         p0 = self.pred_roles[i][0]
                #         if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and i not in self.divined_list and self.base_info['agentIdx'] != i:
                #             p = p0
                #             idx = i
                #         self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'
                #         self.divined_list.append(i)
                #         return self.myresult
                # else:
                #
                #     p = 100000
                #     idx = 1
                #     for i in range(1, 16):
                #         p0 = self.pred_roles[i][0]
                #         if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and i not in self.divined_list and self.base_info['agentIdx'] != i:
                #             p = p0
                #             idx = i
                #         self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'WEREWOLF'
                #         self.divined_list.append(i)
                #         return self.myresult

            # 3.declare vote if not yet
            if self.vote_declare != self.vote():
                self.vote_declare = self.vote()
                return cb.vote(self.vote_declare)

            # 4.Other
            if self.talk_turn <= 10:
                rnd = np.random.rand(1)

                if (rnd[0] <= 0.35):
                    p = -100000
                    idx = 1
                    if len(self.estimate_list) < 3:
                        for i in range(1, 16):
                            p0 = self.pred_roles[i][3]
                            if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and i not in self.estimate_list and self.base_info['statusMap'][str(i)] == 'ALIVE':
                                p = p0
                                idx = i
                        self.estimate_list.append(idx)
                        return cb.estimate(idx, 'VILLAGER')
                elif (rnd[0] <= 0.7):
                    return cb.skip()
                else:
                    return cb.over()
        else:

            # 1.
            if myrole == 'SEER' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(agentidx, self.comingout)

            elif myrole == 'MEDIUM' and self.comingout == '':
                self.comingout = 'MEDIUM'
                return cb.comingout(agentidx, self.comingout)

            elif myrole == 'POSSESSED' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(agentidx, self.comingout)

            # 2.
            if myrole == 'SEER' and self.not_reported:
                self.not_reported = False
                return self.myresult

            elif myrole == 'MEDIUM' and self.not_reported:
                self.not_reported = False
                return self.myresult

            elif myrole == 'POSSESSED' and self.comingout == 'SEER' and self.not_reported:
                self.not_reported = False

                #rnd = np.random.rand(1)

                name_map = self.predictor_p.get_pred_assign_names(self.base_info)
                role_map = self.predictor_p.get_pred_assign_roles(self.base_info)

                p = 0
                i = 1
                for idx in range(1, 6):
                    if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] and i not in self.divined_list and self.pred_roles[idx][0] > 0 and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                        p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])]
                        i = idx

                self.divined_list.append(i)

                if role_map[i] == 'WEREWOLF':
                    self.myresult = 'DIVINED Agent[' + "{0:02d}".format(i) + '] ' + 'HUMAN'
                else:
                    self.myresult = 'DIVINED Agent[' + "{0:02d}".format(i) + '] ' + 'WEREWOLF'

                return self.myresult

                # if rnd[0] <= 0.2:
                #
                #     p = -100000
                #     idx = 1
                #     for i in range(1, 6):
                #         p0 = self.pred_roles[i][0]
                #         if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and i not in self.divined_list and self.base_info['agentIdx'] != i:
                #             p = p0
                #             idx = i
                #         self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'
                #         self.divined_list.append(i)
                #         return self.myresult
                # else:
                #
                #     p = 100000
                #     idx = 1
                #     for i in range(1, 6):
                #         p0 = self.pred_roles[i][0]
                #         if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and i not in self.divined_list and self.base_info['agentIdx'] != i:
                #             p = p0
                #             idx = i
                #         self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'WEREWOLF'
                #         self.divined_list.append(i)
                #         return self.myresult

            # 3.declare vote if not yet
            if self.vote_declare != self.vote():
                self.vote_declare = self.vote()
                return cb.vote(self.vote_declare)

            # 4.Other
            if self.talk_turn <= 10:

                rnd = np.random.rand(1)

                if (rnd[0] <= 0.35):
                    p = -100000
                    idx = 1
                    if len(self.estimate_list) < 2:
                        for i in range(1, 6):
                            p0 = self.pred_roles[i][3]
                            if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and i not in self.estimate_list:
                                p = p0
                                idx = i
                        self.estimate_list.append(idx)
                        return cb.estimate(idx, 'VILLAGER')
                elif (rnd[0] <= 0.7):
                    return cb.skip()
                else:
                    return cb.over()


        return cb.over()

    def whisper(self):

        if self.game_count <= self.ps_num:
            return self.ps.whisper()

        # declare vote if not yet
        if self.attack_declare != self.attack():
            self.attack_declare = self.attack()
            return cb.attack(self.attack_declare)

        return cb.over()

    def vote(self):

        if self.game_count <= self.ps_num:
            return self.ps.vote()

        # print (self.predictor_p.get_pred_assign_names(self.base_info))

        if self.is_power_play == True and self.pp_comingout == 'POSSESSED':

            werewolf_num = 0
            werewolf_idx = -1
            if self.base_info['myRole'] == 'POSSESSED':
                for k, v in self.co_map.items():

                    if k == self.base_info['agentIdx']:
                        continue

                    if v == 'WEREWOLF':
                        werewolf_num += 1
                        werewolf_idx = k

            if werewolf_num == 1:
                for i in range(1, self.game_setting['playerNum']+1):
                    if self.base_info['statusMap'][str(i)] == 'ALIVE':
                        if i != k and i != werewolf_idx:
                            return i

        elif self.is_power_play and self.pp_comingut == 'WEREWOLF':

            possessed_num = 0
            possessed_idx = -1
            if self.base_info['myRole'] == 'WEREWOLF':
                for k, v in self.co_map.items():

                    if k == self.base_info['agentIdx']:
                        continue

                    if v == 'POSSESSED':
                        possessed_num += 1
                        possessed_idx = k


            if possessed_num == 1:
                for i in range(1, self.game_setting['playerNum'] + 1):
                    if self.base_info['statusMap'][str(i)] == 'ALIVE':
                        if i != k and i != possessed_idx:
                            return i



        try:
            if self.is_negative_vote == 1:
                self.negative_vote_declare = re.split(r'\s|"|,|\.|\n|\(|\)|\[|\]', self.negative_vote_declare)
                idx = 1
                if self.negative_vote_declare != '' and len(self.negative_vote_declare) > 1:
                    idx = int(self.negative_vote_declare[1])
                return idx
        except:
            print ('error')


        myrole = self.base_info['myRole']

        if self.game_setting['playerNum'] == 15:

            if myrole == 'WEREWOLF':
                p = -100000
                idx = 1
                for i in range(1, 16):
                    p0 = self.pred_roles[i][0]
                    if str(i) in self.base_info['roleMap'].keys():
                        p0 -= 100
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and self.base_info['agentIdx'] != i:
                        p = p0
                        idx = i

            elif myrole == 'POSSESSED':
                p = -100000
                idx = 1
                for i in range(1, 16):
                    p0 = self.pred_roles[i][0]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and self.base_info['agentIdx'] != i:
                        p = p0
                        idx = i
            else:
                p = -100000
                idx = 1
                for i in range(1, 16):
                    p0 = self.pred_roles[i][0]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and self.base_info['agentIdx'] != i:
                        p = p0
                        idx = i

            return idx

        else:

            # lowest possessed
            if myrole == 'WEREWOLF':
                p = 100000
                idx = 1
                for i in range(1, 6):
                    p0 = self.pred_roles[i][2]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and self.base_info['agentIdx'] != i:
                        p = p0
                        idx = i

            elif myrole == 'POSSESSED':
                p = 100000
                idx = 1
                for i in range(1, 6):
                    p0 = self.pred_roles[i][2]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and self.base_info['agentIdx'] != i:
                        p = p0
                        idx = i
            else:

                # highest ww
                p = -100000
                idx = 1
                for i in range(1, 6):
                    p0 = self.pred_roles[i][0]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p and self.base_info['agentIdx'] != i:
                        p = p0
                        idx = i

            return idx

        return self.base_info['agentIdx']

    def attack(self):

        if self.game_count <= self.ps_num:
            return self.ps.attack()

        # lowest possessed
        if self.game_setting['playerNum'] == 15:

            name_map = self.predictor_p.get_pred_assign_names(self.base_info)
            role_map = self.predictor_p.get_pred_assign_roles(self.base_info)
            p = 0
            i = 1
            for idx in range(1, 16):
                if str(idx) in self.base_info['roleMap'].keys():
                    continue
                if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] and role_map[idx] != 'POSSESSED' and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                    p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])]
                    i = idx
            return i

            # p = 100000
            # idx = 1
            # for i in range(1, 16):
            #     if str(i) in self.base_info['roleMap'].keys():
            #         continue
            #     p0 = self.pred_roles[i][1]
            #     if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and self.base_info['agentIdx'] != i:
            #         p = p0
            #         idx = i
            # return idx

        else:

            name_map = self.predictor_p.get_pred_assign_names(self.base_info)
            role_map = self.predictor_p.get_pred_assign_roles(self.base_info)
            p = 0
            i = 1
            for idx in range(1, 6):
                if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] and role_map[idx] != 'POSSESSED' and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                    p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])]
                    i = idx
            return i


            # p = 100000
            # idx = 1
            # for i in range(1, 6):
            #     if str(i) in self.base_info['roleMap'].keys():
            #         continue
            #     p0 = self.pred_roles[i][1]
            #     if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and self.base_info['agentIdx'] != i:
            #         p = p0
            #         idx = i
            # return idx

        return self.base_info['agentIdx']

    def divine(self):

        if self.game_count <= self.ps_num:
            return self.ps.divine()

        # name decied
        # if self.game_count > 0:

        if self.game_setting['playerNum'] == 15:

            #if self.base_info['day'] == 1 or self.base_info['day'] == 0 or self.base_info['day'] == 2:

            name_map = self.predictor_p.get_pred_assign_names(self.base_info)
            p = 0
            i = 1
            for idx in range(1, 16):
                if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] and i not in self.divined_list and self.pred_roles[idx][0] > 0 and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                    p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])]
                    i = idx

            self.divined_list.append(i)
            return i

            # highest ww
            # p = -100000
            # idx = 1
            # for i in range(1, 16):
            #     p0 = self.pred_roles[i][0]
            #     if self.base_info['statusMap'][str(i)] == 'ALIVE' and i not in self.divined_list and self.divined_list and p0 > p and self.base_info['agentIdx'] != i:
            #         p = p0
            #         idx = i
            #
            # self.divined_list.append(idx)
            # return idx

        else:

            if self.base_info['day'] == 1:

                name_map = self.predictor_p.get_pred_assign_names(self.base_info)
                p = 0
                i = 1
                for idx in range(1, 6):
                    if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] and i not in self.divined_list and self.pred_roles[idx][0] > 0 and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                        p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])]
                        i = idx

                self.divined_list.append(i)
                return i

            p = -100000
            idx = 1
            for i in range(1, 6):
                p0 = self.pred_roles[i][0]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and i not in self.divined_list and p0 > p and self.base_info['agentIdx'] != i:
                    p = p0
                    idx = i

            self.divined_list.append(idx)
            return idx

        return self.base_info['agentIdx']

    def guard(self):

        if self.game_count <= self.ps_num:
            return self.ps.guard()

        role_point = [ 0.1, 0.5, 3, 1, 2, 0 ]


        # if self.game_setting['playerNum'] == 15:
        #

        # lowest ww and highest seer
        if self.game_setting['playerNum'] == 15:

            try:
                name_map = self.predictor_p.get_pred_assign_names(self.base_info)
                role_map = self.predictor_p.get_pred_assign_roles(self.base_info)
                p = 0
                i = 1

                for idx in range(1, 16):
                    if p < self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] * role_point[self.predictor_p.role_to_idx_5(role_map[idx])] and i not in self.divined_list and self.pred_roles[idx][0] > 0 and self.base_info['statusMap'][str(idx)] == 'ALIVE':
                        p = self.predictor_p.power[self.predictor_p.name_to_idx(name_map[idx])] * role_point[self.predictor_p.role_to_idx_5(role_map[idx])]
                        i = idx
            except:
                print ('error')



            return i

            p = 100000
            p2 = -10000
            idx = 1
            for i in range(1, 16):
                p0 = self.pred_roles[i][0]
                p1 = self.pred_roles[i][2]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and p1 > p2 and self.base_info['agentIdx'] != i:
                    p = p0
                    p2 = p1
                    idx = idx
            return idx
        else:
            p = 100000
            p2 = -10000
            idx = 1
            for i in range(1, 6):
                p0 = self.pred_roles[i][0]
                p1 = self.pred_roles[i][2]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and p1 > p2 and self.base_info['agentIdx'] != i:
                    p = p0
                    p2 = p1
                    idx = idx
            return idx

        return self.base_info['agentIdx']

    def finish(self):

        if self.is_finish == False:

            # for idx in range(1, self.game_setting['playerNum']+1):
            #     if self.base_info['agentIdx'] == idx:
            #         continue
            #     print ('idx=', idx, ': ', self.predictor_p.role_15[self.pred_roles[idx].argmax()])

            # DEBUG
            # print (self.predictor_p.get_pred_assign_roles(self.base_info))

            self.predictor_p.finish(self.base_info, self.talks, self.roles, self.game_count, whisper=self.whispers)

            if self.is_win == 1:
                self.win_num += 1

            #print (self.win_num, self.game_count)
            #print ('win_rate = ', self.win_num / self.game_count)

            self.is_finish = True

            #print ('')

            if self.game_count <= self.ps_num:
                return self.finish()

        pass

        return None


agent = SampleAgent(myname)



# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
