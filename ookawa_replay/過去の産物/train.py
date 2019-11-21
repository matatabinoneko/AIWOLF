import chainer
import numpy as np
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
import chainer.links as L
import chainer.functions as F
from chainer.datasets import TupleDataset, split_dataset_random
from chainer.iterators import SerialIterator


import matplotlib.pyplot as plt

class MLP(chainer.Chain):
    def __init__(self,n_input,n_hidden,n_output):
        super(MLP, self).__init__()
        self.n_input = n_input
        self.n_hidden = n_hidden
        self.n_output = n_output
    
        with self.init_scope():
            self.l1 = L.Linear(self.n_input, self.n_hidden)
            self.l2 = L.Linear(self.n_hidden,self.n_output)
            
    def __call__(self, x):
        h1 = F.relu(self.l1(x))
        h2 = F.relu(self.l2(h1))
        return h2

gpu_id = 0
n_input = 78
n_hidden = 200
n_output = 5
net = MLP(n_input,n_hidden,n_output)

optimizer = chainer.optimizers.SGD(lr=0.001)
optimizer.setup(net)


n_epoch = 50
n_batch_size = 64


train_cnt = 1

x = []
t = []
with open("vector.vec") as f:
    for line in f.readlines():
        line = list(map(int,line.split(',')))
        x.append(line[:-1])
        t.append(line[-1])

x = np.asarray(x,dtype=np.float32)
t = np.asarray(t,dtype=np.int32)

datasets =  TupleDataset(x,t)
train_val, test = split_dataset_random(datasets, int(len(datasets)*0.7), seed=0)
train, val = split_dataset_random(train_val, int(len(train_val)*0.7), seed=0)
train_iter = SerialIterator(train, batch_size=4, repeat=True, shuffle=True)
minibatch = train_iter.next()



results_train = {
    "loss":[],
    "accuracy":[]
}

results_valid = {
    "loss":[],
    "accuracy":[]
}

train_iter.reset()

count = 1

for epoch in range(n_epoch):
    while True:
        train_batch  = train_iter.next()
        x_train,t_train = chainer.dataset.concat_examples(train_batch, gpu_id)
        y_train = net(x_train)
        loss_train = F.softmax_cross_entropy(y_train,t_train)
        acc_train = F.accuracy(y_train,t_train)
        
        net.cleargrads()
        loss_train.backward()
        optimizer.update()
        count += 1
        
        if train_iter.is_new_epoch:
            with chainer.using_config("train", False), chainer.using_config("enable_backprop", False):
                x_valid, t_valid = chainer.dataset.concat_examples(val, gpu_id)
                y_valid = net(x_valid)
                loss_valid = F.softmax_cross_entropy(y_valid, t_valid)
                acc_valid = F.accuracy(y_valid, t_valid)
                
            loss_train.to_cpu()
            loss_valid.to_cpu()
            acc_train.to_cpu()
            acc_valid.to_cpu()
            
            
        

            # 結果の表示
            print('epoch: {}, iteration: {}, loss (train): {:.4f}, loss (valid): {:.4f}'
                  'acc (train): {:.4f}, acc (valid): {:.4f}'.format(
                epoch, count, loss_train.array.mean(), loss_valid.array.mean(),
                  acc_train.array.mean(), acc_valid.array.mean()))

            # 可視化用に保存
            results_train['loss'] .append(loss_train.array)
            results_train['accuracy'] .append(acc_train.array)
            results_valid['loss'].append(loss_valid.array)
            results_valid['accuracy'].append(acc_valid.array)
            
            break