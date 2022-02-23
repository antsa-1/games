package com.tauhka.portal.highscore;

import java.io.Serializable;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 19 Feb 2022
 **/

public class TopPlayer implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonbProperty("nickname")
	private String nickname;
	@JsonbProperty("rankingTicTacToe")
	private int rankingTicTacToe;
	@JsonbProperty("playedTicTacToes")
	private int playedTicTacToes;
	@JsonbProperty("rankingConnectFour")
	private int rankingConnectFour;
	@JsonbProperty("playedConnectFours")
	private int playedConnectFours;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getRankingTicTacToe() {
		return rankingTicTacToe;
	}

	public void setRankingTicTacToe(int rankingTicTacToe) {
		this.rankingTicTacToe = rankingTicTacToe;
	}

	public int getPlayedTicTacToes() {
		return playedTicTacToes;
	}

	public void setPlayedTicTacToes(int playedTicTacToes) {
		this.playedTicTacToes = playedTicTacToes;
	}

	public int getRankingConnectFour() {
		return rankingConnectFour;
	}

	public void setRankingConnectFour(int rankingConnectFour) {
		this.rankingConnectFour = rankingConnectFour;
	}

	public int getPlayedConnectFours() {
		return playedConnectFours;
	}

	public void setPlayedConnectFours(int playedConnectFours) {
		this.playedConnectFours = playedConnectFours;
	}

}
