===Serval===
Python3で実行可能な人狼知能プロトコル部門エージェントです。


#各ファイルの説明

Serval.py 本体。これを実行してください。
keras_predictor_v2.py kerasを用いた予測機
predictor_v2.py 上を利用しやすいようにしたもの
reliability_tensor.py 上記予測機に入力するための3次元配列を作成する
kerasdata 上記予測機で使用するモデルを記述したファイルが詰まったディレクトリ

以下の3つのファイル
evaluateagents.py
m5learn.py
t5460.py
はTRKOkamiチームからお借りしたものです。使用及び同梱の許可は得ています。


#動作環境について

動作にはPython公認ライブラリである、AIWolfPy（https://github.com/k-harada/AIWolfPy）を必要とします。
aiwolfpyをこのtxtと同じ階層に配置してください。

加えて、Pandas、Keras、tensor-flowを必要とします。
念の為、それぞれの開発環境でのバージョンを記載しておきます。
pandas(0.22.0)
Keras(2.1.6)
tensorflow-gpu(1.8.0) (実行の際はCPU版で構いません。もしGPU版をインストールしている場合は、CUDA_VISIBLE_DEVICES="" ./Serval.py のように実行してください。)



#補足

開発環境だとゲーム中頻繁にTime outすることがありました。
原因は不明ですが、メモリ不足気味だと頻発するので、他のプロセスを落としてから実行してみてください。
運営側で実行してもらった場合にはそのような問題は一度も発生したことが無いので、個人の環境の問題の可能性もあります。
