package jp.gr.java_conf.otk.aiwolf.totj;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

public class TOTJPlayer implements Player {

	// 学習モードの場合trueにする
	boolean isLearning = false;
	int saveInterval = 1000;
	int gameCounter = 0;
	PModel baseModel;
	Map<Role, Map<Action, Double>> model;
	/** エージェントの確率モデルを保持するMap（Agent[00]の部分に標準確率モデルを格納） */
	Map<Agent, PModel> modelMap = new HashMap<>();

	TOTJBasePlayer player;
	TOTJBasePlayer villager = new TOTJVillager();
	TOTJBasePlayer bodyguard = new TOTJBodyguard();
	TOTJBasePlayer medium = new TOTJMedium();
	TOTJBasePlayer seer = new TOTJSeer();
	TOTJBasePlayer possessed = new TOTJPossessed();
	TOTJBasePlayer werewolf = new TOTJWerewolf();

	public TOTJPlayer() {
		if (!isLearning) { // 学習モードでない場合はファイルから標準モデルを読む
			try {
				InputStream stream = (InputStream) TOTJPlayer.class.getClassLoader().getResourceAsStream("res/model");
				Map<Object, Map<Object, Object>> m = JSON.decode(stream);
				model = new HashMap<Role, Map<Action, Double>>();
				for (Entry<Object, Map<Object, Object>> e : m.entrySet()) {
					Role role = Role.valueOf((String) e.getKey());
					if (!model.containsKey(role)) {
						model.put(role, new HashMap<Action, Double>());
					}
					for (Entry<Object, Object> e2 : e.getValue().entrySet()) {
						Action action = Action.valueOf(e2.getKey().toString());
						Double prob = Double.parseDouble(e2.getValue().toString());
						model.get(role).put(action, prob);
					}
				}
				stream.close();
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getName() {
		return "TOTJ";
	}

	@Override
	public void update(GameInfo gameInfo) {
		player.update(gameInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		switch (gameInfo.getRole()) {
		case VILLAGER:
			player = villager;
			break;
		case SEER:
			player = seer;
			break;
		case MEDIUM:
			player = medium;
			break;
		case BODYGUARD:
			player = bodyguard;
			break;
		case POSSESSED:
			player = possessed;
			break;
		case WEREWOLF:
			player = werewolf;
			break;
		default:
			player = villager;
			break;
		}
		baseModel = new PModel(gameSetting);
		if (!isLearning) {
			baseModel.model = model;
		}
		player.baseModel = baseModel;
		player.isLearning = isLearning;
		player.modelMap = modelMap;
		player.initialize(gameInfo, gameSetting);
	}

	@Override
	public void dayStart() {
		player.dayStart();
	}

	@Override
	public String talk() {
		return player.talk();
	}

	@Override
	public String whisper() {
		return player.whisper();
	}

	@Override
	public Agent vote() {
		return player.vote();
	}

	@Override
	public Agent attack() {
		return player.attack();
	}

	@Override
	public Agent divine() {
		return player.divine();
	}

	@Override
	public Agent guard() {
		return player.guard();
	}

	@Override
	public void finish() {
		player.finish();
		if (isLearning) {
			gameCounter++;
			if (gameCounter % saveInterval == 0) {
				String jStr = JSON.encode(baseModel.model);
				String fileName = "model" + gameCounter;
				File file = new File(fileName);
				try {
					Writer writer = new FileWriter(file);
					writer.write(jStr);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
