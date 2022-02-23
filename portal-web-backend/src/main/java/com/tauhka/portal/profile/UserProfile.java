package com.tauhka.portal.profile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;

/**
 * @author antsa-1 from GitHub 22 Feb 2022 <br>
 *         TODO missing Testing JPA here, users are created earlier using JDBC
 *         in UserEJB.java. ProfileText can contain SQLi, XSS, HTML-injections
 *         and fellows, this is definately "danger-alert" point of the
 *         application. TODO missing
 **/

// TODO THIS CLASS IS MISSING validations to prevent sql-injections/XSS and such.. TODO 
@Entity
@Table(name = "Users")

public class UserProfile implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "UserName")
	private String userName;

	@Column(name = "profile_text")
	@JsonbProperty("profileText")
	@Max(value = 250, message = "Profiletext longer than expected")
	private String profileText;

	@Column(name = "Created")
	@JsonbTransient
	private Timestamp created; // Timestamp doesn't work well with JSON-conversion

	@Transient
	@JsonbProperty("memberSince")
	private Instant memberSince;

	public String getProfileText() {
		return profileText;
	}

	public void setProfileText(String profileText) {
		this.profileText = profileText;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Instant getMemberSince() {
		return memberSince;
	}

	public void setMemberSince(Instant memberSince) {
		this.memberSince = memberSince;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
}
