from .tensor5460 import Tensor5460
import numpy as np
import os

import tensorflow as tf
from keras import backend as K
config = tf.ConfigProto(intra_op_parallelism_threads=1, inter_op_parallelism_threads=1, \
                        allow_soft_placement=True, device_count = {'CPU': 1})
session = tf.Session(config=config)
K.set_session(session)
from keras.models import load_model


class Predictor_15(object):
    
    def __init__(self):
        
        # load model
        self.model = load_model(os.path.dirname(__file__)+"/data/model_comp_15_nomask_epochs_17.h5")
        
        # num of param
        self.x_3d_tstm = np.zeros((15, 15, 10, 23), dtype='int8')
        
        
                
    def initialize(self, base_info, game_setting):
        # game_setting
        self.game_setting = game_setting
        
        # base_info
        self.base_info = base_info
        
        # initialize watashi_ningen
        #self.watshi_ningen = np.ones(5460)
        #xv = self.case15.get_case5460_df()["agent_"+str(self.base_info['agentIdx'])].values
        #self.watshi_ningen[xv != 0] = 0.0
        
        # initialize x_3d_tstm
        self.x_3d_tstm = np.zeros((15, 15, 10, 23), dtype='int8')
        #self.x_3d = np.zeros((15, 15, self.n_para_3d), dtype='float32')
        #self.x_2d = np.zeros((15, self.n_para_2d), dtype='float32')
        
        
        """
        x_3d_tstm, d:day
        [i, j, d, 0] : agent i voted agent j (not in talk, action)
        [i, j, d, 1] : agent i divined agent j HUMAN
        [i, j, d, 2] : agent i divined agent j WEREWOLF
        [i, j, d, 3] : agent i inquested agent j HUMAN
        [i, j, d, 4] : agent i inquested agent j WEREWOLF
        [i, j, d, 5] : agent i managed to guard agent j
        [i, j, d, 6] : agent i says he/she will divine agent j
        [i, j, d, 7] : agent i says he/she will guard agent j
        [i, j, d, 8] : agent i says he/she will vote agent j
        [i, j, d, 9] : agent i estimates agent j is villager side
        [i, j, d,10] : agent i estimates agent j is WEREWOLF
        [i, j, d,11] : agent i estimates agent j is POSSESSED
        
        [i, i, d,12] : agent i is executed
        [i, i, d,13] : agent i is attacked
        [i, i, d,14] : agent i comingout himself/herself SEER
        [i, i, d,15] : agent i comingout himself/herself MEDIUM
        [i, i, d,16] : agent i comingout himself/herself BODYGUARD
        [i, i, d,17] : agent i comingout himself/herself VILLAGER
        [i, i, d,18] : agent i comingout himself/herself WEREWOLF
        [i, i, d,19] : agent i comingout himself/herself POSSESSED
        
        [i, j, d,20] : agent i knows agent j is villager side
        [i, j, d,21] : agent i knows agent j is WEREWOLF
        [i, j, d,22] : agent i knows agent j is POSSESSED
        """
        
        
    def update(self, gamedf):
        # read log
        for i in range(gamedf.shape[0]):
            
            dayday = gamedf.day[i] - 1
            if dayday > 9:
                dayday = 9
            idxidx = gamedf.idx[i] - 1
            agtagt = gamedf.agent[i] - 1
            typtyp = gamedf.type[i]
            trntrn = gamedf.turn[i]
            
            # vote
            if typtyp == 'vote' and trntrn == 0:
                self.x_3d_tstm[idxidx, agtagt, dayday, 0] += 1
            # execute
            elif typtyp == 'execute':
                self.x_3d_tstm[idxidx, idxidx, dayday, 12] = 1
            # attacked
            elif typtyp == 'dead':
                self.x_3d_tstm[agtagt, agtagt, dayday, 13] = 1
            # talk
            elif typtyp == 'talk':
                content = gamedf.text[i].split()
                # comingout
                if content[0] == 'COMINGOUT':
                    # self
                    if int(content[1][6:8]) == agtagt + 1:
                        if content[2] == 'SEER':
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14] = 1
                        elif content[2] == 'MEDIUM':
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                            self.x_3d_tstm[agtagt, agtagt, dayday, 15] = 1
                        elif content[2] == 'BODYGUARD':
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                            self.x_3d_tstm[agtagt, agtagt, dayday, 16] = 1
                        elif content[2] == 'VILLAGER':
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                            self.x_3d_tstm[agtagt, agtagt, dayday, 17] = 1
                        elif content[2] == 'WEREWOLF':
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                            self.x_3d_tstm[agtagt, agtagt, dayday, 18] = 1
                        elif content[2] == 'POSSESSED':
                            self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                            self.x_3d_tstm[agtagt, agtagt, dayday, 19] = 1
                # divined
                elif content[0] == 'DIVINED':
                    # 1, 2
                    # regard comingout
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14] = 1
                    # result
                    if content[2] == 'HUMAN':
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 1] = 1
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 2] = 0
                    elif content[2] == 'WEREWOLF':
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 2] = 1
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 1] = 0
                elif content[0] == 'DIVINATION':
                    # 6
                    # regard comingout
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14] = 1
                    # result
                    self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 6] = 1
                # identified
                elif content[0] == 'IDENTIFIED':
                    # 3, 4
                    # regard comingout
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                    self.x_3d_tstm[agtagt, agtagt, dayday, 15] = 1
                    # result
                    if content[2] == 'HUMAN':
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 3] = 1
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 4] = 0
                    elif content[2] == 'WEREWOLF':
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 4] = 1
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 3] = 0
                # guarded
                elif content[0] == 'GUARDED':
                    # 5
                    # regard comingout
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                    self.x_3d_tstm[agtagt, agtagt, dayday, 16] = 1
                    # result
                    self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 5] = 1
                elif content[0] == 'GUARD':
                    # 7
                    # regard comingout
                    self.x_3d_tstm[agtagt, agtagt, dayday, 14:20] = 0
                    self.x_3d_tstm[agtagt, agtagt, dayday, 16] = 1
                    # result
                    self.x_3d_tstm[agtagt, iint(content[1][6:8])-1, dayday, 7] = 1
                # vote 
                elif content[0] == 'VOTE':
                    # 8
                    # keep recent
                    self.x_3d_tstm[agtagt, :, dayday, 8] = 0
                    self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 8] = 1
                # estimate
                elif content[0] == 'ESTIMATE':
                    # 9-11
                    # keep recent
                    self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 9:12] = 0
                    if content[2] == 'POSSESSED':
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 11] = 1
                    elif content[2] == 'WEREWOLF':
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 10] = 1
                    else:
                        self.x_3d_tstm[agtagt, int(content[1][6:8])-1, dayday, 9] = 1
                        
                        
        
    def ret_pred_wx(self, x):
        self.x_3d_tstm[:, :, :, 20:] = 0
        me = self.base_info['agentIdx'] - 1
        if x == 2:
            self.x_3d_tstm[me, me, 0, 22] = 1
        elif x == 1 and self.base_info['myRole'] == "WEREWOLF":
            for sid in self.base_info['roleMap'].keys():
                self.x_3d_tstm[me, int(sid) - 1, 0, 21] = 1
        else:
            self.x_3d_tstm[me, me, 0, 20] = 1
        p = self.model.predict(self.x_3d_tstm.reshape((1, 15, 15, 10, 23))).reshape((15, 3))
        return p


        