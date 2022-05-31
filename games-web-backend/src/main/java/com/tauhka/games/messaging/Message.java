package com.tauhka.games.messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tauhka.games.core.Game;
import com.tauhka.games.core.User;
import com.tauhka.games.core.tables.Table;
import com.tauhka.games.core.twodimen.GameResult;

import jakarta.json.bind.annotation.JsonbProperty;

//Base class for Messaging. Different fields are populated based on different situations. MessageTitle is indicator for what fields should/could be read.
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonbProperty("title")
	private MessageTitle title;
	@JsonbProperty("message")
	private String message;
	@JsonbProperty("x") // somewhere else
	private int x; // somewhere else
	@JsonbProperty("y")
	private int y;
	@JsonbProperty("who")
	private User who;
	@JsonbProperty("to")
	private String to;
	@JsonbProperty("from")
	private String from;
	@JsonbProperty("token") // somewhere else ?
	private String token;
	@JsonbProperty("players")
	private List<User> users;
	@JsonbProperty("table")
	private Table table;
	@JsonbProperty("tables")
	private List<Table> tables = new ArrayList<Table>();
	@JsonbProperty("games")
	private List<Game> games;
	@JsonbProperty("gameResult")
	private GameResult gameResult;
	@JsonbProperty("computer")
	private Boolean computer;
	@JsonbProperty("randomStarter")
	private Boolean randomStarter;
	@JsonbProperty("onlyRegistered")
	private Boolean onlyRegistered;
	@JsonbProperty("timeControlIndex")
	private int timeControlIndex;
	@JsonbProperty("pool")
	private PoolMessage poolMessage;
	@JsonbProperty("playerAmount")
	private Integer playerAmount;

	public MessageTitle getTitle() {
		return title;
	}

	public void addTable(Table table) {
		this.tables.add(table);
	}

	public void setTitle(MessageTitle title) {
		this.title = title;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Boolean getComputer() {
		return computer;
	}

	public GameResult getGameResult() {
		return gameResult;
	}

	public void setGameResult(GameResult gameResult) {
		this.gameResult = gameResult;
	}

	public void setComputer(Boolean computer) {
		this.computer = computer;
	}

	public Integer getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public PoolMessage getPoolMessage() {
		return poolMessage;
	}

	public void setPoolMessage(PoolMessage poolMessage) {
		this.poolMessage = poolMessage;
	}

	public Boolean getRandomStarter() {
		return randomStarter;
	}

	public void setRandomStarter(Boolean randomStarter) {
		this.randomStarter = randomStarter;
	}

	public Boolean getOnlyRegistered() {
		return onlyRegistered;
	}

	public Integer getPlayerAmount() {
		return playerAmount;
	}

	public void setPlayerAmount(Integer playerAmount) {
		this.playerAmount = playerAmount;
	}

	public void setOnlyRegistered(Boolean onlyRegistered) {
		this.onlyRegistered = onlyRegistered;
	}

	public List<Game> getGames() {
		return games;
	}

	public void setGames(List<Game> games) {
		this.games = games;
	}

	public String getMessage() {
		return message;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	public User getWho() {
		return who;
	}

	public void setWho(User who) {
		this.who = who;
	}

	public int getTimeControlIndex() {
		return timeControlIndex;
	}

	public void setTimeControlIndex(int timeControlIndex) {
		this.timeControlIndex = timeControlIndex;
	}

	@Override
	public String toString() {
		return "Message [title=" + title + ", message=" + message + ", who=" + who + ", to=" + to + "]";
	}

}
