# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy

import copy,random
import MyData_15 as md

class BaseVillager(object):

    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.myData     = md.MyData()
        self.role = 'VILLAGER'
        self.gameInfo   = None
        self.candidate  = []

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        self.gameInfo = game_info

    def initialize(self, game_info, game_setting):
        self.agentIdx   = game_info['agent']
        self.playerNum  = game_setting['playerNum']
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'
        self.gameInfo   = game_info

    def dayStart(self):
        self.candidate = self.myData.getAliveAgentIndexList()
        self.candidate.remove(self.agentIdx)
        self.willvote   = None



    def talk(self):
        return ttf.over()

    def vote(self):
        target      = self.myData.getMaxLikelyWolfAll()
        return target

    def finish(self):
        return None

    def setmyData(self,mydata):
        self.myData = mydata

    #客観的偽者調査
    def o_fakeSearch(self):

        #偽霊媒師探し
        mmap        = self.myData.getMediumCODataMap()
        mediumlist  = self.myData.getMediumCOAgentList()
        fakeMedium  = []

        if(self.playerNum>5):
            werewolves  = 3
        else:
            werewolves  = 1

        if (len(mediumlist) > 0):
            #人狼数以上の黒出し
            for k,v in mmap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #黒を人狼数以上出していたら偽者
                if(len(black) > werewolves):    fakeMedium.append(k)

                #生存者に結果を出していたら偽者
                result = v.getKnownList()
                for i in result.keys():
                    if(i in self.myData.getAliveAgentList()):
                        fakeMedium.append(k)

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        if(self.agentIdx in seerlist):
            seerlist.remove(self.agentIdx)
        fakeSeer    = []

        if(len(seerlist) > 0):
            #人狼数以上の黒出し
            for k,v in smap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #黒を人狼数以上出していたら偽者
                if(len(black) > werewolves):
                    if(not k in fakeSeer):  fakeSeer.append(k)

            #2日目以降、霊媒師1人のときに、霊媒と異なる結果であれば偽者
            if(len(mediumlist) == 1):
                for k,v in smap.items():
                    med_black   = v.getBlackList()
                    med_white   = v.getWhiteList()

                for k,v in smap.items():
                    black   = v.getBlackList()
                    white   = v.getWhiteList()

                    #占い師の白出しに霊媒師が黒を出していたら偽者
                    if(len(white) > 0):
                        for i in white:
                            if(i in med_black):
                                if(not k in fakeSeer):  fakeSeer.append(k)

                    #占い師の黒出しに霊媒師が白を出していたら偽者
                    if(len(black) > 0):
                        for i in white:
                            if(i in med_white):
                                if(not k in fakeSeer):  fakeSeer.append(k)

        return fakeMedium, fakeSeer

    #主観的偽者調査
    def s_fakeSearch(self,me):

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []

        if(len(seerlist) > 0):
            #自分に黒を出していたら偽者
            for k,v in smap.items():
                black   = v.getBlackList()
                if(me in black):    fakeSeer.append(k)

        return fakeSeer

    #ランダムで予想を言う
    def Estimate(self):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,3)
            if(rand > 1 and len(self.candidate) > 0):
                target = self.myData.getMaxLikelyWolf(self.candidate)
                if(target != None):
                    return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None):
                    return ttf.estimate(target,'VILLAGER')

        return None

    '''
    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y or not self.isDivined):
            rand = random.randint(1,3)
            if(rand > 1):
                target = self.MyTarget()
                return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None
    '''

    #talkやvoteのtargetを決める
    def Target(self,mylist):

        for i in mylist:
            if(not self.myData.isAliveIndex(i)):
                mylist.remove(i)

        if(len(mylist) > 0):
            if( self.willvote != self.myData.getMaxLikelyWolf(mylist) ):
                self.willvote = self.myData.getMaxLikelyWolf(mylist)
                return True

        return False


class BaseBodyguard(BaseVillager):

    def __init__(self, agent_name):
        super(BaseBodyguard,self).__init__(agent_name)
        self.role = 'BODYGUARD'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        return super(BaseBodyguard,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        return super(BaseBodyguard,self).initialize(game_info, game_setting)

    def dayStart(self):
        return super(BaseBodyguard,self).dayStart()

    def talk(self):
        return super(BaseBodyguard,self).talk()

    def vote(self):
        return super(BaseBodyguard,self).vote()

    def guard(self):
        if(len(self.candidate) > 0):
            target      = self.myData.getMaxLikelyVill(self.candidate)
            return target
        return self.agentIdx

    def finish(self):
        return super(BaseBodyguard,self).finish()

class BaseMedium(BaseVillager):

    def __init__(self, agent_name):
        super(BaseMedium,self).__init__(agent_name)
        self.role = 'MEDIUM'

        #白黒リスト
        self.myBlack    = []    #霊媒結果黒(人狼)だったプレイヤのリスト
        self.myWhite    = []    #霊媒結果白(人間)だったプレイヤのリスト
        self.willSay    = []    #まだいっていない霊媒結果のリスト

        #偽確定リスト
        self.fakeSeer   = []    #自分の霊媒結果と異なる結果を出した占い師
        self.fakeMedium = []    #自分以外に霊媒師COしたプレイヤ

        self.isCall     = False
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BaseMedium,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(BaseMedium,self).initialize(game_info, game_setting)
        self.isCO         = False

    def dayStart(self):
        super(BaseMedium,self).dayStart()

        if(self.gameInfo['day'] > 1):
            identified   = self.gameInfo['mediumResult']
            result      = identified['result']
            target      = identified['target']

            self.willSay.append(identified)

            if(result == 'HUMAN'):
                self.myWhite.append(target)
            else:
                self.myBlack.append(target)

    def talk(self):

        self.myfakeSearch()

        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger):
            self.isCO = True
            return ttf.comingout(self.agentIdx,self.Role)

        #COしたら言っていない結果がなくなるまでinquested
        if(self.isCO and len(self.willSay) > 0):
            target = self.willSay[0]['target']
            result = self.willSay[0]['result']
            self.willSay.pop()
            return ttf.identified(target,result)

        return None

    def vote(self):
        target      = self.myData.getMaxLikelyWolfAll()
        return target

    def finish(self):
        super(BaseMedium,self).finish()

    #COするか否か
    def COTrigger(self):
        #ひとまず2日目1ターン目CO
        #Trigger条件はここで調整可能。
        if(self.myData.getToday() > 1):
            return True
        else:
            return False

    #偽者調査
    def myfakeSearch(self):
        #偽霊媒師探し
        mediumlist = self.myData.getMediumCOAgentList()

        if self.agentIdx in mediumlist:   mediumlist.remove(self.agentIdx)
        if len(mediumlist) > 0:
            for i in mediumlist:
                if i not in self.fakeMedium:  self.fakeMedium.append(i)

        #偽占い師探し
        seer = self.myData.getSeerCODataMap()
        if(len(seer) > 0):
            for k,v in seer.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()
                #自分の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in self.myWhite and k not in self.fakeSeer):
                            self.fakeSeer.append(k)
                #自分の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in self.myBlack and k not in self.fakeSeer):
                            self.fakeSeer.append(k)
        return None

class BaseSeer(BaseVillager):

    def __init__(self, agent_name):
        super(BaseSeer,self).__init__(agent_name)
        self.role = 'SEER'
        self.willvote   = None

        #占い結果リスト
        self.myDivine   = []
        self.myBlack    = []    #自分の黒
        self.aliveBlack = []    #生存している黒
        self.myWhite    = []    #自分の白
        self.aliveWhite = []    #生存している白

        #偽確定リスト
        self.fakeSeer   = []
        self.fakeMedium = []
        self.isCall     = False
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        return super(BaseSeer,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(BaseSeer,self).initialize(game_info, game_setting)
        self.divineList = self.myData.getAliveAgentIndexList()
        self.divineList.remove(self.agentIdx)

    def dayStart(self):
        super(BaseSeer,self).dayStart()
        self.willvote   = None
        self.isCall = False
        divined  = self.gameInfo['divineResult']

        #占い結果処理
        if(divined != None):
            self.myDivine.append(divined)
            target  = divined['target']
            result  = divined['result']

            if(result == 'HUMAN'):
                self.myWhite.append(target)
                self.aliveWhite.append(target)
                if(target in self.candidate):  self.candidate.remove(target)
            else:
                self.myBlack.append(target)
                self.aliveBlack.append(target)
            if(target in self.divineList):  self.divineList.remove(target)

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.divineList):     self.divineList.remove(execute)
            if(execute in self.aliveBlack):     self.aliveBlack.remove(execute)
            if(execute in self.aliveWhite):     self.aliveWhite.remove(execute)
            if(execute in self.candidate):      self.candidate.remove(execute)
            if(attacked in self.divineList):    self.divineList.remove(attacked)
            if(attacked in self.aliveBlack):    self.aliveBlack.remove(attacked)
            if(attacked in self.aliveWhite):    self.aliveWhite.remove(attacked)
            if(attacked in self.candidate):     self.candidate.remove(attacked)

    def talk(self):

        self.myfakeSearch()
        #COTrigerがTrueならCO
        if((not self.isCO) and self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,self.Role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.myDivine) > 0):
            target = self.myDivine[0]['target']
            result = self.myDivine[0]['result']
            self.myDivine.pop()
            return ttf.divined(target,result)

        return super(BaseSeer,self).talk()

    def vote(self):

        #白出し以外から選択
        if(len(self.candidate) > 0):
            return self.myData.getMaxLikelyWolf(self.candidate)

        #例外対策
        return super(BaseSeer,self).vote()

    def divine(self):

        #未占いから選択
        if(len(self.divineList) > 0):
            target = self.myData.getMaxLikelyWolf(self.divineList)
            return int(target)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(BaseSeer,self).finish()

    #COするか否か
    def COTrigger(self):
        #初日1ターン目(確率0.9)
        if(self.myData.getToday() == 1 and self.myData.getTurn() == 0):
            rand = random.randint(1,10)
            if(rand < 10):  return True

        #黒を見つけたとき
        if(len(self.myBlack) > 0):  return True

        #他占い師COがあったとき
        seerlist    = self.myData.getSeerCOAgentList()
        if(len(seerlist) > 0):      return True

        #吊られそうなとき
        exe     = self.myData.getMaxLikelyExecuteAgentAll()
        if(exe == self.agentIdx):     return True

        #2日目は必ずCO
        if(self.myData.getToday == 2):  return True

        return False

    #偽者調査
    def myfakeSearch(self):

        #偽占い師探し
        seerlist = self.myData.getSeerCOAgentList()

        if self.agentIdx in seerlist:   seerlist.remove(self.agentIdx)
        if len(seerlist) > 0:
            for i in seerlist:
                if i not in self.fakeSeer:  self.fakeSeer.append(i)

        #偽霊媒探し
        medium = self.myData.getMediumCODataMap()
        if(len(medium) > 0):
            for k,v in medium.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()
                #自分の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in self.myWhite and i not in self.fakeMedium):
                            self.fakeMedium.append(k)
                #自分の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in self.myBlack and i not in self.fakeMedium):
                            self.fakeMedium.append(k)

    def setmyData(self,mydata):
        self.myData = mydata

class BaseWerewolf(BaseVillager):

    def __init__(self, agent_name):
        super(BaseWerewolf,self).__init__(agent_name)
        self.role = 'WEREWOLF'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BaseWerewolf,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(BaseWerewolf,self).initialize(game_info, game_setting)
        self.werewolves = []      #仲間の人狼
        self.villagers  = []      #村人たち
        self.aliveWerewolves = []      #仲間の人狼
        self.aliveVillagers  = []      #村人たち

        #自分以外の人狼リストの作成
        for k,v in game_info['roleMap'].items():
            if(self.agentIdx != int(k)):
                self.werewolves.append(int(k))

        #村人リストの作成
        self.villagers = [target for target in range(1, self.playerNum+1)]
        for i in self.werewolves:
            if (i in self.villagers):   self.villagers.remove(i)
        self.villagers.remove(self.agentIdx)

    def dayStart(self):
        super(BaseWerewolf,self).dayStart()

        #生存リストの更新
        self.aliveVillagers, self.aliveWerewolves = [], []
        for i in self.myData.getAliveAgentIndexList():
            if(i in self.villagers):
                self.aliveVillagers.append(i)
            if(i in self.werewolves):
                self.aliveWerewolves.append(i)

    def talk(self):
        return ttf.over()

    def whisper(self):
        return twf.over()

    def vote(self):

        #村人から最も狼らしい者(身内切りはしない)
        if(len(self.aliveVillagers) > 0):
            return self.myData.getMaxLikelyWolf(self.aliveVillagers)

        #例外対策
        return super(BaseWerewolf,self).vote()

    def attack(self):

        #村人から最も村人らしい者(役職狙い撃ちはしない)
        if(len(self.aliveVillagers) > 0):
            return self.myData.getMaxLikelyVill(self.aliveVillagers)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(BaseWerewolf,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

class BasePossessed(BaseVillager):

    def __init__(self, agent_name):
        super(BasePossessed,self).__init__(agent_name)
        self.Role = 'POSSESSED'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BasePossessed,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(BasePossessed,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(BasePossessed,self).dayStart()

    def talk(self):
        return super(BasePossessed,self).talk()

    def vote(self):
        return super(BasePossessed,self).vote()

    def finish(self):
        super(BasePossessed,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata
