package com.tauhka.games.messaging;

import java.util.List;

import com.tauhka.games.yatzy.Dice;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 8 Jun 2022
 **/

public class YatzyMessage {
	@JsonbProperty
	public List<Dice> dices;

	@JsonbProperty("whoPlayed")
	private String whoPlayed;

	public List<Dice> getDices() {
		return dices;
	}

	public void setDices(List<Dice> dices) {
		this.dices = dices;
	}

	public String getWhoPlayed() {
		return whoPlayed;
	}

	public void setWhoPlayed(String whoPlayed) {
		this.whoPlayed = whoPlayed;
	}

}
