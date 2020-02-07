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
    def __init__(self, agent_name):
        self.agent_name = agent_name

    def getName(self):
        return self.agent_name

    def update(self, base_info, diff_data, request):
        pass

    def initialize(self, base_info, diff_data, game_setting):
        self.my_id = base_info['agentIdx']

    def dayStart(self):
        pass

    def talk(self):
        return cb.over()

    def whisper(self):
        return cb.over()

    def vote(self):
        return self.my_id

    def attack(self):
        return self.my_id

    def divine(self):
        return self.my_id

    def guard(self):
        return self.my_id

    def finish(self):
        return None

    def daily_finish(self):
        pass

agent = Agent('Flex5')

# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
