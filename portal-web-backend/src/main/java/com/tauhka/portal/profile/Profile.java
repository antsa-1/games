package com.tauhka.portal.profile;

import java.time.Instant;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * @author antsa-1 from GitHub 2 Mar 2022
 **/


public class Profile {

	@JsonbProperty("user")
	private User user;
	@JsonbProperty("fetched")
	private Instant fetched;

	public Profile(User user) {
		super();
		this.user = user;
		fetched = Instant.now();

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
