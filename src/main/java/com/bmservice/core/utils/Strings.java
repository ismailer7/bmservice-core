package com.bmservice.core.utils;

import java.util.Random;

public class Strings {

    public static String getSaltString(final int length, final boolean letters, final boolean uppercase, final boolean numbers, final boolean specialCharacters) {
        String SALTCHARS = "";
        if (letters) {
            SALTCHARS += "abcdefghijklmnopqrstuvwxyz";
            if (uppercase) {
                SALTCHARS += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            }
        }
        if (numbers) {
            SALTCHARS += "1234567890";
        }
        if (specialCharacters) {
            SALTCHARS += "@\\\\/_*$&-#[](){}";
        }
        final StringBuilder salt = new StringBuilder();
        final Random rnd = new Random();
        while (salt.length() < length) {
            final int index = (int)(rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        final String saltStr = salt.toString();
        return saltStr;
    }

    public static String randomizeCase(final String str) {
        final Random rnd = new Random();
        final StringBuilder sb = new StringBuilder(str.length());
        for (final char c : str.toCharArray()) {
            sb.append(rnd.nextBoolean() ? Character.toLowerCase(c) : Character.toUpperCase(c));
        }
        return sb.toString();
    }

}
