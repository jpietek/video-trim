package com.cloud.video.editor.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class StringUtils {

	public static String getRandomId() {
		SecureRandom random = new SecureRandom();
		String token = new BigInteger(128, random).toString(16);
		while (token.length() < 16) {
			token = "0" + token;
		}
		return token;
	}

}
