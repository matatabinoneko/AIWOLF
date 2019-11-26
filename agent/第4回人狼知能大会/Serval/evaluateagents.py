import pandas as pd
import numpy as np
import itertools


class EvaluateAgents(object):
    """
    誰からやっても良い状況では優秀そうな（ポンコツそうな）エージェントから排除したい
    マッチ開始時にインスタンスを作成し、
    ゲームスタート時とアップデート時に呼べばOK    
    """

    def __init__(self, player_num):
        self.player_num = player_num
        self.df = pd.DataFrame(np.array([[0], [1e-4]])*np.ones((2, player_num)), index=[
                               "correct", "total"], columns=[x+1 for x in range(player_num)])

    def gameInitialize(self,):
        """
        ゲーム開始時に呼ぶ
        投票履歴辞書の初期化
        """
        self.dic = dict(zip([x+1 for x in range(self.player_num)], [[]
                                                                    for x in range(self.player_num)]))

    def add_dict(self, who, to):
        """
        whoがtoに投票
        int型で送る事
        """
        if type(who) != int or type(to) != int or who < 1 or who > self.player_num or to < 1 or to > self.player_num:
            #print("add error")
            return
        self.dic[who].append(to)
        return

    def finish(self, humans, wolfs, possesseds):
        """
        ゲーム終了時にそのゲームの投票記録と役職から
        エージェント評価値を更新
        """

        # 役職情報を取得
        #humans = set([4, 5, 6])
        #wolfs = set([1, 2, 3])
        #print("finish called")
        #print(humans, wolfs, possesseds, self.dic)

        for who, votes in self.dic.items():
            # 狼陣営は無視
            if who in wolfs:
                continue
            if who in possesseds:
                continue
            # print(who)
            for vote in votes:
                self.df[who]["total"] += 1
                if vote in wolfs:
                    self.df[who]["correct"] += 1
        return self.df

    def evaluate(self):
        """
        正答率をリストで返す
        """
        t = np.array(self.df)
        return t[0]/t[1]

    def highest(self, cand):
        """
        候補者の中で最も高いやつ
        """
        import random
        p = self.evaluate()
        t = []
        for i in range(len(p)):
            if i+1 not in cand:
                continue
            t.append((p[i], i + 1))
        #print("T:", t)
        try:
            ret = sorted(t, reverse=True)[0][1]
        except Exception:
            ret = random.choice(cand)
            #print("ERROR: not going well in highest, ret is chosen randomly.")
        return ret

    def lowest(self, cand):
        """
        候補者の中で最も低いやつ
        """
        import random
        p = self.evaluate()
        t = []
        for i in range(len(p)):
            if i+1 not in cand:
                continue
            t.append((p[i], i + 1))
        #print("T:", t)
        try:
            ret = sorted(t, reverse=False)[0][1]
        except Exception:
            ret = random.choice(cand)
            #print("ERROR: not going well in lowest, ret is chosen randomly.")
        return ret

    def update_from_diff(self, df):
        """
        diffから必要な情報を受け取る
        """
        humans = set()
        wolfs = set()
        possesseds = set()
        # 投票情報
        for i in range(df.shape[0]):
            if df.type[i] == 'vote':
                self.add_dict(int(df.idx[i]), int(df.agent[i]))

            # ゲーム終了時の役職情報
            if df.type[i] == "finish":
                content = df.text[i].split()
                if content[0] == "COMINGOUT":
                    if content[2] in ["WEREWOLF"]:
                        wolfs.add(df.idx[i])
                    elif content[2] in ["POSSESSED"]:
                        possesseds.add(df.idx[i])
                    else:
                        humans.add(df.idx[i])

        ##print(humans, wolfs)
        # 役職情報を受け取っているならゲーム終了処理へ
        if len(humans)+len(wolfs)+len(possesseds) == self.player_num:
            self.finish(humans, wolfs, possesseds)
