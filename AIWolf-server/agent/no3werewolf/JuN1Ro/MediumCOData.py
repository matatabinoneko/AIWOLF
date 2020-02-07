# -*- coding: utf-8 -*
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import scipy.sparse as sp
import sklearn
import pandas as pd
import COData

class MediumCOData(COData.COData):

    def __init__(self,day,turn,isCO):
        super(MediumCOData,self).__init__(day,turn,isCO)

    def getAgent(self,day):
        if (len(self.getKnownList()) < day):
            return -1
        return self.getKnownList().get(day)

    def getResult(self,day):
        for i in range(len(self.getDayList())):
            if (self.getDayList().get(i) == day ):
                result = dict()
                result.update({self.getKnownList().get(i):self.getIsBlackList().get(i)})
                return result
        return None

    def isFullResult(self,day):
        if (len(self.getIsBlackList()) != day):
            return False
        return True
