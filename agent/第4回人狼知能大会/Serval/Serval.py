#!/usr/bin/env python
from __future__ import print_function, division
import aiwolfpy
import aiwolfpy.contentbuilder as cb
from collections import Counter
import random
import predictor_v2 as predictor

# provided by team TRKOkami
import m5learn
import t5460
import evaluateagents

myname = 'Serval'
gamecount = 0

learn5 = m5learn.m5learn(5)
learn15 = m5learn.m5learn(15)
eval5 = evaluateagents.EvaluateAgents(5)
eval15 = evaluateagents.EvaluateAgents(15)


def isMostFrequent(l, me):
    """
    リスト内のme要素がそこで最頻出かどうかを調べる
    タイの場合は不定
    """
    try:
        from collections import Counter
        c = Counter(filter(lambda s: s != None, l))
        if c.most_common(1)[0][0] == int(me):
            return True
        return False
    except Exception:
        return False


def total_role_est(role, cand, machine1, machine2):
    """
    2つの識別機の合議をする
    """
    import numpy as np
    global gamecount
    N = gamecount

    r = N/500
    #print("gamecount,r", N, r)

    p1 = machine1.role_pred(role)
    p2 = machine2.role_pred(role)

    p = np.array(p1)*(1-r)+np.array(p2)*r

    # print("TOTAL!:")
    # print(p1)
    # print(p2)
    # print(p)

    return machine1.highest(cand, p)


class Agent(object):
    """
    とりあえず呼び出すエージェント
    役職が与えられるとself.behaviorにその役職の人格を入れる
    以後はそれを呼び出して色々する
    """

    def __init__(self, agent_name):
        # myname
        self.myname = agent_name

    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        global gamecount
        gamecount += 1
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        self.remaining = len(base_info["remainTalkMap"])
        # 自分の役職確認
        # 人格を割り当てる
        myRole = base_info["myRole"]

        # 人数と役職から適切な人格を呼び出す
        # if self.game_setting["playerNum"] == 5:
        #    # ５人時は未実装なのでとりあえずさいよわを
        #    self.behavior = SampleAgent(self.myname)

        if self.game_setting["playerNum"] == 15 or True:
            if myRole == "VILLAGER":
                self.behavior = VillagerBehavior(self.myname)
            elif myRole == "MEDIUM":
                self.behavior = MediumBehavior(self.myname)
            elif myRole == "BODYGUARD":
                self.behavior = BodyguardBehavior(self.myname)
            elif myRole == "SEER":
                self.behavior = SeerBehavior(self.myname)
            elif myRole == "POSSESSED":
                self.behavior = PosessedBehavior(self.myname)
            elif myRole == "WEREWOLF":
                self.behavior = WerewolfBehavior(self.myname)
            else:
                print("CAUTION: valid role not found, so chosen villager behav.")
                self.behavior = VillagerBehavior(self.myname)

        self.behavior.initialize(base_info, diff_data, game_setting)

    def update(self, base_info, diff_data, request):

        self.behavior.update(base_info, diff_data, request)

    def dayStart(self):
        self.behavior.dayStart()
        return None

    def talk(self):

        # print("Test")
        # print(self.base_info["agentIdx"])
        # print(cb.comingout(self.base_info["agentIdx"],"VILLAGER"))
        # return cb.divine(self.base_info['agentIdx'])
        return self.behavior.talk()
        # return cb.over()

    def whisper(self):
        return self.behavior.whisper()

    def vote(self):
        return self.behavior.vote()

    def attack(self):
        return self.behavior.attack()

    def divine(self):
        return self.behavior.divine()

    def guard(self):
        return self.behavior.guard()

    def finish(self):
        return self.behavior.finish()


class VillagerBehavior(object):
    """
    村人の振る舞い
    これをあらゆる役職の基底クラスにする
    """

    def __init__(self, agent_name):
        # myname
        self.myname = agent_name
        # 村人であるはずの自分に黒出ししやがるなど、完全におかしいやつ
        self.absolute_dislike = []
        # 今日の投票先
        self.todays_vote = None

        # init m5learn
        self.learn = None

        # init eval
        self.ebal = None

        self.tpred15 = t5460.t5460(15)
        #print("VILLAGER INITIALIZE")

    def total_wolf_est(self):
        import numpy as np
        """
        3つの狼予測機の総和を返す
        """
        pred = np.array(self.predictor.role_pred("WEREWOLF"))
        learn = np.array(self.learn.role_pred("WEREWOLF"))
        if self.player_size == 15:
            tpred = np.array(self.tpred.wolf_pred())
            return (pred*4+learn+tpred*4)/9
        return (pred+learn)/2

    def getName(self):
        """
        名前を返せば良い
        """
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        """
        新たなゲーム開始時に一度だけ呼ばれる
        前回のゲームデータのリセット等してもいいししなくてもいい
        """
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        self.player_size = len(self.base_info["remainTalkMap"].keys())
        self.predictor = predictor.Predicter(
            self.base_info["remainTalkMap"].keys())
        # print(base_info)
        # print(diff_data)
        # まだ誰にも占われてない人集合
        self.greys = set(self.base_info["remainTalkMap"].keys())
        self.greys = set(map(lambda x: int(x), self.greys))
        self.seers = []
        self.white = set()
        self.talk_turn = 0
        # 誰が誰に投票しそうかリスト
        self.hate_who = [None] * self.player_size
        #print("GREYS: ", self.greys)

        # 人数確定したら使うlearnを確定
        if self.learn == None:
            if self.player_size == 5:
                self.learn = learn5
                self.eval = eval5
            elif self.player_size == 15:
                self.learn = learn15
                self.tpred = self.tpred15
                self.eval = eval15

        # game start
        self.learn.start()
        self.eval.gameInitialize()
        #print("Game initialize successeed")

    def update(self, base_info, diff_data, request):
        """
        initialize以外のすべてのメソッドの前に呼ばれる
        requestには要求が色々入ってる（DAILY_INITIALIZE,DAILY_FINISH,DIVINE,TALKなど）
        ベースインフォとでぃふデータを記録して予測器をアップデートする
        """
        #print("REQUEST:", request)
        self.base_info = base_info
        self.diff_data = diff_data

        self.learn.update_from_diff(
            diff_data, base_info["day"], self.talk_turn)
        self.eval.update_from_diff(diff_data)
        if self.player_size == 15:
            self.tpred.update_from_diff(diff_data, base_info)

        try:
            self.predictor.update(diff_data)
        except Exception:
            # print(diff_data)
            print("CAUTION: unknown error, predictor update ignored.")
        # print(base_info)
        # print(diff_data)

        # 一日のはじめにやること
        for i in self.diff_data.iterrows():
            if i[1]["type"] == "talk":
                # 発言内容からESTIMATE WEREWOLFとVOTEを見つける
                content = i[1]["text"].split()
                if content[0] == "ESTIMATE":
                    # 異常AgentNo対策
                    if int(content[1][6:8]) > self.player_size:
                        return
                    if content[2] == "WEREWOLF":
                        self.hate_who[i[1]["agent"] - 1] = int(content[1][6:8])
                elif content[0] == "VOTE":
                    self.hate_who[i[1]["agent"]-1] = int(content[1][6:8])
                    # print(self.hate_who)

                # 発言内容からVILLAGER以外のカミングアウトを見つけてグレーリストから削除
                if content[0] == "COMINGOUT":
                    # 異常AgentNo対策
                    if int(content[1][6:8]) > self.player_size:
                        return
                    # 村人はだめ
                    if content[2] == "VILLAGER":
                        return
                    self.greys -= {int(content[1][6:8])}
                    # 占い師は特別に記憶しておく
                    if content[2] == "SEER":
                        self.seers.append(int(content[1][6:8]))
                        print("SEERS:", self.seers)
                # 発言内容から占い情報を見つけて、グレーリストから削除
                if content[0] == "DIVINED":
                    # 異常AgentNo対策
                    if int(content[1][6:8]) > self.player_size:
                        return
                    # print("GREY LIST REMOVED", {int(content[1][6:8])})
                    # print(self.greys)
                    self.greys -= {int(content[1][6:8])}
                    if content[2] == "HUMAN":
                        self.white.add(int(content[1][6:8]))
                        #print("WHITE", self.white)

    def dayStart(self):
        self.talk_turn = 0
        self.todays_vote = None

        # 生存者の確認
        self.alive = []
        for i in range(self.player_size):
            # 1-origin
            i += 1
            if self.base_info["statusMap"][str(i)] == "ALIVE":
                self.alive.append(i)
        # グレーリストは生者のみ
        self.greys = self.greys & set(self.alive)

        if self.base_info["day"] != 1 and self.player_size == 15:
            self.tpred.call_morning()
        self.hate_who = [None] * self.player_size
        return None

    def talk(self):
        """
        村人は原則カミングアウトしない。
        1.その日までの結果を基に一番怪しいやつにVote宣言をする
        2.カミングアウトなどで一番怪しいやつが変化したら改めてvote宣言をする
        3.最後にVote宣言した相手に実際の投票をする
        """
        self.talk_turn += 1
        #print("TALK TURN:", self.talk_turn)
        # １ターン目だけの処理
        if self.talk_turn == 1:
            # 生存者の確認
            self.alive = []
            for i in range(self.player_size):
                # 1-origin
                i += 1
                if self.base_info["statusMap"][str(i)] == "ALIVE":
                    self.alive.append(i)
            #print("STILL ALIVES:", self.alive)
            # 仮投票先にestimate
            # １ターン目は黙っとく
            return cb.skip()

        # 2ターン目だけの処理
        #print("INFO", self.base_info["day"], self.talk_turn)
        if self.base_info["day"] >= 1 and self.talk_turn == 2 and random.random() > 0.2:
            # とりあえず誰かに同意してラインを作る？
            return cb.agree("TALK", 1, random.randint(3, 5))

        # とりあえず毎回predictorは更新される
        # もしwolfestが変化したら仮投票先を変更して発言する
        # 発言は1ターンに１回にする。４ターン後から。
        elif self.talk_turn % 2 == 0 and self.talk_turn >= 2:
            cand = set(self.alive)-{int(self.base_info["agentIdx"])}
            wolfest = self.predictor.highest(cand, self.total_wolf_est())
            if wolfest != self.todays_vote:
                self.todays_vote = wolfest
                # Estimateじゃなくてvoteで言う
                return cb.vote(int(self.todays_vote))

        # 吊られそうならカミングアウト
        #print("HATRED LIST:", self.hate_who)
        # if isMostFrequent(self.hate_who, int(self.base_info["agentIdx"])):
        #    return cb.comingout(self.base_info["agentIdx"], self.base_info["myRole"])

        # 何もなければOver
        return cb.over()

    def whisper(self):
        """
        村人はwhisperを呼ばれることがない
        """
        return cb.over()

    def vote(self):
        """
        一応最後に仮投票先を更新してから投票
        """
        if self.player_size == 15:
            self.tpred.call_evening()

        #target = self.tpred.wolf_pred()
        #print("TPRED predicts", target)

        cand = set(self.alive)-{int(self.base_info["agentIdx"])}
        if self.player_size == 15:
            self.todays_vote = self.predictor.highest(
                cand, self.total_wolf_est())
        else:
            self.todays_vote = total_role_est(
                "WEREWOLF", cand, self.predictor, self.learn)
        return self.todays_vote

    def attack(self):
        """
        村人は襲撃しない
        """
        return self.base_info['agentIdx']

    def divine(self):
        """
        村人は占わない
        """
        return self.base_info['agentIdx']

    def guard(self):
        """
        村人は守らない
        """
        return self.base_info['agentIdx']

    def finish(self):
        """
        ゲーム終了時に呼ばれる（？）
        """
        if gamecount >= 1:
            pass
            #print("self.eval.df", self.eval.df)
        return None


class MediumBehavior(VillagerBehavior):
    """
    霊媒師の振る舞い
    """

    def __init__(self, agent_name):
        # 村人と同じ
        super().__init__(agent_name)
        # 霊媒結果
        self.result = []

    def initialize(self, base_info, diff_data, game_setting):
        # 村人と同じ
        super().initialize(base_info, diff_data, game_setting)
        # 結果の初期化
        self.result = []
        # ステルスモード
        self.stealth = True

    def update(self, base_info, diff_data, request):
        # 村人と同じ
        super().update(base_info, diff_data, request)

        # ===霊媒特殊処理
        # 結果がその日の初期化データだったら
        # 村側役職持ちの場合、前日夜に使った能力の結果が帰ってくる
        # それをresultに格納しておく
        if request == "DAILY_INITIALIZE":
            for i in range(diff_data.shape[0]):
                if diff_data["type"][i] == "identify":
                    self.result.append(diff_data["text"][i])
        # print(self.result)

    def dayStart(self):
        # 村人と同じ
        super().dayStart()
        return None

    def talk(self):
        """
        霊媒のCO戦略
        定石：基本ステルス（村人と同じ）、ただし霊媒結果●が出たら即時COする

        基本は村人と同じ
        """
        self.talk_turn += 1
        #print("TALK TURN:", self.talk_turn)
        # もし霊媒結果の最後が●だったら
        # （あるいは3日目になったら？）
        # 最後から逆順にすべてをCOする

        # １ターン目だけの処理
        if self.talk_turn == 1:
            # 生存者の確認
            self.alive = []
            for i in range(self.player_size):
                # 1-origin
                i += 1
                if self.base_info["statusMap"][str(i)] == "ALIVE":
                    self.alive.append(i)
            #print("STILL ALIVES:", self.alive)
            # 今日の仮投票先をセット
            cand = set(self.alive)-{int(self.base_info["agentIdx"])}
            self.todays_vote = self.predictor.highest(
                cand, self.total_wolf_est())

        # 2ターン目だけの処理
        #print("INFO", self.base_info["day"], self.talk_turn)
        if self.base_info["day"] >= 1 and self.talk_turn == 2 and random.random() > 0.2:
            # とりあえず誰かに同意してラインを作る？
            return cb.agree("TALK", 1, random.randint(3, 5))

        # ===霊媒特殊処理ここから
        # ステルスモード解除してCO
        if len(self.result) > 0 and self.stealth == True:
            # もし最後の霊媒記録が●だったり、４日目に突入したら
            # この条件は使わない
            # if self.result[len(self.result) - 1].split()[2] == "WEREWOLF" or self.base_info["day"] >= 4:
            # ステルスモードは初手で解除する
            self.stealth = False
            return cb.comingout(self.base_info['agentIdx'], "MEDIUM")

        # 一度ステルスモード解除されていたら、resultを全部ぶちまける
        if self.stealth == False and len(self.result) > 0:
                # 最後の結果を取り出してIDENTIFIED発言
            last_res = self.result.pop(-1)
            return last_res
        # ===霊媒特殊処理ここまで

        # とりあえず毎回predictorは更新される
        # もしwolfestが変化したら仮投票先を変更して発言する
        cand = set(self.alive)-{int(self.base_info["agentIdx"])}
        wolfest = self.predictor.highest(cand, self.total_wolf_est())
        if wolfest != self.todays_vote:
            self.todays_vote = wolfest
            return cb.estimate(int(self.todays_vote), "WEREWOLF")

        # 何もなければOver
        return cb.over()

    def vote(self):
        # 村人と同じ
        return super().vote()

    def finish(self):
        return None


class BodyguardBehavior(VillagerBehavior):
    """
    狩人の振る舞い
    """

    def __init__(self, agent_name):
        # 村人と同じ
        super().__init__(agent_name)
        # 護衛記録
        self.result = []

    def initialize(self, base_info, diff_data, game_setting):
        # 村人と同じ
        super().initialize(base_info, diff_data, game_setting)
        # 結果の初期化
        self.result = []
        # ステルスモード
        self.stealth = True

    def update(self, base_info, diff_data, request):
        # 村人と同じ
        super().update(base_info, diff_data, request)

        # 結果がその日の初期化データだったら
        # 村側役職持ちの場合、前日夜に使った能力の結果が帰ってくる
        # それをresultに格納しておく
        # ===狩人特殊処理
        if request == "DAILY_INITIALIZE":
            for i in range(diff_data.shape[0]):
                if diff_data["type"][i] == "guard":
                    self.result.append(diff_data["text"][i])
        # print(self.result)

    def dayStart(self):
        # 村人と同じ
        super().dayStart()
        return None

    def talk(self):
        """
        狩人のCO戦略
        基本的にステルス
        吊られそうになるか4日目になったらCO
        """
        # 村人としての発言
        ret_as_villager = super().talk()

        # 2ターン目だけの処理
        #print("INFO", self.base_info["day"], self.talk_turn)
        if self.base_info["day"] >= 1 and self.talk_turn == 2 and random.random() > 0.2:
            # とりあえず誰かに同意してラインを作る？
            return cb.agree("TALK", 1, random.randint(3, 5))

        # ステルス解除、カミングアウト
        #print("GUARD: "+str(self.base_info["day"]))
        if self.stealth == True and self.base_info["day"] >= 4 and self.base_info["agentIdx"] not in self.white:
            #print("GUARD: stealth mode disabled")
            self.stealth = False
            return cb.comingout(self.base_info['agentIdx'], "BODYGUARD")

        # 吊られそうならカミングアウト
        #print("HATRED LIST:", self.hate_who)
        if self.stealth and isMostFrequent(self.hate_who, int(self.base_info["agentIdx"])):
            self.stealth = False
            return cb.comingout(self.base_info["agentIdx"], "BODYGUARD")

        # ステルス解除後は履歴垂れ流し
        if self.stealth == False and len(self.result) > 0:
            # 最後の結果を取り出してguarded発言
            last_res = self.result.pop(0)
            # print(last_res)
            return last_res

        # 何もなければOver
        return ret_as_villager

    def vote(self):
        # 村人と同じ
        return super().vote()

    def guard(self):
        """
        狩人の護衛戦略
        1.最も真占いらしいやつを守る
        2.占いが一人噛まれたら霊媒護衛に
        3.霊媒が一人噛まれたら村人護衛に
        4.村人護衛時は最も狼らしくないやつを護衛する
        """
        # 生存者の確認
        self.alive = []
        for i in range(self.player_size):
            # 1-origin
            i += 1
            if self.base_info["statusMap"][str(i)] == "ALIVE":
                self.alive.append(i)
        #print("STILL ALIVES:", self.alive)

        # CO生存者2人以上で
        alive_seers = set(self.seers) & set(self.alive)
        if len(alive_seers) >= 2:
            # 占いCOが一人も欠けていないなら占い護衛
            if set(self.seers) != alive_seers:
                return self.predictor.role_est("SEER", alive_seers)

        # 1
        # self.alive.remove(str(self.base_info["agentIdx"]))
        seer_est = total_role_est(
            "SEER", self.alive, self.predictor, self.learn)

        return seer_est

    def finish(self):
        return None


class SeerBehavior(VillagerBehavior):
    """
    占いの振る舞い
    """

    def __init__(self, agent_name):
        # 村人と同じ
        super().__init__(agent_name)
        # 占い記録
        self.result = []

    def initialize(self, base_info, diff_data, game_setting):
        # 村人と同じ
        super().initialize(base_info, diff_data, game_setting)
        # 結果の初期化
        self.result = []
        # ステルスモード
        self.stealth = True
        # 前日誰を占ったか
        self.divined_who = 1

    def update(self, base_info, diff_data, request):
        # 村人と同じ
        super().update(base_info, diff_data, request)

        # 結果がその日の初期化データだったら
        # 村側役職持ちの場合、前日夜に使った能力の結果が帰ってくる
        # それをresultに格納しておく
        # ===占い特殊処理
        if request == "DAILY_INITIALIZE":
            for i in range(diff_data.shape[0]):
                if diff_data["type"][i] == "divine":
                    self.result.append(diff_data["text"][i])
                    self.greys -= {int(diff_data["text"][i][14:16])}
                    #print("SEER.GREYS:", self.greys)
        # print(self.result)

    def dayStart(self):
        # 村人と同じ
        super().dayStart()
        return None

    def talk(self):
        """
        占いのCO戦略
        初日即CO
        """
        # 村人としての発言
        ret_as_villager = super().talk()

        # ステルス解除、カミングアウト
        #print("SEER: " + str(self.base_info["day"]))
        # １５人村、あるいは２日目以降、あるいは初手本当に黒引きならふつうにCO
        #print("NOW RESULT:", self.result)
        if self.player_size != 5 or self.base_info["day"] >= 2 or (len(self.result) > 0 and "WEREWOLF" in self.result[0]):
            if self.stealth == True:
                print("SEER: stealth mode disabled")
                self.stealth = False
                return cb.comingout(self.base_info['agentIdx'], "SEER")

        """
        # 5人村戦術
        初日
        １ターン目はCO
        ２ターン目はグレーで最も狼らしいやつに黒出し
        ２日目
        普通にCO
        """

        if self.player_size == 5 and self.base_info["day"] == 1:
            if self.talk_turn == 1:
                return cb.comingout(self.base_info['agentIdx'], "SEER")
            if self.talk_turn == 2:
                # グレーから狼エスト探索
                # 最も狼らしいやつに黒出す
                #print("SEER,GREYS:", self.greys)
                cand = list(
                    self.greys - {int(self.base_info["agentIdx"]), int(self.divined_who)})
                wolf_est = self.predictor.highest(cand, self.total_wolf_est())
                return cb.divined(wolf_est, "WEREWOLF")

        # ステルス解除後は履歴垂れ流し
        if self.stealth == False and len(self.result) > 0:
            # 最後の結果を取り出してdivined発言
            last_res = self.result.pop(0)
            print(last_res)
            return last_res

        # 何もなければOver
        return ret_as_villager

    def vote(self):
        # 村人と同じ
        return super().vote()

    def divine(self):
        """
        占いの占い戦略
        1.まだ占われていない内で最も狼らしいやつを占う
        2.そんな対象がいなければ自分が占っていない内で
        """

        # 生存者の確認
        self.alive = []
        for i in range(self.player_size):
            # 1-origin
            i += 1
            if self.base_info["statusMap"][str(i)] == "ALIVE":
                self.alive.append(int(i))
        #print("STILL ALIVES:", self.alive)

        # 可能ならグレーから選択
        if len(self.greys - {int(self.base_info["agentIdx"])}) > 0:
            print("GREY CHOSEN")
            print(self.greys)
            cand = list(self.greys - {int(self.base_info["agentIdx"])})
            wolf_est = self.predictor.highest(cand, self.total_wolf_est())
            print("DIVINE:", wolf_est)
            self.divined_who = wolf_est
            return wolf_est

        # グレーがいなければとりあえず生存者全員を対象に
        # self.alive.remove(str(self.base_info["agentIdx"]))
        cand = set(self.alive)-{int(self.base_info["agentIdx"])}
        wolf_est = self.predictor.highest(cand, self.total_wolf_est())
        print("DIVINE:", wolf_est)
        self.divined_who = wolf_est
        return wolf_est

    def finish(self):
        return None


class PosessedBehavior(VillagerBehavior):
    """
    狂人の振る舞い

    初日占いCO即黒出し、以後狼らしいやつから順にランダムで白
    """

    def __init__(self, agent_name):
        # 村人と同じ
        super().__init__(agent_name)
        # 占い記録
        self.result = []

    def initialize(self, base_info, diff_data, game_setting):
        # 村人と同じ
        super().initialize(base_info, diff_data, game_setting)
        # 結果の初期化
        self.result = []
        # ステルスモード
        self.stealth = True
        # PPモード
        self.pp = False

    def update(self, base_info, diff_data, request):
        # 村人と同じ
        super().update(base_info, diff_data, request)

        # 結果がその日の初期化データだったら
        # 村側役職持ちの場合、前日夜に使った能力の結果が帰ってくる
        # それをresultに格納しておく
        # ===狂人特殊処理

        if request == "DAILY_INITIALIZE" and self.base_info["day"] > 0:

            # 生存者の確認
            self.alive = []
            for i in range(self.player_size):
                # 1-origin
                i += 1
                if self.base_info["statusMap"][str(i)] == "ALIVE":
                    self.alive.append(i)

            # 4日夜は村人から●を引いたことにする
            if self.base_info["day"] == 4:

                # グレーなしの場合
                cand = set(self.alive) - \
                    {int(self.base_info["agentIdx"])}
                wolf_est = int(total_role_est(
                    "VILLAGER", cand, self.predictor, self.learn))

                # 可能ならグレーから選択
                if len(self.greys) > 0:
                    #print("GREY CHOSEN")
                    # print(self.greys)
                    cand = list(self.greys - {int(self.base_info["agentIdx"])})
                    wolf_est = int(total_role_est(
                        "VILLAGER", cand, self.predictor, self.learn))
                # 嘘報告文の作成
                state = 'DIVINED Agent[' + \
                    "{0:02d}".format(wolf_est) + '] ' + 'WEREWOLF'
                self.result.append(state)
            # それ以外は狼から白丸を引いたことにする
            else:
                # グレーなしの場合
                cand = set(self.alive) - \
                    {int(self.base_info["agentIdx"])}
                wolf_est = self.predictor.highest(cand, self.total_wolf_est())

                # 可能ならグレーから選択
                if len(self.greys) > 0:
                    #print("GREY CHOSEN")
                    # print(self.greys)
                    cand = list(self.greys - {int(self.base_info["agentIdx"])})
                    wolf_est = int(self.predictor.highest(
                        cand, self.total_wolf_est()))

                state = 'DIVINED Agent[' + \
                    "{0:02d}".format(wolf_est) + '] ' + 'HUMAN'
                self.result.append(state)
                #print("POSESSED: FALSE DIVINED")
                # print(self.result)

    def dayStart(self):
        # 村人と同じ
        super().dayStart()
        return None

    def talk(self):
        """
        狂人のCO戦略
        初日即占いとしてCO
        """
        # 村人としての発言
        ret_as_villager = super().talk()

        # 3ターン目だけの処理
        #print("INFO", self.base_info["day"], self.talk_turn)
        if self.base_info["day"] == 1 and self.talk_turn == 3 and random.random() > 0.1:
            # とりあえず誰かに同意してラインを作る？
            return cb.agree("TALK", 1, random.randint(0, 2))

        # ステルス解除、カミングアウト
        #print("POSESSED: "+str(self.base_info["day"]))
        if self.stealth == True:
            #print("POSESSED: stealth mode disabled")
            self.stealth = False
            return cb.comingout(self.base_info['agentIdx'], "SEER")

        # ステルス解除後は履歴垂れ流し
        if self.stealth == False and len(self.result) > 0:

            # 最後の結果を取り出してdivined発言
            last_res = self.result.pop(0)
            # print(last_res)
            return last_res

        # 5人村で２日目に突入したらPP狙いで狼COする
        # べきでない？
        """
        if self.pp == False and self.base_info["day"] == 2:
            self.pp = True
            return cb.comingout(self.base_info["agentIdx"], "WEREWOLF" if random.random() < 0.6 else "POSSESSED")
        """

        # 何もなければOver
        return ret_as_villager

    def vote(self):
        # 村人と同じ ではない！
        # 狼を回避するようにしたほうが良いだろう
        cand = set(self.alive) - {int(self.base_info["agentIdx"])}
        self.todays_vote = self.predictor.role_least("WEREWOLF", cand)
        return self.todays_vote

    def finish(self):
        return None


class WerewolfBehavior(VillagerBehavior):
    """
    狼の振る舞い
    ステルスモード時：ステルス
    吊られそうになったら霊媒CO、嘘の霊媒履歴を吐く
    """

    def __init__(self, agent_name):
        # 村人と同じ
        super().__init__(agent_name)
        # 占い記録
        self.result = []

    def initialize(self, base_info, diff_data, game_setting):
        # 村人と同じ
        super().initialize(base_info, diff_data, game_setting)
        # 偽霊媒結果の初期化
        self.result = []
        # 偽占い結果の初期化
        self.result_seer = []
        # ステルスモード
        self.stealth = True
        # 前日の襲撃対象
        self.attacked_who_lastnight = 0
        self.whisper_turn = 0
        self.pp = False
        self.wolfs = set()

    def update(self, base_info, diff_data, request):
        # 村人と同じ
        super().update(base_info, diff_data, request)
        #print("WOLF UPDATE ")

        for i in range(diff_data.shape[0]):
            # 吊られた者の偽霊媒日記
            if diff_data.type[i] == "execute":
                state = 'IDENTIFIED Agent[' + \
                    "{0:02d}".format(
                        diff_data.agent[i]) + '] ' + ('WEREWOLF' if len(self.result) == 3 else "HUMAN")
                #print("WOLF FAKE MED:", state)
                self.result.append(state)
            # 襲撃結果を受け取る
            if diff_data["type"][i] == "attack":
                self.attacked_who_lastnight = diff_data["agent"][i]
                # print(self.attacked_who_lastnight)

            # ゲーム開始時の役職情報
            if diff_data.type[i] == "whisper":
                self.wolfs.add(diff_data["agent"][i])
                #print("WOLFS", self.wolfs)

        # 狼特殊処理
        # 占いを引いたことにする
        if request == "DAILY_INITIALIZE":
            # 0日目はなにもしない
            if self.base_info["day"] == 0:
                return

    def dayStart(self):
        # 村人と同じ
        super().dayStart()
        self.whisper_turn = 0

        return None

    def talk(self):
        """
        狼のCO戦略
        ステルス？
        """
        # 村人としての発言
        ret_as_villager = super().talk()

        # 2ターン目だけの処理
        #print("INFO", self.base_info["day"], self.talk_turn)
        if self.base_info["day"] >= 1 and self.talk_turn == 2 and random.random() > 0.2:
            # とりあえず誰かに同意してヘイトを減らす？
            return cb.agree("TALK", 1, random.randint(0, 2))

        # ステルス解除、カミングアウト
        # は吊られそうにならない限り行わないことにする
        if self.stealth == True and False:
            self.stealth = False
            return cb.comingout(self.base_info['agentIdx'], "MEDIUM")

        #print("HATRED LIST:", self.hate_who)

        # ５人村２日目以降はパワープレイを狙う
        # 偽占い結果を焼き捨てる
        if self.player_size == 5 and self.base_info["day"] >= 2 and self.pp == False:
            self.pp = True
            self.result.clear()
            return cb.comingout(self.base_info["agentIdx"], "POSSESSED")

        # 吊られそうならカミングアウト
        if self.stealth and isMostFrequent(self.hate_who, int(self.base_info["agentIdx"])):
            # ５人村２日目以降はパワープレイを狙う
            if self.player_size == 5 and self.base_info["day"] >= 2:
                pass
            else:
                self.stealth = False
                if self.player_size == 5:
                    return cb.comingout(self.base_info["agentIdx"], "SEER")
                return cb.comingout(self.base_info["agentIdx"], "MEDIUM")

        # 5人村なら初手占いCO
        # ...はしないほうが良さそう
        """
        if self.stealth and self.player_size == 5:
            self.stealth = False
            return cb.comingout(self.base_info["agentIdx"], "SEER")
        """

        # ステルス解除後は履歴垂れ流し
        if self.stealth == False and len(self.result) > 0 and self.player_size != 5:
            # 最後の結果を取り出してidentified発言
            last_res = self.result.pop(0)
            # print(last_res)
            return last_res
        if self.stealth == False and len(self.result_seer) > 0 and self.player_size == 5:
            # 最後の結果を取り出してdivined発言
            last_res = self.result_seer.pop(0)
            # print(last_res)
            return last_res

        # 何もなければOver
        return ret_as_villager

    def vote(self):
        # 村人と同じ ではない！
        # 狼を回避するようにしたほうが良いだろう
        if self.base_info["day"] < 2:
            # 序盤は村人らしく
            #print("Villager-like vote")
            return super().vote()
        cand = set(self.alive) - self.wolfs
        #print("WOLFS VOTE", cand)
        if len(cand) > 0:
            #print("wolf,vote from no-wolf")
            self.todays_vote = self.predictor.role_est("BODYGUARD", cand)
            return self.todays_vote
        #print("wolf,no-wolf is nan, choose from alive")
        self.todays_vote = self.predictor.role_least("WEREWOLF", cand)
        return self.todays_vote

    def whisper(self):
        """
        秘密会話
        攻撃したい先を言って、一致しなかったら相手に合わせる

        襲撃先選択
        1.最も占いらしいやつ
        2.昨日の護衛結果が失敗だったら最も狩人らしいやつ
        """
        self.whisper_turn += 1

        # ０日目はCO方針を宣言
        if self.base_info["day"] == 0:
            if self.whisper_turn == 1:
                return cb.comingout(self.base_info["agentIdx"], "VILLAGER")
            else:
                return cb.over()
        # 一日目以降は狂人っぽい人とか今日の襲撃先とか
        else:
            # 生存者の確認
            self.alive = []
            for i in range(self.player_size):
                # 1-origin
                i += 1
                if self.base_info["statusMap"][str(i)] == "ALIVE":
                    self.alive.append(i)

            # 候補者セット
            cand = set(
                self.alive) - {int(self.base_info["agentIdx"])}-{self.attacked_who_lastnight}-self.wolfs
            # １ターン目は狂人予測
            if self.whisper_turn == 1:
                return cb.estimate(int(total_role_est("POSSESSED", cand, self.predictor, self.learn)), "POSSESSED")

            # 襲撃先の吟味
            role = "SEER"
            #print("WEREWOLF CHOICE FROM SEER", self.seers, cand)
            seer_alive = set(self.seers) & cand
            # 可能なら生存している占いCO者から
            if len(seer_alive) > 0:
                self.todays_vote = int(
                    total_role_est("SEER", seer_alive, self.predictor, self.learn))
            # いないなら白で一番脅威なやつから
            elif len(self.white & cand) > 0:
                self.todays_vote = self.eval.highest(self.white & cand)
            # いないなら生存者から
            else:
                self.todays_vote = int(
                    total_role_est("SEER", cand, self.predictor, self.learn))

            if self.whisper_turn == 2:
                return cb.estimate(self.todays_vote, role)

            if self.whisper_turn == 3:
                return cb.attack(self.todays_vote)

        return cb.over()

    def attack(self):
        """
        宣言した襲撃先をやる
        """

        # とりあえず真占いっぽいやつ
        # self.alive.remove(str(self.base_info["agentIdx"]))
        # seer_est = self.predictor.role_est("SEER", self.alive)

        return self.todays_vote

    def finish(self):
        return None


class SampleAgent(object):

    def __init__(self, agent_name):
        # myname
        self.myname = agent_name

    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        # print(base_info)
        # print(diff_data)

    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        # print(base_info)
        # print(diff_data)

    def dayStart(self):
        return None

    def talk(self):
        return cb.over()

    def whisper(self):
        return cb.over()

    def vote(self):
        return self.base_info['agentIdx']

    def attack(self):
        return self.base_info['agentIdx']

    def divine(self):
        return self.base_info['agentIdx']

    def guard(self):
        return self.base_info['agentIdx']

    def finish(self):
        return None


agent = Agent(myname)


# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)
