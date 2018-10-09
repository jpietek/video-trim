package com.cloud.video.editor.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class CryptUtils {

	private final static Logger LOGGER = Logger.getLogger(CryptUtils.class.getName());

	public static String generateId() {
		SecureRandom random = new SecureRandom();	
		String token = new BigInteger(128, random).toString(16);
		while (token.length() < 32) {
			token = "0" + token;
		}
		return token;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static int[] pack(byte[] bytes) {
		int n = bytes.length >> 1;
		int[] packed = new int[n];
		for (int i = 0; i < n; ++i) {
			int i2 = i << 1;
			int b1 = bytes[i2] & 0xff;
			int b2 = bytes[i2 + 1] & 0xff;
			packed[i] = (b1 << 8) | b2;
		}
		return packed;
	}
}

