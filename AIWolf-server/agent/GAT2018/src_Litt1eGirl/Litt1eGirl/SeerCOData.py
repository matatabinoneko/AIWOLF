# -*- coding: utf-8 -*
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import COData as co

class SeerCOData(co.COData):
    def __init__(self,day,turn,isCO):
        super(SeerCOData,self).__init__(day,turn,isCO)

    def getAgent(self):
        gnow = self.getKnownList()
        if (len(gnow) > day):
           return gnow.get[day]
        return -1

    def getResult(self,day):
        for i in range(0,len(self.getDayList())):
            if (self.getDayList()[i] == day ):
                result = {}
                result.update({self.getTargetResultList()[i]:self.getIsBlackList()[i]})
                return result
        return None

    def isFullResult(self,day):
        if (len(self.getIsBlackList()) != day):
            return False
        return True
