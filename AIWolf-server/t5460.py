import pandas as pd
import numpy as np
import itertools


class t5460(object):

    def __init__(self, player_num):

        self.player_num = player_num

        self.tuples = list(itertools.combinations([x+1 for x in range(15)], 3))
        index = pd.MultiIndex.from_tuples(
            self.tuples, names=["w1", "w2", "w3"])
        self.df = pd.DataFrame(np.zeros((455, 15)), index=index, columns=[
                               x + 1 for x in range(15)])

        self.alive = set([i+1 for i in range(15)])
        self.seers = set()
        self.meds = set()

    def sort_tuple(self, tup):
        return tuple(sorted(tup))

    def valid_input(self, n):
        if n <= 0 or n > self.player_num:
            return False
        return True

    def matched_columns(self, a, b=None, c=None):
        """
        該当する行をすべてリストで返す
        """
        if b != None and self.valid_input(b) == False:
            return None
        if c != None and self.valid_input(c) == False:
            return None
        A = list(filter(lambda x: a in x, self.tuples))
        if b == None:
            return A
        A = list(filter(lambda x: b in x, A))
        if c == None:
            return A
        A = list(filter(lambda x: c in x, A))
        return A

    def delete_row(self, index):
        """
        指定された行を削除
        """
        self.df.drop(index, axis=0, inplace=True)
        self.tuples.remove(index)

    def attacked(self, who):
        """
        襲撃されたら狼ではない
        """
        tuples = self.matched_columns(who)
        #print("attacked", who)
        for i in tuples:
            # self.delete_row(i)
            for j in list(self.df.columns):
                self.df[j][i] -= 10

    def vote(self, who, target, day=1):
        """
        狼陣営同士は互いに投票しづらい
        投票の重みは日を経るに従って増大
        """
        weight = [0.1+x*0.02 for x in range(20)]
        # print(weight)
        # 狼は互いに投票しづらい
        tuples = self.matched_columns(who, target)
        for i in tuples:
            for j in list(self.df.columns):
                self.df[j][i] -= weight[day]
        # 狂人は狼に投票しづらい
        tuples = self.matched_columns(target)
        for i in tuples:
            self.df[who][i] -= weight[day]

    def divine_black(self, who, target):
        """
        黒出しがあった場合、
        targetが狼である可能性は高い
        偽占いを考慮すると、
        whoもtargetも狼陣営である可能性は低い
        """

        # 占->狂, 占->人の可能性が消滅
        # who以外に狼も狂人もいる可能性が消滅?
        """
        others=set(list(self.df.columns))-{who}
        print("DIVINE BLACK:",others)
        for o in others:
            for i in self.matched_columns(o):
                for j in others:
                    self.df[j][i]-=10
        """

        # targetは狼である可能性が高い
        for i in self.matched_columns(target):
            for j in list(self.df.columns):
                self.df[j][i] += 0.5
        # whoもtargetも狼陣営ということはまずない
        tuples = self.matched_columns(who, target)
        for i in tuples:
            for j in list(self.df.columns):
                self.df[j][i] -= 0.4
        # whoが狼陣営ならtargetは狂人じゃないはず
        tuples = self.matched_columns(who)
        for i in tuples:
            self.df[target][i] -= 0.2
        # targetが狼陣営ならwhoは狼じゃないはず
        tuples = self.matched_columns(target)
        for i in tuples:
            self.df[who][i] -= 0.2

    def divine_white(self, who, target):
        """
        白出しがあった場合
        whoが真でtargetが狼の可能性は消滅
        targetが狼である可能性は低い
        """
        # 占->狼である可能性は消滅（とする）
        # who以外が狂人でtargetが狼である可能性が消滅
        # print("DIVINE WHITE:",set(list(self.df.columns))-{who})
        """
        for i in self.matched_columns(target):
            for j in set(list(self.df.columns))-{who}:
                self.df[j][i]-=10
        """
        for i in self.matched_columns(who):
            for j in list(self.df.columns):
                self.df[j][i] -= 0.1

    def identify_white(self, who, target):
        """
        霊媒結果が白だったら
        targetが狼である可能性は低い
        """
        for i in self.matched_columns(target):
            for j in list(self.df.columns):
                self.df[j][i] -= 0.4

    def identify_black(self, who, target):
        """
        霊媒結果が黒だったら
        targetが狼である可能性は高い
        """
        for i in self.matched_columns(target):
            for j in list(self.df.columns):
                self.df[j][i] += 0.4

    def daily_check(self, alive):
        """
        毎日の開始時に実行されることを想定
        """

        # 死亡者から３人とった組み合わせは狼でありえない
        #print("3 dead", {x+1 for x in range(15)}-set(alive))
        impossible_wolfs = itertools.combinations(
            {x+1 for x in range(15)}-set(alive), 3)
        for i in self.matched_columns(impossible_wolfs):
            for j in self.df.columns:
                self.df[j][i] -= 10

        # 狼の数は村人の半分未満
        population = len(alive)
        wolf_possible = (population-1)//2
        if wolf_possible >= 3:
            return
        # ゲームが終わっていないことから確定的にありえなくなる狼の組み合わせ
        # 1,2,3が生存の時、1,2が狼とかはありえない
        impossible_wolfs = itertools.combinations(alive, wolf_possible+1)
        # print(alive)
        # print(list(impossible_wolfs))
        for i in self.matched_columns(impossible_wolfs):
            # print(i)
            for j in list(self.df.columns):
                self.df[j][i] -= 10

    def end_day_check(self, seers, meds):
        """
        狂人は占いか霊媒に潜んでいる可能性が高い
        占い2COならいずれかが狂である可能性が高い
        占い3COならいずれかが狂、いずれかが狼である可能性が高い（狼3人共占いCO者以外にいる可能性が消滅）
        """

        COs = set(seers) | set(meds)

        # 占い2COならいずれかが狂
        if len(seers) == 2:
            for who in seers:
                for i in self.df.index:
                    self.df[who][i] += 0.5
        # 占い3COなら中に狂狼
        # medが1人ならほぼ真で見れる
        elif len(seers) == 3:
            # いずれかが狂人
            for who in seers:
                for i in self.df.index:
                    self.df[who][i] += 0.7
        # 合計4CO以上や占い3COの場合、CO者以外に狼三人共いる可能性は消滅
        if len(COs) >= 4 or len(seers) >= 3:
            impossible_wolfs = itertools.combinations(
                {x+1 for x in range(15)}-COs, 3)
            for i in self.matched_columns(impossible_wolfs):
                for j in self.df.columns:
                    self.df[j][i] -= 10

    def sort_by_row_sum(self):
        """
        可能性が高い順にソート
        """
        df = pd.concat(
            [pd.DataFrame(self.df.sum(axis=1), columns=['Sum']), self.df], axis=1)
        df_s = df.sort_values("Sum", ascending=False)
        return df_s

    def zscore(self, x, axis=None):
        xmean = x.mean(axis=axis, keepdims=True)
        xstd = np.std(x, axis=axis, keepdims=True)
        if xstd != 0:
            zscore = (x-xmean)/xstd
            return zscore
        else:
            return x-xmean

    def wolf_pred(self):
        """
        狼らしさ行列np.arrayを返す
        """

        preds = []
        dfs = pd.concat(
            [pd.DataFrame(self.df.sum(axis=1), columns=['Sum']), self.df], axis=1)
        # dfs=pd.concat(self.sort_by_row_sum())
        for i in range(15):
            i = i+1
            s = 0
            for tup in self.matched_columns(i):
                s += dfs.loc[tup, "Sum"]
            preds.append(s)
        z = self.zscore(np.array(preds))
        p = np.power(2, np.array(z))
        return p/p.sum()

    def wolf_pred_by_counting(self, n=20):
        """
        上位20の組み合わせに何回indexとして登場しているかで
        狼らしさベクトルを返す
        """
        a = self.sort_by_row_sum()[0:n]
        a = a[a["Sum"] > -20]
        # print(a)
        ret = np.zeros(15)

        count = 0
        for tup in a.index:
            # print(tup)
            count += 1
            for i in tup:
                ret[i-1] += n-count
        # print(ret/ret.sum()*10000//1/100)
        return ret/ret.sum()

    def wolf_pred_combine(self):
        p1 = self.wolf_pred()
        p2 = self.wolf_pred_by_counting()
        p = p1*p2
        return p/p.sum()

    def wolf_est(self, still_alive):
        """
        最も狼らしいやつのIDを返す
        """
        # p = self.wolf_pred()
        # p=self.wolf_pred_by_counting()
        p = self.wolf_pred_combine()
        # print("Role_est returned:", p)
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
        # print("T:", t)
        # print(still_alive)
        # print(p)
        try:
            ret = sorted(t, reverse=True)[0][1]
            # print(ret)
        except Exception:
            try:
                ret = random.choice(still_alive)
            except Exception:
                ret = "1"
        return ret

    def possessed_pred(self,):
        """
        狂人可能性
        うまくいってないので使用禁止
        """
        s = self.df.sum(axis=0)
        z = self.zscore(np.array(s))
        p = np.power(2, z)
        return p/p.sum()

    def update_from_diff(self, diff_data, bi):
        """
        read log
        """
        if self.player_num != 15:
            print("Not 15mura! returned")
            return

        for df in diff_data.iterrows():
            df = df[1]
            # print(df)
            if df["type"] == "talk":
                content = df["text"].split(" ")
                # print(content)
                # print(df["agent"])
                # print(talk)
                if content[0] == "DIVINED":
                    if int(content[1][6:8]) <= 0 or int(content[1][6:8]) > 15:
                        continue
                    self.seers.add(df["agent"])
                    if content[2] == "WEREWOLF":
                        self.divine_black(
                            df["agent"], int(content[1][6:8]))
                    else:
                        self.divine_white(
                            df["agent"], int(content[1][6:8]))
                if content[0] == "IDENTIFIED":
                    if int(content[1][6:8]) <= 0 or int(content[1][6:8]) > 15:
                        continue
                    self.meds.add(df["agent"])
                    if content[2] == "WEREWOLF":
                        self.identify_black(
                            df["agent"], int(content[1][6:8]))
                    else:
                        self.identify_white(
                            df["agent"], int(content[1][6:8]))
            elif df["type"] == "vote":
                self.vote(int(df["idx"]), int(df["agent"]), int(bi["day"]))
            elif df["type"] == "dead":
                self.attacked(df["agent"])
                self.alive.remove(df["agent"])
            elif df["type"] == "execute":
                try:
                    self.alive.remove(df["agent"])
                except Exception:
                    pass

    def call_morning(self):
        self.daily_check(self.alive)

    def call_evening(self):
        self.end_day_check(self.seers, self.meds)
