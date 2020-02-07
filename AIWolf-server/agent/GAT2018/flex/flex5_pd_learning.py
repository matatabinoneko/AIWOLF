#!/usr/bin/env python
from __future__ import print_function, division

import aiwolfpy
import aiwolfpy.contentbuilder as cb

import os
import csv
import numpy as np
import chainer
from chainer import datasets, Link, Chain, ChainList, training
from chainer.training import extensions
from chainer.datasets import tuple_dataset
import chainer.functions as F
import chainer.links as L

myname = 'flex_learning'

STATE_DOA = 0
STATE_SEER_NUM = 1
STATE_WHITE = 2
STATE_BLACK = 3
STATE_SEER_ORDER = 4
STATE_TO_WHITE = 5
STATE_TO_BLACK = 6
STATE_CHANGE_VOTE = 7
STATE_DAY = 8
STATE_POSI = 9
STATE_NEGA = 14

ALPHA = 3 / 2
BETA = 2 / 3
DEBUG = True

MID4 = [130,175,130,450,215,4,3]
MID5 = [480,390,140,160,370,5,2]


class AgentModel(Chain):
    def __init__(self, n_units1, n_units2, n_units3, n_units4, n_units5, n_out, layer_num):
        super(AgentModel, self).__init__()
        with self.init_scope():
            self.l1 = L.Linear(None, n_units1)
            self.l2 = L.Linear(None, n_units2)
            self.l3 = L.Linear(None, n_units3)
            self.l4 = L.Linear(None, n_units4)
            self.l5 = L.Linear(None, n_units5)
            self.lfinal = L.Linear(None, n_out)
        self.layer_num = layer_num

    def __call__(self, x):
        h1 = F.relu(self.l1(x))
        if self.layer_num == 1:
            return self.lfinal(h1)
        h2 = F.relu(self.l2(h1))
        if self.layer_num == 2:
            return self.lfinal(h2)
        h3 = F.relu(self.l3(h2))
        if self.layer_num == 3:
            return self.lfinal(h3)
        h4 = F.relu(self.l4(h3))
        if self.layer_num == 4:
            return self.lfinal(h4)
        h5 = F.relu(self.l5(h4))
        #layer_num == 5
        return self.lfinal(h5)


class Agent(object):
    VILLAGE = ['VILLAGER', 'SEER', 'MEDIUM', 'BODYGUARD']
    WOLF = ['WEREWOLF', 'POSSESSED']

    def __init__(self, agent_name, debug=False):
        self.my_name = agent_name
        self.debug = debug

    def getName(self):
        return self.my_name

    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        # print("initialize")
        # print(base_info)
        # print(diff_data)

        self.divined_list = []
        self.comingout = ''
        self.my_result = ''
        self.not_reported = False
        self.vote_declare = 0
        self.talk_turn = 0
        self.day = -1
        self.my_id = base_info['agentIdx']
        self.my_role = base_info['myRole']
        self.agent_num = len(base_info['statusMap'])
        self.coMap = dict()
        self.divineMap = dict()

        # agent b/g/w list
        self.white_list = []
        self.black_list = []
        self.gray_list = [1, 2, 3, 4, 5]

        # my talked or my will talk list
        self.mytalks = []
        self.mywilltalks = []

        # all talk list
        self.talk_list = np.empty([4, 100], np.int32)
        self.talk_list.fill(-1)
        self.talk_vote_list = np.zeros(self.agent_num, dtype=np.int8)

        # initialize state and model
        self.state_agent = np.zeros((self.agent_num, self.num_of_astates()), dtype=np.float32)
        self.state_game = np.zeros(self.num_of_gstates(), dtype=np.float32)
        path = os.path.dirname(__file__)
        self.model_agent = L.Classifier(AgentModel(MID4[0], MID4[1], MID4[2], MID4[3], MID4[4], MID4[5], MID4[6]))
        chainer.serializers.load_npz(path + "/redata20_re.model", self.model_agent)
        self.model_game = L.Classifier(AgentModel(MID5[0], MID5[1], MID5[2], MID5[3], MID5[4], MID5[5], MID5[6]))
        chainer.serializers.load_npz(path + "/redata20_game_re.model", self.model_game)

        # result
        self.finished = False
        self.predict = [[], []]
        self.predict_result = [[0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0]]
        self.predict_result_wolf = [0, 0, 0, 0, 0]

    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        self.diff_data = diff_data
        # print("update:"+request)
        # print(base_info)
        # print(diff_data)
        if request == 'DAILY_INITIALIZE':
            for i in range(diff_data.shape[0]):
                agent = diff_data.idx[i]
                content = diff_data.text[i].split()
                # DIVINE
                if diff_data.type[i] == 'divine':
                    self.not_reported = True
                    self.my_result = diff_data.text[i]
                    target = int(content[1][6:8])
                    if content[2] == 'WEREWOLF':
                        self.gray_list.remove(target)
                        self.black_list.append(target)
                    elif content[2] == 'HUMAN':
                        self.gray_list.remove(target)
                        self.white_list.append(target)
                # VOTE
                elif diff_data.type[i] == 'vote':
                    target = int(content[1][6:8])
                    # difference talk_vote and vote
                    if self.talk_vote_list[agent - 1] != 0 and target != self.talk_vote_list[agent - 1]:
                        self.state_agent[agent - 1, STATE_CHANGE_VOTE] += 1
                # cause of death
                elif diff_data.type[i] == 'execute':
                    self.state_agent[agent - 1, STATE_DOA] = 2
                elif diff_data.type[i] == 'dead':
                    self.state_agent[agent - 1, STATE_DOA] = 1
            # POSSESSED
            if self.my_role == 'POSSESSED':
                self.not_reported = True
        elif request == 'VOTE':
            for i in range(diff_data.shape[0]):
                agent = diff_data.agent[i]
                if diff_data.type[i] == 'vote':
                    content = diff_data.text[i].split()
                    target = int(content[1][6:8])
                    if self.talk_vote_list[agent - 1] != 0 and target != self.talk_vote_list[agent - 1]:
                        self.state_agent[agent - 1, STATE_CHANGE_VOTE] += 1
        else:
            # update state
            for i in range(diff_data.shape[0]):
                agent = diff_data.agent[i]
                if diff_data.type[i] == 'talk':
                    self.talk_list[diff_data.day, i] = agent
                    content = diff_data.text[i].split()
                    # update DAY
                    self.state_agent[agent - 1, STATE_DAY] = diff_data.day[0]
                    # comingout
                    if content[0] == 'COMINGOUT':
                        self.coMap[agent] = content[2]
                        co_seer = 0
                        for role in self.coMap.values():
                            if role == 'SEER':
                                co_seer += 1
                        self.state_agent[agent - 1, STATE_SEER_ORDER] = co_seer
                        for i in range(5):
                            self.state_agent[i, STATE_SEER_NUM] = co_seer
                    # divined
                    elif content[0] == 'DIVINED':
                        target = int(content[1][6:8])
                        if agent not in self.divineMap.keys():
                            self.divineMap[agent] = dict()
                        self.divineMap[agent][target] = content[2]
                        if content[2] == 'HUMAN':
                            self.state_agent[agent - 1, STATE_TO_WHITE] += 1
                            self.state_agent[target - 1, STATE_WHITE] += 1
                        elif content[2] == 'WEREWOLF':
                            self.state_agent[agent - 1, STATE_TO_BLACK] += 1
                            self.state_agent[target - 1, STATE_BLACK] += 1
                    # estimate
                    elif content[0] == 'ESTIMATE':
                        target = int(content[1][6:8])
                        if content[2] in self.VILLAGE:
                            self.state_agent[agent - 1, STATE_POSI + target - 1] += 0.1
                        elif content[2] in self.WOLF:
                            self.state_agent[agent - 1, STATE_NEGA + target - 1] += 0.1
                    # agree
                    elif content[0] == 'AGREE':
                        tday = int(content[2][3:])
                        tid = int(content[3][3:])
                        target = self.talk_list[tday, tid]
                        self.state_agent[agent - 1, STATE_POSI + target - 1] += 0.1
                    # disagree
                    elif content[0] == 'DISAGREE':
                        tday = int(content[2][3:])
                        tid = int(content[3][3:])
                        target = self.talk_list[tday, tid]
                        self.state_agent[agent - 1, STATE_NEGA + target - 1] += 0.1
                    # vote
                    elif content[0] == 'VOTE':
                        target = int(content[1][6:8])
                        self.talk_vote_list[agent - 1] = target
            self.state_game = self.convert_game_info(self.state_agent)

    def dayStart(self):
        self.day += 1
        self.vote_declare = 0
        self.talk_turn = 0
        self.talk_vote_list = np.zeros(self.agent_num, dtype=np.int8)

        return None

    def talk(self):
        self.talk_turn += 1

        # 1:COMINGOUT
        if self.my_role == 'SEER' and self.comingout == '':
            self.comingout = 'SEER'
            return cb.comingout(self.my_id, self.comingout)
        elif self.my_role == 'POSSESSED' and self.comingout == '':
            self.comingout = 'SEER'
            return cb.comingout(self.my_id, self.comingout)

        # 2:RESULT
        if self.my_role == 'SEER' and self.not_reported:
            self.not_reported = False
            return self.my_result
        elif self.my_role == 'POSSESSED' and self.not_reported:
            self.not_reported = False
            # FAKE DIVINE
            # other SEER agent == 1 -> black
            seer = 0
            id = -1
            for agent, role in self.coMap.items():
                if role == 'SEER':
                    seer += 1
                    if agent != self.my_id:
                        id = agent
            if seer == 2 and int(self.state_agent[id - 1, STATE_DOA]) == 0:
                self.my_result = 'DIVINED Agent[' + "{0:02d}".format(id) + '] WEREWOLF'
            else:
                # lower wolf agent
                w = self.predict_wolf()
                # exclude divined agent
                # TODO
                p = 100
                id = -1
                for i in range(self.agent_num):
                    # exclude myself
                    # exclude dead agent
                    if p > w[i] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                        p = w[i]
                        id = i
                if id != -1:
                    self.my_result = 'DIVINED Agent[' + "{0:02d}".format(id + 1) + '] WEREWOLF'
                else:
                    self.my_result = 'DIVINED Agent[' + "{0:02d}".format(self.my_id) + '] HUMAN'

            return self.my_result
        # 3:ESTIMATE
        # TODO

        # 4: VOTE
        if self.vote_declare != self.vote():
            self.vote_declare = self.vote()
            if self.vote_declare != self.my_id:
                return cb.vote(self.vote_declare)

        # 5: SKIP
        if self.talk_turn <= 10:
            return cb.skip()

        return cb.over()

    def whisper(self):
        # No use
        return cb.over()

    def vote(self):
        # print(self.predict_role())
        # print(self.predict_wolf())
        r = self.predict_role()
        w = self.predict_wolf()
        r = self.inclinate_predict(r, w)
        # WEREWOLF
        if self.my_role == 'WEREWOLF':
            p = -1
            id = -1
            for i in range(self.agent_num):
                if p < r[i][0] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                    p = r[i][0]
                    id = i
            if id != -1:
                return id + 1
        # POSSESSED
        elif self.my_role == 'POSSESSED':
            p = -1
            id = -1
            for i in range(self.agent_num):
                if p < r[i][0] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                    p = r[i][0]
                    id = i
            if id != -1:
                return id + 1
        # SEER
        elif self.my_role == 'SEER':
            if len(self.black_list) != 0:
                agent = self.black_list[0]
                return agent

            p = -1
            id = -1
            for i in range(self.agent_num):
                if p < r[i][3] and int(
                        self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id and i + 1 in self.gray_list:
                    p = r[i][3]
                    id = i
            if id != -1:
                return id + 1
        # VILLAGER
        elif self.my_role == 'VILLAGER':
            p = -1
            id = -1
            for i in range(self.agent_num):
                if p < r[i][3] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                    p = r[i][3]
                    id = i
            if id != -1:
                return id + 1

        return self.my_id

    def attack(self):
        r = self.predict_role()
        w = self.predict_wolf()
        r = self.inclinate_predict(r, w)
        # SEER
        p = -1
        id = -1
        for i in range(self.agent_num):
            # exclude myself
            # exclude dead agent
            if p < r[i][0] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                p = r[i][0]
                id = i
        if id != -1:
            return id + 1
        # VILLAGER
        p = -1
        id = -1
        for i in range(self.agent_num):
            # exclude myself
            # exclude dead agent
            if p < r[i][1] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                p = r[i][1]
                id = i
        if id != -1:
            return id + 1
        # POSSESSED
        p = -1
        id = -1
        for i in range(self.agent_num):
            # exclude myself
            # exclude dead agent
            if p < r[i][2] and int(self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id:
                p = r[i][2]
                id = i
        if id != -1:
            return id + 1
        else:
            return self.my_id

    def divine(self):
        r = self.predict_role()
        w = self.predict_wolf()
        r = self.inclinate_predict(r, w)

        p = -1
        id = -1
        for i in range(self.agent_num):
            # exclude myself
            # exclude dead agent
            if p < r[i][3] and int(
                    self.state_agent[i, STATE_DOA]) == 0 and i + 1 != self.my_id and i + 1 not in self.divined_list:
                p = r[i][3]
                id = i
        self.divined_list.append(id + 1)
        if id != -1:
            return id + 1
        else:
            return self.my_id

    def guard(self):
        # No use
        return self.base_info['agentIdx']

    def finish(self):
        # double finish
        if self.finished:
            return None
        self.finished = True
        return None

    def daily_finish(self):
        pass

    def num_of_astates(self):
        return 19

    def num_of_gstates(self):
        return 2 + 5 * 17

    def convert_game_info(self, info):
        game_info = np.zeros(2, np.float32)
        for j in range(self.agent_num):
            co_seer = info[0, 1]
            game_info[0] = self.day
            game_info[1] = co_seer
            temp_info = np.delete(info[j], [1, 8])
            game_info = np.append(game_info, temp_info)
        return game_info

    def predict_role(self):
        y = self.model_agent.predictor(self.state_agent)
        sf_y = F.softmax(y, axis=1)
        return sf_y.data
        # return np.argmax(y.data, axis=1)

    def predict_wolf(self):
        y = self.model_game.predictor(np.reshape(self.state_game, (1, self.num_of_gstates())))
        sf_y = F.softmax(y)
        return sf_y.data[0]
        # return np.argmax(y.data)

    def inclinate_predict(self, pr, pw):
        p = [[0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0]]
        idx = 0
        for agent in pr:
            # print(agent)
            # print(pw[idx])
            # SEER
            p[idx][0] = agent[0] * (1 - pw[idx])
            # VILLAGER
            p[idx][1] = agent[1] * (1 - pw[idx])
            # POSSESSED
            p[idx][2] = agent[2] * pw[idx]
            # WEREWOLF
            p[idx][3] = agent[3] * pw[idx]

            idx += 1
        return p


agent = Agent(myname, DEBUG)

# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
