package com.tauhka.portal.profile;

import java.io.Serializable;
import java.time.Instant;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author antsa-1 from GitHub 24 Feb 2022
 **/
@Entity
@Table(name = "game")
public class Game implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "game_id")
	@JsonbProperty("gameId")
	private String gameId;

	@JsonbTransient
	@Column(name = "playera_id")
	private String playerAId;

	@JsonbTransient
	@Column(name = "playerb_id")
	private String playerBId;

	@Column(name = "playera_start_ranking")
	@JsonbProperty("playerAStartRanking")
	private Integer playerAStartRanking;

	@Column(name = "winner_username")
	@JsonbProperty("winnerUsername")
	private String winnerUsername;

	@Column(name = "playerb_start_ranking")
	@JsonbProperty("playerBStartRanking")
	private Integer playerBStartRanking;

	@Column(name = "playera_end_ranking")
	@JsonbProperty("playerAEndRanking")
	private Integer playerAEndRanking;

	@Column(name = "playerb_end_ranking")
	@JsonbProperty("playerBEndRanking")
	private Integer playerBEndRanking;

	@Column(name = "playera_username")
	@JsonbProperty("playerAName")
	private String playerAName;

	@Column(name = "playerb_username")
	@JsonbProperty("playerBName")
	private String playerBName;

	@Column(name = "game_type")
	@JsonbProperty("gameType")
	private Integer gameType;

	@Column(name = "result")
	@JsonbProperty("result")
	private Integer result;

	@Column(name = "start_time")
	@Convert(converter = InstantConverter.class)
	@JsonbProperty("startInstant")
	private Instant startInstant;

	@Convert(converter = InstantConverter.class)
	@Column(name = "end_time")
	@JsonbProperty("endInstant")
	private Instant endInstant;

	@Convert(converter = BoardToTextConverter.class)
	@Column(name = "game_type", insertable=false, updatable=false)
	@JsonbProperty("boardDescription")
	private String boardDescription;
	
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getPlayerAId() {
		return playerAId;
	}

	public String getWinnerUsername() {
		return winnerUsername;
	}

	public void setWinnerUsername(String winnerUsername) {
		this.winnerUsername = winnerUsername;
	}

	public void setPlayerAId(String playerAId) {
		this.playerAId = playerAId;
	}

	public String getPlayerBId() {
		return playerBId;
	}

	public void setPlayerBId(String playerBId) {
		this.playerBId = playerBId;
	}

	public Integer getPlayerAStartRanking() {
		return playerAStartRanking;
	}

	public void setPlayerAStartRanking(Integer playerAStartRanking) {
		this.playerAStartRanking = playerAStartRanking;
	}

	public Integer getPlayerBStartRanking() {
		return playerBStartRanking;
	}

	public void setPlayerBStartRanking(Integer playerBStartRanking) {
		this.playerBStartRanking = playerBStartRanking;
	}

	public Integer getPlayerAEndRanking() {
		return playerAEndRanking;
	}

	public Instant getStartInstant() {
		return startInstant;
	}

	public void setStartInstant(Instant startInstant) {
		this.startInstant = startInstant;
	}

	public Instant getEndInstant() {
		return endInstant;
	}

	public void setEndInstant(Instant endInstant) {
		this.endInstant = endInstant;
	}

	public void setPlayerAEndRanking(Integer playerAEndRanking) {
		this.playerAEndRanking = playerAEndRanking;
	}

	public Integer getPlayerBEndRanking() {
		return playerBEndRanking;
	}

	public void setPlayerBEndRanking(Integer playerBEndRanking) {
		this.playerBEndRanking = playerBEndRanking;
	}

	public String getPlayerAName() {
		return playerAName;
	}

	public void setPlayerAName(String playerAName) {
		this.playerAName = playerAName;
	}

	public String getPlayerBName() {
		return playerBName;
	}

	public void setPlayerBName(String playerBName) {
		this.playerBName = playerBName;
	}

	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

}
