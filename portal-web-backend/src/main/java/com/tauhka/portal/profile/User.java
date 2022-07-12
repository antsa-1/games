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

	@Transient
	@JsonbProperty("yatzyClassics")
	private List<Game> yatzyClassics;

	@Transient
	@JsonbProperty("yatzyFasts")
	private List<Game> yatzyFasts;

	@Transient
	@JsonbProperty("yatzySupers")
	private List<Game> yatzySupers;

	@Transient
	@JsonbProperty("yatzyHypers")
	private List<Game> yatzyHypers;

	@Transient
	@JsonbProperty("eightBalls")
	private List<Game> eightBalls;

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

	public Instant getMemberSince() {
		return memberSince;
	}

	public void setMemberSince(Instant memberSince) {
		this.memberSince = memberSince;
	}

	public List<Game> getConnectFours() {
		return connectFours;
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

	public List<Game> getYatzyClassics() {
		return yatzyClassics;
	}

	public void setYatzyClassics(List<Game> yatzyClassics) {
		this.yatzyClassics = yatzyClassics;
	}

	public List<Game> getYatzyFasts() {
		return yatzyFasts;
	}

	public void setYatzyFasts(List<Game> yatzyFasts) {
		this.yatzyFasts = yatzyFasts;
	}

	public List<Game> getYatzySupers() {
		return yatzySupers;
	}

	public void setYatzySupers(List<Game> yatzySupers) {
		this.yatzySupers = yatzySupers;
	}

	public List<Game> getYatzyHypers() {
		return yatzyHypers;
	}

	public void setYatzyHypers(List<Game> yatzyHypers) {
		this.yatzyHypers = yatzyHypers;
	}

	public List<Game> getEightBalls() {
		return eightBalls;
	}

	public void setEightBalls(List<Game> eightBalls) {
		this.eightBalls = eightBalls;
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
