#!/usr/bin/env python
# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import scipy.sparse as sp
import sklearn,copy
import pandas as pd
import MyData_base as md
import copy

class Seer(object):





    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.Role       = 'SEER'
        self.myData     = md.MyData()
        self.gameInfo   = None
        self.targetList = []
        self.grayList = []
        self.whiteList = []
        self.black = None
        self.possessed = None

        self.divineFlag = 0

        self.talkList = []
        self.talkIdx = 0

        self.otherCOMap = {}
        self.divineMap = {}#{agentIdx:{targetIdx:'WEREWOLF'or'HUMAN'}}

        self.divined = False
    def initialize(self, game_info, game_setting):

        self.agentIdx   = game_info['agent']
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'
        self.gameInfo   = game_info


        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)
        self.targetList = self.myData.getAliveAgentIndexList()
        self.targetList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        self.gameInfo = game_info
        self.myData.update(game_info, talk_history)


        for i in range(len(talk_history)):
            talk    = talk_history[i]
            talks   = talk['text'].split()
            day = talk['day']
            agentIdx    = talk['agent']
            turn    = talk['turn']
            isBlack = False


            if(talks[0] == 'DIVINED'):
                if(day == 1):
                    if(turn < 2):
                        self.divineMap.update({agentIdx : {talks[1][7] : talks[2]}})
                        if(len(self.divineMap) == 1):
                            self.divineFlag = 1
                        elif(len(self.divineMap) == 2):
                            self.divineFlag = 2
                        else:
                            self.divineFlag = 3
        if self.myData.getToday() == 1 and self.divineFlag == 2:

            for seer in self.divineMap.keys():
                if(seer != self.agentIdx):
                    if(seer in self.grayList):
                        self.grayList.remove(seer)
                    self.whiteList.append(seer)
                    self.possessed = seer


            if 'divineResult' in game_info.keys() and self.divined is False:
                self.divined = True
                if self.gameInfo['divineResult'] is not None:
                    if(game_info['divineResult']['target'] in self.grayList):
                        self.grayList.remove(game_info['divineResult']['target'])
                    if self.gameInfo['divineResult']['result'] is 'WEREWOLF':
                        self.black = self.gameInfo['divineResult']['target']
                        self.whiteList = copy.deepcopy(self.grayList)
                        if(t in self.whiteList):
                            self.whiteList.remove(game_info['divineResult']['target'])
                        self.grayList = []
                    else:
                        self.whiteList.append(self.gameInfo['divineResult']['target'])

            seerA = None
            seerB = None
            for seer in self.divineMap.keys():
                if(seerA is None):
                    seerA = seer
                else:
                    seerB = seer


            if seerA == self.agentIdx:
                self.possessed = seerB
                self.whiteList.append(self.possessed)
            else:
                self.possessed = seerA
                self.whiteList.append(self.possessed)
        elif len(self.divineMap) == 3 and self.divined is False:
            self.divined = True




    def dayStart(self):
        self.talkList = []
        self.talkIdx = 0
        if self.myData.getToday() == 1:
            self.talkList.append(ttf.comingout(self.agentIdx, self.Role))
        if 'divineResult' in self.gameInfo.keys() and self.gameInfo['divineResult'] is not None:
            #print(int(self.gameInfo['divineResult']['target']))
            #print(self.gameInfo['divineResult']['result'])
            talk = ttf.divined(self.gameInfo['divineResult']['target'], self.gameInfo['divineResult']['result'])
            self.talkList.append(talk)
            if(self.gameInfo['divineResult']['result'] == 'WEREWOLF'):
                self.black = self.gameInfo['divineResult']['target']
                self.whiteList = self.myData.getAliveAgentIndexList()
                self.whiteList.remove(self.agentIdx)
                if(self.black in self.whiteList):
                    self.whiteList.remove(self.black)
            else:
                if(self.gameInfo['divineResult']['target'] not in self.whiteList):
                    self.whiteList.append(self.gameInfo['divineResult']['target'])
                if(self.gameInfo['divineResult']['target'] in self.grayList):
                    self.grayList.remove(self.gameInfo['divineResult']['target'])


        self.talkList.append(ttf.over())


    def talk(self):
        if(self.talkIdx < len(self.talkList)):
            t = self.talkList[self.talkIdx]
            self.talkIdx += 1
        else:
            t = ttf.skip()
        return t


    def vote(self):
        #print (self.Role + str(self.divineFlag))


        if self.black is not None:
            self.targetList = [self.black]
        else:
            self.targetList = self.grayList

        #print("seer" + str(self.agentIdx) + "'s target:" + str(self.targetList))
        if(len(self.targetList) == 0):
            self.targetList = self.myData.getAliveAgentIndexList()
            self.targetList.remove(self.agentIdx)
        return self.targetList[np.random.randint(len(self.targetList))]

    def divine(self):
        divineTargetList = []
        for agent in self.grayList:
            if agent in self.myData.getAliveAgentIndexList():
                divineTargetList.append(agent)
        if len(divineTargetList) == 0:
            divineTargetList = self.myData.getAliveAgentIndexList()
            divineTargetList.remove(self.agentIdx)
        return divineTargetList[np.random.randint(len(divineTargetList))]


    def finish(self):
        #print ("finish")
        pass
    def setmyData(self,mydata):
        self.myData = mydata
