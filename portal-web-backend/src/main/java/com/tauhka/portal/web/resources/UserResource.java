package com.tauhka.portal.web.resources;

import static com.tauhka.portal.util.Constants.LOG_PREFIX_PORTAL;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.tauhka.portal.ejb.UserEJB;
import com.tauhka.portal.login.LoginInput;
import com.tauhka.portal.login.LoginOutput;

import jakarta.ejb.DuplicateKeyException;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/user")

public class UserResource {
	private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());
	private static final String USER_STATUS_ACTIVE = "ACTIVE";
	@Inject
	private UserEJB userEJB;

	@POST
	@Path("/login")
	public Response login(String userAndPassWord) {
		LOGGER.entering(LOG_PREFIX_PORTAL + UserResource.class.getName(), " login");
		try {
			Thread.sleep(7000); // Login should not be too fast for security purposes -> adds delay
		} catch (InterruptedException e1) {
			LOGGER.fine(LOG_PREFIX_PORTAL + "UserResource: login thread woke up");
		}
		try {
			if (userAndPassWord == null || userAndPassWord.length() > 150) {
				throw new IllegalArgumentException("Login creds. error");
			}
			// TODO check later if direct Mapping in method parameter works
			Jsonb jsonb = JsonbBuilder.create();
			LoginInput login = jsonb.fromJson(userAndPassWord, LoginInput.class);
			LoginOutput user = userEJB.login(login.getUserName(), login.getPassword());
			if (user != null && user.getStatus().equals(USER_STATUS_ACTIVE)) {
				LOGGER.exiting(LOG_PREFIX_PORTAL + UserResource.class.getName(), " login");
				return Response.ok(jsonb.toJson(user)).build();
			} else {
				LOGGER.warning(LOG_PREFIX_PORTAL + UserResource.class.getName() + " Login attempt failed for:" + login.getUserName());
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, LOG_PREFIX_PORTAL + "User:login ", e);
		}

		LOGGER.exiting(LOG_PREFIX_PORTAL + UserResource.class.getName(), " login");
		return Response.status(Status.BAD_REQUEST).build();
	}

	@POST
	@Path("/register")
	public Response register(String userAndPassWordAndEmail) {
		LOGGER.entering(LOG_PREFIX_PORTAL + UserResource.class.getName(), " register");
		try {
			Thread.sleep(7000); // Registration should not be too fast -> artificial delay
		} catch (InterruptedException e1) {
			LOGGER.fine(LOG_PREFIX_PORTAL + "UserResource: login thread wokeup");
		}
		try {
			// TODO check later if direct Mapping in method parameter works
			Jsonb jsonb = JsonbBuilder.create();
			LoginInput login = jsonb.fromJson(userAndPassWordAndEmail, LoginInput.class);
			LoginOutput user = userEJB.register(login.getUserName(), login.getPassword(), login.getEmail());
			if (user != null) {
				LOGGER.exiting(LOG_PREFIX_PORTAL + UserResource.class.getName(), " register");
				return Response.ok(jsonb.toJson(user)).build();
			}
		} catch (DuplicateKeyException de) {
			LOGGER.exiting(LOG_PREFIX_PORTAL + UserResource.class.getName(), " register2");
			return Response.status(Status.NOT_ACCEPTABLE).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, LOG_PREFIX_PORTAL + "User:register ", e);
		}
		LOGGER.exiting(LOG_PREFIX_PORTAL + UserResource.class.getName(), " register3");
		return Response.status(Status.BAD_REQUEST).build();
	}

	@POST
	@Path("/logout")
	public Response logout(String token) {
		LOGGER.entering("User", " logout");

		try {
			// TODO check later if direct Mapping in method parameter works
			Jsonb jsonb = JsonbBuilder.create();
			LoginInput login = jsonb.fromJson(token, LoginInput.class);
			userEJB.logout(login.getToken());
			return Response.ok().build();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "User logout ", e);
		}
		return Response.ok().build();
	}

}
