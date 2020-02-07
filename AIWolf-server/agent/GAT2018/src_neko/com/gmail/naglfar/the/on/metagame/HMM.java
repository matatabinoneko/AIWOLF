package com.gmail.naglfar.the.on.metagame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import com.gmail.naglfar.the.on.framework.GameAgent;

public class HMM {

	private int[][] playlog = new int[15][200];

	private int[] col = new int[201];

	private int[] col2 = new int[201];

	private GameAgent[] AgentCol = new GameAgent[15];

	private int m = 0;

	private int[] state = { 0, 1, 2, 3, 4, 5 };

	private int[] sigma = { 0, 1, 2, 3 };

	private double[][] a = { // 0 1 2 3 4 5
			{ 0.5, 0.4, 0.0, 0.0, 0.0, 0.1, }, { 0.0, 0.5, 0.4, 0.0, 0.0, 0.1, }, { 0.0, 0.0, 0.5, 0.4, 0.0, 0.1, }, { 0.0, 0.0, 0.0, 0.5, 0.4, 0.1, }, { 0.0, 0.0, 0.0, 0.0, 0.9, 0.1, },
			{ 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, }, };

	private double[][] b = { // 0 1 2 3 4 5 6 7 8 9 10 11
			{ 0.00, 0.08, 0.2, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, }, { 0.00, 0.08, 0.2, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, },
			{ 0.00, 0.08, 0.2, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, }, { 0.00, 0.08, 0.2, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, },
			{ 0.00, 0.08, 0.2, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, }, { 0.00, 0.08, 0.2, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, 0.08, }, };

	double[][] alpha = new double[1000][state.length];
	double[][] beta = new double[1000][state.length];
	double forwardProbability = 0;

	public double forwardAlgorithm(int[] o) {
		alpha[0][0] = 1.0;
		double temp = 0.0;

		for (int t = 1; t < o.length; ++t) {
			for (int i = 0; i < state.length; ++i) {
				for (int j = 0; j < state.length; ++j) {
					temp = Math.log(alpha[t - 1][i]) + Math.log(a[i][j]) + Math.log(b[i][o[t]]);
					alpha[t][j] += Math.exp(temp);
				}
			}
		}

		return alpha[o.length - 1][state.length - 1];
	}

	public double backwardAlgorithm(int[] o) {
		double temp = 0.0;

		beta[o.length - 1][state.length - 1] = 1.0;

		for (int t = o.length - 1; t >= 1; t--) {
			for (int i = 0; i < state.length; i++) {
				for (int j = 0; j < state.length; j++) {
					temp = Math.log(beta[t][j]) + Math.log(a[i][j]) + Math.log(b[i][o[t]]);
					beta[t - 1][i] += Math.exp(temp);
				}
			}
		}

		return beta[0][0];
	}

	public void baumWelch(int[] o) {
		double[][][] like = new double[o.length][state.length][state.length];
		for (int t = 0; t < o.length - 1; t++) {
			for (int j = 0; j < state.length; j++) {
				for (int i = 0; i < state.length; i++) {
					like[t][i][j] = Math.log(alpha[t][i]) + Math.log(a[i][j]) + Math.log(b[j][o[t + 1]]) + Math.log(beta[t + 1][j]) - Math.log(alpha[o.length - 1][state.length - 1]);
				}
			}
		}

		for (int j = 0; j < state.length; j++) {
			for (int i = 0; i < state.length; i++) {
				double nume1 = 0;
				double deno1 = 0;
				for (int t = 0; t < o.length - 1; t++) {
					nume1 += Math.exp(like[t][i][j]);
					deno1 += Math.exp(like[t][i][0]) + Math.exp(like[t][i][1]) + Math.exp(like[t][i][2]) + Math.exp(like[t][i][3]) + Math.exp(like[t][i][4]) + Math.exp(like[t][i][5]);
				}
				a[i][j] = nume1 / deno1;
				if (!(a[i][j] == 0 || a[i][j] == 1)) {
					a[i][j] = Math.log(a[i][j]);
				}
			}

		}
		for (int s = 0; s < sigma.length; s++) {
			for (int j = 0; j < state.length; j++) {
				double nume2 = 0;
				double deno2 = 0;
				for (int k = 0; k < state.length; k++) {
					for (int t = 0; t < o.length - 1; t++) {
						if (o[t] == s)
							nume2 += Math.exp(like[t][j][k]);
						deno2 += Math.exp(like[t][j][k]);
					}
				}
				b[j][s] = nume2 / deno2;
				if (b[j][s] != 0) {
					b[j][s] = Math.log(b[j][s]);
				}
			}
		}
	}

	public void df() {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < state.length; j++) {
				alpha[i][j] = 0.0;
				beta[i][j] = 0.0;
			}
		}
	}

	public void Filein() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(HMM.class.getClassLoader().getResourceAsStream("com/gmail/naglfar/the/on/metagame/sample.txt")));
			StreamTokenizer st = new StreamTokenizer(br); // StreamTokenizerオブジェクトの作成

			for (int j = 0; j < state.length; j++) {
				for (int m = 0; m < state.length; m++) {
					for (;;) {
						if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
							a[j][m] = st.nval; // 読み取ったデータを配列に代入
							if (!(a[j][m] == 0.0 || a[j][m] == 1.0)) {
								a[j][m] = Math.exp(a[j][m]);
							}
							break;
						}
					}
				}
			}

			for (int j = 0; j < state.length; j++) {
				for (int m = 0; m < sigma.length; m++) {
					for (;;) {
						if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
							b[j][m] = st.nval; // 読み取ったデータを配列に代入
							if (b[j][m] != 0.0) {
								b[j][m] = Math.exp(b[j][m]);
							}
							break;
						}
					}
				}
			}

			br.close();
		} catch (Exception e) {
			System.out.println(e); // エラーが起きたらエラー内容を表示
		}
	}

	public void ReadyCol() {

		for (int j = 0; j < 200; j++) {
			col[j] = 0;
		}
	}

	public int[] TopicCol(GameAgent agent, int day, int turn, Role role, int s) {
		for (int i = 0; i < 15; i++) {
			if (AgentCol[i] == null) {
				AgentCol[i] = agent;
			}
			if (agent == AgentCol[i]) {
				playlog[i][(day - 1) * 20 + turn] = s;
				m = 0;
				for (int j = 0; j < 200; j++) {
					col[j] = playlog[i][j];
					if (col[j] != 0) {
						col2[m] = col[j];
						m++;
					}
				}
				int[] col3 = new int[m];
				System.arraycopy(col2, 0, col3, 0, col3.length);
				return col3;
			}
		}
		return col;
	}

	@SuppressWarnings("incomplete-switch")
	public int TopicNum(Topic topic) {
		int t = 0;
		switch (topic) {
		case ESTIMATE:
			t = 1;
		case SKIP:
			t = 2;
		case VOTE:
			t = 3;
		case COMINGOUT:
			t = 4;
		case OVER:
			t = 2;
		case AGREE:
			t = 5;
		case DISAGREE:
			t = 6;
		case OPERATOR:
			t = 7;
		case GUARDED:
			t = 8;
		case IDENTIFIED:
			t = 9;
		case DIVINATION:
			t = 10;
		case DIVINED:
			t = 11;
		}
		return t;
	}

	public double HMM1(GameAgent agent, int day, int turn, Role role, Topic topic) {

		double em = 0.0;
		HMM hmm = new HMM();
		hmm.ReadyCol();
		int s = hmm.TopicNum(topic);
		int[] col4 = hmm.TopicCol(agent, day, turn, role, s);
		if (turn >= 3 && day > 1) {
			em = hmm.forwardAlgorithm(col4);
		} else {
			em = 0.5;
		}
		return em;
	}
}
