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
	@JsonbProperty("ranking")
	private int ranking;
	@JsonbProperty("playedGames")
	private int playedGames;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public int getPlayedGames() {
		return playedGames;
	}

	public void setPlayedGames(int playedGames) {
		this.playedGames = playedGames;
	}

}
