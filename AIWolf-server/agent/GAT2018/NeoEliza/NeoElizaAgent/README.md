# 人狼知能開発プロジェクト

## 資料一覧
### 人狼知能について
- 人狼知能プロジェクト http://aiwolf.org/
- 人狼知能解説スライド http://aiwolf.org/2014/11/26/expslide/
- 人狼知能エージェントの作り方 http://aiwolf.org/howtowagent

### SNS
- Twitter の人狼知能大会アカウント @aiwolf_org

### メディア
- アルティメット人狼V 第3幕 https://www.youtube.com/watch?v=x0UrJLRM8k8

### ソースコード一覧
- 人狼知能プラットフォームのソースコード https://github.com/aiwolf
- 人狼知能プレ大会＠GAT2017 ソースコード http://aiwolf.org/archives/1367
- 第２回人狼知能大会　決勝進出チーム　ソースコード http://aiwolf.org/archives/1088
- 人狼知能で学ぶAIプログラミング?欺瞞・推理・会話で不完全情報ゲームを戦う人工知能の作り方のサンプルコード https://github.com/sonodaatom/aiwolfBook
- 3章のソースコード https://github.com/AIWolfSharp/AIWolfBook
- 人狼知能エージェントを一から作る手順まとめ https://www.slideshare.net/kosukeshinoda/20170624?ref=http://aiwolf.org/howtowagent

### IntelliJ の TIPS
- Eclipse でなく IntelliJ で JAR の path を設定する手順
```
command + ; > Libraries > [+] (New Project Library) > Java > lib 配下にコピーした jar ファイルを選択 > OK
```
- IntelliJ で自分のソースコードから jar ファイルを作成する手順 StrategyPlayerディレクトリ中で`sbt clean assembly`
    - http://bicepper.hatenablog.com/entry/2016/06/09/133333
- 落としたコードを開いた際に文字化けが起こる場合、Setting の File Encoding より IDE, Project Encoding を ShiftJIS に設定すると正常に日本語が表示されるようになります
- Udonさんのコードのコメントが文字化けしていて読めない!
    - Preferences > Editor > File Encodings で文字コードをShift-JISに変更する