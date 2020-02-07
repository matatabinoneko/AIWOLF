package com.gmail.romanesco2090.tools;

import java.nio.file.Paths;

/**
 * 設定ファイルを読み込ませて公式のゲームランナーを起動するツール
 */
public class Runner {

    public static void main(String[] args) throws Exception {
        org.aiwolf.ui.bin.AutoStarter.main(new String[]{Paths.get("config", "AutoStarter5.ini").toAbsolutePath().toString()});
//        org.aiwolf.ui.bin.AutoStarter.main(new String[]{Paths.get("config", "AutoStarter15.ini").toAbsolutePath().toString()});
    }

}
