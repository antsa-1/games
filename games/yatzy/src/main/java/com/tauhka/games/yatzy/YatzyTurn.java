package com.tauhka.games.yatzy;

import java.io.Serializable;
import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 6 Jun 2022
 **/

public class YatzyTurn implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonbProperty(value ="yatzyAction")
	private YatzyAction yatzyAction;
	@JsonbProperty(value ="diceIds")
	private List<String> diceIds;
	@JsonbProperty(value ="handType")
	private HandType handType;

	public YatzyAction getYatzyAction() {
		return yatzyAction;
	}

	public void setYatzyAction(YatzyAction yatzyAction) {
		this.yatzyAction = yatzyAction;
	}

	public List<String> getDiceIds() {
		return diceIds;
	}

	public void setDiceIds(List<String> diceIds) {
		this.diceIds = diceIds;
	}

	public HandType getHandType() {
		return handType;
	}

	public void setHandType(HandType handType) {
		this.handType = handType;
	}

}
