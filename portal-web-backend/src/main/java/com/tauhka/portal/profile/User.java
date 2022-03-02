package com.tauhka.portal.profile;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * @author antsa-1 from GitHub 22 Feb 2022 <br>
 * 
 **/

@Entity
@Table(name = "user")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id")
	@JsonbTransient
	private String id;

	@Column(name = "name")
	@JsonbProperty("name")
	private String name;

	@Column(name = "profile_text")
	@JsonbProperty("profileText")
	private String profileText;

	@Convert(converter = InstantConverter.class)
	@Column(name = "created")
	@JsonbProperty("memberSince")
	private Instant memberSince;

	@Transient
	@JsonbProperty("connectFours")
	private List<Game> connectFours;

	@Transient
	@JsonbProperty("tictactoes")
	private List<Game> ticatactoes;

	public String getProfileText() {
		return profileText;
	}

	public void setProfileText(String profileText) {
		this.profileText = profileText;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Game> getConnectFours() {
		return connectFours;
	}

	public Instant getMemberSince() {
		return memberSince;
	}

	public void setMemberSince(Instant memberSince) {
		this.memberSince = memberSince;
	}

	public void setConnectFours(List<Game> connectFours) {
		this.connectFours = connectFours;
	}

	public List<Game> getTicatactoes() {
		return ticatactoes;
	}

	public void setTicatactoes(List<Game> ticatactoes) {
		this.ticatactoes = ticatactoes;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
