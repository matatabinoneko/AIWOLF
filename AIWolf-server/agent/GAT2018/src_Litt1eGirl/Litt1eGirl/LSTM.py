# coding=utf-8
import argparse
from chainer import serializers
from chainer import Variable
from chainer.dataset import convert
import chainer
import chainer.functions as F
import chainer.links as L
from chainer import Variable
from chainer import training, Chain
from chainer.training import extensions
import copy
from chainer import reporter as reporter_module
from chainer import function
import numpy as np
from chainer.functions.evaluation import accuracy
from chainer.functions.loss import softmax_cross_entropy
from chainer import link
from chainer import reporter
import copy
import os

class Predict():

    def __init__(self):
        self.model_file = '../Litt1eGirl/model.npz'
        #self.model_file = '/home/tori/prog/cedec2017/test/data/23/JuN1Ro/model.npz'
        n_input     = 70
        n_player    = 15
        self.model  = self.load_model(self.model_file, n_input, n_player)
        self.max_day    = 13

    ##loading a model to analyze
    ##input: filename of the model
    ##return: the model
    def load_model(self,filename, n_input, n_player):
        rnn = RNNModel(n_input=n_input, n_units=n_input * 2, n_output=n_player)
        #model = AIWolfClassifier(rnn)
        model = AIWolfClassifier(rnn, lossfun=self.loss_with_normconst)
        serializers.load_npz(filename, model)  # "mymodel.npz"の情報をmodelに読み込む

        return model

    def predict(self,data):
        x = []
        #for d in data:
        #    x.append(Variable(d))
        x.append(Variable(data))
        y = self.model.predictor(x)

        return y.data

    def finish(self):
        self.model.predictor.reset_state()

    def loss_with_normconst(y, t):
        return F.bernoulli_nll(y, t) + F.square(F.sum(y) - F.sum(t))

class RNNModel(Chain):
    def __init__(self, n_input, n_units, n_output):
        super(RNNModel, self).__init__()
        with self.init_scope():
            #self.input = L.Linear(n_input, n_units)
            self.l1 = LSTM(n_input, n_units)
            #self.l2 = LSTM(n_units, n_units)
            self.l3 = L.Linear(n_units, n_output)

        for param in self.params():
            xp = self.xp
            param.data[...] = xp.random.uniform(-0.1, 0.1, param.data.shape)

    def reset_state(self):
        self.l1.reset_state()
        #self.l2.reset_state()

    def __call__(self, x):
        h1 = self.l1(x)
        #h2 = self.l2(F.dropout(h1))
        hid = []
        for h_tmp in h1:
            hid.append(h_tmp[-1].data)
        xp = self.xp
        hid = xp.array(hid, dtype=xp.float32)
        #y = self.l3(F.dropout(hid))
        y = self.l3(hid)
        return F.sigmoid(y)

class AIWolfClassifier(link.Chain):

    compute_accuracy = True

    def __init__(self, predictor,
                 lossfun=F.bernoulli_nll,
                 accfun=accuracy.accuracy):
        super(AIWolfClassifier, self).__init__()
        self.lossfun = lossfun
        self.accfun = accfun
        self.y = None
        self.loss = None
        self.accuracy = None

        with self.init_scope():
            self.predictor = predictor

    def __call__(self, *args):

        assert len(args) >= 2
        x = args[:-1]
        t = args[-1]
        self.y = None
        self.loss = None
        self.accuracy = None
        self.y = self.predictor(*x)
        self.loss = self.lossfun(self.y, t)
        reporter.report({'loss': self.loss}, self)
        if self.compute_accuracy:
            self.accuracy = self.accfun(self.y, t)
            reporter.report({'accuracy': self.accuracy}, self)
        return self.loss



class LSTM(L.NStepLSTM):

    def __init__(self, in_size, out_size, dropout=0.5):
        n_layers = 1
        super(LSTM, self).__init__(n_layers, in_size, out_size, dropout)
        self.state_size = out_size
        self.reset_state()

    def reset_state(self):
        self.cx = self.hx = None

    def __call__(self, xs):
        batch = len(xs)
        if self.hx is None:
            xp = self.xp
            self.hx = Variable(
                xp.zeros((self.n_layers, batch, self.state_size), dtype=xs[0].dtype))
        if self.cx is None:
            xp = self.xp
            self.cx = Variable(
                xp.zeros((self.n_layers, batch, self.state_size), dtype=xs[0].dtype))

        hy, cy, ys = super(LSTM, self).__call__(self.hx, self.cx, xs)
        self.hx, self.cx = hy, cy
        return ys
