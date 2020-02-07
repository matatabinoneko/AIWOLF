package com.gmail.k14.itolab.aiwolf.action;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import com.gmail.k14.itolab.aiwolf.data.ForecastMap;
import com.gmail.k14.itolab.aiwolf.data.MyTalking;
import com.gmail.k14.itolab.aiwolf.data.OwnData;
import com.gmail.k14.itolab.aiwolf.data.VoteCounter;
import com.gmail.k14.itolab.aiwolf.util.Check;
import com.gmail.k14.itolab.aiwolf.util.CountAgent;
import com.gmail.k14.itolab.aiwolf.util.RandomSelect;
import com.gmail.k14.itolab.aiwolf.util.TalkFactory;

/**
 * 一般的な発言をまとめたクラス<br>
 * 複数役職で共通で用いることができる発言や行動がまとめられている<br>
 * 主にフラグの管理を考慮したものが多い
 * @author k14096kk
 *
 */
public class GeneralAction {

	/**
	 *「人狼予想」発言を作成する<br>
	 * 呼び出した日付に報告された占い結果を参照する．<br>
	 * 参照結果の中で黒発見がいる場合，疑い度最小の占い師が報告した占い対象に対して，人狼予想する<br>
	 * 黒発見がいない場合，疑い度最大のエージェントに対して，人狼予想する<br>
	 * @param forecastMap :予想一覧
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @return 予想対象エージェント
	 */
	public static Agent sayEstimateWolf(ForecastMap forecastMap, OwnData ownData, MyTalking myTalking) {
		// 渡す予想先エージェント
		Agent estimateTarget = null;
		// 今日された占い報告をリスト化
		List<Judge> judgeList = forecastMap.getDivineJudgeDay(ownData.getDay());
		// 占い結果が狼のJudgeを格納するリスト
		List<Judge> wolfList = new ArrayList<>();
		
		// 狼を見つけたJudgeがあれば格納
		for(Judge judge: judgeList) {
			if(Check.isSpecies(judge.getResult(), Species.WEREWOLF)) {
				wolfList.add(judge);
			}
		}
		
		// 報告が無ければ疑い度最大のエージェントを投票
		if(wolfList.isEmpty()) {
			// 自分を除いた疑い度降順エージェントリスト
			List<Agent> voteList = forecastMap.moreDoubtAgentList(ownData.getAliveOtherAgentList());
			// 疑い度最大エージェントを予想対象
			estimateTarget = voteList.get(0);
		}else {
			List<Agent> seerList = new ArrayList<>();
			for(Judge judge: wolfList) {
				// 狼を報告した占い師のエージェントリストを取得
				seerList.add(judge.getAgent());
			}
			// 疑い度が小さい順にエージェントを並べ
			seerList = forecastMap.fewerDoubtAgentList(seerList);
			
			if(!seerList.isEmpty()) {
				for(Judge judge: wolfList) {
					// 疑い度が最小の占い師の報告対象を投票対象とする
					if(Check.isAgent(judge.getAgent(), seerList.get(0))) {
						estimateTarget = judge.getAgent();
					}
				}
			}
		}
		// 人狼予想
		myTalking.addTalk(TalkFactory.estimateRemark(estimateTarget, Role.WEREWOLF));
		
		return estimateTarget;
	}
	
	/**
	 * 「占い先リクエスト」発言を作成する<br>
	 * 自分と投票対象以外の中で疑い度が最大のエージェントを投票対象とする
	 * @param forecastMap :予想一覧マップ
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param removeAgent :除外エージェント
	 */
	public static void sayDoubtRequestDivine(ForecastMap forecastMap, OwnData ownData, MyTalking myTalking, Agent removeAgent) {
		// 自分と除外対象を除いたエージェントの中から疑い度が高い順にエージェントを並び替え
		List<Agent> doubtList = ownData.getAliveOtherAgentList();
		doubtList.remove(removeAgent);
		doubtList = forecastMap.moreDoubtAgentList(doubtList);
		// 疑い度が最大エージェントを占い対象とするリクエスト
		myTalking.addTalk(TalkFactory.requestAllDivinationRemark(doubtList.get(0)));
	}
	
	/**
	 *「投票」発言を作成する<br>
	 * 投票発言と投票済みフラグをセット
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 */
	public static void sayVote(OwnData ownData, MyTalking myTalking) {
		// 投票対象が決まっていないならばランダム
		if(Check.isNull(ownData.getVoteTarget())) {
			ownData.setVoteTarget(RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList()));
		}
		// 投票発言
		myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
		ownData.setActFlagVote();
	}
	
	/**
	 *「投票」発言を作成する<br>
	 * 投票発言と投票済みフラグをセット
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param target :投票対象
	 */
	public static void sayVote(OwnData ownData, MyTalking myTalking, Agent target) {
		// 投票対象をtargetに設定
		ownData.setVoteTarget(target);
		// 投票対象が決まっていないならばランダム
		if(Check.isNull(ownData.getVoteTarget())) {
			ownData.setVoteTarget(RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList()));
		}
		// 投票発言
		myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
		ownData.setActFlagVote();
	}
	
	/**
	 *「投票」発言を作成する<br>
	 * 投票発言と投票済みフラグをセット<br>
	 * 投票済みフラグがTrueならば投票対象は上書きされず，発言しない
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param target :投票対象
	 */
	public static void sayLowVote(OwnData ownData, MyTalking myTalking, Agent target) {
		// フラグがTrueならば何もしない
		if(!ownData.isVote()) {
			// 投票対象をtargetに設定
			ownData.setVoteTarget(target);
			// 投票対象が決まっていないならばランダム
			if (Check.isNull(ownData.getVoteTarget())) {
				ownData.setVoteTarget(RandomSelect.randomAgentSelect(ownData.getAliveOtherAgentList()));
			}
			// 投票発言
			myTalking.addTalk(TalkFactory.voteRemark(ownData.getVoteTarget()));
			ownData.setActFlagVote();
		}
		
	}
	
	/**
	 *「自分のCO」発言を作成する<br>
	 * CO発言をしたらCOフラグをたてる
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param role :CO役職
	 */
	public static void sayComingout(OwnData ownData, MyTalking myTalking, Role role) {
		if(ownData.isCO()) {
			return;
		}
		myTalking.addTalk(TalkFactory.comingoutRemark(ownData.getMe(), role));
		ownData.setActFlagCO();
	}
	
	/**
	 *「占い結果」発言を作成する<br>
	 * 占い結果発言をしたらフラグを報告済みにする
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param judge :占いのJudge 
	 */
	public static void sayDivine(OwnData ownData, MyTalking myTalking, Judge judge) {
		if(ownData.isDivine()) {
			return;
		}
		myTalking.addTalk(TalkFactory.divinedResultRemark(judge.getTarget(), judge.getResult()));
		ownData.setActFlagDivine();
	}
	
	/**
	 *「占い結果」発言を作成する<br>
	 * 占い結果発言をしたらフラグを報告済みにする
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param target :占い対象
	 * @param result :占い結果
	 */
	public static void sayDivine(OwnData ownData, MyTalking myTalking, Agent target, Species result) {
		if(ownData.isDivine()) {
			return;
		}
		myTalking.addTalk(TalkFactory.divinedResultRemark(target, result));
		ownData.setActFlagDivine();
	}
	
	/**
	 * 「霊媒結果」発言を作成する<br>
	 * 霊媒結果発言をしたらフラグを報告済みにする<br>
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param judge :霊媒のJudge
	 */
	public static void sayIdent(OwnData ownData, MyTalking myTalking, Judge judge) {
		if(ownData.isIdent()) {
			return;
		}
		myTalking.addTopTalk(TalkFactory.identRemark(judge.getTarget(), judge.getResult()));
		ownData.setActFlagIdent();
	}
	
	/**
	 * 「霊媒結果」発言を作成する<br>
	 * 霊媒結果発言をしたらフラグを報告済みにする<br>
	 * @param ownData :データ
	 * @param myTalking :発言キュー
	 * @param target :占い対象
	 * @param result :占い結果
	 */
	public static void sayIdent(OwnData ownData, MyTalking myTalking, Agent target, Species result) {
		if(ownData.isIdent()) {
			return;
		}
		myTalking.addTopTalk(TalkFactory.identRemark(target, result));
		ownData.setActFlagIdent();
	}
	
	/**
	 * 投票と投票リクエストの数に便乗して投票対象を決定する(多数決)<br>
	 * 投票された回数が最大かつ疑い度が高めならば投票対象にする<br>
	 * 注意：投票対象を決めるもので，vote発言はまだしていない
	 * @param forecastMap :予想一覧
	 * @param ownData :データ
	 * @param voteCounter :投票回数
	 */
	public static void decideAvailVote(ForecastMap forecastMap, OwnData ownData, VoteCounter voteCounter) {
		// 投票とリクエストの合計回数が多い順のリスト(自分の除く)
		List<CountAgent> voteCountList = voteCounter.moreCountList(voteCounter.getAllMap());
		voteCountList.remove(ownData.getMe());
		
		if(!voteCountList.isEmpty()) {
			// 生存人数の半分以上投票&リクエストされているか
			if(voteCountList.get(0).count >= (ownData.getAliveAgentList().size()/2)) {
				// 対象が疑い度が高めであれば，便乗して投票対象決定
				if(forecastMap.getDoubt(voteCountList.get(0).agent) >= 0.2) {
					ownData.setVoteTarget(voteCountList.get(0).agent);
				}
			}
		}
	}
	
}
