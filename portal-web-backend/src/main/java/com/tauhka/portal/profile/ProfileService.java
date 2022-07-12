package com.tauhka.portal.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.tauhka.games.core.stats.Player;
import com.tauhka.portal.util.Validators;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

/**
 * @author antsa-1 from GitHub 1 Mar 2022
 **/

@ApplicationScoped
@Transactional
public class ProfileService {
	private static final Logger LOGGER = Logger.getLogger(ProfileService.class.getName());
	@PersistenceContext
	private EntityManager em;

	// 03.02.2022 NOTE: Username/profiletext(still todo), can contain XSS, SQLi, Html-injections.
	// Mitigations for SQLi/XSS/HTML-injections -> prepared statements, VUE 3 character escaping
	// https://vuejs.org/guide/best-practices/security.html#potential-dangers
	public Profile fetchProfile(String userName) {
		Validators.validateUserName(userName);
		String userId = fetchUserId(userName);
		if (userId == null) {
			LOGGER.info("ProfileService no userId for" + userName);
			return null;
		}
		User user = em.find(User.class, userId);
		List<TwoPlayerGameRow> tictactoes = fetchTwoPlayerGames(userId, 0, 19, Integer.MIN_VALUE, Integer.MAX_VALUE); // 0-19 reserved for tictactoe types boardSize etc.
		List<TwoPlayerGameRow> connectFours = fetchTwoPlayerGames(userId, 20, 29, Integer.MIN_VALUE, Integer.MAX_VALUE);// 20-29 reserved for connectfours types, boardSize etc.
		List<TwoPlayerGameRow> eightBalls = fetchTwoPlayerGames(userId, 30, 32, Integer.MIN_VALUE, Integer.MAX_VALUE);// 30 + reserved for pool types.
		List<YatzyGameRow> yatzyClassics = fetchYatzys(userId, 40, 40, 0, 20); // First 20 games of each yatzy
		List<YatzyGameRow> yatzyFast = fetchYatzys(userId, 41, 41, 0, 20); // First 20 games of each yatzy
		List<YatzyGameRow> yatzySuper = fetchYatzys(userId, 42, 42, 0, 20); // First 20 games of each yatzy
		List<YatzyGameRow> yatzyHyper = fetchYatzys(userId, 43, 43, 0, 20); // First 20 games of each yatzy
		user.setTicatactoes(convertTwoPlayerRowToGame(tictactoes));
		user.setConnectFours(convertTwoPlayerRowToGame(connectFours));
		user.setEightBalls(convertTwoPlayerRowToGame(eightBalls));
		user.setYatzyClassics(convertYatzyRowToGame(yatzyClassics));
		user.setYatzyFasts(convertYatzyRowToGame(yatzyFast));
		user.setYatzySupers(convertYatzyRowToGame(yatzySuper));
		user.setYatzyHypers(convertYatzyRowToGame(yatzyHyper));
		return new Profile(user);
	}

	private List<Game> convertTwoPlayerRowToGame(List<TwoPlayerGameRow> games) {
		List<Game> convertedGames = new ArrayList<Game>();
		for (TwoPlayerGameRow row : games) {
			List<Player> players = new ArrayList<Player>(2);
			Game game = new Game();
			game.setGameId(row.getGameId());
			game.setGameType(row.getGameType());
			game.setStartInstant(row.getStartInstant());
			game.setEndInstant(row.getEndInstant());
			game.setWinner(row.getWinnerUsername());
			game.setResult(row.getResult());
			game.setDescription(row.getBoardDescription());
			Player p = new com.tauhka.games.core.stats.Player();
			p.setName(row.getPlayerAName());
			p.setInitialRanking(row.getPlayerAStartRanking());
			p.setFinalRanking(row.getPlayerAEndRanking());
			players.add(p);

			Player p2 = new Player();
			p2.setName(row.getPlayerBName());
			p2.setInitialRanking(row.getPlayerBStartRanking());
			p2.setFinalRanking(row.getPlayerBEndRanking());
			players.add(p2);
			game.setPlayers(players);
			convertedGames.add(game);
		}
		return convertedGames;

	}

	private List<Game> convertYatzyRowToGame(List<YatzyGameRow> games) {
		List<Game> convertedGames = new ArrayList<Game>();
		for (YatzyGameRow row : games) {
			List<Player> players = new ArrayList<Player>(4);
			Game game = new Game();
			game.setGameId(row.getGameId());
			game.setGameType(row.getGameType());
			game.setStartInstant(row.getStartInstant());
			game.setEndInstant(row.getEndInstant());
			game.setTimeControlSeconds(row.getTimeControlSeconds());
			game.setFinishStatus(row.getFinishStatus());
			com.tauhka.games.core.stats.Player p = new com.tauhka.games.core.stats.Player();
			p.setName(row.getPlayer1Name());
			p.setInitialRanking(row.getPlayer1StartRanking());
			p.setFinalRanking(row.getPlayer1EndRanking());
			p.setFinishPosition(1);
			p.setScore(row.getPlayer1Score());
			players.add(p);

			Player p2 = new Player();
			p2.setName(row.getPlayer2Name());
			p2.setInitialRanking(row.getPlayer2StartRanking());
			p2.setFinalRanking(row.getPlayer2EndRanking());
			p2.setFinishPosition(2);
			p2.setScore(row.getPlayer2Score());
			players.add(p2);

			if (row.getPlayer3Name() != null) {
				Player p3 = new Player();
				p3.setName(row.getPlayer3Name());
				p3.setInitialRanking(row.getPlayer3StartRanking());
				p3.setFinalRanking(row.getPlayer3EndRanking());
				p3.setFinishPosition(3);
				p3.setScore(row.getPlayer3Score());
				players.add(p3);
			}
			if (row.getPlayer4Name() != null) {
				Player p4 = new Player();
				p4.setName(row.getPlayer4Name());
				p4.setInitialRanking(row.getPlayer4StartRanking());
				p4.setFinalRanking(row.getPlayer4EndRanking());
				p4.setFinishPosition(4);
				p4.setScore(row.getPlayer4Score());
				players.add(p4);
			}
			game.setPlayers(players);
			convertedGames.add(game);
		}
		return convertedGames;
	}

	private List<TwoPlayerGameRow> fetchTwoPlayerGames(String userId, int gameTypeFrom, int gameTypeTo, int gamesFrom, int gamesTo) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<TwoPlayerGameRow> criteriaQuery = criteriaBuilder.createQuery(TwoPlayerGameRow.class);
		Root<TwoPlayerGameRow> gameRoot = criteriaQuery.from(TwoPlayerGameRow.class);
		Predicate gameTypePredicate = criteriaBuilder.between(gameRoot.get("gameType"), gameTypeFrom, gameTypeTo);
		Predicate playerAPredicate = criteriaBuilder.equal(gameRoot.get("playerAId"), userId);
		Predicate playerBPredicate = criteriaBuilder.equal(gameRoot.get("playerBId"), userId);
		Predicate playerAorPlayerBPredicate = criteriaBuilder.or(playerAPredicate, playerBPredicate);
		Predicate gameTypeAndPlayerPredicate = criteriaBuilder.and(playerAorPlayerBPredicate, gameTypePredicate);
		Order order = criteriaBuilder.desc(gameRoot.get("startInstant"));
		criteriaQuery.orderBy(order);
		criteriaQuery.where(gameTypeAndPlayerPredicate);
		List<TwoPlayerGameRow> games = em.createQuery(criteriaQuery).getResultList();
		return games;
	}

	private List<YatzyGameRow> fetchYatzys(String userId, int gameTypeFrom, int gameTypeTo, int gamesFrom, int gamesTo) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<YatzyGameRow> criteriaQuery = criteriaBuilder.createQuery(YatzyGameRow.class);
		Root<YatzyGameRow> gameRoot = criteriaQuery.from(YatzyGameRow.class);
		Predicate gameTypePredicate = criteriaBuilder.between(gameRoot.get("gameType"), gameTypeFrom, gameTypeTo);
		Predicate player1Predicate = criteriaBuilder.equal(gameRoot.get("player1Id"), userId);
		Predicate player2Predicate = criteriaBuilder.equal(gameRoot.get("player2Id"), userId);
		Predicate player3Predicate = criteriaBuilder.equal(gameRoot.get("player3Id"), userId);
		Predicate player4Predicate = criteriaBuilder.equal(gameRoot.get("player4Id"), userId);

		Predicate anyOfThePlayersPredicate = criteriaBuilder.or(player1Predicate, player2Predicate, player3Predicate, player4Predicate);
		Predicate gameTypeAndPlayerPredicate = criteriaBuilder.and(anyOfThePlayersPredicate, gameTypePredicate);
		Order order = criteriaBuilder.desc(gameRoot.get("startInstant"));
		criteriaQuery.orderBy(order);
		criteriaQuery.where(gameTypeAndPlayerPredicate);
		List<YatzyGameRow> games = em.createQuery(criteriaQuery).setMaxResults(20).getResultList();
		return games;
	}

	private String fetchUserId(String userName) {
		try {
			String id = (String) em.createNativeQuery("select id from user where name = :name and status in ('ACTIVE','BOT')").setParameter("name", userName).getSingleResult();
			return id;
		} catch (jakarta.persistence.NoResultException nre) {
			LOGGER.info("No userId for:" + userName);
		}
		return null;
	}
}
