#!/usr/bin/env python
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import sys
import time
import flex5_pd_learning
import flex15agent
import flex15agent_no

class FlexAgent(object):
    agent = None

    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.agent5 = flex5_pd_learning.Agent('flex')
        self.agent15 = flex15agent_no.Agent('flex')
        self.agentX = flex15agent.Agent('flex')

    def getName(self):
        return self.agent_name

    def update(self, base_info, diff_data, request):
        self.agent.update(base_info, diff_data, request)

    def initialize(self, base_info, diff_data, game_setting):
        if int(game_setting['playerNum']) == 5:
            self.agent = self.agent5
        elif int(game_setting['playerNum']) == 15:
            self.agent = self.agent15
        else:
            self.agent = self.agentX
        self.agent.initialize(base_info, diff_data, game_setting)

    def dayStart(self):
        return self.agent.dayStart()

    def talk(self):
        return self.agent.talk()

    def whisper(self):
        return self.agent.whisper()

    def vote(self):
        return self.agent.vote()

    def attack(self):
        return self.agent.attack()

    def divine(self):
        return self.agent.divine()

    def guard(self):
        return self.agent.guard()

    def daily_finish(self):
        return self.agent.daily_finish()

    def finish(self):
        return self.agent.finish()


connect_agent = FlexAgent('flex')



# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(connect_agent)
