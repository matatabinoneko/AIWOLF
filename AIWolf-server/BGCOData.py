#!/usr/bin/env python
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd
import COData


class BGCOData(COData.COData):

    def __init__(self,day, turn,isCO):
        super(BGCOData,self).__init__(day,turn,isCO)
        self.guardList  = []

    def getAgent(self):
        gnow = self.getKnownList()
        if (len(gnow) > day):
           return gnow.get[day]
        return -1

    def isFullResult(self,day):
        if (len(self.guardList) != day - 1):
            return False
        return True

    def add(self,targetIdx):
        self.guardList.append(targetIdx)

    def getResultNum(self):
        return len(self.guardList)

    def getGuardList(self):
        return self.guardList

    def setGuardList(self,guardList):
        self.guardList = guardList
