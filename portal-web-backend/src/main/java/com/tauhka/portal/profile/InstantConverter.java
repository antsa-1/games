package com.tauhka.portal.profile;

import java.sql.Timestamp;
import java.time.Instant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * @author antsa-1 from GitHub 1 Mar 2022
 **/

@Converter
public class InstantConverter implements AttributeConverter<Instant, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(Instant instant) {
		return Timestamp.from(instant);
	}

	@Override
	public Instant convertToEntityAttribute(Timestamp dbData) {
		return dbData.toInstant();
	}

}
