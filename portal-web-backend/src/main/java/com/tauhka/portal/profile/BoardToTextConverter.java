package com.tauhka.portal.profile;

import com.tauhka.games.core.GameMode;

import jakarta.persistence.AttributeConverter;

/**
 * @author antsa-1 from GitHub 2 Mar 2022
 **/

public class BoardToTextConverter implements AttributeConverter<String, Integer> {

	@Override
	public Integer convertToDatabaseColumn(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertToEntityAttribute(Integer dbData) {
		return GameMode.getBoardDescription(dbData);
	}

}
