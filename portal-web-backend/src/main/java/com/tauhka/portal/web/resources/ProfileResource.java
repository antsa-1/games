package com.tauhka.portal.web.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.tauhka.portal.profile.UserProfile;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author antsa-1 from GitHub 23 Feb 2022
 * 
 * 
 **/

@Path("/profile")
public class ProfileResource {
	private static final Logger LOGGER = Logger.getLogger(ProfileResource.class.getName());

	@PersistenceContext(unitName = "profile-persistence-unit", type = PersistenceContextType.TRANSACTION)
	private EntityManager em;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{username}")
	public Response getProfile(@PathParam("username") String username) {
		LOGGER.entering(ProfileResource.class.getName(), "getProfile");
		try {
			UserProfile userProfile = em.find(UserProfile.class, username);
			if (userProfile == null) {
				Response.status(204).build();
			}
			Jsonb jsonb = JsonbBuilder.create();
			userProfile.setMemberSince(userProfile.getCreated().toInstant());
			String userProfileInJSON = jsonb.toJson(userProfile, UserProfile.class);
			return Response.ok(userProfileInJSON).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "TopListsResource failed", e);
		} finally {
			LOGGER.exiting(ProfileResource.class.getName(), "getProfile");
		}
		LOGGER.exiting("TopListsResource", " getTopPlayers");
		return Response.serverError().build();
	}
}
