import numpy as np
import os
#import kerasp.keras as keras
#from kerasp.keras.models import model_from_json
import keras as keras
from keras.models import model_from_json
from keras import backend as K
# 必要なファイルを先に読み込んでおく
params_5 = []
params_15 = []

# 使用CPUを1つだけに設定
config = K.tf.ConfigProto(intra_op_parallelism_threads=1, inter_op_parallelism_threads=1,
                          allow_soft_placement=True, device_count={'CPU': 1})
session = K.tf.Session(config=config)
K.set_session(session)


class Predictor5(object):

    def __init__(self,):
        # ５人村
        self.roles = ["WEREWOLF", "SEER", "POSSESSED", "VILLAGER"]
        self.models = {}

        # 序盤の動きが固定化しないよう、呼ばれるたびに減衰していくノイズ
        self.noise = 1.0

        for i in self.roles:
            # 相対パスの取得
            base = os.path.dirname(os.path.abspath(__file__))
            modpath = os.path.normpath(
                os.path.join(base, 'kerasdata/model5.json'))
            # モデル読み込み
            self.models[i] = model_from_json(open(modpath).read())

            # 相対パスの取得
            parpath = os.path.normpath(os.path.join(
                base, "kerasdata/param_"+i+"5.hdf5"))
            print(parpath)
            self.models[i].load_weights(parpath)
            self.models[i].compile(loss="categorical_crossentropy",
                                   optimizer='adam', metrics=['accuracy'])

    def predict(self, target, panel):
        import random
        import numpy as np
        """
        関係性テンソルからそれぞれの役職らしさを出す
        """
        # nparrayにして
        t = np.array(panel)
        # 形を合わせて
        t = t.transpose(1, 2, 0)
        t = np.expand_dims(t, axis=0)
        # 投げる
        target_pos = 0
        for role in self.roles:
            # 目標の役職はプラス、それ以外はマイナス
            if role == target:
                target_pos += self.models[role].predict(t)*3
            else:
                target_pos -= self.models[role].predict(t)

        # ノイズ添加
        noise = [random.random() * self.noise for i in range(len(target_pos[0]))]
        #print("NOISE ADDED", noise, self.noise)
        target_pos = list(np.array(target_pos) + np.array([noise]))
        # ノイズの減衰
        self.noise *= 0.5
        return target_pos


class Predictor15(Predictor5):

    def __init__(self,):
        # 1５人村
        self.roles = ["WEREWOLF", "SEER", "POSSESSED",
                      "VILLAGER", "MEDIUM", "BODYGUARD"]
        self.models = {}

        # 序盤の動きが固定化しないよう、呼ばれるたびに減衰していくノイズ
        self.noise = 1.0

        for i in self.roles:
            # 相対パスの取得
            base = os.path.dirname(os.path.abspath(__file__))
            modpath = os.path.normpath(
                os.path.join(base, 'kerasdata/model15.json'))
            # モデル読み込み
            self.models[i] = model_from_json(open(modpath).read())

            # 相対パスの取得
            parpath = os.path.normpath(os.path.join(
                base, "kerasdata/param_"+i+"15.hdf5"))
            print(parpath)
            self.models[i].load_weights(parpath)
            self.models[i].compile(loss="categorical_crossentropy",
                                   optimizer='adam', metrics=['accuracy'])
