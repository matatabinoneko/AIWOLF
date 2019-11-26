#!/usr/bin/env python
from __future__ import print_function, division

import random

import aiwolfpy
import aiwolfpy.contentbuilder as cb

import m5learn

import t5460
import evaluateagents

myname = 'TRKOkami'


class SampleAgent(object):

    def __init__(self, agent_name):
        self.myname = agent_name

        self.learn5 = m5learn.m5learn(5)
        self.learn15 = m5learn.m5learn(15)
        self.learn = None

        self.ebal = None
        self.eval5 = evaluateagents.EvaluateAgents(5)
        self.eval15 = evaluateagents.EvaluateAgents(15)

        self.greys = set()

        self.gamecount = 0

    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):

        self.base_info = base_info
        self.game_setting = game_setting
        self.myrole = base_info["myRole"]
        self.myID = self.base_info["agentIdx"]
        self.result_seer = []
        self.result_med = []
        self.player_size = len(self.base_info["remainTalkMap"].keys())
        self.check_alive()
        self.talk_turn = 0
        self.honest = False
        self.divined_as_wolf = set()
        self.divined_as_human = set()
        self.wrong_divine = set()
        self.black = set()
        self.white = set()
        self.seers = set()
        self.PPtry = set()
        self.black_to_seer = set()
        self.greys = set(self.alive)-{int(base_info["agentIdx"])}
        self.players = self.greys.copy()
        self.gamecount += 1
        self.whisper_turn = 0
        self.attack_success = True
        self.attacked_who_lastnight = 0

        if self.learn == None:
            if self.player_size == 5:
                self.learn = self.learn5
                self.eval = self.eval5
            elif self.player_size == 15:
                self.learn = self.learn15
                self.eval = self.eval15
        if self.player_size == 15:
            self.tpred = t5460.t5460(15)

        self.learn.start()
        self.eval.gameInitialize()

    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        self.diff_data = diff_data
        self.check_alive()

        self.learn.update_from_diff(
            diff_data, base_info["day"], self.talk_turn)
        self.eval.update_from_diff(diff_data)
        if self.player_size == 15:
            self.tpred.update_from_diff(diff_data, base_info)

        for i in self.diff_data.iterrows():
            content = i[1]["text"].split()
            if content[0] == "COMINGOUT":
                if int(content[1][6:8]) > self.player_size:
                    return
                if content[2] in ["VILLAGER"]:
                    return
                if content[2] in ["POSSESSED", "WEREWOLF"]:
                    self.PPtry.add(int(content[1][6:8]))
                    return
                if content[2] == "SEER":
                    self.seers.add(int(content[1][6:8]))
                self.greys -= {int(content[1][6:8])}

            if content[0] == "DIVINED":
                if int(content[1][6:8]) > self.player_size:
                    return
                self.greys -= {int(content[1][6:8])}
                if content[2] == "HUMAN":
                    self.white.add(int(content[1][6:8]))
                    if self.base_info["day"] == 1 and int(content[1][6:8]) == self.myID and self.myrole == "WEREWOLF":
                        self.wrong_divine.add(int(i[1]["agent"]))
                else:
                    if self.base_info["day"] == 1 and self.player_size == 5 and int(content[1][6:8]) != self.myID and self.myrole == "WEREWOLF":
                        self.wrong_divine.add(int(i[1]["agent"]))
                    if len(self.seers) == 2 and int(content[1][6:8]) in self.seers:
                        self.black_to_seer.add(int(i[1]["agent"]))
                    else:
                        self.black.add(int(content[1][6:8]))

                if self.base_info["day"] == 1:
                    self.seers.add(int(i[1]["agent"]))

                if request == "DAILY_INITIALIZE":
                    if content[2] == "WEREWOLF":
                        self.divined_as_wolf.add(int(content[1][6:8]))
                    elif content[2] == "HUMAN":
                        self.divined_as_human.add(int(content[1][6:8]))
        if request == "DAILY_INITIALIZE":
            for i in range(diff_data.shape[0]):
                if diff_data["type"][i] == "divine":
                    self.result_seer.append(diff_data["text"][i])
                    self.greys -= {int(diff_data["text"][i][14:16])}
                if diff_data["type"][i] == "identify":
                    self.result_med.append(diff_data["text"][i])

                if diff_data["type"][i] == "attack":
                    self.attacked_who_lastnight = diff_data["agent"][i]

    def dayStart(self):
        self.talk_turn = 0
        self.whisper_turn = 0

        self.check_alive()

        if self.player_size == 15:
            self.tpred.call_morning()
        return None

    def grey_random(self):
        """
        グレラン
        """
        if len(self.greys) == 0:
            return int(random.choice(list(self.alive_without_me)))
        t = int(random.choice(list(self.greys)))
        return t

    def check_alive(self):
        """
        生存者の確認
        """
        self.alive = []
        for i in range(self.player_size):
            i += 1
            if self.base_info["statusMap"][str(i)] == "ALIVE":
                self.alive.append(int(i))

        self.alive_without_me = list(
            set(self.alive) - {int(self.myID)})

        self.greys = self.greys & set(self.alive_without_me)

    def binjo(self):
        for i in self.diff_data.iloc[::-1].iterrows():
            content = i[1]["text"].split()
            if content[0] == "VOTE":
                if int(content[1][6:8]) in self.greys:
                    return cb.agree("TALK", int(self.base_info["day"]), int(i[1]["idx"]))
        return cb.skip()

    def talk(self):
        self.talk_turn += 1

        if self.myrole == "WEREWOLF":
            if self.player_size == 5 and self.base_info["day"] == 2 and self.talk_turn == 1:
                return cb.comingout(self.myID, "POSSESSED")
            if self.talk_turn == 2:
                return self.binjo()
            if self.gamecount < 500 or self.player_size == 15:
                return cb.over()
            elif self.base_info["day"] == 1 and self.talk_turn == 1:
                if len(self.greys) > 0:
                    return cb.divined(self.eval.highest(self.greys), "WEREWOLF")
                return cb.divined(self.grey_random(), "WEREWOLF")

        if self.myrole == "SEER":

            if self.base_info["day"] == 1:

                if self.talk_turn == 1:
                    return cb.comingout(self.myID, "SEER")
                if self.talk_turn == 2:

                    if len(self.divined_as_wolf) > 0:
                        if len(self.result_seer) > 0:
                            return self.result_seer.pop(0)
                    elif len(self.greys) > 0:
                        return cb.divined(self.eval.lowest(self.greys), "WEREWOLF")
                    else:
                        return cb.divined(self.grey_random(), "WEREWOLF")

            else:
                if len(self.result_seer) != 0:
                    return self.result_seer.pop(0)

        if self.myrole == "MEDIUM":
            if self.base_info["day"] == 2 and self.talk_turn == 2:
                return cb.comingout(self.myID, "MEDIUM")
            if self.talk_turn == 1:
                if len(self.result_med) > 0:
                    return self.result_med.pop(0)

        if self.myrole == "POSSESSED":
            if self.base_info["day"] == 1:
                if self.talk_turn == 1:
                    return cb.comingout(self.myID, "SEER")
                if self.talk_turn == 2:
                    if len(self.seers) == 2:
                        return cb.divined(list(self.seers - {self.myID})[0], "WEREWOLF")
            else:
                if self.talk_turn == 1:
                    if self.base_info["day"] < 3:
                        if len(self.greys) > 0:
                            return cb.divined(self.eval.highest(self.greys), "WEREWOLF")
                        return cb.divined(self.grey_random(), "WEREWOLF")
                    else:
                        if len(self.greys) > 0:
                            return cb.divined(self.eval.lowest(self.greys), "HUMAN")
                        return cb.divined(self.grey_random(), "HUMAN")
            if self.talk_turn == 3:
                return self.binjo()

        if self.talk_turn == 3:
            return cb.vote(self.vote())

        if self.talk_turn in [4, 5] and random.random() < 0.5:
            return self.binjo()

        return cb.over()

    def whisper(self):
        self.whisper_turn += 1

        if self.base_info["day"] == 0:
            if self.whisper_turn == 1:
                return cb.comingout(self.myID, "VILLAGER")
            else:
                return cb.over()

        COs = self.seers & set(self.alive_without_me)
        non_COs = set(self.alive_without_me) - self.seers
        if self.base_info["day"] > 0:
            if self.whisper_turn == 1:
                if len(COs) > 1:
                    return cb.estimate(self.learn.role_least("POSSESSED", COs), "POSSESSED")
                else:
                    return cb.attack(self.vote())
        return cb.over()

    def vote(self):
        """
        学習装置を使ってみる
        """
        if self.player_size == 15:
            self.tpred.call_evening()

        COs = self.seers & set(self.alive_without_me)-self.divined_as_human
        non_COs = set(self.alive_without_me) - \
            self.seers-self.divined_as_human

        if len(self.divined_as_wolf & set(self.alive)) > 0:
            return self.learn.role_est("WEREWOLF", self.divined_as_wolf & set(self.alive))

        if self.myrole in ["VILLAGER", "SEER"]:
            if len(self.seers) < 3:
                if len(self.black & set(self.alive_without_me)) == 1:
                    return self.learn.role_est("WEREWOLF", self.black & set(self.alive_without_me))

                if len(non_COs) > 0:
                    if self.player_size == 15:
                        return self.tpred.wolf_est(non_COs)
                    return self.learn.role_est("WEREWOLF", non_COs)
            else:
                if len(COs) > 0:
                    if self.player_size == 15:
                        return self.tpred.wolf_est(COs)
                    return self.learn.role_est("WEREWOLF", COs)

        if self.myrole in ["POSSESSED", "WEREWOLF"]:

            # if possible, remove wrong divined seer from candidate.
            if len(COs - self.wrong_divine) > 0:
                COs = COs - self.wrong_divine
            if len(non_COs - self.wrong_divine) > 0:
                non_COs = non_COs - self.wrong_divine

            # if possible, remove PPtry from candidate.
            if len(COs - self.PPtry) > 0:
                COs = COs - self.PPtry
            if len(non_COs - self.PPtry) > 0:
                non_COs = non_COs - self.PPtry

            # if possible, remove black to seer from candidate
            if len(COs - self.black_to_seer) > 0:
                COs = COs - self.black_to_seer

            if len(self.seers) == 1:
                if self.myrole == "WEREWOLF" and len(COs) > 0:
                    return self.learn.role_least("POSSESSED", COs)

            if len(self.seers) == 2:
                if self.base_info["day"] == 1:
                    if self.myrole == "POSSESSED" and len(COs) > 0:
                        return self.learn.role_least("WEREWOLF", COs)
                    if self.myrole == "WEREWOLF" and len(non_COs) > 0:
                        return self.eval.highest(non_COs)

                if self.myrole == "POSSESSED" and len(COs) > 0:
                    return self.learn.role_least("WEREWOLF", COs)
                if self.myrole == "WEREWOLF" and len(COs) > 0:
                    return self.eval.highest(COs)

            if len(self.seers) >= 3:
                if self.myrole == "POSSESSED" and len(non_COs) > 0:
                    return self.learn.role_least("WEREWOLF", non_COs)
                if self.myrole == "WEREWOLF" and len(non_COs) > 0:
                    return self.learn.role_least("POSSESSED", non_COs)
            cand = set(self.alive_without_me) - self.wrong_divine
            if len(cand) > 0:
                return self.learn.role_least("WEREWOLF", cand)
            return self.learn.role_least("WEREWOLF", self.alive_without_me)
        cand = list(set(self.alive_without_me)-self.divined_as_human)
        if self.player_size == 15:
            return self.tpred.wolf_est(cand)
        return self.learn.role_est("WEREWOLF", cand)

    def attack(self):
        """
        可能な限りCO者以外の白もらいを
        """
        COs = self.seers & set(self.alive_without_me) - \
            {self.attacked_who_lastnight}
        white_cand = set(self.alive_without_me) & self.white - \
            self.seers-{self.attacked_who_lastnight}
        non_COs = set(self.alive_without_me) - self.seers - \
            {self.attacked_who_lastnight}

        if len(white_cand) > 0:
            return self.eval.highest(white_cand)
        if len(non_COs) > 0:
            return self.eval.highest(non_COs)
        if len(COs) > 0:
            return self.eval.highest(COs)
        return self.grey_random()

    def divine(self):
        """
        可能な限りグレーから
        """
        self.check_alive()
        if len(self.greys) > 0:
            return self.eval.highest(self.greys)
        return self.grey_random()

    def guard(self):
        """
        可能な限りSEERっぽいやつ
        """
        return self.learn.role_est("SEER", self.alive_without_me)

    def finish(self):
        self.learn.finish()
        return None


agent = SampleAgent(myname)


if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
