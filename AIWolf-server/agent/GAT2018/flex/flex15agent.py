#!/usr/bin/env python3
import aiwolfpy
import aiwolfpy.contentbuilder as cb

import sys
import os
from collections import namedtuple
import itertools
import random

import numpy as np

class Agent(object):
    ROLE5_LIST = ['SEER','VILLAGER','POSSESSED','WEREWOLF']
    VILLAGE = ['VILLAGER', 'SEER', 'MEDIUM', 'BODYGUARD']
    WOLF = ['WEREWOLF', 'POSSESSED']

    def __init__(self, agent_name):
        self.agent_name = agent_name

    def getName(self):
        return self.agent_name

    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        self.diff_data = diff_data
        if request == 'DAILY_INITIALIZE':
            for i in range(diff_data.shape[0]):
                if diff_data.type[i] == 'identify':
                    self.not_reported = True
                    self.my_result = diff_data.text[i]
                if diff_data.type[i] == 'divine':
                    self.not_reported = True
                    self.my_result = diff_data.text[i]
                if diff_data.type[i] == 'guard':
                    self.my_result = diff_data.text[i]
            if self.my_role == 'POSSESSED':
                self.not_reported = True

        for i in range(diff_data.shape[0]):
            if diff_data.type[i] == 'talk':
                talk = diff_data.text[i].split()
                talker = diff_data.agent[i]
                if talk[0] == 'COMINGOUT':
                    self.coMap[talker] = talk[2]
                elif talk[0] == 'DIVINED':
                    if talker not in self.divineMap.keys():
                        self.divineMap[talker] = dict()
                    self.divineMap[talker][talk[1]] = talk[2]
                elif talk[0] == 'IDENTIFIED':
                    if talker not in self.mediumMap.keys():
                        self.mediumMap[talker] = dict()
                    self.mediumMap[talker][talk[1]] = talk[2]
            elif diff_data.type[i] == 'execute':
                self.executedAgents.append(diff_data.agent[i])
                self.aliveOthers.remove(diff_data.agent[i])
            elif diff_data.type[i] == 'dead':
                self.killedAgents.append(diff_data.agent[i])
                self.aliveOthers.remove(diff_data.agent[i])

    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        # print("initialize")
        # print(base_info)
        # print(diff_data)

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
        self.mediumMap = dict()
        self.aliveOthers = list(range(1,16))
        self.aliveOthers.remove(self.my_id)
        self.divine_list = list(range(1,16))
        self.divine_list.remove(self.my_id)
        self.executedAgents = []
        self.killedAgents = []
        self.talkQueue = []
        self.whisperQueue = []
        self.humans = []
        self.werewolves = []

        self.finished = False

    def dayStart(self):
        self.day += 1
        self.vote_declare = 0
        self.talk_turn = 0
        return None

    def talk(self):
        self.talk_turn += 1
        # comingout
        if self.comingout == '':
            if self.my_role == 'SEER':
                self.comingout = 'SEER'
            elif self.my_role == 'MEDIUM':
                self.comingout = 'MEDIUM'
            elif self.my_role == 'POSSESSED':
                self.comingout = 'SEER'
            return cb.comingout(self.my_id, self.comingout)

        #report
        if self.not_reported:
            if self.my_role == 'SEER':
                self.not_reported = False
                return self.my_result
            elif self.my_role == 'MEDIUM':
                self.not_reported = False
                return self.my_result
            elif self.my_role == 'POSSESSED':
                self.not_reported = False
                agent = random.choice(self.aliveOthers)
                self.my_result = 'DIVINED Agent['+"{0:02d}".format(agent)+'] HUMAN'
                return self.my_result

        #declare vote
        if self.vote_declare != self.vote():
            self.vote_declare = self.vote()
            return cb.vote(self.vote_declare)

        #skip
        if self.talk_turn <= 10:
            return cb.skip()

        return cb.over()

    def whisper(self):
        return cb.over()

    def vote(self):
        return random.choice(self.aliveOthers)

    def attack(self):
        return random.choice(self.aliveOthers)

    def divine(self):
        list = self.and_list(self.aliveOthers, self.divine_list)
        if len(list) == 0:
            return self.my_id
        agent = random.choice(list)
        self.divine_list.remove(agent)
        return agent

    def guard(self):
        agent = random.choice(list(self.divineMap.keys()))
        if agent not in self.aliveOthers:
            agent = random.choice(self.aliveOthers)
        return agent

    def finish(self):
        if self.finished is True:
            return None
        self.finished = True

        return None

    def daily_finish(self):
        pass

    def diff_list(self, list1, list2):
        return list(set(list1) - set(list2))

    def and_list(self, list1, list2):
        return list(set(list1) & set(list2))

agent = Agent('Flex5')

# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
