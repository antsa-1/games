package com.tauhka.portal.profile;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * @author antsa-1 from GitHub 23 Feb 2022
 **/
@ApplicationScoped
@Transactional
public class ProfileService {

	@PersistenceContext
	private EntityManager em;

	public UserProfile findProfile(UUID id) {
		return em.find(UserProfile.class, id);
	}

}
