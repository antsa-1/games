package com.tauhka.portal.profile;

import java.io.Serializable;
import java.util.UUID;

import com.tauhka.portal.highscore.TopPlayer;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;

/**
 * @author antsa-1 from GitHub 22 Feb 2022
 * 
 *         Testing JPA here, users are created earlier using JDBC in
 *         UserEJB.java. ProfileText can contain SQLi, XSS, HTML-injections and
 *         fellows, this is definately "danger-alert" point of the application.
 **/

@Entity
@Table(name = "User")
public class UserProfile implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@JsonbTransient
	private UUID id;

	@Column(name = "profile_text")
	@JsonbProperty("profileText")
	@Max(value = 250, message = "Profiletext longer than expected")
	private String profileText;

	private TopPlayer topPlayer; // TODO naming..

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getProfileText() {
		return profileText;
	}

	public void setProfileText(String profileText) {
		this.profileText = profileText;
	}

	public TopPlayer getTopPlayer() {
		return topPlayer;
	}

	public void setTopPlayer(TopPlayer topPlayer) {
		this.topPlayer = topPlayer;
	}

}
