package com.tauhka.portal.web.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.tauhka.portal.ejb.UserEJB;
import com.tauhka.portal.pojos.tops.TopLists;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author antsa-1 from GitHub 19 Feb 2022
 **/

@Path("/toplists")
public class TopListsResource {
	private static final Logger LOGGER = Logger.getLogger(TopListsResource.class.getName());
	@Inject
	private UserEJB userEJB;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopPlayers() {
		LOGGER.entering("TopListsResource", " getTopPlayers");
		try {
			TopLists topLists = userEJB.getTopPlayers();
			Jsonb jsonb = JsonbBuilder.create();
			String toplists = jsonb.toJson(topLists, TopLists.class);
			LOGGER.exiting("TopListsResource", " getTopPlayers");
			return Response.ok(toplists).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "TopListsResource failed", e);
		}
		LOGGER.exiting("TopListsResource", " getTopPlayers");
		return Response.serverError().build();
	}

}
