package com.tauhka.portal.profile;

import java.util.List;
import java.util.logging.Logger;

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

	public Profile fetchProfile(String userName) {
		if (userName.isBlank()) {
			LOGGER.info("ProfileService no userName");
			return null;
		}
		String userId = getUserId(userName);
		if (userId == null) {
			LOGGER.info("ProfileService no userId for" + userName);
			return null;
		}
		User user = em.find(User.class, userId);
		List<Game> tictactoes = fetchGames(userId, 0, 19, Integer.MIN_VALUE, Integer.MAX_VALUE); // 0-19 reserved for tictactoe types
		List<Game> connectFours = fetchGames(userId, 20, 30, Integer.MIN_VALUE, Integer.MAX_VALUE);// 20-30 reserved for connectfours types
		user.setTicatactoes(tictactoes);
		user.setConnectFours(connectFours);
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

	private String getUserId(String userName) {
		try {
			String id = (String) em.createNativeQuery("select id from user where name = :name and status in ('ACTIVE','BOT')").setParameter("name", userName).getSingleResult();
			return id;
		} catch (jakarta.persistence.NoResultException nre) {
			LOGGER.info("No userId for:" + userName);
		}
		return null;
	}
}
