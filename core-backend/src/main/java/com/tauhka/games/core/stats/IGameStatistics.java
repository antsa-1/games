package com.tauhka.games.core.stats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/**
 * 
 * Statistics Qualifier (just testing qualifiers)..
 *
 */
@Qualifier
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface IGameStatistics {

}
