#!/usr/bin/env python
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd

class COData(object):

    def __init__(self,day,turn,isCO):
        self.day_ = day
        self.turn_ = turn
        self.isCO_ = isCO
        self.isAfterSlide = False
        self.isAlive_ = True
        self.dayList_ = []
        self.knownList_ = []
        self.blackList = []
        self.whiteList = []
        self.result_dict = {}

    def getDay(self):
        return self.day_

    def getTurn(self):
        return self.turn_

    def getisCO(self):
        return self.isCO_

    def add(self,day,targetIdx, isBlack):
        self.dayList_.append(day)
        if (not targetIdx in self.result_dict.keys()):
            self.result_dict.update({targetIdx:isBlack})
        if (isBlack):
            self.blackList.append(targetIdx)
        else:
            self.whiteList.append(targetIdx)
        #	public abstract int getAgent(int day);
        #	public abstract HashMap<Integer, Boolean> getResult(int day);

    def getResultNum(self):
        return len(self.result_dict)

    def getBlackList(self):
        return self.blackList

    def getWhiteList(self):
        return self.whiteList

    def getDayList(self):
        return self.dayList_

    def getKnownList(self):
        return self.result_dict

    def isAlive(self):
        return self.isAlive_

    def setAlive(self,isAlive):
        self.isAlive_ = isAlive

    def isAfterSlide(self):
        return isAfterSlide_

    def setAfterSlide(self,isAfterSlide):
        self.isAfterSlide_ = isAfterSlide
