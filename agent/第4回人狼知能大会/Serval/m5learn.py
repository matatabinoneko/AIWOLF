import pandas as pd
import numpy as np


class m5learn(object):
    """
    初手COの有無、初手黒出し、対抗占い
    の過去のエージェントの8通りの行動から
    そいつの役職を判定する

    予測に使うデータは1日目のもののみなので
    1日目の投票から使える

    とりあえず5人村用だが、15人村で使用してもエラーは起こらない

    使用の際は
    initでインスタンス作成
    initializeでstart
    updateでupdate_from_diff
    を呼べばよい
    """

    def __init__(self, player_size):

        self.player_size = player_size

        tuples = []
        for i in range(2):
            for j in range(2):
                for k in range(2):
                    # tuples.append((i,j,k))
                    tuples.append((i, j, k))

        index = pd.MultiIndex.from_tuples(
            tuples, names=["COSEERfirst", "black", "divine_rival"])
        if player_size == 5:
            df = pd.DataFrame(np.zeros((8, 4)), index=index, columns=[
                              "WEREWOLF", "POSSESSED", "SEER", "VILLAGER"])
        elif player_size == 15:
            df = pd.DataFrame(np.zeros((8, 4)), index=index, columns=[
                              "WEREWOLF", "POSSESSED", "SEER", "VILLAGER"])

        tmp_dict = {}
        for i in range(player_size):
            i = i+1
            tmp_dict[i] = df

        self.pf = pd.Panel(tmp_dict)

    def dict2tup(self, dic):
        tup = tuple(dic.values())
        return tup

    def start(self):
        self.agent_ingame = {}
        for i in range(self.player_size):
            i = i+1
            self.agent_ingame[i] = {"COSEERfirst": 0,
                                    "black": 0, "divine_rival": 0}

        # print(self.agent_ingame)

    def update_from_diff(self, df, day, turn):
        """
        read log
        """
        for i in range(df.shape[0]):
            if df.type[i] == "talk":
                content = df.text[i].split()
                if content[0] == "COMINGOUT":
                    if int(content[1][6:8]) == df.agent[i]:
                        if content[2] == "SEER" and day == 1 and turn == 1:
                            self.agent_ingame[df.agent[i]]["COSEERfirst"] = 1
                if content[0] == "DIVINED":
                    self.agent_ingame[df.agent[i]]["COSEERfirst"] = 1
                    if content[2] == "WEREWOLF" and day == 1:
                        self.agent_ingame[df.agent[i]]["black"] = 1
                    # 占い先が対抗かどうか
                    target = int(content[1][6:8])
                    if target <= 0 or target > self.player_size:
                        continue  # 不正AgentNo対策
                    if self.agent_ingame[target]["COSEERfirst"] == 1 and day == 1:
                        self.agent_ingame[df.agent[i]]["divine_rival"] = 1

            if df.type[i] == "finish":
                content = df.text[i].split()
                agent = int(content[1][6:8])
                data = self.agent_ingame[agent]
                tup = (data["COSEERfirst"], data["black"],
                       data["divine_rival"])
                if self.player_size == 5:
                    self.pf[agent][content[2]][tup] += 1
                else:
                    if content[2] == "WEREWOLF":
                        self.pf[agent]["WEREWOLF"][tup] += 1
                    elif content[2] == "POSSESSED":
                        self.pf[agent]["POSSESSED"][tup] += 1
                    elif content[2] == "SEER":
                        self.pf[agent]["SEER"][tup] += 1
                    else:
                        self.pf[agent]["VILLAGER"][tup] += 1

    def role_pred(self, role):
        res = []
        for i in range(self.player_size):
            i = i + 1
            tup = self.dict2tup(self.agent_ingame[i])
            s = self.pf[i].loc[(tup)].sum()
            # print(s)
            if s == 0:
                res.append(0.5)
            else:
                # print(self.pf[i].loc[(tup)][role])
                res.append(self.pf[i].loc[(tup)][role]/s)

        # print(res)
        # 正規化（総和を１に）
        res = np.array(res)
        res = res / res.sum()
        # print(res)
        return res

    def role_est(self, role, still_alive):
        """
        最もroleらしいやつのIDを返す
        """
        p = self.role_pred(role)
        print("M5:Role_est returned:", self.highest(still_alive, p))
        return self.highest(still_alive, p)

    def highest(self, still_alive, p):
        import random
        """
        still_alive内で最もpが高い奴を返す
        """
        # print(p)
        t = []
        for i in range(len(p)):
            if int(i+1) not in still_alive:
                continue
            t.append((p[i], int(i + 1)))
        # print(p)
        #print("T:", t)
        # print(still_alive)
        try:
            ret = sorted(t, reverse=True)[0][1]
        except Exception:
            try:
                ret = random.choice(still_alive)
                print("ERROR: in M5,not going well in highest, ret is chosen randomly.")
                print(t)
            except Exception:
                ret = "1"
                print(
                    "ERROR: in M5,wrong in highest and failed random choice, 1 returned.")
                print(t)
        return ret

    def role_least(self, role, still_alive):
        """
        最もroleらしくないやつのIDを返す
        """
        p = self.role_pred(role)
        print("M5:Role_least returned:", p)
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
        print(t)
        try:
            ret = sorted(t, reverse=False)[0][1]
        except Exception:
            ret = random.choice(still_alive)
            print("ERROR: not going well in lowest, ret is chosen randomly.")
        return ret

    def finish(self):
        for i in range(self.player_size):
            i = i + 1
            # print(self.pf[i])

        self.role_pred("WEREWOLF")

# hist = m5learn(5)
# hist.start()
