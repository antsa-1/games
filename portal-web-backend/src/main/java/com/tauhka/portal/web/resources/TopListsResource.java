package com.tauhka.portal.web.resources;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tauhka.portal.cache.PortalCache;
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
	@Inject
	private PortalCache cache;
	private static final int CACHE_WAIT_TIME_SECONDS = 60;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopPlayers() {
		LOGGER.entering("TopListsResource", " getTopPlayers");
		try {
			Jsonb jsonb = JsonbBuilder.create();
			if (isFromCache()) {
				String toplists = jsonb.toJson(cache.getTopLists(), TopLists.class);
				return Response.ok(toplists).build();
			}
			TopLists topLists = userEJB.getTopPlayers();
			cache.setTopLists(topLists);
			String toplists = jsonb.toJson(topLists, TopLists.class);
			LOGGER.exiting("TopListsResource", " getTopPlayers");
			return Response.ok(toplists).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "TopListsResource failed", e);
		}
		LOGGER.exiting("TopListsResource", " getTopPlayers");
		return Response.serverError().build();
	}

	private boolean isFromCache() {
		if (cache.getTopLists() == null) {
			return false;
		}
		Instant now = Instant.now();
		Instant cacheInstant = cache.getTopLists().getFetchInstant();
		if (now.getEpochSecond() - cacheInstant.getEpochSecond() < CACHE_WAIT_TIME_SECONDS) {
			return true;
		}
		return false;
	}

}
