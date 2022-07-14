package com.tauhka.portal.profile;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author antsa-1 from GitHub 11 Jul 2022
 **/

@Entity
@Table(name = "yatzy_game")
public class YatzyGameRow implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "game_id")
	@JsonbProperty("gameId")
	private String gameId;

	@Id
	@Column(name = "game_type")
	@JsonbProperty("gameType")
	private Integer gameType;

	@JsonbTransient
	@Column(name = "player1_id")
	private String player1Id;

	@JsonbTransient
	@Column(name = "player2_id")
	private String player2Id;
	@JsonbTransient
	@Column(name = "player3_id")
	private String player3Id;
	@JsonbTransient
	@Column(name = "player4_id")
	private String player4Id;

	@Column(name = "player1_start_ranking")
	@JsonbProperty("player1StartRanking")
	private Double player1StartRanking;

	@Column(name = "player2_start_ranking")
	@JsonbProperty("player2StartRanking")
	private Double player2StartRanking;

	@Column(name = "player3_start_ranking")
	@JsonbProperty("player3StartRanking")
	private Double player3StartRanking;

	@Column(name = "player4_start_ranking")
	@JsonbProperty("player4StartRanking")
	private Double player4StartRanking;

	@Column(name = "player1_end_ranking")
	@JsonbProperty("player1EndRanking")
	private Double player1EndRanking;

	@Column(name = "player2_end_ranking")
	@JsonbProperty("player2EndRanking")
	private Double player2EndRanking;

	@Column(name = "player3_end_ranking")
	@JsonbProperty("player3EndRanking")
	private Double player3EndRanking;

	@Column(name = "player4_end_ranking")
	@JsonbProperty("player4EndRanking")
	private Double player4EndRanking;

	@Column(name = "player1_name")
	@JsonbProperty("player1Name")
	private String player1Name;

	@Column(name = "player2_name")
	@JsonbProperty("player2Name")
	private String player2Name;

	@Column(name = "player3_name")
	@JsonbProperty("player3Name")
	private String player3Name;

	@Column(name = "player4_name")
	@JsonbProperty("player4Name")
	private String player4Name;

	@Column(name = "player1_score")
	@JsonbProperty("player1Score")
	private Integer player1Score;
	@Column(name = "player2_score")
	@JsonbProperty("player2Score")
	private Integer player2Score;
	@Column(name = "player3_score")
	@JsonbProperty("player3Score")
	private Integer player3Score;
	@Column(name = "player4_score")
	@JsonbProperty("player4Score")
	private Integer player4Score;

	@Column(name = "timecontrol_seconds")
	@JsonbProperty("timeControlSeconds")
	private Integer timeControlSeconds;

	@Column(name = "finish_status")
	@JsonbProperty("finishStatus")
	private String finishStatus;

	@Column(name = "start_time")
	@Convert(converter = InstantConverter.class)
	@JsonbProperty("startInstant")
	private Instant startInstant;

	@Convert(converter = InstantConverter.class)
	@Column(name = "end_time")
	@JsonbProperty("endInstant")
	private Instant endInstant;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	public String getPlayer1Id() {
		return player1Id;
	}

	public String getFinishStatus() {
		return finishStatus;
	}

	public void setFinishStatus(String finishStatus) {
		this.finishStatus = finishStatus;
	}

	public void setPlayer1Id(String player1Id) {
		this.player1Id = player1Id;
	}

	public String getPlayer2Id() {
		return player2Id;
	}

	public void setPlayer2Id(String player2Id) {
		this.player2Id = player2Id;
	}

	public String getPlayer3Id() {
		return player3Id;
	}

	public void setPlayer3Id(String player3Id) {
		this.player3Id = player3Id;
	}

	public String getPlayer4Id() {
		return player4Id;
	}

	public void setPlayer4Id(String player4Id) {
		this.player4Id = player4Id;
	}

	public Double getPlayer1StartRanking() {
		return player1StartRanking;
	}

	public void setPlayer1StartRanking(Double player1StartRanking) {
		this.player1StartRanking = player1StartRanking;
	}

	public Double getPlayer2StartRanking() {
		return player2StartRanking;
	}

	public void setPlayer2StartRanking(Double player2StartRanking) {
		this.player2StartRanking = player2StartRanking;
	}

	public Double getPlayer3StartRanking() {
		return player3StartRanking;
	}

	public void setPlayer3StartRanking(Double player3StartRanking) {
		this.player3StartRanking = player3StartRanking;
	}

	public Double getPlayer4StartRanking() {
		return player4StartRanking;
	}

	public void setPlayer4StartRanking(Double player4StartRanking) {
		this.player4StartRanking = player4StartRanking;
	}

	public Double getPlayer1EndRanking() {
		return player1EndRanking;
	}

	public void setPlayer1EndRanking(Double player1EndRanking) {
		this.player1EndRanking = player1EndRanking;
	}

	public Double getPlayer2EndRanking() {
		return player2EndRanking;
	}

	public void setPlayer2EndRanking(Double player2EndRanking) {
		this.player2EndRanking = player2EndRanking;
	}

	public Double getPlayer3EndRanking() {
		return player3EndRanking;
	}

	public void setPlayer3EndRanking(Double player3EndRanking) {
		this.player3EndRanking = player3EndRanking;
	}

	public Double getPlayer4EndRanking() {
		return player4EndRanking;
	}

	public void setPlayer4EndRanking(Double player4EndRanking) {
		this.player4EndRanking = player4EndRanking;
	}

	public String getPlayer1Name() {
		return player1Name;
	}

	public void setPlayer1Name(String player1Name) {
		this.player1Name = player1Name;
	}

	public String getPlayer2Name() {
		return player2Name;
	}

	public void setPlayer2Name(String player2Name) {
		this.player2Name = player2Name;
	}

	public String getPlayer3Name() {
		return player3Name;
	}

	public void setPlayer3Name(String player3Name) {
		this.player3Name = player3Name;
	}

	public String getPlayer4Name() {
		return player4Name;
	}

	public void setPlayer4Name(String player4Name) {
		this.player4Name = player4Name;
	}

	public Integer getPlayer1Score() {
		return player1Score;
	}

	public void setPlayer1Score(Integer player1Score) {
		this.player1Score = player1Score;
	}

	public Integer getPlayer2Score() {
		return player2Score;
	}

	public void setPlayer2Score(Integer player2Score) {
		this.player2Score = player2Score;
	}

	public Integer getPlayer3Score() {
		return player3Score;
	}

	public void setPlayer3Score(Integer player3Score) {
		this.player3Score = player3Score;
	}

	public Integer getPlayer4Score() {
		return player4Score;
	}

	public void setPlayer4Score(Integer player4Score) {
		this.player4Score = player4Score;
	}

	public Integer getTimeControlSeconds() {
		return timeControlSeconds;
	}

	public void setTimeControlSeconds(Integer timeControlSeconds) {
		this.timeControlSeconds = timeControlSeconds;
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

	@Override
	public int hashCode() {
		return Objects.hash(gameId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YatzyGameRow other = (YatzyGameRow) obj;
		return Objects.equals(gameId, other.gameId);
	}

}
