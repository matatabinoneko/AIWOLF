#!/usr/bin/env python
from __future__ import print_function, division

# this is main script

import aiwolfpy
import aiwolfpy.contentbuilder as cb

# sample
import aiwolfpy.cash
# import aiwolfpy.daisyo

import re
import numpy as np
from collections import Counter

myname = 'daisyo'

class PythonPlayer(object):

    def __init__(self, agent_name):

        # myname
        self.myname = agent_name

        # predictor from sample
        # DataFrame -> P
        self.predicter_15 = aiwolfpy.cash.Predictor_15()
        self.predicter_5 = aiwolfpy.cash.Predictor_5()

        #self.predictor_p = aiwolfpy.daisyo.PlayerPredictor()

        self.talks = {}
        for gc in range(102):
            self.talks[gc] = {}
            for i in range(17):
                self.talks[gc][i] = []

        self.game_count = 0
        self.win_num = 0


    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        # print(base_info)
        # print(diff_data)
        # base_info
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting

        # initialize
        #self.predictor_p.initialize(game_setting)
        if self.game_setting['playerNum'] == 15:
            self.predicter_15.initialize(base_info, game_setting)
        elif self.game_setting['playerNum'] == 5:
            self.predicter_5.initialize(base_info, game_setting)


        ### EDIT FROM HERE ###
        self.divined_list = []
        self.comingout = ''
        self.myresult = ''
        self.not_reported = False
        self.vote_declare = 0
        self.attack_declare = 0
        self.is_finish = False
        self.co_map = {}
        self.vote_map = {}

        self.game_count += 1
        self.is_win = 0

        self.is_power_play = False
        self.is_power_play_reported = False
        self.is_werewolf_co = False
        self.pp_comingout = ''

        self.is_true_comingout = False


        for i in range(self.game_setting['playerNum'] + 1):
            self.co_map[i] = ''
            self.vote_map[i] = ''



    def update(self, base_info, diff_data, request):

        # print(base_info)
        # print(diff_data)
        # update base_info
        self.base_info = base_info

        # print (self.base_info)

        # result
        if request == 'DAILY_INITIALIZE':
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

            # POSSESSED
            if self.base_info['myRole'] == 'POSSESSED':
                self.not_reported = True

            # is_power_play
            alive_num = 0
            for i in range(1, self.game_setting['playerNum']+1):
                if self.base_info['statusMap'][str(i)] == 'ALIVE':
                    alive_num += 1

            if alive_num == 3:
                is_power_play = True


        elif request == 'TALK':

            for i in range(diff_data.shape[0]):

                # SKIP (MY TALK)
                if diff_data['agent'][i] == self.base_info['agentIdx']:
                    continue

                # UPDATE PLAYER PREDICOTR
                #self.talks[self.game_count][diff_data['agent'][i]].append(diff_data['text'][i])

                #
                text = re.split(r'\s|"|,|\.|\n|\(|\)', diff_data['text'][i])

                # MAKE VOTE MAP
                if (text[0] == 'VOTE'):
                    self.vote_map[diff_data['agent'][i]] = text[1]

                if (text[0] == 'COMINGOUT'):
                    self.co_map[diff_data['agent'][i]] = text[2]

                # print (self.talk_turn)
                # print (self.vote_map)

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

                for word, cnt in agent_counter.most_common():
                    if cnt > (alive_num-1)/2 and word == 'Agent[' + "{0:02d}".format(self.base_info['agentIdx']) + ']':
                        self.is_true_comingout = True


        elif request == 'WHISPER':
            for i in range(diff_data.shape[0]):
                if diff_data['agent'][i] == self.base_info['agentIdx']:
                    continue

        elif request == 'VOTE':
            agent_counter = Counter()

            for k, v in self.vote_map.items():
                if k == 0:
                    continue;
                v = re.split(r'\s|"|,|\.|\n|\(|\)', v)
                cnt = Counter(v)
                agent_counter += cnt


            # print (self.vote_map)

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



        if request == 'FINISH':
            self.roles = []
            self.roles.append('')

            aiwolf_list = []
            for i in range(diff_data.shape[0]):
                t = re.split(r'\s|"|,|\.|\n|\(|\)', diff_data['text'][i])
                self.roles.append(t[2])
                if t[2] == 'WEREWOLF':
                    aiwolf_list.append(diff_data['agent'][i])

            ww = 0
            for i in range(1, self.game_setting['playerNum'] + 1):
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and aiwolf_list.count(i) > 0:
                    ww = 1

            if ((self.base_info['myRole'] == 'WEREWOLF' or self.base_info['myRole'] == 'POSSESSED') and ww == 1) or ((self.base_info['myRole'] != 'WEREWOLF' and self.base_info['myRole'] != 'POSSESSED') and ww == 0):
                self.is_win = 1




        # UPDATE
        if self.game_setting['playerNum'] == 15:
            if self.base_info["day"] == 0 and request == 'DAILY_INITIALIZE' and self.game_setting['talkOnFirstDay'] == False:
                # update pred
                self.predicter_15.update_features(diff_data)
                self.predicter_15.update_df()

            elif self.base_info["day"] == 0 and request == 'DAILY_FINISH' and self.game_setting['talkOnFirstDay'] == False:
                # no talk at day:0
                self.predicter_15.update_pred()

            else:
                # update pred
                self.predicter_15.update(diff_data)
        else:
            self.predicter_5.update(diff_data)




    def dayStart(self):
        self.vote_declare = 0
        self.attack_declare = 0
        self.talk_turn = 0
        self.is_negative_vote = 0
        self.negative_vote_declare = 0

        for i in range(self.game_setting['playerNum'] + 1):
            self.vote_map[i] = ''

        # print (self.co_map)

        return None

    def talk(self):
        self.talk_turn += 1

        if self.is_true_comingout == True and self.comingout == '':
            if self.base_info['myRole'] != 'WEREWOLF':
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

            # if self.talk_turn >= 5 and self.base_info['myRole'] == 'WEREWOLF':
            #
            #     possessed_num = 0
            #     possessed_idx = -1
            #     for k, v in self.co_map.items():
            #
            #         if k == self.base_info['agentIdx']:
            #             continue
            #
            #         if v == 'POSSESSED':
            #             possessed_num += 1
            #             possessed_idx = k
            #
            #     if possessed_num == 1 and self.pp_comingout == '':
            #         self.pp_comingout = 'WEREWOLF'
            #         return cb.comingout(self.base_info['agentIdx'], self.pp_comingout)

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
            if self.base_info['myRole'] == 'SEER' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)
            elif self.base_info['myRole'] == 'MEDIUM' and self.comingout == '':
                self.comingout = 'MEDIUM'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)
            elif self.base_info['myRole'] == 'POSSESSED' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)

            # 2.report
            if self.base_info['myRole'] == 'SEER' and self.not_reported:
                self.not_reported = False
                return self.myresult
            elif self.base_info['myRole'] == 'MEDIUM' and self.not_reported:
                self.not_reported = False
                return self.myresult
            elif self.base_info['myRole'] == 'POSSESSED' and self.not_reported:
                self.not_reported = False
                # FAKE DIVINE
                # highest prob ww in alive agents
                p = -1
                idx = 1
                p0_mat = self.predicter_15.ret_pred()
                for i in range(1, 16):
                    p0 = p0_mat[i-1, 1]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
                self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'
                return self.myresult

            # 3.declare vote if not yet
            if self.vote_declare != self.vote():
                self.vote_declare = self.vote()
                return cb.vote(self.vote_declare)

            # 4. Other
            if self.talk_turn <= 10:

                rnd = np.random.rand(1)

                if (rnd[0] <= 0.35):
                    p = 1000000000
                    idx = 1
                    p0_mat = self.predicter_15.ret_pred()
                    for i in range(1, 16):
                        p0 = p0_mat[i-1, 1]
                        if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p:
                            p = p0
                            idx = i
                    return cb.estimate(idx, 'VILLAGER')
                elif (rnd[0] <= 0.8):
                    return cb.skip()
                else:
                    return cb.over()

                return cb.skip()

            return cb.over()
        else:

            # 1.comingout anyway
            if self.base_info['myRole'] == 'SEER' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)
            elif self.base_info['myRole'] == 'MEDIUM' and self.comingout == '':
                self.comingout = 'MEDIUM'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)
            elif self.base_info['myRole'] == 'POSSESSED' and self.comingout == '':
                self.comingout = 'SEER'
                return cb.comingout(self.base_info['agentIdx'], self.comingout)

            # 2.report
            if self.base_info['myRole'] == 'SEER' and self.not_reported:
                self.not_reported = False
                return self.myresult
            elif self.base_info['myRole'] == 'MEDIUM' and self.not_reported:
                self.not_reported = False
                return self.myresult
            elif self.base_info['myRole'] == 'POSSESSED' and self.not_reported:
                self.not_reported = False
                # FAKE DIVINE
                # highest prob ww in alive agents
                p = -1
                idx = 1
                p0_mat = self.predicter_5.ret_pred_wx(2)
                for i in range(1, 6):
                    p0 = p0_mat[i-1, 1]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
                self.myresult = 'DIVINED Agent[' + "{0:02d}".format(idx) + '] ' + 'HUMAN'
                return self.myresult

            # 3.declare vote if not yet
            if self.vote_declare != self.vote():
                self.vote_declare = self.vote()
                return cb.vote(self.vote_declare)

            # 4. skip
            if self.talk_turn <= 10:

                rnd = np.random.rand(1)

                if (rnd[0] <= 0.35):
                    p = 1000000000
                    idx = 1
                    p0_mat = self.predicter_5.ret_pred_wx(2)
                    for i in range(1, 6):
                        p0 = p0_mat[i-1, 1]
                        if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p:
                            p = p0
                            idx = i
                    return cb.estimate(idx, 'VILLAGER')
                elif (rnd[0] <= 0.8):
                    return cb.skip()
                else:
                    return cb.over()


                return cb.skip()

            return cb.over()

    def whisper(self):

        # declare vote if not yet
        if self.attack_declare != self.attack():
            self.attack_declare = self.attack()
            return cb.attack(self.attack_declare)

        return cb.skip()

    def vote(self):

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

        if self.game_setting['playerNum'] == 15:
            p0_mat = self.predicter_15.ret_pred_wn()
            if self.base_info['myRole'] == "WEREWOLF":
                p = -1
                idx = 1
                for i in range(1, 16):
                    p0 = p0_mat[i-1, 1]
                    if str(i) in self.base_info['roleMap'].keys():
                        p0 *= 0.5
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            elif self.base_info['myRole'] == "POSSESSED":
                p = -1
                idx = 1
                for i in range(1, 16):
                    p0 = p0_mat[i-1, 1]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            else:
                # highest prob ww in alive agents provided watashi ningen
                p = -1
                idx = 1
                for i in range(1, 16):
                    p0 = p0_mat[i-1, 1]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            return idx
        else:
            if self.base_info['myRole'] == "WEREWOLF":
                p0_mat = self.predicter_5.ret_pred_wx(1)
                p = -1
                idx = 1
                for i in range(1, 6):
                    p0 = p0_mat[i-1, 3]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            elif self.base_info['myRole'] == "POSSESSED":
                p0_mat = self.predicter_5.ret_pred_wx(2)
                p = -1
                idx = 1
                for i in range(1, 6):
                    p0 = p0_mat[i-1, 3]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            elif self.base_info['myRole'] == "SEER":
                p0_mat = self.predicter_5.ret_pred_wx(3)
                p = -1
                idx = 1
                for i in range(1, 6):
                    p0 = p0_mat[i-1, 1]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            else:
                p0_mat = self.predicter_5.ret_pred_wx(0)
                p = -1
                idx = 1
                for i in range(1, 6):
                    p0 = p0_mat[i-1, 1]
                    if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                        p = p0
                        idx = i
            return idx

    def attack(self):
        if self.game_setting['playerNum'] == 15:
            # highest prob hm in alive agents
            p = -1
            idx = 1
            p0_mat = self.predicter_15.ret_pred()
            for i in range(1, 16):
                p0 = p0_mat[i-1, 0]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                    p = p0
                    idx = i
            return idx
        else:
            # lowest prob ps in alive agents
            p = 1
            idx = 1
            p0_mat = self.predicter_5.ret_pred_wx(1)
            for i in range(1, 6):
                p0 = p0_mat[i-1, 2]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 < p and i != self.base_info['agentIdx']:
                    p = p0
                    idx = i
            return idx

    def divine(self):
        if self.game_setting['playerNum'] == 15:
            # highest prob ww in alive and not divined agents provided watashi ningen
            p = -1
            idx = 1
            p0_mat = self.predicter_15.ret_pred_wn()
            for i in range(1, 16):
                p0 = p0_mat[i-1, 1]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and i not in self.divined_list and p0 > p:
                    p = p0
                    idx = i
            self.divined_list.append(idx)
            return idx
        else:
            # highest prob ww in alive and not divined agents provided watashi ningen
            p = -1
            idx = 1
            p0_mat = self.predicter_5.ret_pred_wx(3)
            for i in range(1, 6):
                p0 = p0_mat[i-1, 1]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and i not in self.divined_list and p0 > p:
                    p = p0
                    idx = i
            self.divined_list.append(idx)
            return idx

    def guard(self):
        if self.game_setting['playerNum'] == 15:
            # highest prob hm in alive agents
            p = -1
            idx = 1
            p0_mat = self.predicter_15.ret_pred()
            for i in range(1, 16):
                p0 = p0_mat[i-1, 0]
                if self.base_info['statusMap'][str(i)] == 'ALIVE' and p0 > p:
                    p = p0
                    idx = i
            return idx
        else:
            # no need
            return 1

    def finish(self):

        if self.is_finish == False:

            # print ('game_count = ', self.game_count)
            #self.predictor_p.finish(self.base_info, self.talks, self.roles, self.game_count)
            self.is_finish = True

            if self.is_win == 1:
                self.win_num += 1

            # print (self.win_num, self.game_count)
            # print ('win_rate = ', self.win_num / self.game_count)

        pass



agent = PythonPlayer(myname)

# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
