import numpy as np
import reliability_tensor
import pandas as pd

import keras_predictor_v2 as kp

predictor15 = kp.Predictor15()
predictor5 = kp.Predictor5()


class Predicter(object):
    """
    いろんな確率を考える予測機
    合議で得た和を得る
    #すべての予測は(1,player_size)で返し、和は１とする
    #（つまり信頼できる確率とかを返す）
    """

    def __init__(self, players_name):
        self.relationship = reliability_tensor.ReliabilityTensor(players_name)
        self.player_size = len(players_name)
        print("RELATIONSHIP TENSOR SUCCESSFULLY GENERATED")
        # print(self.relationship.table["DIVINED"])

        # 各役職識別機の読み込み（インスタンス作成）
        if len(players_name) == 15:
            self.predictor = predictor15
        elif len(players_name) == 5:
            self.predictor = predictor5
        else:
            print("Something wrong in predicter __init__")

    def update(self, diff_data):
        """
        Pandas形式でdiff_dataを受取り
        ReliabilityTensorにそれを反映する
        """
        for i in diff_data.iterrows():
            # print(i)
            # print("IDX,TEXT", i["idx"], i["text"])
            self.relationship.talk_parser(i[1])
        # print(self.relationship.table["OTHER"])

    def role_pred(self, role):
        """
        狼可能性
        print(self.relationship.table["CO"])
        print(self.relationship.table["DIVINED"])
        print(self.relationship.table["OTHER"])
        print(self.relationship.table["ATTACKED"])
        """
        #ret = [[0, 0, 0, 0, 0]]
        ret = self.predictor.predict(role, self.relationship.table)
        #print("WOLF PRED:")
        # print(ret[0])
        return ret[0]

    def role_est(self, role, still_alive):
        """
        最もroleらしいやつのIDを返す
        """
        p = self.role_pred(role)
        #print("Role_est returned:", p)
        return self.highest(still_alive, p)

    def highest(self, still_alive, p):
        import random
        """
        still_alive内で最もpが高い奴を返す
        """
        t = []
        for i in range(len(p)):
            if int(i+1) not in still_alive:
                continue
            t.append((p[i], int(i + 1)))
        #print("T:", t)
        # print(still_alive)
        # print(p)
        try:
            ret = sorted(t, reverse=True)[0][1]
        except Exception:
            try:
                ret = random.choice(still_alive)
                #print("ERROR: not going well in highest, ret is chosen randomly.")
            except Exception:
                ret = 1
                #print("ERROR: wrong in highest and failed random choice, 1 returned.")
        return ret

    def role_least(self, role, still_alive):
        """
        最もroleらしくないやつのIDを返す
        """
        p = self.role_pred(role)
        #print("Role_least returned:", p)
        return self.lowest(still_alive, p)

    def lowest(self, still_alive, p):
        import random
        """
        still_alive内で最もpが低い奴を返す
        """
        t = []
        for i in range(len(p)):
            if int(i+1) not in still_alive:
                continue
            t.append((p[i], int(i+1)))
        try:
            ret = sorted(t, reverse=False)[0][1]
        except Exception:
            ret = random.choice(still_alive)
            #print("ERROR: not going well in highest, ret is chosen randomly.")
        return ret
