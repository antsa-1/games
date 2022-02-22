package com.tauhka.portal.highscore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * @author antsa-1 from GitHub 19 Feb 2022
 **/

public class TopLists {

	@JsonbProperty("connectFours")
	private List<TopPlayer> connectFourList;
	@JsonbProperty("tictactoes")
	private List<TopPlayer> ticTacToeList;
	@JsonbProperty("instant")
	private Instant fetchInstant;
	@JsonbProperty("totalConnectFours")
	private int totalConnectFours;
	@JsonbProperty("totalTictactoes")
	private int totalTictactoes;

	@JsonbTransient
	public void addConnectFourPlayer(TopPlayer topPlayer) {
		if (this.connectFourList == null) {
			this.connectFourList = new ArrayList<TopPlayer>();
		}
		this.connectFourList.add(topPlayer);
	}

	@JsonbTransient
	public void addTicTacToePlayer(TopPlayer topPlayer) {
		if (this.ticTacToeList == null) {
			this.ticTacToeList = new ArrayList<TopPlayer>();
		}
		this.ticTacToeList.add(topPlayer);
	}

	public List<TopPlayer> getConnectFourList() {
		return connectFourList;
	}

	public int getTotalConnectFours() {
		return totalConnectFours;
	}

	public void setTotalConnectFours(int totalConnectFours) {
		this.totalConnectFours = totalConnectFours;
	}

	public int getTotalTictactoes() {
		return totalTictactoes;
	}

	public void setTotalTictactoes(int totalTictactoes) {
		this.totalTictactoes = totalTictactoes;
	}

	public Instant getFetchInstant() {
		return fetchInstant;
	}

	public void setFetchInstant(Instant fetchInstant) {
		this.fetchInstant = fetchInstant;
	}

	public void setConnectFourList(List<TopPlayer> connectFourList) {
		this.connectFourList = connectFourList;
	}

	public List<TopPlayer> getTicTacToeList() {
		return ticTacToeList;
	}

	public void setTicTacToeList(List<TopPlayer> ticTacToeList) {
		this.ticTacToeList = ticTacToeList;
	}

}
