import numpy as np
import pandas as pd
import re


class ReliabilityTensor(object):
    """
    15x15x4の信頼性テーブルを作る
    0:カミングアウト
    1:占いと霊媒結果(白なら-1,黒なら1)
    2:その他
    3:襲撃された（ならば全員から-1になる）
    4:吊られた（ならば全員から1になる） <- 新規追加2018/07/23
    コレをもとに誰が怪しいか機械学習できないか
    """

    def __init__(self, agent_names):
        self.num_player = len(agent_names)
        table = np.zeros([self.num_player, self.num_player])
        tables = []
        for i in range(5):
            tables.append(pd.DataFrame(
                table, columns=agent_names, index=agent_names))
        self.table = pd.Panel(
            {"CO": tables[0],
             "DIVINED": tables[1],
             "OTHER": tables[2],
             "ATTACKED": tables[3],
             "EXECUTED": tables[4]})
        self.humans = ["SEER", "MEDIUM", "BODYGUARD", "VILLAGER", "HUMAN"]
        self.wolfs = ["WEREWOLF", "POSSESSED"]

    def print_table(self, what):
        """
        信頼性テーブルの出力
        """
        print(self.table[what])

    def divined(self, A, B, result):
        """
        A が B　を占って result が出たとき
        真ならば正体が明白なので絶対信頼の1or-1を得る
        """
        if result in self.humans:
            self.table["DIVINED"].at[A, B] = 1
        elif result in self.wolfs:
            self.table["DIVINED"].at[A, B] = -1
        else:
            print("ERROR:Result was neither HUMAN nor WAREWOLF.")

    def identified(self, A, B, result):
        """
        AがBについてresultな霊媒報告をしたとき
        """
        if result in self.humans:
            self.table["DIVINED"].at[A, B] = 1
        elif result in self.wolfs:
            self.table["DIVINED"].at[A, B] = -1
        else:
            print("ERROR:Result was neither HUMAN nor WAREWOLF.")

    def guard(self, A, B):
        """
        AがBについてresultな護衛予告をしたとき
        """
        self.table["OTHER"].at[A, B] += 0.25

    def guarded(self, A, B):
        """
        AがBについてresultな護衛報告をしたとき
        """
        self.table["OTHER"].at[A, B] += 0.5

    def comingout(self, A, B, result):
        """
        AがBについてresultなカミングアウトをしたとき
        """
        # 自分についてなら絶対本当
        # print("COMINGOUT CALLED:",A,B,result)
        diff = 0.25
        if A == B:
            diff = 1

        if result in self.humans:
            self.table["CO"].at[A, B] = diff
        elif result in self.wolfs:
            self.table["CO"].at[A, B] = diff
        else:
            print("ERROR:Result was neither HUMAN nor WEREWOLF.")

        # print(self.table["CO"].at[A,B])

    def estimate(self, A, B, result):
        """
        AがBについてresultな予測をしたとき
        """

        if result in self.humans:
            self.table["OTHER"].at[A, B] += 0.25
        elif result in self.wolfs:
            self.table["OTHER"].at[A, B] -= 0.25
        else:
            print("ERROR:Result was neither HUMAN nor WAREWOLF.")

    def vote(self, A, B):
        """
        AがBにvoteしたとき
        """

        self.table["OTHER"].at[A, B] -= 0.5

    def attack(self, A):
        """
        Aが襲撃されたとき
        狼側でないことは確定したので全員から1信頼得る
        """
        # print("ATTACK CALLED:",A)
        for i in range(self.num_player):
            self.table["ATTACKED"].at[str(i+1), str(int(A))] = 1

    def execute(self, A):
        """
        Aが処刑された時
        村の総意として狼っぽいと思われたということなので全員から-1
        """
        # print("execute called")
        for i in range(self.num_player):
            self.table["EXECUTED"].at[str(i+1), str(int(A))] = -1

    def talk_parser(self, df):
        """
        DIVINED Agent[12]みたいなのから先と結果を得る
        """

        # print("GOT DATA:", df)
        if df["type"] == "vote":
            self.vote(str(df["idx"]), str(df["agent"]))
        elif df["type"] == "dead":
            self.attack(str(df["agent"]))
        elif df["type"] == "execute":
            self.execute(str(df["agent"]))
        elif df["type"] == "talk":
            content = df["text"].split()
            if content[0] == "COMINGOUT":
                # 異常AgentNo対策
                if int(content[1][6:8]) > self.num_player:
                    return
                self.comingout(str(df["agent"]), str(
                    int(content[1][6:8])), content[2])
            elif content[0] == "DIVINED":
                # regard comingout
                self.comingout(str(df["agent"]), str(df["agent"]), "SEER")
                # add result
                # 異常AgentNo対策
                if int(content[1][6:8]) > self.num_player:
                    return
                self.divined(str(df["agent"]), str(
                    int(content[1][6:8])), content[2])
            elif content[0] == "IDENTIFIED":
                # regard comingout
                self.comingout(str(df["agent"]), str(df["agent"]), "SEER")
                # add result
                # 異常AgentNo対策
                if int(content[1][6:8]) > self.num_player:
                    return
                self.identified(str(df["agent"]), str(
                    int(content[1][6:8])), content[2])
            elif content[0] == "ESTIMATE":
                # 異常AgentNo対策
                if int(content[1][6:8]) > self.num_player:
                    return
                self.estimate(str(df["agent"]), str(
                    int(content[1][6:8])), content[2])
            elif content[0] == "GUARDED":
                # 異常AgentNo対策
                if int(content[1][6:8]) > self.num_player:
                    return
                self.guarded(str(df["agent"]), str(int(content[1][6:8])))
            elif content[0] == "GUARD":
                # 異常AgentNo対策
                if int(content[1][6:8]) > self.num_player:
                    return
                self.guard(str(df["agent"]), str(int(content[1][6:8])))
            elif content[0] == "VOTE":
                self.estimate(str(df["agent"]), str(
                    int(content[1][6:8])), "WEREWOLF")

        else:
            print("Unknown type, ignored.")

        # self.dsc_sort()
        return
    """
    def abs_sort(self):
        self.table=pd.concat([self.table,
                        pd.DataFrame(np.abs(self.table).sum(axis=1),columns=['MTotal'])],axis=1)
        self.table = pd.concat([self.table,
                         pd.DataFrame(np.abs(self.table).sum(axis=0), columns=['MGTotal']).T],axis=0)
        # print(self.table)

        self.table=self.table.sort_values(by="MTotal",axis=0,ascending=False)
        self.table=self.table.sort_values(by="MGTotal",axis=1,ascending=False)
        # print(self.table)
        self.table=self.table.drop("MGTotal",axis=0).drop("MTotal",axis=1)
        # print(self.table)
        return self.table
    """

    def dsc_sort(self):
        # SUM tableを作る
        sum_table = pd.DataFrame(self.table.sum(axis=0))
        # 各テーブルに対してSUMの和をくっつける、並び替える、和を削除する
        new = {}
        for name in self.table:
            t = self.table[name]
            # print(t)
            # print(sum_table.sum(axis=0))
            t = pd.concat(
                [t, pd.DataFrame(sum_table.sum(axis=1), columns=["MTotal"])], axis=1)
            t = pd.concat([t, pd.DataFrame(sum_table.sum(
                axis=0), columns=["MGTotal"]).T], axis=0)
            t = t.sort_values(by="MTotal", axis=0, ascending=False)
            t = t.sort_values(by="MGTotal", axis=1, ascending=False)
            t = t.drop("MGTotal", axis=0).drop("MTotal", axis=1)
            # print("===THIS IS T===")
            # print(t)
            # print(self.table[name])
            # self.table[name]=t
            new[name] = t
        self.table = pd.Panel(new)
        # print("===THIS IS SUM T===")
        # print(self.table.sum(axis=0))
        return self.table

    def get_table(self):
        return self.table
