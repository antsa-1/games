package com.tauhka.portal.profile;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
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

	@Inject
	private ProfileService profileService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{username}")
	public Response getProfile(@PathParam("username") String username) {
		LOGGER.entering(ProfileResource.class.getName(), "getProfile");
		try {
			Profile userProfile = profileService.fetchProfile(username);
			if (userProfile == null) {
				return Response.noContent().build();
			}
			Jsonb jsonb = JsonbBuilder.create();
			String userProfileInJSON = jsonb.toJson(userProfile, User.class);
			return Response.ok(userProfileInJSON).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "ProfileResource fetch failed", e);
		} finally {
			LOGGER.exiting(ProfileResource.class.getName(), "getProfile");
		}
		return Response.serverError().build();
	}
}
