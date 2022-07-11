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
	@JsonbProperty("eightBalls")
	private List<TopPlayer> eightBallsList;
	@JsonbProperty("yatzyClassics")
	private List<TopPlayer> yatzyClassicList;
	@JsonbProperty("yatzyFasts")
	private List<TopPlayer> yatzyFastList;
	@JsonbProperty("yatzySupers")
	private List<TopPlayer> yatzySuperList;
	@JsonbProperty("yatzyHypers")
	private List<TopPlayer> yatzyHyperList;
	@JsonbProperty("instant")
	private Instant fetchInstant;
	@JsonbProperty("totalConnectFours")
	private int totalConnectFours;
	@JsonbProperty("totalTictactoes")
	private int totalTictactoes;
	@JsonbProperty("totalEightBalls")
	private int totalEightBalls;
	@JsonbProperty("totalYatzyClassics")
	private int totalYatzyClassics;
	@JsonbProperty("totalYatzyFasts")
	private int totalYatzyFasts;
	@JsonbProperty("totalYatzySupers")
	private int totalYatzySupers;
	@JsonbProperty("totalYatzyHypers")
	private int totalYatzyHypers;

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

	@JsonbTransient
	public void addEightBallPlayer(TopPlayer topPlayer) {
		if (this.eightBallsList == null) {
			this.eightBallsList = new ArrayList<TopPlayer>();
		}
		this.eightBallsList.add(topPlayer);
	}

	@JsonbTransient
	public void addYatzyClassicPlayer(TopPlayer topPlayer) {
		if (this.yatzyClassicList == null) {
			this.yatzyClassicList = new ArrayList<TopPlayer>();
		}
		this.yatzyClassicList.add(topPlayer);
	}

	@JsonbTransient
	public void addYatzySuperPlayer(TopPlayer topPlayer) {
		if (this.yatzySuperList == null) {
			this.yatzySuperList = new ArrayList<TopPlayer>();
		}
		this.yatzySuperList.add(topPlayer);
	}

	@JsonbTransient
	public void addYatzyFastPlayer(TopPlayer topPlayer) {
		if (this.yatzyFastList == null) {
			this.yatzyFastList = new ArrayList<TopPlayer>();
		}
		this.yatzyFastList.add(topPlayer);
	}

	@JsonbTransient
	public void addYatzyHyperPlayer(TopPlayer topPlayer) {
		if (this.yatzyHyperList == null) {
			this.yatzyHyperList = new ArrayList<TopPlayer>();
		}
		this.yatzyHyperList.add(topPlayer);
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

	public List<TopPlayer> getEightBallsList() {
		return eightBallsList;
	}

	public void setEightBallsList(List<TopPlayer> eightBallsList) {
		this.eightBallsList = eightBallsList;
	}

	public int getTotalEightBalls() {
		return totalEightBalls;
	}

	public void setTotalEightBalls(int totalEightBalls) {
		this.totalEightBalls = totalEightBalls;
	}

	public Instant getFetchInstant() {
		return fetchInstant;
	}

	public List<TopPlayer> getYatzyClassicList() {
		return yatzyClassicList;
	}

	public void setYatzyClassicList(List<TopPlayer> yatzyClassicList) {
		this.yatzyClassicList = yatzyClassicList;
	}

	public List<TopPlayer> getYatzyFastList() {
		return yatzyFastList;
	}

	public void setYatzyFastList(List<TopPlayer> yatzyFastList) {
		this.yatzyFastList = yatzyFastList;
	}

	public List<TopPlayer> getYatzySuperList() {
		return yatzySuperList;
	}

	public void setYatzySuperList(List<TopPlayer> yatzySuperList) {
		this.yatzySuperList = yatzySuperList;
	}

	public List<TopPlayer> getYatzyHyperList() {
		return yatzyHyperList;
	}

	public void setYatzyHyperList(List<TopPlayer> yatzyHyperList) {
		this.yatzyHyperList = yatzyHyperList;
	}

	public int getTotalYatzyClassics() {
		return totalYatzyClassics;
	}

	public void setTotalYatzyClassics(int totalYatzyClassics) {
		this.totalYatzyClassics = totalYatzyClassics;
	}

	public int getTotalYatzyFasts() {
		return totalYatzyFasts;
	}

	public void setTotalYatzyFasts(int totalYatzyFasts) {
		this.totalYatzyFasts = totalYatzyFasts;
	}

	public int getTotalYatzySupers() {
		return totalYatzySupers;
	}

	public void setTotalYatzySupers(int totalYatzySupers) {
		this.totalYatzySupers = totalYatzySupers;
	}

	public int getTotalYatzyHypers() {
		return totalYatzyHypers;
	}

	public void setTotalYatzyHypers(int totalYatzyHypers) {
		this.totalYatzyHypers = totalYatzyHypers;
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
