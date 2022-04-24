package com.tauhka.portal.profile;

import java.util.List;
import java.util.logging.Logger;

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
		List<Game> tictactoes = fetchGames(userId, 0, 19, Integer.MIN_VALUE, Integer.MAX_VALUE); // 0-19 reserved for tictactoe types boardSize etc.
		List<Game> connectFours = fetchGames(userId, 20, 29, Integer.MIN_VALUE, Integer.MAX_VALUE);// 20-29 reserved for connectfours types, boardSize etc.
		List<Game> eightBalls = fetchGames(userId, 30, 32, Integer.MIN_VALUE, Integer.MAX_VALUE);// 30 + reserved for pool types.
		user.setTicatactoes(tictactoes);
		user.setConnectFours(connectFours);
		user.setEightBalls(eightBalls);
		return new Profile(user);
	}

	private List<Game> fetchGames(String userId, int gameTypeFrom, int gameTypeTo, int gamesFrom, int gamesTo) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Game> criteriaQuery = criteriaBuilder.createQuery(Game.class);
		Root<Game> gameRoot = criteriaQuery.from(Game.class);
		Predicate gameTypePredicate = criteriaBuilder.between(gameRoot.get("gameType"), gameTypeFrom, gameTypeTo);
		Predicate playerAPredicate = criteriaBuilder.equal(gameRoot.get("playerAId"), userId);
		Predicate playerBPredicate = criteriaBuilder.equal(gameRoot.get("playerBId"), userId);
		Predicate playerAorPlayerBPredicate = criteriaBuilder.or(playerAPredicate, playerBPredicate);
		Predicate gameTypeAndPlayerPredicate = criteriaBuilder.and(playerAorPlayerBPredicate, gameTypePredicate);
		Order order = criteriaBuilder.desc(gameRoot.get("startInstant"));
		criteriaQuery.orderBy(order);
		criteriaQuery.where(gameTypeAndPlayerPredicate);
		List<Game> games = em.createQuery(criteriaQuery).getResultList();
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
