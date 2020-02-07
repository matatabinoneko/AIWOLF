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

class Werewolf(object):






    def __init__(self, agent_name):
        #print("__init__")
        self.agent_name = agent_name
        self.Role       = 'WEREWOLF'
        self.myData     = md.MyData()
        self.gameInfo   = None
        self.targetList = []
        self.otherCOMap = {}
        self.divineFlag = 0
        self.trueSeer = None
        self.fakeSeerList = []
        self.possessed = None
        self.divineMap = {}#{agentIdx:{targetIdx:'WEREWOLF'or'HUMAN'}}

        self.turn = 0

    def initialize(self, game_info, game_setting):
        #print("initialize")
        self.agentIdx   = game_info['agent']
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'
        self.gameInfo   = game_info




    def update(self, game_info, talk_history, whisper_history, request):
        #print ("update day"+str(self.myData.getToday())+" turn"+str(self.turn) +" talk"+str(len(talk_history)))
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
                #print("DIVINED")
                #print(talks[1] + " " + talks[1][7] + " "+str(self.agentIdx))
                if((int)(talks[1][7]) == self.agentIdx):
                    if(talks[2] == "HUMAN") and agentIdx not in self.fakeSeerList:
                        #print("fake1:"+str(talk))
                        self.fakeSeerList.append(agentIdx)
                else:
                    if(talks[2] == "WEREWOLF" and agentIdx not in self.fakeSeerList):
                        #print("fake2:"+str(talk))
                        self.fakeSeerList.append(agentIdx)
                if(day == 1):
                    self.divineMap.update({agentIdx : {int(talks[1][7]) : talks[2]}})
                    if(turn < 2):
                        if(len(self.divineMap) == 1):
                            self.divineFlag = 1
                        elif(len(self.divineMap) == 2):
                            self.divineFlag = 2
                        else:
                            self.divineFlag = 3

        #print ("seerList:" + str(self.divineMap) + " fekeSeerList" + str(self.fakeSeerList))
        if(len(self.divineMap) - len(self.fakeSeerList) == 1):
            for seer in self.divineMap.keys():
                if seer not in self.fakeSeerList:
                    self.trueSeer = seer

    def dayStart(self):
        #print("dayStart")
        self.turn = 0

    def talk(self):
        #print ("talk day"+str(self.myData.getToday())+" turn"+str(self.turn))
        self.turn +=1
        return ttf.over()

    def vote(self):
        self.targetList = self.myData.getAliveAgentIndexList()

        self.targetList.remove(self.agentIdx)

        #print (self.Role + str(self.divineFlag))
        if(len(self.divineMap) == 1):
            for seer in self.divineMap.keys():
                self.trueSeer = seer
                if(seer in self.targetList):
                    self.targetList.remove(seer)
            for target in self.divineMap[self.trueSeer]:
                if self.divineMap[self.trueSeer][target] == "WEREWOLF":
                    if(target == self.agentIdx):
                        self.targetList = [self.trueSeer]
                    else:
                        self.possessed = self.trueSeer
                        self.trueSeer = None
                        self.targetList = [target]
                        break
                else:
                    if(target in self.targetList):
                        self.targetList.remove(target)


        elif(len(self.divineMap) == 2):
            #first day
            if(self.myData.getToday() == 1):
                #print(self.divineMap)
                #print(self.divineMap)
                #voteTable[resultA][targetA][resultB][targetB]
                voteTable = np.array([[[[-1, -1, -1, -1, -1],   [-1, -1, -1, -1, -1]],
                [[34, -1, 34, 34, 34],   [34, -1, 34, 3, 4]],
                [[34, -1, 34, 34, 34],   [34, -1, 34, 3, 4]],
                [[34, -1, 34, 4, 34],   [4, -1, 4, 3, 4]],
                [[34, -1, 34, 34, 3],   [3, -1, 3, 3, 4]]],
                [[[-1, -1, -1, -1, -1],   [-1, -1, -1, -1, -1]],
                [[34, -1, 34, 4, 3],   [-1, -1, 1, 3, 4]],
                [[34, -1, 34, 4, 3],   [0, -1, 1, 3, 4]],
                [[3, -1, 3, 3, 3],   [3, -1, 3, 3, 34]],
                [[4, -1, 4, 4, 4],   [4, -1, 4, 34, 4]]]])

                otherList = self.myData.getAgentIdxList()
                #print (otherList)
                if(self.agentIdx in otherList):
                    otherList.remove(self.agentIdx)
                #print (otherList)
                #print self.divineMap
                for seer in self.divineMap.keys():
                    if(seer in otherList):
                        otherList.remove(seer)

                seerA = None
                seerB = None
                for seer in self.divineMap.keys():
                    if(seerA is None):
                        seerA = seer
                    else:
                        seerB = seer
                print (otherList)

                indexMap = {seerA:0, seerB:1, self.agentIdx:2, otherList[0]:3, otherList[1]:4}
                indexList = [seerA, seerB, self.agentIdx, otherList[0], otherList[1]]

                self.targetList = []
                targetListA = self.divineMap[seerA].keys()
                targetListB = self.divineMap[seerB].keys()
                for target in targetListA:
                    targetA = target
                for target in targetListB:
                    targetB = target
                if self.divineMap[seerA][targetA] == 'HUMAN':
                    resultA = 0
                else:
                    resultA = 1
                if self.divineMap[seerB][targetB] == 'HUMAN':
                    resultB = 0
                else:
                    resultB = 1
                #print(indexMap)
                #print(resultA, int(targetA), resultB, int(targetB))
                #print(resultA,indexMap[int(targetA)],resultB,indexMap[int(targetB)])
                targetInt = voteTable[resultA][indexMap[int(targetA)]][resultB][indexMap[int(targetB)]]
                #print(targetInt)
                if targetInt  != -1:
                    if targetInt >= 10:
                        target1 = targetInt%10
                        #print(target)
                        target2 = (int)(targetInt/10)
                        #print(target1, target2)
                        self.targetList= [indexList[target1],indexList[target2]]
                    else:
                        #print(target)
                        self.targetList=[indexList[targetInt]]
                else:
                    #print("error werewolf vote -1")
                    self.targetList = self.myData.getAliveAgentIndexList()

                    self.targetList.remove(self.agentIdx)

            #second day
            else:
                for seer in self.divineMap.keys():
                    if seer in self.myData.getAliveAgentIndexList():
                        self.targetList = [seer]

        elif(len(self.divineMap) == 3):
            if(self.myData.getToday() ==1):
                if(len(self.fakeSeerList) >0):
                    self.targetList = self.fakeSeerList
                else:
                    whiteList = self.myData.getAliveAgentIndexList()
                    self.targetList = []
                    for seer in self.divineMap.keys():
                        self.targetList.append(seer)
                        if(seer in whiteList):
                            whiteList.remove(seer)

                    for seer in self.divineMap.keys():
                        for target in self.divineMap[seer].keys():
                            if(target in whiteList):
                                self.fakeSeerList.append(seer)

            else:
                if(self.trueSeer is not None):
                    self.targetList = []
                    for fakeSeer in self.fakeSeerList:
                        if(fakeSeer in self.myData.getAliveAgentList()):
                            self.targetList.append(fakeSeer)
                for seer in self.divineMap.keys():
                    for target in self.divineMap[seer].keys():
                        if(self.divineMap[seer][target] == "WEREWOLF" and target in self.myData.getAliveAgentIndexList() and seer not in self.fakeSeerList):
                            self.fakeSeerList.append(seer)
            if(len(self.fakeSeerList) == 2):
                self.targetList = self.fakeSeerList
                for seer in self.divineMap.keys():
                    if(seer not in self.fakeSeerList):
                        self.trueSeer = seer
            for target in self.targetList:
                if(target not in self.myData.getAliveAgentIndexList()):
                    self.targetList.remove(target)

        if len(self.targetList) > 0:
            #print("werewolf" + str(self.agentIdx) + "'s target:" + str(self.targetList))
            return self.targetList[np.random.randint(len(self.targetList))]
        else:
            #print("error:targetList is empty")

            #print("werewolf" + str(self.agentIdx) + "'s target:" + str(self.targetList))
            self.targetList = self.myData.getAliveAgentIndexList()
            self.targetList.remove(self.agentIdx)
            return self.targetList[np.random.randint(len(self.targetList))]


    def whisper(self):
        return ttf.over()

    def attack(self):

        attackTargetList = self.myData.getAliveAgentIndexList()
        executed = self.gameInfo["latestExecutedAgent"]
        #print(executed,attackTargetList)
        if(self.agentIdx in attackTargetList):
            attackTargetList.remove(self.agentIdx)
        if(executed in attackTargetList):
            attackTargetList.remove(executed)

        if(self.divineFlag == 1):
            if(self.myData.getToday() == 1):
                for seer in self.divineMap.keys():
                    attackTargetList = [seer]
        elif(self.divineFlag == 2):
            if(self.myData.getToday() == 1):
                for seer in self.divineMap.keys():
                    if(seer in attackTargetList):
                        attackTargetList.remove(seer)

        elif(self.divineFlag == 3):
            if(self.myData.getToday() == 1):
                for seer in self.divineMap.keys():

                    if seer in attackTargetList:
                        attackTargetList.remove(seer)

                    for target in self.divineMap[seer].keys():
                        if self.divineMap[seer][target] == "HUMAN":
                            attackTargetList = [target]
                        else:
                            if(target in attackTargetList):
                                attackTargetList.remove(target)

        if(len(attackTargetList) == 0):
            attackTargetList = self.myData.getAliveAgentIndexList()
            if(self.agentIdx in attackTargetList):
                attackTargetList.remove(self.agentIdx)
            if(executed in attackTargetList):
                attackTargetList.remove(executed)


        return attackTargetList[np.random.randint(len(attackTargetList))]
    def finish(self):
        pass
    def setmyData(self,mydata):
        self.myData = mydata
