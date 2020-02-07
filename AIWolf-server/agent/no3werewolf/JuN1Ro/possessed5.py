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

class Possessed(object):





    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.Role       = 'POSSESSED'
        self.myData     = md.MyData()
        self.gameInfo   = None
        self.targetList = []
        self.seerList = []
        self.otherSeerList = []
        self.grayList = []
        self.whiteList = []
        self.black = None
        self.trueSeer = None
        self.seerCOFlag = 0

        self.talkList = []
        self.talkIdx = 0

        self.turn = 0

    def initialize(self, game_info, game_setting):
        #print ("initialize")

        self.agentIdx   = game_info['agent']
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'
        self.gameInfo   = game_info

        self.otherCOMap = {}
        self.seerCOFlag = 0
        self.divineFlag = 0
        self.divineMap = {}#{agentIdx:{targetIdx:'WEREWOLF'or'HUMAN'}}
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)
        self.targetList = self.myData.getAliveAgentIndexList()
        self.targetList.remove(self.agentIdx)
        self.talkdiv = False
    def update(self, game_info, talk_history, whisper_history, request):
        #print "update"
        self.gameInfo = game_info
        self.myData.update(game_info, talk_history)
        self.divined = False


        for i in range(len(talk_history)):
            talk    = talk_history[i]
            talks   = talk['text'].split()
            day = talk['day']
            agentIdx    = talk['agent']
            turn    = talk['turn']
            isBlack = False
            #print ("talk process day " + str(talk))

            #CO
            if (talks[0]== 'COMINGOUT'):
                if (talks[2] == 'SEER'):
                    if(day == 1):
                        self.seerList.append(agentIdx)
                        if(turn == 0):
                            if(len(self.seerList) == 1):
                                self.seerCOFlag = 1
                            elif(len(self.seerList) == 2):
                                self.seerCOFlag = 2
                            else:
                                self.seerCOFlag = 3

                else:
                    self.otherCOMap.update({agentIdx : talks[2]})

            elif(talks[0] == 'DIVINED'):
                #print("DIVINED")

                if(day == 1):
                    divine = {agentIdx : {int(talks[1][7]) : talks[2]}}
                    self.divineMap.update(divine)
                    if(turn < 2):
                        if(len(self.divineMap) == 1):
                            self.divineFlag = 1
                        elif(len(self.divineMap) == 2):
                            self.divineFlag = 2
                        else:
                            self.divineFlag = 3

        if self.myData.getToday() == 1:
            #print ( self.seerList)
            #print ("self.turn is " + str(self.turn) + ", seer is " + str(len(self.seerList)))
            if self.turn ==1:
                #print ("day 1 self.turn 0")
                if self.seerCOFlag == 1:
                    pass

                elif self.seerCOFlag == 2:
                    #print ("seerList 2")
                    for seer in self.seerList:
                        if(seer != self.agentIdx):
                            if(seer in self.grayList):
                                self.grayList.remove(seer)

                elif self.seerCOFlag == 3:
                    for agent in self.grayList:
                        if agent not in self.divineMap.keys():
                            self.grayList.remove(agent)
                            if(agent not in self.whiteList):
                                self.whiteList.append(agent)


                if(self.talkdiv is False):
                    self.talkdiv = True
                else:
                    self.talkList.pop()
                    self.talkList.pop()
                self.talkList.append(ttf.divined(self.grayList[np.random.randint(len(self.grayList))], "WEREWOLF"))
                self.talkList.append(ttf.over())

            elif self.turn > 1:
                if(len(self.divineMap) == 1):
                    pass

                elif(len(self.divineMap) == 2):
                    if self.divined is False:
                        self.divined = True
                        #print(self.divineMap)
                        #print(self.trueSeer)
                        #print(self.seerList)
                        for seer in self.divineMap.keys():
                            if(self.agentIdx != seer):
                                self.trueSeer = seer
                        if(self.trueSeer is not None):

                            for t in self.divineMap[self.trueSeer].keys():
                                if self.divineMap[self.trueSeer][t] == "WEREWOLF":
                                    self.black = t
                                    self.whiteList = self.myData.getAliveAgentIndexList()
                                    if(self.black in self.whiteList):
                                        self.whiteList.remove(self.black)
                                    self.grayList = []
                                else:
                                    if(t not in self.whiteList):
                                        self.whiteList.append(t)
                                    if(t in self.grayList):
                                        self.grayList.remove(t)


                elif(len(self.divineMap) == 3):
                    for seer in self.divineMap.keys():
                        if(seer != self.agentIdx):
                            for target in self.divineMap[seer].keys():
                                if(self.divineMap[seer][target] == "WEREWOLF" and target not in self.divineMap.keys()):
                                    self.black = seer
                                    self.grayList = []
                                    self.whiteList = self.myData.getAliveAgentIndexList()
                                    if(self.black in self.whiteList):
                                        self.whiteList.remove(self.black)

                                    for s in self.divineMap.keys():
                                        if(s != self.agentIdx and s != self.black):
                                            self.trueSeer = s
                                    if(self.trueSeer in self.whiteList):
                                        self.whiteList.remove(self.trueSeer)





        self.otherSeerList = copy.deepcopy(self.seerList)
        if self.agentIdx in self.otherSeerList:
            self.otherSeerList.remove(self.agentIdx)




    def dayStart(self):
        #print ("daystart")
        self.talkList = []
        self.talkIdx = 0
        self.talkdiv = False
        if self.myData.getToday() == 1:
            self.talkList.append(ttf.comingout(self.agentIdx, "SEER"))
        elif self.myData.getToday() == 2:
            if self.black is not None:
                self.talkList.append(ttf.divined(self.black, "HUMAN"))
            else:
                if(len(self.grayList) >0):
                    self.talkList.append(ttf.divined(self.grayList[np.random.randint(len(self.grayList))], "WEREWOLF"))

            self.talkList.append(ttf.over())
        else:
            self.talkList.append(ttf.over())
        self.turn = 0

    def talk(self):
        #print "talk"
        if(self.talkIdx >= len(self.talkList)):
            return ttf.skip()
        #print self.myData.getToday(), self.turn
        #print self.talkList
        t = self.talkList[self.talkIdx]
        self.talkIdx += 1
        self.turn += 1
        return t


    def vote(self):
        #print ("vote")
        self.targetList = self.myData.getAliveAgentIndexList()
        self.targetList.remove(self.agentIdx)
        #print (self.Role + str(self.divineFlag))
        #print "day:",self.myData.getToday()," divineFlag:", self.divineFlag
        if(self.divineFlag == 1):
            if(self.myData.getToday() ==1):
                for target in self.divineMap[self.agentIdx].keys():

                    self.targetList = [target]


        elif(self.divineFlag == 2):
            #print (self.myData.getToday())
            if(self.myData.getToday() == 1):
                #print("day1")
                for seer in self.divineMap.keys():
                    if(self.agentIdx != seer):
                        self.trueSeer = seer
                self.targetList = [self.trueSeer]
                #print(self.targetList)
            else:

                #print("not day1")
                self.targetList = []
                for seer in self.seerList:
                    if(seer in self.myData.getAliveAgentIndexList()):
                        self.targetList.append(seer)
                #print(self.targetList)

        elif(self.divineFlag == 3):
            if(self.myData.getToday() == 1):
                self.targetList = self.myData.getAliveAgentIndexList()
                self.targetList.remove(self.agentIdx)

                for target in self.targetList:
                    if target in self.divineMap.keys():
                        self.targetList.remove(target)
            else:
                self.targetList = self.myData.getAliveAgentIndexList()
                self.targetList.remove(self.agentIdx)
                if(self.trueSeer is not None):
                    #print "trueSeer = ", self.trueSeer
                    self.targetList = [self.trueSeer]


        if(len(self.otherCOMap) > 0):
            for agent in self.otherCOMap.keys():
                if(self.otherCOMap[agent] == "WEREWOLF" and self.otherCOMap[agent] in self.targetList) and self.targetList > 1:

                    self.targetList.remove(agent)
        if(len(self.targetList) == 0):
            self.targetList = self.myData.getAliveAgentIndexList()
            self.targetList.remove(self.agentIdx)


        #print("possessed" + str(self.agentIdx) + "'s target:" + str(self.targetList))
        target = self.targetList[np.random.randint(len(self.targetList))]
        #print target
        return target


    def finish(self):
        #print ("finish")
        pass
    def setmyData(self,mydata):

        #print ("setmyData")
        self.myData = mydata
