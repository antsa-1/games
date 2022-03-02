package com.tauhka.portal.util;

/**
 * @author antsa-1 from GitHub 2 Mar 2022
 **/

public class Validators {

	public static void validateUserName(String username) {

		if (username.isBlank()) {
			throw new IllegalArgumentException("Username is not allowed to be blank");
		}
		if (username.length() < Constants.USER_NAME_MIN_LENGTH) { // Min length subject to change? vs. blank check
			throw new IllegalArgumentException("Username is not allowed to be less than " + Constants.USER_NAME_MIN_LENGTH);
		}

		if (username.length() > Constants.USER_NAME_MAX_LENGTH) { // Min length subject to change? vs. blank check
			throw new IllegalArgumentException("Username is not allowed to be more than " + Constants.USER_NAME_MIN_LENGTH + "_" + username);
		}
	}

}
