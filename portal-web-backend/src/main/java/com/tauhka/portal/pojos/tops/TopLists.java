package com.tauhka.portal.pojos.tops;

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
